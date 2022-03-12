package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.effect.FlashbangEffect;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.FlashbangEffectData")
public class FlashbangEffectData extends EffectData
{
    private final FlashbangEffect effect;
    private float flashBang;
    private float fadeOut;
    private OrthographicCamera camera;
    private Batch batch;
    private FrameBuffer frame;
    private TextureRegion region;

    public FlashbangEffectData(FlashbangEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        this.effect = effect;
        this.flashBang = effect.getFlashDuration();
        this.fadeOut = effect.getFadeOutDuration();
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (this.region == null)
            return;

        if (!context.drawRecursive)
        {
            return;
        }

        camera.update();

        if (fadeOut > 0)
        {
            float f = Interpolation.circleOut.apply(fadeOut / effect.getFadeOutDuration());

            batch.end();

            Gdx.gl.glEnable(GL20.GL_BLEND);

            this.batch.setProjectionMatrix(camera.combined);
            this.batch.begin();
            this.batch.setColor(1, 1, 1, f);

            this.batch.draw(region, 0, 0, BrainOutClient.getWidth(),
                    BrainOutClient.getHeight());

            this.batch.end();

            batch.begin();
        }

        if (flashBang > 0)
        {
            float f = flashBang / effect.getFlashDuration();

            batch.end();

            Gdx.gl.glEnable(GL20.GL_BLEND);

            ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1, 1, 1, f);

            shapeRenderer.rect(0, 0, BrainOutClient.getWidth(),
                    BrainOutClient.getHeight());

            shapeRenderer.end();

            batch.begin();
        }
    }

    @Override
    public int getEffectLayer()
    {
        return 2;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public boolean done()
    {
        return fadeOut < 0;
    }

    @Override
    public void init()
    {
        camera = new OrthographicCamera(BrainOutClient.getWidth(), BrainOutClient.getHeight());
        camera.position.x = BrainOutClient.getWidth() / 2;
        camera.position.y = BrainOutClient.getHeight() / 2;

        batch = BrainOutClient.ClientSett.allocateNewBatch();

        RenderContext renderContext = new RenderContext(BrainOutClient.getWidth(), BrainOutClient.getHeight());
        renderContext.drawRecursive = false;

        try
        {
            this.frame = new FrameBuffer(Pixmap.Format.RGBA8888, BrainOutClient.getWidth(),
                    BrainOutClient.getHeight(), false);
        }
        catch (Exception ignored)
        {
            this.frame = null;
            return;
        }

        this.region = new TextureRegion(frame.getColorBufferTexture());
        region.flip(false, true);

        batch.begin();
        batch.setProjectionMatrix(BrainOutClient.ClientController.getBatch().getProjectionMatrix());
        frame.begin();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        BrainOutClient.ClientController.render(batch, renderContext);

        frame.end();
        batch.end();

    }

    @Override
    public void release()
    {
        super.release();

        if (frame != null)
        {
            frame.dispose();
            frame = null;
        }

        if (batch != null)
        {
            batch.dispose();
            batch = null;
        }
    }

    @Override
    public void update(float dt)
    {
        flashBang -= dt;

        if (flashBang <= 0)
        {
            fadeOut -= dt;
        }
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
