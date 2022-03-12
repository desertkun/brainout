package com.desertkun.brainout.data.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.containers.ClientChunkData;
import com.desertkun.brainout.content.components.ClientBackgroundEffectComponent;
import com.desertkun.brainout.content.effect.ParticleEffect;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientBackgroundEffectComponent")
@ReflectAlias("data.components.ClientBackgroundEffectComponentData")
public class ClientBackgroundEffectComponentData extends Component<ClientBackgroundEffectComponent>
{
    private final ParticleEffect particleEffect;

    private EffectData effectData;
    private FrameBuffer frameBuffer;
    private Batch batch;
    private OrthographicCamera camera;
    private TextureRegion textureRegion;
    private RenderContext context;

    public ClientBackgroundEffectComponentData(ComponentObject componentObject,
                                               ClientBackgroundEffectComponent contentComponent)
    {
        super(componentObject, contentComponent);

        particleEffect = contentComponent.getParticleEffect();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (BrainOutClient.ClientSett.isBackgroundEffectsEnabled() && effectData != null)
        {
            effectData.update(dt);
            renderBuffer();
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        if (BrainOutClient.ClientSett.isBackgroundEffectsEnabled())
        {
            Map map = getMap();

            if (map == null)
                return;

            if (textureRegion == null)
                return;

            for (int j = 0, t = map.getBlocks().getBlockHeight(); j < t; j++)
            {
                for (int i = 0, k = map.getBlocks().getBlockWidth(); i < k; i++)
                {
                    ClientChunkData chunkData = ((ClientChunkData) map.getChunk(i, j));

                    if (chunkData != null && chunkData.isVisible())
                    {
                        batch.draw(textureRegion, i * Constants.Core.CHUNK_SIZE,
                                j * Constants.Core.CHUNK_SIZE,
                                Constants.Core.CHUNK_SIZE,
                                Constants.Core.CHUNK_SIZE);
                    }
                }
            }
        }

    }

    private void renderBuffer()
    {
        if (frameBuffer == null)
            return;

        frameBuffer.begin();

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);


        this.batch.begin();

        drawEffect(0, 0, context);
        drawEffect(0, 1, context);
        drawEffect(0, -1, context);
        drawEffect(1, 0, context);
        drawEffect(-1, 0, context);

        this.batch.end();

        frameBuffer.end();
    }

    private void drawEffect(float x, float y, RenderContext context)
    {
        camera.position.set(Constants.Core.CHUNK_SIZE * (0.5f + x),
                Constants.Core.CHUNK_SIZE * (0.5f + y), 0);

        camera.update();
        this.batch.setProjectionMatrix(camera.combined);

        if (effectData != null)
            effectData.render(this.batch, context);
    }

    @Override
    public void init()
    {
        super.init();

        int size = Constants.Core.CHUNK_SIZE_PIX;
        try
        {
            frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, size, size, true, false);
        }
        catch (IllegalStateException ignored)
        {
            frameBuffer = null;
            return;
        }

        Texture texture = frameBuffer.getColorBufferTexture();
        textureRegion = new TextureRegion(texture);
        textureRegion.flip(false, true);

        if (particleEffect.isEnabled())
        {
            effectData = particleEffect.getEffect(new PointLaunchData(0, 0, 0,
                    getComponentObject().getDimension()));
            effectData.init();
        }

        camera = new OrthographicCamera(Constants.Core.CHUNK_SIZE, Constants.Core.CHUNK_SIZE);

        batch = BrainOutClient.ClientSett.allocateNewBatch();
        context = new RenderContext(camera.viewportWidth, camera.viewportHeight);
    }

    @Override
    public void release()
    {
        super.release();

        if (frameBuffer != null)
        {
            frameBuffer.dispose();
        }

        if (effectData != null)
        {
            effectData.release();
        }

        if (batch != null)
        {
            batch.dispose();
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return BrainOutClient.ClientSett.isBackgroundEffectsEnabled();
    }

    @Override
    public boolean hasUpdate()
    {
        return BrainOutClient.ClientSett.isBackgroundEffectsEnabled();
    }
}
