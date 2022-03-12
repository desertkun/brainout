package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class EffectSetGroup extends ObjectMap<String, EffectSet>
{
    public EffectSetGroup()
    {
    }

    public EffectSetGroup(String[] effects)
    {
        for (String effect : effects)
        {
            put(effect, new EffectSet());
        }
    }

    public EffectSetGroup(EffectSetGroup effects)
    {
        putAll(effects);
    }

    public void update(EffectSetGroup from)
    {
        putAll(from);
    }

    public void readAll(JsonValue jsonData)
    {
        for (JsonValue value : jsonData)
        {
            String key = value.name();
            EffectSet effectSet = new EffectSet();
            effectSet.read(value);

            put(key, effectSet);
        }
    }

    public void read(JsonValue jsonData)
    {
        for (Entry<String, EffectSet> entry : this)
        {
            if (jsonData.has(entry.key))
            {
                JsonValue ch = jsonData.get(entry.key);

                if (ch != null)
                {
                    entry.value.read(ch);
                }
            }
        }

        if (jsonData.has("custom"))
        {
            JsonValue custom = jsonData.get("custom");

            for (JsonValue entry : custom)
            {
                String name = entry.name();
                EffectSet effectSet = new EffectSet();
                effectSet.read(entry);

                put(name, effectSet);
            }
        }
    }

    public boolean hasEffects(String key)
    {
        return containsKey(key);
    }

    public void launchEffects(String key, LaunchData launchData)
    {
        if (key == null)
            return;

        EffectSet set = get(key);

        if (set != null)
        {
            set.launchEffects(launchData);
        }
    }

    public void launchEffects(String key, LaunchData launchData, Array<EffectData> res)
    {
        if (key == null)
            return;

        EffectSet set = get(key);

        if (set != null)
        {
            set.launchEffects(launchData, res);
        }
    }

    public void launchEffects(String key, EffectSet.EffectAttacher attacher)
    {
        if (key == null)
            return;

        EffectSet set = get(key);

        if (set != null)
        {
            set.launchEffects(attacher);
        }
    }

    public ClientMap launchEffects(String key, EffectSet.EffectAttacher attacher, Array<EffectData> res)
    {
        if (key == null)
            return null;

        EffectSet set = get(key);

        if (set != null)
        {
            return set.launchEffects(attacher, res);
        }

        return null;
    }
}
