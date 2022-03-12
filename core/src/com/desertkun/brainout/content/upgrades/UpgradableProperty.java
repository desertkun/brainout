package com.desertkun.brainout.content.upgrades;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.Arrays;

@Reflect("content.upgrades.UpgradableProperty")
public class UpgradableProperty
{
    private final String key;
    private String value;

    public UpgradableProperty(String key, float defaultValue)
    {
        this.key = key;
        this.value = String.valueOf(defaultValue);
    }

    public UpgradableProperty(String key, String defaultValue)
    {
        this.key = key;
        this.value = defaultValue;
    }

    public <T extends Enum<?>> UpgradableProperty(String key, Array<T> arrayOfEnums)
    {
        this.key = key;
        this.value = arrayOfEnums.toString(",");
    }

    public UpgradableProperty(UpgradableProperty other)
    {
        this.key = other.key;
        this.value = other.asString();
    }

    public UpgradableProperty(UpgradableProperty other, Iterable<Upgrade> upgrades)
    {
        this.key = other.key;
        this.value = other.asString();

        init(upgrades);
    }

    public UpgradableProperty(String key, float defaultValue, Iterable<Upgrade> upgrades)
    {
        this(key, defaultValue);

        init(upgrades);
    }

    public UpgradableProperty(String key, String defaultValue, Iterable<Upgrade> upgrades)
    {
        this(key, defaultValue);

        init(upgrades);
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public void setValue(float value)
    {
        this.value = String.valueOf(value);
    }

    public void init(Iterable<Upgrade> upgrades)
    {
        if (upgrades == null)
        {
            return;
        }

        for (Upgrade upgrade : upgrades)
        {
            if (upgrade == null)
                continue;

            Upgrade.UpgradeProperty property = upgrade.getProperty(key);

            if (property != null)
            {
                value = property.apply(value);
            }
        }
    }

    public <T extends Content> T asContent(Class<T> tClass)
    {
        return BrainOut.ContentMgr.get(value, tClass);
    }

    public float asFloat()
    {
        try
        {
            return Float.valueOf(value);
        }
        catch (NumberFormatException ignored)
        {
            return 0;
        }
    }

    public <T extends Enum<T>> Array<T> asEnumArray(Class<T> tClass)
    {
        Array<T> array = new Array<>();

        for (String s : value.split(","))
        {
            array.add(Enum.valueOf(tClass, s));
        }

        return array;
    }

    public int asInt()
    {
        try
        {
            return (int)(float)Float.valueOf(value);
        }
        catch (NumberFormatException ignored)
        {
            return 0;
        }
    }

    public boolean asBoolean()
    {
        return !(asInt() == 0 || value.equals("false"));
    }

    public String asString()
    {
        return value;
    }

    public String getKey()
    {
        return key;
    }

    public void set(UpgradableProperty property)
    {
        this.value = property.asString();
    }
}
