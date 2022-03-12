package com.desertkun.brainout.data.parallax;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.parallax.FreePlayTimeOfDayLayer;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.mode.ClientFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.parallax.FreePlayTimeOfDayLayerData")
public class FreePlayTimeOfDayLayerData extends DynamicLayerData
{
    private final float timeOfDay;
    private final float timeLength;
    private float brightness;

    public FreePlayTimeOfDayLayerData(FreePlayTimeOfDayLayer layer, ParallaxData parallaxData, Map map)
    {
        super(layer, parallaxData, map);

        this.timeOfDay = layer.getTimeOfDay();
        this.timeLength = layer.getTimeLength();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        float timeOfDay;

        if ((BrainOutClient.ClientController.getGameMode().getRealization() instanceof ClientFreeRealization))
        {
            ClientFreeRealization fr = ((ClientFreeRealization) BrainOutClient.ClientController.getGameMode().getRealization());
            timeOfDay = fr.getGameMode().getTimeOfDay();
        }
        else
        {
            timeOfDay = 0.5f;
        }

        float day = Math.abs(timeOfDay - this.timeOfDay);
        if (day > 0.5)
            day = 1f - day;

        if (day > this.timeLength)
        {
            brightness = 0;
            return;
        }

        brightness = Interpolation.pow5.apply(1.0f - (day / this.timeLength));
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (brightness <= 0)
            return;

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        texture.getColor().a = brightness;
        super.render(batch, context);
    }
}
