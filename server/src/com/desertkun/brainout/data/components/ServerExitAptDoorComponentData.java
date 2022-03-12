package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.FreeDimensionMsg;
import com.desertkun.brainout.content.components.ServerFreeplayExitAptDoorComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerFreeplayMap;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RealEstateInfo;
import com.esotericsoftware.minlog.Log;

@Reflect("ServerExitAptDoorComponentData")
@ReflectAlias("data.components.ServerExitAptDoorComponentData")
public class ServerExitAptDoorComponentData extends Component<ServerFreeplayExitAptDoorComponent>
{
    private ActiveData enterDoor;

    public ServerExitAptDoorComponentData(ComponentObject componentObject,
                                          ServerFreeplayExitAptDoorComponent contentComponent)
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

    public ActiveData getEnterDoor()
    {
        return enterDoor;
    }

    public void setEnterDoor(ActiveData enterDoor)
    {
        this.enterDoor = enterDoor;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeActivateData:
            {
                ActivateActiveEvent ev = ((ActivateActiveEvent) event);
                enter(ev.client, ev.playerData);
                break;
            }
        }

        return false;
    }

    private boolean isAllowedToLeave(PlayerData playerData)
    {
        return true;
    }

    private void enter(Client client, PlayerData playerData)
    {
        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return;

        if (!isAllowedToLeave(playerData))
            return;

        PlayState playState = BrainOutServer.Controller.getPlayState();

        if (playState.getID() != PlayState.ID.game)
            return;

        GameMode mode = ((ServerPSGame) playState).getMode();

        if (mode.getID() != GameMode.ID.free)
            return;

        ServerFreeRealization free = ((ServerFreeRealization) mode.getRealization());
        String oldDimension = playerData.getDimension();

        playerClient.enablePlayer(false);

        progress.startCancellable(1.0f, () ->
        {
            free.playerExit(enterDoor, playerData, playerClient);
            freePersonalMaps(oldDimension, playerClient);
            playerClient.enablePlayer(true);
        }, () ->
        {
            playerClient.enablePlayer(true);
        });

    }

    private void freePersonalMaps(String exitedFromDimension, PlayerClient playerClient)
    {
        ServerFreeplayMap oldMap = ServerFreeplayMap.Get(exitedFromDimension, ServerFreeplayMap.class);
        if (oldMap == null)
        {
            return;
        }

        RealEstateInfo rsInfo = oldMap.getRealEstateInfo();
        if (rsInfo == null)
        {
            return;
        }

        Array<ServerFreeplayMap> mapsOfInterest = new Array<>();
        // cleanup private items
        Array<ConsumableRecord> toRemove = new Array<>();

        for (ServerMap map : Map.All(ServerMap.class))
        {
            if (!(map instanceof ServerFreeplayMap))
                continue;

            ServerFreeplayMap f = ((ServerFreeplayMap) map);
            if (f.getRealEstateInfo() != rsInfo)
                continue;

            mapsOfInterest.add(f);
        }

        for (ServerFreeplayMap map : mapsOfInterest)
        {
            if (map.countActivesForTag(Constants.ActiveTags.PLAYERS) > 0)
            {
                // someone's here, abort
                return;
            }
        }

        playerClient.log("Disposing real estate dimension " + exitedFromDimension);

        for (ServerFreeplayMap map : mapsOfInterest)
        {
            ModePayload p = playerClient.getModePayload();
            if (p instanceof FreePayload)
            {
                ObjectMap<String, Map> personalMaps = ((FreePayload) p).getPersonalMaps();
                if (personalMaps != null)
                {
                    personalMaps.remove(map.getDimension());
                }
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ConsumableContainer records = ((ItemData) activeData).getRecords();
                for (ObjectMap.Entry<Integer, ConsumableRecord> entry : records.getData())
                {
                    ConsumableItem item = entry.value.getItem();
                    if (item.getPrivate() == playerClient.getId())
                    {
                        toRemove.add(entry.value);
                    }
                }

                if (toRemove.size > 0)
                {
                    for (ConsumableRecord item : toRemove)
                    {
                        records.removeRecord(item);
                    }

                    toRemove.clear();
                }
            }
        }

        PlayState ps = BrainOutServer.Controller.getPlayState();

        for (ServerMap map : mapsOfInterest)
        {
            if (Log.INFO) Log.info("Unloading personal map: " + map.getDimension());

            BrainOutServer.Controller.getClients().sendTCP(new FreeDimensionMsg(map.getDimension()));
            map.dispose();

            if (ps instanceof ServerPSGame)
            {
                ((ServerPSGame) ps).getMaps().removeValue(map, true);
            }
        }
    }
}
