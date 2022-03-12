package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.WeightedRandom;

@Reflect("content.components.QualityComponent")
public class QualityComponent extends ContentComponent
{
    private Array<Probability> probabilities;

    public static class Probability implements WeightedRandom.WithWeight
    {
        public int from;
        public int to;
        public int weight;

        @Override
        public float getWeight()
        {
            return weight;
        }

        @Override
        public String getShareWeight()
        {
            return null;
        }
    }

    public QualityComponent()
    {
        probabilities = new Array<>();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    public int pick()
    {
        Probability prob = WeightedRandom.random(Probability.class, getProbabilities());
        if (prob == null)
        {
            return -1;
        }

        return MathUtils.random(prob.from, prob.to);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        for (JsonValue value : jsonData.get("probabilities"))
        {
            Probability probability = new Probability();
            probability.from = value.getInt("from");
            probability.to = value.getInt("to");
            probability.weight = value.getInt("weight");

            this.probabilities.add(probability);
        }
    }

    public Array<Probability> getProbabilities()
    {
        return probabilities;
    }
}
