package com.desertkun.brainout.posteffects.plain;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOutClient;

public class NoiseEffect implements Disposable
{
    private final Array<TiledDrawable> noise;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private float value;

    public NoiseEffect(float value)
    {
        this.value = value;

        noise = new Array<>();

        camera = new OrthographicCamera(BrainOutClient.getWidth(), BrainOutClient.getHeight());
        camera.position.x = BrainOutClient.getWidth() / 2;
        camera.position.y = BrainOutClient.getHeight() / 2;
        batch = new SpriteBatch();

        noise.addAll(
            new TiledDrawable(BrainOutClient.getRegion("noise1")),
            new TiledDrawable(BrainOutClient.getRegion("noise2")),
            new TiledDrawable(BrainOutClient.getRegion("noise3")),
            new TiledDrawable( BrainOutClient.getRegion("noise4")));

    }

    public void setValue(float value)
    {
        this.value = value;
    }

    public void render()
    {
        TiledDrawable t = noise.random();
        t.getColor().a = value;

        float offsetX = MathUtils.random(0, 256);
        float offsetY = MathUtils.random(0, 256);

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        t.draw(batch, -offsetX, -offsetY,
                BrainOutClient.getWidth() + offsetX,
                BrainOutClient.getHeight() + offsetY);

        batch.end();
    }

    @Override
    public void dispose()
    {
        batch.dispose();
    }
}
