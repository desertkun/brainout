package com.desertkun.brainout.data.active;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.active.FreePlayPlayer;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.components.FreeplayPlayerComponentData;
import com.desertkun.brainout.data.components.PlayerBoostersComponentData;
import com.desertkun.brainout.data.components.RadioactiveComponentData;
import com.desertkun.brainout.data.components.WindComponentData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.FreePlayPlayerData")
public class FreePlayPlayerData extends PlayerData
{
    private PlayerBoostersComponentData bst;
    private FreeplayPlayerComponentData fp;
    private float speedCoef;
    private float cnt;

    public FreePlayPlayerData(FreePlayPlayer player, String dimension)
    {
        super(player, dimension);

        speedCoef = 1.0f;
        cnt = 0;
    }

    @Override
    public void init()
    {
        super.init();

        bst = getComponent(PlayerBoostersComponentData.class);
        fp = getComponent(FreeplayPlayerComponentData.class);
        speedCoef = updateSpeedCoef();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        cnt -= dt;

        if (cnt < 0)
        {
            cnt = 0.125f;

            speedCoef = updateSpeedCoef();
        }
    }

    private float updateSpeedCoef()
    {
        float value = 1.0f;

        if (bst != null)
        {
            PlayerBoostersComponentData.Booster speedBooster = bst.getBooster("speed");

            if (speedBooster != null)
            {
                value *= speedBooster.value;
            }
        }

        if (fp != null)
        {
            value *= fp.calculateSpeedCoefficient();
        }

        Map map = getMap();

        if (map != null)
        {
            ActiveData closesSpot = map.getClosestActiveForTag(192, x, y,
                ActiveData.class, Constants.ActiveTags.WIND, activeData -> true);

            if (closesSpot != null)
            {
                WindComponentData wind = closesSpot.getComponent(WindComponentData.class);

                if (wind != null)
                {
                    value *= 1.0f - (wind.func(getX(), getY(), 0.5f) / 2.0f);
                }
            }
        }


        PlayerOwnerComponent poc = getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            float weight = poc.getConsumableContainer().getWeight();
            float maxWeight = getMaxWeight();
            float maxOverweight = getMaxOverweight();

            if (weight > maxWeight)
            {
                float c = 1.0f - MathUtils.clamp((weight - maxWeight) / (maxOverweight - maxWeight), 0, 1.0f);
                float a = Interpolation.pow5Out.apply(c) * 0.75f + 0.25f;

                value *= a;
            }
        }

        return value;
    }

    @Override
    public float getSpeedCoef()
    {
        return super.getSpeedCoef() * speedCoef;
    }

    @Override
    public boolean canRun()
    {
        return fp == null || (!fp.isHungry() && !fp.hasBonesBroken());
    }
}
