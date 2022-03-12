package com.desertkun.brainout.data.containers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.interfaces.ComponentWritable;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Updatable;
import com.desertkun.brainout.data.physics.PhysicChunk;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.SetDirtyEvent;
import com.desertkun.brainout.managers.ContentManager;

import java.lang.ref.WeakReference;

public class ChunkData implements Updatable, Json.Serializable, Disposable, EventReceiver, ComponentWritable
{
    private WeakReference<BlockMatrixData> matrixData;
    protected int idX, idY;
    private IntSet dirty;
    private boolean inited;
    private ObjectSet<ChunkFlag> flags;

    public enum ChunkFlag
    {
        indoor,
        shootingDisabled,
        hideOthers
    }

    public static class BlocksLayer implements Disposable
    {
        public Array<BlockData> blocks;
        public BlockDataContainer activeBlockList;
        public int layer;

        public BlocksLayer(int layer)
        {
            this.layer = layer;
            this.blocks = new Array<>(Constants.Core.CHUNK_SIZE_SQR);

            for (int j = 0; j < Constants.Core.CHUNK_SIZE_SQR; j++)
            {
                blocks.add(null);
            }

            this.activeBlockList = new BlockDataContainer(layer);
        }

        public BlockData getNoCheck(int x, int y)
        {
            return blocks.get(x + y * Constants.Core.CHUNK_SIZE);
        }

        public void init()
        {
            init(false);
        }

        public void init(boolean updateCache)
        {
        }

        public void updateCache(int layer) {}

        @Override
        public void dispose()
        {
            blocks.clear();
            activeBlockList.clear();
        }
    }

    protected Array<BlocksLayer> layers;
    protected Array<PhysicChunk> physicChunks;

    public ChunkData(BlockMatrixData matrixData, int idX, int idY)
    {
        this.matrixData = new WeakReference<>(matrixData);
        this.idX = idX;
        this.idY = idY;
        this.dirty = new IntSet();
        this.layers = new Array<>(Constants.Layers.BLOCK_LAYERS_COUNT);
        this.physicChunks = new Array<>(Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK_SQR);
        this.flags = null;

        this.layers.ensureCapacity(Constants.Layers.BLOCK_LAYERS_COUNT);

        for (int i = 0; i < Constants.Layers.BLOCK_LAYERS_COUNT; i++)
        {
            this.layers.add(null);
        }

        for (int j = 0; j < Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK; j++)
        {
            for (int i = 0; i < Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK; i++)
            {
                physicChunks.add(new PhysicChunk(this,
                    i + idX * Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK,
                    j + idY * Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK,
                    i,
                    j));
            }
        }
    }

    public boolean hasFlag(ChunkFlag flag)
    {
        if (flags == null)
            return false;

        return flags.contains(flag);
    }

    public void setFlag(ChunkFlag flag)
    {
        if (flags == null)
        {
            flags = new ObjectSet<>();
        }

        flags.add(flag);
    }

    public void removeFlag(ChunkFlag flag)
    {
        if (flags == null)
            return;

        flags.remove(flag);

        if (flags.size == 0)
        {
            flags = null;
        }
    }

    public void updateInfo(int x, int y, BlockMatrixData matrixData)
    {
        this.idX = x;
        this.idY = y;
        this.matrixData = new WeakReference<>(matrixData);

        for (int j = 0; j < Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK; j++)
        {
            for (int i = 0; i < Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK; i++)
            {
                PhysicChunk physicChunk = physicChunks.get(j * Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK + i);

                physicChunk.updateInfo(
                    i + idX * Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK,
                    j + idY * Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK
                );
            }
        }
    }

    public void init()
    {
        for (int layer = 0; layer < Constants.Layers.BLOCK_LAYERS_COUNT; layer++)
        {
            BlocksLayer b = layers.get(layer);

            if (b == null)
                continue;

            for (int j = 0; j < Constants.Core.CHUNK_SIZE; j++)
            {
                for (int i = 0; i < Constants.Core.CHUNK_SIZE; i++)
                {
                    BlockData item = b.getNoCheck(i, j);
                    if (item != null)
                    {
                        item.initAt(
                            idX * Constants.Core.CHUNK_SIZE + i,
                            idY * Constants.Core.CHUNK_SIZE + j,
                            layer
                        );
                    }
                }
            }
        }

        for (int j = 0; j < Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK; j++)
        {
            for (int i = 0; i < Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK; i++)
            {
                PhysicChunk chunk = getPhysicNoCheck(i, j);

                chunk.init();
            }
        }

        inited = true;
    }

    protected BlocksLayer newLayer(int layer)
    {
        return new BlocksLayer(layer);
    }

    public PhysicChunk getPhysic(int x, int y)
    {
        if (x < 0 | y < 0 |
            x >= Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK |
            y >= Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK)
        {
            return null;
        }

        return getPhysicNoCheck(x, y);
    }

    public PhysicChunk getPhysicNoCheck(int x, int y)
    {
        return physicChunks.get(x + y * Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK);
    }

    public boolean isInRange(int x, int y, int layer)
    {
        return (layer >= 0 &&
                layer < Constants.Layers.BLOCK_LAYERS_COUNT &&
                x >= 0 &&
                y >= 0 &&
                x < Constants.Core.CHUNK_SIZE &&
                y < Constants.Core.CHUNK_SIZE);
    }

    public BlockData get(int x, int y, int layer)
    {
        if (!isInRange(x, y, layer)) return null;
        BlocksLayer blocksLayer = layers.get(layer);
        if (blocksLayer == null) return null;
        return blocksLayer.blocks.get(x + y * Constants.Core.CHUNK_SIZE);
    }

    public Array<BlockData> getLayer(int layer)
    {
        BlocksLayer b = layers.get(layer);
        if (b == null) return null;
        return b.blocks;
    }

    public int getIndex(int x, int y)
    {
        return x + y * Constants.Core.CHUNK_SIZE;
    }

    public void set(int blockX, int blockY, BlockData item, int layer, boolean updateCache)
    {
        int x = blockX % Constants.Core.CHUNK_SIZE, y = blockY % Constants.Core.CHUNK_SIZE;

        BlocksLayer blocksLayer = layers.get(layer);

        if (blocksLayer == null)
        {
            blocksLayer = newLayer(layer);
            layers.set(layer, blocksLayer);

            if (inited)
            {
                BlocksLayer finalBlocksLayer = blocksLayer;
                BrainOut.getInstance().postRunnable(() -> finalBlocksLayer.init(true));
            }
        }

        BlockData before = get(x, y, layer);
        if (before != null)
        {
            blocksLayer.activeBlockList.removeItem(blockX, blockY, before);
            before.release();
        }

        blocksLayer.blocks.set(x + y * Constants.Core.CHUNK_SIZE, item);

        if (item != null)
        {
            blocksLayer.activeBlockList.addItem(blockX, blockY, item);
        }

        if (updateCache)
        {
            setDirty(x, y, layer);
        }
    }

    public void setDirty(int x, int y, int layer)
    {
        this.dirty.add(layer);

        PhysicChunk chunk = getPhysic(
                (x - idX * Constants.Core.CHUNK_SIZE) / Constants.Physics.PHYSIC_BLOCK_SIZE,
                (y - idY * Constants.Core.CHUNK_SIZE) / Constants.Physics.PHYSIC_BLOCK_SIZE);

        if (chunk != null)
        {
            chunk.setDirty();
        }
    }

    protected void updateCache(int layer) {}

    public void renderLayer(Batch batch, RenderContext context, int layer)
    {

    }

    @Override
    public void update(float dt)
    {
        for (BlocksLayer layer : layers)
        {
            if (layer == null)
                continue;

            layer.activeBlockList.update(dt);
        }

        if (dirty.size > 0)
        {
            IntSet.IntSetIterator it = dirty.iterator();

            while (it.hasNext)
            {
                updateCache(it.next());
            }

            dirty.clear();
        }

        for (int j = 0; j < Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK; j++)
        {
            for (int i = 0; i < Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK; i++)
            {
                PhysicChunk chunk = getPhysicNoCheck(i, j);

                if (chunk != null)
                {
                    chunk.update(dt);
                }
            }
        }
    }

    public int getIdX()
    {
        return idX;
    }

    public int getIdY()
    {
        return idY;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        JsonValue data = jsonData.get("data");

        if (data != null)
        {
            readLayer(json, data, Constants.Layers.BLOCK_LAYER_FOREGROUND);
        }
        else
        {
            data = jsonData.get("layers");
            if (data != null && data.isArray())
            {
                int i = 0;
                for (JsonValue layer : data)
                {
                    if (layer.isNull())
                    {
                        i++;
                        continue;
                    }

                    readLayer(json, layer, i++);
                }
            }
        }
    }

    public Map getMap()
    {
        BlockMatrixData blockMatrixData = matrixData.get();

        if (blockMatrixData == null)
            return null;

        return blockMatrixData.getMap();
    }

    private void readLayer(Json json, JsonValue data, int layer)
    {
        BlockMatrixData blockMatrixData = matrixData.get();

        if (blockMatrixData == null)
            return;

        BlocksLayer blocksLayer = layers.get(layer);

        if (blocksLayer == null)
        {
            blocksLayer = newLayer(layer);
            layers.set(layer, blocksLayer);

            if (inited)
            {
                Gdx.app.postRunnable(blocksLayer::init);
            }
        }

        if (data != null && data.size <= Constants.Core.CHUNK_SIZE_SQR)
        {
            Map map = blockMatrixData.getMap();

            if (map == null)
                return;

            int offsetX = getIdX() * Constants.Core.CHUNK_SIZE,
                offsetY = getIdY() * Constants.Core.CHUNK_SIZE;

            ContentManager cntMng = BrainOut.ContentMgr;

            JsonValue child = data.child();

            int i = 0;
            while (child != null)
            {
                int xi = i % Constants.Core.CHUNK_SIZE;
                int yi = i / Constants.Core.CHUNK_SIZE;

                if (child.isObject())
                {
                    int repeat = child.getInt("rpt", 0);
                    if (repeat > 0)
                    {
                        if (child.has("c"))
                        {
                            Block creator;

                            JsonValue clazz = child.get("c");
                            if (clazz.isNumber())
                            {
                                Content content = map.getContentIndex().getContent(clazz.asInt());
                                creator = ((Block) content);
                            }
                            else
                            {
                                String className = clazz.asString();
                                creator = (Block) cntMng.get(className);
                            }

                            if (creator != null)
                            {
                                i--;

                                for (int r = 0; r < repeat; r++)
                                {
                                    i++;
                                    xi = i % Constants.Core.CHUNK_SIZE;
                                    yi = i / Constants.Core.CHUNK_SIZE;

                                    int x = xi + offsetX, y = yi + offsetY;

                                    BlockData blockData = creator.getBlock();

                                    if (blockData != null)
                                    {
                                        set(x, y, blockData, layer, true);
                                    }
                                }
                            }
                            else
                            {
                                i += repeat - 1;
                            }
                        }
                        else
                        {
                            i += repeat - 1;
                        }

                    }
                    else
                    {
                        Block creator;

                        if (child.has("c"))
                        {
                            JsonValue clazz = child.get("c");
                            if (clazz.isNumber())
                            {
                                Content content = map.getContentIndex().getContent(clazz.asInt());
                                creator = ((Block) content);
                            }
                            else
                            {
                                String className = clazz.asString();
                                creator = (Block) cntMng.get(className);
                            }

                            if (creator != null)
                            {
                                int x = xi + offsetX, y = yi + offsetY;

                                BlockData blockData = creator.getBlock();

                                if (blockData != null)
                                {
                                    blockData.read(json, child);
                                    set(x, y, blockData, layer, true);
                                }
                            }
                        }
                    }
                }
                else if (child.isNumber())
                {
                    Block creator;

                    Content content = map.getContentIndex().getContent(child.asInt());
                    creator = ((Block) content);

                    if (creator != null)
                    {
                        int x = xi + offsetX, y = yi + offsetY;

                        BlockData blockData = creator.getBlock();

                        if (blockData != null)
                        {
                            set(x, y, blockData, layer, true);
                        }
                    }
                }

                child = child.next();
                i++;
            }

        }
    }

    @Override
    public void dispose()
    {
        for (BlocksLayer layer : layers)
        {
            if (layer != null)
            {
                layer.dispose();
            }
        }

        for (int j = 0; j < Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK; j++)
        {
            for (int i = 0; i < Constants.Physics.PHYSIC_BLOCKS_PER_CHUNK; i++)
            {
                PhysicChunk chunk = getPhysicNoCheck(i, j);

                chunk.dispose();
            }
        }

        layers.clear();
        matrixData.clear();
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case setDirty:
            {
                SetDirtyEvent setDirtyEvent = ((SetDirtyEvent) event);
                setDirty(setDirtyEvent.x, setDirtyEvent.y, setDirtyEvent.layer);

                return true;
            }
        }

        return false;
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        write(json);

        json.writeArrayStart("layers");

        for (int layer = 0, ttt = layers.size; layer < ttt; layer++)
        {
            BlocksLayer l = layers.get(layer);

            if (l == null)
            {
                json.writeValue(null);
                continue;
            }

            boolean hasBlock = false;

            for (BlockData block : l.blocks)
            {
                if (block != null)
                {
                    hasBlock = true;
                    break;
                }
            }

            if (!hasBlock)
            {
                json.writeValue(null);
                continue;
            }

            json.writeArrayStart();

            int i = 0;
            int repeatID = -1;
            int repeat = 0;
            int t = Constants.Core.CHUNK_SIZE * Constants.Core.CHUNK_SIZE;
            while (i < t)
            {
                int xi = i % Constants.Core.CHUNK_SIZE;
                int yi = i / Constants.Core.CHUNK_SIZE;

                BlockData blockData = l.getNoCheck(xi, yi);

                if (blockData != null)
                {
                    int bsimple = blockData.simple();
                    if (bsimple >= 0)
                    {
                        if (repeat == 0)
                        {
                            repeatID = bsimple;
                            repeat = 1;
                        }
                        else
                        {
                            if (repeatID == bsimple)
                            {
                                repeat++;
                            }
                            else
                            {
                                if (repeat == 1)
                                {
                                    if (repeatID >= 0)
                                    {
                                        json.writeValue(repeatID);
                                    }
                                    else
                                    {
                                        json.writeValue(null);
                                    }
                                }
                                else
                                {
                                    json.writeObjectStart();
                                    json.writeValue("rpt", repeat);
                                    if (repeatID >= 0)
                                    {
                                        json.writeValue("c", repeatID);
                                    }
                                    json.writeObjectEnd();
                                }

                                repeatID = bsimple;
                                repeat = 1;
                            }
                        }
                    }
                    else
                    {
                        if (repeat > 0)
                        {
                            if (repeat == 1)
                            {
                                if (repeatID >= 0)
                                {
                                    json.writeValue(repeatID);
                                }
                                else
                                {
                                    json.writeValue(null);
                                }
                            }
                            else
                            {
                                json.writeObjectStart();
                                json.writeValue("rpt", repeat);
                                if (repeatID >= 0)
                                {
                                    json.writeValue("c", repeatID);
                                }
                                json.writeObjectEnd();
                            }
                            repeat = 0;
                        }

                        json.writeObjectStart();
                        blockData.write(json, componentWriter, owner);
                        json.writeObjectEnd();
                    }
                }
                else
                {
                    if (repeat == 0)
                    {
                        repeatID = -1;
                        repeat = 1;
                    }
                    else
                    {
                        if (repeatID == -1)
                        {
                            repeat++;
                        }
                        else
                        {
                            if (repeat == 1)
                            {
                                json.writeValue(repeatID);
                            }
                            else
                            {
                                json.writeObjectStart();
                                json.writeValue("rpt", repeat);
                                json.writeValue("c", repeatID);
                                json.writeObjectEnd();
                            }

                            repeatID = -1;
                            repeat = 1;
                        }
                    }
                }

                i++;
            }

            if (repeat > 0)
            {
                if (repeat == 1)
                {
                    if (repeatID >= 0)
                    {
                        json.writeValue(repeatID);
                    }
                    else
                    {
                        json.writeValue(null);
                    }
                }
                else
                {
                    json.writeObjectStart();
                    json.writeValue("rpt", repeat);
                    if (repeatID >= 0)
                    {
                        json.writeValue("c", repeatID);
                    }
                    json.writeObjectEnd();
                    repeat = 0;
                }
            }

            json.writeArrayEnd();
        }

        json.writeArrayEnd();
    }

    public Array<BlocksLayer> getLayers()
    {
        return layers;
    }
}
