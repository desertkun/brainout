package com.desertkun.brainout.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.containers.ActiveDataMap;
import com.desertkun.brainout.data.interfaces.Cacheble;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;
import com.desertkun.brainout.data.interfaces.Renderable;

import java.util.Comparator;

public class ClientActiveDataMap extends ActiveDataMap
{
    public class ClientRenderMap extends RenderMap
    {
        public class ActiveCache extends ActiveData implements Comparator<ActiveData>
        {
            private SpriteCache cache;
            private OrderedMap<ActiveData, Cacheble> cacheList;
            private OrderedMap<Integer, ActiveData> cacheIDList;
            private boolean dirty;
            private int cacheId;

            public ActiveCache(String dimension)
            {
                super(null, dimension);

                cacheList = new OrderedMap<>();
                cacheIDList = new OrderedMap<>();
                dirty = true;
                cacheId = -1;
            }

            protected OrderedMap<ActiveData, Cacheble> getCacheList()
            {
                return cacheList;
            }

            public OrderedMap<Integer, ActiveData> getCacheIDList()
            {
                return cacheIDList;
            }

            @Override
            public void init()
            {
                try
                {
                    cache = new SpriteCache(4000, true);
                }
                catch (IllegalArgumentException ignore)
                {
                    cache = null;
                }
            }

            @Override
            public void release()
            {
                if (cache != null)
                    cache.dispose();
            }

            @Override
            public float getX()
            {
                return 0;
            }

            @Override
            public float getY()
            {
                return 0;
            }

            @Override
            public int getZIndex()
            {
                return 10;
            }

            @Override
            public int getLayer()
            {
                return ClientRenderMap.this.getLayer();
            }

            @Override
            public boolean hasRender()
            {
                return true;
            }

            @Override
            public void render(Batch batch, RenderContext context)
            {
                if (cache != null)
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

                    if (dirty)
                    {
                        dirty = false;

                        updateCache();
                    }
                }
                else
                {
                    for (Entry<ActiveData, Cacheble> entry : cacheList)
                    {
                        if (!(entry.value instanceof RenderUpdatable))
                            continue;

                        ((RenderUpdatable) entry.value).render(batch, context);
                    }
                }
            }

            public void updateCache()
            {
                cacheList.orderedKeys().sort(this);

                if (cache == null)
                    return;

                Map map = getMap();

                if (map == null)
                    return;

                cache.clear();
                cache.beginCache();

                for (Cacheble entry : cacheList.values())
                {
                    entry.cache(map, cache);
                }

                cacheId = cache.endCache();
            }

            public void setDirty()
            {
                this.dirty = true;
            }

            @Override
            public void setX(float x)
            {

            }

            @Override
            public void setY(float y)
            {

            }

            @Override
            public int compare(ActiveData o1, ActiveData o2)
            {
                int i1 = o1.getZIndex();
                int i2 = o2.getZIndex();

                return (int)Math.signum(i1 - i2);
            }

            @Override
            public float getAngle()
            {
                return 0;
            }
        }

        private ActiveCache cache;

        public ClientRenderMap(int layer, String dimension)
        {
            super(layer);

            cache = new ActiveCache(dimension);
        }

        public ActiveCache getCache()
        {
            return cache;
        }

        @Override
        public void init()
        {
            super.init();

            cache.init();
            addItem(generateClientId(), cache);
        }

        @Override
        public ActiveData put(Integer key, ActiveData value)
        {
            Component it = value.getFistComponent();

            while (it != null)
            {
                if (it instanceof Cacheble)
                {
                    Cacheble cacheble = ((Cacheble) it);

                    if (cacheble.hasCache())
                    {
                        cache.getCacheList().put(value, cacheble);
                        cache.getCacheIDList().put(value.getId(), value);
                        cache.setDirty();

                        return null;
                    }
                }

                it = it.getNext();
            }

            return super.put(key, value);
        }

        @Override
        public ActiveData remove(Integer key)
        {
            ActiveData value = cache.getCacheIDList().remove(key);

            if (value != null)
            {
                Cacheble cached = cache.getCacheList().remove(value);
                if (cached != null)
                {
                    cache.setDirty();
                }
            }

            return super.remove(key);
        }

        public void setDirty()
        {
            cache.setDirty();
        }

        public Renderable removeNoCheck(Integer key)
        {
            return super.remove(key);
        }

        public ActiveData putNoCheck(Integer key, ActiveData value)
        {
            return super.put(key, value);
        }

    }

    public ClientActiveDataMap(int layers, String dimension)
    {
        super(layers, dimension);
    }

    @Override
    public RenderMap newRenderMap(int layer, String dimension)
    {
        return new ClientRenderMap(layer, dimension);
    }
}
