package com.desertkun.brainout.data.containers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.interfaces.*;

import java.util.Comparator;
import java.util.Random;

public class ActiveDataMap extends ObjectMap<Integer, ActiveData> implements RenderUpdatable,
    Comparator<Integer>
{
    private boolean updateDirty;

    public class RenderMap extends OrderedMap<Integer, ActiveData> implements Renderable
    {
        private final int layer;

        public RenderMap(int layer)
        {
            this.layer = layer;
        }

        @Override
        public void render(Batch batch, RenderContext context)
        {
            for (Entry<Integer, ActiveData> item : this)
            {
                item.value.render(batch, context);
            }

        }

        @Override
        public boolean hasRender()
        {
            return true;
        }

        @Override
        public int getZIndex()
        {
            return 0;
        }

        @Override
        public int getLayer()
        {
            return layer;
        }

        public void release()
        {
        }

        public void init()
        {
        }

    }

    private static Random idGen = new Random();

    private final int layersCount;
    private ObjectMap<Integer, Updatable> updateList;
    private ObjectMap<Integer, ObjectMap<Integer, ActiveData>> tagList;
    private Array<RenderMap> renderList;

    public ActiveDataMap(int layers, String dimension)
    {
        super();

        updateList = null;
        layersCount = layers;
        tagList = new ObjectMap<>();

        renderList = new Array<>(layersCount);
        for (int i = 0; i < layersCount; i++)
        {
            renderList.add(newRenderMap(i, dimension));
        }
    }

    public ObjectMap<Integer, ActiveData> getItemsForTag(int tag)
    {
        return getItemsForTag(tag, true);
    }

    private static ObjectMap<Integer, ActiveData> empty = new ObjectMap<>();

    public ObjectMap<Integer, ActiveData> getItemsForTag(int tag, boolean allocate)
    {
        ObjectMap<Integer, ActiveData> list = tagList.get(tag);

        if (list == null)
        {
            if (allocate)
            {
                list = new ObjectMap<>();
                tagList.put(tag, list);
            }
            else
            {
                return empty;
            }
        }

        return list;
    }

    public ObjectMap<Integer, ObjectMap<Integer, ActiveData>> getTagList()
    {
        return tagList;
    }

    private void addTagItem(int tag, Integer Integer, ActiveData item)
    {
        ObjectMap<Integer, ActiveData> list = getItemsForTag(tag);
        list.put(Integer, item);
    }

    private void removeTagItem(int tag, Integer Integer)
    {
        ObjectMap<Integer, ActiveData> list = getItemsForTag(tag);
        list.remove(Integer);
    }

    public RenderMap newRenderMap(int layer, String dimension)
    {
        return new RenderMap(layer);
    }

    public RenderMap getRenderLayer(int layer)
    {
        return renderList.get(Math.min(Math.max(layer, 0), layersCount - 1));
    }

    public void addItem(Integer id, ActiveData item)
    {
        if (item == null) return;

        put(id, item);

        if (item.hasRender())
        {
            getRenderLayer(item.getLayer()).put(id, item);

            sort(item.getLayer());
        }

        if (item.hasUpdate())
        {
            if (updateList == null)
            {
                updateList = new ObjectMap<>();
            }
            updateList.put(id, item);
        }

        int tags = item.getTags();

        for (int tag = 0; tag < WithTag.MAX_TAGS; ++tag)
        {
            if (((tags >> tag) & 1) == 1)
            {
                addTagItem(tag, id, item);
            }
        }
    }

    public void reattach(Integer id, int layer)
    {
        ActiveData item = get(id);

        getRenderLayer(item.getLayer()).remove(id);
        getRenderLayer(layer).put(id, item);
    }

    public void removeItem(Integer id)
    {
        ActiveData item = get(id);

        if (item == null) return;

        if (item.hasRender())
        {
            if (renderList != null)
            {
                OrderedMap<Integer, ActiveData> map = getRenderLayer(item.getLayer());

                map.remove(id);
            }

        }

        if (item.hasUpdate())
        {
            if (updateList != null)
            {
                updateList.remove(id);
            }
        }

        int tags = item.getTags();

        for (int tag = 0; tag < WithTag.MAX_TAGS; ++tag)
        {
            if (((tags >> tag) & 1) == 1)
            {
                removeTagItem(tag, id);
            }
        }

        remove(id);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (renderList != null)
            for (RenderMap map: renderList)
            {
                map.render(batch, context);
            }
    }

    @Override
    public void update(float dt)
    {
        if (updateList != null)
        {
            if (updateDirty)
            {
                Array<Integer> toRemove = new Array<>();

                for (Entry<Integer, Updatable> item: updateList)
                {
                    if (item.value == null)
                    {
                        toRemove.add(item.key);
                    }
                }

                for (Integer key : toRemove)
                {
                    updateList.remove(key);
                }

                updateDirty = false;
            }

            for (Entry<Integer, Updatable> item: updateList)
            {
                if (item.value == null)
                {
                    updateDirty = true;
                    continue;
                }
                item.value.update(dt);
            }
        }
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public int getZIndex()
    {
        return 0;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public int compare(Integer o1, Integer o2)
    {
        ActiveData a1 = get(o1);
        ActiveData a2 = get(o2);

        int i1 = a1 != null ? a1.getZIndex() : 0;
        int i2 = a2 != null ? a2.getZIndex() : 0;

        return (int)Math.signum(i1 - i2);
    }

    public void sort(int layer)
    {
        getRenderLayer(layer).orderedKeys().sort(this);
    }

    @Override
    public int getLayer()
    {
        return 0;
    }

    public void release()
    {
        for (ActiveData activeData : values())
        {
            activeData.release();
        }

        for (RenderMap layer : renderList)
        {
            layer.release();
        }
    }

    public void init()
    {
        for (ActiveData activeData : values())
        {
            activeData.init();
        }

        for (RenderMap layer : renderList)
        {
            layer.init();
        }
    }

    protected int generateId(int begin, int amount)
    {
        for (int i = 0; i < 1000; i++)
        {
            int check = idGen.nextInt(amount) + begin;
            if (!containsKey(check))
            {
                return check;
            }
        }

        return -1;
    }

    @Override
    public void clear()
    {
        super.clear();

        tagList.clear();

        if (updateList != null)
        {
            updateList.clear();
            updateList = null;
        }

        if (renderList != null)
        {
            renderList.clear();
        }
    }

    public int generateServerId()
    {
        return generateId(Constants.Core.SERVER_ACTIVE_START, Constants.Core.SERVER_ACTIVE_AMOUNT);
    }

    public int generateClientId()
    {
        return generateId(0, Constants.Core.SERVER_ACTIVE_START - 1);
    }
}
