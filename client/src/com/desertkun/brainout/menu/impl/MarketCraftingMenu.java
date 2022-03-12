package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.ContentND;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Shader;
import com.desertkun.brainout.content.active.RealEstateItem;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.RecipeComponent;
import com.desertkun.brainout.content.consumable.Resource;
import com.desertkun.brainout.content.consumable.impl.RealEstateItemConsumableItem;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.NotifyEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.utils.ContentImage;
import com.desertkun.brainout.utils.MarketUtils;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.services.MarketService;
import org.json.JSONObject;

import java.util.List;

public class MarketCraftingMenu extends MarketItemsCategoryMenu
{
    private final ButtonGroup<Button> buttons;
    private TextButton craft;
    private Table itemsToCraft;
    private ScrollPane scrollPane;
    private boolean crafting = false;

    public MarketCraftingMenu(PlayerData playerData)
    {
        super(playerData, "rs");

        resourcesAvailable = new Table();
        resourcesAvailable.align(Align.right);
        buttons = new ButtonGroup<>();
        buttons.setMaxCheckCount(1);
        buttons.setMinCheckCount(0);
    }

    private final Table resourcesAvailable;

    @Override
    protected MarketItemsInventoryPanel createTargetPanel()
    {
        MarketItemsInventoryPanel items = super.createTargetPanel();

        if (items != null)
        {
            items.setItemsLoadedHook(this::refreshResources);
        }

        return items;
    }

    private void refreshResources(List<MarketService.MarketItemEntry> entries)
    {
        resourcesAvailable.clearChildren();

        ObjectMap<Resource, Integer> amounts = new ObjectMap<>();

        for (MarketService.MarketItemEntry entry : entries)
        {
            ConsumableRecord r = MarketUtils.MarketObjectToConsumableRecord(
                entry.name, entry.payload, entry.amount);
            if (r == null)
                continue;

            if (!(r.getItem().getContent() instanceof Resource))
                continue;
            Resource rr = ((Resource) r.getItem().getContent());
            amounts.put(rr, amounts.get(rr, 0) + r.getAmount());
        }

        itemsToCraft.clearChildren();
        renderItemsToCraft(itemsToCraft, amounts);

        for (ObjectMap.Entry<Resource, Integer> amount : amounts)
        {
            Table r = new Table(BrainOutClient.Skin);
            r.setBackground("form-default");

            Content content = amount.key;
            if (content.hasComponent(IconComponent.class))
            {
                IconComponent iconComponent = content.getComponent(IconComponent.class);

                TextureAtlas.AtlasRegion iconRegion = iconComponent.getIcon("icon-medium");

                if (iconRegion == null)
                {
                    throw new RuntimeException("Icon " + iconComponent.getItemName() + " cannot be found on content " +
                            content.getID());
                }

                Image iconImage = new Image(iconRegion);
                iconImage.setScaling(Scaling.none);

                r.add(iconImage).expand().fill().row();
            }

            Label a = new Label(String.valueOf(amount.value), BrainOutClient.Skin, "title-yellow");
            a.setAlignment(Align.right | Align.bottom);
            a.setTouchable(Touchable.disabled);
            a.setPosition(4, 4);
            a.setSize(56, 56);

            r.addActor(a);

            Tooltip.RegisterToolTip(r, amount.key.getTitle().get() + ": " + amount.value, MarketCraftingMenu.this);

            resourcesAvailable.add(r).size(64, 64);
        }
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-clan");
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    protected void generateOtherPanels(Table data)
    {
        Table a = new Table();

        {
            Table title = new Table(BrainOutClient.Skin);
            title.setBackground("form-gray");
            title.add(new Label(L.get("MENU_WORKBENCH"),  BrainOutClient.Skin, "title-yellow")).row();
            a.add(title).expandX().fillX().row();
        }

        {
            Table contents = new Table(BrainOutClient.Skin);
            contents.setBackground("form-default");

            itemsToCraft = new Table();
            itemsToCraft.align(Align.left | Align.top);

            scrollPane = new ScrollPane(itemsToCraft, BrainOutClient.Skin, "scroll-default");
            scrollPane.setFadeScrollBars(false);
            scrollPane.setForceScroll(false, true);
            scrollPane.setScrollbarsVisible(true);
            contents.add(scrollPane).expand().fill().minWidth(400).maxHeight(400).row();

            a.add(contents).expand().fill().row();
        }

        {
            Table buttons = new Table();
            buttons.align(Align.right);

            craft = new TextButton(L.get("MENU_CREATE"), BrainOutClient.Skin, "button-gray");
            craft.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    if (crafting)
                    {
                        return;
                    }

                    if (MarketCraftingMenu.this.buttons.getAllChecked().size == 0)
                    {
                        Menu.playSound(MenuSound.denied);
                        return;
                    }

                    RealEstateItem selected =
                        ((RealEstateItem) MarketCraftingMenu.this.buttons.getAllChecked().first().getUserObject());

                    JSONObject args = new JSONObject();
                    args.put("item", selected.getID());

                    BrainOutClient.SocialController.sendRequest("assemble_rs_item", args,
                        new SocialController.RequestCallback()
                    {
                        @Override
                        public void success(JSONObject response)
                        {
                            craft.setText("");
                            crafting = true;

                            ProgressBar progressBar = new ProgressBar(1, 15, 1, false,
                                BrainOutClient.Skin, "progress-score");

                            craft.addActor(progressBar);
                            progressBar.setBounds(16, 24, 192 - 32, 16);

                            craft.addAction(Actions.sequence(
                                Actions.repeat(15, Actions.sequence(
                                    Actions.delay(0.05f),
                                    Actions.run(() -> progressBar.setValue(progressBar.getValue() + 1))
                                )),
                                Actions.run(() ->
                                {
                                    crafting = false;
                                    progressBar.remove();
                                    craft.setDisabled(true);
                                    craft.setStyle(BrainOutClient.Skin.get("button-gray", TextButton.TextButtonStyle.class));
                                    MarketCraftingMenu.this.buttons.uncheckAll();
                                    craft.setText(L.get("MENU_CREATE"));
                                    targetPanel.refresh();
                                })
                            ));

                            Menu.playSound(MenuSound.contentOwnedEx);
                        }

                        @Override
                        public void error(String reason)
                        {
                            if (Log.ERROR) Log.error(reason);
                            Menu.playSound(MenuSound.denied);
                        }
                    });
                }
            });

            buttons.add(craft).size(192, 64);

            a.add(buttons).expandX().fill().row();
        }

        data.add(a).expandY().fillY().top().pad(10);
    }

    private void renderItemsToCraft(Table itemsToCraft, ObjectMap<Resource, Integer> resources)
    {
        Shader grayShader = ((Shader) BrainOut.ContentMgr.get("shader-grayed"));

        Array<RealEstateItem> items = BrainOut.ContentMgr.queryContent(RealEstateItem.class, realEstateItem ->
            realEstateItem.hasComponent(RecipeComponent.class));

        buttons.clear();

        int i = 0;

        for (RealEstateItem realEstateItem : items)
        {
            RecipeComponent recipeComponent = realEstateItem.getComponent(RecipeComponent.class);

            boolean unlocked = true;

            if (recipeComponent.getRequiredStat() != null)
            {
                if (BrainOutClient.ClientController.getUserProfile().getInt(recipeComponent.getRequiredStat(), 0 ) <= 0)
                {
                    unlocked = false;
                }
            }

            Button btn = new Button(BrainOutClient.Skin,
                unlocked && recipeComponent.isThereEnough(resources) ? "button-green-checkable" : "button-checkable");

            if (unlocked)
            {
                ContentImage.RenderImage(realEstateItem, btn, 1);
            }
            else
            {
                Table wrap = new Table();
                ContentImage.RenderImage(realEstateItem, wrap, 1);
                ShaderedActor shaderedActor = new ShaderedActor(wrap, grayShader);
                wrap.setBounds(0, 0, 192, 64);
                shaderedActor.setFillParent(true);
                shaderedActor.setTouchable(Touchable.disabled);
                btn.addActor(shaderedActor);
            }

            itemsToCraft.add(btn).size(192, 64).pad(4);

            buttons.add(btn);

            if (unlocked && !recipeComponent.isThereEnough(resources))
            {
                btn.addActor(new ButtonProgressBar(
                    recipeComponent.getMatchingPercentage(resources), 100, BrainOutClient.Skin, "progress-parts"
                ));
            }
            btn.setUserObject(realEstateItem);

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    if (buttons.getAllChecked().size == 0)
                    {
                        craft.setDisabled(true);
                        craft.setStyle(BrainOutClient.Skin.get("button-gray", TextButton.TextButtonStyle.class));
                    }
                    else
                    {
                        boolean enough = recipeComponent.isThereEnough(resources);
                        if (recipeComponent.getRequiredStat() != null)
                        {
                            enough = enough &&
                                BrainOutClient.ClientController.getUserProfile().getInt(recipeComponent.getRequiredStat(), 0) >= 1;
                        }

                        if (enough)
                        {
                            Menu.playSound(MenuSound.select);
                        }
                        else
                        {
                            buttons.uncheckAll();
                            Menu.playSound(MenuSound.denied);
                        }

                        craft.setDisabled(!enough);
                        craft.setStyle(BrainOutClient.Skin.get(enough ? "button-green" : "button-gray",
                            TextButton.TextButtonStyle.class));
                    }
                }
            });

            {
                ConsumableRecord r = new ConsumableRecord(
                    new RealEstateItemConsumableItem(realEstateItem), 1, -1);

                Gdx.app.postRunnable(() ->
                    Tooltip.RegisterToolTip(btn,
                        new InventoryPanel.RecipeItemTooltip(r, false, resources), MarketCraftingMenu.this));
            }

            i++;
            if (i % 2 == 0)
            {
                itemsToCraft.row();
            }
        }
    }

    @Override
    public void onInit()
    {
        super.onInit();

        setScrollFocus(scrollPane);
    }

    @Override
    protected void addUserPanel()
    {
        MenuHelper.AddCloseButton(this, this::close);

        resourcesAvailable.setBounds(32, BrainOutClient.getHeight() - 84, BrainOutClient.getWidth() - 212 - 64, 64);
        addActor(resourcesAvailable);
    }

    private void close()
    {
        pop();
    }
}
