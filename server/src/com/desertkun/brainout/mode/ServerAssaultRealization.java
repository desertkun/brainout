package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskAssault;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.data.containers.ActiveDataMap;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.playstate.PlayStateEndGame;
import com.esotericsoftware.minlog.Log;

import java.util.TimerTask;

public class ServerAssaultRealization extends ServerRealization<GameModeAssault>
{
    private float timeBeforeRound;
    private float timeAfterRound;
    private float timeRound;

    private boolean allowFullSpawn;

    public ServerAssaultRealization(GameModeAssault gameMode)
    {
        super(gameMode);
    }

    @Override
    public boolean spectatorsCanSeeEnemies()
    {
        return false;
    }

    @Override
    public void clientInitialized(Client client, boolean reconnected)
    {
        super.clientInitialized(client, reconnected);

        if (getGameMode().isGameActive(false, false) &&
            getGameMode().getState() == GameModeAssault.State.active &&
            !reconnected)
        {
            client.setSpectator(true);
        }

        if (!client.isAlive())
        {
            client.requestSpawn();
        }
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        if (getGameMode().getScores().size < 2)
        {
            return timedOutDraw(gameResult);
        }

        int teams = 0;

        Team teamA = null, teamB = null;
        int scoreA = 0, scoreB = 0;

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (team instanceof SpectatorTeam)
                continue;

            teams++;

            if (teamA == null)
            {
                teamA = team;
                scoreA = getGameMode().getPoints(team);
                continue;
            }

            teamB = team;
            scoreB = getGameMode().getPoints(team);
        }

        if (teams == 2 && scoreA == scoreB)
        {
            return timedOutDraw(gameResult);
        }

        Team best = getGameMode().getBestTeam();

        if (best != null)
        {
            gameResult.setTeamWon(best);
            return true;
        }

        return false;
    }

    @Override
    public boolean isComplete(PlayStateEndGame.GameResult gameResult)
    {
        int teams = 0;

        Team teamA = null, teamB = null;
        int scoreA = 0, scoreB = 0;

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (team instanceof SpectatorTeam)
                continue;

            teams++;

            if (teamA == null)
            {
                teamA = team;
                scoreA = getGameMode().getPoints(team);
                continue;
            }

            teamB = team;
            scoreB = getGameMode().getPoints(team);
        }

        if (teams != 2)
            return false;

        int halfScore = getGameMode().getTargetScore() / 2 + 1;

        if (scoreA >= halfScore || scoreB >= halfScore)
        {
            Team best = getGameMode().getBestTeam();

            if (best != null)
            {
                gameResult.setTeamWon(best);
                return true;
            }
        }

        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        timeAfterRound = jsonData.getFloat("timeAfterRound", 5.0f);
        timeBeforeRound = jsonData.getFloat("timeBeforeRound", 15.0f);
        timeRound = jsonData.getFloat("timeRound", 300.0f);

        int targetPoints = jsonData.getInt("rounds", 15);
        ObjectMap<Team, Integer> points = getGameMode().getScores();

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            points.put(team, 0);
        }

        getGameMode().setTargetScore(targetPoints);
    }

    @SuppressWarnings("unchecked")
    public void putPoints(Team team, int pointsValue)
    {
        ObjectMap<Team, Integer> points = getGameMode().getScores();

        Integer have = points.get(team);

        points.put(team, have != null ? have + pointsValue : pointsValue);
    }

    @Override
    public void onClientDeath(Client client, Client killer, PlayerData playerData, InstrumentInfo info)
    {
        super.onClientDeath(client, killer, playerData, info);

        switch (getGameMode().getState())
        {
            case active:
            {
                client.setSpectator(true);
                break;
            }
        }

        updateTeams();
    }

    @Override
    public void clientReleased(Client client) {
        super.clientReleased(client);
        updateTeams();
    }

    private void updateTeams()
    {
        if (getGameMode().getState() != GameModeAssault.State.active)
            return;

        Team teamLost = null;

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (team instanceof SpectatorTeam)
                continue;

            getGameMode().setPlayersStats(team, 0, 0);
        }

        for (ObjectMap.Entry<Integer, Client> clientEntry : BrainOutServer.Controller.getClients())
        {
            Client client = clientEntry.value;

            if (client.getTeam() instanceof SpectatorTeam)
                continue;

            if (!client.isInitialized())
                continue;

            GameModeAssault.TeamState stat =
                getGameMode().getPlayersStats(client.getTeam());

            if (stat == null)
                continue;

            if (client.isAlive() && !client.isSpectator())
            {
                stat.alive++;
            }
            else
            {
                stat.dead++;
            }
        }

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (team instanceof SpectatorTeam)
                continue;

            GameModeAssault.TeamState stat = getGameMode().getPlayersStats(team);

            if (stat == null)
                continue;

            if (stat.alive == 0)
            {
                teamLost = team;
                break;
            }
        }

        if (teamLost == null)
        {
            updated();
        }
        else
        {
            for (Team team : BrainOutServer.Controller.getTeams())
            {
                if (team instanceof SpectatorTeam)
                    continue;

                if (BrainOutServer.Controller.getClients().getAmount(team) == 0)
                    continue;

                if (team != teamLost)
                {
                    teamWon(team);
                }
            }

            getGameMode().cancelTimer();
            roundCompleted(false);
        }
    }

    private void teamWon(Team team)
    {
        if (Log.INFO) Log.info("Team " + team.getID() + " won the round!");


        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (client.getTeam() instanceof SpectatorTeam)
                continue;

            if (client.getTeam() == team)
            {
                client.notify(NotifyAward.score, 0, NotifyReason.roundYourTeamWon, NotifyMethod.message, null);
            }
            else
            {
                client.notify(NotifyAward.score, 0, NotifyReason.roundYourTeamLost, NotifyMethod.message, null);
            }
        }

        putPoints(team, 1);
        updated();
    }

    @Override
    protected void warmUpComplete()
    {
        super.warmUpComplete();

        roundCompleted(true);
    }

    private void roundCompleted(boolean first)
    {
        if (Log.INFO) Log.info("Round completed!");

        getGameMode().setTimer(timeAfterRound, this::nextRound);
        getGameMode().setState(GameModeAssault.State.end);

        updated();
    }

    private void enablePlayer(Client client)
    {
        if (!client.isAlive())
            return;

        PlayerData player = client.getPlayerData();

        if (player == null)
            return;

        client.enablePlayer(true);
    }

    @Override
    public void onClientSpawn(Client client, PlayerData player)
    {
        super.onClientSpawn(client, player);

        if (!getGameMode().isGameActive())
            return;

        if (allowFullSpawn)
            return;

        if (client.getTeam() instanceof SpectatorTeam)
            return;

        PlayerOwnerComponent own =
                player.getComponent(PlayerOwnerComponent.class);

        ServerPlayerControllerComponentData ctr =
            player.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

        switch (getGameMode().getState())
        {
            case waiting:
            {
                ctr.setEnabled(false);
                own.setEnabled(false);

                //In case of respawn, we delete all cartridges thrown by the player to the ground to avoid copying them
                Map map = player.getMap();

                if (map != null)
                {
                    ActiveDataMap actives = map.getActives();
                    for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.ITEM,
                            throwableData -> throwableData.getOwnerId() == client.getId()))
                    {
                        if (activeData instanceof ItemData)
                        {
                            map.removeActive(activeData, true, true, false);
                        }
                    }
                }

                break;
            }
        }
    }

    private boolean isValidToStart()
    {
        int count = 0;

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (team instanceof SpectatorTeam)
                continue;

            if (BrainOutServer.Controller.getClients().getAmount(team) == 0)
                continue;

            boolean alive = false;

            for (Client client : BrainOutServer.Controller.getClients().getClients(team))
            {
                if (client.isAlive() && !client.isSpectator())
                {
                    alive = true;
                    break;
                }
            }

            if (alive)
            {
                count++;
            }
        }

        return count >= 2;
    }

    private void cleanupStuff()
    {
        for (Map map : Map.All())
        {
            Queue<ActiveData> toRemove = new Queue<>();

            for (ObjectMap.Entry<Integer, ActiveData> entry : map.getActives())
            {
                if (entry.value.getCreator().getID().equals("c4-active")
                        || entry.value.getCreator().getID().equals("claymore-active"))
                {
                    toRemove.addLast(entry.value);
                }
            }

            for (ActiveData activeData : toRemove)
            {
                map.removeActive(activeData, true, true, false);
            }
        }
    }

    private void roundStarted()
    {
        if (Log.INFO) Log.info("Round started!");

        allowFullSpawn = true;

        cleanupStuff();

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (client.isAlive())
            {
                enablePlayer(client);
            }
            else
            {
                client.setSpectator(false);
                if (!(client.getTeam() instanceof SpectatorTeam))
                {
                    client.forceSpawn();
                }
            }
        }

        allowFullSpawn = false;

        getGameMode().setTimer(timeRound, this::forceRoundCompletion);
        getGameMode().setState(GameModeAssault.State.active);

        updateTeams();
    }

    private void forceRoundCompletion()
    {
        Team bestTeam = null;
        Team worstTeam = null;

        int bestLives = 0;
        int worstLives = 0;

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (team instanceof SpectatorTeam)
                continue;

            if (BrainOutServer.Controller.getClients().getAmount(team) == 0)
                continue;

            int lives = 0;

            for (Client client : BrainOutServer.Controller.getClients().getClients(team))
            {
                if (client.isAlive() && !client.isSpectator())
                {
                    lives++;
                }
            }

            if (bestTeam == null)
            {
                bestTeam = team;
                bestLives = lives;
            }
            else
            {
                if (lives > bestLives)
                {
                    bestLives = lives;
                    bestTeam = team;
                }
            }

            if (worstTeam == null)
            {
                worstTeam = team;
                worstLives = lives;
            }
            else
            {
                if (lives < worstLives)
                {
                    worstLives = lives;
                    worstTeam = team;
                }
            }
        }

        if (worstLives == bestLives)
        {
            roundDraw();
        }
        else
        {
            teamWon(bestTeam);
        }

        roundCompleted(false);
        killAll();
    }

    private void roundDraw()
    {
        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            client.notify(NotifyAward.score, 0, NotifyReason.roundDraw, NotifyMethod.message, null);
        }
    }

    private void killAll()
    {
        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (client.getTeam() instanceof SpectatorTeam)
                continue;

            client.setSpectator(false);

            if (client.isAlive())
            {
                client.kill(false, false);
            }
        }
    }

    private void nextRound()
    {
        if (Log.INFO) Log.info("Next round!");

        cleanupStuff();

        getGameMode().setRound(getGameMode().getRound() + 1);

        if (!getGameMode().isGameFinished())
        {
            killAll();
        }

        getGameMode().setTimer(timeBeforeRound, this::validateStart);
        getGameMode().setState(GameModeAssault.State.waiting);

        updated();
    }

    private void validateStart()
    {
        if (isValidToStart())
        {
            roundStarted();
        }
        else
        {
            getGameMode().setTimer(0.25f, this::validateStart);
        }
    }

    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.allowed;
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
    public boolean immediateRespawn()
    {
        return true;
    }

    @Override
    public SpawnMode canSpawn(Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public Task getBotStartingTask(TaskStack taskStack, BotClient client)
    {
        return new TaskAssault(taskStack, client);
    }

    @Override
    public boolean needRolesForBots()
    {
        return true;
    }
}
