package com.desertkun.brainout.menu.impl.realestate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.*;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.data.FreePlayMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.ExitMenu;
import com.desertkun.brainout.menu.impl.realestate.modes.ActivesEditorMode;
import com.desertkun.brainout.menu.impl.realestate.modes.EditorMode;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;

public class RealEstateEditMenu extends Menu implements EventReceiver
{
    private final PlayerData playerData;
    private final String dimension;
    private Vector2 touchDownPosition, mousePosition;

    private Cursor handCursor;
    private Table contextPanel;

    private static EditorMode.ID lastEditorMode = EditorMode.ID.actives;

    private EditorMode currentMode, modeActives;
    private DragState dragState;
    private Label mousePositionLabel;
    private boolean scrolling;

    private DragAndDrop.Target dragAndDropTarget;
    private Actor dragAndDropTargetActor;
    private Table modePanel;
    private int touchDownButton;

    private ImageButton activesModeButton;

    public enum DragState
    {
        normal,
        down,
        dragging
    }

    public RealEstateEditMenu(PlayerData playerData)
    {
        this.playerData = playerData;
        this.dimension = playerData.getDimension();
        touchDownPosition = new Vector2();

        mousePosition = new Vector2();
        dragState = DragState.normal;

        setDragAndDrop(new DragAndDrop());

        modeActives = new ActivesEditorMode(playerData.getDimension(), this);
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

    private void renderTopToolbar(Table toolbar)
    {
        // save, load, map properties
        {
            Table panel = new Table(BrainOutClient.Skin);
            panel.setBackground("buttons-group");

            renderSaveButton(panel);

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
            case actives:
            {
                switchActivesMode();
                activesModeButton.setChecked(true);
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

    private void renderModeButtons(Table panel)
    {
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMaxCheckCount(1);
        group.setMinCheckCount(1);

        {
            activesModeButton = new ImageButton(BrainOutClient.Skin, "button-editor-mode-sprites");
            Tooltip.RegisterToolTip(activesModeButton, L.get("MENU_CATEGORY_REAL_ESTATE_DECOR"), this);

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
    }

    public boolean isMoveAlt()
    {
        return scrolling;
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

        if (currentMode.mouseDrag(convertMousePosition(screenX, screenY), button))
        {
            setDragState(DragState.dragging);
            updateStats();
            return true;
        }

        return false;
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
        Map map = playerData.getMap();

        if (map == null)
            return mousePosition;

        Map.GetMouseScaleWatcher(screenX - BrainOutClient.getWidth() / 2f,
                - (screenY - BrainOutClient.getHeight() / 2f), mousePosition);

        mousePosition.x = MathUtils.clamp(mousePosition.x, 0, map.getWidth());
        mousePosition.y = MathUtils.clamp(mousePosition.y, 0, map.getHeight());

        return mousePosition;
    }

    private void renderSaveButton(Table panel)
    {
        final ImageButton button = new ImageButton(BrainOutClient.Skin, "button-editor-exit");

        button.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                pop();
            }
        });

        Tooltip.RegisterToolTip(button, L.get("MENU_EXIT"), this);

        panel.add(button);
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
            case Input.Keys.TAB:
            {
                if (escape())
                {
                    return true;
                }
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

        switchActivesMode();
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        modeActives.dispose();

        if (handCursor != null)
        {
            handCursor.dispose();
            handCursor = null;
        }
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public boolean escape()
    {
        pop();
        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
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
