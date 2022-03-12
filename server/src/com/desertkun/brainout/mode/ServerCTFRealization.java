package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ServerChipReceiverComponentData;
import com.desertkun.brainout.data.components.ServerChipSpawnerComponentData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateEndGame;

import java.util.Objects;

public class ServerCTFRealization extends ServerRealization<GameModeCTF>
{
    private float counter;
    private ObjectMap<Team, Integer> teamChips;


    public ServerCTFRealization(GameModeCTF<GameModeRealization> gameMode)
    {
        super(gameMode);

        teamChips = new ObjectMap<>();
        counter = 0.5f;
    }

    @Override
    public void init(PlayState.InitCallback callback)
    {
        super.init(callback);

    }

    @Override
    protected void warmUpComplete()
    {
        super.warmUpComplete();

        Array<ServerChipSpawnerComponentData> spawners = new Array<>();

        for (Map map : Map.All())
        {
            for (ActiveData chip : map.getActivesForTag(Constants.ActiveTags.CHIP_SPAWNER, false))
            {
                ServerChipSpawnerComponentData spawner = chip.getComponent(ServerChipSpawnerComponentData.class);

                if (spawner != null)
                {
                    spawners.add(spawner);
                }
            }
        }

        spawners.shuffle();

        int chips = getGameMode().getChips();

        for (int i = 0; i < chips && i < spawners.size; i++)
        {
            spawners.get(i).setEnabled(true);
        }
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        OrderedMap<Team, Integer> points = new OrderedMap<>();

        for (Team team: BrainOutServer.Controller.getTeams())
        {
            points.put(team, getGameMode().getPoints(team) * 2 + getGameMode().getTakingPoints(team));
        }

        if (points.size == 2 &&
            Objects.equals(points.get(points.orderedKeys().get(0), 0), points.get(points.orderedKeys().get(1), 0)))
        {
            return timedOutDraw(gameResult);
        }

        points.orderedKeys().sort((o1, o2) -> points.get(o2, 0) - points.get(o1, 0));

        gameResult.setTeamWon(points.orderedKeys().get(0));

        return true;
    }

    @Override
    public boolean isComplete(PlayStateEndGame.GameResult gameResult)
    {
        for (Team team: BrainOutServer.Controller.getTeams())
        {
            if (getGameMode().getPoints(team) >= getGameMode().getWinPoints())
            {
                gameResult.setTeamWon(team);
                return true;
            }
        }

        return false;
    }
    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void clientInitialized(Client client, boolean reconnected)
    {
        super.clientInitialized(client, reconnected);

        if (!client.isAlive())
        {
            client.requestSpawn();
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        {
            Map map = Map.GetDefault();
            counter -= dt * map.getSpeed();
        }

        if (counter < 0)
        {
            counter = 0.5f;

            boolean canUpdate = false;

            for (Team team : BrainOutServer.Controller.getTeams())
            {
                teamChips.put(team, 0);
            }

            for (Map map : Map.All())
            {
                for (ActiveData chip: map.getActivesForTag(Constants.ActiveTags.CHIP_SPAWNER, false))
                {
                    ServerChipSpawnerComponentData spawner = chip.getComponent(ServerChipSpawnerComponentData.class);

                    if (spawner.isDelivered())
                        continue;

                    if (spawner.getOwner() != null)
                    {
                        Team team = spawner.getOwner();
                        teamChips.put(team, teamChips.get(team) + 1);
                    }
                }

                for (ActiveData chip: map.getActivesForTag(Constants.ActiveTags.CHIP_RECEIVER, false))
                {
                    ServerChipReceiverComponentData receiver = chip.getComponent(ServerChipReceiverComponentData.class);
                    if (getGameMode().setPoints(chip.getTeam(), receiver.getChips().size))
                    {
                        canUpdate = true;
                    }
                }
            }

            for (Team team : BrainOutServer.Controller.getTeams())
            {
                if (getGameMode().setTakingPoints(team, teamChips.get(team)))
                {
                    canUpdate = true;
                }
            }

            if (canUpdate)
            {
                updated();
            }
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
    }

    @Override
    public void write(Json json, int owner)
    {
    }

    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.allowed;
    }
}
