package com.desertkun.brainout.data.containers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.interfaces.*;

public class BlockDataContainer extends Array<BlockData> implements RenderUpdatable
{
    private final int layer;
    private LongMap<Updatable> updateList;
    private LongMap<Renderable> renderList;

    public BlockDataContainer(int layer)
    {
        super();

        this.layer = layer;
        updateList = null;
        renderList = null;
    }

    public long getIndex(int x, int y)
    {
        return ((long)y << 32) | x;
    }

    public void addUpdateItem(int x, int y, Updatable updatable)
    {
        if (updateList == null)
        {
            updateList = new LongMap<>();
        }

        updateList.put(getIndex(x, y), updatable);
    }

    public void addRenderableItem(int x, int y, Renderable renderable)
    {
        if (renderList == null)
        {
            renderList = new LongMap<>();
        }

        renderList.put(getIndex(x, y), renderable);
    }

    public void removeUpdateItem(int x, int y)
    {
        if (updateList != null)
        {
            updateList.remove(getIndex(x, y));
        }
    }

    public void removeRenderableItem(int x, int y)
    {
        if (renderList != null)
        {
            renderList.remove(getIndex(x, y));
        }
    }

    public void addItem(int x, int y, BlockData item)
    {
        add(item);

        if (item.hasRender())
        {
            addRenderableItem(x, y, item);
        }

        if (item.hasUpdate())
        {
            addUpdateItem(x, y, item);
        }
    }

    public void removeItem(int x, int y, BlockData item)
    {
        removeValue(item, true);

        if (item.hasRender())
        {
            removeRenderableItem(x, y);
        }

        if (item.hasUpdate())
        {
            removeUpdateItem(x, y);
        }
    }

    public void insertItem(int i, BlockData item)
    {
        insert(i, item);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (renderList != null)
        {
            BlockData.CURRENT_LAYER = layer;

            for (LongMap.Entry<Renderable> entry : renderList)
            {
                long key = entry.key;

                BlockData.CURRENT_X = (int)(key & (long)0xFFFFFFFF);
                BlockData.CURRENT_Y = (int)(key >> 32);

                entry.value.render(batch, context);
            }
        }
    }

    @Override
    public void update(float dt)
    {
        if (updateList != null)
        {
            BlockData.CURRENT_LAYER = layer;

            for (LongMap.Entry<Updatable> entry : updateList)
            {
                if (entry.value == null)
                    continue;

                long key = entry.key;

                BlockData.CURRENT_X = (int)(key & (long)0xFFFFFFFF);
                BlockData.CURRENT_Y = (int)(key >> 32);

                entry.value.update(dt);
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
