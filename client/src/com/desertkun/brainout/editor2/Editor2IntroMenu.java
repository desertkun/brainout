package com.desertkun.brainout.editor2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.GameUser;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.http.ContentClient;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.WaitLoadingMenu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.*;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;

public class Editor2IntroMenu extends Menu
{
    private Table contents;
    private Table loadingBlock;

    public Editor2IntroMenu()
    {
    }

    @Override
    public void onInit()
    {
        super.onInit();

        MenuHelper.AddCloseButton(this, this::exit);
    }

    private void exit()
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        BrainOutClient.ClientController.disconnect(DisconnectReason.leave, () ->
        {
            BrainOutClient.Env.gameCompleted();

            BrainOutClient.getInstance().popState();
            BrainOutClient.getInstance().initMainMenu().loadPackages();
        });
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();
        contents = new Table();
        contents.align(Align.top | Align.center);
        ScrollPane maps = new ScrollPane(contents, BrainOutClient.Skin, "scroll-default");

        renderLoading();
        requestMaps();

        data.add(maps).expand().fillY().center().width(670).row();

        return data;
    }

    @Override
    public boolean escape()
    {
        exit();

        return true;
    }

    private void clearContents()
    {
        contents.clearChildren();

        if (loadingBlock != null)
        {
            loadingBlock.clear();
            loadingBlock = null;
        }
    }

    private void renderLoading()
    {
        loadingBlock = new Table();

        loadingBlock.add(new Label(L.get("MENU_LOADING"), BrainOutClient.Skin, "title-messages-white"));
        loadingBlock.add(new LoadingBlock()).padLeft(16);

        loadingBlock.setBounds(getWidth() - 288, 32, 224, 16);

        addActor(loadingBlock);
    }

    private void requestMaps()
    {
        if (!BrainOutClient.Env.getGameUser().hasWorkshop())
        {
            renderFailed("Workshop is not implemented");
            return;
        }

        GameUser.WorkshopItemsQuery query = BrainOutClient.Env.getGameUser().queryMyPublishedWorkshopItems();

        query.addRequiredTag("map");
        query.sendQuery(new GameUser.WorkshopItemsQueryCallback()
        {
            @Override
            public void success(Queue<GameUser.WorkshopItem> items, int results, int totalResults)
            {
                renderMaps(items);
            }

            @Override
            public void failed(String reason)
            {
                renderFailed(L.get("MENU_ERROR_TRY_AGAIN") + "\n\nReason: " + reason);
            }
        });
    }

    private void renderMaps(Queue<GameUser.WorkshopItem> items)
    {
        clearContents();

        Label text = new Label(L.get("MENU_EDITOR_SELECT_MAP"), BrainOutClient.Skin, "title-yellow");
        contents.add(text).colspan(2).expandX().center().pad(32).row();

        // create new one

        {
            Table entry = new Table();

            {
                Table header = new Table(BrainOutClient.Skin);
                header.setBackground("form-gray");

                Label title = new Label(L.get("EDITOR_NEW_MAP"), BrainOutClient.Skin, "title-small");
                title.setAlignment(Align.center);
                title.setWrap(true);

                header.add(title).expandX().row();
                entry.add(header).expandX().fillX().height(32).row();
            }

            {
                Button btn = new Button(BrainOutClient.Skin, "button-notext");

                Image add = new Image(BrainOutClient.Skin, "icon-add");
                add.setTouchable(Touchable.disabled);
                btn.add(add);

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        pushMenu(new Editor2CreateMapMenu());
                    }
                });

                entry.add(btn).size(320, 180).row();
            }

            contents.add(entry).pad(8);
        }

        int i = 1;

        for (GameUser.WorkshopItem item : items)
        {
            Table entry = new Table();
            contents.add(entry).pad(8);

            {
                Table header = new Table(BrainOutClient.Skin);
                header.setBackground("form-gray");

                Label title = new Label(item.getTitle(), BrainOutClient.Skin, "title-small");
                title.setAlignment(Align.center);
                title.setWrap(true);

                header.add(title).expandX().row();
                entry.add(header).size(320, 32).row();
            }

            {
                Button btn = new Button(BrainOutClient.Skin, "button-notext");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        openMap(item);
                    }
                });

                Image image = new Image();

                if (item.getPreviewURL() != null)
                {
                    Avatars.Get(item.getPreviewURL(), (has, avatar) ->
                    {
                        if (has)
                        {
                            image.setDrawable(new TextureRegionDrawable(avatar));
                        }
                    });
                }

                btn.add(image).expand().fill().row();

                {
                    Button workshopBtn = new Button(BrainOutClient.Skin, "button-notext");

                    Image steam = new Image(BrainOutClient.Skin, "steam-icon");
                    workshopBtn.add(steam);

                    workshopBtn.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            BrainOutClient.Env.openURI(item.getWebURL());
                        }
                    });

                    workshopBtn.setBounds(0, 0, 48, 48);

                    btn.addActor(workshopBtn);
                }

                {
                    Label date = new Label(DateFormat.getInstance().format(item.getTimeUpdated()), BrainOutClient.Skin,
                        "title-small");
                    date.setAlignment(Align.right);
                    date.setBounds(96, 4, 220, 16);
                    date.setTouchable(Touchable.disabled);

                    btn.addActor(date);
                }

                entry.add(btn).size(320, 180).row();
            }

            i++;

            if (i % 2 == 0)
            {
                contents.row();
            }
        }
    }

    private void openMap(GameUser.WorkshopItem item)
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();

        args.put("workshop_id", item.getID());
        args.put("time_updated", item.getTimeUpdated());

        BrainOutClient.SocialController.sendRequest("editor2_open_map", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(reason));
            }
        });
    }


    @Override
    public void onRelease()
    {
        super.onRelease();

        Avatars.Reset();
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-loading");
    }

    private void renderFailed(String reason)
    {
        clearContents();

        Label text = new Label(reason, BrainOutClient.Skin, "title-red");
        contents.add(text).expandX().center().padTop(64).row();
    }
}
