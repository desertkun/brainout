package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOutClient;

public class ColorDrawable extends BaseDrawable implements Disposable
{
    private final Color color;

    public ColorDrawable(Color color)
    {
        this.color = color;
    }

    public ColorDrawable(float r, float g, float b, float a)
    {
        this.color = new Color(r, g, b, a);
    }

    public Color getColor()
    {
        return color;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height)
    {
        batch.end();

        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setColor(color);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        batch.begin();
    }

    @Override
    public void dispose()
    {

    }
}
