package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class GamePadKeycodeProperty extends IntegerProperty implements Property.KeyProperty
{
    private final GamePadKeyProperties properties;

    private GamePadKeyProperties.Keys key;
    private boolean stick;
    private boolean stickOnly;

    public GamePadKeycodeProperty(String name, String localization, Integer def, GamePadKeyProperties.Keys key,
                                  boolean stickOnly)
    {
        super(name, localization, def);

        setKey(key);

        this.properties = null;
        setStickOnly(stickOnly);
    }

    public GamePadKeycodeProperty(String name, String localization, Integer def,
                                  GamePadKeyProperties properties, GamePadKeyProperties.Keys key,
                                  boolean stickOnly)
    {
        super(name, localization, def, properties);

        setKey(key);

        this.properties = properties;
        setStickOnly(stickOnly);
    }

    @Override
    public void write(Json json)
    {
        json.writeObjectStart(getName());

        json.writeValue("value", getValue());
        json.writeValue("stick", isStick());

        json.writeObjectEnd();
    }

    public void setStickOnly(boolean stickOnly)
    {
        this.stickOnly = stickOnly;
    }

    public boolean isStickOnly()
    {
        return stickOnly;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        JsonValue child = jsonData.get(getName());

        if (child == null)
            return;

        setValue(child.getInt("value", getValue()));
        setStick(child.getBoolean("stick", false));
    }

    public GamePadKeyProperties.Keys getKey()
    {
        return key;
    }

    public void setStick(boolean stick)
    {
        this.stick = stick;
    }

    public boolean isStick()
    {
        return stick;
    }

    public void setKey(GamePadKeyProperties.Keys key)
    {
        this.key = key;
    }

    @Override
    public void setKeyValue(int keycode)
    {
        setValue(keycode);

        if (properties != null)
        {
            properties.update();
        }
    }
}
