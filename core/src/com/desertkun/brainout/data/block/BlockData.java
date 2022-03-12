package com.desertkun.brainout.data.block;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.BlockTextureComponent;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.components.ConnectionComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.*;
import com.desertkun.brainout.events.*;


public abstract class BlockData extends Data implements Json.Serializable, CacheUpdatable
{
    private static Vector2 TEMP_COLLIDE = new Vector2();

    public static int CURRENT_X, CURRENT_Y, CURRENT_LAYER;
    public static String CURRENT_DIMENSION;
    private int simple = -2;

    public interface ContactPayload {}

    public BlockData(Block creator)
    {
        super(creator, null);
    }

    public static BlockData GetNeighbor(Map map, int currentX, int currentY, int layer, int x, int y)
    {
        if (map == null)
            return null;

        return map.getBlock(currentX + x, currentY + y, layer);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case destroyBlock:
            {
                destroy(((DestroyBlockEvent) event));

                break;
            }

            case step:
            {
                StepEvent stepEvent = (StepEvent)event;

                BrainOut.EventMgr.sendDelayedEvent(this, LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.step,
                        stepEvent.launchData));

                break;
            }
        }

        return super.onEvent(event);
    }

    private boolean isMatchNeighbor(Map map, int currentX, int currentY, int layer, int x, int y)
    {
        if (getCreator().getContactTo() == null)
        {
            return false;
        }

        BlockData neighbor = GetNeighbor(map, currentX, currentY, layer, x, y);

        if (neighbor == null)
        {
            return false;
        }

        Block block = neighbor.getCreator();

        return block.getContactId() != null &&
            getCreator().getContactTo().indexOf(block.getContactId(), false) >= 0;
    }

    public byte calculateNeighborMask(Map map, int currentX, int currentY, int layer)
    {
        final byte a = (byte)(isMatchNeighbor(map, currentX, currentY, layer, 0, 1) ? 1 : 0);
        final byte b = (byte)(isMatchNeighbor(map, currentX, currentY, layer,1, 0) ? 2 : 0);
        final byte c = (byte)(isMatchNeighbor(map, currentX, currentY, layer, 0, -1) ? 4 : 0);
        final byte d = (byte)(isMatchNeighbor(map, currentX, currentY, layer, -1, 0) ? 8 : 0);

        return (byte)(a + b + c + d);
    }

    private void destroy(DestroyBlockEvent e)
    {
        Map map = e.map;

        if (map == null)
            return;

        BrainOut.EventMgr.sendDelayedEvent(this, LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.destroy,
            new PointLaunchData(e.x + 0.5f, e.y + 0.5f, 0, getDimension())));

        final int x = e.x, y = e.y, layer = e.layer;

        map.postRunnable(() -> map.setBlock(x, y, null, layer, true, false));
    }

    public Block getCreator() { return ((Block) getContent()); }

    public boolean isContact(
        ContactPayload payload, float x, float y, Vector2 speed,
        Vector2 impulse, float reduce, Map map, int blockX, int blockY)
    {
        return isContact(payload, x, y, speed, impulse, TEMP_COLLIDE, reduce, map, blockX, blockY);
    }

    public abstract boolean isContact(
        ContactPayload payload, float x, float y,
        Vector2 speed, Vector2 impulse, Vector2 moveForce,
        float reduce, Map map, int blockX, int blockY);

    public abstract boolean isFixture();

    public abstract LaunchData calculateContact(
        LaunchData launchFrom, LaunchData launchTo,
        boolean in, Map map, int blockX, int blockY);

    private int calculateSimple()
    {
        Component it = getFistComponent();

        if (it != null)
        {
            if (it instanceof Json.Serializable)
            {
                return -1;
            }

            it = it.getNext();
        }

        Map map = Map.Get(CURRENT_DIMENSION);

        if (map == null)
            return -1 ;

        Block creator = getCreator();
        int index = map.getContentIndex().getIndex(creator);
        if (index != 0)
        {
            return index;
        }
        else
        {
            return -1;
        }
    }

    public int simple()
    {
        if (simple == -2)
        {
            simple = calculateSimple();
        }

        return simple;
    }

    @Override
    public void write(Json json)
    {
        Map map = Map.Get(CURRENT_DIMENSION);

        if (map == null)
            return;

        Block creator = getCreator();

        int index = map.getContentIndex().getIndex(creator);
        
        if (index != 0)
        {
            json.writeValue("c", index);
        }
        else
        {
            json.writeValue("c", creator.getID());
        }
    }

    @Override
    public void cache(Map map, SpriteCache cache)
    {
        {
            BlockTextureComponent tx = getCreator().getComponent(BlockTextureComponent.class);

            if (tx != null)
            {
                cache.setColor(Color.WHITE);
                cache.add(tx.getRegion(), CURRENT_X, CURRENT_Y, 1, 1);
            }
        }

        ConnectionComponentData cn = getComponent(ConnectionComponentData.class);

        if (cn != null)
        {
            TextureRegion rg = cn.getRegion(map, CURRENT_X, CURRENT_Y, CURRENT_LAYER);
            if (rg != null)
            {
                cache.setColor(Color.WHITE);
                cache.add(rg, CURRENT_X, CURRENT_Y, 1, 1);
            }
        }
    }

    public void initAt(int x, int y, int layer)
    {
        CURRENT_X = x;
        CURRENT_Y = y;
        CURRENT_LAYER = layer;

        init();
    }

    @Override
    public void release()
    {
        if (!getCreator().isStatic())
        {
            super.release();
        }
    }

    @Override
    public boolean hasCache()
    {
        return true;
    }

    public float limitPower(float power)
    {
        return power;
    }

    public boolean isCopyable()
    {
        return true;
    }

    public boolean isRemovable()
    {
        return true;
    }

    public abstract boolean isConcrete();
}
