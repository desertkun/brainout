package com.desertkun.brainout.server.mapsource;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerFreeplayMap;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.containers.BlockMatrixData;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.server.ServerSettings;
import com.esotericsoftware.minlog.Log;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Reflect("source.ShuffledMapSource")
public class ShuffledMapSource extends MapSource implements MapSource.Settings
{
    private ServerSettings.MapConditions map;
    private ServerSettings.GameModeConditions mode;
    private PlayState.ID playState;
    private Array<DimensionCollection> dimensions;

    public class MapInDimension
    {
        public String map;
        public Array<ServerMap> data;
        public int position;

        public MapInDimension(String map, JSONObject settings)
        {
            this.map = map;
            this.position = settings.optInt("position", -1);
        }

        public void load()
        {
            this.data = BrainOutServer.Controller.loadMaps(this.map, ServerConstants.Maps.MAP_KEY);
        }
    }

    public class DimensionCollection
    {
        private String name;
        private Array<MapInDimension> maps;
        private ObjectMap<String, String> custom;

        public DimensionCollection(String name, JSONObject maps)
        {
            this.name = name;
            this.maps = new Array<>();
            this.custom = new ObjectMap<>();

            read(maps);
        }

        public void read(JSONObject data)
        {
            JSONObject maps = data.getJSONObject("maps");

            for (String mapName : maps.keySet())
            {
                JSONObject settings = maps.optJSONObject(mapName);
                MapInDimension m = new MapInDimension(mapName, settings);
                this.maps.add(m);
            }

            this.maps.shuffle();

            JSONObject custom = data.optJSONObject("custom");

            if (custom != null)
            {
                for (String c : custom.keySet())
                {
                    this.custom.put(c, custom.getString(c));
                }
            }

            Array<MapInDimension> temp = new Array<>(this.maps);

            for (MapInDimension map : temp)
            {
                int realPosition = this.maps.indexOf(map, true);

                if (map.position != -1 && realPosition != map.position)
                {
                    this.maps.swap(realPosition, map.position);
                }
            }
        }
    }

    public ShuffledMapSource()
    {
        this.map = new ShuffledMapConditions();
        this.mode = new ServerSettings.GameModeConditions();
        this.playState = PlayState.ID.game;
    }

    public class ShuffledMapConditions extends ServerSettings.MapConditions
    {
        public ShuffledMapConditions()
        {
            super("shuffled");
        }
    }

    public ShuffledMapSource(String map, GameMode.ID mode)
    {
        JSONObject shuffle;

        this.dimensions = new Array<>();

        try
        {
            shuffle = new JSONObject(new String(Files.readAllBytes(Paths.get(map))));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        JSONObject dimensions = shuffle.optJSONObject("dimensions");

        for (String dimensionName : dimensions.keySet())
        {
            DimensionCollection dimensionCollection = new DimensionCollection(dimensionName,
                dimensions.optJSONObject(dimensionName));
            this.dimensions.add(dimensionCollection);
        }


        this.map = new ShuffledMapConditions();
        this.mode = new ServerSettings.GameModeConditions(mode);
        this.playState = PlayState.ID.game;
    }

    @Override
    public Array<ServerMap> loadMaps(ServerSettings.MapConditions settings, boolean init)
    {
        Array<ServerMap> maps = new Array<>();

        for (DimensionCollection dimension : this.dimensions)
        {
            for (MapInDimension m : dimension.maps)
            {
                m.load();
            }
        }

        for (DimensionCollection dimension : this.dimensions)
        {
            String dimensionName = dimension.name;

            if (Log.INFO) Log.info("Processing dimension " + dimensionName);

            int width = 0;
            int height = 0;
            boolean hadDefaultDimension = false;

            for (MapInDimension m : dimension.maps)
            {
                for (ServerMap serverMap : m.data)
                {
                    if (serverMap.getDimension().equals("default"))
                    {
                        hadDefaultDimension = true;
                        width += serverMap.getBlocks().getBlockWidth();
                        height = Math.max(height, serverMap.getBlocks().getBlockHeight());
                    }
                    else
                    {
                        if (Log.INFO) Log.info("Preloaded map " + m.map);

                        maps.add(serverMap);

                        for (ObjectMap.Entry<String, String> entry : serverMap.custom)
                        {
                            Log.info("Custom " + entry.key + " = " + entry.value);
                        }
                    }
                }
            }

            if (hadDefaultDimension)
            {
                ServerMap map = new ServerFreeplayMap(dimensionName, width, height, false);

                for (ObjectMap.Entry<String, String> entry : dimension.custom)
                {
                    map.custom.put(entry.key, entry.value);
                }

                int offsetX = 0;
                int offsetY = 0;

                for (MapInDimension m : dimension.maps)
                {
                    for (ServerMap serverMap : m.data)
                    {
                        if (!serverMap.getDimension().equals("default"))
                            continue;

                        if (Log.INFO) Log.info("Loading map " + m.map + " into dimension " + dimensionName);

                        for (ChunkData chunk : serverMap.getBlocks())
                        {
                            map.getBlocks().setChunk(
                                    offsetX + chunk.getIdX(),
                                    offsetY + chunk.getIdY(),
                                    chunk
                            );
                        }

                        serverMap.setDimension(dimensionName);
                        map.getActiveNameIndex().putAll(serverMap.getActiveNameIndex());

                        int blockWidth = serverMap.getBlocks().getBlockWidth();

                        serverMap.getBlocks().clear();

                        for (ObjectMap.Entry<Integer, ActiveData> entry : serverMap.getActives())
                        {
                            ActiveData activeData = entry.value;
                            activeData.setDimension(dimensionName);
                            activeData.setPosition(
                                    activeData.getX() + offsetX * Constants.Core.CHUNK_SIZE,
                                    activeData.getY() + offsetY * Constants.Core.CHUNK_SIZE
                            );
                            map.addActive(map.generateServerId(), activeData, false);
                        }

                        for (ObjectMap.Entry<Integer, ObjectMap<Integer, ActiveData>> entry : serverMap.getActives().getTagList())
                        {
                            int tag = entry.key;
                            ObjectMap<Integer, ActiveData> items = map.getActives().getItemsForTag(tag, true);

                            // the ids have changed
                            for (ObjectMap.Entry<Integer, ActiveData> activeDataEntry : entry.value)
                            {
                                items.put(activeDataEntry.value.getId(), activeDataEntry.value);
                            }

                            entry.value.clear();
                        }

                        serverMap.getActives().getTagList().clear();
                        serverMap.getActives().clear();

                        offsetX += blockWidth;
                    }
                }

                BlockMatrixData blocks = map.getBlocks();

                for (int j = 0; j < blocks.getBlockHeight(); j++)
                {
                    for (int i = 0; i < blocks.getBlockWidth(); i++)
                    {
                        ChunkData chunk = blocks.getChunk(i, j);

                        if (chunk != null)
                            continue;

                        ChunkData newChunk = new ChunkData(blocks, i, j);
                        newChunk.init();

                        blocks.setChunk(i, j, newChunk);
                    }
                }

                maps.add(map);

                if (Log.INFO)
                {
                    for (ObjectMap.Entry<String, String> entry : map.custom)
                    {
                        Log.info("Custom " + entry.key + " = " + entry.value);
                    }

                    Log.info("Map has " + map.getActiveNameIndex().size + " indexed items and " + width + " width.");
                }
            }
        }

        for (ServerMap serverMap : maps)
        {
            Log.info("Map " + serverMap.getDimension() + " -> " + serverMap.getDimensionId());
        }

        return maps;
    }

    @Override
    public void wrapPlayState(PlayState.ID playState)
    {
        this.playState = playState;
    }

    @Override
    public Settings next()
    {
        return this;
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
        return null;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        String map = jsonData.getString("map");
        String mode = jsonData.getString("mode");

        this.map.map = map;
        this.mode.mode = GameMode.ID.valueOf(mode);
    }

    @Override
    public PlayState init(PlayState.InitCallback callback)
    {
        return BrainOutServer.Controller.setPlayState(callback, playState);
    }
}
