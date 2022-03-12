package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.components.ContentCardComponent;
import com.desertkun.brainout.online.UserProfile;

public class StatsFilters implements Json.Serializable
{
    protected ObjectMap<String, StatsFilter> filters;

    public StatsFilters()
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
            StatsFilter f = new StatsFilter();

            f.func = filter.getString("func", ">");
            f.value = filter.getFloat("value", 0);

            this.filters.put(filter.name(), f);
        }
    }

    private static class StatsFilter
    {
        public String func;
        public float value;
    }

    private boolean checkFilter(UserProfile profile, String stat, StatsFilter filter)
    {
        float targetValue = filter.value;
        float sourceValue = profile.getStats().get(stat, 0.0f);

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
        for (ObjectMap.Entry<String, StatsFilter> entry : filters)
        {
            if (!checkFilter(profile, entry.key, entry.value))
            {
                return false;
            }
        }

        return true;
    }

}
