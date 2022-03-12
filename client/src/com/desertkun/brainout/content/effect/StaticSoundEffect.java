package com.desertkun.brainout.content.effect;

import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.StaticSoundEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.StaticSoundEffect")
public class StaticSoundEffect extends SoundEffect
{

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new StaticSoundEffectData(this, launchData);
    }

}
