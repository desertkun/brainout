package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.GunshotTailSoundEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.components.ClientWeaponComponentData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.GunshotTailSoundEffectData")
public class GunshotTailSoundEffectData extends EffectData implements Runnable
{
    private static boolean hit = false;

    private static final float DISTANCE = 48.0f;

    private static Vector2[] DIRECTIONS;

    static
    {
        DIRECTIONS = new Vector2[24];

        for (int i = 0; i < 24; i++)
        {
            DIRECTIONS[i] = new Vector2(0, DISTANCE).rotate(((float)i / 16.0f) * 360.0f);
        }
    }

    protected final GunshotTailSoundEffect soundEffect;
    protected long soundId;

    protected float volume;
    protected float pan;
    private float soundDistance;

    private String key;
    private float startTimer;
    private boolean done;
    private float power;

    public GunshotTailSoundEffectData(GunshotTailSoundEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        startTimer = effect.getDelayCheck();
        key = effect.getKey();
        soundEffect = effect;
        soundId = -1;
        power = 1.0f;
    }

    public GunshotTailSoundEffectData(GunshotTailSoundEffect effect, LaunchData launchData,
                                        EffectSet.EffectAttacher attacher)
    {
        this(effect, launchData);

        EffectData bound = attacher.getBoundEffect(key);

        if (bound instanceof GunshotTailSoundEffectData)
        {
            ((GunshotTailSoundEffectData) bound).cancelTail();
        }

        attacher.bindEffect(key, startTimer, this);

        if (attacher instanceof ClientWeaponComponentData.ClientWeaponComponentAttacher)
        {
            ClientWeaponComponentData wcd =
                ((ClientWeaponComponentData.ClientWeaponComponentAttacher) attacher).getWCD();

            power = MathUtils.clamp((wcd.getDamage().asFloat() - 30.0f) / 60.0f, 0.0f, 1.0f);
        }
    }

    private void startTail()
    {
        if (done)
            return;

        calculateSound();
        run();

        done = true;
    }

    private void cancelTail()
    {
        done = true;
    }

    @Override
    public void init()
    {
        soundDistance = soundEffect.getSoundDistance();
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
        return soundId != -1;
    }

    protected void play()
    {
        if (soundId == -1)
        {
            Sound sound = soundEffect.getSound();
            soundId = sound.play();
            sound.setLooping(soundId, soundEffect.isLoop());
        }
    }

    protected void stop()
    {
        if (soundId != -1)
        {
            Sound sound = soundEffect.getSound();
            sound.stop(soundId);

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
            Sound sound = soundEffect.getSound();

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

            calculateEnclosure();
        }
        else
        {
            volume = 0;
        }
    }

    private void calculateEnclosure()
    {
        LaunchData launchData = getLaunchData();

        Map map = Map.Get(launchData.getDimension());

        if (map == null)
            return;

        if (map.getWidth() <= 512)
        {
            volume = 0;
            return;
        }

        volume *= power;

        /*
        World world = map.getPhysicWorld();

        if (world == null)
            return;

        Vector2 tmpStart = new Vector2(launchData.getX(), launchData.getY()), tmpEnd = new Vector2();

        int outputs = 0;

        for (Vector2 direction : DIRECTIONS)
        {
            tmpEnd.set(tmpStart).add(direction);
            hit = false;
            world.rayCast(this::collision, tmpStart, tmpEnd);

            if (!hit)
            {
                outputs++;
            }
        }

        float coef = MathUtils.clamp(outputs / 4.0f, 0.f, 1.0f);

        volume *= coef;
        */
    }

    private float collision(Fixture fixture, Vector2 point, Vector2 normal, float fraction)
    {
        Object userData = fixture.getUserData();

        if (userData == null)
        {
            hit = true;
            return 0;
        }

        return -1;
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

        if (soundEffect.isLoop())
        {
            soundEffect.getSound().stop(soundId);
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
        if (done)
            return;

        if (startTimer > 0)
        {
            startTimer -= dt;

            if (startTimer <= 0)
            {
                startTimer = 0;

                startTail();
            }
        }
    }

    @Override
    public boolean done()
    {
        return done;
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
