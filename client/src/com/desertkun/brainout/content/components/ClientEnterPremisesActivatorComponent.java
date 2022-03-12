package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.EnterPremisesDoorData;
import com.desertkun.brainout.data.components.ClientEnterPremisesActivatorComponentData;
import com.desertkun.brainout.data.components.ClientMenuActivatorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.components.ClientEnterPremisesActivatorComponent")
public class ClientEnterPremisesActivatorComponent extends ClientActiveActivatorComponent
{
    private SoundEffect beep;
    private Array<String> digits;
    private String empty;
    private String activateEffect;
    private String deniedEffect;

    public ClientEnterPremisesActivatorComponent()
    {
        digits = new Array<>();
        activateEffect = "";
        deniedEffect = "";
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ClientEnterPremisesActivatorComponentData((EnterPremisesDoorData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

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

        this.activateEffect = jsonData.getString("activateEffect", "");
        this.deniedEffect = jsonData.getString("deniedEffect", "");

    }

    public String getActivateEffect()
    {
        return activateEffect;
    }

    public String getDeniedEffect()
    {
        return deniedEffect;
    }

}
