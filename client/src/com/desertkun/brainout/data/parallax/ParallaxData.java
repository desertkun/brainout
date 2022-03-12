package com.desertkun.brainout.data.parallax;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.content.parallax.Layer;
import com.desertkun.brainout.content.parallax.Parallax;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.parallax.ParallaxData")
public class ParallaxData extends Data implements RenderUpdatable
{
    private final Parallax parallax;
    private Array<LayerData> layers;

    public ParallaxData(Parallax content, Map map)
    {
        super(content, map.getDimension());

        this.layers = new Array<>();
        this.parallax = content;

        for (Layer layer: content.getLayers())
        {
            LayerData data = layer.getData(this, map);

            if (data == null)
                continue;

            layers.add(data);
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        for (LayerData layerData: layers)
        {
            layerData.update(dt);
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        for (LayerData layerData: layers)
        {
            layerData.render(batch, context);
        }
    }

    public Parallax getParallax() {
        return parallax;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
