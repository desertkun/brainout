package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.effect.RandomSoundEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.StepPointData;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.RandomSoundEffectData")
public class RandomSoundEffectData extends EffectData implements Runnable
{
    protected final RandomSoundEffect soundEffect;
    protected Sound sound;
    protected long soundId;

    protected float volume;
    protected float pan;
    private float soundDistance;
    private float minimumDistance;

    private float updater;

    public RandomSoundEffectData(RandomSoundEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        soundEffect = effect;
        updater = 0;
        soundId = -1;
    }

    @Override
    public void init()
    {
        soundDistance = soundEffect.getSoundDistance();
        minimumDistance = soundEffect.getMinimumDistance();

        calculateSound();

        run();
    }

    @Override
    public void run()
    {
        if (volume != 0)
        {
            play();

            updateSound();
        }
    }

    public boolean isPlaying()
    {
        return soundId != -1 && sound != null;
    }

    protected void play()
    {
        if (soundId == -1)
        {
            sound = soundEffect.getSounds().random();
            soundId = sound.play();
            sound.setLooping(soundId, soundEffect.isLoop());
        }
    }

    protected void stop()
    {
        if (soundId != -1)
        {
            sound.stop(soundId);

            sound = null;
            soundId = -1;
        }
    }

    protected void updateSound()
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        if (isPlaying())
        {
            sound.setPan(soundId, pan, volume);

            float f = soundEffect.getPitch() != null ? soundEffect.getPitch().getValue() : 1.0f;
            sound.setPitch(soundId, map.getSpeed() * f);
        }
        else
        {
            if (volume > 0)
            {
                play();
                updateSound();
            }
        }
    }

    protected void calculateSound()
    {
        if (getLaunchData() instanceof StepPointData)
        {
            float v = getLaunchData().getX();
            volume = v * BrainOutClient.ClientSett.getSoundVolume().getFloatValue();
            this.pan = getLaunchData().getY();
        }
        else
        {
            ClientMap map = ((ClientMap) getMap());

            if (map == null)
                return;

            this.volume = 1;
            this.pan = 0;

            Watcher watcher = Map.GetWatcher();

            if (watcher != null && watcher.getDimension().equals(getDimension()))
            {
                float a = Vector2.len(watcher.getWatchX() - getX(), watcher.getWatchY() - getY());
                float d = Constants.Sound.SOUND_HEAR_DIST * soundDistance;
                float v = a / d;


                calculateDistance(v);
                if (minimumDistance != 0)
                {
                    if (a < minimumDistance)
                    {
                        volume *= Interpolation.exp10In.apply(a / minimumDistance);
                    }
                }

                pan = (float)Math.sqrt(Math.abs(watcher.getWatchX() - getX()) /
                        d);
                if (getX() < watcher.getWatchX()) pan = -pan;
            }
            else
            {
                volume = 0;
            }
        }
    }

    protected void calculateDistance(float v)
    {
        volume = Interpolation.circleIn.apply(MathUtils.clamp(1.0f - v, 0.0f, 1.0f));
        volume *= BrainOutClient.ClientSett.getSoundVolume().getFloatValue();
    }

    @Override
    public void release()
    {
        super.release();

        if (soundEffect.isLoop() && sound != null)
        {
            sound.stop(soundId);
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        //
    }

    @Override
    public void update(float dt)
    {
        updater -= dt;

        if (updater < 0)
        {
            updater = 0.025f;

            calculateSound();
            updateSound();
        }
    }

    @Override
    public boolean done()
    {
        return isDone() || !soundEffect.isLoop();
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    public float getSoundDistance()
    {
        return soundDistance;
    }

    public void setSoundDistance(float distance)
    {
        this.soundDistance = distance;
    }
}
