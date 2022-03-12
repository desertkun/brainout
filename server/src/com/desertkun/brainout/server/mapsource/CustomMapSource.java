package com.desertkun.brainout.server.mapsource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
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
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.server.ServerSettings;
import com.desertkun.brainout.utils.SteamAPIUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Reflect("source.CustomMapSource")
public class CustomMapSource extends MapSource implements MapSource.Settings
{
    private ObjectMap<String, ServerSettings.GameModeConditions> modes;

    private static Array<GameMode.ID> DEFAULT_MODES = new Array<>(new GameMode.ID[]{
        GameMode.ID.normal,
        GameMode.ID.domination
    });

    private final String mapId;
    private PlayState.ID playState;
    private GameMode.ID preferableMode;
    private final byte[] map;
    private final SteamAPIUtil.WorkshopItemResult workshopItemResult;

    private static byte[] convert(InputStream is) throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    public CustomMapSource(InputStream map, String mapId, SteamAPIUtil.WorkshopItemResult workshopItemResult) throws IOException
    {
        this.modes = new ObjectMap<>();
        this.map = convert(map);
        this.mapId = mapId;
        this.playState = PlayState.ID.game;
        this.workshopItemResult = workshopItemResult;

        read(new Json(), new JsonReader().parse(Gdx.files.local("maps-set-custom.json")));
    }

    public void setPreferableMode(String preferableMode)
    {
        try
        {
            this.preferableMode = GameMode.ID.valueOf(preferableMode);
        }
        catch (Exception ignored)
        {
            this.preferableMode = null;
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("modes"))
        {
            this.modes = json.readValue(ObjectMap.class,
                    ServerSettings.GameModeConditions.class, jsonData.get("modes"));
        }
    }

    @Override
    public boolean insert(String map, String mode)
    {
        this.playState = PlayState.ID.game;

        return true;
    }

    @Override
    public void wrapPlayState(PlayState.ID playState)
    {
        this.playState = playState;
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

        if (data == null)
        {
            data = spawn.getData(map.getDimension());
            map.addActive(map.generateServerId(), data, true);
        }

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

    @Override
    public Settings next()
    {
        return this;
    }

    @Override
    public ServerSettings.MapConditions acquireMap()
    {
        return new ServerSettings.MapConditions(mapId);
    }

    @Override
    public ServerSettings.GameModeConditions acquireMode()
    {
        Array<GameMode.ID> suitableModes = new Array<>();

        for (String tagName : workshopItemResult.getTags())
        {
            GameMode.ID gameModeId;

            try
            {
                gameModeId = GameMode.ID.valueOf(tagName);
            }
            catch (Exception ignored)
            {
                continue;
            }

            suitableModes.add(gameModeId);
        }

        if (preferableMode != null && suitableModes.indexOf(preferableMode, true) >= 0)
        {
            ServerSettings.GameModeConditions m = modes.get(preferableMode.toString());

            if (m != null)
            {
                return m;
            }
        }

        if (suitableModes.size == 0)
        {
            return modes.get(DEFAULT_MODES.random().toString());
        }

        return modes.get(suitableModes.random().toString());
    }

    @Override
    public ServerSettings.BaseConditions getAdditionalSettings()
    {
        return null;
    }

    @Override
    public Array<ServerMap> loadMaps(ServerSettings.MapConditions settings, boolean init)
    {
        Array<ServerMap> maps = BrainOutServer.Controller.loadMaps(new ByteArrayInputStream(this.map), true);

        if (maps != null)
        {
            for (ServerMap map : maps)
            {
                map.setName("custom");
                map.setCustom("workshop-id", mapId);
                renderSpawns(map);
            }

            if (init)
            {
                for (ServerMap map : maps)
                {
                    map.init();
                }
            }
        }

        return maps;
    }

    @Override
    public PlayState init(PlayState.InitCallback callback)
    {
        return BrainOutServer.Controller.setPlayState(callback, playState);
    }
}
