package com.desertkun.brainout.data.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.active.Portal;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.PortalData")
public class PortalData extends PointData
{
    private boolean locked;

    @InspectableProperty(name = "tag", kind = PropertyKind.string, value = PropertyValue.vString)
    public String tag;

    public PortalData(Portal portal, String dimension)
    {
        super(portal, dimension);

        locked = portal.hasLock();
        if (portal.getTag() != null)
        {
            tag = portal.getTag();
        }
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public boolean isLocked()
    {
        return locked;
    }

    public ConsumableContent getKey()
    {
        return ((Portal) getContent()).getKey();
    }

    public boolean isAllowSneak()
    {
        return ((Portal) getContent()).isAllowSneak();
    }

    public void setLocked(boolean locked)
    {
        this.locked = locked;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("tag", tag);
        json.writeValue("locked", locked);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        tag = jsonData.getString("tag", "");
        locked = jsonData.getBoolean("locked", locked);
    }

    @Override
    public String getEditorTitle()
    {
        return tag;
    }
}
