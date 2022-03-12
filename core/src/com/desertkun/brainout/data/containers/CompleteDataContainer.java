package com.desertkun.brainout.data.containers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;
import com.desertkun.brainout.data.interfaces.Renderable;
import com.desertkun.brainout.data.interfaces.CompleteUpdatable;

public class CompleteDataContainer<T extends CompleteUpdatable & Renderable> extends Array<T> implements RenderUpdatable
{
    public CompleteDataContainer()
    {
        super();
    }

    public void addItem(T item)
    {
        add(item);
        item.init();
    }

    public void removeItem(T item)
    {
        item.release();
        removeValue(item, true);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        for (Renderable renderable: this)
        {
            renderable.render(batch, context);
        }
    }

    @Override
    public void update(float dt)
    {
        for (int i = size - 1; i >= 0; i--)
        {
            CompleteUpdatable updatable = get(i);

            updatable.update(dt);
            if (updatable.done())
            {
                updatable.release();
                removeIndex(i);
            }
        }
    }

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
