package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.bot.*;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.playstate.PlayStateEndGame;
import com.desertkun.brainout.playstate.ServerPSEndGame;

import java.util.TimerTask;

public class ServerDeathmatchRealization extends ServerRealization<GameModeDeathmatch>
{
    private int ticketsForNewPlayer;

    public ServerDeathmatchRealization(GameModeDeathmatch gameMode)
    {
        super(gameMode);

        this.ticketsForNewPlayer = 0;
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        Array<Client> clients = BrainOutServer.Controller.getClients().values().toArray();
        clients.sort((o1, o2) -> o1.getScore() < o2.getScore() ? 1 : -1);

        if (clients.size > 0)
        {
            gameResult.setPlayerWon(clients.get(0).getId());
        }

        if (gameResult instanceof ServerPSEndGame.ServerGameResult)
        {
            ServerPSEndGame.ServerGameResult serverGameResult = ((ServerPSEndGame.ServerGameResult) gameResult);
            serverGameResult.getRewardClients().clear();

            for (int i = 0; i < (float)clients.size / 2.0f; i++)
            {
                serverGameResult.getRewardClients().add(clients.get(i));
            }
        }

        return true;
    }

    @Override
    public boolean isComplete(PlayStateEndGame.GameResult gameResult)
    {
        if (getGameMode().getTickets() <= 0)
        {
            Array<Client> clients = BrainOutServer.Controller.getClients().values().toArray();
            clients.sort((o1, o2) -> o1.getScore() < o2.getScore() ? 1 : -1);

            if (clients.size > 0)
            {
                gameResult.setPlayerWon(clients.get(0).getId());
            }

            if (gameResult instanceof ServerPSEndGame.ServerGameResult)
            {
                ServerPSEndGame.ServerGameResult serverGameResult = ((ServerPSEndGame.ServerGameResult) gameResult);
                serverGameResult.getRewardClients().clear();

                for (int i = 0; i < (float)clients.size / 2.0f; i++)
                {
                    serverGameResult.getRewardClients().add(clients.get(i));
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.ticketsForNewPlayer = jsonData.getInt("ticketsForNewPlayer", 0);

        int tickets = jsonData.getInt("tickets", 0);
        getGameMode().setInitialTickets(tickets);
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
        int addPoints = diff * ticketsForNewPlayer;

        getGameMode().setTickets(getGameMode().getTickets() + addPoints);
        getGameMode().setInitialTickets(getGameMode().getInitialTickets() + addPoints);

        updated();
    }

    @Override
    public void onClientDeath(Client client, Client killer, PlayerData playerData, InstrumentInfo info)
    {
        super.onClientDeath(client, killer, playerData, info);

        if (!getGameMode().isGameActive())
            return;

        getGameMode().setTickets(getGameMode().getTickets() - 1);

        updated();
    }

    @Override
    public SpawnMode canSpawn(Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public Task getBotStartingTask(TaskStack taskStack, BotClient client)
    {
        return new TaskHunter(taskStack, client);
    }

    @Override
    public boolean needRolesForBots()
    {
        return false;
    }
}
