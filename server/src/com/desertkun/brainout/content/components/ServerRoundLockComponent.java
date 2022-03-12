package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.RoundLockSafeData;
import com.desertkun.brainout.data.components.ServerRoundLockComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerRoundLockComponent")
public class ServerRoundLockComponent extends ContentComponent
{
    private String activateEffect;
    private String safeActivatedTexture;
    private String safeClosedTexture;

    @Override
    public ServerRoundLockComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerRoundLockComponentData((RoundLockSafeData)componentObject, this);
    }

    public ServerRoundLockComponent()
    {
        activateEffect = "";
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.activateEffect = jsonData.getString("activateEffect", "");
        this.safeActivatedTexture = jsonData.getString("safeActivatedTexture", "");
        this.safeClosedTexture = jsonData.getString("safeClosedTexture", "");
    }

    public String getSafeActivatedTexture()
    {
        return safeActivatedTexture;
    }

    public String getSafeClosedTexture()
    {
        return safeClosedTexture;
    }

    public String getActivateEffect()
    {
        return activateEffect;
    }

}
