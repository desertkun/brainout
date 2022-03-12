package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientActiveActivatorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;

@Reflect("content.components.ClientActiveActivatorComponent")
public class ClientActiveActivatorComponent extends ContentComponent
{
    private LocalizedString activateText;

    public ClientActiveActivatorComponent()
    {
        this.activateText = new LocalizedString();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ClientActiveActivatorComponentData<>((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("activateText"))
        {
            this.activateText.set(jsonData.getString("activateText"));
        }
    }

    public LocalizedString getActivateText()
    {
        return activateText;
    }
}
