package com.desertkun.brainout.server.mapsource;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Background;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.SpawnPoint;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.SpawnPointData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.server.ServerController;
import com.desertkun.brainout.server.ServerSettings;

@Reflect("source.Editor2MapSource")
public class Editor2MapSource extends MapSource implements MapSource.Settings
{
    private final Constants.Editor.MapSize size;
    private final String mapName;
    private final Background background;

    private ServerSettings.MapConditions map;
    private ServerSettings.GameModeConditions mode;

    public Editor2MapSource(String mapName, Constants.Editor.MapSize size, Background background)
    {
        this.map = new ServerSettings.MapConditions(mapName);
        this.mode = new ServerSettings.GameModeConditions(GameMode.ID.editor2);

        this.mapName = mapName;
        this.size = size;
        this.background = background;
    }

    @Override
    public Array<ServerMap> loadMaps(ServerSettings.MapConditions settings, boolean init)
    {
        ServerController C = BrainOutServer.Controller;

        Array<ServerMap> maps = new Array<>();

        ServerMap map = C.createMap(size.getW(), size.getH(), "default");
        maps.add(map);
        map.setName(mapName);
        background.activate(map);

        if (init)
        {
            map.init();
        }

        renderGround(map);
        renderBorders(map);

        return maps;
    }

    private void renderBorders(ServerMap map)
    {
        Block collision = BrainOutServer.ContentMgr.get("collision-nophy", Block.class);
        int[] xs = new int[]
                {
                        0, 1, map.getWidth() - 2, map.getWidth() - 1
                };

        for (int y = 64; y < 80; y++)
        {
            for (int x : xs)
            {
                BlockData blockData = collision.getBlock();
                map.setBlock(x, y, blockData, Constants.Layers.BLOCK_LAYER_FOREGROUND, false, false);
            }
        }
    }

    private void renderGround(ServerMap map)
    {
        Block ground = BrainOutServer.ContentMgr.get("ground", Block.class);

        for (int j = 0; j < 64; j++)
        {
            for (int i = 0, t = map.getWidth(); i < t; i++)
            {
                BlockData blockData = ground.getBlock();
                map.setBlock(i, j, blockData, Constants.Layers.BLOCK_LAYER_FOREGROUND, false, false);
            }
        }
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
    public PlayState init(PlayState.InitCallback callback)
    {
        return BrainOutServer.Controller.setPlayState(callback, PlayState.ID.game);
    }
}
