package com.desertkun.brainout.data;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.containers.ClientChunkData;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.containers.ActiveDataMap;
import com.desertkun.brainout.data.containers.BlockMatrixData;
import com.desertkun.brainout.data.interfaces.Cacheble;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.editor.data.EditorWatcher;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;

public class EditorMap extends ClientMap
{
    private boolean lightsVisible;
    private boolean shadowEnabled;
    private Array<BlockLayerInfo> blockLayers;
    private EditorWatcher editorWatcher;

    public class BlockLayerInfo
    {
        public boolean visible = true;
        public boolean enabled = true;
    }

    public EditorMap(String dimension)
    {
        this(dimension, 0, 0);
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
            return new EditorRenderMap(layer, dimension);
        }

        @Override
        public EditorRenderMap getRenderLayer(int layer)
        {
            return (EditorRenderMap)super.getRenderLayer(layer);
        }
    }

    public class EditorChunkData extends ClientChunkData
    {
        public class EditorBlocksLayer extends ClientChunkData.ClientBlocksLayer
        {
            public EditorBlocksLayer(int layer)
            {
                super(layer);
            }

            @Override
            public void render(Batch batch, RenderContext context)
            {
                if (blockLayers.get(layer).visible)
                {
                    super.render(batch, context);
                }
            }
        }

        public EditorChunkData(BlockMatrixData matrixData, int idX, int idY)
        {
            super(matrixData, idX, idY);
        }

        @Override
        protected BlocksLayer newLayer(int layer)
        {
            return new EditorBlocksLayer(layer);
        }
    }

    public EditorMap(String dimension, int width, int height)
    {
        super(dimension, width, height);

        lightsVisible = true;
        shadowEnabled = true;
        blockLayers = new Array<>(Constants.Layers.BLOCK_LAYERS_COUNT);

        for (int i = 0; i < Constants.Layers.BLOCK_LAYERS_COUNT; i++)
        {
            blockLayers.add(new BlockLayerInfo());
        }
    }

    @Override
    public EditorActiveDataMap getActives()
    {
        return ((EditorActiveDataMap) super.getActives());
    }

    @Override
    protected ActiveDataMap newActives(int layerCount, String dimension)
    {
        return new EditorActiveDataMap(layerCount, dimension);
    }

    public ChunkData getChunkData(BlockMatrixData matrixData, int x, int y)
    {
        return new EditorChunkData(matrixData, x, y);
    }

    public BlockLayerInfo getBlocksLayer(int layer)
    {
        return blockLayers.get(layer);
    }

    @Override
    protected void renderLights()
    {
        if (isLightsVisible())
        {
            super.renderLights();
        }
    }

    @Override
    public void reattachActive(int id, int layer)
    {
        super.reattachActive(id, layer);
    }

    public boolean isShadowEnabled()
    {
        return shadowEnabled;
    }

    public void setShadowEnabled(boolean shadowEnabled)
    {
        this.shadowEnabled = shadowEnabled;
    }

    public boolean isLightsVisible()
    {
        return lightsVisible;
    }

    public void setLightsVisible(boolean lightsVisible)
    {
        this.lightsVisible = lightsVisible;
    }

    @Override
    public void dispose()
    {
        super.dispose();

        setEditorWatcher(null);
    }

    public void setEditorWatcher(EditorWatcher editorWatcher)
    {
        this.editorWatcher = editorWatcher;
    }

    public EditorWatcher getEditorWatcher()
    {
        return editorWatcher;
    }
}
