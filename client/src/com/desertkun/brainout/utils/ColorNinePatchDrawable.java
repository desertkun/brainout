package com.desertkun.brainout.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class ColorNinePatchDrawable extends NinePatchDrawable
{
    private final Color color;

    public ColorNinePatchDrawable(NinePatch ninePatch, Color color)
    {
        super(ninePatch);

        this.color = color;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height)
    {
        batch.setColor(color);
        super.draw(batch, x, y, width, height);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void draw(Batch batch, float x, float y, float originX, float originY,
                     float width, float height, float scaleX, float scaleY, float rotation)
    {
        batch.setColor(color);
        super.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
        batch.setColor(Color.WHITE);
    }
}
