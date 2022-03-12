package com.desertkun.brainout.events;

public class CustomInstrumentEffectEvent extends Event
{
    public String effect;

    @Override
    public ID getID()
    {
        return ID.customInstrumentEffect;
    }

    private Event init(String effect)
    {
        this.effect = effect;

        return this;
    }

    public static Event obtain(String effect)
    {
        CustomInstrumentEffectEvent e = obtain(CustomInstrumentEffectEvent.class);
        if (e == null) return null;
        return e.init(effect);
    }

    @Override
    public void reset()
    {
        effect = null;
    }
}
