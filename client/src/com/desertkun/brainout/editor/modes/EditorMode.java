package com.desertkun.brainout.editor.modes;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.desertkun.brainout.data.EditorMap;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;
import com.desertkun.brainout.editor.menu.EditorMenu;

public abstract class EditorMode implements RenderUpdatable
{
    private final EditorMap map;
    private final EditorMenu menu;

    protected EditorMode(EditorMenu menu, EditorMap map)
    {
        this.menu = menu;
        this.map = map;
    }

    public EditorMenu getMenu()
    {
        return menu;
    }

    public void touchDown(Vector2 pos) {}
    public void touchMove(Vector2 pos) {}
    public void touchUp(Vector2 pos) {}
    public void mouseMove(Vector2 pos) {}
    public void properties(Vector2 pos) {}

    @Override
    public void update(float dt) {}

    @Override
    public void render(Batch batch, RenderContext context) {}

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    public EditorMap getMap()
    {
        return map;
    }

    public boolean keyDown(int keyCode) { return false; }


    public enum Mode
    {
        blocks,
        actives
    }

    public abstract static class RegisterButton
    {
        public abstract Button registerIconButton(TextureAtlas.AtlasRegion region, Runnable selected, ButtonGroup customGroup);
        public abstract void registerSpace();
        public abstract Button registerGroupButton(String buttonStyle, Runnable selected, ButtonGroup customGroup);
        public Button registerGroupButton(String buttonStyle, Runnable selected)
        {
            return registerGroupButton(buttonStyle, selected, null);
        }
        public Button registerIconButton(TextureAtlas.AtlasRegion region, Runnable selected)
        {
            return registerIconButton(region, selected, null);
        }
        public abstract Actor registerButton(String buttonStyle, Runnable selected);
        public abstract RegisterButton registerSubMenu();
        public abstract void clear();
        public abstract Cell registerActor(Actor actor);
    }

    public abstract void initContextMenu(RegisterButton callback);

    public void init() {}
    public void release() {}

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
}
