package com.desertkun.brainout.data.effect;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.effect.MixedSoundEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.MixedSoundEffectData")
public class MixedSoundEffectData extends SoundEffectData
{
    private final MixedSoundEffect effect;
    private float distantVolume;
    private long distantSoundId;

    public MixedSoundEffectData(MixedSoundEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        this.effect = effect;
        this.distantVolume = 0;
    }

    @Override
    public void run()
    {
        super.run();

        if (distantVolume != 0)
        {
            ClientMap map = ((ClientMap) getMap());

            if (map == null)
                return;

            distantSoundId = effect.getDistantSound().play();
            effect.getSound().setPan(distantSoundId, pan, distantVolume);
            effect.getSound().setPitch(distantSoundId, map.getSpeed());
        }
    }

    @Override
    protected void calculateDistance(float v)
    {
        super.calculateDistance(v);

        float from = effect.getDistantChangeFrom();
        float to = effect.getDistantChangeTo();

        if (v > from && v < to)
        {
            float fade = ((v - from) / (to - from));
            distantVolume = volume * fade;
            volume = volume * (1.0f - fade);
        }
        else if (v > to)
        {
            distantVolume = volume;
            volume = 0;
        }

        distantVolume *= BrainOutClient.ClientSett.getSoundVolume().getFloatValue();
    }
}
