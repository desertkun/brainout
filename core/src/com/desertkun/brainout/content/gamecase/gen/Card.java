package com.desertkun.brainout.content.gamecase.gen;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.gamecase.CardGroup;
import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.utils.WeightedRandom;

public abstract class Card extends Content implements WeightedRandom.WithWeight
{
    private CardGroup group;
    private float weight;
    private String shareWeight;

    public abstract CardData getCard(CaseData caseData, String dimension);

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        String group = jsonData.getString("group");

        this.group = ((CardGroup) BrainOut.ContentMgr.get(group));
        this.weight = jsonData.getFloat("weight");
        this.shareWeight = jsonData.getString("weight", null);
    }

    public CardGroup getGroup()
    {
        return group;
    }

    @Override
    public float getWeight()
    {
        return weight;
    }

    public String getShareWeight()
    {
        return shareWeight;
    }
}
