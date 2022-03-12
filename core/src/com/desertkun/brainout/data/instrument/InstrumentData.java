package com.desertkun.brainout.data.instrument;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.components.interfaces.AffectedByUpgrades;
import com.desertkun.brainout.content.components.interfaces.UpgradeComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.OwnerChangedEvent;

import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.instrument.InstrumentData")
public class InstrumentData extends Data implements Json.Serializable
{
    private ActiveData owner;
    private boolean upgradesInited;
    private InstrumentInfo info;
    private boolean forceSelect;

    public InstrumentData(Instrument instrument, String dimension)
    {
        super(instrument, dimension);

        this.upgradesInited = false;
        this.info = new InstrumentInfo();
        this.info.instrument = instrument;

        setForceSelect(instrument.isForceSelect());
    }

    public void setOwner(ActiveData owner)
    {
        if (owner != this.owner)
        {
            this.owner = owner;

            BrainOut.EventMgr.sendDelayedEvent(this, OwnerChangedEvent.obtain(owner));
        }
    }

    @Override
    public boolean disposeOnRelease()
    {
        return false;
    }

    public ActiveData getOwner()
    {
        return owner;
    }

    @Override
    public String getDimension()
    {
        if (owner != null)
        {
            return owner.getDimension();
        }

        return super.getDimension();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        // map.newInstrument

        if (jsonData.has("skin"))
        {
            JsonValue skin = jsonData.get("skin");
            if (skin.isNumber())
            {
                info.skin = BrainOut.getInstance().getController().getContentFromIndex(skin.asInt(), Skin.class);
            }
            else if (skin.isString())
            {
                info.skin = BrainOut.ContentMgr.get(skin.asString(), Skin.class);
            }
            else
            {
                info.skin = null;
            }
        }

        if (jsonData.has("u"))
        {
            info.upgrades.clear();

            for (JsonValue u: jsonData.get("u"))
            {
                Upgrade upgrade = BrainOut.ContentMgr.get(u.asString(), Upgrade.class);

                if (upgrade == null)
                    continue;

                info.upgrades.put(u.name(), upgrade);
            }
        }
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("class", info.instrument.getID());

        if (info.skin != null)
        {
            json.writeValue("skin", info.skin.getID());
        }

        json.writeObjectStart("u");
        for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
        {
            if (entry.value == null)
                continue;

            json.writeValue(entry.key, entry.value.getID());
        }
        json.writeObjectEnd();
    }

    public void attachUpgrade(String key, Upgrade upgrade)
    {
        getUpgrades().put(key, upgrade);

        upgrade.preApply(this);
        upgrade.postApply(this);

        for (ContentComponent component : upgrade.getComponents() )
        {
            if (component instanceof UpgradeComponent)
            {
                UpgradeComponent upg = (UpgradeComponent) component;

                if (!upg.pre())
                {
                    upg.upgrade(this);
                }
            }
        }

        Component it = getFistComponent();

        while (it != null)
        {
            if (it instanceof AffectedByUpgrades)
            {
                AffectedByUpgrades upg = (AffectedByUpgrades) it;
                upg.upgraded(getUpgrades());
            }

            it = it.getNext();
        }

        BrainOut.EventMgr.sendEvent(this, SimpleEvent.obtain(SimpleEvent.Action.upgradesUpdated));
    }

    @Override
    public void init()
    {
        if (!upgradesInited)
        {
            if (getContent().getComponents() != null)
            {
                for (ContentComponent component : getContent().getComponents() )
                {
                    if (component instanceof UpgradeComponent)
                    {
                        UpgradeComponent upg = (UpgradeComponent) component;

                        if (upg.pre())
                        {
                            upg.upgrade(this);
                        }
                    }
                }
            }

            for (Upgrade upgrade : info.upgrades.values())
            {
                if (upgrade == null)
                    continue;

                upgrade.preApply(this);
            }
        }

        super.init();

        if (!upgradesInited)
        {
            
            for (ObjectMap.Entry<String, Upgrade> entry : new ObjectMap.Entries<>(info.upgrades))
            {
                entry.value.postApply(this);
            }

            if (getContent().getComponents() != null)
            {
                for (ContentComponent component : getContent().getComponents() )
                {
                    if (component instanceof UpgradeComponent)
                    {
                        UpgradeComponent upg = (UpgradeComponent) component;

                        if (!upg.pre())
                        {
                            upg.upgrade(this);
                        }
                    }
                }
            }
        }

        upgradesInited = true;
    }

    public Instrument getInstrument()
    {
        return info.instrument;
    }

    public void setSkin(Skin skin)
    {
        this.info.skin = skin;
    }

    public ObjectMap<String, Upgrade> getUpgrades()
    {
        return info.upgrades;
    }

    public InstrumentInfo getInfo()
    {
        return info;
    }

    public void setInfo(InstrumentInfo info)
    {
        this.info = info;
    }

    public boolean isForceSelect()
    {
        return forceSelect;
    }

    public void setForceSelect(boolean forceSelect)
    {
        this.forceSelect = forceSelect;
    }
}
