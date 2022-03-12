package com.desertkun.brainout.content.instrument;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.content.upgrades.UpgradableProperty;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;

@Reflect("content.instrument.Instrument")
public class Instrument extends ConsumableContent
{
    public static class InstrumentProperties implements Json.Serializable
    {
        private ObjectMap<String, UpgradableProperty> properties;

        public InstrumentProperties()
        {
            properties = new ObjectMap<>();
        }

        public void addProperty(UpgradableProperty property)
        {
            this.properties.put(property.getKey(), property);
        }

        public UpgradableProperty property(String key)
        {
            return properties.get(key);
        }

        protected void initProperties()
        {
            //
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {

        }
    }

    public InstrumentData getData(String dimension)
    {
        return new InstrumentData(this, dimension);
    }

    private Slot slot;
    private InstrumentSlotItem slotItem;
    private float speedCoef;
    private boolean forceSelect;
    private Skin defaultSkin;
    private Array<String> instrumentTags;
    private InstrumentProperties primaryProperties;

    public enum Action
    {
        reload,
        reloadSecondary,
        reloadBoth,
        shoot,
        hit,
        cock,
        cockSecondary,
        reset,
        buildUp,
        fetch,
        fetchSecondary,
        loadMagazineRound
    }

    public Instrument()
    {
        speedCoef = 1.0f;
        instrumentTags = new Array<>();
        primaryProperties = newProperties();
    }

    public Slot getSlot()
    {
        return slot;
    }

    public InstrumentSlotItem getSlotItem()
    {
        return slotItem;
    }

    public void setSlotItem(InstrumentSlotItem slotItem)
    {
        this.slotItem = slotItem;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        slot = ((Slot) BrainOut.ContentMgr.get(jsonData.getString("slot", "")));
        speedCoef = jsonData.getFloat("speedCoef", 1.0f);
        forceSelect = jsonData.getBoolean("forceSelect", false);

        if (jsonData.has("instrumentTags"))
        {
            if (jsonData.get("instrumentTags").isString())
            {
                instrumentTags.add(jsonData.get("instrumentTags").asString());
            }
            else
            {
                instrumentTags.addAll(jsonData.get("instrumentTags").asStringArray());
            }
        }

        if (jsonData.has("defaultSkin"))
        {
            defaultSkin = (Skin) BrainOut.ContentMgr.get(jsonData.getString("defaultSkin"));
        }
        else
        {
            throw new RuntimeException("No default skin passed for instrument: " + getID());
        }

        if (jsonData.has("primary"))
        {
            this.primaryProperties.read(json, jsonData.get("primary"));
        }
    }

    protected InstrumentProperties newProperties()
    {
        return new InstrumentProperties();
    }

    public InstrumentProperties getPrimaryProperties()
    {
        return primaryProperties;
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        initProperties();
    }

    protected void initProperties()
    {
        getPrimaryProperties().initProperties();
    }

    public float getSpeedCoef()
    {
        return speedCoef;
    }

    public boolean isForceSelect()
    {
        return forceSelect;
    }

    public Skin getDefaultSkin()
    {
        return defaultSkin;
    }

    public String getKillsStat()
    {
        return ContentLockTree.GetComplexValue("kills-from", getID());
    }

    public String getPartsStat()
    {
        return ContentLockTree.GetComplexValue("parts-of", getID());
    }

    public String getSkillStat()
    {
        return ContentLockTree.GetComplexValue("skills-with", getID());
    }

    public String getDurabilityStat()
    {
        return ContentLockTree.GetComplexValue("durability-of", getID());
    }

    public String getShotsStat()
    {
        return ContentLockTree.GetComplexValue("shots-from", getID());
    }

    public static String getTagKillsStat(String tag)
    {
        return ContentLockTree.GetComplexValue("kills-tag", tag);
    }

    public Array<String> getInstrumentTags()
    {
        return instrumentTags;
    }

    @Override
    public LocalizedString getTitle()
    {
        if (super.getTitle().isValid()) return super.getTitle();

        if (getDefaultSkin() != null)
            return getDefaultSkin().getTitle();

        return name;
    }
}
