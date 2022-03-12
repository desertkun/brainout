package com.desertkun.brainout.server.mapsource;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.SpawnTarget;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.SpawnPoint;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.SpawnPointData;
import com.desertkun.brainout.data.components.SubPointComponentData;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.server.ServerSettings;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;

@Reflect("source.MapSetSource")
public class MapSetSource extends MapSource
{
    private ObjectMap<String, ServerSettings.MapConditions> maps;
    private ObjectMap<String, ServerSettings.GameModeConditions> modes;
    private Array<SetItem> sets;
    private Array<QueuedItem> queue;
    private Rotary rotary;
    private String forceMode;

    public enum Rotary
    {
        incremental,
        random
    }

    public MapSetSource()
    {
        sets = new Array<>();
        queue = new Array<>();

        rotary = Rotary.incremental;
    }

    @Override
    public Settings next()
    {
        if (queue.size == 0)
        {
            shuffle();
        }

        return queue.pop();
    }

    public Array<QueuedItem> getRandomMaps(int count)
    {
        Array<QueuedItem> maps = new Array<>();

        while (count > 0)
        {
            if (queue.size <= 0)
            {
                shuffle();
            }

            maps.add(queue.pop());
            count--;
        }

        return maps;
    }

    private void shuffle()
    {
        queue.clear();

        for (SetItem item: sets)
        {
            if (forceMode != null)
            {
                if (!item.modesList.contains(forceMode, false))
                    continue;
            }

            queue.add(new QueuedItem(item));
        }

        if (rotary == Rotary.random)
        {
            queue.shuffle();
        }
    }

    private QueuedItem findItem(String map, String mode)
    {
        for (SetItem item: sets)
        {
            if (map != null && !item.mapString.equals(map))
            {
                continue;
            }

            if (mode != null)
            {
                if (!item.isAllModes() && !item.modesList.contains(mode, false))
                {
                    continue;
                }
            }

            if (map == null)
            {
                map = item.mapString;
            }

            if (mode == null)
            {
                mode = item.modesList.random();
            }

            return new QueuedItem(map, mode);
        }

        return null;
    }

    @Override
    public boolean insert(String map, String mode)
    {
        QueuedItem item = findItem(map, mode);

        if (item == null)
            return false;

        queue.add(item);

        return true;
    }

    public void wrapPlayState(PlayState.ID playState)
    {
        if (queue.size == 0)
        {
            shuffle();
        }

        if (queue.size != 0)
        {
            queue.peek().setPlayState(playState);
        }
    }

    public void setForceMode(String mode)
    {
        this.forceMode = mode;
    }

    public class QueuedItem extends ServerSettings.BaseConditions implements Settings
    {
        public ServerSettings.MapConditions map;
        public ServerSettings.GameModeConditions mode;
        public ServerSettings.BaseConditions additional;
        public PlayState.ID playState;

        public QueuedItem(SetItem item)
        {
            map = item.acquireMap();
            mode = item.acquireMode();
            additional = item.getAdditionalSettings();
            playState = PlayState.ID.game;
        }

        public QueuedItem(String map, String mode)
        {
            this.map = maps.get(map);
            this.mode = modes.get(mode);
            additional = null;
            playState = PlayState.ID.game;
        }

        public void setPlayState(PlayState.ID playState)
        {
            this.playState = playState;
        }

        @Override
        public ServerSettings.MapConditions acquireMap()
        {
            return map;
        }

        @Override
        public ServerSettings.GameModeConditions acquireMode()
        {
            return mode;
        }

        @Override
        public ServerSettings.BaseConditions getAdditionalSettings()
        {
            return additional;
        }

        @Override
        public PlayState init(PlayState.InitCallback callback)
        {
            return BrainOutServer.Controller.setPlayState(callback, playState);
        }
    }

    public class SetItem extends ServerSettings.BaseConditions implements Json.Serializable
    {
        public String mapString;
        public Array<String> modesList;
        public ServerSettings.BaseConditions baseConditions;

        public SetItem()
        {
            baseConditions = new ServerSettings.BaseConditions();
        }

        @Override
        public void write(Json json)
        {
            super.write(json);
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            super.read(json, jsonData);

            mapString = jsonData.getString("map");
            modesList = new Array<>(jsonData.getString("mode").split(","));

            baseConditions.read(json, jsonData);
        }

        public ServerSettings.BaseConditions getAdditionalSettings()
        {
            return baseConditions;
        }

        public ServerSettings.MapConditions acquireMap()
        {
            if (mapString.equals("*"))
            {
                return maps.get(maps.keys().toArray().random());
            }
            else
            {
                return maps.get(mapString);
            }
        }

        public boolean isAllModes()
        {
            return modesList.size == 1 && modesList.get(0).equals("*");
        }

        public ServerSettings.GameModeConditions acquireMode()
        {
            if (forceMode != null)
            {
                ServerSettings.GameModeConditions m = modes.get(forceMode);

                if (m != null)
                {
                    return m;
                }
            }

            if (isAllModes())
            {
                return modes.get(modes.keys().toArray().random());
            }
            else
            {
                return modes.get(modesList.random());
            }
        }
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        rotary = Rotary.valueOf(jsonData.getString("rotary", Rotary.incremental.toString()));

        if (jsonData.has("maps"))
        {
            this.maps = json.readValue(ObjectMap.class,
                    ServerSettings.MapConditions.class, jsonData.get("maps"));
        }

        if (jsonData.has("modes"))
        {
            this.modes = json.readValue(ObjectMap.class,
                    ServerSettings.GameModeConditions.class, jsonData.get("modes"));
        }

        if (jsonData.has("sets"))
        {
            JsonValue sets = jsonData.get("sets");

            if (sets.isArray())
            {
                for (JsonValue v: sets)
                {
                    SetItem setting = new SetItem();
                    setting.read(json, v);
                    this.sets.add(setting);
                }
            }
        }

        sets.shuffle();

        if (Log.INFO) Log.info("Loaded " + this.sets.size + " sets.");
    }

    @Override
    public Array<ServerMap> loadMaps(ServerSettings.MapConditions settings, boolean init)
    {
        Array<ServerMap> maps_ = super.loadMaps(settings, init);
        for (ServerMap map : maps_)
        {
            renderSpawns(map);
        }
        return maps_;
    }

    public ObjectMap<String, ServerSettings.GameModeConditions> getModes()
    {
        return modes;
    }

    public ObjectMap<String, ServerSettings.MapConditions> getMaps()
    {
        return maps;
    }

    public Array<SetItem> getSets()
    {
        return sets;
    }

    private void renderSpawns(ServerMap map)
    {
        renderSpawnsForTeam(map, "team-green", "SPAWN_BASE");
        renderSpawnsForTeam(map, "team-blue", "SPAWN_BASE");
        renderSpawnsForTeam(map, "team-dm", "SPAWN_RANDOM");
    }

    private void renderSpawnsForTeam(ServerMap map, String teamName, String spawnName)
    {
        SpawnPoint spawn = BrainOutServer.ContentMgr.get("spawn", SpawnPoint.class);
        Team team = BrainOutServer.ContentMgr.get(teamName, Team.class);

        SpawnPointData data = (SpawnPointData)map.getActiveForTag(Constants.ActiveTags.SPAWNABLE, activeData ->
        {
            if (!(activeData instanceof SpawnPointData))
                return false;

            if (activeData.getContent() != spawn)
                return false;

            return activeData.getTeam() == team;
        });

        if (data != null)
            return;

        data = spawn.getData(map.getDimension());
        map.addActive(map.generateServerId(), data, true);

        Vector2 middleGround = new Vector2();

        int amount = map.countActivesForTag(Constants.ActiveTags.SPAWNABLE, new Map.Predicate()
        {
            @Override
            public boolean match(ActiveData activeData)
            {
                if (activeData instanceof SpawnPointData)
                    return false;

                SubPointComponentData spc = activeData.getComponent(SubPointComponentData.class);

                if (spc == null)
                    return false;

                if (spc.getTarget() == SpawnTarget.flag)
                    return false;

                if (activeData.getTeam() == team)
                {
                    middleGround.add(activeData.getX(), activeData.getY());
                    return true;
                }

                return false;
            }
        });

        if (amount > 0)
        {
            middleGround.scl(1.0f / (float) amount);
        }

        data.spawnRange = 256;
        data.setTeam(team);
        data.setName(spawnName);
        data.setPosition(middleGround.x, middleGround.y);
    }
}
