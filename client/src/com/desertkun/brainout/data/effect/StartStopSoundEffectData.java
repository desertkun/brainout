package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.effect.StartStopSoundEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.StartStopSoundEffectData")
public class StartStopSoundEffectData extends EffectData implements Runnable, CancellableEffect
{
    protected final StartStopSoundEffect soundEffect;
    protected long soundId, prevSoundId;

    protected float volume;
    protected float pan;
    protected boolean released;

    private float updater;
    private float timer, breakingTimer;
    private Stage stage;
    private Sound prevSound;

    private float soundDistance;

    public enum Stage
    {
        start,
        loop,
        stop,
        done
    }

    public StartStopSoundEffectData(StartStopSoundEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        soundEffect = effect;
        updater = 0;
        soundId = -1;
        stage = Stage.start;
    }

    @Override
    public void init()
    {
        soundDistance = soundEffect.getSoundDistance();

        calculateSound();
        run();
    }

    @Override
    public void run()
    {
        if (volume != 0)
        {
            play();
            updateSound(soundId);
        }
    }

    public boolean isPlaying()
    {
        return soundId != -1;
    }

    protected void play()
    {
        if (soundId == -1)
        {
            switch (stage)
            {
                case start:
                {
                    Sound sound = soundEffect.getStartSound();
                    soundId = sound.play();
                    timer = soundEffect.getStartTime();
                    sound.setLooping(soundId, false);

                    break;
                }
                case loop:
                {
                    Sound sound = soundEffect.getLoopSound();
                    soundId = sound.play();
                    sound.setLooping(soundId, true);

                    break;
                }
                case stop:
                {
                    Sound sound = soundEffect.getStopSound();
                    soundId = sound.play();
                    timer = soundEffect.getStopTime();
                    sound.setLooping(soundId, false);

                    break;
                }

            }

        }
    }

    private Sound getStageSound()
    {
        switch (stage)
        {
            case start:
            {
                return soundEffect.getStartSound();
            }
            case loop:
            {
                return soundEffect.getLoopSound();
            }
            case stop:
            {
                return soundEffect.getStopSound();
            }
            default:
            {
                return null;
            }
        }
    }

    protected void stop()
    {
        if (soundId != -1)
        {
            Sound sound = getStageSound();

            if (sound != null)
            {
                sound.stop(soundId);
            }

            soundId = -1;
        }
    }

    protected void updateSound(long soundId)
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        if (isPlaying())
        {
            Sound sound = getStageSound();

            if (sound != null)
            {
                sound.setPan(soundId, pan, volume);
                float f = soundEffect.getPitch() != null ? soundEffect.getPitch().getValue() : 0;
                sound.setPitch(soundId, map.getSpeed() + f);
            }
        }
        else
        {
            if (volume > 0)
            {
                play();
                updateSound(soundId);
            }
        }
    }

    protected void calculateSound()
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        this.volume = 1;
        this.pan = 0;

        Watcher watcher = Map.GetWatcher();

        if (watcher != null && watcher.getDimension().equals(getDimension()))
        {
            float d = Constants.Sound.SOUND_HEAR_DIST * soundDistance;

            float v = (Vector2.len(watcher.getWatchX() - getX(), watcher.getWatchY() - getY()) /
                d);

            calculateDistance(v);

            pan = (float)Math.sqrt(Math.abs(watcher.getWatchX() - getX()) /
                d);
            if (getX() < watcher.getWatchX()) pan = -pan;
        }
        else
        {
            volume = 0;
        }
    }

    protected void calculateDistance(float v)
    {
        volume = Math.max(0, Math.min(1, - (float)Math.log10(v) / 2));

        volume *= BrainOutClient.ClientSett.getSoundVolume().getFloatValue();
    }

    @Override
    public void release()
    {
        super.release();

        stop();
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        //
    }

    @Override
    public void update(float dt)
    {
        switch (stage)
        {
            case start:
            {
                timer -= dt;

                if (timer <= 0)
                {
                    stop();
                    setStage(Stage.loop);
                    play();
                    updater = -1;
                }

                break;
            }

            case stop:
            {
                if (breakingTimer > 0)
                {
                    breakingTimer -= dt;

                    if (breakingTimer <= 0)
                    {
                        prevSound.stop(prevSoundId);
                        prevSound = null;
                        prevSoundId = -1;
                    }
                }

                timer -= dt;

                if (timer <= 0)
                {
                    stop();
                    setStage(Stage.done);
                    return;
                }

                break;
            }
            case done:
            {
                return;
            }
        }

        if (released)
            return;

        updater -= dt;

        if (updater < 0)
        {
            updater = 0.025f;

            calculateSound();
            updateSound(soundId);
        }
    }

    @Override
    public boolean done()
    {
        return stage == Stage.done;
    }

    public void setStage(Stage stage)
    {
        this.stage = stage;
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

    @Override
    public void cancel()
    {
        switch (stage)
        {
            case start:
            {
                prevSound = soundEffect.getStartSound();
                prevSoundId = soundId;

                Sound newSound = soundEffect.getStopSound();
                soundId = newSound.play();
                newSound.setLooping(soundId, false);
                calculateSound();
                updateSound(soundId);

                timer = soundEffect.getStopTime();
                breakingTimer = 0.2f;
                setStage(Stage.stop);

                break;
            }
            case loop:
            {
                prevSound = soundEffect.getLoopSound();
                prevSoundId = soundId;

                Sound newSound = soundEffect.getStopSound();
                soundId = newSound.play();
                newSound.setLooping(soundId, false);
                calculateSound();
                updateSound(soundId);

                timer = soundEffect.getStopTime();
                breakingTimer = 0.2f;
                setStage(Stage.stop);

                break;
            }
        }

        released = true;
    }
}
