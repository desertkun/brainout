package com.desertkun.brainout.data.interfaces;

import java.util.Random;

public class RandomLaunchData extends LaunchData
{
    private final float angleDifference;
    private final LaunchData owner;

    public RandomLaunchData(LaunchData owner, Random random, float difference)
    {
        super();

        this.owner = owner;
        this.angleDifference = (random.nextFloat() - 0.5f) * difference;
    }


    @Override
    public float getX()
    {
        return owner.getX();
    }

    @Override
    public float getY()
    {
        return owner.getY();
    }

    @Override
    public float getAngle()
    {
        return owner.getAngle() + angleDifference;
    }

    @Override
    public String getDimension()
    {
        return owner.getDimension();
    }

    @Override
    public boolean getFlipX()
    {
        return owner.getFlipX();
    }
}
