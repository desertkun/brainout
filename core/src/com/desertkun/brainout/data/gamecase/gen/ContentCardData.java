package com.desertkun.brainout.data.gamecase.gen;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.gamecase.gen.ContentCard;

import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.gamecase.gen.ContentCardData")
public class ContentCardData extends CardData<ContentCard>
{
    private OwnableContent cardContent;
    private int amount;

    public ContentCardData(ContentCard card, CaseData caseData, String dimension)
    {
        super(card, caseData, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("cardContent"))
        {
            cardContent = ((OwnableContent) BrainOut.ContentMgr.get(jsonData.getString("cardContent")));
        }
        amount = jsonData.getInt("amount", 1);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        if (cardContent != null)
        {
            json.writeValue("cardContent", cardContent.getID());
        }

        json.writeValue("amount", 1);
    }

    public void setCardContent(OwnableContent content)
    {
        this.cardContent = content;
    }

    public OwnableContent getCardContent()
    {
        return cardContent;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }
}
