package com.desertkun.brainout.editor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.editor.data.EditorGrid;
import com.desertkun.brainout.editor.menu.EditorMenu;
import com.desertkun.brainout.events.Event;

public class EditorComponent extends Component
{
    private EditorMenu menu;
    private EditorGrid editorGrid;

    public EditorComponent(String dimension)
    {
        super(null, null);

        editorGrid = new EditorGrid(dimension);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        if (menu.getCurrentMode() != null)
        {
            menu.getCurrentMode().render(batch, context);
        }

        editorGrid.render(batch, context);
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (menu.getCurrentMode() != null)
        {
            menu.getCurrentMode().update(dt);
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    public EditorGrid getEditorGrid()
    {
        return editorGrid;
    }

    public void setMenu(EditorMenu menu)
    {
        this.menu = menu;
    }
}
