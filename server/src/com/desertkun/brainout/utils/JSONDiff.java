package com.desertkun.brainout.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONDiff
{
    private static Object Diff(Object old, Object value)
    {
        if (value instanceof Integer && old instanceof Number)
        {
            int currentInt = (Integer) value;
            int oldInt = ((Number) old).intValue();

            if (currentInt == oldInt)
                return null;

            int diff = currentInt - oldInt;

            if (diff > 0)
            {
                JSONObject result = new JSONObject();
                result.put("@func", "++");
                result.put("@value", diff);
                return result;
            }
            else
            {
                JSONObject result = new JSONObject();
                result.put("@func", "--");
                result.put("@value", -diff);
                return result;
            }
        }

        if (value instanceof Double && old instanceof Number)
        {
            double currentDouble = (Double) value;
            double oldDouble = ((Number) old).doubleValue();

            if (currentDouble == oldDouble)
                return null;

            double diff = currentDouble - oldDouble;

            if (diff > 0)
            {
                JSONObject result = new JSONObject();
                result.put("@func", "++");
                result.put("@value", diff);
                return result;
            }
            else
            {
                JSONObject result = new JSONObject();
                result.put("@func", "--");
                result.put("@value", -diff);
                return result;
            }
        }

        if (value instanceof Float && old instanceof Number)
        {
            float currentFloat = (Float) value;
            float oldFloat = ((Number) old).floatValue();

            if (currentFloat == oldFloat)
                return null;

            float diff = currentFloat - oldFloat;

            if (diff > 0)
            {
                JSONObject result = new JSONObject();
                result.put("@func", "++");
                result.put("@value", diff);
                return result;
            }
            else
            {
                JSONObject result = new JSONObject();
                result.put("@func", "--");
                result.put("@value", -diff);
                return result;
            }
        }

        if (value instanceof String)
        {
            if (value.equals(old))
                return null;

            return value;
        }

        if (value instanceof JSONArray)
        {
            if (old instanceof JSONArray)
            {
                JSONArray valueAsArray = ((JSONArray) value);
                JSONArray oldAsArray = ((JSONArray) old);

                if (valueAsArray.length() == oldAsArray.length())
                {
                    boolean same = true;

                    for (int i = 0, t = valueAsArray.length(); i < t; i++)
                    {
                        if (!valueAsArray.get(i).equals(oldAsArray.get(i)))
                        {
                            same = false;
                            break;
                        }
                    }

                    if (same)
                    {
                        return null;
                    }
                }
            }

            return value;
        }

        if (value instanceof JSONObject && old instanceof JSONObject)
        {
            JSONObject current = ((JSONObject) value);
            JSONObject previous = ((JSONObject) old);

            JSONObject result = null;

            for (String key : current.keySet())
            {
                if (previous.has(key))
                {
                    Object compared = Diff(previous.get(key), current.get(key));

                    if (compared != null)
                    {
                        if (result == null)
                            result = new JSONObject();

                        result.put(key, compared);
                    }
                }
                else
                {
                    if (result == null)
                        result = new JSONObject();

                    result.put(key, current.get(key));
                }
            }

            return result;
        }

        return value;
    }

    public static JSONObject Diff(JSONObject previous, JSONObject current)
    {
        return ((JSONObject) Diff((Object)previous, (Object)current));
    }
}
