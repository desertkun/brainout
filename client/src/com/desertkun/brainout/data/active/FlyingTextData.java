package com.desertkun.brainout.data.active;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.RenderContext;

public class FlyingTextData extends PointData
{
    private final Label label;
    private float timer;

    public FlyingTextData(String text, float x, float y, String dimension, String style)
    {
        super(null, dimension);

        setPosition(x + MathUtils.random(-2, 2), y + MathUtils.random(-2, 2));

        label = new Label(text, BrainOutClient.Skin, style);

        label.setFontScale(1.0f / ClientConstants.Graphics.RES_SIZE);
        label.setScale(1.0f / ClientConstants.Graphics.RES_SIZE);

        timer = 2.0f;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        label.setPosition(x, y);
        label.draw(batch, Interpolation.circleOut.apply(timer / 2.0f));
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer -= dt;

        setY(getY() + dt);

        if (timer <= 0)
        {
            Map map = getMap();
            if (map != null)
            {
                map.removeActive(this, true);
            }
        }
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }
}
