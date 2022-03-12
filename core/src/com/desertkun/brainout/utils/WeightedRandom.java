package com.desertkun.brainout.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

public class WeightedRandom
{
    public interface WithWeight
    {
        float getWeight();
        String getShareWeight();
    }

    public static <T extends WithWeight> T random(Class<T> classOf, Array<T> from)
    {
        float weight = 0;

        ObjectMap<String, Float> sharedWeights = new ObjectMap<>();
        ObjectMap<String, Integer> sharedWeightsCount = new ObjectMap<>();

        for (T item : from)
        {
            if (item.getShareWeight() != null)
            {
                if (sharedWeights.containsKey(item.getShareWeight()))
                {
                    float share = sharedWeights.get(item.getShareWeight(), 0.f);
                    int cnt = sharedWeightsCount.get(item.getShareWeight(), 1);

                    sharedWeights.put(item.getShareWeight(), share + item.getWeight());
                    sharedWeightsCount.put(item.getShareWeight(), cnt + 1);
                }
                else
                {
                    sharedWeights.put(item.getShareWeight(), item.getWeight());
                    sharedWeightsCount.put(item.getShareWeight(), 1);
                }
            }
        }

        for (ObjectMap.Entry<String, Float> entry : sharedWeights)
        {
            entry.value /= sharedWeightsCount.get(entry.key, 1);
        }

        for (T item : from)
        {
            if (item.getShareWeight() != null)
            {
                weight += sharedWeights.get(item.getShareWeight(), 0.0f);
            }
            else
            {
                weight += item.getWeight();
            }
        }

        float randomValue = MathUtils.random(weight);

        weight = 0;

        for (T item : from)
        {
            if (item.getShareWeight() != null)
            {
                weight += sharedWeights.get(item.getShareWeight(), 0.0f);
            }
            else
            {
                weight += item.getWeight();
            }

            if (randomValue < weight)
            {
                return item;
            }
        }

        if (from.size == 0)
            return null;

        return from.get(from.size - 1);
    }
}
