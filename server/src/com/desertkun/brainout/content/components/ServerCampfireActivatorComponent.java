package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.CustomPlayerAnimationMsg;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerCampfireActivatorComponent")
public class ServerCampfireActivatorComponent extends ContentComponent
{
    private String effect;
    private String successEffect;
    private String animation;
    private String active;
    private float time;

    public ServerCampfireActivatorComponent()
    {
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ServerCampfireActivatorComponentData(componentObject, this);
    }


    @Override
    public void write(Json json)
    {

    }

    public String getAnimation()
    {
        return animation;
    }

    public String getEffect()
    {
        return effect;
    }

    public String getSuccessEffect()
    {
        return successEffect;
    }

    public float getTime()
    {
        return time;
    }

    public String getActive()
    {
        return active;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.effect = jsonData.getString("effect");
        this.successEffect = jsonData.getString("success-effect");
        this.active = jsonData.getString("active");
        this.animation = jsonData.getString("animation");
        this.time = jsonData.getFloat("time");
    }
}
