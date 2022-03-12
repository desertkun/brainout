package com.desertkun.brainout.data.gamecase;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.content.gamecase.gen.Card;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.gamecase.gen.CardData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.gamecase.CaseData")
public class CaseData extends Data
{
    private Array<CardResult> cards;

    public static class CardResult implements Json.Serializable
    {
        private final CaseData caseData;
        public CardData cardData;

        public CardResult(CaseData caseData)
        {
            this.caseData = caseData;
        }

        public CardResult(CardData of, CaseData caseData)
        {
            this.cardData = of;
            this.caseData = caseData;
        }

        @Override
        public void write(Json json)
        {
            json.writeValue("id", cardData.getContent().getID());
            json.writeValue("cardData", cardData);
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            String id = jsonData.getString("id");
            Card card = ((Card) BrainOut.ContentMgr.get(id));

            if (card != null)
            {
                cardData = card.getCard(caseData, caseData.getDimension());
                cardData.read(json, jsonData.get("cardData"));
            }
        }
    }

    public CaseData(Case gameCase)
    {
        super(gameCase, null);

        cards = new Array<>();
    }

    public Case getCase()
    {
        return ((Case) getContent());
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("cards"))
        {
            for (JsonValue value : jsonData.get("cards"))
            {
                CardResult cardResult = new CardResult(this);
                cardResult.read(json, value);

                this.cards.add(cardResult);
            }
        }
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeArrayStart("cards");

        for (CardResult item : cards)
        {
            json.writeValue(item);
        }

        json.writeArrayEnd();
    }

    public Array<CardResult> getCards()
    {
        return cards;
    }
}
