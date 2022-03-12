package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.msg.client.ContentActionMsg;
import com.desertkun.brainout.content.InventoryContent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.tutorial.RichTutorialMenu;
import com.desertkun.brainout.menu.tutorial.Tutorials;
import com.desertkun.brainout.menu.ui.BadgeButton;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.MenuHelper;
import com.desertkun.brainout.online.UserProfile;

public class ContainersMenu extends Menu
{
    private final UserProfile profile;

    private Label description;
    private Label title;

    private OrderedMap<InventoryContent, Integer> items;
    private InventoryContent selectedItem;
    private ScrollPane itemsPane;
    private int totalAmount;
    private Table cases;

    public ContainersMenu(UserProfile profile)
    {
        this.profile = profile;
    }

    @Override
    public void onInit()
    {
        updateProfile();

        super.onInit();

        if (BrainOut.OnlineEnabled())
        {
            for (ObjectMap.Entry<InventoryContent, Integer> entry : items)
            {
                InventoryContent inventoryContent = entry.key;

                if (entry.value > 0 && inventoryContent.getID().equals(Constants.User.DAILY_CONTAINER))
                {
                    showTutorial();

                    break;
                }
            }
        }
        else
        {
            showTutorial();
        }
    }

    private void showTutorial()
    {
        if (!Tutorials.IsFinished("daily"))
        {
            Gdx.app.postRunnable(() ->
            {
                pushMenu(new RichTutorialMenu(L.get("MENU_TUTORIAL_DAILY"), () -> {
                    Tutorials.Done("daily");
                }));
            });
        }
    }

    @Override
    public void initTable()
    {
        super.initTable();

        MenuHelper.AddCloseButton(this, this::pop);
    }

    private void updateProfile()
    {
        this.items = profile.getItemsOf(InventoryContent.class);
        this.selectedItem = null;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        addMain(data);
        addButtons(data);

        if (items.size > 0)
        {
            for (ObjectMap.Entry<InventoryContent, Integer> entry : items)
            {
                if (entry.value > 0)
                {
                    selectItem(entry.key);
                    break;
                }
            }
        }

        return data;
    }

    private void addMain(Table data)
    {
        Table main = new Table();
        main.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

        this.cases = new Table();
        cases.align(Align.bottom | Align.center);
        cases.setBackground(new TextureRegionDrawable(BrainOutClient.getRegion("standard-case-bg")));

        Table elements = new Table();

        this.title = new Label("", BrainOutClient.Skin, "title-small");
        title.setAlignment(Align.center);
        title.setWrap(true);
        cases.add(title).expand().fillX().top().pad(36).row();

        this.itemsPane = new ScrollPane(elements, BrainOutClient.Skin, "scroll-default");
        cases.add(itemsPane).size(512, 96).padBottom(14).row();

        updateElements(elements);

        main.add(cases).size(640, 302).row();

        this.description = new Label(L.get("CASE_STANDARD_DESC"),
            BrainOutClient.Skin, "title-small");
        this.description.setWrap(true);
        description.setAlignment(Align.center);

        main.add(description).pad(16).expandX().fillX().row();

        data.add(main).size(704, 480).row();
    }

    private void updateElements(Table elements)
    {
        itemsPane.setVisible(items.size > 0);

        ButtonGroup<BadgeButton> buttonGroup = new ButtonGroup<>();
        buttonGroup.setMinCheckCount(1);

        this.totalAmount = 0;
        int typesAmount = 0;

        for (ObjectMap.Entry<InventoryContent, Integer> item : items)
        {
            InventoryContent content = item.key;
            int amount = item.value;

            if (amount <= 0)
            {
                continue;
            }

            totalAmount += amount;
            typesAmount++;

            IconComponent iconComponent = content.getComponent(IconComponent.class);
            BadgeButton button = new BadgeButton(iconComponent.getIcon("big-icon"), amount, true);

            elements.add(button).size(128, 96);

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    selectItem(content);
                }
            });

            buttonGroup.add(button);
        }

        if (typesAmount < 4)
        {
            for (int i = 0; i < 4 - typesAmount; i++)
            {
                BadgeButton button = new BadgeButton();
                elements.add(button).size(128, 96);
            }
        }
    }

    private void selectItem(InventoryContent item)
    {
        selectedItem = item;

        if (item.hasComponent(IconComponent.class))
        {
            IconComponent icon = item.getComponent(IconComponent.class);

            if (icon.hasIcon("bg"))
            {
                cases.setBackground(new TextureRegionDrawable(icon.getIcon("bg")));
            }
        }

        this.title.setText(item.getTitle().get());
        this.description.setText(item.getDescription().get());
    }

    public void open(InventoryContent content)
    {
        if (content == null)
            return;

        GameState topState = BrainOutClient.getInstance().topState();
        topState.pushMenu(new OpenCaseMenu()
        {
            @Override
            public void onRelease()
            {
                super.onRelease();

                updateProfile();
                ContainersMenu.this.reset();
            }
        });

        BrainOutClient.ClientController.sendTCP(new ContentActionMsg(content, ContentActionMsg.Action.open));
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }

    private void addButtons(Table data)
    {
        Table buttons = new Table();
        buttons.align(Align.right);

        if (BrainOut.OnlineEnabled())
        {
            TextButton promo = new TextButton(L.get("MENU_ENTER_PROMO_BTN"),
                    BrainOutClient.Skin, "button-default");

            promo.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    popMeAndPushMenu(new PromoCodeMenu());
                }
            });

            buttons.add(promo).size(160, 64).expandX().left();
        }

        if (totalAmount > 0)
        {
            TextButton open = new TextButton(L.get("MENU_OPEN_CASE"),
                    BrainOutClient.Skin, "button-important");

            open.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    open(selectedItem);
                }
            });

            buttons.add(open).size(160, 64);
        }

        if (BrainOutClient.Env.storeEnabled())
        {
            TextButton store = new TextButton(L.get("MENU_STORE"),
                    BrainOutClient.Skin, "button-green");

            store.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    pushMenu(new StoreMenu());
                }
            });

            buttons.add(store).size(160, 64);
        }

        data.add(buttons).expandX().fillX().right().row();
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        updateProfile();
        reset();
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
