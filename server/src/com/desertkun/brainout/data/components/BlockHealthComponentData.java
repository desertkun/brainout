package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.common.msg.server.BlockDestroyMsg;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.BlockHealthComponent;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.*;

import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("BlockHealthComponent")
@ReflectAlias("data.components.BlockHealthComponentData")
public class BlockHealthComponentData extends Component<BlockHealthComponent> implements Json.Serializable, WithTag
{
    private float health;

    public BlockHealthComponentData(BlockData blockData, BlockHealthComponent component)
    {
        super(blockData, component);

        this.health = component.getHealth();
    }

    public BlockData getBlockData()
    {
        return ((BlockData) getComponentObject());
    }


    public float addHealth(float health)
    {
        float newValue = health + getHealth();
        float oldValue = this.health;

        this.health = newValue;

        return newValue - oldValue;
    }

    public void damage(DamageBlockEvent e)
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return;

        if (!gameMode.isGameActive(true, true))
            return;

        float damage = e.damage;

        if (e.bulletData != null || e.info != null)
        {
            String id = e.bulletData != null ? e.bulletData.getContent().getID() :
                    e.info.instrument.getID();

            Float damageCoef = getContentComponent().getDamageCoef().get(id);

            if (damageCoef == null)
            {
                damageCoef = getContentComponent().getDamageCoef().get("*");
            }

            if (damageCoef != null)
            {
                damage *= damageCoef;
            }
        }

        if (health > 0)
        {
            health -= damage;

            if (health <= 0)
            {
                destroy(e);
            }
        }
    }

    protected void destroy(DamageBlockEvent e)
    {
        BrainOut.EventMgr.sendDelayedEvent(getComponentObject(),
            DestroyBlockEvent.obtain(e.map, e.x, e.y, e.layer, true));
    }

    public float getHealth()
    {
        return health;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case damageBlock:
            {
                DamageBlockEvent damageEvent = (DamageBlockEvent)event;
                damage(damageEvent);

                return true;
            }
        }

        return false;
    }


    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("h", health);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        health = jsonData.getFloat("h");
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.WITH_HEALTH);
    }
}
