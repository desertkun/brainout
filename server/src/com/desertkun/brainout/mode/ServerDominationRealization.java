package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskEnforceFlags;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FlagTakenEvent;
import com.desertkun.brainout.playstate.PlayStateEndGame;

import java.util.Objects;

public class ServerDominationRealization extends ServerRealization<GameModeDomination>
{
    private float counter;
    private ObjectMap<Team, Integer> takenPoints;

    public ServerDominationRealization(GameModeDomination gameMode)
    {
        super(gameMode);

        counter = getGameMode().getHoldFlagTime();
        takenPoints = new ObjectMap<>();
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        OrderedMap<Team, Integer> points = new OrderedMap<>();

        for (Team team: BrainOutServer.Controller.getTeams())
        {
            points.put(team, getGameMode().getPoints(team));
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

    @SuppressWarnings("unchecked")
    public void addPoints(Team team, int pointsValue)
    {
        ObjectMap<Team, Integer> points = getGameMode().getPoints();

        if (points.get(team) == null)
        {
            points.put(team, pointsValue);
        }
        else
        {
            points.put(team, points.get(team) + pointsValue);
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        if (super.onEvent(event))
        {
            return true;
        }

        switch (event.getID())
        {
            case flagTaken:
            {
                FlagTakenEvent flagTakenEvent = ((FlagTakenEvent) event);

                flagTaken(flagTakenEvent.team, flagTakenEvent.flagData);

                break;
            }
        }

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

        updated();
    }

    private void flagTaken(Team team, FlagData flagData)
    {
        // team could be 'null' because flag was taken back
        if (team != null)
        {
            addPoints(team, getGameMode().getTakeFlagPoints());
        }

        updated();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        Map map = Map.GetDefault();

        if (map == null)
            return;

        counter -= dt * map.getSpeed();
        if (counter < 0)
        {
            counter = getGameMode().getHoldFlagTime();

            takenPoints.clear();

            for (ObjectMap.Entry<FlagData, Team> takenFlag : getTakenFlags())
            {
                if (takenFlag.key.getState() != FlagData.State.normal)
                    return;

                Integer oldValue = takenPoints.get(takenFlag.value);
                int amount = oldValue != null ? oldValue : 0;
                takenPoints.put(takenFlag.value, amount + 1);
            }

            if (takenPoints.size > 0)
            {
                int minValue = 99999, maxValue = 0;

                // get the team with minimum points taken
                // and with the maximum points taken
                for (ObjectMap.Entry<Team, Integer> point : takenPoints)
                {
                    if (point.value > maxValue)
                    {
                        maxValue = point.value;
                    }

                    if (point.value < minValue)
                    {
                        minValue = point.value;
                    }
                }

                if (minValue != maxValue || (takenPoints.size == 1))
                {
                    for (ObjectMap.Entry<Team, Integer> point : takenPoints)
                    {
                        if (point.value == maxValue)
                        {
                            addPoints(point.key, getGameMode().getHoldFlagPoints());
                        }
                    }
                }
            }

            if (getTakenFlags().size > 0)
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

    @Override
    public boolean canTakeFlags()
    {
        return true;
    }

    @Override
    public Task getBotStartingTask(TaskStack taskStack, BotClient client)
    {
        return new TaskEnforceFlags(taskStack, client);
    }
}
