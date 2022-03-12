package com.desertkun.brainout.data.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.active.Sprite;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.inspection.*;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.SpriteData")
public class SpriteData extends PointData implements WithTag
{
    @InspectableProperty(name = "sprite", kind = PropertyKind.textureRegion, value = PropertyValue.vString)
    public String spriteName;

    @InspectableProperty(name = "scale", kind = PropertyKind.string, value = PropertyValue.vFloat)
    public float scale;

    @InspectableProperty(name = "flipX", kind = PropertyKind.checkbox, value = PropertyValue.vBoolean)
    public boolean flipX;

    @InspectableProperty(name = "flipY", kind = PropertyKind.checkbox, value = PropertyValue.vBoolean)
    public boolean flipY;

    private boolean cache;

    public SpriteData(Sprite sprite, String dimension)
    {
        super(sprite, dimension);

        spriteName = sprite.getSpriteName();
        scale = sprite.getScale();
        flipX = false;
        flipY = false;
        cache = true;
    }

    public float getScale()
    {
        return scale;
    }

    public String getSpriteName()
    {
        return spriteName;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("sname", spriteName);
        json.writeValue("sscale", scale);
        json.writeValue("flipX", flipX);
        json.writeValue("flipY", flipY);
        json.writeValue("cache", cache);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        spriteName = jsonData.getString("sname", ((Sprite) getContent()).getSpriteName());
        scale = jsonData.getFloat("sscale", ((Sprite) getContent()).getScale());
        flipX = jsonData.getBoolean("flipX", false);
        flipY = jsonData.getBoolean("flipY", false);
        cache = jsonData.getBoolean("cache", true);
    }

    public boolean isFlipX()
    {
        return flipX;
    }

    public boolean isFlipY()
    {
        return flipY;
    }

    @Override
    public int getTags()
    {
        return 0;
    }

    public boolean isCache()
    {
        return cache;
    }

    @InspectableGetter(name = "cache", kind = PropertyKind.checkbox, value = PropertyValue.vBoolean)
    public boolean getCache()
    {
        return cache;
    }

    @InspectableSetter(name = "cache")
    public void setCache(boolean cache)
    {
        this.cache = cache;

        updated();
    }
}
