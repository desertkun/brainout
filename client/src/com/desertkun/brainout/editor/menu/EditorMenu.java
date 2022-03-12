package com.desertkun.brainout.editor.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.http.ContentClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.common.editor.DeleteDimensionMsg;
import com.desertkun.brainout.common.editor.EditorProperty;
import com.desertkun.brainout.common.editor.NewDimensionMsg;
import com.desertkun.brainout.common.editor.ResizeMapMsg;
import com.desertkun.brainout.common.editor.props.get.EditorGetMapPropertiesMsg;
import com.desertkun.brainout.common.editor.props.set.EditorSetActivePropertiesMsg;
import com.desertkun.brainout.common.editor.props.set.EditorSetMapPropertiesMsg;
import com.desertkun.brainout.common.msg.client.editor.EditorActionMsg;
import com.desertkun.brainout.common.msg.server.ChatMsg;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.EditorMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.editor.EditorComponent;
import com.desertkun.brainout.editor.data.EditorWatcher;
import com.desertkun.brainout.editor.modes.ActivesEditorMode;
import com.desertkun.brainout.editor.modes.BlocksEditorMode;
import com.desertkun.brainout.editor.modes.EditorMode;
import com.desertkun.brainout.editor.widgets.LayersWidget;
import com.desertkun.brainout.events.ChatEvent;
import com.desertkun.brainout.gs.ActionPhaseState;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.*;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;

import java.io.*;
import java.util.List;

public class EditorMenu extends Menu
{
    private EditorMap map;
    private LayersWidget layersWidget;
    private ImageButton modeBlocks;

    public enum DragState
    {
        none,
        dragging,
        moving
    }

    private Vector2 touchPos, diffPos, sendPos;
    private int moveMultiplier;

    private EditorMode currentMode;
    private EditorMode.Mode editorMode;
    private EditorRegisterButton rootContext;
    private Label stats;
    private DragState dragState;

    private FileHandle editorFile;

    public EditorMenu()
    {
        this(null);
    }

    public EditorMenu(EditorMap map)
    {
        this.touchPos = new Vector2();
        this.diffPos = new Vector2();
        this.sendPos = new Vector2();
        this.moveMultiplier = 2;

        this.map = map;

        for (EditorMap editorMap : Map.All(EditorMap.class))
        {
            EditorComponent editorComponent = editorMap.getComponents().getComponent(EditorComponent.class);

            if (editorComponent != null)
            {
                editorComponent.setMenu(this);
            }

            if (map == null)
            {
                if (this.map == null)
                {
                    this.map = editorMap;
                }
                else
                {
                    if (editorMap.getDimension().equals("default"))
                    {
                        this.map = editorMap;
                    }
                }
            }
        }

        if (this.map == null)
        {
            throw new RuntimeException("No map");
        }

        Map.SetWatcher(this.map.getEditorWatcher());
    }

    private EditorWatcher getEditorWatcher()
    {
        return map.getEditorWatcher();
    }

    private EditorComponent getEditorComponent()
    {
        return map.getComponents().getComponent(EditorComponent.class);
    }

    @Override
    public boolean escape()
    {
        pushMenu(new ExitMenu());
        return true;
    }

    public boolean isMoveAlt()
    {
        return Gdx.input.isKeyPressed(Input.Keys.SPACE);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        if (super.touchDown(screenX, screenY, pointer, button))
        {
            return true;
        }

        switch (button)
        {
            case Input.Buttons.LEFT:
            {
                if (isMoveAlt())
                {
                    dragState = DragState.moving;
                    touchPos.set(screenX, screenY);
                    return true;
                }
                else
                {
                    dragState = DragState.dragging;
                    currentMode.touchDown(setMousePos(screenX, screenY));
                }

                break;
            }
        }

        return true;
    }

    private Vector2 setMousePos(int screenX, int screenY)
    {
        map.GetMouseScaleWatcher(screenX - BrainOutClient.getWidth() / 2f,
                - (screenY - BrainOutClient.getHeight() / 2f), sendPos);
        return sendPos;
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        switch (keyCode)
        {
            case Input.Keys.B:
            {
                modeBlocks.setChecked(true);
                setEditorMode(EditorMode.Mode.blocks);

                return true;
            }

            case Input.Keys.LEFT_BRACKET:
            {
                getEditorWatcher().setScale(Math.min(getEditorWatcher().getScale() * 1.25f, 4.0f));

                return true;
            }
            case Input.Keys.RIGHT_BRACKET:
            {
                getEditorWatcher().setScale(Math.max(getEditorWatcher().getScale() / 1.25f, 0.5f));

                return true;
            }
            case Input.Keys.EQUALS:
            {
                getEditorWatcher().setScale(1);

                return true;
            }
        }

        if (currentMode.keyDown(keyCode))
        {
            return true;
        }

        return super.keyDown(keyCode);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        if (super.touchDragged(screenX, screenY, pointer))
        {
            return false;
        }

        EditorWatcher watcher = getEditorWatcher();

        if (dragState == null)
            return false;

        switch (dragState)
        {
            case moving:
            {
                ClientMap.getMouseScale(screenX - touchPos.x, screenY - touchPos.y, diffPos);
                watcher.setPosition(watcher.getX() - diffPos.x * moveMultiplier * watcher.getScale(),
                        watcher.getY() + diffPos.y * moveMultiplier * watcher.getScale());
                touchPos.set(screenX, screenY);

                break;
            }
            case dragging:
            {
                if (isMoveAlt())
                {
                    ClientMap.getMouseScale(screenX - touchPos.x, screenY - touchPos.y, diffPos);
                    watcher.setPosition(watcher.getX() - diffPos.x * moveMultiplier * watcher.getScale(),
                            watcher.getY() + diffPos.y * moveMultiplier * watcher.getScale());
                }

                Vector2 mousePos = setMousePos(screenX, screenY);
                currentMode.touchMove(mousePos);

                touchPos.set(screenX, screenY);

                break;
            }
        }

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        if (super.mouseMoved(screenX, screenY))
        {
            return true;
        }

        currentMode.mouseMove(setMousePos(screenX, screenY));

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        if (super.touchUp(screenX, screenY, pointer, button))
        {
            return false;
        }

        switch (button)
        {
            case Input.Buttons.LEFT:
            {
                if (isMoveAlt())
                {
                    dragState = DragState.none;
                }
                else
                {
                    dragState = DragState.none;

                    currentMode.touchUp(setMousePos(screenX, screenY));
                }

                break;
            }
            case Input.Buttons.RIGHT:
            {
                if (dragState == DragState.none)
                {
                    currentMode.properties(setMousePos(screenX, screenY));
                }

                break;
            }
        }

        return true;
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }


    @SuppressWarnings("unchecked")
    @Override
    public Table createUI()
    {
        Table data = new Table();
        data.align(Align.left | Align.top);

        Table groupCommon = new Table();
        Table groupMode = new Table();

        this.rootContext = new EditorRegisterButton(true);
        ScrollPane scrollPane = new ScrollPane(rootContext.getGroupContext(), BrainOutClient.Skin, "scroll-default");

        NinePatchDrawable npd = new NinePatchDrawable(BrainOutClient.getNinePatch("buttons-group"));
        groupCommon.setBackground(npd);
        groupMode.setBackground(npd);

        final ImageButton editMapButton = new ImageButton(BrainOutClient.Skin, "button-editor-map-edit");
        editMapButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                editMapProperies();
            }
        });

        Tooltip.RegisterToolTip(editMapButton, L.get("EDITOR_MAP_PROPERTIES"), this);

        final ImageButton saveButton = new ImageButton(BrainOutClient.Skin, "button-editor-save");
        saveButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                saveMap(map.getName());
            }
        });

        Tooltip.RegisterToolTip(saveButton, L.get("EDITOR_SAVE_MAP"), this);

        final ImageButton unloadButton = new ImageButton(BrainOutClient.Skin, "button-editor-unload");
        unloadButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                pushMenu(new ConfirmationPopup(L.get("EDITOR_UNLOAD_CONFIRM"))
                {
                    @Override
                    public void yes()
                    {
                        unload();
                    }
                });
            }
        });

        Tooltip.RegisterToolTip(unloadButton, L.get("EDITOR_UNLOAD"), this);

        final ImageButton playButton = new ImageButton(BrainOutClient.Skin, "button-editor-play");
        playButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                play();
            }
        });

        Tooltip.RegisterToolTip(playButton, L.get("EDITOR_PLAY"), this);

        final ImageButton gridButton = new ImageButton(BrainOutClient.Skin, "button-editor-grid");
        Tooltip.RegisterToolTip(gridButton, L.get("EDITOR_GRID"), this);
        gridButton.setChecked(getEditorComponent().getEditorGrid().isEnabled());

        gridButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                enableGrid(gridButton.isChecked());
            }
        });

        final ImageButton resizeButton = new ImageButton(BrainOutClient.Skin, "button-editor-resize");
        Tooltip.RegisterToolTip(resizeButton, L.get("EDITOR_RESIZE_MAP"), this);

        resizeButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                pushMenu(new ResizeMapMenu(map, EditorMenu.this::resizeMap));
            }
        });
        final ImageButton dimensionsButton = new ImageButton(BrainOutClient.Skin, "button-editor-dimensions");
        Tooltip.RegisterToolTip(dimensionsButton, L.get("EDITOR_MAP_DIMENSIONS"), this);

        dimensionsButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                pushMenu(new MapDimensionsMenu(map,
                        EditorMenu.this::newDimension,
                        EditorMenu.this::deleteDimension,
                        EditorMenu.this::switchDimension));
            }
        });

        final Button multiplierButton = new Button(BrainOutClient.Skin, "button-editor-empty");
        Tooltip.RegisterToolTip(multiplierButton, L.get("EDITOR_SCROLL"), this);
        final Label muliplierLabel = new Label(String.valueOf(moveMultiplier), BrainOutClient.Skin, "title-messages-white");

        muliplierLabel.setFillParent(true);
        muliplierLabel.setTouchable(Touchable.disabled);
        muliplierLabel.setAlignment(Align.center, Align.center);

        multiplierButton.addActor(muliplierLabel);

        multiplierButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                moveMultiplier *= 2;

                if (moveMultiplier > ClientConstants.Editor.MAX_MODE_SPEED)
                {
                    moveMultiplier = 1;
                }

                muliplierLabel.setText(String.valueOf(moveMultiplier));
            }
        });

        groupCommon.add(saveButton);
        groupCommon.add(unloadButton);
        groupCommon.add(editMapButton);
        groupCommon.add(playButton);
        groupCommon.add(gridButton);
        groupCommon.add(multiplierButton);
        groupCommon.add(resizeButton);
        groupCommon.add(dimensionsButton);

        ButtonGroup modes = new ButtonGroup<ImageButton>();

        this.modeBlocks = new ImageButton(BrainOutClient.Skin, "button-editor-mode-blocks");
        modeBlocks.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                setEditorMode(EditorMode.Mode.blocks);
            }
        });
        Tooltip.RegisterToolTip(modeBlocks, L.get("EDITOR_MODE_BLOCKS"), this);

        modes.add(modeBlocks);
        groupMode.add(modeBlocks);

        ImageButton modeSprites = new ImageButton(BrainOutClient.Skin, "button-editor-mode-sprites");
        modeSprites.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                setEditorMode(EditorMode.Mode.actives,
                    new ActivesEditorMode(EditorMenu.this, map, ActivesEditorMode.Filter.sprites));
            }
        });
        Tooltip.RegisterToolTip(modeSprites, L.get("EDITOR_MODE_SPRITES"), this);

        modes.add(modeSprites);
        groupMode.add(modeSprites);

        ImageButton modeLights = new ImageButton(BrainOutClient.Skin, "button-editor-mode-lights");
        modeLights.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                setEditorMode(EditorMode.Mode.actives,
                        new ActivesEditorMode(EditorMenu.this, map, ActivesEditorMode.Filter.lights));
            }
        });
        Tooltip.RegisterToolTip(modeLights, L.get("EDITOR_MODE_LIGHTS"), this);

        modes.add(modeLights);
        groupMode.add(modeLights);

        ImageButton modeOther = new ImageButton(BrainOutClient.Skin, "button-editor-mode-other");
        modeOther.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                setEditorMode(EditorMode.Mode.actives);
            }
        });
        Tooltip.RegisterToolTip(modeOther, L.get("EDITOR_MODE_OTHER"), this);

        modes.add(modeOther);
        groupMode.add(modeOther).padLeft(4);

        data.add(groupCommon).pad(4);
        data.add(groupMode).pad(4);
        data.add(scrollPane).expandX().fillX().pad(4).row();

        this.stats = new Label("", BrainOutClient.Skin, "title-small");

        data.add(stats).padLeft(16).colspan(3).expandX().fillX().row();

        setEditorMode(EditorMode.Mode.blocks);

        layersWidget = new LayersWidget(map, this,
            BrainOutClient.getWidth() - ClientConstants.Menu.Layers.OFFSET_X - ClientConstants.Menu.Layers.WIDTH,
            ClientConstants.Menu.Layers.OFFSET_Y,
            ClientConstants.Menu.Layers.WIDTH,
            ClientConstants.Menu.Layers.HEIGHT);

        GameState gs = getGameState();

        if (gs != null)
            gs.getWidgets().addWidget(layersWidget);

        return data;
    }

    private void switchDimension(EditorMap map)
    {
        this.map = map;

        setEditorMode(EditorMode.Mode.blocks);

        Map.SetWatcher(getEditorWatcher());
    }

    private void deleteDimension(String name)
    {
        BrainOutClient.ClientController.sendTCP(new DeleteDimensionMsg(name));
    }

    private void newDimension(String name, int w, int h)
    {
        BrainOutClient.ClientController.sendTCP(new NewDimensionMsg(name, w, h));
    }

    private void resizeMap(int w, int h, int aX, int aY)
    {
        BrainOutClient.ClientController.sendTCP(new ResizeMapMsg(map.getDimension(),
            w, h, aX, aY));
    }

    private void editMapProperies()
    {
        BrainOutClient.ClientController.sendTCP(new EditorGetMapPropertiesMsg(map.getDimension()));
    }

    private void enableGrid(boolean checked)
    {
        getEditorComponent().getEditorGrid().setEnabled(checked);
    }

    private void play()
    {
        final CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);

        pushMenu(new SelectTeamMenu(csGame.getTeams(),
                new SelectTeamMenu.Select()
                {
                    @Override
                    public void onSelect(Team item, GameState gs)
                    {
                        final ActionPhaseState ap = ((ActionPhaseState) getGameState());

                        if (ap == null)
                            return;

                        pop();

                        final CSGame gameController = BrainOutClient.ClientController.getState(CSGame.class);

                        ap.pushMenu(new SpawnMenu(gameController.getShopCart(),
                            new SpawnMenu.Spawn()
                            {
                                @Override
                                public void ready(Spawnable spawnAt)
                                {
                                    gameController.spawnAt(spawnAt);
                                }

                                @Override
                                public void selected(Spawnable spawnable)
                                {
                                    //
                                }

                                @Override
                                public void notReady()
                                {
                                    gameController.cancelSpawn();
                                }

                                @Override
                                public void spawned()
                                {
                                    final ActionPhaseMenu apm = ap.getActionPhaseMenu();

                                    final Label notice = new Label(L.get("EDITOR_STOP_NOTICE"),
                                            BrainOutClient.Skin, "title-small");

                                    notice.setPosition(16, 16);
                                    apm.addActor(notice);
                                    enableGrid(false);

                                    apm.overrideEscape(() ->
                                    {
                                        csGame.executeConsole("kill");

                                        notice.remove();
                                        apm.overrideEscape(null);

                                        ap.pushMenu(new EditorMenu(map));
                                    });
                                }
                            },
                                gameController.getLastSpawnPoint())
                        {
                            @Override
                            public boolean escape()
                            {
                                pop();
                                return true;
                            }
                        });
                    }

                    @Override
                    public void failed(String reason)
                    {

                    }
                }));
    }

    public Label getStats()
    {
        return stats;
    }

    private void saveMap(String name)
    {
        if (editorFile == null)
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

            SelectMapMenu m = new SelectMapMenu(SelectMapMenu.Mode.save, directory,
                new SelectMapMenu.SelectCallback()
                {
                    @Override
                    public void selected(FileHandle map)
                    {
                        editorFile = map;
                        doSaveMapToFile();
                    }

                    @Override
                    public void cancelled()
                    {

                    }
                }, name);

            pushMenu(m);
        }
        else
        {
            doSaveMapToFile();
        }
    }

    private void doSaveMapToFile()
    {
        ObjectMap<String, String> headers = new ObjectMap<>();
        headers.put("X-Verify", "true");

        ContentClient.download("map", new ContentClient.DownloadResult()
        {
            @Override
            public void success(byte[] data, java.util.Map<String, List<String>> headers)
            {
                try
                {
                    FileOutputStream out = new FileOutputStream(editorFile.file());
                    out.write(data);
                    out.flush();

                    BrainOutClient.EventMgr.sendDelayedEvent(ChatEvent.obtain(
                            new ChatMsg("", L.get("EDITOR_SAVE_SUCCESS"), "server", Color.GREEN, -1)));
                }
                catch (Exception e)
                {
                    BrainOutClient.EventMgr.sendDelayedEvent(ChatEvent.obtain(
                            new ChatMsg("", L.get("EDITOR_SAVE_ERROR"), "server", Color.GREEN, -1)));
                }
            }

            @Override
            public void failed()
            {
                BrainOutClient.EventMgr.sendDelayedEvent(ChatEvent.obtain(
                        new ChatMsg("", L.get("EDITOR_SAVE_ERROR"), "server", Color.GREEN, -1)));
            }
        }, headers);
    }

    private void unload()
    {
        BrainOutClient.ClientController.sendTCP(new EditorActionMsg(EditorActionMsg.ID.unload));
    }

    public void showActiveProperties(final ActiveData activeData, Array<EditorProperty> properties)
    {
        pushMenu(new InspectObjectMenu(activeData, properties,
            (inspectable, properties1) ->
                BrainOutClient.ClientController.sendTCP(new EditorSetActivePropertiesMsg(activeData, properties1))));
    }

    public void showMapProperties(EditorMap editorMap, Array<EditorProperty> properties)
    {
        pushMenu(new InspectObjectMenu(editorMap, properties,
            (inspectable, properties1) ->
                BrainOutClient.ClientController.sendTCP(new EditorSetMapPropertiesMsg(
                    map.getDimension(), properties1))));
    }

    @Override
    public void pushMenu(Menu menu)
    {
        GameState gs = BrainOutClient.getInstance().topState();

        if (gs == null)
            return;

        gs.pushMenu(menu);
    }

    public class EditorRegisterButton extends EditorMode.RegisterButton
    {
        private final ButtonGroup buttonGroup;
        private final Table groupContext;

        public EditorRegisterButton(boolean setBackground)
        {
            this.buttonGroup = new ButtonGroup();
            this.groupContext = new Table();
            groupContext.align(Align.left);

            if (setBackground)
            {
                groupContext.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("buttons-group")));
            }
        }

        public Table getGroupContext()
        {
            return groupContext;
        }

        @Override
        public Button registerIconButton(TextureAtlas.AtlasRegion region, final Runnable selected, ButtonGroup customGroup)
        {
            Button button = new Button(BrainOutClient.Skin, "button-editor-checkable");
            Image image = new Image(region);
            image.setFillParent(true);
            image.setScaling(Scaling.none);
            image.setTouchable(Touchable.disabled);

            button.addActor(image);

            (customGroup != null ? customGroup : buttonGroup).add(button);
            groupContext.add(button).padRight(4);

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    selected.run();
                }
            });

            return button;
        }

        @Override
        public void registerSpace()
        {
            groupContext.add(new Image(BrainOutClient.Skin, "editor-space")).padLeft(2).padRight(6);
        }

        @Override
        public Button registerGroupButton(String buttonStyle, final Runnable selected, ButtonGroup customGroup)
        {
            ImageButton button = new ImageButton(BrainOutClient.Skin, buttonStyle);

            // holy shit
            (customGroup != null ? customGroup : buttonGroup).add(button);
            groupContext.add(button).padRight(4);

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    selected.run();
                }
            });

            return button;
        }

        @Override
        public Actor registerButton(String buttonStyle, final Runnable selected)
        {
            ImageButton button = new ImageButton(BrainOutClient.Skin, buttonStyle);

            groupContext.add(button).padRight(4);

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    selected.run();
                }
            });

            return button;
        }

        @Override
        public EditorMode.RegisterButton registerSubMenu()
        {
            EditorRegisterButton registerButton = new EditorRegisterButton(false);
            groupContext.add(registerButton.getGroupContext());
            return registerButton;
        }

        @Override
        public void clear()
        {
            groupContext.clear();
        }

        public Cell registerActor(Actor actor)
        {
            return groupContext.add(actor);
        }
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }
    }

    public void setEditorMode(EditorMode.Mode editorMode, EditorMode modeInstance)
    {
        this.editorMode = editorMode;
        rootContext.clear();

        if (currentMode != null)
        {
            currentMode.release();
        }

        this.currentMode = modeInstance;

        currentMode.init();
        currentMode.initContextMenu(rootContext);

        if (layersWidget != null)
        {
            layersWidget.updateLayers();
        }
    }

    public void setEditorMode(EditorMode.Mode editorMode)
    {
        final EditorMode mode;

        switch (editorMode)
        {
            case actives:
            {
                mode = new ActivesEditorMode(this, map, ActivesEditorMode.Filter.other);

                break;
            }
            case blocks:
            default:
            {
                mode = new BlocksEditorMode(this, map);

                break;
            }
        }

        setEditorMode(editorMode, mode);
    }

    public EditorMode.Mode getEditorMode()
    {
        return editorMode;
    }

    public EditorMode getCurrentMode()
    {
        return currentMode;
    }
}
