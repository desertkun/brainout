package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.bot.TaskEnforceFlags;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.playstate.PlayStateEndGame;

import java.util.Objects;
import java.util.TimerTask;

public class ServerNormalRealization extends ServerRealization<GameModeNormal>
{
    private int ticketsForNewPlayer;

    public ServerNormalRealization(GameModeNormal gameMode)
    {
        super(gameMode);

        this.ticketsForNewPlayer = 0;
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
    protected void maxPlayersIncreased(int diff)
    {
        float addPoints = diff * ticketsForNewPlayer;

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (!(team instanceof SpectatorTeam))
            {
                putPoints(team, addPoints);
            }
        }

        getGameMode().setInitialPoints(getGameMode().getInitialPoints() + addPoints);

        updated();
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
        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (BrainOutServer.Controller.getClients().getAmount(team) == 0) continue;

            // check if there's no one

            int points = getGameMode().getPoints(team);

            for (Map map : Map.All())
            {
                for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
                {
                    // add alive
                    if (activeData instanceof PlayerData && activeData.isAlive())
                    {
                        Client client = BrainOutServer.Controller.getClients().getByActive(activeData);

                        if (client != null && (client.isSpectator() || !client.isAlive()))
                        {
                            continue;
                        }

                        if (activeData.getTeam() == team)
                        {
                            points++;
                        }
                    }
                }
            }

            if (points == 0)
            {
                Array<Team> tms = new Array<>(BrainOutServer.Controller.getTeams());
                tms.removeValue(team, true);

                if (tms.size > 0) {
                    // return other team
                    gameResult.setTeamWon(tms.get(0));
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.ticketsForNewPlayer = jsonData.getInt("ticketsForNewPlayer", 0);

        float tickets = jsonData.getInt("tickets", 0);
        ObjectMap<Team, Float> points = getGameMode().getPoints();

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            points.put(team, tickets);
        }

        getGameMode().setInitialPoints(tickets);
    }

    @SuppressWarnings("unchecked")
    public boolean hasPoints(Team team)
    {
        ObjectMap<Team, Float> points = getGameMode().getPoints();

        return points.get(team, 0.0f) > 0.0f;
    }

    @SuppressWarnings("unchecked")
    public float getPoints(Team team, float pointsValue)
    {
        ObjectMap<Team, Float> points = getGameMode().getPoints();

        if (points.get(team) == null)
        {
            return 0.0f;
        }
        else
        {
            float a = points.get(team);
            float n = Math.max(a - pointsValue, 0);

            points.put(team, n);

            return a - n;
        }
    }

    @SuppressWarnings("unchecked")
    public void putPoints(Team team, float pointsValue)
    {
        ObjectMap<Team, Float> points = getGameMode().getPoints();
        points.put(team, points.get(team, 0.0f) + pointsValue);
    }

    private static ObjectMap<Team, Integer> s_flagsCount = new ObjectMap<>();

    private boolean isTeamBeingDominated(Team team)
    {
        s_flagsCount.clear();

        for (Map map : Map.All())
        {
            for (ActiveData active : map.getActivesForTag(Constants.ActiveTags.FLAG, false))
            {
                FlagData flag = ((FlagData) active);

                if (flag.getState() == FlagData.State.normal && flag.getTeam() != null)
                {
                    s_flagsCount.put(flag.getTeam(), s_flagsCount.get(flag.getTeam(), 0) + 1);
                }
            }
        }

        if (s_flagsCount.size == 0)
            return false;

        if (!s_flagsCount.containsKey(team))
            return true;

        int minValue = 99999, maxValue = 0;
        Team minTeam = null;

        // get the team with minimum points taken
        // and with the maximum points taken
        for (ObjectMap.Entry<Team, Integer> point : s_flagsCount)
        {
            if (point.value > maxValue)
            {
                maxValue = point.value;
            }

            if (point.value < minValue)
            {
                minValue = point.value;
                minTeam = point.key;
            }
        }

        return minValue != maxValue && team == minTeam;
    }


    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        if (team instanceof SpectatorTeam)
        {
            return SpawnMode.allowed;
        }

        if (!getGameMode().isGameActive())
        {
            return SpawnMode.allowed;
        }

        float a = getPoints(team, isTeamBeingDominated(team) ? 1.5f : 1.0f);

        if (a > 0)
        {
            updated();
            return SpawnMode.allowed;
        }

        return SpawnMode.forceSpectator;
    }

    @Override
    public void finished()
    {
        BrainOutServer.Controller.setSpeed(0.5f);

        BrainOut.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() -> BrainOutServer.Controller.setSpeed(1f));
            }
        }, 4000);
    }

    @Override
    public SpawnMode canSpawn(Team team)
    {
        if (!hasPoints(team))
        {
            return SpawnMode.forceSpectator;
        }

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
