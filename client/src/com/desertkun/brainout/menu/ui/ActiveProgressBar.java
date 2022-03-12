package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.data.interfaces.Animable;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Renderable;

public class ActiveProgressBar implements Renderable, Disposable
{
    private final Animable animable;
    private float value;

    private Color baseColor;
    private Color activeColor;

    public ActiveProgressBar(Animable animable)
    {
        this.animable = animable;
        this.value = 0;

        baseColor = null;
        activeColor = Color.WHITE;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        batch.end();

        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        float bit = 2f / ClientConstants.Graphics.RES_SIZE;

        float x = animable.getX() - ClientConstants.Flag.WIDTH / 2f,
                y = animable.getY() + ClientConstants.Flag.OFFSET_Y,
                w = ClientConstants.Flag.WIDTH,
                h = ClientConstants.Flag.HEIGHT;

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, w, h);

        if (baseColor != null)
        {
            shapeRenderer.setColor(baseColor);
            shapeRenderer.rect(x + bit, y + bit, w - bit * 2, h - bit * 2);
        }

        shapeRenderer.setColor(activeColor);
        shapeRenderer.rect(x + bit, y + bit, (w - bit * 2) * value, h - bit * 2);

        shapeRenderer.end();

        batch.begin();
    }

    public void setValue(float value)
    {
        this.value = value;
    }

    public void setForegroundColor(Color activeColor)
    {
        this.activeColor = activeColor;
    }

    public void setBackgroundColor(Color baseColor)
    {
        this.baseColor = baseColor;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public int getZIndex()
    {
        return 0;
    }

    @Override
    public int getLayer()
    {
        return 0;
    }

    @Override
    public void dispose()
    {

    }
}
