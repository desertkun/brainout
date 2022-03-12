package com.desertkun.brainout.content.gamecase;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.InventoryContent;
import com.desertkun.brainout.content.components.CardComponent;
import com.desertkun.brainout.content.gamecase.gen.Card;
import com.desertkun.brainout.data.gamecase.CaseData;

import com.desertkun.brainout.managers.ContentManager;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.function.Consumer;

@Reflect("content.gamecase.Case")
public class Case extends InventoryContent
{
    private Array<Card> cards;
    private boolean demand;

    public class Card implements Json.Serializable
    {
        private Array<CardGroup> groups = new Array<>();

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            if (jsonData.has("groups"))
            {
                JsonValue groups = jsonData.get("groups");

                if (groups.isString())
                {
                    CardGroup group = ((CardGroup) BrainOut.ContentMgr.get(groups.asString()));
                    this.groups.add(group);
                }
                else
                {
                    for (JsonValue value : groups)
                    {
                        CardGroup group = ((CardGroup) BrainOut.ContentMgr.get(value.asString()));
                        this.groups.add(group);
                    }
                }
            }
        }

        public Array<CardGroup> getGroups()
        {
            return groups;
        }
    }

    public Case()
    {
        this.cards = new Array<>();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        clear();

        if (jsonData.has("cards"))
        {
            for (JsonValue value : jsonData.get("cards"))
            {
                Card card = new Card();
                card.read(json, value);
                cards.add(card);
            }
        }

        demand = jsonData.getBoolean("demand", false);
    }

    private void clear()
    {
        cards.clear();
    }

    public Array<Card> getCards()
    {
        return cards;
    }

    public CaseData getData()
    {
        return new CaseData(this);
    }

    public boolean applicable(UserProfile profile)
    {
        boolean haveAtLeaseOne = false;

        for (Case.Card card : getCards())
        {
            boolean haveOne = false;

            // select the applicable generators of the groups in this cardData
            for (CardGroup group : card.getGroups())
            {
                com.desertkun.brainout.content.gamecase.gen.Card groupSelect =
                    BrainOut.ContentMgr.queryOneContentTpl(
                    com.desertkun.brainout.content.gamecase.gen.Card.class,
                    content ->
                {
                    if (!content.getGroup().equals(group))
                    {
                        return false;
                    }

                    CardComponent scc = content.getComponentFrom(CardComponent.class);

                    return scc != null && scc.applicable(profile);
                });

                if (groupSelect != null)
                {
                    haveAtLeaseOne = true;
                    haveOne = true;
                }
            }

            if (demand && !haveOne)
            {
                return false;
            }
        }

        return haveAtLeaseOne;
    }

    public boolean isDemand()
    {
        return demand;
    }
}
