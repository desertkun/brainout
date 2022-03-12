package com.desertkun.brainout.data.parallax;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.content.parallax.Layer;

import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.parallax.LayerData")
public class LayerData
{
    private final Layer layer;
    private final ParallaxData parallaxData;
    private final float x;
    private final float y;
    private final float coefX;
    private final float coefY;
    private final OrthographicCamera camera;
    private final boolean scale;

    public LayerData(Layer layer, ParallaxData parallaxData, Map map)
    {
        this.layer = layer;
        this.parallaxData = parallaxData;

        this.camera = map.getCameraBefore().getCamera();

        this.x = layer.getX();
        this.y = layer.getY();
        this.coefX = layer.getCoefX();
        this.coefY = layer.getCoefY();
        this.scale = layer.isScale();
    }

    public Layer getLayer()
    {
        return layer;
    }

    public void update(float dt) {}
    public void render(Batch batch, RenderContext context) {}

    public float getCameraX()
    {
        return camera.position.x - camera.viewportWidth / 2f;
    }

    public float getCameraY()
    {
        return camera.position.y - camera.viewportHeight / 2f;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public float getCoefX()
    {
        return coefX;
    }

    public float getCoefY()
    {
        return coefY;
    }

    public boolean isScale()
    {
        return scale;
    }

    public ParallaxData getParallaxData()
    {
        return parallaxData;
    }
}
