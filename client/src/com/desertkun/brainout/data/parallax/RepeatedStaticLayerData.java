package com.desertkun.brainout.data.parallax;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.parallax.RepeatedStaticLayer;

import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.parallax.RepeatedStaticLayerData")
public class RepeatedStaticLayerData extends StaticLayerData
{
    private final float repeatY;

    public RepeatedStaticLayerData(RepeatedStaticLayer layer, ParallaxData parallaxData, Map map)
    {
        super(layer, parallaxData, map);

        this.repeatY = layer.getRepeatY();
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        calculatePosition(context);

        float w;
        float h;
        float s;

        if (isScale())
        {
            s = textureSize.y < BrainOutClient.getHeight() ? BrainOutClient.getHeight() / textureSize.y : 1.0f;
            w = s * (BrainOutClient.getWidth() + textureSize.x * 2);
            h = repeatY * s;
        }
        else
        {
            s = 1.0f;
            w = BrainOutClient.getWidth() + textureSize.x * 2;
            h = repeatY;
        }

        texture.draw(batch, (int)position.x, (int)position.y, w, h, s);
    }
}
