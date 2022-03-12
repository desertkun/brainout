package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.common.msg.server.UpdateActiveAnimationMsg;
import com.desertkun.brainout.content.components.ServerElevatorComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.inspection.*;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.TimerTask;

@Reflect("ServerElevatorComponent")
@ReflectAlias("data.components.ServerElevatorComponentData")
public class ServerElevatorComponentData extends Component<ServerElevatorComponent>
    implements Json.Serializable
{
    private ServerElevatorFloorComponentData currentFloor;
    private State state;
    private TimerTask currentTask, checkStateTask;
    private ObjectSet<PlayerData> aboard;
    private boolean wasWorking;

    @InspectableProperty(name = "inner-portal", kind = PropertyKind.string, value = PropertyValue.vString)
    public String innerPortal;

    @InspectableProperty(name = "floor-a", kind = PropertyKind.string, value = PropertyValue.vString)
    public String floorA;

    @InspectableProperty(name = "floor-b", kind = PropertyKind.string, value = PropertyValue.vString)
    public String floorB;

    @InspectableProperty(name = "work-holder", kind = PropertyKind.string, value = PropertyValue.vString)
    public String workHolder;

    public enum State
    {
        running,
        opened,
        closed
    }

    public ServerElevatorComponentData(ComponentObject componentObject,
                                       ServerElevatorComponent contentComponent)
    {
        super(componentObject, contentComponent);

        currentFloor = null;
        state = State.closed;
        aboard = new ObjectSet<>();
        wasWorking = false;
    }

    @Override
    public void init()
    {
        super.init();

        checkStateTask = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() -> checkState());
            }
        };

        BrainOutServer.Timer.schedule(checkStateTask, 1000, 1000);
    }

    private void checkState()
    {
        boolean working = canWork();

        if (working != wasWorking)
        {
            if (working)
            {
                ActiveData a = getFloorA();
                ActiveData b = getFloorB();

                if (!getContentComponent().getStartupEffect().isEmpty())
                {
                    if (a != null)
                    {
                        BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
                            a.getDimension(), a.getX(), a.getY(), getContentComponent().getStartupEffect()
                        ));
                    }

                    if (b != null)
                    {
                        BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
                            b.getDimension(), b.getX(), b.getY(), getContentComponent().getStartupEffect()
                        ));
                    }
                }

            }

            wasWorking = working;
        }
    }

    @Override
    public void release()
    {
        super.release();

        if (currentTask != null)
        {
            currentTask.cancel();
            currentTask = null;
        }

        if (checkStateTask != null)
        {
            checkStateTask.cancel();
            checkStateTask = null;
        }
    }

    public ItemData getWorkHolder()
    {
        ActiveData activeData = findActiveData(workHolder);

        if (!(activeData instanceof ItemData))
            return null;

        return ((ItemData) activeData);
    }

    public boolean canWork()
    {
        if (getContentComponent().getItemsRequiredToWork() == null)
            return true;

        ItemData workHolder = getWorkHolder();

        if (workHolder == null)
            return true;

        int amount = workHolder.getRecords().getTotalAmount(getContentComponent().getItemsRequiredToWork());

        return amount >= getContentComponent().getItemsRequiredToWorkAmount();
    }

    public ActiveData getInnerPortal()
    {
        return findActiveData(innerPortal);
    }

    public boolean enter(Client client, PlayerData playerData)
    {
        if (state != State.opened)
            return false;

        ActiveData innerPortal = getInnerPortal();

        if (innerPortal == null)
            return false;

        if (!playerData.isAlive())
            return false;

        if (!(client instanceof PlayerClient))
            return false;

        PlayerClient playerClient = ((PlayerClient) client);

        playerClient.moveTo(innerPortal.getDimension(), innerPortal.getX(), innerPortal.getY());

        aboard.add(playerData);

        return true;
    }

    private void arrived(ServerElevatorFloorComponentData floor)
    {
        currentFloor = floor;

        if (aboard.size > 0)
        {
            unloadPlayers(floor);
            aboard.clear();
        }

        openDoor(floor);

        currentTask = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() ->
                {
                    currentTask = null;
                    closeDoor(floor);

                    if (aboard.size > 0)
                    {
                        moveToNextFloor();
                    }
                });
            }
        };

        long t = (long)(getContentComponent().getOpenTime() * 1000.0f);
        BrainOutServer.Timer.schedule(currentTask, t);
    }

    private void unloadPlayers(ServerElevatorFloorComponentData floor)
    {
        for (PlayerData playerData : aboard)
        {
            unloadPlayer(playerData, floor);
        }
    }

    private boolean unloadPlayer(PlayerData playerData, ServerElevatorFloorComponentData floor)
    {
        if (!playerData.isAlive())
            return false;

        int ownerId = playerData.getOwnerId();

        Client client = BrainOutServer.Controller.getClients().get(ownerId);

        if (!(client instanceof PlayerClient))
            return false;

        PortalData portalData = floor.getDoorPortal();

        if (portalData == null)
            return false;

        PlayerClient playerClient = ((PlayerClient) client);

        playerClient.moveTo(portalData.getDimension(), portalData.getX(), portalData.getY());

        return true;
    }

    private void moveToNextFloor()
    {
        if (state != State.closed)
            return;

        ServerElevatorFloorComponentData nextFloor = getNextFloor();

        if (nextFloor == null)
            return;

        ActiveData innerPortal = getInnerPortal();

        if (innerPortal == null)
            return;

        state = State.running;
        currentFloor = nextFloor;

        if (!getContentComponent().getMoveEffect().isEmpty())
        {
            BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
                innerPortal.getDimension(), innerPortal.getX(), innerPortal.getY(), getContentComponent().getMoveEffect()
            ));
        }

        currentTask = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() ->
                {
                    currentTask = null;
                    arrived(currentFloor);
                });
            }
        };

        long t = (long)(getContentComponent().getMoveTime() * 1000.0f);
        BrainOutServer.Timer.schedule(currentTask, t);
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

    private ActiveData getFloorA()
    {
        return findActiveData(floorA);
    }

    private ActiveData getFloorB()
    {
        return findActiveData(floorB);
    }

    private ServerElevatorFloorComponentData getNextFloor()
    {
        String floor = ((ActiveData) currentFloor.getComponentObject()).getNameId();

        if (floor == null)
            return null;

        Map map = getMap();

        if (map == null)
            return null;

        String nextFloor = floor.equals(floorA) ? floorB : floorA;

        ActiveData activeData = findActiveData(nextFloor);

        if (activeData == null)
            return null;

        return activeData.getComponent(ServerElevatorFloorComponentData.class);
    }

    private void move(ServerElevatorFloorComponentData floor)
    {
        if (state != State.closed)
            return;

        state = State.running;
        currentFloor = floor;

        currentTask = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() ->
                {
                    currentTask = null;
                    arrived(floor);
                });
            }
        };

        long t = (long)(getContentComponent().getMoveTime() * 1000.0f);
        BrainOutServer.Timer.schedule(currentTask, t);
    }

    public boolean call(ServerElevatorFloorComponentData floor)
    {
        if (state != State.closed)
        {
            fail(floor);
            return false;
        }

        if (!canWork())
        {
            fail(floor);
            return false;
        }

        if (currentFloor == floor)
        {
            arrived(floor);
            return false;
        }

        if (currentFloor != null)
        {
            move(floor);
            return true;
        }
        else
        {
            arrived(floor);
            return false;
        }
    }

    private void fail(ServerElevatorFloorComponentData floor)
    {
        ActiveData floorData = ((ActiveData) floor.getComponentObject());

        if (floorData != null)
        {
            BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
                floorData.getDimension(), floorData.getX(), floorData.getY(), getContentComponent().getFailEffect()
            ));
        }
    }

    private void updateDoorAnimation(ServerElevatorFloorComponentData floor, String animation)
    {
        if (floor.getDoorAnimation() == null)
            return;

        BrainOutServer.Controller.getClients().sendTCP(
            new UpdateActiveAnimationMsg(floor.getDoorAnimation(), new String[]{animation}, false));
    }

    private void closeDoor(ServerElevatorFloorComponentData floor)
    {
        PortalData portalData = floor.getDoorPortal();

        if (portalData != null)
        {
            BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
                portalData.getDimension(), portalData.getX(), portalData.getY(),
                    getContentComponent().getClosedEffect()
            ));
        }

        state = State.closed;
        updateDoorAnimation(floor, "close");
    }

    private void openDoor(ServerElevatorFloorComponentData floor)
    {
        floor.updateButton(false);

        PortalData portalData = floor.getDoorPortal();

        if (portalData != null)
        {
            BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
                portalData.getDimension(), portalData.getX(), portalData.getY(),
                    getContentComponent().getArrivedEffect()
            ));
        }

        state = State.opened;
        updateDoorAnimation(floor, "open");
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
        if (innerPortal != null)
            json.writeValue("inner-portal", innerPortal);
        if (floorA != null)
            json.writeValue("floor-a", floorA);
        if (floorB != null)
            json.writeValue("floor-b", floorB);
        if (workHolder != null)
            json.writeValue("work-holder", workHolder);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        innerPortal = jsonData.getString("inner-portal", "");
        floorA = jsonData.getString("floor-a", "");
        floorB = jsonData.getString("floor-b", "");
        workHolder = jsonData.getString("work-holder", "");
    }
}
