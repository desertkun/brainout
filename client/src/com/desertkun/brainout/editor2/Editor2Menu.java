package com.desertkun.brainout.editor2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.http.ContentClient;
import com.desertkun.brainout.common.msg.client.editor.EditorActionMsg;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Editor2Map;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.editor2.modes.*;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.ExitMenu;
import com.desertkun.brainout.menu.impl.WaitLoadingMenu;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.esotericsoftware.minlog.Log;

import java.io.*;
import java.util.List;

public class Editor2Menu extends Menu implements Watcher, EventReceiver
{
    private String dimension;
    private Vector2 screenPosition, screenHalfSize, touchDownPosition, mousePosition, tmp;
    private float moveMultiplier;
    private float scale;

    private Cursor handCursor;
    private Table contextPanel;

    private static EditorMode.ID lastEditorMode = EditorMode.ID.blocks;

    private EditorMode currentMode, modeActives, modeBlocks, modeLight, modePlay;
    private DragState dragState;
    private Label mousePositionLabel;
    private boolean scrolling;
    private ImageButton gridButton;

    private DragAndDrop.Target dragAndDropTarget;
    private Actor dragAndDropTargetActor;
    private Table modePanel;
    private int touchDownButton;

    private ImageButton blocksModeButton;
    private ImageButton activesModeButton;
    private ImageButton lightsModeButton;
    private ImageButton playModeButton;

    public enum DragState
    {
        normal,
        down,
        dragging
    }

    public Editor2Menu()
    {
        dimension = "default";
        screenHalfSize = new Vector2(BrainOutClient.getWidth() / (Constants.Graphics.BLOCK_SIZE * 2.0f),
            BrainOutClient.getHeight() / (Constants.Graphics.BLOCK_SIZE * 2.0f));

        touchDownPosition = new Vector2();
        screenPosition = new Vector2();

        if (Editor2Map.StartLocation.isZero())
        {
            screenPosition.set(30, 80);
        }
        else
        {
            screenPosition.set(Editor2Map.StartLocation);
        }

        tmp = new Vector2();
        mousePosition = new Vector2();
        moveMultiplier = 2.0f;
        scale = 1.0f;
        dragState = DragState.normal;

        setDragAndDrop(new DragAndDrop());

        modeActives = new ActivesEditorMode(this);
        modeBlocks = new BlocksEditorMode(this);
        modeLight = new LightEditorMode(this);
        modePlay = new PlayEditorMode(this);

        touchDownButton = -1;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            dragAndDropTargetActor = new Widget();
            ((Widget) dragAndDropTargetActor).setFillParent(true);
            data.addActor(dragAndDropTargetActor);
        }

        modePanel = new Table();

        {
            Table toolbar = new Table(BrainOutClient.Skin);
            toolbar.align(Align.left);
            renderTopToolbar(toolbar);
            data.add(toolbar).pad(8).expandX().fillX().height(32).row();
        }

        data.add().expand().row();

        {
            Table toolbar = new Table(BrainOutClient.Skin);
            toolbar.align(Align.left);
            renderBottomToolbar(toolbar);
            data.add(toolbar).pad(8).expandX().fillX().height(32).row();
        }

        return data;
    }

    private void renderBottomToolbar(Table toolbar)
    {
        // zoom
        {
            Table panel = new Table(BrainOutClient.Skin);
            panel.setBackground("buttons-group");
            renderZoomButton(panel);
            toolbar.add(panel).padRight(8);
        }

        {
            toolbar.add(modePanel).expandX().center();
        }

        // stats
        {
            Table panel = new Table(BrainOutClient.Skin);
            panel.setBackground("buttons-group");
            renderStats(panel);
            toolbar.add(panel).row();
        }
    }

    private void renderStats(Table panel)
    {
        mousePositionLabel = new Label("", BrainOutClient.Skin, "title-small");
        mousePositionLabel.setAlignment(Align.center);
        panel.add(mousePositionLabel).size(96, 32);

        updateStats();
    }

    private void updateStats()
    {
        String stats = String.valueOf((int)mousePosition.x) + ", " + (int)mousePosition.y;
        mousePositionLabel.setText(stats);
    }

    private void renderZoomButton(Table panel)
    {
        Label value = new Label("100%", BrainOutClient.Skin, "title-small");

        final ImageButton button = new ImageButton(BrainOutClient.Skin, "button-editor-zoom");
        Tooltip.RegisterToolTip(button, L.get("EDITOR_ZOOM"), this);

        button.setChecked(false);

        button.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                scale = button.isChecked() ? 2.0f : 1.0f;
                value.setText(button.isChecked() ? "50%" : "100%");

                // just to clamp
                moveCursor(0, 0);
            }
        });

        panel.add(button);
        panel.add(value).padLeft(8).padRight(8);
    }

    private void renderTopToolbar(Table toolbar)
    {
        // save, load, map properties
        {
            Table panel = new Table(BrainOutClient.Skin);
            panel.setBackground("buttons-group");

            renderSaveButton(panel);
            renderLoadButton(panel);
            //renderMapPropertiesButton(panel);

            toolbar.add(panel).padRight(8);
        }

        // grid
        {
            Table panel = new Table(BrainOutClient.Skin);
            panel.setBackground("buttons-group");

            renderGridButton(panel);

            toolbar.add(panel).padRight(8);
        }

        // current move
        {
            Table panel = new Table(BrainOutClient.Skin);
            panel.setBackground("buttons-group");

            renderModeButtons(panel);

            toolbar.add(panel).padRight(32);
        }

        // context
        {
            contextPanel = new Table(BrainOutClient.Skin);
            toolbar.add(contextPanel).padRight(8);
        }

        switch (lastEditorMode)
        {
            case blocks:
            {
                switchBlocksMode();
                blocksModeButton.setChecked(true);
                break;
            }
            case actives:
            {
                switchActivesMode();
                activesModeButton.setChecked(true);
                break;
            }
            case light:
            {
                switchLightMode();
                lightsModeButton.setChecked(true);
                break;
            }
            case play:
            {
                switchPlayMode();
                playModeButton.setChecked(true);
                break;
            }
        }
    }

    public EditorMode getCurrentMode()
    {
        return currentMode;
    }

    private void switchMode(EditorMode mode)
    {
        if (currentMode != null && currentMode != mode)
        {
            this.currentMode.release();
        }

        EditorMode prevMode = currentMode;

        this.currentMode = mode;
        contextPanel.clearChildren();

        if (prevMode != mode)
        {
            mode.init();
        }

        mode.selected();
        mode.renderPanels(contextPanel);

        modePanel.clear();
        mode.renderBottomPanel(modePanel);

        lastEditorMode = mode.getID();
    }

    private void switchActivesMode()
    {
        switchMode(modeActives);
    }

    private void switchBlocksMode()
    {
        switchMode(modeBlocks);
    }

    private void switchLightMode()
    {
        switchMode(modeLight);
    }

    private void switchPlayMode()
    {
        ((PlayEditorMode) modePlay).setPreviousMode(currentMode);

        switchMode(modePlay);
    }

    private void renderModeButtons(Table panel)
    {
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMaxCheckCount(1);
        group.setMinCheckCount(1);

        {
            blocksModeButton = new ImageButton(BrainOutClient.Skin, "button-editor-mode-blocks");
            Tooltip.RegisterToolTip(blocksModeButton, L.get("EDITOR_MODE_BLOCKS"), this);

            blocksModeButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    switchBlocksMode();
                }
            });

            panel.add(blocksModeButton);
            group.add(blocksModeButton);
        }

        {
            activesModeButton = new ImageButton(BrainOutClient.Skin, "button-editor-mode-sprites");
            Tooltip.RegisterToolTip(activesModeButton, L.get("EDITOR_MODE_SPRITES"), this);

            activesModeButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    switchActivesMode();
                }
            });

            panel.add(activesModeButton);
            group.add(activesModeButton);
        }

        {
            lightsModeButton = new ImageButton(BrainOutClient.Skin, "button-editor-mode-lights");
            Tooltip.RegisterToolTip(lightsModeButton, L.get("EDITOR_MODE_LIGHTS"), this);

            lightsModeButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    switchLightMode();
                }
            });

            panel.add(lightsModeButton);
            group.add(lightsModeButton);
        }

        {
            playModeButton = new ImageButton(BrainOutClient.Skin, "button-editor-mode-play");
            Tooltip.RegisterToolTip(playModeButton, L.get("EDITOR_PLAY"), this);

            playModeButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);
                    switchPlayMode();
                }
            });

            panel.add(playModeButton);
            group.add(playModeButton);
        }
    }

    private void renderMapPropertiesButton(Table panel)
    {
        final ImageButton button = new ImageButton(BrainOutClient.Skin, "button-editor-map-edit");
        button.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                //
            }
        });

        Tooltip.RegisterToolTip(button, L.get("EDITOR_MAP_PROPERTIES"), this);

        panel.add(button);
    }

    public boolean isMoveAlt()
    {
        return scrolling;
    }

    private void renderGridButton(Table panel)
    {
        gridButton = new ImageButton(BrainOutClient.Skin, "button-editor-grid");
        Tooltip.RegisterToolTip(gridButton, L.get("EDITOR_GRID"), this);

        gridButton.setChecked(true);
        Editor2Map.SetGridEnabled(true);

        gridButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);
                Editor2Map.SetGridEnabled(gridButton.isChecked());
            }
        });

        panel.add(gridButton);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        if (super.touchDown(screenX, screenY, pointer, button))
        {
            return true;
        }

        if (touchDownButton != -1 && touchDownButton != button)
            return false;

        touchDownButton = button;

        if (isMoveAlt())
        {
            touchDownPosition.set(screenX, screenY);
            return true;
        }

        if (currentMode.mouseDown(convertMousePosition(screenX, screenY), button))
        {
            setDragState(DragState.down);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int button)
    {
        if (super.touchDragged(screenX, screenY, button))
        {
            return true;
        }

        if (touchDownButton != button)
            return false;

        if (tryScroll(screenX, screenY))
        {
            return true;
        }

        if (currentMode.mouseDrag(convertMousePosition(screenX, screenY), button))
        {
            setDragState(DragState.dragging);
            updateStats();
            return true;
        }

        return false;
    }

    private boolean tryScroll(int screenX, int screenY)
    {
        if (!isMoveAlt())
        {
            return false;
        }

        ClientMap.getMouseScale(screenX - touchDownPosition.x, screenY - touchDownPosition.y, tmp);
        moveCursor(tmp.x * moveMultiplier * getScale(),
                tmp.y * moveMultiplier * getScale());
        touchDownPosition.set(screenX, screenY);

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        if (super.touchUp(screenX, screenY, pointer, button))
        {
            return false;
        }

        if (touchDownButton != button)
            return false;

        touchDownButton = -1;

        if (getDragState() == DragState.normal)
            return true;

        setDragState(DragState.normal);
        return currentMode.mouseUp(convertMousePosition(screenX, screenY), button);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        if (super.mouseMoved(screenX, screenY))
        {
            return true;
        }

        if (currentMode.mouseMove(convertMousePosition(screenX, screenY)))
        {
            updateStats();
            return true;
        }

        return false;
    }

    public Vector2 getMousePosition()
    {
        return mousePosition;
    }

    private Vector2 convertMousePosition(float screenX, float screenY)
    {
        Map map = Map.Get(getDimension());

        if (map == null)
            return mousePosition;

        Map.GetMouseScaleWatcher(screenX - BrainOutClient.getWidth() / 2f,
                - (screenY - BrainOutClient.getHeight() / 2f), mousePosition);

        mousePosition.x = MathUtils.clamp(mousePosition.x, 0, map.getWidth());
        mousePosition.y = MathUtils.clamp(mousePosition.y, 0, map.getHeight());

        return mousePosition;
    }

    private void moveCursor(float x, float y)
    {
        Editor2Map map = Map.Get(getDimension(), Editor2Map.class);

        if (map == null)
            return;

        final float limit = 32.0f;

        screenPosition.set(
            MathUtils.clamp(
                screenPosition.x - x,
                screenHalfSize.x * getScale() - limit,
                map.getWidth() - screenHalfSize.x * getScale() + limit),

            MathUtils.clamp(
                screenPosition.y + y,
                screenHalfSize.y * getScale() - limit,
                map.getHeight() - screenHalfSize.y * getScale() + limit)
        );
    }

    private void renderSaveButton(Table panel)
    {
        final ImageButton button = new ImageButton(BrainOutClient.Skin, "button-editor-save");

        button.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                createOrSaveMap();
            }
        });

        Tooltip.RegisterToolTip(button, L.get("EDITOR_SAVE_MAP"), this);

        panel.add(button);
    }

    private void createOrSaveMap()
    {
        Editor2Map map = Map.Get(getDimension(), Editor2Map.class);

        if (map == null)
            return;

        String fileId = map.getCustom("workshop-item");

        if (fileId == null)
        {
            createMap();
        }
        else
        {
            BrainOutClient.Env.getGameUser().queryWorkshopItem(fileId, new GameUser.WorkshopItemQueryCallback()
            {
                @Override
                public void success(GameUser.WorkshopItem item)
                {
                    updateMap(item);
                }

                @Override
                public void failed(String reason)
                {
                }
            });
        }
    }

    private void updateMap(GameUser.WorkshopItem item)
    {
        Editor2Map map = Map.Get(getDimension(), Editor2Map.class);

        if (map == null)
            return;

        File previewFile;

        try
        {
            previewFile = map.renderPreview();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        if (previewFile == null)
            return;

        if (Log.INFO) Log.info("Rendered preview to: " + previewFile.getAbsolutePath());

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu(L.get("MENU_DOWNLOADING_MAP"));
        pushMenu(waitLoadingMenu);

        ObjectMap<String, String> headers = new ObjectMap<>();

        headers.put("X-Owner-Key", BrainOutClient.ClientController.getOwnerKey());
        headers.put("X-Verify", "true");

        ContentClient.download("map", new ContentClient.DownloadResult()
        {
            @Override
            public void success(byte[] data, java.util.Map<String, List<String>> headers)
            {
                final File mapFile;

                try
                {
                    mapFile = File.createTempFile("brainout-map", ".tmp");

                    OutputStream mapFileOutput = new FileOutputStream(mapFile);
                    mapFileOutput.write(data);
                    mapFileOutput.flush();
                    mapFileOutput.close();


                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }

                Gdx.app.postRunnable(() ->
                {
                    waitLoadingMenu.pop();
                    pushMenu(new UpdateMapPreviewMenu(item, previewFile, mapFile, map));
                });

            }

            @Override
            public void failed()
            {
                waitLoadingMenu.pop();
            }

        }, headers);
    }

    private void createMap()
    {
        Editor2Map map = Map.Get(getDimension(), Editor2Map.class);

        if (map == null)
            return;

        File previewFile;

        try
        {
            previewFile = map.renderPreview();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        if (previewFile == null)
            return;

        if (Log.INFO) Log.info("Rendered preview to: " + previewFile.getAbsolutePath());

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu(L.get("MENU_DOWNLOADING_MAP"));
        pushMenu(waitLoadingMenu);

        ObjectMap<String, String> headers = new ObjectMap<>();

        headers.put("X-Owner-Key", BrainOutClient.ClientController.getOwnerKey());
        headers.put("X-Verify", "true");

        ContentClient.download("map", new ContentClient.DownloadResult()
        {
            @Override
            public void success(byte[] data, java.util.Map<String, List<String>> headers)
            {
                final File mapFile;

                try
                {
                    mapFile = File.createTempFile("brainout-map", ".tmp");

                    OutputStream mapFileOutput = new FileOutputStream(mapFile);
                    mapFileOutput.write(data);
                    mapFileOutput.flush();
                    mapFileOutput.close();


                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }

                Gdx.app.postRunnable(() ->
                {
                    waitLoadingMenu.pop();
                    pushMenu(new PublishMapPreviewMenu(previewFile, mapFile, map));
                });

            }

            @Override
            public void failed()
            {
                waitLoadingMenu.pop();
            }

        }, headers);
    }

    private void renderLoadButton(Table panel)
    {
        final ImageButton button = new ImageButton(BrainOutClient.Skin, "button-editor-unload");

        button.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

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

        Tooltip.RegisterToolTip(button, L.get("EDITOR_UNLOAD"), this);

        panel.add(button);
    }

    private void unload()
    {
        BrainOutClient.ClientController.sendTCP(new EditorActionMsg(EditorActionMsg.ID.unload));
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        if (super.keyDown(keyCode))
        {
            return true;
        }

        switch (keyCode)
        {
            case Input.Keys.SPACE:
            {
                if (handCursor != null)
                    Gdx.graphics.setCursor(handCursor);

                if (getDragState() == DragState.normal)
                {
                    scrolling = true;
                }

                break;
            }
            case Input.Keys.G:
            {
                Menu.playSound(MenuSound.select);
                gridButton.setChecked(!gridButton.isChecked());
                Editor2Map.SetGridEnabled(gridButton.isChecked());

                break;
            }
        }

        return currentMode.keyDown(keyCode);
    }

    @Override
    public boolean keyUp(int keyCode)
    {
        if (currentMode.keyUp(keyCode))
            return true;

        switch (keyCode)
        {
            case Input.Keys.SPACE:
            {
                if (handCursor != null)
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);

                scrolling = false;

                break;
            }
        }

        return super.keyUp(keyCode);
    }

    @Override
    public void onInit()
    {
        super.onInit();

        FileHandle handle = Gdx.files.internal("icons/icon-hand.png");
        handCursor = handle.exists() ? Gdx.graphics.newCursor(new Pixmap(handle), 8, 8) : null;

        setWatcher();

        dragAndDropTarget = new DragAndDrop.Target(dragAndDropTargetActor)
        {

            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer)
            {
                return currentMode.drag(source, payload, convertMousePosition(x, dragAndDropTargetActor.getHeight() - y), pointer);
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer)
            {
                currentMode.drop(source, payload, convertMousePosition(x, dragAndDropTargetActor.getHeight() - y), pointer);
            }
        };

        getDragAndDrop().addTarget(dragAndDropTarget);
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if (getGameState().topMenu() == this)
        {
            float speed = (delta * 100.f) * scale;

            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W))
                moveCursor(0, speed);
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S))
                moveCursor(0, -speed);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A))
                moveCursor(speed, 0);
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D))
                moveCursor(-speed, 0);
        }
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        modeBlocks.dispose();
        modeLight.dispose();
        modePlay.dispose();
        modeActives.dispose();

        if (handCursor != null)
        {
            handCursor.dispose();
            handCursor = null;
        }
    }

    private void setWatcher()
    {
        Map.SetWatcher(this);
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public boolean escape()
    {
        if (getCurrentMode().escape())
        {
            return true;
        }

        pushMenu(new ExitMenu(true));

        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public float getWatchX()
    {
        return screenPosition.x;
    }

    @Override
    public float getWatchY()
    {
        return screenPosition.y;
    }

    @Override
    public boolean allowZoom()
    {
        return false;
    }

    @Override
    public float getScale()
    {
        return scale;
    }

    @Override
    public String getDimension()
    {
        return dimension;
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }

        BrainOutClient.Env.getGameController().setControllerMode(GameController.ControllerMode.disabled);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case gameController:
            {
                break;
            }
        }

        return false;
    }

    public void setDragState(DragState dragState)
    {
        this.dragState = dragState;
    }

    public DragState getDragState()
    {
        return dragState;
    }
}
