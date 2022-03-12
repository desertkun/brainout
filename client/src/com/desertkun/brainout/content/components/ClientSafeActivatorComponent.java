package com.desertkun.brainout.content.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientSafeActivatorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientSafeActivatorComponent")
public class ClientSafeActivatorComponent extends ClientActiveActivatorComponent
{
    private SoundEffect beep;
    private Array<String> digits;
    private String empty;

    public ClientSafeActivatorComponent()
    {
        digits = new Array<>();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ClientSafeActivatorComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("beep"))
        {
            beep = BrainOutClient.ContentMgr.get(jsonData.getString("beep"), SoundEffect.class);
        }
        empty = jsonData.getString("empty");

        if (jsonData.has("digits"))
        {
            for (JsonValue digit : jsonData.get("digits"))
            {
                this.digits.add(digit.asString());
            }
        }
    }

    public String getEmptyDigit()
    {
        return empty;
    }

    public Array<String> getDigits()
    {
        return digits;
    }

    public SoundEffect getBeep()
    {
        return beep;
    }
}
