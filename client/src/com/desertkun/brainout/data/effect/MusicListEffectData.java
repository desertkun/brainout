package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.effect.MusicListEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.MusicListEffectData")
public class MusicListEffectData extends EffectData implements Runnable
{
    protected final MusicListEffect musicEffect;
    private int musicId;

    protected float volume;
    protected float pan;
    private float soundDistance;
    private Music currentMusic;

    private float updater;

    public MusicListEffectData(MusicListEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        musicEffect = effect;
        currentMusic = musicEffect.getMusic().first();
        musicId = 0;
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
        play();
        updateSound();
    }

    public boolean isPlaying()
    {
        if (currentMusic == null)
            return false;

        return currentMusic.isPlaying();
    }

    protected void play()
    {
        if (currentMusic == null)
            return;

        currentMusic.play();
        currentMusic.setOnCompletionListener(music -> next());
    }

    private void next()
    {
        musicId++;

        if (musicId >= musicEffect.getMusic().size)
        {
            musicId = 0;
        }

        currentMusic = musicEffect.getMusic().get(musicId);
        play();
    }

    protected void stop()
    {
        if (currentMusic == null)
            return;

        if (currentMusic.isPlaying())
        {
            currentMusic.stop();
        }
    }

    protected void updateSound()
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        if (isPlaying())
        {
            currentMusic.setPan(pan, volume);
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

        if (currentMusic == null)
            return;

        currentMusic.stop();
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
        return false;
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
