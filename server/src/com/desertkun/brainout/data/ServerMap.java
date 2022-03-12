package com.desertkun.brainout.data;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.content.components.ServerItemComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.BlockMatrixData;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.inspection.Inspectable;
import com.desertkun.brainout.inspection.props.PropertiesRegistration;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.server.ServerConstants;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.HashSet;
import java.util.Set;

@Reflect("data.ServerMap")
public class ServerMap extends Map implements Inspectable
{
    public static final int RESIZE_ALIGN_LEFT = -1;
    public static final int RESIZE_ALIGN_RIGHT = 1;
    public static final int RESIZE_ALIGN_CENTER = 0;

    private static int _DimensionIdGenerator = 0;

    private static int GenerateNextDimensionId()
    {
        return _DimensionIdGenerator++;
    }

    private WayPointMap wayPointMap;
    private Set<String> personalRequestOnly = null;

    public ServerMap(String dimension, int width, int height)
    {
        this(dimension, width, height, true);
    }

    public ServerMap(String dimension, int width, int height, boolean init)
    {
        super(dimension, width, height, init);

        wayPointMap = new WayPointMap(this);
        dimensionId = GenerateNextDimensionId();
        DimensionIds.put(dimensionId, dimension);
    }

    public boolean needWayPoints()
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return true;

        return ((ServerRealization) gameMode.getRealization()).needWayPoints();
    }

    public void setPersonalRequestOnly(String for_)
    {
        if (personalRequestOnly == null)
        {
            personalRequestOnly = new HashSet<>();
        }

        personalRequestOnly.add(for_);
    }

    public static String GetTargetMapForPersonalUse(String location, String id)
    {
        return "real-" + location + "-" + id + "-estate";
    }

    public void setPersonalRequestOnly()
    {
        if (personalRequestOnly == null)
        {
            personalRequestOnly = new HashSet<>();
        }
    }

    public boolean isPersonalRequestOnly()
    {
        return personalRequestOnly != null;
    }

    public boolean suitableForPersonalRequestFor(String for_)
    {
        return personalRequestOnly != null && personalRequestOnly.contains(for_);
    }

    public Set<String> getSuitableForPersonalRequests()
    {
        return personalRequestOnly;
    }

    public WayPointMap getWayPointMap()
    {
        return wayPointMap;
    }

    @Override
    protected void readDimension(Json json, JsonValue jsonData)
    {
        // no dimension reading for you, leads to overlaps
    }

    public ServerMap(String dimension)
    {
        this(dimension, 0, 0);
    }

    private static Item getDefaultDropItem()
    {
        return (Item) BrainOutServer.ContentMgr.get(Constants.Drop.DEFAULT_DROP_ITEM);
    }

    public static ItemData dropItem(String dimension,
        Array<ConsumableRecord> records,
        int ownerId,
        float x, float y, float angle)
    {
        Item dropItem = getDefaultDropItem();

        return dropItem(dimension, dropItem, records, ownerId, x, y, angle);
    }

    public static ItemData dropItem(String dimension, Item dropItem, Array<ConsumableRecord> records,
                                    int ownerId,
                                    float x, float y, float angle)
    {

        float speed = ServerConstants.Drop.DROP_SPEED_THROW;

        if (dropItem != null)
        {
            ServerItemComponent serverItemComponent = dropItem.getComponentFrom(ServerItemComponent.class);
            if (serverItemComponent != null)
            {
                speed = serverItemComponent.getDropSpeed();
            }
        }

        return dropItem(dimension, dropItem, records, ownerId, x, y, angle, speed);
    }

    public static ItemData dropItem(String dimension, Item dropItem, Array<ConsumableRecord> records,
                                    int ownerId,
                                    float x, float y, float angle, float speed)
    {
        Map map = Map.Get(dimension);

        if (map == null)
            return null;

        if (dropItem == null)
            dropItem = getDefaultDropItem();

        ItemData itemData = dropItem.getData(dimension);
        itemData.setOwnerId(ownerId);

        SimplePhysicsComponentData phy = itemData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        double a = Math.toRadians(angle);

        itemData.setPosition(x, y);
        phy.getSpeed().set(
                (float)Math.cos(a) * speed,
                (float)Math.sin(a) * speed
        );

        for (ConsumableRecord record : records)
        {
            record.setId(itemData.getRecords().newId());
            itemData.getRecords().addRecord(record);
        }

        map.addActive(map.generateServerId(), itemData, true);

        return itemData;
    }

    @Override
    protected boolean validateActive(Active active)
    {
        if (active instanceof Player)
        {
            // do not load player objects
            return false;
        }

        if (active instanceof ThrowableActive)
        {
            // do not load thrown objects
            return false;
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode != null)
        {
            return gameMode.validateActive(active);
        }

        return super.validateActive(active);
    }

    public void resize(int width, int height, int alignX, int alignY)
    {
        Array<ChunkData> existingChunks = new Array<>(blocks);

        alignX = (int)Math.signum(alignX);
        alignY = (int)Math.signum(alignY);

        int oldWidth = blocks.getBlockWidth(),
            oldHeight = blocks.getBlockHeight();

        blocks.setSize(this, width, height, false);

        for (ObjectMap.Entry<Integer, ActiveData> entry : getActives())
        {
            ActiveData activeData = entry.value;

            int offsetX, offsetY;

            switch (alignX)
            {
                case RESIZE_ALIGN_LEFT:
                {
                    offsetX = 0;
                    break;
                }
                case RESIZE_ALIGN_RIGHT:
                {
                    offsetX = (width - oldWidth);
                    break;
                }
                case RESIZE_ALIGN_CENTER:
                default:
                {
                    offsetX = (width - oldWidth) / 2;
                }
            }

            switch (alignY)
            {
                case RESIZE_ALIGN_LEFT:
                {
                    offsetY = 0;
                    break;
                }
                case RESIZE_ALIGN_RIGHT:
                {
                    offsetY = (height - oldHeight);
                    break;
                }
                case RESIZE_ALIGN_CENTER:
                default:
                {
                    offsetY = (height - oldHeight) / 2;
                }
            }

            activeData.setPosition(
                activeData.getX() + offsetX * Constants.Core.CHUNK_SIZE,
                activeData.getY() + offsetY * Constants.Core.CHUNK_SIZE
            );
        }

        for (int j = 0; j < oldHeight; j++)
        {
            for (int i = 0; i < oldWidth; i++)
            {
                ChunkData chunk = existingChunks.get(i + j * oldWidth);

                if (chunk == null)
                    continue;

                int newX, newY;

                switch (alignX)
                {
                    case RESIZE_ALIGN_LEFT:
                    {
                        newX = i;
                        break;
                    }
                    case RESIZE_ALIGN_RIGHT:
                    {
                        newX = i + (width - oldWidth);
                        break;
                    }
                    case RESIZE_ALIGN_CENTER:
                    default:
                    {
                        newX = i + (width - oldWidth) / 2;
                    }
                }

                switch (alignY)
                {
                    case RESIZE_ALIGN_LEFT:
                    {
                        newY = j;
                        break;
                    }
                    case RESIZE_ALIGN_RIGHT:
                    {
                        newY = j + (height - oldHeight);
                        break;
                    }
                    case RESIZE_ALIGN_CENTER:
                    default:
                    {
                        newY = j + (height - oldHeight) / 2;
                    }
                }

                if (newX < 0 || newY < 0 || newX >= width || newY >= height)
                    continue;

                blocks.setChunk(newX, newY, chunk);
            }
        }

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                ChunkData chunk = getChunk(i, j);

                if (chunk != null)
                    continue;

                ChunkData newChunk = new ChunkData(blocks, i, j);
                newChunk.init();

                blocks.setChunk(i, j, newChunk);
            }
        }

        existingChunks.clear();
    }

    @Override
    public void init()
    {
        super.init();

        if (needWayPoints())
        {
            wayPointMap.init();
            wayPointMap.generate();
        }
    }

    @Override
    public ChunkData getChunkData(BlockMatrixData matrixData, int x, int y)
    {
        return new ServerChunkData(matrixData, x, y);
    }

    public boolean moveChunks(int fromX, int fromY, ServerMap targetMap, int toX, int toY, int width, int height)
    {
        if (!blocks.isInBlockRange(fromX, fromY, width, height))
            return false;

        if (!targetMap.blocks.isInBlockRange(toX, toY, width, height))
            return false;

        Array<ChunkData> chunks = new Array<>(width * height);

        for (int j = fromY; j < fromY + height; j++)
        {
            for (int i = fromX; i < fromX + width; i++)
            {
                chunks.add(blocks.getChunk(i, j));
                blocks.setChunk(i, j, null);
            }
        }

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                ChunkData chunk = chunks.get(i + j * width);
                if (chunk == null)
                    continue;

                ChunkData old = targetMap.blocks.getChunk(toX + i, toY + j);
                if (old != null)
                {
                    old.dispose();
                }

                targetMap.blocks.setChunk(toX + i, toY + j, chunk);
            }
        }

        float moveOffsetX = (toX - fromX) * Constants.Core.CHUNK_SIZE;
        float moveOffsetY = (toY - fromY) * Constants.Core.CHUNK_SIZE;

        boolean differentDimension = targetMap != this;

        for (ObjectMap.Entry<Integer, ActiveData> entry : getActives())
        {
            ActiveData activeData = entry.value;

            if (activeData == null)
                continue;

            float x = activeData.getX(), y = activeData.getY();

            if (x >= fromX * Constants.Core.CHUNK_SIZE && x < ((fromX + width) * Constants.Core.CHUNK_SIZE) &&
                y >= fromY * Constants.Core.CHUNK_SIZE && y < ((fromY + height) * Constants.Core.CHUNK_SIZE))
            {
                activeData.setPosition(x + moveOffsetX, y + moveOffsetY);

                if (differentDimension)
                {
                    activeData.setDimension(targetMap.generateServerId(), targetMap.getDimension(), false);
                }
            }
        }

        chunks.clear();

        for (int j = 0; j < blocks.getBlockHeight(); j++)
        {
            for (int i = 0; i < blocks.getBlockWidth(); i++)
            {
                ChunkData chunk = getChunk(i, j);

                if (chunk != null)
                    continue;

                ChunkData newChunk = new ChunkData(blocks, i, j);
                newChunk.init();

                blocks.setChunk(i, j, newChunk);
            }
        }

        return true;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (wayPointMap != null)
        {
            wayPointMap.update(dt);
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();

        this.wayPointMap.release();
        this.wayPointMap = null;
    }

    @Override
    public void postRunnable(Runnable runnable)
    {
        BrainOutServer.PostRunnable(runnable);
    }

    @Override
    public void inspect(PropertiesRegistration registration)
    {
        //
    }
}
