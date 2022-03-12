package com.desertkun.brainout.content.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.ClientItemComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;

@Reflect("content.components.ClientItemComponent")
public class ClientItemComponent extends ContentComponent
{
    private boolean fetchIcon;
    private boolean iconBadge;
    private boolean discover;
    private float offsetY;
    private ObjectMap<String, String> placeholders;
    private LocalizedString title;

    public ClientItemComponent()
    {
    }

    @Override
    public ClientItemComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientItemComponentData((ItemData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    public LocalizedString getTitle()
    {
        return title;
    }

    public float getOffsetY()
    {
        return offsetY;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        fetchIcon = jsonData.getBoolean("fetchIcon", true);
        iconBadge = jsonData.getBoolean("iconBadge", true);
        discover = jsonData.getBoolean("discover", false);
        offsetY = jsonData.getFloat("offsetY", 0);

        if (jsonData.has("title"))
        {
            title = new LocalizedString(jsonData.getString("title"));
        }

        if (jsonData.has("placeholders"))
        {
            placeholders = new ObjectMap<>();

            for (JsonValue entry : jsonData.get("placeholders"))
            {
                placeholders.put(entry.name(), entry.asString());
            }
        }
    }

    public boolean isDiscover()
    {
        return discover;
    }

    public ObjectMap<String, String> getPlaceholders()
    {
        return placeholders;
    }

    public boolean isFetchIcon()
    {
        return fetchIcon;
    }

    public boolean isIconBadge()
    {
        return iconBadge;
    }
}
