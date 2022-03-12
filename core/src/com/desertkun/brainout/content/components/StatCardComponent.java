package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.StatCardComponent")
public class StatCardComponent extends CardComponent
{
    @Override
    public boolean applicable(UserProfile profile)
    {
        return true;
    }

    @Override
    public void write(Json json)
    {

    }
}
