package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.http.ContentClient;
import com.desertkun.brainout.common.msg.client.editor.CreateMapMsg;
import com.desertkun.brainout.common.msg.client.editor.GetMapListMsg;
import com.desertkun.brainout.common.msg.client.editor.LoadMapMsg;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.MapListReceivedEvent;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.ExitPopup;
import com.desertkun.brainout.menu.popups.YesNoInputPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class EmptyStateMenu extends FormMenu implements EventReceiver
{
    public class NewMapMenu extends YesNoInputPopup
    {
        private TextField widthInput;
        private TextField heightInput;

        public NewMapMenu(String text, String value)
        {
            super(text, value);
        }

        @Override
        public boolean lockRender()
        {
            return true;
        }

        @Override
        protected void initContent(Table data)
        {
            super.initContent(data);

            Table sizes = new Table();

            Label widthLabel = new Label("Width:", BrainOutClient.Skin, "title-small");
            Label heightLabel = new Label("Height:", BrainOutClient.Skin, "title-small");

            widthInput = new TextField("4", BrainOutClient.Skin, "edit-default");
            heightInput = new TextField("4", BrainOutClient.Skin, "edit-default");

            sizes.add(widthLabel).pad(8);
            sizes.add(widthInput).pad(8);
            sizes.add(heightLabel).pad(8);
            sizes.add(heightInput).pad(8);

            data.add(sizes).pad(10).fillX().expandX().height(35).row();
        }

        public int getMapWidth()
        {
            try
            {
                return Integer.valueOf(widthInput.getText());
            }
            catch (NumberFormatException ignored)
            {
                return 0;
            }
        }

        public int getMapHeight()
        {
            try
            {
                return Integer.valueOf(heightInput.getText());
            }
            catch (NumberFormatException ignored)
            {
                return 0;
            }
        }
    }

    public class OpenMapMenu extends FormMenu
    {
        private final String[] maps;

        public OpenMapMenu(String maps[])
        {
            this.maps = maps;
        }

        @Override
        public Table createUI()
        {
            Table data = super.createUI();

            for (String map : maps)
            {
                final TextButton mapButton = new TextButton(map, BrainOutClient.Skin, "button-yellow");
                mapButton.setUserObject(map);
                data.add(mapButton).size(256, 40).pad(10).row();

                mapButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        playSound(MenuSound.select);

                        String map = ((String) mapButton.getUserObject());

                        pop();

                        doLoadMap(map);
                    }
                });
            }

            TextButton cancelButton = new TextButton(L.get("MENU_CANCEL"),
                    BrainOutClient.Skin, "button-small");
            data.add(cancelButton).size(256, 40).pad(10).padTop(0).row();

            cancelButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    playSound(MenuSound.back);

                    pop();
                }
            });

            return data;
        }
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-loading");
    }

    @Override
    public void onInit()
    {
        BrainOutClient.EventMgr.subscribe(Event.ID.mapListReceived, this);

        super.onInit();
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.mapListReceived, this);
    }

    @Override
    public Table createUI()
    {
        Table data = super.createUI();

        Label notice = new Label(L.get("EDITOR_MODE_NOTICE"), BrainOutClient.Skin, "title-yellow");
        notice.setAlignment(Align.center);

        data.add(notice).colspan(2).pad(10).expandX().fillX().row();

        TextButton createMap = new TextButton(L.get("EDITOR_NEW_MAP"), BrainOutClient.Skin, "button-small");
        data.add(createMap).size(192, 64).pad(10);

        createMap.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);

                if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT))
                {
                    pushMenu(new NewMapMenu("Enter map name", "")
                    {
                        @Override
                        public void ok()
                        {
                            String mapName = getValue();
                            int mapWidth = getMapWidth(), mapHeight = getMapHeight();

                            if (!mapName.isEmpty() && mapWidth != 0 && mapHeight != 0)
                            {
                                doCreateMap(mapName, mapWidth, mapHeight);
                            }
                        }
                    });
                }
                else
                {
                    BrainOutClient.ClientController.sendTCP(new GetMapListMsg());
                }
            }
        });

        TextButton loadMap = new TextButton(L.get("EDITOR_OPEN_MAP"), BrainOutClient.Skin, "button-small");
        data.add(loadMap).size(192, 64).pad(10).row();

        loadMap.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                doOpenMap();
            }
        });

        return data;
    }

    private void doOpenMap()
    {
        String maps = System.getenv("BRAINOUT_MAPS");
        FileHandle directory;

        if (maps != null)
        {
            directory = Gdx.files.absolute(maps);
        }
        else
        {
            directory = Gdx.files.external("brainout-maps");
        }

        if (!directory.exists())
            directory.mkdirs();

        pushMenu(new SelectMapMenu(SelectMapMenu.Mode.open, directory, new SelectMapMenu.SelectCallback()
        {
            @Override
            public void selected(FileHandle map)
            {
                FileInputStream inputStream;

                File file = map.file();

                try
                {
                    inputStream = new FileInputStream(file);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                    openFailed();
                    return;
                }

                ObjectMap<String, String> headers = new ObjectMap<>();

                headers.put("X-Map-Name", map.nameWithoutExtension());

                ContentClient.upload("upload-map", inputStream,
                    file.length(), new ContentClient.UploadResult()
                {
                    @Override
                    public void success()
                    {
                        //
                    }

                    @Override
                    public void failed()
                    {
                        openFailed();
                    }

                }, headers);
            }

            @Override
            public void cancelled()
            {

            }
        }));
    }

    private void openFailed()
    {
        Gdx.app.postRunnable(() -> pushMenu(new AlertPopup(L.get("EDITOR_LOAD_ERROR"))));
    }

    @Override
    public boolean escape()
    {
        pushMenu(new ExitPopup());
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case mapListReceived:
            {
                MapListReceivedEvent ev = ((MapListReceivedEvent) event);

                mapListReceived(ev.maps);

                break;
            }
        }

        return false;
    }

    private void mapListReceived(String[] maps)
    {
        if (maps.length == 0)
        {
            pushMenu(new AlertPopup("No map to load."));
        }
        else
        {
            pushMenu(new OpenMapMenu(maps));
        }
    }

    private void doLoadMap(String map)
    {
        BrainOutClient.ClientController.sendTCP(new LoadMapMsg(map));
    }

    private void doCreateMap(String mapName, int mapWidth, int mapHeight)
    {
        BrainOutClient.ClientController.sendTCP(new CreateMapMsg(mapName, mapWidth, mapHeight));
    }
}
