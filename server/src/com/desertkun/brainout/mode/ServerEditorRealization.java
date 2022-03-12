package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.editor.DeleteDimensionMsg;
import com.desertkun.brainout.common.editor.EditorProperty;
import com.desertkun.brainout.common.editor.NewDimensionMsg;
import com.desertkun.brainout.common.editor.ResizeMapMsg;
import com.desertkun.brainout.common.editor.props.get.*;
import com.desertkun.brainout.common.editor.props.set.*;
import com.desertkun.brainout.common.msg.client.editor.*;
import com.desertkun.brainout.common.msg.server.editor.MapSettingsUpdatedMsg;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.inspection.Inspectable;
import com.desertkun.brainout.inspection.props.PropertiesGetter;
import com.desertkun.brainout.inspection.props.PropertiesSetter;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.online.RoomSettings;
import com.desertkun.brainout.playstate.PlayStateEndGame;
import com.desertkun.brainout.server.mapsource.EmptyMapSource;
import com.esotericsoftware.minlog.Log;

import java.util.regex.Pattern;

public class ServerEditorRealization extends ServerRealization<GameModeEditor>
{
    private String owner;
    private ObjectMap<String, String> defaultCustom;

    public ServerEditorRealization(GameModeEditor gameMode)
    {
        super(gameMode);

        defaultCustom = new ObjectMap<>();
    }

    private Array<EditorProperty> getProperties(Inspectable inspectable)
    {
        PropertiesGetter getter = new PropertiesGetter();
        getter.act(inspectable);

        return getter.getProperties();
    }

    private void setProperties(Inspectable inspectable, Array<EditorProperty> properties)
    {
        PropertiesSetter setter = new PropertiesSetter(properties);
        setter.act(inspectable);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        defaultCustom.clear();

        JsonValue jsonValue = jsonData.get("defaultCustom");
        if (jsonValue != null)
        {
            if (jsonValue.isObject())
            {
                for (JsonValue v : jsonValue)
                {
                    defaultCustom.put(v.name(), v.asString());
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorGetActivePropertiesMsg msg)
    {
        final PlayerClient messageClient = getMessageClient();

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.activeId);

            if (activeData != null)
            {
                messageClient.sendTCP(new EditorSetActivePropertiesMsg(activeData,
                        getProperties(activeData)));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorSetActivePropertiesMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.activeId);

            if (activeData != null)
            {
                setProperties(activeData, new Array<>(msg.properties));

                activeData.updated();
            }
        });

        return true;
    }
    @SuppressWarnings("unused")
    public boolean received(final EditorSetActivePropertyMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.activeId);

            if (activeData != null)
            {
                setProperties(activeData, new Array<>(new EditorProperty[]{msg.property}));
                activeData.updated();
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ResizeMapMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            ServerMap map = Map.Get(msg.d, ServerMap.class);

            if (map == null)
                return;

            map.resize(msg.w, msg.h, msg.aX, msg.aY);
            redeliverMap();
        });

        return true;
    }

    public void redeliverMap()
    {
        BrainOutServer.Controller.playStateChanged();
    }

    private static Pattern DIMENSION_PATTERN = Pattern.compile("([a-z0-9-]{3,})");

    @SuppressWarnings("unused")
    public boolean received(final NewDimensionMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            if (Map.Get(msg.d, ServerMap.class) != null)
                return;

            if (!DIMENSION_PATTERN.matcher(msg.d).matches())
                return;

            ServerMap map = BrainOutServer.Controller.createMap(msg.w, msg.h, msg.d);

            Map defaultMap = Map.GetDefault();
            if (defaultMap != null)
            {
                map.setName(defaultMap.getName());
            }

            map.init();
            redeliverMap();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final DeleteDimensionMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            if ("default".equals(msg.d))
                return;

            ServerMap map = Map.Get(msg.d, ServerMap.class);
            if (map == null)
                return;

            map.dispose();

            redeliverMap();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorGetMapPropertiesMsg msg)
    {
        final PlayerClient messageClient = getMessageClient();

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            messageClient.sendTCP(new EditorSetMapPropertiesMsg(msg.d, getProperties(map)));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorSetMapPropertiesMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            setProperties(map, new Array<>(msg.properties));

            String data = BrainOut.R.JSON.toJson(map.getCustom());
            BrainOutServer.Controller.getClients().sendTCP(new MapSettingsUpdatedMsg(data, msg.d));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorBlockMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            Block block = msg.block == null ? null : BrainOutServer.ContentMgr.get(msg.block, Block.class);

            if (block == null)
            {
                map.setBlock(msg.x, msg.y, null, msg.layer, true);
            }
            else
            {
                BlockData blockData = block.getBlock();
                map.setBlock(msg.x, msg.y, blockData, msg.layer, true);
            }
        });

        return true;
    }

    @Override
    public boolean enableLoginPopup()
    {
        return false;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorActionMsg msg)
    {
        BrainOutServer.PostRunnable(() -> {
            switch (msg.id)
            {
                case saveMap:
                {
                    final Client messageClient1 = getMessageClient();
                    if (isEditor(messageClient1))
                    {
                        saveMap();
                    }

                    break;
                }
                case unload:
                {
                    unload();

                    break;
                }
            }
        });

        return true;
    }

    private void unload()
    {
        BrainOutServer.Controller.setMapSource(new EmptyMapSource(GameMode.ID.editor));
        BrainOutServer.PackageMgr.unloadPackages(true);
        BrainOutServer.Controller.next(null);
    }

    private void saveMap()
    {
        Array<ActiveData> toRemove = new Array<>();

        String name = null;

        for (Map map : Map.All())
        {
            name = map.getName();

            for (ObjectMap.Entry<Integer, ActiveData> entry : map.getActives())
            {
                if (entry.value instanceof PlayerData)
                {
                    toRemove.add(entry.value);
                }
            }

            for (ActiveData integer : toRemove)
            {
                map.removeActive(integer, true);
            }

            toRemove.clear();
        }

        if (name == null)
            return;

        BrainOutServer.Controller.getClients().sendChat("server", "Saving map...");

        if (BrainOutServer.Controller.saveAll("maps/" + name + ".map"))
        {
            BrainOutServer.Controller.getClients().sendChat("server", "Successfully!");
        }
        else
        {
            BrainOutServer.Controller.getClients().sendChat("server", "Failed!");
        }
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorActiveAddMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            Active active = ((Active) BrainOut.ContentMgr.get(msg.id));
            Team team = msg.team == null ? null : BrainOut.ContentMgr.get(msg.team, Team.class);
            if (active != null)
            {
                ActiveData activeData = active.getData(map.getDimension());

                activeData.setLayer(msg.layer);
                activeData.setPosition(msg.x, msg.y);
                activeData.setTeam(team);

                map.addActive(map.generateServerId(), activeData, true);
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorActiveRemoveMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.id);
            if (activeData != null)
            {
                map.removeActive(activeData, true);
            }
        });

        return true;
    }

    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.allowed;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorActiveMoveMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.id);
            if (activeData != null)
            {
                map.moveActive(activeData, msg.x, msg.y);
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorActiveCloneMsg msg)
    {
        if (!validateEditor()) return true;

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.id);

            if (activeData != null)
            {
                ActiveData copy = activeData.getCreator().getData(map.getDimension());

                Json json = BrainOut.R.JSON;

                String dump = Data.ComponentSerializer.toJson(activeData, Data.ComponentWriter.TRUE, -1);

                copy.read(json, new JsonReader().parse(dump));
                copy.setPosition(msg.x, msg.y);

                map.addActive(map.generateServerId(), copy, true);
            }
        });

        return true;
    }

    public boolean validateEditor()
    {
        /*
        final PlayerClient messageClient = getMessageClient();
        if (isEditor(messageClient)) return true;

        messageClient.sendTCP(new SimpleMsg(SimpleMsg.Code.notAllowed));

        return false;
        */

        return true;
    }

    public boolean isEditor(Client client)
    {
        switch (client.getRights())
        {
            case admin:
            case mod:
            case editor:
                return true;
            default:
                return false;
        }
    }

    public boolean isMod(Client client)
    {
        switch (client.getRights())
        {
            case admin:
            case owner:
            case mod:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean isComplete(PlayStateEndGame.GameResult gameResult)
    {
        return false;
    }

    @Override
    public void clientInitialized(Client client, boolean reconnected)
    {
        super.clientInitialized(client, reconnected);

        if (client instanceof PlayerClient)
        {
            PlayerClient playerClient = ((PlayerClient) client);

            String account = playerClient.getAccount();

            if (account != null && owner != null && account.equals(owner))
            {
                playerClient.setRights(PlayerRights.owner);
                playerClient.log("Player is a server owner now.");
            }

            playerClient.sendRightsUpdated();
            playerClient.sendUserProfile();
        }

        client.setState(Client.State.readyToSpawn);
    }

    @Override
    public void initSettings(RoomSettings settings)
    {
        super.initSettings(settings);

        this.owner = settings != null ? settings.getParty() : "";

        if (Log.INFO) Log.info("Updating party settings");

        if (this.owner != null)
        {
            if (Log.INFO) Log.info("Owner: @" + this.owner);
        }
    }

    @Override
    public boolean needsDeploymentsCheck()
    {
        return false;
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        return false;
    }

    @Override
    public void onClientDeath(Client client, Client killer, PlayerData playerData, InstrumentInfo info)
    {
        client.setState(Client.State.readyToSpawn);
    }

    public ObjectMap<String, String> getDefaultCustom()
    {
        return defaultCustom;
    }
}
