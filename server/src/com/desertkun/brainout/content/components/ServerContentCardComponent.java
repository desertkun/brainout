package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.data.gamecase.gen.ContentCardData;
import com.desertkun.brainout.data.gamecase.gen.StatCardData;
import com.desertkun.brainout.online.UserProfile;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerContentCardComponent")
public class ServerContentCardComponent extends ServerCardComponent
{

    public ServerContentCardComponent()
    {
    }

    @Override
    public void generate(CardData cardData)
    {
        if (!(cardData instanceof ContentCardData))
            return;

        ContentCardData contentCardData = ((ContentCardData) cardData);

        ContentCardComponent cmp = getContent().getComponentFrom(ContentCardComponent.class);

        if (cmp == null)
            return;

        contentCardData.setCardContent(cmp.getOwnableContent());
        contentCardData.setAmount(cmp.getAmount());
    }

    @Override
    public void apply(PlayerClient client, UserProfile profile, CardData cardData)
    {
        if (!(cardData instanceof ContentCardData))
            return;

        ContentCardData contentCardData = ((ContentCardData) cardData);

        if (profile != null)
        {
            profile.getBadges().add(contentCardData.getCardContent().getID());
            contentCardData.getCardContent().addItem(profile, contentCardData.getAmount());

            client.resourceEvent(1,
                "card",
                cardData.getCaseData().getCase().getID(),
                ((ContentCardData) cardData).getCard().getID());

            client.resourceEvent(contentCardData.getAmount(),
                "item",
                "case-card",
                contentCardData.getCardContent().getID());

            client.log("Unlocked from card: " + contentCardData.getCardContent().getID() +
                " Amount: " + contentCardData.getAmount());
        }
    }
}
