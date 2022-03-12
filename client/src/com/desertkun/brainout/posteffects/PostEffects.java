package com.desertkun.brainout.posteffects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.posteffects.effects.PostEffect;

public class PostEffects implements Disposable
{
    private PostEffect postEffect;
    private boolean enabled;
    private boolean inited;

    private FrameBuffer fbo;
    private TextureRegion fboRegion;

    private FrameBuffer targetFbo;
    private TextureRegion targetFboRegion;

    private OrthographicCamera camera;
    private Batch batch;

    private boolean disabled;
    private int w;
    private int h;

    public PostEffects()
    {
        this.postEffect = null;
        this.enabled = false;
        this.batch = new SpriteBatch();
        this.disabled = false;
    }

    public PostEffect getPostEffect()
    {
        return postEffect;
    }

    private int nextPowerOfTwo(int num)
    {
        return num == 1 ? 1 : Integer.highestOneBit(num - 1) * 2;
    }

    public void init()
    {
        if (inited) return;
        if (postEffect == null) return;

        Gdx.app.postRunnable(() -> {
            try
            {
                this.w = BrainOutClient.getWidth() / postEffect.getDownscale();
                this.h = BrainOutClient.getHeight() / postEffect.getDownscale();

                fbo = new FrameBuffer(Pixmap.Format.RGBA8888, BrainOutClient.getWidth(),
                    BrainOutClient.getHeight(), false);
                fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                fboRegion = new TextureRegion(fbo.getColorBufferTexture());
                fboRegion.flip(false, true);


                targetFbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
                targetFbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                targetFboRegion = new TextureRegion(targetFbo.getColorBufferTexture());
                targetFboRegion.flip(false, true);

                camera = new OrthographicCamera(BrainOutClient.getWidth(), BrainOutClient.getHeight());
                camera.position.set(BrainOutClient.getWidth() / 2.0f, BrainOutClient.getHeight() / 2.0f, 0.0f);

                camera.update();
                inited = true;
            }
            catch (Exception e)
            {
                disabled = true;
            }
        });
    }

    public boolean begin()
    {
        if (inited && enabled && !disabled)
        {
            fbo.begin();

            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

            return true;
        }

        return false;
    }

    public void end()
    {
        if (inited && enabled && !disabled)
        {
            fbo.end();

            targetFbo.begin();

            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            postEffect.begin(batch);

            camera.update();

            batch.draw(fboRegion, 0, 0, BrainOutClient.getWidth(), BrainOutClient.getHeight());
            batch.end();
            targetFbo.end();

            postEffect.end(batch);

            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            batch.draw(targetFboRegion, 0, 0, BrainOutClient.getWidth(), BrainOutClient.getHeight());
            batch.end();
        }
    }

    public void setEffect(PostEffect effect)
    {
        release();

        postEffect = effect;
        enabled = postEffect != null;

        init();
    }

    public void reset()
    {
        release();
        init();
    }

    public void release()
    {
        if (fbo != null)
        {
            fbo.dispose();
            fbo = null;
        }

        if (targetFbo != null)
        {
            targetFbo.dispose();
            targetFbo = null;
        }

        inited = false;
        disabled = false;
    }

    public void resetEffect()
    {
        postEffect = null;

        enabled = false;
    }

    @Override
    public void dispose()
    {
        release();

        resetEffect();
    }
}
