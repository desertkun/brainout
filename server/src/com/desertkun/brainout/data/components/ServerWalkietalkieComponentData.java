package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.components.ServerElevatorFloorComponent;
import com.desertkun.brainout.content.components.ServerWalkietalkieComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.*;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerWalkietalkieComponent")
@ReflectAlias("data.components.ServerWalkietalkieComponentData")
public class ServerWalkietalkieComponentData extends Component<ServerWalkietalkieComponent>
    implements Json.Serializable
{
    //private float frequency;

    public ServerWalkietalkieComponentData(ComponentObject componentObject,
                                           ServerWalkietalkieComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void write(Json json)
    {
        //json.writeValue("frequency", frequency);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        //frequency = jsonData.getFloat("frequency", getRandomFrequency());
    }
}
