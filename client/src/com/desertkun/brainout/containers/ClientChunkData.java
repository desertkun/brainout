package com.desertkun.brainout.containers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.math.Vector3;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.containers.BlockMatrixData;
import com.desertkun.brainout.data.interfaces.RenderContext;

public class ClientChunkData extends ChunkData
{
    private boolean visible;

    public class ClientBlocksLayer extends BlocksLayer
    {
        private SpriteCache cache;
        private int cacheId;

        public ClientBlocksLayer(int layer)
        {
            super(layer);

            this.cacheId = -1;
        }

        public void init(boolean updateCache)
        {
            this.cache = new SpriteCache(4096, true);

            if (updateCache)
            {
                updateCache(this.layer);
            }
        }

        public void updateCache(int layer)
        {
            if (cache == null)
                return;

            BlocksLayer l = layers.get(layer);

            if (l == null)
                return;

            Map map = getMap();
            if (map == null)
                return;

            cache.clear();
            cache.beginCache();

            BlockData.CURRENT_DIMENSION = map.getDimension();
            BlockData.CURRENT_LAYER = layer;

            for (int j = 0; j < Constants.Core.CHUNK_SIZE; j++)
            {
                for (int i = 0; i < Constants.Core.CHUNK_SIZE; i++)
                {
                    BlockData item = l.getNoCheck(i, j);

                    if (item != null)
                    {
                        BlockData.CURRENT_X = i + idX * Constants.Core.CHUNK_SIZE;
                        BlockData.CURRENT_Y = j + idY * Constants.Core.CHUNK_SIZE;

                        item.cache(map, cache);
                    }
                }
            }

            cacheId = cache.endCache();
        }

        public void render(Batch batch, RenderContext context)
        {
            if (cacheId >= 0)
            {
                batch.end();

                cache.setProjectionMatrix(batch.getProjectionMatrix());
                cache.begin();
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                Gdx.gl.glEnable(GL20.GL_BLEND);
                cache.draw(cacheId);
                cache.end();

                batch.begin();
            }

            activeBlockList.render(batch, context);
        }

        @Override
        public void dispose()
        {
            super.dispose();

            if (this.cache != null)
            {
                cache.dispose();
            }
        }
    }

    public ClientChunkData(BlockMatrixData matrixData, int idX, int idY)
    {
        super(matrixData, idX, idY);
    }

    @Override
    protected BlocksLayer newLayer(int layer)
    {
        return new ClientBlocksLayer(layer);
    }

    @Override
    public void init()
    {
        for (BlocksLayer layer : layers)
        {
            if (layer == null)
                continue;

            layer.init();
        }

        super.init();
    }

    @Override
    protected void updateCache(int layer)
    {
        if (Gdx.graphics == null)
        {
            return;
        }

        ClientBlocksLayer blocksLayer = ((ClientBlocksLayer) layers.get(layer));

        if (blocksLayer != null)
        {
            blocksLayer.updateCache(layer);
        }
    }

    @Override
    public void renderLayer(Batch batch, RenderContext context, int layer)
    {
        visible = isVisible(context);

        if (visible)
        {
            BlocksLayer layer_ = layers.get(layer);

            if (layer_ == null)
                return;

            ((ClientBlocksLayer) layer_).render(batch, context);
        }
    }

    private boolean isVisible(RenderContext context)
    {
        Vector3 pos = context.camera.position;

        float
              watcherW = (BrainOutClient.getWidth() / Constants.Graphics.RES_SIZE),
              watcherH = (BrainOutClient.getHeight() / Constants.Graphics.RES_SIZE),
              watcherX = pos.x - watcherW / 2f,
              watcherY = pos.y - watcherH / 2f;

        float posX = idX * Constants.Core.CHUNK_SIZE,
              posY = idY * Constants.Core.CHUNK_SIZE;

        return
            watcherX < posX + Constants.Core.CHUNK_SIZE + Constants.Graphics.DISPLAY_ADDITINAL_BLOCKS &&
            watcherY < posY + Constants.Core.CHUNK_SIZE + Constants.Graphics.DISPLAY_ADDITINAL_BLOCKS &&
            watcherX + watcherW + Constants.Graphics.DISPLAY_ADDITINAL_BLOCKS > posX &&
            watcherY + watcherH + Constants.Graphics.DISPLAY_ADDITINAL_BLOCKS > posY;
    }

    @Override
    public void dispose()
    {
        super.dispose();
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    public boolean isVisible()
    {
        return visible;
    }
}
