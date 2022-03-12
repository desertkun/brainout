package com.desertkun.brainout.data.interfaces;

import com.badlogic.gdx.graphics.Camera;

public class RenderContext
{
    public float x, y, width, height, angle;
    public boolean drawRecursive;
    public Camera camera;
    public boolean post;

    public RenderContext(float x, float y, float width, float height, boolean drawRecursive, float angle)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.drawRecursive = drawRecursive;
        this.angle = angle;
        this.camera = null;
    }

    public void resize(float width, float height)
    {
        this.width = width;
        this.height = height;
    }

    public RenderContext(float width, float height)
    {
        this(0, 0, width, height, true, 0);
    }
}
