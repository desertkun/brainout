package com.desertkun.brainout.data.active;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.data.components.HealthComponentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.*;
import com.desertkun.brainout.inspection.*;
import com.esotericsoftware.minlog.Log;

public abstract class ActiveData extends Data implements
    Json.Serializable, Renderable, Updatable, Inspectable, WithTag
{
    private final Active active;
    private int id;
    private int ownerId;
    private LastHitInfo lastHitInfo;
    private boolean alive;
    private boolean visible;
    private boolean detectable;
    private String name;

    private int zIndex;
    private byte layer;

    private static Vector2 tmp = new Vector2();

    @InspectableGetter(name = "layer", kind = PropertyKind.select, value = PropertyValue.vEnum)
    public ActiveLayer getEditorLayer()
    {
        return ActiveLayer.values()[layer];
    }

    @InspectableSetter(name = "layer")
    public void setEditorLayer(ActiveLayer layer)
    {
        this.layer = (byte)layer.ordinal();
    }


    @InspectableGetter(name = "id", kind = PropertyKind.string, value = PropertyValue.vString)
    public String getNameId()
    {
        return name;
    }

    @InspectableSetter(name = "id")
    public void setNameId(String id)
    {
        if (id.equals(""))
        {
            if (this.name != null)
            {
                Map map = getMap();

                if (map != null)
                {
                    map.getActiveNameIndex().remove(this.name);
                    this.name = null;
                }
            }

            return;
        }

        this.name = id;

        Map map = getMap();
        if (map != null)
        {
            map.getActiveNameIndex().put(this.name, this);
        }
    }

    @Override
    @InspectableGetter(name = "zIndex", kind = PropertyKind.string, value = PropertyValue.vInt)
    public int getZIndex()
    {
        return zIndex;
    }

    @Override
    public int getLayer()
    {
        return layer;
    }

    public void setLayer(int layer)
    {
        this.layer = (byte)layer;
    }

    @InspectableSetter(name = "zIndex")
    public void setzIndex(int zIndex)
    {
        if (this.zIndex != zIndex)
        {
            this.zIndex = zIndex;

            if (isAlive())
            {
                Map map = getMap();
                if (map != null)
                {
                    map.sortActives(getLayer());
                }
            }
        }
    }

    private Team team;

    @InspectableGetter(name = "x", kind = PropertyKind.string, value = PropertyValue.vFloat)
    public abstract float getX();

    @InspectableGetter(name = "y", kind = PropertyKind.string, value = PropertyValue.vFloat)
    public abstract float getY();

    @InspectableSetter(name = "x")
    public abstract void setX(float x);

    @InspectableSetter(name = "y")
    public abstract void setY(float y);

    public abstract float getAngle();

    public boolean hover(float x, float y) { return false; }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public boolean isDetectable()
    {
        return detectable;
    }

    public void setDetectable(boolean detectable)
    {
        this.detectable = detectable;
    }

    public enum LastHitKind
    {
        normal,
        headshot
    }

    public class LastHitInfo
    {
        public int hitterId;
        public LastHitKind kind;
        public InstrumentInfo instrument;
        public Bullet bullet;
        public boolean silent;

        public LastHitInfo()
        {
            hitterId = -1;
            instrument = null;
            bullet = null;
            kind = LastHitKind.normal;
            silent = false;
        }
    }

    public void setPosition(float x, float y)
    {
        setX(x);
        setY(y);
    }

    public void setAngle(float angle) {}

    @Override
    public void init()
    {
        super.init();

        BrainOut.EventMgr.subscribe(Event.ID.damaged, this);

        alive = true;
    }

    public void updated()
    {
        BrainOut.EventMgr.sendDelayedEvent(ActiveActionEvent.obtain(this, ActiveActionEvent.Action.updated));
    }

    @Override
    public void release()
    {
        super.release();

        BrainOut.EventMgr.unsubscribe(Event.ID.damaged, this);

        alive = false;
    }

    public ActiveData(Active active, String dimension)
    {
        super(active, dimension);

        this.id = -1;
        this.active = active;
        this.ownerId = -1;
        this.zIndex = 0;
        this.layer = 1;
        this.lastHitInfo = new LastHitInfo();
        this.visible = true;
        this.detectable = true;

        if (active != null)
        {
            this.zIndex = active.getzIndex();
        }

        if (active != null && active.getTeam() != null)
        {
            this.team = active.getTeam();
        }
    }

    @InspectableGetter(name = "team", kind = PropertyKind.select, value = PropertyValue.vContent)
    public Team getTeam()
    {
        return team;
    }

    @InspectableSetter(name = "team")
    public void setTeam(Team team)
    {
        this.team = team;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        if (active != null)
        {
            json.writeValue("class", active.getID());
        }

        if (ownerId != -1)
        {
            json.writeValue("owner", ownerId);
        }

        if (team != null)
        {
            json.writeValue("team", team.getID());
        }

        if (name != null)
        {
            json.writeValue("id", name);
        }

        json.writeValue("zIndex", zIndex);
        json.writeValue("layer", layer);
    }

    @Override
    public void write(Json json, ComponentWriter componentWriter, int owner)
    {
        json.writeValue("v", getVisibility(componentWriter, owner));

        super.write(json, componentWriter, owner);
    }

    protected boolean getVisibility(ComponentWriter componentWriter, int owner)
    {
        return visible;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case destroy:
            {
                // call the components listeners first
                super.onEvent(event);

                Map map = getMap();

                DestroyEvent e = (DestroyEvent) event;

                if (map != null)
                    map.removeActive(this, e.notify, true, e.ragdoll);

                return true;
            }

            case damaged:
            {
                DamagedEvent damaged = ((DamagedEvent) event);

                if (damaged.data == this)
                {
                    HealthComponentData hcd = getComponent(HealthComponentData.class);

                    if (hcd != null)
                    {
                        hcd.setHealth(damaged.health);
                    }
                }

                break;
            }
        }

        return super.onEvent(event);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        visible = jsonData.getBoolean("v", true);

        if (jsonData.has("owner"))
        {
            this.ownerId = jsonData.getInt("owner");
        }

        if (jsonData.has("team"))
        {
            JsonValue t = jsonData.get("team");

            if (t.isString())
            {
                this.team = (Team)BrainOut.ContentMgr.get(t.asString());
            }
            else if (t.isNumber())
            {
                this.team = BrainOut.getInstance().getController().getContentFromIndex(t.asInt(), Team.class);
            }
            else
            {
                this.team = null;
            }
        }
        else
        {
            this.team = null;
        }

        if (jsonData.has("id"))
        {
            setNameId(jsonData.getString("id", ""));
        }

        if (jsonData.has("layer"))
        {
            int layer = jsonData.getInt("layer", 0);

            if (layer != this.layer)
            {
                if (isAlive())
                {
                    Map map = getMap();

                    if (map != null)
                        map.reattachActive(getId(), layer);
                }

                this.setLayer(layer);
            }
        }

        if (jsonData.has("zIndex"))
        {
            this.setzIndex(jsonData.getInt("zIndex", 0));
        }

        super.read(json, jsonData);
    }

    public Active getCreator()
    {
        return active;
    }

    public String toString()
    {
        return (name != null ? "[" + name + "] " : "") + active.getTitle().get();
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(int ownerId)
    {
        this.ownerId = ownerId;
    }

    public LastHitInfo getLastHitInfo()
    {
        return lastHitInfo;
    }

    public boolean isAlive()
    {
        return alive;
    }

    public boolean isVisible(ActiveData other)
    {
        tmp.set(other.getX(), other.getY());
        tmp.sub(getX(), getY());

        Map map = getMap();

        if (map == null)
            return false;

        return !map.trace(getX(), getY(), Constants.Layers.BLOCK_LAYER_UPPER, tmp.angleDeg(), tmp.len(), null) &&
            !map.trace(getX(), getY(), Constants.Layers.BLOCK_LAYER_FOREGROUND, tmp.angleDeg(), tmp.len(), null);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (!visible) return;

        super.render(batch, context);
    }

    public String getEditorTitle()
    {
        return null;
    }

    @Override
    public int getTags()
    {
        int tags = 0;

        Component it = getFistComponent();

        while (it != null)
        {
            if (it instanceof WithTag)
            {
                tags |= ((WithTag) it).getTags();
            }

            it = it.getNext();
        }

        return tags;
    }

    public void setDimension(int newId, String dimension)
    {
        setDimension(newId, dimension, true);
    }

    public void setDimension(int newId, String dimension, boolean notify)
    {
        if (!dimension.equals(getDimension()))
        {
            Map oldMap = Map.Get(getDimension());
            Map newMap = Map.Get(dimension);

            if (oldMap != null && newMap != null)
            {
                int oldId = getId();

                oldMap.relocateActive(this, newMap, newId);

                if (notify)
                {
                    BrainOut.EventMgr.sendEvent(
                            ActiveChangeDimensionEvent.obtain(this, oldId, getDimension(), dimension));
                }
            }
            else
            {
                if (Log.INFO) Log.info("Cannot relocate active " + newId + " to dimension " + dimension);
            }
        }

        super.setDimension(dimension);
    }
}
