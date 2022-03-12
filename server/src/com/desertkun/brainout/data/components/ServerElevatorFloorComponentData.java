package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.components.ServerElevatorFloorComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.*;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.inspection.*;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerElevatorFloorComponent")
@ReflectAlias("data.components.ServerElevatorFloorComponentData")
public class ServerElevatorFloorComponentData extends Component<ServerElevatorFloorComponent>
    implements Json.Serializable
{
    @InspectableProperty(name = "button-sprite", kind = PropertyKind.string, value = PropertyValue.vString)
    public String buttonSprite;

    @InspectableProperty(name = "elevator", kind = PropertyKind.string, value = PropertyValue.vString)
    public String elevator;

    @InspectableProperty(name = "door-portal", kind = PropertyKind.string, value = PropertyValue.vString)
    public String doorPortal;

    @InspectableProperty(name = "door-animation", kind = PropertyKind.string, value = PropertyValue.vString)
    public String doorAnimation;

    public ServerElevatorFloorComponentData(ComponentObject componentObject,
                                            ServerElevatorFloorComponent contentComponent)
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
        switch (event.getID())
        {
            case activeActivateData:
            {
                ActivateActiveEvent ev = ((ActivateActiveEvent) event);
                call(ev.client, ev.playerData);
                return true;
            }
        }

        return false;
    }

    private void call(Client client, PlayerData playerData)
    {
        ServerElevatorComponentData elevator = getElevator();

        if (elevator == null)
            return;

        ActiveData button = ((ActiveData) getComponentObject());

        if (button != null)
        {
            BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
                button.getDimension(), button.getX(), button.getY(), getContentComponent().getPushEffect()
            ));
        }

        if (elevator.call(this))
        {
            updateButton(true);
        }
    }

    private ActiveData findActiveData(String tag)
    {
        for (Map map : Map.All())
        {
            ActiveData found = map.getActiveNameIndex().get(tag);

            if (found != null)
                return found;
        }

        return null;
    }

    public ServerElevatorComponentData getElevator()
    {
        ActiveData activeData = findActiveData(elevator);

        if (activeData == null)
            return null;

        return activeData.getComponent(ServerElevatorComponentData.class);
    }

    public AnimationData getDoorAnimation()
    {
        ActiveData activeData = findActiveData(doorAnimation);

        if (!(activeData instanceof AnimationData))
            return null;

        return ((AnimationData) activeData);
    }

    public void updateButton(boolean on)
    {
        SpriteData data = getButtonSprite();

        if (data == null)
            return;

        data.spriteName = on ? getContentComponent().getButtonOn() : getContentComponent().getButtonOff();
        data.updated();
    }

    public SpriteData getButtonSprite()
    {
        ActiveData activeData = findActiveData(buttonSprite);

        if (!(activeData instanceof SpriteData))
            return null;

        return ((SpriteData) activeData);
    }

    public PortalData getDoorPortal()
    {
        ActiveData activeData = findActiveData(doorPortal);

        if (!(activeData instanceof PortalData))
            return null;

        return ((PortalData) activeData);
    }

    @Override
    public void write(Json json)
    {
        if (elevator != null)
            json.writeValue("elevator", elevator);
        if (doorPortal != null)
            json.writeValue("door-portal", doorPortal);
        if (doorAnimation != null)
            json.writeValue("door-animation", doorAnimation);
        if (buttonSprite != null)
            json.writeValue("button-sprite", buttonSprite);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        elevator = jsonData.getString("elevator", "");
        doorAnimation = jsonData.getString("door-animation", "");
        doorPortal = jsonData.getString("door-portal", "");
        buttonSprite = jsonData.getString("button-sprite", "");
    }
}
