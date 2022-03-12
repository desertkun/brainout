package com.desertkun.brainout.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.containers.ActiveDataMap;
import com.desertkun.brainout.data.containers.CompleteDataContainer;
import com.desertkun.brainout.data.editor2.Editor2Grid;
import com.desertkun.brainout.data.editor2.Editor2Helper;
import com.desertkun.brainout.data.interfaces.Cacheble;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.utils.JPGWriter;

import java.io.File;
import java.io.IOException;

public class Editor2Map extends ClientMap
{
    private Editor2Helper helper;
    private Editor2Grid grid;
    private boolean previewMode;

    public static Vector2 StartLocation = new Vector2();

    public Editor2Map(String dimension)
    {
        super(dimension);
    }

    public Editor2Map(String dimension, int width, int height)
    {
        super(dimension, width, height);
    }

    public boolean isGridEnabled()
    {
        return grid.isGridEnabled();
    }

    public class EditorActiveDataMap extends ClientActiveDataMap
    {
        public class EditorRenderMap extends ClientRenderMap implements EventReceiver
        {
            private boolean visible = true;
            private boolean enabled = true;

            public EditorRenderMap(int layer, String dimension)
            {
                super(layer, dimension);
            }

            @Override
            public void render(Batch batch, RenderContext context)
            {
                if (isVisible())
                {
                    super.render(batch, context);
                }
            }

            public boolean isVisible()
            {
                return visible;
            }

            public void setVisible(boolean visible)
            {
                this.visible = visible;
            }

            public boolean isEnabled()
            {
                return enabled && visible;
            }

            public void setEnabled(boolean enabled)
            {
                this.enabled = enabled;
            }

            @Override
            public void init()
            {
                super.init();

                BrainOut.EventMgr.subscribe(Event.ID.activeAction, this);
            }

            @Override
            public void release()
            {
                super.release();

                BrainOut.EventMgr.unsubscribe(Event.ID.activeAction, this);
            }

            public void updated(ActiveActionEvent.Action action, ActiveData value)
            {
                Cacheble hasCache = null;

                Component it = value.getFistComponent();

                while (it != null)
                {
                    if (it instanceof Cacheble)
                    {
                        Cacheble cacheble = ((Cacheble) it);

                        if (cacheble.hasCache())
                        {
                            hasCache = cacheble;
                        }
                    }

                    it = it.getNext();
                }

                switch (action)
                {
                    case updated:
                    {
                        OrderedMap<ActiveData, Cacheble> cacheList = getCache().getCacheList();

                        if (value.getLayer() == getLayer())
                        {
                            if (!cacheList.containsKey(value))
                            {
                                if (hasCache != null)
                                {
                                    cacheList.put(value, hasCache);
                                }
                            }

                            setDirty();
                        }
                        else
                        {
                            if (cacheList.containsKey(value))
                            {
                                cacheList.remove(value);
                                setDirty();
                            }
                        }

                        break;
                    }
                    case added:
                    {
                        OrderedMap<ActiveData, Cacheble> cacheList = getCache().getCacheList();

                        if (value.getLayer() == getLayer())
                        {
                            if (!cacheList.containsKey(value))
                            {
                                if (hasCache != null)
                                {
                                    cacheList.put(value, hasCache);
                                    setDirty();
                                }
                            }
                        }

                        break;
                    }
                    case removed:
                    {
                        OrderedMap<ActiveData, Cacheble> cacheList = getCache().getCacheList();

                        if (value.getLayer() == getLayer())
                        {
                            if (cacheList.containsKey(value))
                            {
                                if (hasCache != null)
                                {
                                    cacheList.remove(value);
                                    setDirty();
                                }
                            }
                        }

                        break;
                    }
                }
            }

            @Override
            public boolean onEvent(Event event)
            {
                switch (event.getID())
                {
                    case activeAction:
                    {
                        ActiveActionEvent e = ((ActiveActionEvent) event);
                        updated(e.action, e.activeData);

                        break;
                    }
                }

                return false;
            }
        }

        public EditorActiveDataMap(int layers, String dimension)
        {
            super(layers, dimension);
        }

        @Override
        public RenderMap newRenderMap(int layer, String dimension)
        {
            return new Editor2Map.EditorActiveDataMap.EditorRenderMap(layer, dimension);
        }

        @Override
        public EditorRenderMap getRenderLayer(int layer)
        {
            return (EditorRenderMap)super.getRenderLayer(layer);
        }
    }

    public void setGridEnabled(boolean grid)
    {
        this.grid.setGridEnabled(grid);
    }

    public static void SetGridEnabled(boolean grid)
    {
        for (Editor2Map map: Map.All(Editor2Map.class))
        {
            map.setGridEnabled(grid);
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();

        helper.dispose();
    }

    @Override
    protected ActiveDataMap newActives(int layerCount, String dimension)
    {
        return new Editor2Map.EditorActiveDataMap(layerCount, dimension);
    }

    @Override
    protected void initRoot()
    {
        createRoot();

        effects = new Array<>();

        for (int i = 0; i < Constants.Layers.EFFECT_LAYERS_COUNT; i++)
        {
            effects.add(new CompleteDataContainer<>());
        }

        ActiveDataMap.RenderMap layer1 = actives.getRenderLayer(Constants.Layers.ACTIVE_LAYER_1);
        layer1.put(generateClientId(), grid);


        root.addItem(effects.get(Constants.Layers.EFFECT_LAYER_1), RenderFilter.pre);
        root.addRenderableItem(layer1, RenderFilter.pre);
        root.addRenderableItem(blocks.getRenderLayer(Constants.Layers.BLOCK_LAYER_BACKGROUND), RenderFilter.pre);
        root.addRenderableItem(actives.getRenderLayer(Constants.Layers.ACTIVE_LAYER_1TOP), RenderFilter.pre);
        root.addRenderableItem(blocks.getRenderLayer(Constants.Layers.BLOCK_LAYER_FOREGROUND), RenderFilter.pre);
        root.addUpdateItem(blocks);
        root.addRenderableItem(actives.getRenderLayer(Constants.Layers.ACTIVE_LAYER_2), RenderFilter.pre);
        root.addItem(bullets, RenderFilter.pre);
        root.addItem(effects.get(Constants.Layers.EFFECT_LAYER_2), RenderFilter.pre);
        root.addRenderableItem(actives.getRenderLayer(Constants.Layers.ACTIVE_LAYER_3), RenderFilter.pre);
        root.addRenderableItem(blocks.getRenderLayer(Constants.Layers.BLOCK_LAYER_UPPER), RenderFilter.pre);
        root.addItem(effects.get(Constants.Layers.EFFECT_LAYER_3), RenderFilter.post);
        root.addItem(components, RenderFilter.pre);
        root.addUpdateItem(actives);
        root.addItem(helper, RenderFilter.pre);

        root.addRenderableItem(ClientPlayerComponent.AimRenderable(), RenderFilter.post);

    }

    private void createRoot()
    {
        helper = new Editor2Helper(dimension);
        grid = new Editor2Grid(dimension);
    }

    public File renderPreview() throws IOException
    {
        File file = File.createTempFile("map-preview", ".png");

        Watcher old = Map.GetWatcher();

        float DOWNSCALE = 2f;

        float width = blocks.getWidth() * ((float)Constants.Graphics.BLOCK_SIZE / DOWNSCALE),
              height = blocks.getHeight() * ((float)Constants.Graphics.BLOCK_SIZE / DOWNSCALE);

        Map.SetWatcher(new Watcher()
        {
            @Override
            public float getWatchX()
            {
                return blocks.getWidth() / 2.0f;
            }

            @Override
            public float getWatchY()
            {
                return blocks.getHeight() / 2.0f;
            }

            @Override
            public boolean allowZoom()
            {
                return true;
            }

            @Override
            public float getScale()
            {
                return 2.0f;
            }

            @Override
            public String getDimension()
            {
                return Editor2Map.this.getDimension();
            }
        });

        OrthographicCamera camera = new OrthographicCamera(width, height);
        camera.setToOrtho(true, width, height);
        Batch batch = BrainOutClient.ClientSett.allocateNewBatch();
        batch.setProjectionMatrix(camera.combined);
        RenderContext context = new RenderContext(0, 0, width, height, false, 0);
        context.camera = camera;

        FrameBuffer frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, (int)width, (int)height, false);
        Texture texture = frameBuffer.getColorBufferTexture();
        texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap);

        //root.getCamera().setToOrtho(true, (int) width, (int) height);
        root.setFlipY(true);
        cameraBefore.setFlipY(true);
        cameraAfter.setFlipY(true);
        setScale(DOWNSCALE);

        setPreviewMode(true);

        /*
        if (lights != null)
        {
            lights.resizeFBO((int) width, (int) height);
            lights.setCombinedMatrix(root.getCamera().combined, 0, 0, width, height);
            lights.setLightMapRendering(false);
            lights.updateAndRender();
            lights.setLightMapRendering(true);
        }
        */

        frameBuffer.begin();
        batch.begin();
        render(batch, context);
        batch.end();

        //lights.renderLightMap();
        setPreviewMode(false);

        frameBuffer.end();

        frameBuffer.bind();

        Pixmap p = new Pixmap((int)width, (int)height, Pixmap.Format.RGBA8888);
        Gdx.gl.glReadPixels(0, 0, (int)width, (int)height, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, p.getPixels());

        FrameBuffer.unbind();

        /*
        int quality = BrainOutClient.ClientSett.getLightDiv();

        if (lights != null)
        {
            try
            {
                lights.resizeFBO(BrainOutClient.getWidth() / quality, BrainOutClient.getHeight() / quality);
                lights.update();
            } catch (IllegalStateException e)
            {
                //
            }
        }
        */

        JPGWriter.Write(Gdx.files.absolute(file.getAbsolutePath()), p);

        frameBuffer.dispose();
        batch.dispose();

        root.setFlipY(false);
        cameraBefore.setFlipY(false);
        cameraAfter.setFlipY(false);
        setScale(1);

        Map.SetWatcher(old);


        return file;
    }

    private void setPreviewMode(boolean mode)
    {
        this.previewMode = mode;
    }

    public boolean isPreviewMode()
    {
        return previewMode;
    }
}
