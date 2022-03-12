package com.desertkun.brainout.data.active;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.AddWeightComponent;
import com.desertkun.brainout.content.components.SimplePhysicsComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.components.ColliderComponentData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.components.VisibilityComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.*;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.PlayerData")
public class PlayerData extends PointData
{
    private LaunchData launchData;
    private PointLaunchData tmp;
    private InstrumentData currentInstrument;
    private InstrumentData hookedInstrument;
    private float speedCoef;
    private boolean wounded;
    private ObjectMap<String, String> customAnimationSlots;

    private Player.State state;

    public PlayerData(Player player, String dimension)
    {
        super(player, dimension);

        this.launchData = new ActiveLaunchData(this);
        this.currentInstrument = null;
        this.hookedInstrument = null;
        this.state = Player.State.normal;
        this.speedCoef = 1.0f;
        this.tmp = new PointLaunchData(0, 0, 0, dimension);
    }

    public boolean isWounded()
    {
        return wounded;
    }

    public void setWounded(boolean wounded)
    {
        this.wounded = wounded;

        updateWounded();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case setInstrument:
            {
                setCurrentInstrument(((SetInstrumentEvent) event).selected);

                // continue to components
                break;
            }

            case hookInstrument:
            {
                setHookedInstrument(((HookInstrumentEvent) event).selected);

                // continue to components
                break;
            }

            case damaged:
            {
                DamagedEvent damaged = ((DamagedEvent) event);

                tmp.set(damaged.x, damaged.y, damaged.angle);
                tmp.setDimension(getDimension());

                super.onEvent(LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.custom, tmp, damaged.damageKind));

                break;
            }
        }

        if (super.onEvent(event))
        {
            return true;
        }

        if (currentInstrument != null)
        {
            if (currentInstrument.onEvent(event))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("sst", state.toString());
        json.writeValue("spd", speedCoef);

        if (wounded)
        {
            json.writeValue("w", true);
        }

        if (customAnimationSlots != null)
        {
            json.writeObjectStart("cas");
            for (ObjectMap.Entry<String, String> entry : customAnimationSlots)
            {
                json.writeValue(entry.key, entry.value);
            }
            json.writeObjectEnd();
        }
    }

    public void setCustomAnimationSlot(String key, String value)
    {
        if (customAnimationSlots == null)
        {
            customAnimationSlots = new ObjectMap<>();
        }

        customAnimationSlots.put(key, value);
    }

    public ObjectMap<String, String> getCustomAnimationSlots()
    {
        return customAnimationSlots;
    }

    @Override
    protected boolean getVisibility(ComponentWriter componentWriter, int owner)
    {
        boolean visible = isVisible();

        if (!visible)
        {
            return false;
        }

        VisibilityComponentData v = getComponentWithSubclass(VisibilityComponentData.class);

        if (v != null)
        {
            return v.isVisibleTo(owner);
        }

        return true;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        // read sst before because that might affect position and super.read will perfectly adjust that
        if (jsonData.has("sst"))
        {
            setState(Player.State.valueOf(jsonData.getString("sst")));
        }

        if (jsonData.has("cas"))
        {
            if (customAnimationSlots == null)
            {
                customAnimationSlots = new ObjectMap<>();
            }
            else
            {
                customAnimationSlots.clear();
            }

            for (JsonValue value : jsonData.get("cas"))
            {
                customAnimationSlots.put(value.name, value.asString());
            }
        }
        else
        {
            customAnimationSlots = null;
        }

        super.read(json, jsonData);

        wounded = jsonData.getBoolean("w", false);
        speedCoef = jsonData.getFloat("spd", 1.0f);
    }

    private void updateWounded()
    {
        SimplePhysicsComponentData phy = getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy != null)
        {
            phy.setCanFix(!wounded);
        }
    }

    public LaunchData getLaunchData()
    {
        return launchData;
    }

    public Player getPlayer()
    {
        return ((Player) getContent());
    }

    public InstrumentData getCurrentInstrument()
    {
        return currentInstrument;
    }

    public InstrumentData getHookedInstrument()
    {
        return hookedInstrument;
    }

    public void setCurrentInstrument(InstrumentData currentInstrument)
    {
        if (this.currentInstrument != null && this.currentInstrument != currentInstrument)
        {
            Event e = SimpleEvent.obtain(SimpleEvent.Action.deselected);
            if (e != null)
            {
                this.currentInstrument.onEvent(e);
                e.free();
            }
        }

        this.currentInstrument = currentInstrument;
    }

    public void setHookedInstrument(InstrumentData hookedInstrument)
    {
        if (this.hookedInstrument != null && this.hookedInstrument != hookedInstrument)
        {
            Event e = SimpleEvent.obtain(SimpleEvent.Action.deselected);
            if (e != null)
            {
                this.hookedInstrument.onEvent(e);
                e.free();
            }
        }

        this.hookedInstrument = hookedInstrument;
    }

    @Override
    public int getZIndex()
    {
        // players is always on top of the screen
        return 100;
    }

    public Player.State getState()
    {
        return state;
    }

    public void setState(Player.State state)
    {
        this.state = state;

        SimplePhysicsComponentData pcd = getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (pcd != null)
        {
            Player.PlayerState currentState = getCurrentState();

            if (currentState != null)
            {
                Vector2 oldSize = pcd.getSize();
                // move position according to new size
                pcd.setX(pcd.getX() + (currentState.width - oldSize.x) / 2f);
                pcd.setY(pcd.getY() + (currentState.height - oldSize.y) / 2f);

                pcd.setSize(currentState.width, currentState.height);
            }
        }

        updateCollider();
    }

    @Override
    public void init()
    {
        super.init();

        updateCollider();
    }

    private void updateCollider()
    {
        ColliderComponentData cc = getComponent(ColliderComponentData.class);

        if (cc != null)
        {
            Player.PlayerState currentState = getCurrentState();

            if (currentState != null)
            {
                for (ObjectMap.Entry<String, ColliderComponentData.Collider> entry : cc.getColliders())
                {
                    Player.PlayerState.ColliderState collider = currentState.colliders.get(entry.key);

                    if (collider == null)
                        continue;

                    entry.value.update(collider);
                }
            }
        }
    }

    @Override
    public void release()
    {
        super.release();

        if (currentInstrument != null)
        {
            currentInstrument.setOwner(null);
        }

        if (hookedInstrument != null)
        {
            hookedInstrument.setOwner(null);
        }
    }

    public Player.PlayerState getCurrentState()
    {
        return getPlayer().getState(state);
    }

    public float getSpeedCoef()
    {
        return speedCoef;
    }

    public void setSpeedCoef(float speedCoef)
    {
        this.speedCoef = speedCoef;
    }

    @Override
    public int getTags()
    {
        return super.getTags()
                | WithTag.TAG(Constants.ActiveTags.PLAYERS)
                | WithTag.TAG(Constants.ActiveTags.DETECTABLE);
    }

    @Override
    public void setDimension(int newId, String dimension, boolean notify)
    {
        super.setDimension(newId, dimension, notify);

        PlayerOwnerComponent poc = getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            for (ObjectMap.Entry<Integer, ConsumableRecord> recordEntry :
                poc.getConsumableContainer().getData())
            {
                ConsumableRecord record = recordEntry.value;
                ConsumableItem item = record.getItem();

                if (item instanceof InstrumentConsumableItem)
                {
                    InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);

                    ici.getInstrumentData().setDimension(dimension);
                }
            }
        }

        if (currentInstrument != null)
        {
            currentInstrument.setDimension(dimension);
        }

        if (hookedInstrument != null)
        {
            hookedInstrument.setDimension(dimension);
        }
    }

    public boolean canRun()
    {
        return true;
    }

    public float getMaxWeight()
    {
        float maxWeight = getPlayer().getMaxWeight();

        PlayerOwnerComponent poc = getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            {
                ObjectMap<Integer, ConsumableRecord> backpack = poc.getConsumableContainer().getCategory("backpack");

                if (backpack != null)
                {
                    for (ObjectMap.Entry<Integer, ConsumableRecord> entry : backpack)
                    {
                        Content c = entry.value.getItem().getContent();

                        AddWeightComponent w = c.getComponent(AddWeightComponent.class);

                        if (w != null)
                        {
                            maxWeight += w.getWeight(entry.value.getQuality());
                        }
                    }
                }
            }
            {
                ObjectMap<Integer, ConsumableRecord> backpack = poc.getConsumableContainer().getCategory("armor");

                if (backpack != null)
                {
                    for (ObjectMap.Entry<Integer, ConsumableRecord> entry : backpack)
                    {
                        Content c = entry.value.getItem().getContent();

                        AddWeightComponent w = c.getComponent(AddWeightComponent.class);

                        if (w != null)
                        {
                            maxWeight += w.getWeight(entry.value.getQuality());
                        }
                    }
                }
            }
        }

        return maxWeight;
    }

    public float getMaxOverweight()
    {
        float maxWeight = getPlayer().getMaxOverweight();

        PlayerOwnerComponent poc = getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            {
                ObjectMap<Integer, ConsumableRecord> backpack = poc.getConsumableContainer().getCategory("backpack");

                if (backpack != null)
                {
                    for (ObjectMap.Entry<Integer, ConsumableRecord> entry : backpack)
                    {
                        Content c = entry.value.getItem().getContent();

                        if (c != null)
                        {
                            AddWeightComponent w = c.getComponent(AddWeightComponent.class);

                            if (w != null)
                            {
                                maxWeight += w.getWeight(entry.value.getQuality());
                            }
                        }
                    }
                }
            }
            {
                ObjectMap<Integer, ConsumableRecord> backpack = poc.getConsumableContainer().getCategory("armor");

                if (backpack != null)
                {
                    for (ObjectMap.Entry<Integer, ConsumableRecord> entry : backpack)
                    {
                        Content c = entry.value.getItem().getContent();

                        if (c == null)
                            continue;

                        AddWeightComponent w = c.getComponent(AddWeightComponent.class);

                        if (w != null)
                        {
                            maxWeight += w.getWeight(entry.value.getQuality());
                        }
                    }
                }
            }
        }

        return maxWeight;
    }
}
