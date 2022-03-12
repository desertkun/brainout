package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ServerElevatorFloorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.inspection.InspectableGetter;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerElevatorFloorComponent")
public class ServerElevatorFloorComponent extends ContentComponent
{
    private String buttonOn;
    private String buttonOff;
    private String pushEffect;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ServerElevatorFloorComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    public String getButtonOn()
    {
        return buttonOn;
    }

    public String getPushEffect()
    {
        return pushEffect;
    }

    public String getButtonOff()
    {
        return buttonOff;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        buttonOn = jsonData.getString("button-on");
        buttonOff = jsonData.getString("button-off");
        pushEffect = jsonData.getString("push-effect");
    }
}
