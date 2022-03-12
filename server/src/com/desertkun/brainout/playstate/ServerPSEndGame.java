package com.desertkun.brainout.playstate;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.ChatMsg;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.content.shop.SlotItem;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.events.PlayerWonEvent;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.server.ServerController;
import com.desertkun.brainout.server.mapsource.MapSetSource;
import com.desertkun.brainout.server.mapsource.MapSource;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.SocialService;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ServerPSEndGame extends PlayStateEndGame
{
    private float deliverRewards;

    public ServerPSEndGame()
    {
        deliverRewards = 2.0f;
    }

    public void generateVotesMaps(int count)
    {
        MapSource mapSource = BrainOutServer.Controller.getMapSource();

        if (!(mapSource instanceof MapSetSource))
            return;

        MapSetSource mapSetSource = (MapSetSource) mapSource;

        Array<MapSetSource.QueuedItem> maps = mapSetSource.getRandomMaps(count);

        votesMaps = new Array<>();

        for (MapSetSource.QueuedItem map : maps)
        {
            votesMaps.add(new VoteMap(map.map.name, map.mode.mode, 0));
        }
    }

    public void mapVoted(int clientId, int voteMapId)
    {
        if (votedPlayers != null && votedPlayers.contains(clientId)) return;

        if (votesMaps == null || getRestartIn() < VOTING_RESULTS_TIME) return;

        VoteMap voteMap = votesMaps.get(voteMapId);
        if (voteMap != null)
        {
            voteMap.votes++;
            if (votedPlayers == null)
            {
                votedPlayers = new HashSet<>();
            }
            votedPlayers.add(clientId);

            BrainOutServer.Controller.playStateUpdated();
        }
    }

    public static class ServerGameResult extends GameResult
    {
        private Array<Client> rewardClients;
        private GameMode.ID gameMode;

        public ServerGameResult()
        {
            rewardClients = new Array<>();
        }

        public void setGameMode(GameMode.ID gameMode)
        {
            this.gameMode = gameMode;
        }

        public GameMode.ID getGameMode()
        {
            return gameMode;
        }

        public void generateInstrumentInfo()
        {
            getPlayerInstruments().clear();

            Slot primarySlot = BrainOutServer.ContentMgr.get("slot-primary", Slot.class);

            if (primarySlot == null)
                return;

            for (ObjectMap.Entry<Integer, Client> client : BrainOutServer.Controller.getClients())
            {
                if (!(client.value instanceof PlayerClient))
                    continue;

                PlayerClient playerClient = ((PlayerClient) client.value);

                SlotItem.Selection selection = playerClient.getShopCart().getItem(primarySlot);

                if (selection instanceof InstrumentSlotItem.InstrumentSelection)
                {
                    InstrumentSlotItem.InstrumentSelection isi =
                            ((InstrumentSlotItem.InstrumentSelection) selection);

                    getPlayerInstruments().put(client.key, isi.getInfo());
                }
            }
        }

        public Array<Client> getRewardClients()
        {
            return rewardClients;
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (votesMaps != null && votesMaps.size > 0 && winningMapIndex < 0 && getRestartIn() - VOTING_RESULTS_TIME < 0)
        {
            int bestResult = 0;

            for (VoteMap map : votesMaps)
            {
                if (map.votes > bestResult) bestResult = map.votes;
            }

            Array<VoteMap> winningMaps;

            if (bestResult == 0)
            {
                winningMaps = votesMaps;
            } else
            {
                winningMaps = new Array<>();

                for (VoteMap map : votesMaps)
                {
                    if (map.votes >= bestResult) winningMaps.add(map);
                }
            }

            if (winningMaps.size == 1)
                winningMapIndex = votesMaps.indexOf(winningMaps.pop(), true);
            else if (winningMaps.size > 1)
            {
                VoteMap winner = winningMaps.get(MathUtils.random(winningMaps.size - 1));
                winningMapIndex = votesMaps.indexOf(winner, true);
            }

            if (winningMapIndex >= 0)
            {
                VoteMap winningMap = votesMaps.get(winningMapIndex);
                BrainOutServer.Controller.getMapSource().insert(winningMap.mapName, winningMap.mapMode.name());
                BrainOutServer.Controller.playStateUpdated();
            }
        }

        if (getRestartIn() < 0)
        {
            BrainOutServer.PackageMgr.unloadPackages(true);
            BrainOutServer.Controller.resetSettings();
            BrainOutServer.Controller.next(this::switched);
        }

        if (deliverRewards > 0)
        {
            deliverRewards -= dt;

            if (deliverRewards <= 0)
            {
                deliverRewards();
            }
        }
    }

    private void switched(boolean success)
    {
        if (success)
        {
            if (Log.INFO) Log.info("Switched!");
        }
        else
        {
            if (Log.ERROR) Log.info("Failed to switch!");
        }

        BrainOutServer.Controller.updateRoomSettings();
        BrainOutServer.Controller.applyInitRoomSettings();

        Array<Client> toRemove = new Array<>();
        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (client instanceof PlayerClient)
            {
                client.reset();
            }
            else if (client instanceof BotClient)
            {
                toRemove.add(client);
            }
        }

        if (toRemove.size > 0)
        {
            for (Client client : toRemove)
            {
                BrainOutServer.Controller.getClients().releaseClient(client, false);
            }

            BrainOutServer.Controller.getClients().updateBalance(false);
            BrainOutServer.Controller.getClients().ensureBots();
        }
    }

    private void deliverRewards()
    {
        if (Log.INFO) Log.info("Delivering reward...");

        ServerController C = BrainOutServer.Controller;

        ServerGameResult gameResult = ((ServerGameResult) getGameResult());

        if (C.isRatingEnabled(false))
        {
            deliverRating();
        }

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);

                playerClient.setTookPartInWarmup(false);

                if (playerClient.isInitialized())
                {
                    if (gameResult.hasTeamWon())
                    {
                        if (playerClient.getTeam() == gameResult.getTeamWon())
                        {
                            playerClient.addStat(Constants.Stats.GAMES_WON, 1);
                            playerClient.addStat(Constants.Stats.GAMES_WON + "-" +
                                    gameResult.getGameMode().toString(), 1);
                            playerClient.storeEvents();

                            BrainOutServer.EventMgr.sendDelayedEvent(PlayerWonEvent.obtain(playerClient));
                        }
                        else
                        {
                            playerClient.addStat(Constants.Stats.GAMES_LOST, 1);
                            playerClient.addStat(Constants.Stats.GAMES_LOST + "-" +
                                    gameResult.getGameMode().toString(), 1);
                            playerClient.storeEvents();
                        }
                    }
                    else if (gameResult.hasPlayerWon())
                    {
                        if (playerClient.getId() == gameResult.getPlayerWon())
                        {
                            playerClient.addStat(Constants.Stats.GAMES_WON, 1);
                            playerClient.addStat(Constants.Stats.GAMES_WON + "-" +
                                gameResult.getGameMode().toString(), 1);
                            playerClient.storeEvents();
                        }
                    }

                    ClientProfile profile = playerClient.getProfile();

                    if (profile != null)
                    {
                        profile.setDirty();
                    }

                    ModePayload modePayload = playerClient.getModePayload();

                    if (modePayload != null)
                    {
                        modePayload.release();
                        playerClient.setModePayload(null);
                    }
                }
            }
        }

        for (Client rewardClient : gameResult.getRewardClients())
        {
            BrainOutServer.EventMgr.sendDelayedEvent(PlayerWonEvent.obtain(rewardClient));
        }

        if (BrainOutServer.getInstance().isClanWar() && gameResult.hasTeamWon())
        {
            if (Log.INFO) Log.info("Clan war has concluded");

            String clanWon = null;
            String clanLost = null;

            for (ObjectMap.Entry<String, Team> entry : BrainOutServer.Controller.getClanTeams())
            {
                if (entry.value == gameResult.getTeamWon())
                {
                    clanWon = entry.key;
                }
                else
                {
                    clanLost = entry.key;
                }
            }

            LoginService loginService = LoginService.Get();
            SocialService socialService = SocialService.Get();

            if (clanWon != null && clanLost != null && loginService != null && socialService != null)
            {
                if (Log.INFO) Log.info("Clan won: " + clanWon + " clan lost: " + clanLost);

                ObjectMap<String, String> clanNames = new ObjectMap<>();
                ObjectMap<String, String> clanAvatars = new ObjectMap<>();

                for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
                {
                    Client client = entry.value;

                    if (client.getTeam() instanceof SpectatorTeam)
                        continue;

                    if (client instanceof PlayerClient)
                    {
                        PlayerClient playerClient = ((PlayerClient) client);

                        if (playerClient.isParticipatingClan())
                        {
                            String clanId = playerClient.getClanId();

                            clanNames.put(clanId, playerClient.getClanName());
                            clanAvatars.put(clanId, playerClient.getClanAvatar());
                        }
                    }
                }

                long now = System.currentTimeMillis() / 1000L;

                Map<String, JSONObject> profiles = new HashMap<>();

                {
                    JSONObject updateA = new JSONObject();

                    JSONObject record = new JSONObject();
                    record.put("result", "won");
                    record.put("clan-with", clanLost);
                    record.put("time", now);

                    if (clanNames.containsKey(clanLost))
                        record.put("clan-with-name", clanNames.get(clanLost));
                    if (clanAvatars.containsKey(clanLost))
                        record.put("clan-with-avatar", clanAvatars.get(clanLost));

                    JSONObject func = new JSONObject();
                    func.put("@func", "array_append");
                    func.put("@value", record);
                    func.put("@limit", 32);
                    func.put("@shift", true);

                    updateA.put("clan-war-history", func);

                    JSONObject stats = new JSONObject();

                    JSONObject statsTotal = new JSONObject();
                    statsTotal.put("@func", "++");
                    statsTotal.put("@value", 1);
                    stats.put("clan-war-total", statsTotal);

                    JSONObject statsWon = new JSONObject();
                    statsWon.put("@func", "++");
                    statsWon.put("@value", 1);
                    stats.put("clan-war-won", statsWon);

                    updateA.put("stats", stats);

                    profiles.put(clanWon, updateA);
                }

                {
                    JSONObject updateB = new JSONObject();

                    JSONObject record = new JSONObject();
                    record.put("result", "lost");
                    record.put("clan-with", clanWon);
                    record.put("time", now);

                    if (clanNames.containsKey(clanWon))
                        record.put("clan-with-name", clanNames.get(clanWon));
                    if (clanAvatars.containsKey(clanWon))
                        record.put("clan-with-avatar", clanAvatars.get(clanWon));

                    JSONObject func = new JSONObject();
                    func.put("@func", "array_append");
                    func.put("@value", record);
                    func.put("@limit", 32);
                    func.put("@shift", true);

                    updateB.put("clan-war-history", func);

                    JSONObject stats = new JSONObject();

                    JSONObject statsTotal = new JSONObject();
                    statsTotal.put("@func", "++");
                    statsTotal.put("@value", 1);

                    stats.put("clan-war-total", statsTotal);

                    JSONObject statsWon = new JSONObject();
                    statsWon.put("@func", "++");
                    statsWon.put("@value", 1);
                    stats.put("clan-war-lost", statsWon);

                    updateB.put("stats", stats);

                    profiles.put(clanLost, updateB);
                }

                if (Log.INFO) Log.info("Updating clan profiles: " + profiles.toString());
                
                socialService.updateGroupBatchProfiles(
                    loginService.getCurrentAccessToken(), profiles, true,
                    (service, request, result, updatedProfiles) ->
                {
                    if (result != Request.Result.success)
                    {
                        if (Log.ERROR) Log.error("Failed to update clanwar history: " + result.toString());
                    }
                });
            }
        }

        complete();
    }

    private void complete()
    {
        if (BrainOutServer.Controller.isShutdownRequired() || BrainOutServer.getInstance().isClanWar() ||
                BrainOutServer.Controller.isFreePlay())
        {
            BrainOutServer.TriggerShutdown();
        }
        else
        {
            BrainOutServer.Controller.checkDeployment();
        }
    }

    private void deliverRating()
    {
        GameResult gameResult = getGameResult();

        if (gameResult.hasTeamWon())
        {
            if (gameResult.getTeamWon() != null)
            {
                Team won = gameResult.getTeamWon();

                for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
                {
                    Client client = entry.value;

                    if (client instanceof PlayerClient)
                    {
                        PlayerClient playerClient = ((PlayerClient) client);

                        if (playerClient.getTeam() instanceof SpectatorTeam)
                        {
                            continue;
                        }

                        if (playerClient.getTeam() == won)
                        {
                            addRating(playerClient);
                        }
                        else
                        {
                            removeRating(playerClient);
                        }
                    }
                }
            }
        }
        else if (gameResult instanceof ServerGameResult)
        {
            ServerGameResult serverGameResult = ((ServerGameResult) gameResult);

            if (serverGameResult.hasPlayerWon())
            {
                for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
                {
                    Client client = entry.value;

                    if (client instanceof PlayerClient)
                    {
                        PlayerClient playerClient = ((PlayerClient) client);

                        if (playerClient.getTeam() instanceof SpectatorTeam)
                        {
                            continue;
                        }

                        if (serverGameResult.getRewardClients().indexOf(playerClient, true) >= 0)
                        {
                            addRating(playerClient);
                        } else
                        {
                            removeRating(playerClient);
                        }
                    }
                }
            }
        }
    }

    private void removeRating(PlayerClient playerClient)
    {
        playerClient.removeRating(ServerConstants.Rating.LOST_REMOVE_RATING);

        playerClient.sendTCP(
            new ChatMsg("{MP_SERVER}", "{MP_PLAYER_RATING_LOST," +
                    ServerConstants.Rating.LOST_REMOVE_RATING + "}", "server",
                ServerConstants.Chat.COLOR_IMPORTANT, -1)
        );
    }

    private void addRating(PlayerClient playerClient)
    {
        playerClient.addRating(ServerConstants.Rating.WON_ADD_RATING);

        playerClient.sendTCP(
                new ChatMsg("{MP_SERVER}", "{MP_PLAYER_RATING_EARNED," +
                        ServerConstants.Rating.WON_ADD_RATING + "}", "server",
                        ServerConstants.Chat.COLOR_IMPORTANT, -1)
        );
    }
}
