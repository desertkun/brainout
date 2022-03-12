package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.online.UserProfile;

public class ItemsFilters implements Json.Serializable
{
    protected ObjectMap<String, ItemFilter> filters;

    public ItemsFilters()
    {
        filters = new ObjectMap<>();
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        filters.clear();

        for (JsonValue filter : jsonData)
        {
            ItemFilter f = new ItemFilter();

            f.func = filter.getString("func", ">");
            f.value = filter.getInt("value", 0);

            this.filters.put(filter.name(), f);
        }
    }

    private static class ItemFilter
    {
        public String func;
        public int value;
    }

    private boolean checkFilter(UserProfile profile, String stat, ItemFilter filter)
    {
        float targetValue = filter.value;
        int sourceValue = profile.getItems().get(stat, 0);

        switch (filter.func)
        {
            case "=":
                return sourceValue == targetValue;
            case ">":
                return sourceValue > targetValue;
            case "<":
                return sourceValue < targetValue;
            case ">=":
                return sourceValue >= targetValue;
            case "<=":
                return sourceValue <= targetValue;
            case "!=":
                return sourceValue != targetValue;
        }

        return true;
    }

    public boolean checkFilters(UserProfile profile)
    {
        for (ObjectMap.Entry<String, ItemFilter> entry : filters)
        {
            if (!checkFilter(profile, entry.key, entry.value))
            {
                return false;
            }
        }

        return true;
    }

}
