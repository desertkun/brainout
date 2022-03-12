package com.desertkun.brainout.editor2;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.content.Background;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.WaitLoadingMenu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import org.json.JSONObject;

public class Editor2CreateMapMenu extends Menu
{
    private SelectBox<Constants.Editor.MapSize> mapSize;
    private SelectBox<BackgroundWrapper> mapBackground;
    private TextButton createButton;

    public class BackgroundWrapper
    {
        private Background background;

        public BackgroundWrapper(Background background)
        {
            this.background = background;
        }

        @Override
        public String toString()
        {
            return background.getTitle().get();
        }

        public String getID()
        {
            return background.getID();
        }
    }

    public Editor2CreateMapMenu()
    {

    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("EDITOR_NEW_MAP"), BrainOutClient.Skin, "title-yellow");
            header.add(title).row();

            data.add(header).size(462, 32).row();
        }

        {
            Table contents = new Table(BrainOutClient.Skin);
            contents.setBackground("form-default");
            contents.align(Align.center);

            renderContents(contents);

            data.add(contents).size(464, 304).row();
        }

        {
            Table buttons = new Table();

            {
                TextButton cancel = new TextButton(L.get("MENU_CANCEL"), BrainOutClient.Skin, "button-default");

                cancel.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        close();
                    }
                });

                buttons.add(cancel).expandX().fillX().uniformX().height(64);
            }

            {
                createButton = new TextButton(L.get("MENU_CREATE"), BrainOutClient.Skin, "button-green");

                createButton.addListener(new ClickOverListener()
                {

                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        create();
                    }
                });

                buttons.add(createButton).expandX().fillX().uniformX().height(64);
            }

            data.add(buttons).expandX().fillX().row();
        }

        return data;
    }

    private void renderContents(Table contents)
    {
        {
            Label title = new Label(L.get("MENU_MAP_SIZE"), BrainOutClient.Skin, "title-small");
            contents.add(title).pad(8).row();

            mapSize = new SelectBox<>(BrainOutClient.Skin, "select-badged-yellow");
            mapSize.setAlignment(Align.center);
            mapSize.getList().setAlignment(Align.center);

            mapSize.setItems(Constants.Editor.SIZES);
            mapSize.setSelected(Constants.Editor.SIZES.get(0));

            contents.add(mapSize).size(256, 32).pad(8).row();
        }

        {
            Label title = new Label(L.get("MENU_MAP_BACKGROUND"), BrainOutClient.Skin, "title-small");
            contents.add(title).pad(8).row();

            Array<BackgroundWrapper> items = new Array<>();

            mapBackground = new SelectBox<>(BrainOutClient.Skin, "select-badged-yellow");
            mapBackground.setAlignment(Align.center);
            mapBackground.getList().setAlignment(Align.center);

            BrainOutClient.ContentMgr.iterateContent(Background.class,
                background ->
            {
                items.add(new BackgroundWrapper(background));
                return false;
            });

            mapBackground.setItems(items);
            mapBackground.setSelected(items.get(0));

            contents.add(mapBackground).size(256, 32).pad(8).row();
        }
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    private void create()
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();

        args.put("size", mapSize.getSelected().getID());
        args.put("background", mapBackground.getSelected().getID());

        BrainOutClient.SocialController.sendRequest("editor2_create", args,
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

                pushMenu(new AlertPopup(L.get("MENU_ONLINE_ERROR", L.get(reason))));
            }
        });
    }

    private void close()
    {
        pop();
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }
}
