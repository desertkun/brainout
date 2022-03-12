package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.desertkun.brainout.content.effect.Effect;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class EffectActor extends Group
{
    private EffectData data;
    private Vector2 absolutePosition;

    public EffectActor(Effect effect, String dimension)
    {
        absolutePosition = new Vector2();

        if (effect.isEnabled())
        {
            data = effect.getEffect(new LaunchData()
            {
                @Override
                public float getX()
                {
                    return absolutePosition.x;
                }

                @Override
                public float getY()
                {
                    return absolutePosition.y;
                }

                @Override
                public float getAngle()
                {
                    return 0;
                }

                @Override
                public String getDimension()
                {
                    return dimension;
                }

                @Override
                public boolean getFlipX()
                {
                    return false;
                }
            });

            data.init();
        }
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if (data != null)
        {
            data.update(delta);

            absolutePosition.set(getX(), getY());
            localToStageCoordinates(absolutePosition);
        }
    }

    @Override
    protected void drawChildren(Batch batch, float parentAlpha)
    {
        super.drawChildren(batch, parentAlpha);

        if (data != null)
        {
            data.render(batch, null);
        }
    }

    @Override
    protected void setParent(Group parent)
    {
        super.setParent(parent);

        if (parent == null && data != null)
        {
            data.release();
            data = null;
        }
    }
}
