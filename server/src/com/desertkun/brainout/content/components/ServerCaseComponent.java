package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.gamecase.CardGroup;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.content.gamecase.gen.Card;
import com.desertkun.brainout.data.components.ServerCaseComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.online.UserProfile;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerCaseComponent")
public class ServerCaseComponent extends ContentComponent
{
    @Override
    public ServerCaseComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerCaseComponentData((CaseData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {

    }
}
