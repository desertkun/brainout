package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.Portal")
public class Portal extends Active
{
    private ConsumableContent key;
    private boolean allowSneak;
    private String tag;

    public Portal()
    {
        key = null;
        allowSneak = true;
    }

    @Override
    public PortalData getData(String dimension)
    {
        return new PortalData(this, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("key"))
        {
            key = BrainOut.ContentMgr.get(jsonData.getString("key"), ConsumableContent.class);
        }

        allowSneak = jsonData.getBoolean("allow-sneak", allowSneak);
        if (jsonData.has("tag"))
        {
            tag = jsonData.getString("tag");
        }
    }

    public String getTag()
    {
        return tag;
    }

    public boolean hasLock()
    {
        return key != null;
    }

    public ConsumableContent getKey()
    {
        return key;
    }

    public boolean isAllowSneak()
    {
        return allowSneak;
    }

    @Override
    public boolean isEditorSelectable()
    {
        return true;
    }
}
