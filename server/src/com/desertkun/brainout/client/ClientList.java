package com.desertkun.brainout.client;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.common.msg.ReliableBody;
import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.common.msg.server.ChatMsg;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.ThrowableActiveData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.server.ServerController;

import java.util.TimerTask;

public class ClientList extends ObjectMap<Integer, Client>
{
    private static Matching TRUE = client -> true;

    private final ServerController serverController;
    private final AutoBalance autobalance;
    private int idCounter;
    private ObjectMap<Team, Array<Client>> teams;
    private ObjectMap<ActiveData, Client> actives;
    private ObjectMap<String, HistoryRecord> history;
    private int maxPlayers = ServerConstants.CLIENTS_MAX;
    private TimerTask updateBalanceTask;

    public interface Matching
    {
        boolean match(Client client);
    }

    public interface Foreach
    {
        void call(Client client);
    }

    public static class HistoryRecord
    {
        public String team;
        public boolean kicked;
        public int ratingBuffer;
        public int desertBuffer;
    }

    public ClientList(ServerController serverController)
    {
        this.serverController = serverController;
        this.idCounter = 0;

        this.teams = new ObjectMap<>();
        this.actives = new ObjectMap<>();
        this.autobalance = new AutoBalance(this);
        this.history = new ObjectMap<>();
    }

    public void init()
    {
        autobalance.init();
    }

    public void registerActive(ActiveData activeData, Client client)
    {
        actives.put(activeData, client);
    }

    public void unregiterActive(ActiveData activeData)
    {
        actives.remove(activeData);
    }

    public Client getByActive(ActiveData activeData)
    {
        return actives.get(activeData);
    }

    public AutoBalance getAutobalance()
    {
        return autobalance;
    }

    public void reset(Array<Team> teams)
    {
        this.teams.clear();

        for (Team team : teams)
        {
            if (team == null)
                continue;

            this.teams.put(team, new Array<>());
        }
    }

    public void updateBalance(boolean now)
    {
        if (now)
        {
            autobalance.update();
        }
        else
        {
            if (updateBalanceTask != null)
            {
                updateBalanceTask.cancel();
            }

            updateBalanceTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    updateBalanceTask = null;
                    BrainOutServer.PostRunnable(autobalance::update);
                }
            };

            BrainOut.Timer.schedule(updateBalanceTask, 10000);
        }
    }

    public void sortBalance()
    {
        autobalance.sortRanked();
    }

    public int getMaxAmount()
    {
        Team bestTeam = getBestTeam();
        if (bestTeam != null)
        {
            return getAmount(bestTeam);
        }

        return 0;
    }

    public int getMaxPlayerAmount()
    {
        Team bestTeam = getBestPlayerTeam();
        if (bestTeam != null)
        {
            return getPlayersAmount(bestTeam);
        }

        return 0;
    }

    public int getPlayersCount()
    {
        int result = 0;

        for (Entry<Integer, Client> entry : this)
        {
            if (entry.value instanceof PlayerClient)
            {
                result++;
            }
        }

        return result;
    }

    public int getPlayersAmount(Team team)
    {
        if (team == null) return 0;

        Array<Client> clients = teams.get(team);

        if (clients != null)
        {
            int cnt = 0;

            for (Client client : clients)
            {
                if (client instanceof PlayerClient)
                {
                    cnt++;
                }
            }

            return cnt;
        }

        return 0;
    }

    public int getAmount(Team team)
    {
        if (team == null) return 0;

        Array<Client> clients = teams.get(team);

        if (clients != null)
        {
            return clients.size;
        }

        return 0;
    }

    public Team getMostFreeTeam()
    {
        Team bestTeam = null;
        int min = 99999;

        for (ObjectMap.Entry<Team, Array<Client>> clients: teams)
        {
            if (clients.key instanceof SpectatorTeam)
            {
                continue;
            }

            if (min > clients.value.size)
            {
                min = clients.value.size;
                bestTeam = clients.key;
            }
        }

        return bestTeam;
    }

    public Team getBestPlayerTeam()
    {
        Team bestTeam = null;
        int max = 0;

        for (ObjectMap.Entry<Team, Array<Client>> clients: teams)
        {
            if (clients.key instanceof SpectatorTeam)
            {
                continue;
            }

            int cnt = 0;

            for (Client client : clients.value)
            {
                if (client instanceof PlayerClient)
                {
                    cnt++;
                }
            }

            if (max < cnt)
            {
                max = cnt;
                bestTeam = clients.key;
            }
        }

        return bestTeam;
    }

    public Team getBestTeam()
    {
        Team bestTeam = null;
        int max = 0;

        for (ObjectMap.Entry<Team, Array<Client>> clients: teams)
        {
            if (clients.key instanceof SpectatorTeam)
            {
                continue;
            }

            int cnt = 0;

            for (Client client : clients.value)
            {
                if (client instanceof PlayerClient)
                {
                    cnt++;
                }
            }

            if (max < cnt)
            {
                max = cnt;
                bestTeam = clients.key;
            }
        }

        return bestTeam;
    }

    public void clearHistory()
    {
        history.clear();
    }

    public SpectatorTeam getSpectatorTeam()
    {
        for (ObjectMap.Entry<Team, Array<Client>> clients: teams)
        {
            if (clients.key instanceof SpectatorTeam)
            {
                return ((SpectatorTeam) clients.key);
            }
        }

        return null;
    }

    public Team getFreeTeam(Client clientFor)
    {
        if (clientFor instanceof PlayerClient)
        {
            PlayerClient playerClient = ((PlayerClient) clientFor);

            if (playerClient.getAccount() != null)
            {
                GameMode gameMode = BrainOutServer.Controller.getGameMode();

                if (gameMode != null)
                {
                    HistoryRecord record = history.get(playerClient.getAccount());

                    if (record != null)
                    {
                        if (gameMode.isAboutToEnd())
                        {
                            Content team = BrainOut.ContentMgr.get(record.team);
                            if (team instanceof Team)
                            {
                                return ((Team) team);
                            }
                        }
                    }
                }
            }
        }

        Team freeTeam = null;
        int min = 9999;

        for (ObjectMap.Entry<Team, Array<Client>> clients: teams)
        {
            if (clients.key instanceof SpectatorTeam)
            {
                continue;
            }

            int cnt = 0;

            for (Client client : clients.value)
            {
                if (client instanceof PlayerClient)
                {
                    cnt++;
                }
            }

            if (min > cnt)
            {
                min = cnt;
                freeTeam = clients.key;
            }
        }

        return freeTeam;
    }

    public HistoryRecord getHistoryFor(PlayerClient client)
    {
        if (client.getAccount() == null)
            return null;

        return history.get(client.getAccount());
    }

    public boolean setClientTeam(Client client, Team team, boolean move)
    {
        return setClientTeam(client, team, move, true);
    }

    private void internalSetClientTeam(Client client, Team team)
    {
        if (client.getTeam() != null)
        {
            releaseClientTeam(client);
        }

        client.teamChanged(team);
        client.setTeam(team);

        getClients(team).add(client);
    }

    public boolean setClientTeam(Client client, Team team, boolean move, boolean check)
    {
        if (!check || canHaveTeam(team, move))
        {
            //check for remove grenades thrown by the player but not yet detonated
            PlayerData playerData = client.getPlayerData();

            if (playerData != null)
            {
                Map map = playerData.getMap();

                if (map != null)
                {
                    for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.THROWABLE,
                            throwableData -> throwableData.getOwnerId() == client.getId()))
                    {
                        if (activeData instanceof ThrowableActiveData)
                        {
                            map.removeActive(activeData, true, true, false);
                        }
                    }
                }
            }

            internalSetClientTeam(client, team);
            updateBalance(false);
            return true;
        }

        return false;
    }

    public Array<Client> getClients(Team team)
    {
        if (!teams.containsKey(team))
        {
            Array<Client> data = new Array<>();
            teams.put(team, data);
            return data;
        }

        return teams.get(team);
    }

    public float getEfficiency(Team team)
    {
        Array<Client> clients = teams.get(team);

        if (clients != null)
        {
            float score = 0;

            for (Client client: clients)
            {
                if (client instanceof PlayerClient)
                {
                    score += client.getEfficiency();
                }
            }

            return score;
        }

        return 0;
    }

    public void releaseClientTeam(Client client)
    {
        if (client.getTeam() != null)
        {
            Array<Client> clients = teams.get(client.getTeam());
            if (clients != null)
            {
                clients.removeValue(client, true);
            }
        }
    }

    public boolean canHaveTeam(Team team, boolean move)
    {
        if (autobalance.isEnabled())
        {
            int max = getMaxPlayerAmount();

            return getPlayersAmount(team) + (move ? 1 : 0) < max + autobalance.getBalanceValue();
        }

        return true;
    }

    public int getMaxPlayers()
    {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers)
    {
        this.maxPlayers = maxPlayers;
    }

    public BotClient newBotClient(Team team)
    {
        int id = generateId();
        BotClient client = new BotClient(id, serverController);
        put(id, client);
        internalSetClientTeam(client, team);

        return client;
    }

    public BotClient newFreePlayBotClient(Team team)
    {
        int id = generateId();
        BotClient client = new FreePlayBotClient(id, serverController);
        put(id, client);
        internalSetClientTeam(client, team);

        return client;
    }

    public void ensureBots()
    {
        int teamCount = 0;

        for (Team team: new Array.ArrayIterator<>(BrainOutServer.Controller.getTeams()))
        {
            if (team instanceof SpectatorTeam)
            {
                continue;
            }

            teamCount++;
        }

        if (teamCount == 0)
            return;

        int needBots = BrainOutServer.getInstance().getEnsureBots() / teamCount;

        for (Team team: new Array.ArrayIterator<>(BrainOutServer.Controller.getTeams()))
        {
            if (team instanceof SpectatorTeam)
            {
                continue;
            }

            ensureBots(team, needBots);
        }
    }

    private void ensureBots(Team team, int needBots)
    {
        int havePlayers = 0;
        Queue<BotClient> bots = new Queue<>();

        for (Entry<Integer, Client> entry : this)
        {
            Client client = entry.value;

            if (client.getTeam() != team)
                continue;

            if (client instanceof BotClient)
            {
                bots.addLast(((BotClient) client));
            }
            else if (client instanceof PlayerClient)
            {
                havePlayers++;
            }
        }

        int haveBots = bots.size;
        int updateBots;

        if (havePlayers >= needBots)
        {
            updateBots = -haveBots;
        }
        else
        {
            updateBots = needBots - haveBots - havePlayers;
        }

        if (updateBots > 0)
        {
            for (int i = 0; i < updateBots; i++)
            {
                BotClient botClient = newBotClient(team);

                if (botClient != null)
                {
                    botClient.init();
                }
                else
                {
                    return;
                }
            }
        }
        else if (updateBots < 0)
        {
            // remove old ones
            for (int i = 0; i < -updateBots; i++)
            {
                BotClient botClient = bots.removeFirst();

                if (botClient == null)
                    return;

                if (botClient.isAlive())
                {
                    botClient.kill(false, true);
                }

                releaseClient(botClient);
            }
        }
    }

    public PlayerClient newPlayerClient()
    {
        if (getPlayersCount() >= getMaxPlayers())
        {
            return null;
        }

        int id = generateId();
        PlayerClient client = new PlayerClient(id, serverController);
        put(id, client);

        this.ensureBots();

        return client;
    }

    public void releaseClient(Client client)
    {
        releaseClient(client, true);
    }

    public void releaseClient(Client client, boolean balance)
    {
        client.release();

        releaseClientTeam(client);
        remove(client.getId());

        BrainOutServer.Controller.removeOwnerKey(client.getId());

        if (balance)
        {
            updateBalance(false);

            if (client instanceof PlayerClient)
            {
                this.ensureBots();
            }
        }
    }

    public HistoryRecord newHistoryRecord(PlayerClient playerClient)
    {
        HistoryRecord record = getHistoryFor(playerClient);

        if (record == null)
        {
            record = new HistoryRecord();
            history.put(playerClient.getAccount(), record);
        }

        return record;
    }

    public void sendTCP(Object object)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            Client client = entry.value;
            if (client instanceof PlayerClient)
            {
                ((PlayerClient) client).sendTCP(object);
            }
        }
    }

    public void sendChat(String author, String text)
    {
        sendTCP(new ChatMsg(author, text, "server", ServerConstants.Chat.COLOR_INFO, -1));
    }

    public void sendUDP(UdpMessage object)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            Client client = entry.value;
            if (client instanceof PlayerClient)
            {
                ((PlayerClient) client).sendUDP(object);
            }
        }
    }

    public void sendReliableUDP(ReliableBody object)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            if (entry.value instanceof PlayerClient)
            {
                ((PlayerClient) entry.value).sendReliableUDP(object);
            }
        }
    }

    public void sendTCP(Object object, Matching matching)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            if (entry.value instanceof PlayerClient && matching.match(entry.value))
            {
                ((PlayerClient) entry.value).sendTCP(object);
            }
        }
    }

    public void sendUDP(UdpMessage object, Matching matching)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            if (entry.value instanceof PlayerClient && matching.match(entry.value))
            {
                ((PlayerClient) entry.value).sendUDP(object);
            }
        }
    }

    public void sendTCPExcept(Object object, Client except)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            Client client = entry.value;

            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = (PlayerClient)client;

                if (entry.value != except)
                    playerClient.sendTCP(object);
            }
        }
    }

    public void sendTCPExcept(Object object, int except)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            Client client = entry.value;

            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = (PlayerClient)client;

                if (entry.value.getId() != except)
                    playerClient.sendTCP(object);
            }
        }
    }

    public void sendUDPExcept(UdpMessage object, Client except, Matching matching)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            if (!(entry.value instanceof PlayerClient))
                continue;

            if (entry.value == except)
                continue;

            if (!matching.match(entry.value))
                continue;

            ((PlayerClient) entry.value).sendUDP(object);
        }
    }

    public void sendUDPExcept(UdpMessage object, int except, Matching matching)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            if (!(entry.value instanceof PlayerClient))
                continue;

            if (entry.value.getId() == except)
                continue;

            if (!matching.match(entry.value))
                continue;

            ((PlayerClient) entry.value).sendUDP(object);
        }
    }

    public void sendUDPExcept(UdpMessage object, Client except)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            Client client = entry.value;

            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = (PlayerClient)client;

                if (entry.value != except)
                    playerClient.sendUDP(object);
            }
        }
    }

    public void sendUDPExcept(UdpMessage object, int except)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            Client client = entry.value;

            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = (PlayerClient)client;

                if (entry.value.getId() != except)
                    playerClient.sendUDP(object);
            }
        }
    }

    public void update(float dt)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            entry.value.update(dt);
        }
    }

    public void foreach(Matching matching, Foreach foreach)
    {
        for (ObjectMap.Entry<Integer, Client> entry: this)
        {
            if (matching.match(entry.value))
            {
                foreach.call(entry.value);
            }
        }
    }

    public void foreach(Foreach foreach)
    {
        foreach(TRUE, foreach);
    }

    private int generateId()
    {
        return idCounter++;
    }

    public ObjectMap<Team, Array<Client>> getTeams()
    {
        return teams;
    }
}
