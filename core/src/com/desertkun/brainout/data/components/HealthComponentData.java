package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.HealthComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.TimeoutFlag;

@Reflect("healthc")
@ReflectAlias("data.components.HealthComponentData")
public class HealthComponentData<T extends HealthComponent> extends Component<T> implements Json.Serializable, WithTag
{
    private TimeoutFlag immortalTime;
    private float health;
    private float initHealth;
    private boolean god;

    public HealthComponentData(ComponentObject componentObject, T healthComponent)
    {
        super(componentObject, healthComponent);

        this.health = healthComponent.getHealth();
        this.initHealth = healthComponent.getHealth();

        this.immortalTime = new TimeoutFlag(healthComponent.getImmortalTime());
    }

    public boolean isGod()
    {
        return god;
    }

    public void setGod(boolean god)
    {
        this.god = god;
    }

    public float addHealth(float health)
    {
        float freezeHealth = 0;
        TemperatureComponentData tcd = getComponentObject().getComponent(TemperatureComponentData.class);
        if (tcd != null)
            freezeHealth = tcd.getFreezing();

        float newValue = Math.min(health + getHealth(), getInitHealth() - freezeHealth);
        float oldValue = this.health;

        this.health = newValue;

        return newValue - oldValue;
    }

    public void damage(DamageEvent e)
    {
        if (isImmortal())
        {
            return;
        }

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

            Content content = null;

            if (e.bulletData != null)
            {
                content = e.bulletData.getContent();
            }

            BrainOut.EventMgr.sendDelayedEvent(DamagedEvent.obtain(getComponentObject(), health,
                e.x, e.y, e.angle,
                content, e.damageKind));

            if (health <= 0)
            {
                BrainOut.getInstance().postRunnable(() ->
                    onZeroHealth(e));
            }
        }
    }

    protected void onZeroHealth(DamageEvent e)
    {
        if (BrainOut.EventMgr.sendEvent(getComponentObject(), ZeroHealthEvent.obtain()))
        {
            // ZeroHealthEvent has been consumed so no reason to destroy this object
            return;
        }

        BrainOut.EventMgr.sendEvent(getComponentObject(),
            DestroyEvent.obtain(e.info, e.damager,
                e.x, e.y, e.angle, true));
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case damage:
            {
                DamageEvent damageEvent = (DamageEvent)event;
                damage(damageEvent);

                return true;
            }
        }

        return false;
    }

    public boolean isMaxHealth()
    {
        return getHealth() >= getInitHealth();
    }

    public float getHealth()
    {
        return health;
    }

    public float getInitHealth()
    {
        return initHealth;
    }

    public void setHealth(float health)
    {
        this.health = health;
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
        json.writeValue("ih", initHealth);
        json.writeValue("imm", immortalTime);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        health = jsonData.getFloat("h");
        initHealth = jsonData.getFloat("ih");
        immortalTime.setValue(jsonData.getFloat("imm", 0));
    }

    public boolean isImmortal()
    {
        return isGod() || !immortalTime.isGone();
    }

    public void setImmortalTime(float immortalTime)
    {
        this.immortalTime.setValue(immortalTime);
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.WITH_HEALTH) | WithTag.TAG(Constants.ActiveTags.RESOURCE_RECEIVER);
    }
}
