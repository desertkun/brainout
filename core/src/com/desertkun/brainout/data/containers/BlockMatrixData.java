package com.desertkun.brainout.data.containers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.interfaces.*;
import com.desertkun.brainout.events.SetDirtyEvent;
import com.desertkun.brainout.events.SimpleEvent;

public class BlockMatrixData extends Array<ChunkData> implements Updatable, Json.Serializable, Disposable,
        ComponentWritable
{
    private String dimension;
    private int width;
    private int height;
    protected int blockWidth;
    protected int blockHeight;

    public BlockMatrixData(String dimension)
    {
        this.dimension = dimension;

        width = 0;
        height = 0;
        blockWidth = 0;
        blockHeight = 0;
    }

    public Map getMap()
    {
        return Map.Get(this.dimension);
    }

    @SuppressWarnings("unchecked")
    public <T extends Map> T getMap(Class<T> tClass)
    {
        return Map.Get(this.dimension, tClass);
    }

    public void setSize(Map map, int blockWidth, int blockHeight)
    {
        setSize(map, blockWidth, blockHeight, true);
    }

    public void setSize(Map map, int blockWidth, int blockHeight, boolean init)
    {
        clear();

        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;

        this.width = blockWidth * Constants.Core.CHUNK_SIZE;
        this.height = blockHeight * Constants.Core.CHUNK_SIZE;

        ensureCapacity(this.blockWidth * this.blockHeight);

        for (int j = 0; j < this.blockHeight; j++)
        {
            for (int i = 0; i < this.blockWidth; i++)
            {
                if (init)
                {
                    add(map.getChunkData(this, i, j));
                }
                else
                {
                    add(null);
                }
            }
        }
    }

    public boolean isInBlockRange(int x, int y)
    {
        return (x >= 0 && y >= 0 && x < blockWidth && y < blockHeight);
    }

    public boolean isInBlockRange(int x, int y, int width, int height)
    {
        return (x >= 0 && y >= 0 && x < blockWidth - width + 1 && y < blockHeight - height + 1);
    }

    public boolean isInRange(int x, int y, int layer)
    {
        return (layer >= 0 && layer < Constants.Layers.BLOCK_LAYERS_COUNT && x >= 0 && y >= 0 && x < width && y < height);
    }

    public ChunkData getChunkByBlock(int x, int y)
    {
        return getChunk(x / Constants.Core.CHUNK_SIZE, y / Constants.Core.CHUNK_SIZE);
    }

    public ChunkData getChunk(int x, int y)
    {
        if (!isInBlockRange(x, y)) return null;
        return get(x + blockWidth * y);
    }

    public ChunkData getChunkNoCheck(int x, int y)
    {
        return get(x + blockWidth * y);
    }

    public BlockData get(int x, int y, int layer)
    {
        if (!isInRange(x, y, layer)) return null;

        int blockX = x / Constants.Core.CHUNK_SIZE,
            blockY = y / Constants.Core.CHUNK_SIZE;

        ChunkData data = getChunk(blockX, blockY);

        return data.get(x % Constants.Core.CHUNK_SIZE, y % Constants.Core.CHUNK_SIZE, layer);
    }

    public void set(int x, int y, BlockData item, int layer, boolean updateCache)
    {
        if (!isInRange(x, y, layer)) return;

        int blockX = x / Constants.Core.CHUNK_SIZE,
            blockY = y / Constants.Core.CHUNK_SIZE;

        ChunkData data = getChunk(blockX, blockY);

        data.set(x, y, item, layer, updateCache);
    }

    public void setChunk(int x, int y, ChunkData chunk)
    {
        set(x + blockWidth * y, chunk);

        if (chunk != null)
        {
            chunk.updateInfo(x, y, this);
        }
    }

    public Renderable getRenderLayer(int layer)
    {
        return new Renderable()
        {
            @Override
            public void render(Batch batch, RenderContext context)
            {
                if (size == 0)
                    return;

                for (int j = 0; j < blockHeight; j++)
                {
                    for (int i = 0; i < blockWidth; i++)
                    {
                        ChunkData data = get(i + blockWidth * j);
                        if (data != null)
                        {
                            data.renderLayer(batch, context, layer);
                        }
                    }
                }
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
        };
    }

    @Override
    public void update(float dt)
    {
        for (int j = 0; j < blockHeight; j++)
        {
            for (int i = 0; i < blockWidth; i++)
            {
                ChunkData data = get(i + blockWidth * j);
                if (data != null)
                {
                    data.update(dt);
                }
            }
        }
    }

    @Override
    public void clear()
    {
        super.clear();

        blockHeight = 0;
        blockWidth = 0;
        width = 0;
        height = 0;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void refreshBlock(int x, int y, int layer)
    {
        refreshBlock(x, y, layer, true);
    }

    public void refreshBlock(int x, int y, int layer, boolean delayed)
    {
        BlockData data = get(x, y, layer);

        if (data != null)
        {
            if (delayed)
            {
                BrainOut.EventMgr.sendDelayedEvent(data, SimpleEvent.obtain(SimpleEvent.Action.refresh));
            }
            else
            {
                BrainOut.EventMgr.sendEvent(data, SimpleEvent.obtain(SimpleEvent.Action.refresh));
            }
        }

        ChunkData chunkData = getChunkByBlock(x, y);

        if (chunkData != null)
        {
            Map map = getMap();

            if (map == null)
                return;

            if (delayed)
            {
                BrainOut.EventMgr.sendDelayedEvent(chunkData, SetDirtyEvent.obtain(x, y, layer, map));
            }
            else
            {
                BrainOut.EventMgr.sendEvent(chunkData, SetDirtyEvent.obtain(x, y, layer, map));
            }
        }
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("width", blockWidth);
        json.writeValue("height", blockHeight);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        throw new RuntimeException("Do not ever call that");
    }

    public void read(Map map, Json json, JsonValue jsonData)
    {
        setSize(map, jsonData.get("width").asInt(), jsonData.get("height").asInt());

        JsonValue data = jsonData.get("chunks");

        if (data.size >= blockWidth * blockHeight)
        {
            JsonValue child = data.child();

            for (int j = 0, t = blockHeight; j < t; j++)
            {
                for (int i = 0, k = blockWidth; i < k; i++)
                {
                    getChunkNoCheck(i, j).read(json, child);

                    child = child.next();
                }
            }
        }
    }

    @Override
    public void dispose()
    {
        for (int j = 0, t = blockHeight; j < t; j++)
        {
            for (int i = 0, k = blockWidth; i < k; i++)
            {
                getChunkNoCheck(i, j).dispose();

            }
        }

        clear();
    }

    public void init()
    {
        for (int j = 0, t = blockHeight; j < t; j++)
        {
            for (int i = 0, k = blockWidth; i < k; i++)
            {
                getChunkNoCheck(i, j).init();
            }
        }

        for (int layer = 0; layer < Constants.Layers.BLOCK_LAYERS_COUNT; layer++)
        {
            for (int j = 0, t = getHeight(); j < t; j++)
            {
                for (int i = 0, k = getWidth(); i < k; i++)
                {
                    refreshBlock(i, j, layer, false);
                }
            }
        }
    }

    public int getBlockWidth()
    {
        return blockWidth;
    }

    public int getBlockHeight()
    {
        return blockHeight;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    public String getDimension()
    {
        return dimension;
    }

    public void setDimension(String dimension)
    {
        this.dimension = dimension;
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        write(json);

        json.writeArrayStart("chunks");
        for (int j = 0; j < blockHeight; j++)
        {
            for (int i = 0; i < blockWidth; i++)
            {
                json.writeObjectStart();
                getChunkNoCheck(i, j).write(json, componentWriter, owner);
                json.writeObjectEnd();
            }
        }
        json.writeArrayEnd();
    }
}
