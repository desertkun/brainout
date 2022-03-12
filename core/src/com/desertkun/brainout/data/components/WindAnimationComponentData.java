package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.WindAnimationComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.AnimationData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.Animable;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.spine.AnimationState;

@Reflect("WindAnimationComponent")
@ReflectAlias("data.components.WindAnimationComponentData")
public class WindAnimationComponentData extends AnimationComponentData<WindAnimationComponent>
{
    private AnimationState.TrackEntry track;
    private Interpolation windPowerInterpolation;
    private float updateWindsTimer;
    private final float WIND_TIMER = 2;
    private Array<ActiveData> winds;
    private float time;

    public WindAnimationComponentData(ComponentObject componentObject, WindAnimationComponent animation)
    {
        super(componentObject, animation);

        updateWindsTimer = WIND_TIMER;
        windPowerInterpolation = Interpolation.pow4Out;
        winds = new Array<>();
        time = 0;
    }

    @Override
    public void init()
    {
        super.init();

        if (getComponentObject() instanceof AnimationData)
        {

            final AnimationData activeData = ((AnimationData) getComponentObject());

            attachTo(new Animable()
            {
                @Override
                public float getX()
                {
                    return activeData.getX();
                }

                @Override
                public float getY()
                {
                    return activeData.getY();
                }

                @Override
                public float getAngle()
                {
                    return 0;
                }

                @Override
                public boolean getFlipX()
                {
                    return activeData.isFlipX();
                }
            });
        }
        else
        {
            final ActiveData activeData = ((ActiveData) getComponentObject());

            attachTo(new Animable() {
                @Override
                public float getX()
                {
                    return activeData.getX();
                }

                @Override
                public float getY()
                {
                    return activeData.getY();
                }

                @Override
                public float getAngle()
                {
                    return 0;
                }

                @Override
                public boolean getFlipX()
                {
                    return false;
                }
            });
        }
    }

    @Override
    protected void playInitAnimations(WindAnimationComponent animation)
    {
        track = state.setAnimation(0, "wind", false);
        track.setTimeScale(0);
    }

    @Override
    public void update(float dt)
    {

        Watcher watcher = Map.GetWatcher();
        ActiveData activeData = ((ActiveData) getComponentObject());

        if (watcher == null || !watcher.getDimension().equals(activeData.getDimension()))
        {
            return;
        }

        updateWindsTimer -= dt;
        if (updateWindsTimer <= 0)
        {
            updateWindsTimer = WIND_TIMER;
            updateActiveWinds();
        }

        float windWaveDirection = getWindWaveDirection();

        final float ONE_FULL_WIGGLE_TIME = 5.4f;
        final int FULL_WIGGLES_IN_CYCLE = 7;

        float oneFullWIggleTime, toDegsCoof, windCycleTime;

        oneFullWIggleTime = ONE_FULL_WIGGLE_TIME;
        toDegsCoof = oneFullWIggleTime / 360.0f;
        windCycleTime = oneFullWIggleTime * FULL_WIGGLES_IN_CYCLE;

        time += dt * (1 + windWaveDirection * 3);

        float angle = (time % oneFullWIggleTime) / toDegsCoof + getSkeleton().getX() * 2;

        float cycle1 = (time % windCycleTime) / windCycleTime;
        float cycle2 = cycle1 <= 0.5 ? windPowerInterpolation.apply(cycle1 * 2) * 360 : 0;

        float powerSinusoid = 0.75f - MathUtils.cosDeg(cycle2) * 0.25f;

        float baseWiggle = powerSinusoid * (1 - Math.abs(windWaveDirection)) * 0.2f;
        float windWiggle = Math.abs(windWaveDirection) * 0.1f;
        float windAngle = windWaveDirection * 0.15f;

        float off = 0.5f + (MathUtils.cosDeg(angle) * (baseWiggle + windWiggle)) + windAngle;
        float c = 3.33333f * MathUtils.clamp(off, 0.f, 1.0f);

        track.setTrackTime(c);
        super.update(dt);
    }


    private void updateActiveWinds()
    {
        if (getMap() == null) return;
        winds = getMap().getActivesForTag(Constants.ActiveTags.WIND, activeData -> activeData.getComponent(WindComponentData.class) != null);
    }


    private float getWindWaveDirection()
    {
        for (ActiveData wind : winds)
        {
            if (wind.isAlive())
            {
                WindComponentData windComp = wind.getComponent(WindComponentData.class);
                if (windComp == null) continue;

                float power = windComp.func(getSkeleton().getX(), getSkeleton().getY());
                if (power != 0)
                    return power * Math.signum(windComp.getMovement());
            }
        }

        return 0;
    }
}
