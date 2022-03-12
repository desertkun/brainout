package com.desertkun.brainout.data;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.client.states.map.CSMapLoad;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.components.my.MyPlayerComponent;
import com.desertkun.brainout.containers.ClientChunkData;
import com.desertkun.brainout.content.MusicList;
import com.desertkun.brainout.content.effect.Effect;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.parallax.Parallax;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.containers.ActiveDataMap;
import com.desertkun.brainout.data.containers.BlockMatrixData;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.containers.CompleteDataContainer;
import com.desertkun.brainout.data.effect.CancellableEffect;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.parallax.ParallaxData;
import com.desertkun.brainout.events.ComponentUpdatedEvent;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.utils.TimeMeasure;
import com.esotericsoftware.minlog.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientMap extends Map
{
    protected ParallaxData parallax;
    protected Array<CompleteDataContainer<EffectData>> effects;
    protected MusicList musicList;
    protected RayHandler lights;
    protected boolean lightsEnabled = true;
    protected Color ambientLight;
    protected float scale;

    private TimeMeasure lightCalculation = new TimeMeasure();
    private Box2DDebugRenderer debugRenderer;
    private Matrix4 debugRendererMatrix;

    public ClientMap(String dimension)
    {
        this(dimension, 0, 0);
    }

    @Override
    protected ActiveDataMap newActives(int layerCount, String dimension)
    {
        return new ClientActiveDataMap(layerCount, dimension);
    }

    @Override
    protected BlockMatrixData newBlockMatrixData(String dimension)
    {
        return new ClientBlockMatrixData(dimension);
    }

    public ClientMap(String dimension, int width, int height)
    {
        super(dimension, width, height);

        parallax = null;
        ambientLight = new Color(0, 0, 0, 0.8f);
        scale = 1.0f;

        initCamera();

    }

    @Override
    protected void initChilds()
    {
        if (BrainOutClient.ClientSett.isLightsEnabled())
        {
            int quality = BrainOutClient.ClientSett.getLightDiv();

            try
            {
                lights = new RayHandler(physicWorld,
                    BrainOutClient.getWidth() / quality, BrainOutClient.getHeight() / quality);

                lights.setAmbientLight(ambientLight);

                lights.setBlur(BrainOutClient.ClientSett.hasSoftShadows());
                lights.setShadows(BrainOutClient.ClientSett.hasShadows());
            }
            catch (IllegalStateException e)
            {
                //
            }
        }

        super.initChilds();
    }

    @Override
    public void init()
    {
        super.init();

        getComponents().init();
    }

    public void playMusic()
    {
        if (musicList != null)
        {
            BrainOutClient.MusicMng.playList(musicList);
        }
    }

    @Override
    public ChunkData getChunkData(BlockMatrixData matrixData, int x, int y)
    {
        return new ClientChunkData(matrixData, x, y);
    }

    private void initCamera()
    {
        setScale(1.0f);

        //tileCamera.getCamera().setToOrtho(false, screenSize.x, screenSize.y );
        //cameraBefore.getCamera().setToOrtho(false, BrainOutClient.getWidth(), BrainOutClient.getHeight() );
        //cameraAfter.getCamera().setToOrtho(false, BrainOutClient.getWidth(), BrainOutClient.getHeight());
    }

    public void setScale(float zoom)
    {
        root.getCamera().zoom = zoom / Constants.Graphics.RES_SIZE;
    }

    private void setParallax(ParallaxData parallax)
    {
        if (this.parallax != null)
        {
            cameraBefore.removeItem(this.parallax);
            this.parallax = null;
        }

        if (parallax != null)
        {
            cameraBefore.addItem(parallax, RenderFilter.pre);
        }

        this.parallax = parallax;
    }

    public ParallaxData getParallaxData()
    {
        return parallax;
    }

    @Override
    public void initCustom()
    {
        String parallaxCustom = getCustom("parallax");
        if (parallaxCustom == null) parallaxCustom = getCustom("PARALLAX");

        if (parallaxCustom != null)
        {
            Parallax parallax = (Parallax) BrainOut.ContentMgr.get(parallaxCustom);

            if (parallax != null)
            {
                setParallax(parallax.getData(this));
            }
            else
            {
                setParallax(null);
            }
        }

        String musicListCustom = getCustom("music-list");
        if (musicListCustom == null) musicListCustom = getCustom("musicList");

        if (musicListCustom != null)
        {
            musicList = ((MusicList) BrainOut.ContentMgr.get(musicListCustom));

            BrainOutClient.MusicMng.stopMusic();
            playMusic();
        }

        String ambientLightCustom = getCustom("ambient-light");
        if (ambientLightCustom == null) ambientLightCustom = getCustom("ambientLight");

        if (ambientLightCustom != null)
        {
            try
            {
                float f = Float.valueOf(ambientLightCustom);
                ambientLight.set(0, 0, 0, f);
            }
            catch (NumberFormatException e)
            {
                try
                {
                    ambientLight.set(Color.valueOf(ambientLightCustom));
                }
                catch (StringIndexOutOfBoundsException ignored)
                {

                }
            }
        }

        if (lights != null)
        {
            lights.setAmbientLight(ambientLight);
        }
    }

    public EffectData addEffect(Effect effect, LaunchData launchData)
    {
        if (effect == null || !effect.isEnabled())
            return null;

        return addEffect(effect.getEffect(launchData));
    }

    public EffectData addEffect(Effect effect, LaunchData launchData, EffectSet.EffectAttacher attacher)
    {
        if (effect == null || !effect.isEnabled())
            return null;

        return addEffect(effect.getEffect(launchData, attacher));
    }

    public EffectData addEffect(EffectData effectData)
    {
        effects.get(effectData.getEffectLayer()).addItem(effectData);

        return effectData;
    }

    public void removeEffect(Array<EffectData> effectDataArray)
    {
        for (EffectData effectData: effectDataArray)
        {
            if (effectData instanceof CancellableEffect)
            {
                ((CancellableEffect) effectData).cancel();
            }
            else
            {
                removeEffect(effectData);
            }
        }
    }

    public void removeEffect(EffectData effectData)
    {
        effects.get(effectData.getEffectLayer()).removeItem(effectData);
    }

    @Override
    protected void initRoot()
    {
        effects = new Array<>();

        for (int i = 0; i < Constants.Layers.EFFECT_LAYERS_COUNT; i++)
        {
            effects.add(new CompleteDataContainer<>());
        }

        root.addUpdateItem(blocks);

        root.addItem(effects.get(Constants.Layers.EFFECT_LAYER_1), RenderFilter.pre);
        root.addRenderableItem(actives.getRenderLayer(Constants.Layers.ACTIVE_LAYER_1), RenderFilter.pre);
        root.addRenderableItem(blocks.getRenderLayer(Constants.Layers.BLOCK_LAYER_BACKGROUND), RenderFilter.pre);
        root.addRenderableItem(actives.getRenderLayer(Constants.Layers.ACTIVE_LAYER_1TOP), RenderFilter.pre);
        root.addRenderableItem(blocks.getRenderLayer(Constants.Layers.BLOCK_LAYER_FOREGROUND), RenderFilter.pre);
        root.addRenderableItem(actives.getRenderLayer(Constants.Layers.ACTIVE_LAYER_2), RenderFilter.pre);
        root.addItem(bullets, RenderFilter.pre);
        root.addItem(effects.get(Constants.Layers.EFFECT_LAYER_2), RenderFilter.pre);
        root.addRenderableItem(actives.getRenderLayer(Constants.Layers.ACTIVE_LAYER_3), RenderFilter.pre);
        root.addRenderableItem(blocks.getRenderLayer(Constants.Layers.BLOCK_LAYER_UPPER), RenderFilter.pre);
        root.addItem(effects.get(Constants.Layers.EFFECT_LAYER_3), RenderFilter.post);

        root.addItem(components, RenderFilter.post);

        root.addRenderableItem(ClientPlayerComponent.AimRenderable(), RenderFilter.post);

        root.addUpdateItem(actives);
    }

    public void preRender()
    {
        //
    }

    public void postRender()
    {
        if (debugRenderer != null)
        {
            debugRendererMatrix.set(root.getCamera().combined);
            debugRendererMatrix.scale(
                    Constants.Physics.SCALE_OF,
                    Constants.Physics.SCALE_OF,
                    Constants.Physics.SCALE_OF);
            debugRenderer.render(getPhysicWorld(), debugRendererMatrix);
        }

        renderLights();

    }

    public void setPhysicsDebugging(boolean enabled)
    {
        if (enabled == isPhysicsDebuggingEnabled())
            return;

        if (enabled)
        {
            debugRenderer = new Box2DDebugRenderer(true, false, false, true, false, false);
            debugRendererMatrix = new Matrix4();
        }
        else
        {
            debugRenderer.dispose();
            debugRenderer = null;
        }
    }

    public boolean isPhysicsDebuggingEnabled()
    {
        return debugRenderer != null;
    }

    protected void renderLights()
    {
        if (lights != null && lightsEnabled)
        {
            lights.setCombinedMatrix(root.getCamera());
            lights.render();
        }
    }

    public void setLightsEnabled(boolean lightsEnabled)
    {
        this.lightsEnabled = lightsEnabled;
    }

    public ActiveData updateActiveDataComponent(int id, String data, String clazz, boolean init)
    {
        Class clz = BrainOut.R.forName(clazz);

        ActiveData activeData = getActives().get(id);

        if (activeData != null)
        {
            Component component = activeData.getComponent(clz);

            if (component instanceof Json.Serializable)
            {
                Json.Serializable serializable = ((Json.Serializable) component);

                JsonValue value = new JsonReader().parse(data);
                serializable.read(json, value);

                component.updated(activeData);

                BrainOut.EventMgr.sendDelayedEvent(activeData,
                    ComponentUpdatedEvent.obtain(component, activeData));
            }
        }

        return activeData;
    }

    public BlockData newBlockData(String data, boolean init)
    {
        JsonValue value = new JsonReader().parse(data);

        BlockData blockData = newBlockData(json, value);

        if (blockData == null)
        {
            return null;
        }

        if (init)
        {
            blockData.init();
        }

        return blockData;
    }

    public InstrumentData newInstrumentData(String data)
    {
        JsonValue value = new JsonReader().parse(data);

        if (value == null)
            return null;

        return newInstrumentData(json, value);
    }

    public void removeActiveData(int id, boolean ragdoll)
    {
        ActiveData activeData = getActives().get(id);

        if (activeData != null)
        {
            BrainOutClient.EventMgr.sendEvent(activeData, DestroyEvent.obtain(ragdoll));

            removeActive(activeData, true, true, ragdoll);
        }
    }

    @Override
    public void releaseActive(ActiveData active)
    {
        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);

        if (csGame != null)
        {
            if (active == csGame.getPlayerData())
            {
                csGame.setPlayerData(null);
            }
        }
    }

    @Override
    public void initActive(ActiveData activeData)
    {
        int ownerId = activeData.getOwnerId();
        RemoteClient remoteClient = null;

        if (ownerId != -1)
        {
            remoteClient = BrainOutClient.ClientController.getRemoteClients().get(ownerId);
        }

        // todo: make creating more intelligent

        if (activeData instanceof PlayerData)
        {
            PlayerData playerData = ((PlayerData) activeData);

            ClientPlayerComponent cpc = new ClientPlayerComponent(playerData, remoteClient);
            activeData.addComponent(cpc);
            cpc.init();

            if (ownerId == BrainOutClient.ClientController.getMyId())
            {
                MyPlayerComponent mpc = new MyPlayerComponent(playerData);
                activeData.addComponent(mpc);
                mpc.init();

                SetWatcher(cpc);

                // it's our spawn
                CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);

                if (csGame != null)
                {
                    csGame.onSpawned(playerData);
                }
                else
                {
                    CSMapLoad mapLoad = BrainOutClient.ClientController.getState(CSMapLoad.class);

                    if (mapLoad != null)
                    {
                        mapLoad.setPlayerData(playerData);
                    }
                }
            }
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (lights != null)
        {
            lightCalculation.start();
            lights.update();
            lightCalculation.end();
        }

        if (Watcher != null)
        {
            scale = MathUtils.lerp(scale, Watcher.getScale(), 5.0f * dt);
            setScale(scale);
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();

        if (lights != null)
        {
            lights.dispose();
        }

        if (debugRenderer != null)
        {
            debugRenderer.dispose();
            debugRenderer = null;
        }
    }

    public TimeMeasure getLightCalculation()
    {
        return lightCalculation;
    }

    @Override
    public void postRunnable(Runnable runnable)
    {
        Gdx.app.postRunnable(runnable);
    }

    public RayHandler getLights()
    {
        return lights;
    }

    public static void getMouseScale(float mouseX, float mouseY, Vector2 setTo)
    {
        setTo.set(mouseX / Constants.Graphics.RES_SIZE, mouseY / Constants.Graphics.RES_SIZE);
    }

    @Override
    public void updateExtensions()
    {
        super.updateExtensions();

        TexturePacker texturePacker = null;

        for (ObjectMap.Entry<String, byte[]> entry : getExtensions())
        {
            if (entry.key.endsWith(".png"))
            {
                byte[] imageData = entry.value;
                try
                {
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

                    if (texturePacker == null)
                    {
                        TexturePacker.Settings texturePackerSettings = new TexturePacker.Settings();

                        texturePackerSettings.maxWidth = 2048;
                        texturePackerSettings.maxHeight = 2048;
                        texturePackerSettings.fast = true;

                        texturePacker = new TexturePacker(texturePackerSettings);
                    }

                    texturePacker.addImage(image, entry.key);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    if (Log.ERROR) Log.error("Failed to load image: " + entry.key);
                }
            }
        }

        if (texturePacker != null)
        {
            Path dir;

            try
            {
                dir = Files.createTempDirectory("brainout-texture-pack");
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return;
            }

            texturePacker.pack(dir.toFile(), "extensions");
            Path atlas = dir.resolve("extensions.atlas");
            String atlasPath = Gdx.files.absolute(atlas.toString()).path();
            AssetManager m = BrainOutClient.PackageMgr.getFirstAssetManager();

            if (BrainOutClient.TextureMgr.getAtlas("extensions") != null)
            {
                BrainOutClient.TextureMgr.cleanupAtlas("extensions");
            }

            BrainOutClient.TextureMgr.loadAtlas(atlasPath, m);
            m.finishLoadingAsset(atlasPath);

            BrainOutClient.TextureMgr.registerAtlas("extensions", atlasPath, m);

        }
    }
}
