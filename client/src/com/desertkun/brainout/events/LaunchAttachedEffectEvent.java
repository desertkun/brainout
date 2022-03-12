package com.desertkun.brainout.events;

import com.desertkun.brainout.content.effect.EffectSet;

public class LaunchAttachedEffectEvent extends Event
{
    public String kind;
    public EffectSet.EffectAttacher effectAttacher;

    @Override
    public ID getID()
    {
        return ID.launchAttachedEffect;
    }

    private Event init(String kind, EffectSet.EffectAttacher effectAttacher)
    {
        this.kind = kind;
        this.effectAttacher = effectAttacher;

        return this;
    }

    public static Event obtain(String kind, EffectSet.EffectAttacher effectAttacher)
    {
        LaunchAttachedEffectEvent e = obtain(LaunchAttachedEffectEvent.class);
        if (e == null) return null;
        return e.init(kind, effectAttacher);
    }

    @Override
    public void reset()
    {
        this.kind = null;
        this.effectAttacher = null;
    }
}
