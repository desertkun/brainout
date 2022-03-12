package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.effect.MusicEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.MusicEffectData")
public class MusicEffectData extends EffectData implements Runnable
{
    protected final MusicEffect musicEffect;

    protected float volume;
    protected float pan;
    private float soundDistance;

    private float updater;

    public MusicEffectData(MusicEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        musicEffect = effect;
        updater = 0;
    }

    @Override
    public void init()
    {
        soundDistance = musicEffect.getSoundDistance();

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
        Music music = musicEffect.getMusic();

        if (music == null)
            return false;

        return music.isPlaying();
    }

    protected void play()
    {
        Music music = musicEffect.getMusic();

        if (music == null)
            return;

        if (!music.isPlaying())
        {
            music.setLooping(musicEffect.isLoop());
            music.play();
        }
    }

    protected void stop()
    {
        Music music = musicEffect.getMusic();

        if (music == null)
            return;

        if (music.isPlaying())
        {
            music.stop();
        }
    }

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

        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        if (isPlaying())
        {
            Music music = musicEffect.getMusic();
            music.setPan(pan, volume);
        }
        else
        {
            if (volume > 0)
            {
                play();

                Music music = musicEffect.getMusic();
                music.setPan(pan, volume);
            }
        }
    }

    protected void calculateSound()
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        this.pan = 0;
        this.volume = 1;

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

        Music music = musicEffect.getMusic();

        if (music == null)
            return;

        music.stop();
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
        return !musicEffect.isLoop();
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
