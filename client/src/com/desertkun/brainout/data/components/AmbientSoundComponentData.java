package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.AmbientSoundComponent;
import com.desertkun.brainout.content.effect.Effect;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.active.AmbientSoundData;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("AmbientSoundComponent")
@ReflectAlias("data.components.AmbientSoundComponentData")
public class AmbientSoundComponentData extends ActiveEffectComponentData
{
    private final AmbientSoundData soundData;

    public AmbientSoundComponentData(AmbientSoundData soundData, AmbientSoundComponent ambientSound)
    {
        super(soundData, ambientSound);

        this.soundData = soundData;
    }

    @Override
    public void init()
    {
        super.init();

        updateSound();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeAction:
            {
                switch (((ActiveActionEvent) event).action)
                {
                    case updated:
                    {
                        updateSound();

                        break;
                    }
                }

                break;
            }
        }

        return false;
    }

    private void updateSound()
    {
        String soundName = soundData.getSound();

        try
        {
            Effect soundEffect = BrainOutClient.ContentMgr.get(soundName, Effect.class);

            updateEffect(soundEffect);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
