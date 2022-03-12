package com.desertkun.brainout.data.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.ItemData")
public class ItemData extends PointData
{
    private ConsumableContainer records;
    private ObjectMap<String, String> props;
    private boolean autoRemove;
    private boolean hasItems;

    @InspectableProperty(name="Open Sound", kind= PropertyKind.select, value=PropertyValue.vString, className="content.effect.SoundEffect")
    public String openSound;

    @InspectableProperty(name="Close Sound", kind=PropertyKind.select, value=PropertyValue.vString, className="content.effect.SoundEffect")
    public String closeSound;

    @InspectableProperty(name="Tag", kind=PropertyKind.string, value=PropertyValue.vString)
    public String tag;

    public ItemData(Item active, String dimension)
    {
        super(active, dimension);

        this.records = new ConsumableContainer(this);
        this.props = null;
        this.autoRemove = active.isAutoRemove();
        this.tag = "";
    }

    public ConsumableContainer getRecords()
    {
        return records;
    }

    @Override
    public int getLayer()
    {
        return 2;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        records.read(json, jsonData);

        if (jsonData.has("prop"))
        {
            if (props == null)
            {
                props = new ObjectMap<>();
            }
            else
            {
                props.clear();
            }

            for (JsonValue prop : jsonData.get("prop"))
            {
                props.put(prop.name(), prop.asString());
            }
        }

        openSound = jsonData.getString("open-sound", null);
        closeSound = jsonData.getString("close-sound", null);
        tag = jsonData.getString("tag", "");
        hasItems = jsonData.getBoolean("hi", false);
    }

    @Override
    public int getZIndex()
    {
        return 80;
    }

    public boolean hasItems()
    {
        return hasItems;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("hi", records.size() > 0);
        records.write(json);

        if (props != null)
        {
            json.writeObjectStart("prop");
            for (ObjectMap.Entry<String, String> entry : props)
            {
                json.writeValue(entry.key, entry.value);
            }
            json.writeObjectEnd();
        }

        if (openSound != null)
            json.writeValue("open-sound", openSound);

        if (closeSound != null)
            json.writeValue("close-sound", closeSound);

        json.writeValue("tag", tag);
    }

    public void setProperty(String id, String value)
    {
        if (props == null)
            props = new ObjectMap<>();

        props.put(id, value);
    }

    public String getProperty(String id)
    {
        if (props == null)
            return null;

        return props.get(id);
    }

    public boolean hasProperty(String id)
    {
        return props != null && props.containsKey(id);
    }

    @Override
    public int getTags()
    {
        return super.getTags() | WithTag.TAG(Constants.ActiveTags.DETECTABLE);
    }

    public boolean isAutoRemove()
    {
        return autoRemove;
    }
}
