package com.desertkun.brainout.content.active;


import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.active.ItemData;

import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.Item")
public class Item extends Active
{
    private boolean autoRemove;
    private boolean editor;
    private boolean autoSerialize;
    private ObjectMap<String, ItemFilter> filters;

    public class ItemFilter implements Json.Serializable
    {
        private ObjectSet<ConsumableContent> items;
        private ConsumableContent fulfill;
        private int limit;

        public ItemFilter()
        {
            items = new ObjectSet<>();
            limit = 0;
        }

        public ObjectSet<ConsumableContent> getItems()
        {
            return items;
        }

        public int getLimit()
        {
            return limit;
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonValue)
        {
            limit = jsonValue.getInt("limit", 1);

            if (jsonValue.has("fulfill"))
            {
                fulfill = BrainOut.ContentMgr.get(jsonValue.getString("fulfill"), ConsumableContent.class);
            }

            for (JsonValue entry : jsonValue.get("items"))
            {
                ConsumableContent f = BrainOut.ContentMgr.get(entry.asString(), ConsumableContent.class);

                if (f != null)
                    items.add(f);
            }
        }

        public boolean check(ConsumableContainer to, Content check)
        {
            if (!matches(check))
                return false;

            int have = 0;

            for (ConsumableContent cc : items)
            {
                have += to.getAmount(cc);
            }

            return have < limit;
        }

        public boolean matches(Content content)
        {
            if (!(content instanceof ConsumableContent))
                return false;

            return items.contains(((ConsumableContent) content));
        }

        public boolean fulfilled(ConsumableContainer to)
        {
            return to.getAmount(fulfill) == limit;
        }
    }

    @Override
    public ItemData getData(String dimension)
    {
        return new ItemData(this, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        autoRemove = jsonData.getBoolean("auto-remove", false);
        editor = jsonData.getBoolean("editor", false);
        autoSerialize = jsonData.getBoolean("auto-serialize", true);

        if (jsonData.has("filter"))
        {
            filters = new ObjectMap<>();

            for (JsonValue value : jsonData.get("filter"))
            {
                ItemFilter filter = new ItemFilter();
                filter.read(json, value);
                filters.put(value.name(), filter);
            }
        }

    }

    public ObjectMap<String, ItemFilter> getFilters()
    {
        return filters;
    }

    public boolean fulfilled(ConsumableContainer to)
    {
        if (filters == null)
            return true;

        for (ObjectMap.Entry<String, ItemFilter> entry : filters)
        {
            if (!entry.value.fulfilled(to))
                return false;
        }

        return true;
    }

    public boolean checkFilter(ConsumableContainer to, Content cc)
    {
        if (filters == null)
            return true;

        for (ObjectMap.Entry<String, ItemFilter> entry : filters)
        {
            if (entry.value.check(to, cc))
                return true;
        }

        return false;
    }

    public boolean isAutoSerialize()
    {
        return autoSerialize;
    }

    @Override
    public boolean isEditorSelectable()
    {
        return editor;
    }

    public boolean isAutoRemove()
    {
        return autoRemove;
    }
}
