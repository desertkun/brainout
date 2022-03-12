package com.desertkun.brainout.content.bullet;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.interfaces.LaunchData;

public abstract class Bullet extends ConsumableContent
{
    private float speed;
    private float power;
    private float mass;
    private float powerDistance;
    private float headMultiplier;
    private float hitImpulse;
    private boolean damageMyself;
    private float hitShake;
    private int bulletAtLaunch;
    private float damageCoeficient;
    private Interpolation function;
    private boolean needsValidation;
    private boolean hitEffectNormal;
    private int good;

    private boolean blockEffectEnabled;
    private boolean dropable;
    private int dropAtOnce;
    private BulletSlot slot;
    private float timeToLive;
    private Array<String> bulletTags;

    public Bullet()
    {
        this.bulletTags = new Array<>();
    }

    public enum BulletSlot
    {
        primary,
        secondary
    }

    public enum PowerFunction
    {
        pow5In,
        pow2In,
        pow2Out
    }

    public boolean isNeedValidation()
    {
        return needsValidation;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        good = jsonData.getInt("good", 0);
        needsValidation = jsonData.getBoolean("needsValidation", true);

        bulletAtLaunch = jsonData.getInt("bulletAtLaunch", 1);
        dropAtOnce = jsonData.getInt("dropAtOnce", 15);
        dropable = jsonData.getBoolean("dropable", true);
        speed = jsonData.getFloat("speed");
        power = jsonData.getFloat("power");
        mass = jsonData.getFloat("mass");
        damageCoeficient = jsonData.getFloat("damageCoeficient", 1);
        headMultiplier = jsonData.getFloat("headMultiplier", 1f);
        powerDistance = jsonData.getFloat("powerDistance", 64.0f);
        hitImpulse = jsonData.getFloat("hitImpulse", 10.0f);
        damageMyself = jsonData.getBoolean("damageMyself", false);
        hitEffectNormal = jsonData.getBoolean("hitEffectNormal", true);
        hitShake = jsonData.getFloat("hitShake", 0.2f);
        blockEffectEnabled = jsonData.getBoolean("blockEffect", true);
        timeToLive = jsonData.getFloat("timeToLive", Constants.Core.BULLET_TIME_TO_LIVE);

        if (jsonData.has("bulletTags"))
        {
            if (jsonData.get("bulletTags").isString())
            {
                bulletTags.add(jsonData.getString("bulletTags"));
            }
            else
            {
                bulletTags.addAll(jsonData.get("bulletTags").asStringArray());
            }
        }

        PowerFunction powerFunction =
            PowerFunction.valueOf(jsonData.getString("function", PowerFunction.pow2Out.toString()));


        switch (powerFunction)
        {
            case pow5In:
            {
                function = Interpolation.pow5In;
                break;
            }
            case pow2In:
            {
                function = Interpolation.pow2In;
                break;
            }
            case pow2Out:
            default:
            {
                function = Interpolation.pow2Out;
                break;
            }
        }

        slot = BulletSlot.valueOf(jsonData.getString("slot", BulletSlot.primary.toString()));
    }

    public BulletSlot getSlot()
    {
        return slot;
    }

    public float getSpeed()
    {
        return speed;
    }

    public float getPower()
    {
        return power;
    }

    public int getAmountPerLaunch()
    {
        return bulletAtLaunch;
    }

    public float getPowerDistance()
    {
        return powerDistance;
    }

    public float getMass()
    {
        return mass;
    }

    public float calculateDamage(float powerLeft, float damage)
    {
        float f = MathUtils.clamp(powerLeft, 0, power) / power;
        return function.apply(f) * damage;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);
    }

    public abstract BulletData getData(LaunchData launchData, float damageCoefficient, String dimension);

    public float getHeadMultiplier()
    {
        return headMultiplier;
    }

    public float getDamageCoeficient()
    {
        return damageCoeficient;
    }

    public float getHitImpulse()
    {
        return hitImpulse;
    }

    public boolean isDamageMyself()
    {
        return damageMyself;
    }

    public boolean isDropable()
    {
        return dropable;
    }

    public int getDropAtOnce()
    {
        return dropAtOnce;
    }

    public float getHitShake()
    {
        return hitShake;
    }

    public String getKillsStat()
    {
        return ContentLockTree.GetComplexValue("kills-from-bullet", getID());
    }

    public static String getTagKillsStat(String tag)
    {
        return ContentLockTree.GetComplexValue("kills-bullet-tag", tag);
    }

    public boolean isHitEffectNormal()
    {
        return hitEffectNormal;
    }

    public boolean isBlockEffectEnabled()
    {
        return blockEffectEnabled;
    }

    public float getTimeToLive()
    {
        return timeToLive;
    }

    public Array<String> getBulletTags()
    {
        return bulletTags;
    }

    public int getGood()
    {
        return good;
    }
}
