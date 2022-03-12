package com.desertkun.brainout.data.effect;

import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.HitPhysicEffect;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.HitPhysicEffectData")
public class HitPhysicEffectData extends PhysicEffectData
{
    private final EffectSet onHit;
    private boolean hasContact;

    public HitPhysicEffectData(HitPhysicEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        this.onHit = effect.getOnHit();
        this.hasContact = false;
    }

    @Override
    protected void contact(LaunchData launchData)
    {
        if (!hasContact)
        {
            onHit.launchEffects(launchData);
            hasContact = true;
        }
    }
}
