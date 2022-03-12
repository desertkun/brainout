package com.desertkun.brainout.content.consumable;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.content.consumable.impl.WalkietalkieConsumableItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.Walkietalkie")
public class Walkietalkie extends ConsumableContent
{
    public static final int MIN_FREQUENCY = 420000;
    public static final int MAX_FREQUENCY = 450995;

    public Walkietalkie()
    {
    }

    @Override
    public ConsumableItem acquireConsumableItem()
    {
        return new WalkietalkieConsumableItem(this);
    }

    public static int getRandomFrequency()
    {
        int rawFrequency = MathUtils.random(MIN_FREQUENCY, MAX_FREQUENCY);
        rawFrequency = validateFrequency(rawFrequency);

        return rawFrequency;
    }

    public static int validateFrequency(int value)
    {
        value = MathUtils.clamp(value, MIN_FREQUENCY, MAX_FREQUENCY);

        int frequencyLastNumeral = value % 10;
        if (frequencyLastNumeral != 0 && frequencyLastNumeral != 5)
        {
            int frequencyRoundingAmount = frequencyLastNumeral % 5;
            value -= frequencyRoundingAmount;
        }

        return value;
    }
}
