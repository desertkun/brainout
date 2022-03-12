package com.desertkun.brainout.data.interfaces;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.data.Map;

public class PointLaunchData extends LaunchData
{
    private String dimension;
    private Vector2 position;
    private float angle;
    private boolean flipX;

    public PointLaunchData(float x, float y, float angle, String dimension)
    {
        this(x, y, angle, dimension, false);
    }

    public PointLaunchData(float x, float y, float angle, String dimension, boolean flipX)
    {
        super();

        this.dimension = dimension;
        this.position = new Vector2(x, y);
        this.angle = angle;
        this.flipX = flipX;
    }

    public void setPosition(float x, float y)
    {
        this.position.set(x, y);
    }

    public Vector2 getPosition()
    {
        return position;
    }

    @Override
    public float getX()
    {
        return position.x;
    }

    @Override
    public float getY()
    {
        return position.y;
    }

    @Override
    public float getAngle()
    {
        return angle;
    }

    @Override
    public String getDimension()
    {
        return dimension;
    }

    public void setDimension(String dimension)
    {
        this.dimension = dimension;
    }

    public void setX(float x)
    {
        this.position.x = x;
    }

    public void setY(float y)
    {
        this.position.y = y;
    }

    public void setAngle(float angle)
    {
        this.angle = angle;
    }

    public void set(float x, float y, float angle)
    {
        this.position.set(x, y);
        this.angle = angle;
    }

    @Override
    public boolean getFlipX()
    {
        return flipX;
    }
}
