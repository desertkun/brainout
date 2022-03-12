package com.desertkun.brainout.client.settings;

import com.desertkun.brainout.L;

public class KeycodeProperty extends IntegerProperty implements Property.KeyProperty
{
    private final KeyProperties properties;
    private KeyProperties.Keys key;

    public KeycodeProperty(String name, String localization, Integer def, KeyProperties.Keys key)
    {
        super(name, localization, def);

        setKey(key);

        this.properties = null;
    }

    public KeycodeProperty(String name, String localization, Integer def, KeyProperties properties, KeyProperties.Keys key)
    {
        super(name, localization, def, properties);

        setKey(key);

        this.properties = properties;
    }

    public KeyProperties.Keys getKey()
    {
        return key;
    }

    public void setKey(KeyProperties.Keys key)
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
