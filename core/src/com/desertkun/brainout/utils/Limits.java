package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.online.UserProfile;

public class Limits implements Json.Serializable
{
    public static class Limit
    {
        public String key;
        public String func;
        public int value;
    }

    private Array<Limit> statsLimits;
    private Array<Limit> itemLimits;

    public Limits()
    {
        this.statsLimits = new Array<>();
        this.itemLimits = new Array<>();
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("limit-stats"))
        {
            JsonValue stats = jsonData.get("limit-stats");

            for (JsonValue limit : stats)
            {
                Limit newLimit = new Limit();
                newLimit.key = limit.getString("id");
                newLimit.func = limit.getString("func");
                newLimit.value = limit.getInt("value", 0);

                this.statsLimits.add(newLimit);
            }
        }

        if (jsonData.has("limit-items"))
        {
            JsonValue items = jsonData.get("limit-items");

            for (JsonValue limit : items)
            {
                Limit newLimit = new Limit();
                newLimit.key = limit.getString("id");
                newLimit.func = limit.getString("func");
                newLimit.value = limit.getInt("value", 0);

                this.itemLimits.add(newLimit);
            }
        }
    }

    public boolean passes(UserProfile userProfile)
    {
        for (Limit limit : statsLimits)
        {
            float statValue = userProfile.getStats().get(limit.key, 0.0f);

            if (!checkLimit(limit.func, statValue, limit.value))
            {
                return false;
            }
        }

        for (Limit limit : itemLimits)
        {
            int itemValue = userProfile.getItems().get(limit.key, 0);

            if (!checkLimit(limit.func, itemValue, limit.value))
            {
                return false;
            }
        }

        return true;
    }

    private boolean checkLimit(String func, float a, float b)
    {
        switch (func)
        {
            case "=":
                return a == b;
            case ">":
                return a > b;
            case "<":
                return a < b;
            case ">=":
                return a >= b;
            case "<=":
                return a <= b;
            case "!=":
                return a != b;
            default:
                return false;
        }
    }
}
