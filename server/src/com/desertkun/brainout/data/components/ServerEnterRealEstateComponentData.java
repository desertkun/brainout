package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.components.ServerFreeplayEnterRealEstateComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerFreeplayMap;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.*;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RealEstateInfo;
import org.json.JSONObject;

import java.io.StringWriter;

@Reflect("ServerEnterRealEstateComponentData")
@ReflectAlias("data.components.ServerEnterRealEstateComponentData")
public class ServerEnterRealEstateComponentData extends Component<ServerFreeplayEnterRealEstateComponent>
{
    private final EnterPremisesDoorData a;

    public ServerEnterRealEstateComponentData(EnterPremisesDoorData premisesDoorData,
                                              ServerFreeplayEnterRealEstateComponent contentComponent)
    {
        super(premisesDoorData, contentComponent);

        this.a = premisesDoorData;
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
                enter(ev.client, ev.playerData, ev.payload);
                break;
            }
        }

        return false;
    }

    private void enter(Client client, PlayerData playerData, String payload)
    {
        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        if (!(playerClient.getModePayload() instanceof FreePayload))
            return;

        enterPremises(playerClient, playerData, payload);
    }

    private static Array<String> copyMaps(ActiveData enterDoor, String m, PlayerClient playerClient,
        FreePayload freePayload, String location, String id, String rsName, JSONObject rsPayload)
    {
        Array<ServerMap> mps = null;

        PlayState playState = BrainOutServer.Controller.getPlayState();
        if (playState instanceof ServerPSGame)
        {
            mps = ((ServerPSGame) playState).getMaps();
        }

        if (mps == null)
        {
            return null;
        }

        playerClient.log("Copying over map " + m + " for location " + location + " (id " + id + ")");
        Array<String> newMapDimensions = new Array<>();

        ObjectMap<String, Map> personalMaps = freePayload.getPersonalMaps();
        if (personalMaps == null)
        {
            personalMaps = new ObjectMap<>();
            freePayload.setPersonalMaps(personalMaps);
        }

        StringWriter stringWriter = new StringWriter();
        {
            Json json = new Json();

            BrainOut.R.tag(json);

            json.setWriter(stringWriter);
            json.writeArrayStart();

            for (Map map : Map.All())
            {
                if (map.getDimension().equals(m) || map.getDimension().startsWith(m + "-"))
                {
                    String oldDimension = map.getDimension();
                    int oldDimensionId = map.getDimensionId();
                    map.setDimension(ServerMap.GetTargetMapForPersonalUse(location, id) + oldDimension.substring(m.length()));
                    map.setDimensionId(-1);

                    json.writeObjectStart();
                    map.write(json, ActiveData.ComponentWriter.TRUE, -1);
                    json.writeObjectEnd();

                    map.setDimension(oldDimension);
                    map.setDimensionId(oldDimensionId);
                }
            }

            json.writeArrayEnd();
        }

        {
            JsonReader jsonReader = new JsonReader();
            JsonValue v = jsonReader.parse(stringWriter.toString());
            Json json = new Json();
            BrainOut.R.tag(json);

            RealEstateInfo rs = new RealEstateInfo(ServerMap.GetTargetMapForPersonalUse(location, id), rsPayload);
            rs.name = rsName;
            rs.owner = playerClient.getAccount();

            String suffix = "-" + location + "-" + id;

            for (JsonValue mapValue : v)
            {
                String dimension = mapValue.getString("dimension", "default");
                ServerFreeplayMap mapCopy = new ServerFreeplayMap(dimension);
                mapCopy.setRealEstateInfo(rs);
                playerClient.log("Disabled map " + mapCopy.getDimension() + " for common download.");
                mapCopy.setPersonalRequestOnly(playerClient.getAccount());
                mapCopy.read(json, mapValue);

                for (RealEstateInfo.RealEstatePayload.ObjectAtLocation obj : rs.payload.getItems().values())
                {
                    if (!dimension.equals(obj.getMap()))
                    {
                        continue;
                    }

                    mapCopy.placeRealEstateObject(obj.originalKey, obj.x, obj.y, obj.item);
                    playerClient.log("Spawned rsitem (key " + obj.originalKey + ") " +
                        obj.item.getID() + " at " + obj.x + "x" + obj.y + " at " + dimension);
                }

                mapCopy.init();

                FreeplayExitDoorData exitDoorData = (FreeplayExitDoorData) mapCopy.getActiveForTag(Constants.ActiveTags.EXIT_DOOR,
                    activeData -> activeData instanceof FreeplayExitDoorData);

                if (exitDoorData != null)
                {
                    ServerExitAptDoorComponentData sfapt = exitDoorData.getComponent(ServerExitAptDoorComponentData.class);
                    if (sfapt != null)
                    {
                        sfapt.setEnterDoor(enterDoor);
                    }
                }

                for (ObjectMap.Entry<Integer, ActiveData> active : mapCopy.getActives())
                {
                    active.value.setDimension(dimension);

                    if (active.value instanceof PortalData)
                    {
                        ((PortalData) active.value).setTag(((PortalData) active.value).getTag() + suffix);
                    }
                }

                newMapDimensions.add(dimension);
                personalMaps.put(dimension, mapCopy);
                mps.add(mapCopy);
            }
        }

        return newMapDimensions;
    }

    public static Array<String> generatePremises(ActiveData enterDoor, PlayerClient playerClient,
        String m, String location, String id, String rsName, JSONObject rsPayload)
    {
        String targetMap = ServerMap.GetTargetMapForPersonalUse(location, id);

        if (Map.Get(targetMap) != null)
        {
            Array<String> maps = new Array<>();
            for (ServerMap map : Map.All(ServerMap.class))
            {
                if (!(map.getDimension().equals(targetMap) || map.getDimension().startsWith(targetMap + "-")))
                {
                    continue;
                }

                map.setPersonalRequestOnly(playerClient.getAccount());
                if (map.getDimension().startsWith(targetMap))
                {
                    maps.add(map.getDimension());
                }
            }
            // already exists
            return maps;
        }

        playerClient.log("Generating premises with map " + m + " at location " + location + " (id " + id + ")");
        FreePayload freePayload = ((FreePayload) playerClient.getModePayload());

        return copyMaps(enterDoor, m, playerClient, freePayload, location, id, rsName, rsPayload);
    }

    private boolean enterPremises(PlayerClient playerClient, PlayerData playerData, String targetMap)
    {
        playerClient.log("Entering premises " + targetMap);

        ServerMap map = Map.Get(targetMap, ServerMap.class);

        if (map == null)
        {
            return false;
        }

        if (!map.isPersonalRequestOnly())
        {
            return false;
        }

        if (!map.suitableForPersonalRequestFor(playerClient.getAccount()))
        {
            return false;
        }

        FreeplayExitDoorData exitDoorData = (FreeplayExitDoorData) map.getActiveForTag(Constants.ActiveTags.EXIT_DOOR,
                activeData -> activeData instanceof FreeplayExitDoorData);

        if (exitDoorData == null)
        {
            return false;
        }

        playerClient.log("Success!");

        ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return false;

        playerClient.addStat("apt-opened", 1);
        playerClient.enablePlayer(false);

        SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy != null)
        {
            phy.getSpeed().set(0, 0);
            playerClient.getServerPlayerController().sendPlayerData(false, 0);
        }

        Runnable finally_ = () ->
        {
            playerClient.enablePlayer(true);
        };

        progress.startCancellable(1, () ->
        {
            playerClient.moveTo(exitDoorData.getDimension(), exitDoorData.getX(), exitDoorData.getY());
            finally_.run();
        }, finally_);

        return true;
    }
}
