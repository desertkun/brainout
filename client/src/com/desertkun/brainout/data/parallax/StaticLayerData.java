package com.desertkun.brainout.data.parallax;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.parallax.StaticLayer;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Watcher;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ScaledTiledDrawable;

@Reflect("data.parallax.StaticLayerData")
public class StaticLayerData extends LayerData
{
    protected final ScaledTiledDrawable texture;
    protected final Vector2 textureSize;
    protected final Vector2 position;
    private final boolean top;

    public StaticLayerData(StaticLayer layer, ParallaxData parallaxData, Map map)
    {
        super(layer, parallaxData, map);

        this.textureSize = new Vector2(layer.getTexture().getRegionWidth(), layer.getTexture().getRegionHeight());
        this.position = new Vector2();
        this.texture = new ScaledTiledDrawable(layer.getTexture());
        this.top = layer.isTop();
    }

    public void calculatePosition(RenderContext context)
    {
        Watcher watcher = Map.GetWatcher();

        float offsetX = 0, offsetY = 0;

        if (watcher != null)
        {
            if (!watcher.getDimension().equals(getParallaxData().getDimension()))
                return;

            float s;

            if (isScale())
            {
                s = textureSize.y < context.height ? context.height / textureSize.y : 1.0f;
            }
            else
            {
                s = 1.0f;
            }

            float w = textureSize.x * s;

            offsetX = - getPlayerX(watcher) * Constants.Graphics.RES_SIZE * getCoefX() % w - w;
            if (top)
            {
                offsetY = BrainOutClient.getHeight() - textureSize.y;
            }

            offsetY -= watcher.getWatchY() * Constants.Graphics.RES_SIZE  * getCoefY();
        }

        position.set(getX() + getCameraX() + offsetX, getCameraY() + getY() + offsetY);
    }

    public float getPlayerX(Watcher watcher)
    {
        return watcher.getWatchX();
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
            s = textureSize.y < context.height ? context.height / textureSize.y : 1.0f;

            w = s * (context.width + textureSize.x * 2);
            h = s * (textureSize.y);
        }
        else
        {
            s = 1.0f;

            w = context.width + textureSize.x * 2;
            h = textureSize.y;
        }

        texture.draw(batch, (int)position.x, (int)position.y, w, h, s);
    }
}
