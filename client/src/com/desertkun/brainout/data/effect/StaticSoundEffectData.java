package com.desertkun.brainout.data.effect;

import com.desertkun.brainout.content.effect.StaticSoundEffect;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.StaticSoundEffectData")
public class StaticSoundEffectData extends SoundEffectData
{
    public StaticSoundEffectData(StaticSoundEffect effect, LaunchData launchData)
    {
        super(effect, launchData);
    }

    @Override
    public void release()
    {
        if (isPlaying())
        {
            stop();
        }
    }

    @Override
    public boolean done()
    {
        return false;
    }

    @Override
    protected void updateSound()
    {
        if (isPlaying())
        {
            if (volume == 0)
            {
                stop();
            }
        }
        else
        {
            if (volume != 0)
            {
                play();
            }
        }

        super.updateSound();
    }
}
