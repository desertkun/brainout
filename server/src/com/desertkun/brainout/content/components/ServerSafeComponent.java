package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.ServerPortalComponentData;
import com.desertkun.brainout.data.components.ServerSafeComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerSafeComponent")
public class ServerSafeComponent extends ContentComponent
{
    private String activateEffect;
    private String deniedEffect;

    @Override
    public ServerSafeComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerSafeComponentData((PortalData)componentObject, this);
    }

    public ServerSafeComponent()
    {
        activateEffect = "";
        deniedEffect = "";
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
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
