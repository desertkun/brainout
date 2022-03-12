package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import java.util.Random;

public class EffectSet
{
    private final Array<Array<EffectAttach>> effects;
    private Random random;

    public class EffectAttach
    {
        public final Effect effect;
        public final String attachTo;

        public EffectAttach(Effect effect, String attachTo)
        {
            this.effect = effect;
            this.attachTo = attachTo;
        }
    }

    public static abstract class EffectAttacher
    {
        public abstract LaunchData attachTo(String attachObject);
        public abstract LaunchData attachDefault();

        public void bindEffect(String key, float time, EffectData effectData) {}
        public EffectData getBoundEffect(String key) { return null; }
        public void updateBoundEffects(float dt) {}
    }

    public EffectSet()
    {
        random = new Random();
        effects = new Array<>();
    }

    public void launchEffects(LaunchData launchData)
    {
        ClientMap map = Map.Get(launchData.getDimension(), ClientMap.class);

        if (map == null)
            return;

        if (effects.size > 0)
        {
            int effectId = random.nextInt(effects.size);
            Array<EffectAttach> effects = this.effects.get(effectId);

            for (EffectAttach effect : effects)
            {
                map.addEffect(effect.effect, launchData);
            }
        }
    }

    public void launchEffects(LaunchData launchData, Array<EffectData> res)
    {
        ClientMap map = Map.Get(launchData.getDimension(), ClientMap.class);

        if (map == null)
            return;

        if (effects.size > 0)
        {
            int effectId = random.nextInt(effects.size);
            Array<EffectAttach> effects = this.effects.get(effectId);

            for (EffectAttach effect : effects)
            {
                res.add(map.addEffect(effect.effect, launchData));
            }
        }
    }

    public void launchEffects(EffectAttacher effectAttacher)
    {
        if (effects.size > 0)
        {
            int effectId = random.nextInt(effects.size);
            Array<EffectAttach> effects = this.effects.get(effectId);

            for (EffectAttach effect : effects)
            {
                LaunchData launchData;

                if (effect.attachTo.isEmpty())
                {
                    launchData = effectAttacher.attachDefault();
                }
                else
                {
                    launchData = effectAttacher.attachTo(effect.attachTo);
                }

                if (launchData != null)
                {
                    ClientMap map = Map.Get(launchData.getDimension(), ClientMap.class);

                    if (map != null)
                    {
                        map.addEffect(effect.effect, launchData, effectAttacher);
                    }
                }
            }
        }
    }

    public ClientMap launchEffects(EffectAttacher effectAttacher, Array<EffectData> res)
    {
        if (effects.size > 0)
        {
            int effectId = random.nextInt(effects.size);
            Array<EffectAttach> effects = this.effects.get(effectId);

            for (EffectAttach effect : effects)
            {
                LaunchData launchData;

                if (effect.attachTo.isEmpty())
                {
                    launchData = effectAttacher.attachDefault();
                }
                else
                {
                    launchData = effectAttacher.attachTo(effect.attachTo);
                }

                if (launchData != null)
                {
                    ClientMap map = Map.Get(launchData.getDimension(), ClientMap.class);

                    if (map != null)
                    {
                        res.add(map.addEffect(effect.effect, launchData, effectAttacher));
                    }

                    return map;
                }
            }
        }

        return null;
    }

    public EffectAttach getEffectAttach(JsonValue value)
    {
        if (value.isString())
        {
            Effect effect = (Effect) BrainOut.ContentMgr.get(value.asString());

            if (effect != null)
            {
                return new EffectAttach(effect, "");
            }
        }

        if (value.isObject())
        {
            Effect effect = (Effect) BrainOut.ContentMgr.get(value.getString("object"));

            if (effect != null)
            {
                return new EffectAttach(effect, value.getString("attachTo"));
            }
        }

        return null;
    }

    public void read(JsonValue jsonData)
    {
        if (jsonData == null)
        {
            return;
        }

        if (jsonData.isArray())
        {
            for (JsonValue values : jsonData)
            {
                Array<EffectAttach> fillTo = new Array<>();

                if (values.isArray())
                {
                    for (JsonValue value : values)
                    {
                        EffectAttach effectAttach = getEffectAttach(value);
                        if (effectAttach != null)
                        {
                            fillTo.add(effectAttach);
                        }
                    }
                }
                else
                {
                    EffectAttach effectAttach = getEffectAttach(values);
                    if (effectAttach != null)
                    {
                        fillTo.add(effectAttach);
                    }
                }

                effects.add(fillTo);
            }
        }
        else
        {
            Array<EffectAttach> fillTo = new Array<>();

            EffectAttach effectAttach = getEffectAttach(jsonData);
            if (effectAttach != null)
            {
                fillTo.add(effectAttach);
            }

            effects.add(fillTo);
        }
    }
}
