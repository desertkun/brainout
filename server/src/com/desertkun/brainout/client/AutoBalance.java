package com.desertkun.brainout.client;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.mode.GameMode;
import com.esotericsoftware.minlog.Log;

import java.util.Comparator;

public class AutoBalance
{
    private final ClientList clientList;
    private boolean enabled;
    private int balanceValue;

    public AutoBalance(ClientList clientList)
    {
        this.clientList = clientList;
    }

    void init()
    {
        setEnabled(BrainOutServer.Settings.isAutoBalanceEnabled());
        setBalanceValue(BrainOutServer.Settings.getAutoBalanceValue());
    }

    public int getBalanceValue()
    {
        return balanceValue;
    }

    public void setBalanceValue(int balanceValue)
    {
        this.balanceValue = balanceValue;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void update()
    {
        if (!isEnabled())
        {
            return;
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode != null && gameMode.isAboutToEnd())
        {
            return;
        }

        boolean needToCheck;
        boolean moved = false;

        do
        {
            needToCheck = false;

            Team bestTeam = clientList.getBestPlayerTeam();

            if (bestTeam != null)
            {
                int max = clientList.getMaxPlayerAmount();

                for (Team team : BrainOutServer.Controller.getTeams())
                {
                    if (team instanceof SpectatorTeam || team == null)
                    {
                        continue;
                    }

                    int teamSize = clientList.getPlayersAmount(team);

                    // well, that's not enough people in our team
                    if (teamSize + getBalanceValue() < max)
                    {
                        if (Log.INFO) Log.info("Balancing team " + team.getID());

                        // time to move player from team <bestTeam> to <team>
                        float teamScore = clientList.getEfficiency(team);
                        float bestScore = clientList.getEfficiency(bestTeam);

                        // find out how much score this team is lacking of
                        final float neededScore = bestScore - teamScore;

                        Array<Client> bestClients = new Array<>();

                        for (Client client : clientList.getClients(bestTeam))
                        {
                            if (client instanceof PlayerClient)
                            {
                                bestClients.add(client);
                            }
                        }

                        bestClients.sort((o1, o2) -> Math.abs(neededScore - o1.getEfficiency()) >
                            Math.abs(neededScore - o2.getEfficiency()) ? 1 : -1);

                        Client bestClient = null;

                        // find the closest
                        for (Client client : bestClients)
                        {
                            boolean hasFriends = false;

                            // look for friends
                            for (Client check : new Array.ArrayIterator<>(bestClients))
                            {
                                if (client == check)
                                    continue;

                                if (client.isSocialFriendOf(check))
                                {
                                    hasFriends = true;
                                    break;
                                }
                            }

                            if (!hasFriends)
                            {
                                bestClient = client;
                                break;
                            }
                        }

                        if (bestClient == null && bestClients.size > 0)
                        {
                            // if everyone everyone's friends, move anyway
                            bestClient = bestClients.get(0);
                        }

                        if (bestClient != null && bestClient.getTeam() != team)
                        {
                            if (Log.INFO) Log.info("Moving player " + bestClient.getId() +
                                    " from team " + bestTeam.getID() + " to team " + team.getID());
                            // change the team of bestClient

                            if (clientList.setClientTeam(bestClient, team, true))
                            {
                                if (bestClient.isAlive())
                                {
                                    bestClient.kill();
                                    bestClient.notify(NotifyAward.none, 0, NotifyReason.autoBalanced, NotifyMethod.popup,
                                            null);
                                }

                                moved = true;
                            }

                            needToCheck = true;
                            break;
                        }
                    }
                }
            }
        } while (needToCheck);

        if (moved)
        {
            BrainOutServer.Controller.getClients().ensureBots();
        }
    }

    public void sortRanked()
    {
        if (!isEnabled())
        {
            return;
        }

        BrainOutServer.Controller.getClients().ensureBots();

        if (Log.INFO) Log.info("Sorting ranked!");

        Array<Array<Client>> groups = new Array<>();

        int teamCount = 0;
        int playerCount = 0;

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (team instanceof SpectatorTeam)
            {
                continue;
            }

            teamCount++;
        }

        for (Client client : clientList.values())
        {
            if (client.isInitialized())
            {
                playerCount++;
            }
        }

        int playersPerTeam = teamCount > 0 ? (int)Math.ceil((float)playerCount / teamCount) : 1;

        if (BrainOutServer.Controller.getClients().size > 2 && teamCount > 0)
        {
            int maxGroupSize = Math.max(
                (int)Math.ceil((float)BrainOutServer.Controller.getClients().size / teamCount), 1);

            if (maxGroupSize > 1)
            {
                // create a list of groups

                for (Client client : clientList.values())
                {
                    if (!client.isInitialized())
                        continue;

                    boolean inGroupAlready = false;

                    for (Array<Client> group : groups)
                    {
                        if (group.indexOf(client, true) >= 0)
                        {
                            inGroupAlready = true;
                            break;
                        }
                    }

                    if (inGroupAlready)
                        continue;

                    for (Client check : new ObjectMap.Values<>(clientList))
                    {
                        if (check == client)
                            continue;

                        if (client.isSocialFriendOf(check))
                        {
                            Array<Client> checkGroup = null;

                            for (Array<Client> group : groups)
                            {
                                if (group.indexOf(check, true) >= 0)
                                {
                                    checkGroup = group;
                                    break;
                                }
                            }

                            if (checkGroup != null)
                            {
                                if (checkGroup.size < maxGroupSize)
                                {
                                    checkGroup.add(client);
                                }
                            } else
                            {
                                Array<Client> newGroup = new Array<>();
                                newGroup.add(client);
                                newGroup.add(check);

                                groups.add(newGroup);
                            }
                        }
                    }
                }

                // sort groups by efficiency
                groups.sort((o1, o2) ->
                {
                    float e1 = 0;
                    float e2 = 0;

                    for (Client client : o1)
                    {
                        e1 += client.getEfficiency();
                    }

                    for (Client client : o2)
                    {
                        e2 += client.getEfficiency();
                    }

                    return e1 > e2 ? -1 : 1;
                });

                if (Log.INFO) Log.info("Max group size: " + maxGroupSize);
            }
            else
            {
                if (Log.INFO) Log.info("Not enough players to group!");
            }
        }
        else
        {
            if (Log.INFO) Log.info("Not enough players to group!");
        }

        if (groups.size > 0)
        {
            if (Log.INFO) Log.info("Groups: " + groups.size);

            for (Array<Client> group : groups)
            {
                if (Log.INFO) Log.info("*: " + group.size);

                // sort each group inside, so weak player at bottom will be split in case of overflow
                group.sort((o1, o2) -> o1.getEfficiency() > o2.getEfficiency() ? -1 : 1);
            }
        }

        Array<Client> clients = new Array<>();
        for (Client client : clientList.values())
        {
            if (client.isInitialized())
            {
                boolean inGroupAlready = false;

                for (Array<Client> group : groups)
                {
                    if (group.indexOf(client, true) >= 0)
                    {
                        inGroupAlready = true;
                        break;
                    }
                }

                if (!inGroupAlready)
                {
                    clients.add(client);
                }
            }
        }

        if (clients.size > 0)
        {
            if (Log.INFO) Log.info("Separate players: " + clients.size);
            clients.sort((o1, o2) -> o1.getEfficiency() > o2.getEfficiency() ? -1 : 1);
        }

        clientList.getTeams().clear();

        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (team == null)
                continue;

            if (team instanceof SpectatorTeam)
            {
                continue;
            }

            Array<Client> teamClients = new Array<>();
            clientList.getTeams().put(team, teamClients);
        }

        // fill up the groups first

        for (Array<Client> group : groups)
        {
            Team bestTeam = clientList.getMostFreeTeam();

            if (bestTeam != null)
            {
                Array<Client> teamClients = clientList.getTeams().get(bestTeam);

                if (teamClients.size + group.size >= playersPerTeam + getBalanceValue())
                {
                    // adding a group would cause overflow, so we need to split group into pieces

                    int canTake = playersPerTeam - teamClients.size - 1 + getBalanceValue();

                    for (Client client : group)
                    {
                        if (client.getTeam() instanceof SpectatorTeam)
                        {
                            continue;
                        }

                        if (canTake > 0)
                        {
                            canTake--;

                            teamClients.add(client);
                            client.teamChanged(bestTeam);
                            client.setTeam(bestTeam);

                            if (client.isAlive())
                            {
                                client.kill();
                            }
                        }
                        else
                        {
                            // put players that causing overflow to the rest

                            clients.add(client);
                        }
                    }
                }
                else
                {
                    for (Client client : group)
                    {
                        teamClients.add(client);
                        client.teamChanged(bestTeam);
                        client.setTeam(bestTeam);

                        if (client.isAlive())
                        {
                            client.kill();
                        }
                    }
                }
            }
        }

        // fill up the rest of players

        for (Client client : clients)
        {
            if (client.getTeam() instanceof SpectatorTeam)
            {
                continue;
            }

            Team bestTeam = clientList.getMostFreeTeam();

            if (bestTeam != null)
            {
                Array<Client> teamClients = clientList.getTeams().get(bestTeam);

                teamClients.add(client);
                client.teamChanged(bestTeam);
                client.setTeam(bestTeam);

                if (client.isAlive())
                {
                    client.kill();
                }
            }
        }
    }
}
