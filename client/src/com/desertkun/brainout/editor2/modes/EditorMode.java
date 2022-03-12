package com.desertkun.brainout.editor2.modes;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;
import com.desertkun.brainout.editor2.Editor2Menu;

public abstract class EditorMode implements RenderUpdatable, Disposable
{
    public enum ID
    {
        actives,
        blocks,
        light,
        play
    }

    private final Editor2Menu menu;

    public EditorMode(Editor2Menu menu)
    {
        this.menu = menu;
    }

    public void selected() {}
    public abstract void renderPanels(Table panels);

    public Editor2Menu getMenu()
    {
        return menu;
    }

    public void init() {}
    public void release() {}

    public boolean keyDown(int keyCode)
    {
        return false;
    }

    public boolean keyUp(int keyCode)
    {
        return false;
    }

    public boolean mouseDown(Vector2 position, int button)
    {
        return false;
    }

    public boolean mouseUp(Vector2 position, int button)
    {
        return false;
    }

    public boolean mouseMove(Vector2 position)
    {
        return false;
    }

    public boolean mouseDrag(Vector2 position, int button)
    {
        return false;
    }

    public abstract ID getID();

    @Override
    public void render(Batch batch, RenderContext context)
    {

    }

    public boolean escape()
    {
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public int getZIndex()
    {
        return 0;
    }

    @Override
    public int getLayer()
    {
        return 0;
    }

    @Override
    public void update(float dt)
    {

    }

    public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, Vector2 position, int pointer)
    {
        return false;
    }

    public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, Vector2 position, int pointer)
    {
        //
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public void dispose()
    {

    }

    public Editor2Menu.DragState getDragState()
    {
        return menu.getDragState();
    }

    public void renderBottomPanel(Table panel)
    {
        //
    }
}
