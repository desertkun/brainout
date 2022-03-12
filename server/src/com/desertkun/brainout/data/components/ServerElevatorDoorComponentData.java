package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.components.ServerElevatorDoorComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerElevatorDoorComponent")
@ReflectAlias("data.components.ServerElevatorDoorComponentData")
public class ServerElevatorDoorComponentData extends Component<ServerElevatorDoorComponent>
    implements Json.Serializable
{
    @InspectableProperty(name = "elevator", kind = PropertyKind.string, value = PropertyValue.vString)
    public String elevator;

    public ServerElevatorDoorComponentData(ComponentObject componentObject,
                                           ServerElevatorDoorComponent contentComponent)
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
                PlayerData playerData = ev.playerData;
                Client client = ev.client;
                BrainOutServer.PostRunnable(() -> enter(client, playerData));

                return true;
            }
        }

        return false;
    }

    private void enter(Client client, PlayerData playerData)
    {
        ServerElevatorComponentData elevator = getElevator();

        if (elevator == null)
            return;

        elevator.enter(client, playerData);
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

    @Override
    public void write(Json json)
    {
        if (elevator != null)
            json.writeValue("elevator", elevator);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        elevator = jsonData.getString("elevator", "");
    }
}
