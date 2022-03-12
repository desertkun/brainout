package com.desertkun.brainout.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class CenterSprite extends Sprite
{
    private LaunchData pos;
    private final Vector2 sz;

    public CenterSprite(TextureRegion drawRegion, LaunchData pos)
    {
        super(drawRegion);

        float coef = 1f / Constants.Graphics.RES_SIZE;

        this.pos = pos;
        this.sz = new Vector2(
            coef * (float)drawRegion.getRegionWidth() / 2f,
            coef * (float)drawRegion.getRegionHeight() / 2f
        );

        setSize(coef * getWidth(), coef * getHeight());
        setOrigin(sz.x, sz.y);
    }

    @Override
    public void draw(Batch batch)
    {
        setPosition(pos.getX() - sz.x, pos.getY() - sz.y);
        setRotation(pos.getAngle());

        super.draw(batch);
    }

    public void setLaunchData(LaunchData launchData)
    {
        this.pos = launchData;
    }
}
