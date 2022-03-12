package com.desertkun.brainout.content.components;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.gamecase.gen.Card;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.data.gamecase.gen.ContentCardData;
import com.desertkun.brainout.data.gamecase.gen.StatCardData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientStatCardComponent")
public class ClientStatCardComponent extends ClientCardComponent
{
    private String icon;

    @Override
    public void drawIcon(Table renderTo, CardData cardData)
    {
        TextureAtlas.AtlasRegion region = BrainOutClient.getRegion(icon);

        if (region == null)
            return;

        Image iconImage = new Image(region);
        iconImage.setScaling(Scaling.none);

        renderTo.add(iconImage).expand().fill().row();
    }

    @Override
    public void drawIcon(Table renderTo, Card card)
    {
        TextureAtlas.AtlasRegion region = BrainOutClient.getRegion(icon);

        if (region == null)
            return;

        Image iconImage = new Image(region);
        iconImage.setScaling(Scaling.none);
        iconImage.setScale(0.5f);
        iconImage.setOrigin(16, 16);

        renderTo.add(iconImage).expand().size(32, 32).row();
    }

    @Override
    public String getTitle(CardData cardData)
    {
        int amount = ((StatCardData) cardData).getAmount();

        return String.valueOf(amount);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.icon = jsonData.getString("icon");
    }
}
