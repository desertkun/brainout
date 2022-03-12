package com.desertkun.brainout.data.containers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;
import com.desertkun.brainout.data.interfaces.Renderable;
import com.desertkun.brainout.data.interfaces.Updatable;

public class DataContainer<T extends Updatable & Renderable> extends Array<T> implements RenderUpdatable
{
    private Array<Updatable> updateList;
    private Array<RenderEntry> renderList;

    public enum RenderFilter
    {
        any,
        pre,
        post
    }

    private static class RenderEntry
    {
        Renderable renderable;
        RenderFilter post;

        public RenderEntry(Renderable renderable, RenderFilter post)
        {
            this.renderable = renderable;
            this.post = post;
        }
    }

    public DataContainer()
    {
        super();

        updateList = null;
        renderList = null;
    }

    public void addUpdateItem(Updatable updatable)
    {
        if (updateList == null)
        {
            updateList = new Array<>();
        }

        updateList.add(updatable);
    }

    public void addRenderableItem(Renderable renderable, RenderFilter post)
    {
        if (renderList == null)
        {
            renderList = new Array<>();
        }

        renderList.add(new RenderEntry(renderable, post));
    }

    public void removeUpdateItem(T item)
    {
        if (updateList != null)
        {
            updateList.removeValue(item, true);
        }
    }

    public void removeRenderableItem(T item)
    {
        if (renderList != null)
        {
            for (RenderEntry entry : renderList)
            {
                if (entry.renderable == item)
                {
                    renderList.removeValue(entry, true);
                    break;
                }
            }
        }
    }

    public void addItem(T item, RenderFilter post)
    {
        add(item);

        if (item.hasRender())
        {
            addRenderableItem(item, post);
        }

        if (item.hasUpdate())
        {
            addUpdateItem(item);
        }
    }

    public void removeItem(T item)
    {
        removeValue(item, true);

        if (item.hasRender())
        {
            removeRenderableItem(item);
        }

        if (item.hasUpdate())
        {
            removeUpdateItem(item);
        }
    }

    public void insertItem(int i, T item)
    {
        insert(i, item);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (renderList != null)
        {
            for (RenderEntry entry : renderList)
            {
                if (entry.post == RenderFilter.any)
                {
                    entry.renderable.render(batch, context);
                }
                else
                {
                    if ((entry.post == RenderFilter.post) == context.post)
                    {
                        entry.renderable.render(batch, context);
                    }
                }
            }
        }
    }

    @Override
    public void update(float dt)
    {
        if (updateList != null)
            for (Updatable updatable : updateList)
            {
                updatable.update(dt);
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
