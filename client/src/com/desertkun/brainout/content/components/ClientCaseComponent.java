package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientCaseComponent")
public class ClientCaseComponent extends ContentComponent
{
    private Sound openEffect;

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
        this.openEffect = ((Sound) BrainOutClient.ContentMgr.get(jsonData.getString("openEffect")));
    }

    public Sound getOpenEffect()
    {
        return openEffect;
    }
}
