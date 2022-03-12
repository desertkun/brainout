package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientCardGroupComponent")
public class ClientCardGroupComponent extends ContentComponent
{
    private Array<Sound> openEffect;
    private Sound flipEffect;
    private String titleStyle;

    public ClientCardGroupComponent()
    {
        this.openEffect = new Array<>();
    }

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
        if (jsonData.has("openEffect"))
        {
            JsonValue openEffectValue = jsonData.get("openEffect");

            if (openEffectValue.isString())
            {
                openEffect.add(((Sound) BrainOutClient.ContentMgr.get(openEffectValue.asString())));
            }
            else
            if (openEffectValue.isArray())
            {
                for (JsonValue value : openEffectValue)
                {
                    openEffect.add(((Sound) BrainOutClient.ContentMgr.get(value.asString())));
                }
            }
        }

        this.flipEffect = ((Sound) BrainOutClient.ContentMgr.get(jsonData.getString("flipEffect")));
        this.titleStyle = jsonData.getString("titleStyle");
    }

    public Array<Sound> getOpenEffect()
    {
        return openEffect;
    }

    public Sound getFlipEffect()
    {
        return flipEffect;
    }

    public String getTitleStyle()
    {
        return titleStyle;
    }
}
