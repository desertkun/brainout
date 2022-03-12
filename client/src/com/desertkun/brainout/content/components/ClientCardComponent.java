package com.desertkun.brainout.content.components;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.gamecase.gen.Card;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.gamecase.gen.CardData;

public abstract class ClientCardComponent extends ContentComponent
{
    private Sound flipEffect;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("flipEffect"))
        {
            flipEffect = ((Sound) BrainOut.ContentMgr.get(jsonData.getString("flipEffect")));
        }
    }

    public abstract void drawIcon(Table renderTo, CardData cardData);
    public abstract void drawIcon(Table renderTo, Card card);

    public void flip(CardData cardData, Group cardContainer)
    {
        if (flipEffect != null)
        {
            flipEffect.play();
        }
    }

    public String getTitle(CardData cardData)
    {
        return L.get("CARD_TITLE_NEW");
    }

    public String getGroupTitle(CardData cardData)
    {
        return cardData.getCard().getGroup().getTitle().get();
    }

    public String getDescription(CardData cardData)
    {
        return cardData.getCard().getTitle().get();
    }
}
