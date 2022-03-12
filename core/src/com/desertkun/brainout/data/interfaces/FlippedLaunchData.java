package com.desertkun.brainout.data.interfaces;

public class FlippedLaunchData extends LaunchData
{
    private final LaunchData launchData;

    public FlippedLaunchData(LaunchData launchData)
    {
        super();

        this.launchData = launchData;
    }

    @Override
    public float getX() {
        return launchData.getX();
    }

    @Override
    public float getY() {
        return launchData.getY();
    }

    @Override
    public float getAngle() {
        return FlippedAngle.getAngle(launchData.getAngle(), launchData.getFlipX());
    }

    @Override
    public String getDimension()
    {
        return launchData.getDimension();
    }

    @Override
    public boolean getFlipX() {
        return false;
    }
}
