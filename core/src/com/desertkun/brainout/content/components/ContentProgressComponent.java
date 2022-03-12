package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;

@Reflect("content.components.ContentProgressComponent")
public class ContentProgressComponent extends ContentComponent
{
    private String stat;
    private LocalizedString goal;
    private int value;
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
    public void read(Json json, JsonValue jsonValue)
    {
        stat = jsonValue.getString("stat");
        value = jsonValue.getInt("value");
        goal = new LocalizedString(jsonValue.getString("goal"));
    }

    public LocalizedString getGoal()
    {
        return goal;
    }

    public String getStat()
    {
        return stat;
    }

    public int getValue()
    {
        return value;
    }

    public boolean isComplete(UserProfile userProfile)
    {
        return userProfile.getStats().get(getStat(), 0.0f) >= getValue();
    }
}
