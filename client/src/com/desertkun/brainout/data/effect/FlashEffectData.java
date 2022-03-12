package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.effect.FlashEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.FlashEffectData")
public class FlashEffectData extends EffectData
{
    private float duration;
    private OrthographicCamera camera;

    public FlashEffectData(FlashEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        this.duration = effect.getDuration();
    }

    @Override
    public boolean done()
    {
        return duration <= 0;
    }

    @Override
    public void init()
    {
        ((ClientMap) getMap()).setLightsEnabled(false);

        camera = new OrthographicCamera(BrainOutClient.getWidth(), BrainOutClient.getHeight());
        camera.position.x = BrainOutClient.getWidth() / 2;
        camera.position.y = BrainOutClient.getHeight() / 2;
    }

    @Override
    public void release()
    {
        super.release();

        ((ClientMap) getMap()).setLightsEnabled(true);
    }

    @Override
    public int getEffectLayer()
    {
        return 2;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);

        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 1, 1, 0.15f);

        shapeRenderer.rect(-BrainOutClient.getWidth(), -BrainOutClient.getHeight(), BrainOutClient.getWidth() * 2,
                BrainOutClient.getHeight() * 2);

        shapeRenderer.end();

        batch.begin();
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public void update(float dt)
    {
        duration -= dt;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
