package com.desertkun.brainout.data.parallax;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.parallax.FreePlayDayNightGradientLayer;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.mode.ClientFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ScaledTiledDrawable;

@Reflect("data.parallax.FreePlayDayNightGradientLayerData")
public class FreePlayDayNightGradientLayerData extends LayerData
{
    protected final Array<ScaledTiledDrawable> textures;
    protected final Array<Float> timeStamps;
    protected final Vector2 textureSize;
    protected final Vector2 position;

    private int fromIndex;
    private int toIndex;
    private ScaledTiledDrawable fromTexture;
    private ScaledTiledDrawable toTexture;

    private float degree;
    private float timer;

    public FreePlayDayNightGradientLayerData(FreePlayDayNightGradientLayer layer, ParallaxData parallaxData, Map map)
    {
        super(layer, parallaxData, map);

        this.textureSize = new Vector2(
            layer.getTextures().get(0).getRegionWidth(), layer.getTextures().get(0).getRegionHeight());
        this.position = new Vector2();

        this.textures = new Array<>();
        for (TextureAtlas.AtlasRegion texture : layer.getTextures())
        {
            this.textures.add(new ScaledTiledDrawable(texture));
        }

        int [] timeWeights = layer.getTimeWeights();

        int fullWeight = 0;

        for (int weight: timeWeights)
        {
            fullWeight += weight;
        }

        if (fullWeight == 0) fullWeight = 1;

        Array<Float> timeStamps = new Array<>();

        Float time = 0.f;
        for (int weight: timeWeights)
        {
            timeStamps.add(time);
            time = time + (float)weight / fullWeight;
        }

        this.timeStamps = timeStamps;

        calculateImages();
        timer = .1f;
    }

    private void calculateImages()
    {
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

        float slices = timeOfDay * this.textures.size;

        if (fromIndex == 0 && toIndex == 0)
        {
            int i = 0;

            for (Float stamp : timeStamps)
            {

                if (timeOfDay < stamp)
                {
                    fromIndex = i - 1;
                    toIndex = i;

                    break;
                }

                if (i == timeStamps.size - 1 && timeOfDay > stamp)
                {
                    fromIndex = i;
                    toIndex = 0;

                    break;
                }

                i++;
            }
        }
        else
        {
            if ((toIndex != 0 && timeOfDay > timeStamps.get(toIndex)) || (toIndex == 0 && timeOfDay < timeStamps.get(toIndex + 1)))
            {
                fromIndex++;
                if (fromIndex >= this.textures.size) fromIndex = 0;

                toIndex++;
                if (toIndex >= this.textures.size) toIndex = 0;
            }
        }

        fromTexture = this.textures.get(fromIndex);
        toTexture = this.textures.get(toIndex);

        if (toIndex != 0)
            this.degree = (timeOfDay - timeStamps.get(fromIndex)) / (timeStamps.get(toIndex) - timeStamps.get(fromIndex));
        else
            this.degree = (timeOfDay - timeStamps.get(fromIndex)) / (1 - timeStamps.get(fromIndex));
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
            offsetY = - watcher.getWatchY() * Constants.Graphics.RES_SIZE  * getCoefY();
        }

        position.set(getX() + getCameraX() + offsetX, getCameraY() + getY() + offsetY);
    }

    public float getPlayerX(Watcher watcher)
    {
        return watcher.getWatchX();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer -= dt;
        if (timer < 0)
        {
            timer = 0.1f;

            calculateImages();
        }
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

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        fromTexture.draw(batch, (int)position.x, (int)position.y, w, h, s);

        toTexture.getColor().a = degree;
        toTexture.draw(batch, (int)position.x, (int)position.y, w, h, s);

        batch.getColor().a = 1;
    }
}
