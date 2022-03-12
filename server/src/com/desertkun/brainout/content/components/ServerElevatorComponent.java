package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.data.components.ServerElevatorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerElevatorComponent")
public class ServerElevatorComponent extends ContentComponent
{
    private String moveEffect;
    private String closedEffect;
    private String failEffect;
    private String startupEffect;
    private String arrivedEffect;
    private float moveTime;
    private float openTime;

    private ConsumableContent itemsRequiredToWork;
    private int itemsRequiredToWorkAmount;
    private String itemHolder;

    public ServerElevatorComponent()
    {
        itemsRequiredToWorkAmount = 1;
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ServerElevatorComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        moveEffect = jsonData.getString("move-effect");
        failEffect = jsonData.getString("fail-effect");
        arrivedEffect = jsonData.getString("arrived-effect");
        closedEffect = jsonData.getString("closed-effect");
        startupEffect = jsonData.getString("startup-effect");
        moveTime = jsonData.getFloat("move-time");
        openTime = jsonData.getFloat("open-time");

        if (jsonData.has("items-required"))
        {
            itemsRequiredToWork = BrainOutServer.ContentMgr.get(
                jsonData.getString("items-required"), ConsumableContent.class);
        }

        if (jsonData.has("items-required-amount"))
        {
            itemsRequiredToWorkAmount = jsonData.getInt("items-required-amount");
        }
    }

    public ConsumableContent getItemsRequiredToWork()
    {
        return itemsRequiredToWork;
    }

    public int getItemsRequiredToWorkAmount()
    {
        return itemsRequiredToWorkAmount;
    }

    public float getMoveTime()
    {
        return moveTime;
    }

    public String getFailEffect()
    {
        return failEffect;
    }

    public String getArrivedEffect()
    {
        return arrivedEffect;
    }

    public String getClosedEffect()
    {
        return closedEffect;
    }

    public String getMoveEffect()
    {
        return moveEffect;
    }

    public String getStartupEffect()
    {
        return startupEffect;
    }

    public float getOpenTime()
    {
        return openTime;
    }
}
