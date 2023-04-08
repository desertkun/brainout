package com.desertkun.brainout.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.msg.client.RequestMsg;
import com.desertkun.brainout.common.msg.server.RequestErrorMsg;
import com.desertkun.brainout.common.msg.server.RequestSuccessMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.PersonalContainer;
import com.desertkun.brainout.content.active.RealEstateItem;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.components.RecipeComponent;
import com.desertkun.brainout.content.components.UniqueComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.Resource;
import com.desertkun.brainout.content.consumable.impl.DecayConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.consumable.impl.RealEstateConsumableItem;
import com.desertkun.brainout.content.consumable.impl.RealEstateItemConsumableItem;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.Shop;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerFreeplayMap;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.EnterPremisesDoorData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.battlepass.BattlePassTaskData;
import com.desertkun.brainout.data.components.RealEstateItemComponentData;
import com.desertkun.brainout.data.components.ServerEnterRealEstateComponentData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.data.components.ServerWeaponComponentData;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.mode.*;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.online.*;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSEmpty;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.server.mapsource.Editor2MapSource;
import com.desertkun.brainout.utils.MarketUtils;
import com.desertkun.brainout.utils.RealEstateInfo;
import com.desertkun.brainout.utils.SteamAPIUtil;
import com.esotericsoftware.minlog.Log;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.*;
import org.anthillplatform.runtime.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PlayerHandlers implements Disposable
{
    private PlayerClient playerClient;
    private ObjectMap<String, RequestHandler> requestHandlers;
    private ClientRequest requestResponse;

    @Override
    public void dispose()
    {
        BrainOutServer.PostRunnable(() ->
        {
            playerClient = null;
            requestHandlers.clear();
            requestResponse = null;
        });
    }

    public interface ClientRequest
    {
        void success(int id, JSONObject args);
        void error(int id, String reason);
    }

    public interface RequestHandler
    {
        void handle(JSONObject args, int id, ClientRequest request);
    }
    
    public PlayerHandlers(PlayerClient playerClient)
    {
        this.playerClient = playerClient;
        this.requestHandlers = new ObjectMap<>();
        
        this.requestResponse = new ClientRequest()
        {
            @Override
            public void success(int id, JSONObject args)
            {
                playerClient.sendTCP(new RequestSuccessMsg(args, id));
            }

            @Override
            public void error(int id, String reason)
            {
                playerClient.sendTCP(new RequestErrorMsg(reason, id));
            }
        };

        setupRequestHandlers();
    }

    public ClientProfile getProfile()
    {
        return playerClient.getProfile();
    }
    
    public void handleRequest(String method, RequestHandler handler)
    {
        requestHandlers.put(method, handler);
    }

    private void setupRequestHandlers()
    {
        handleRequest("change_team", this::handleChangeTeam);
        handleRequest("create_clan", this::handleCreateClan);
        handleRequest("leave_clan", this::handleLeaveClan);
        handleRequest("donate_clan", this::handleDonateClan);
        handleRequest("send_resources_to_member", this::handleSendResourceToMember);
        handleRequest("change_member_permissions", this::handleChangeMemberPermissions);
        handleRequest("transfer_ownership", this::handleTransferOwnership);
        handleRequest("kick_clan_member", this::handleKickClanMember);
        handleRequest("join_clan", this::handleJoinClan);
        handleRequest("join_clan_event", this::handleJoinClanEvent);
        handleRequest("invite_to_clan", this::handleInviteToClan);
        handleRequest("respond_clan_invite", this::handleInviteClanResponse);
        handleRequest("request_join_clan", this::handleRequestJoinClan);
        handleRequest("change_clan_avatar", this::handleChangeAvatarClan);
        handleRequest("change_clan_summary", this::handleChangeClanSummary);
        handleRequest("change_clan_description", this::handleChangeClanDescription);
        handleRequest("engage_clan_conflict", this::handleEngageClanConflict);
        handleRequest("cancel_clan_conflict", this::handleCancelClanConflict);
        handleRequest("check_conflict", this::handleCheckClanConflict);
        handleRequest("clan_engage_result", this::handleEngageClanResult);
        handleRequest("follow_clan_avatar", this::handleFollowAvatarClan);
        handleRequest("respond_clan_request", this::handleRespondToClanRequest);
        handleRequest("switch_profile_badge", this::handleSwitchProfileBadge);
        handleRequest("freeplay_partner_again", this::handleFreePlayPlayWithPartnerAgain);
        handleRequest("freeplay_weapon_skin", this::handleFreePlayPlayUpdateWeaponSkin);
        handleRequest("freeplay_weapon_upgrade", this::handleFreePlayPlayInstallUpgrade);
        handleRequest("start_shooting_range", this::handleLobbyStartShootingRange);
        handleRequest("reactivate", this::handleReactivate);
        handleRequest("deactivate", this::handleDeactivate);
        handleRequest("editor2_create", this::handleEditor2CreateMap);
        handleRequest("editor2_open_map", this::handleEditor2OpenMap);
        handleRequest("purchase_offline_item", this::handlePurchaseOfflineItem);
        handleRequest("purchase_ownable", this::handlePurchaseOwnable);
        handleRequest("skip_contract", this::handleSkipContract);
        handleRequest("redeem_contract_reward", this::handleRedeemContractReward);
        handleRequest("ban", this::handleBanPlayer);
        handleRequest("withdraw_market_item", this::withdrawMarketItem);
        handleRequest("destroy_market_item", this::destroyMarketItem);
        handleRequest("destroy_rs_market_item", this::destroyMarketRealEstateItem);
        handleRequest("put_market_item", this::putMarketItem);
        handleRequest("put_market_rs_item", this::putMarketRealEstateItem);
        handleRequest("withdraw_market_rs_item", this::withdrawMarketRealEstateItem);
        handleRequest("get_market_item_container", this::getMarketItemContainer);
        handleRequest("destroy_inventory_item", this::destroyIntvenvoryItem);
        handleRequest("withdraw_market_ru", this::withdrawMarketRU);
        handleRequest("put_market_ru", this::putMarketRU);
        handleRequest("new_market_order", this::newMarketOrder);
        handleRequest("fulfill_market_order", this::fulfillMarketOrder);
        handleRequest("assemble_rs_item", this::assembleRealEstateItem);
        handleRequest("cancel_market_order", this::cancelMarketOrder);
        handleRequest("generate_premises", this::generatePremises);
        handleRequest("real_estate_place_object", this::rsPlaceObject);
        handleRequest("real_estate_remove_object", this::rsRemoveObject);
        handleRequest("claim_battle_task_reward", this::clainBattleTaskReward);
        handleRequest("redeem_bp_reward", this::clainBattleStageReward);

        if (!BrainOut.OnlineEnabled())
        {
            handleRequest("offline_force_unlock", this::handleOfflineForceUnlock);
        }
    }

    private void handleChangeTeam(JSONObject args, int id, ClientRequest request)
    {
        String teamName = args.optString("team", null);

        if (teamName == null)
        {
            request.error(id, "Welp");
            return;
        }

        if (BrainOutServer.Settings.getZone() != null)
        {
            request.error(id, "Cannot change team on global conflict");
            return;
        }

        Team team = BrainOutServer.ContentMgr.get(teamName, Team.class);

        if (team != null)
        {
            GameMode gameMode = BrainOutServer.Controller.getGameMode();

            if (gameMode != null && !gameMode.allowTeamChange())
            {
                request.error(id, "Not allowed");
                return;
            }

            if (gameMode != null && gameMode.isAboutToEnd())
            {
                request.error(id, "MENU_GAME_IS_ABOUT_TO_END");
            }
            else
            {
                if (team instanceof SpectatorTeam)
                {
                    BrainOutServer.Controller.getClients().setClientTeam(playerClient, team, false, false);

                    playerClient.kill();
                    request.success(id, new JSONObject());
                }
                else
                {
                    if (BrainOutServer.Controller.getClients().setClientTeam(playerClient, team,
                        !playerClient.isSpectator()))
                    {
                        playerClient.kill();
                        request.success(id, new JSONObject());
                    }
                    else
                    {
                        request.error(id, "MENU_TEAM_IS_FULL");
                    }
                }

            }
        }
        else
        {
            request.error(id, "No such team");
        }
    }

    private void rsPlaceObject(JSONObject args, int id, ClientRequest request)
    {
        if (MARKET_LOCK.contains(playerClient.getId()))
        {
            request.error(id, "Locked");
            return;
        }

        String mapName = args.optString("map", null);
        if (mapName == null)
        {
            request.error(id, "Welp");
            return;
        }

        ServerFreeplayMap map = Map.Get(mapName, ServerFreeplayMap.class);
        if (map == null)
        {
            request.error(id, "Welp");
            return;
        }

        RealEstateInfo rs = map.getRealEstateInfo();
        if (rs == null)
        {
            request.error(id, "Welp");
            return;
        }

        if (!playerClient.getAccount().equals(rs.owner))
        {
            request.error(id, "Welp");
            return;
        }

        int x = args.optInt("x", -1);
        int y = args.optInt("y", -1);
        if (x <= 0 || y <= 0)
        {
            request.error(id, "Welp");
            return;
        }

        String name = args.optString("name", null);
        JSONObject payload = args.optJSONObject("payload");

        if (name == null || payload == null)
        {
            request.error(id, "Welp");
            return;
        }

        ConsumableRecord r = MarketUtils.MarketObjectToConsumableRecord(name, payload, 1);
        if (r == null)
        {
            request.error(id, "Welp");
            return;
        }

        if (!(r.getItem() instanceof RealEstateItemConsumableItem))
        {
            request.error(id, "Welp");
            return;
        }

        RealEstateItem c = ((RealEstateItemConsumableItem) r.getItem()).getContent();

        RealEstateInfo.PlaceObjectResult placeResult = rs.placeObject(mapName, x, y, c, null);
        if (placeResult == null)
        {
            request.error(id, "Welp");
            return;
        }

        if (playerClient.addMarketCooldown(1))
        {
            request.error(id, "MENU_ERROR_TRY_AGAIN");
            return;
        }

        MarketService marketService = MarketService.Get();
        List<MarketService.MarketItemEntry> entries = new LinkedList<>();

        // remove object we're placing
        entries.add(new MarketService.MarketItemEntry(name, -1, payload));
        // remove the original real estate
        entries.add(new MarketService.MarketItemEntry(rs.name, -1, placeResult.oldPayload.write()));
        // add a modified real estate
        entries.add(new MarketService.MarketItemEntry(rs.name, 1, placeResult.newPayload.write()));

        MARKET_LOCK.add(playerClient.getId());

        marketService.updateMarketItems("freeplay", entries, playerClient.getAccessToken(),
            (r1, result) -> BrainOutServer.PostRunnable(() ->
        {
            MARKET_LOCK.remove(playerClient.getId());

            if (result == Request.Result.success)
            {
                rs.payload = placeResult.newPayload;
                map.placeRealEstateObject(placeResult.newItemKey, x, y, c);
                request.success(id, new JSONObject());
            }
            else
            {
                request.error(id, result.toString());
            }
        }));
    }

    private void clainBattleTaskReward(JSONObject args, int id, ClientRequest request)
    {
        String event = args.optString("event", null);
        String task = args.optString("task", null);

        if (event == null || task == null)
        {
            request.error(id, "No data");
            return;
        }

        ServerBattlePassEvent e = null;

        for (ObjectMap.Entry<Integer, ServerEvent> entry : playerClient.getOnlineEvents())
        {
            if (entry.value instanceof ServerBattlePassEvent)
            {
                ServerBattlePassEvent bpe = ((ServerBattlePassEvent) entry.value);

                if (!event.equals(String.valueOf(bpe.getEvent().id)))
                {
                    continue;
                }

                e = bpe;
                break;
            }
        }

        if (e == null)
        {
            request.error(id, "No such event");
            return;
        }

        final ServerBattlePassEvent ee = e;

        BattlePassTaskData t = null;

        for (BattlePassTaskData data : e.getData().getTasks())
        {
            if (task.equals(data.getTaskKey()))
            {
                t = data;
                break;
            }
        }

        if (t == null)
        {
            request.error(id, "No such task");
            return;
        }

        if (t.isRewardRedeemed())
        {
            request.error(id, "Already redeemed");
            return;
        }

        final BattlePassTaskData tt = t;

        tt.redeemReward(playerClient.getAccessToken(), status ->
        {
            if (status)
            {
                playerClient.log("Battle pass event " + event + " task " + task + " reward redeemed");
                playerClient.getProfile().addStat("bp-earned", tt.getTask().getReward(), true);

                playerClient.getProfile().addBadge("battle-pass-rewards");
                ee.addScore(tt.getTask().getReward());

                JSONObject r = new JSONObject();
                r.put("newScore", (int)ee.score);
                request.success(id, r);
            }
            else
            {
                playerClient.log("Battle pass event " + event + " task " + task + " reward failed to redeem");
                request.error(id, "Unknown");
            }
        });
    }

    private void clainBattleStageReward(JSONObject args, int id, ClientRequest request)
    {
        String event = args.optString("event", null);
        int stage = args.optInt("stage", 0);
        int idx = args.optInt("idx", 0);
        boolean premium = args.optBoolean("premium", false);

        if (event == null)
        {
            request.error(id, "No data");
            return;
        }

        ServerBattlePassEvent e = null;

        for (ObjectMap.Entry<Integer, ServerEvent> entry : playerClient.getOnlineEvents())
        {
            if (entry.value instanceof ServerBattlePassEvent)
            {
                ServerBattlePassEvent bpe = ((ServerBattlePassEvent) entry.value);

                if (!event.equals(String.valueOf(bpe.getEvent().id)))
                {
                    continue;
                }

                e = bpe;
                break;
            }
        }

        if (e == null)
        {
            request.error(id, "No such event");
            return;
        }

        final ServerBattlePassEvent ee = e;

        if (premium && !playerClient.getProfile().hasItem(ee.getData().getBattlePass(), false))
        {
            request.error(id, "You need to purchase battle pass first.");
            return;
        }

        if (stage >= e.getStages().size || stage < 0)
        {
            request.error(id, "No such stage");
            return;
        }

        BattlePassEvent.CurrentStage cs = e.getCurrentStage(e.score);
        if (cs.completedIndex < stage)
        {
            request.error(id, "Not unlocked");
            return;
        }

        BattlePassEvent.Stage stageValue = e.getStages().get(stage);

        if (stageValue.isRewardRedeemed(premium, idx))
        {
            request.error(id, "Already redeemed");
            return;
        }

        Array<Reward> rw = premium ? stageValue.premiumRewards : stageValue.rewards;
        if (idx < 0 || idx >= rw.size)
        {
            request.error(id, "Unknown reward index");
            return;
        }

        ServerReward rww = ((ServerReward) rw.get(idx));

        stageValue.redeem(playerClient.getAccessToken(), event, premium, idx, status ->
        {
            if (status)
            {
                playerClient.log("Battle pass event " + event + " for stage " + stage + (premium ? "(premium) ":"") + " reward redeemed");
                playerClient.getProfile().addStat(premium ? "bp-rw-premium" : "bp-rw", 1, true);
                playerClient.updateEvents();
                rww.apply(playerClient, true);
                request.success(id, new JSONObject());
            }
            else
            {
                playerClient.log("Battle pass event " + event + " reward for stage " + stage + (premium ? "(premium) ":"") + " failed to redeem");
                request.error(id, "Unknown");
            }
        });
    }

    private void rsRemoveObject(JSONObject args, int id, ClientRequest request)
    {
        if (MARKET_LOCK.contains(playerClient.getId()))
        {
            request.error(id, "Locked");
            return;
        }

        String mapName = args.optString("map", null);
        if (mapName == null)
        {
            request.error(id, "Welp");
            return;
        }

        String key = args.optString("key", null);
        if (key == null)
        {
            request.error(id, "Welp");
            return;
        }

        ServerFreeplayMap map = Map.Get(mapName, ServerFreeplayMap.class);
        if (map == null)
        {
            request.error(id, "Welp");
            return;
        }

        int id_ = args.optInt("id", -1);
        ActiveData a = map.getActiveData(id_);
        if (a == null || (!(a.getCreator() instanceof RealEstateItem)))
        {
            request.error(id, "Welp");
            return;
        }

        RealEstateItemComponentData rsi = a.getComponent(RealEstateItemComponentData.class);
        if (rsi == null || (!rsi.getKey().equals(key)))
        {
            request.error(id, "Welp");
            return;
        }

        RealEstateInfo rs = map.getRealEstateInfo();
        if (rs == null)
        {
            request.error(id, "Welp");
            return;
        }

        if (!playerClient.getAccount().equals(rs.owner))
        {
            request.error(id, "Welp");
            return;
        }

        int x = args.optInt("x", -1);
        int y = args.optInt("y", -1);
        if (x <= 0 || y <= 0)
        {
            request.error(id, "Welp");
            return;
        }

        String cn = args.optString("c", null);
        RealEstateItem c = BrainOutServer.ContentMgr.get(cn, RealEstateItem.class);
        if (c == null)
        {
            request.error(id, "Welp");
            return;
        }

        RealEstateInfo.ExtrudedObject removeResult = rs.removeObject(key, mapName, x, y, c);
        if (removeResult == null)
        {
            request.error(id, "Welp");
            return;
        }

        if (playerClient.addMarketCooldown(1))
        {
            request.error(id, "MENU_ERROR_TRY_AGAIN");
            return;
        }

        MarketService marketService = MarketService.Get();
        List<MarketService.MarketItemEntry> entries = new LinkedList<>();

        if (removeResult.extruded != null)
        {
            // create an object we're restoring
            entries.add(new MarketService.MarketItemEntry("rsitem", 1, removeResult.extruded));
        }
        // remove the original real estate
        entries.add(new MarketService.MarketItemEntry(rs.name, -1, rs.payload.write()));
        // add a modified real estate
        entries.add(new MarketService.MarketItemEntry(rs.name, 1, removeResult.newPayload.write()));

        MARKET_LOCK.add(playerClient.getId());

        marketService.updateMarketItems("freeplay", entries, playerClient.getAccessToken(),
            (r1, result) -> BrainOutServer.PostRunnable(() ->
        {
            MARKET_LOCK.remove(playerClient.getId());

            if (result == Request.Result.success)
            {
                rs.payload = removeResult.newPayload;
                map.removeRealEstateObject(a);

                request.success(id, new JSONObject());
            }
            else
            {
                request.error(id, result.toString());
            }
        }));
    }

    private void generatePremises(JSONObject args, int id, ClientRequest request)
    {
        String location_ = args.optString("location", null);
        String id_ = args.optString("id", null);

        String enterDimension = args.optString("dim", null);
        int enterDimensionId = args.optInt("dim-id", -1);

        if (location_ == null || id_ == null)
        {
            request.error(id, "Welp");
            return;
        }

        Map map = Map.Get(enterDimension);
        if (map == null)
        {
            request.error(id, "Welp");
            return;
        }

        ActiveData enterDoorActive = map.getActives().get(enterDimensionId);

        if (!(enterDoorActive instanceof EnterPremisesDoorData))
        {
            request.error(id, "Welp");
            return;
        }

        EnterPremisesDoorData enterDoor = ((EnterPremisesDoorData) enterDoorActive);

        MarketService marketService = MarketService.Get();
        LoginService loginService = LoginService.Get();

        if (marketService == null || loginService == null)
        {
            return;
        }

        marketService.getMarketItems("freeplay", playerClient.getAccessToken(),
            (r, result, entries) -> BrainOutServer.PostRunnable(() ->
        {
            if (result != Request.Result.success)
            {
                request.error(id, "Failure");
                return;
            }

            boolean match = false;
            RealEstateConsumableItem rs = null;
            MarketService.MarketItemEntry originalEntry = null;

            for (MarketService.MarketItemEntry entry : entries)
            {
                if (!"realestate".equals(entry.name))
                {
                    continue;
                }

                ConsumableRecord record = MarketUtils.MarketObjectToConsumableRecord(entry.name, entry.payload, entry.amount);
                if (record == null)
                {
                    continue;
                }

                if (!(record.getItem() instanceof RealEstateConsumableItem))
                {
                    continue;
                }

                rs = ((RealEstateConsumableItem) record.getItem());
                originalEntry = entry;

                if (!location_.equals(rs.getLocation()))
                {
                    continue;
                }

                if (!id_.equals(rs.getId()))
                {
                    continue;
                }

                match = true;
                break;
            }

            if (match)
            {
                if (!location_.equals(enterDoor.location))
                {
                    request.error(id, "Locations doesn't match");
                    return;
                }

                Array<String> dms =
                    ServerEnterRealEstateComponentData.generatePremises(
                        enterDoor, playerClient, rs.getRealEstate().getMap(), location_, id_,
                        originalEntry.name, originalEntry.payload);

                if (dms == null)
                {
                    request.error(id, "Cannot generate premises");
                    return;
                }

                JSONObject ra = new JSONObject();
                JSONArray dim = new JSONArray();
                ra.put("dimensions", dim);
                ra.put("map", ServerMap.GetTargetMapForPersonalUse(location_, id_));

                for (String dm : dms)
                {
                    dim.put(dm);
                }

                request.success(id, ra);
            }
            else
            {
                String targetMap = ServerMap.GetTargetMapForPersonalUse(location_, id_);

                ServerMap serverMap = Map.Get(targetMap, ServerMap.class);
                if (serverMap != null)
                {
                    boolean hasFriend = false;

                    for (String k : serverMap.getSuitableForPersonalRequests())
                    {
                        PlayerClient found = null;

                        for (ObjectMap.Entry<Integer, Client> client : BrainOutServer.Controller.getClients())
                        {
                            if (client.value instanceof PlayerClient)
                            {
                                if (k.equals(((PlayerClient) client.value).getAccount()))
                                {
                                    found = ((PlayerClient) client.value);
                                    break;
                                }
                            }
                        }

                        if (found != null && found.getModePayload() instanceof FreePayload)
                        {
                            if (((FreePayload) found.getModePayload()).isFriend(playerClient))
                            {
                                hasFriend = true;
                                break;
                            }
                        }
                    }

                    if (hasFriend)
                    {
                        JSONObject ra = new JSONObject();
                        JSONArray dim = new JSONArray();
                        ra.put("dimensions", dim);

                        for (ServerMap m : Map.All(ServerMap.class))
                        {
                            if (m.getDimension().startsWith(targetMap))
                            {
                                m.setPersonalRequestOnly(playerClient.getAccount());
                                dim.put(m.getDimension());
                            }
                        }

                        ra.put("map", ServerMap.GetTargetMapForPersonalUse(location_, id_));
                        request.success(id, ra);
                        return;
                    }
                }
                request.error(id, "No premises");
            }
        }));
    }

    private void handleCreateClan(JSONObject args, int id, ClientRequest request)
    {
        if (!isClansUnlocked())
        {
            request.error(id, "Clans are locked for this player");
            return;
        }

        SocialService.Group.JoinMethod joinMethod;

        try
        {
            joinMethod = SocialService.Group.JoinMethod.valueOf(args.optString("join_method", ""));
        }
        catch (IllegalArgumentException ignored)
        {
            request.error(id, "bad_arguments");
            return;
        }

        String avatarKey = args.optString("avatar_key", "avatar");
        String clanName = args.optString("name", "");

        if (!validateClanName(clanName))
        {
            request.error(id, "MENU_BAD_CLAN_NAME");
            return;
        }

        clanName = BrainOutServer.getInstance().validateText(clanName);

        int price = BrainOutServer.Settings.getPrice("createClan");

        if (playerClient.getStat(Constants.Clans.CURRENCY_CREATE_CLAN, 0.0f) < price)
        {
            request.error(id, "MENU_NOT_ENOUGH_SKILLPOINTS");
            return;
        }

        String avatar = args.optString("avatar", "");

        String finalClanName = clanName;

        BrainOutServer.PostRunnable(() ->
            checkAvatar(avatar, new AvatarCheckCallback()
        {
            @Override
            public void success()
            {
                doCreateClan(finalClanName, avatar, avatarKey, joinMethod, id, request);
            }

            @Override
            public void failed()
            {
                request.error(id, "MENU_AVATAR_FAILED");
            }
        }));
    }

    private void doCreateClan(String name, String avatar,
                              String avatarKey, SocialService.Group.JoinMethod joinMethod,
                              int id, ClientRequest request)
    {


        SocialService socialService = SocialService.Get();

        if (socialService != null && playerClient.getAccessToken() != null)
        {
            JSONObject groupProfile = new JSONObject();

            if (!avatar.isEmpty())
            {
                groupProfile.put("avatar", avatar);
                groupProfile.put("avatar_key", avatarKey);
            }

            JSONObject participationProfile = generateNewParticipationProfile();

            socialService.createGroup(playerClient.getAccessToken(),
            name, joinMethod, ServerConstants.Online.GROUP_MAX_MEMBERS,
            groupProfile, participationProfile, true,
                (service, request1, result, clanId) ->
            {
                if (result == Request.Result.success)
                {
                    BrainOutServer.PostRunnable(() ->
                            doCreateClanSuccess(clanId, avatar, id, request));
                }
                else
                {
                    request.error(id, result.toString());
                }
            });
        }
        else
        {
            request.error(id, "No social service");
        }
    }

    private void doCreateClanSuccess(String clanId, String avatar, int id, ClientRequest request)
    {
        float amount = getProfile().getStats().get(Constants.Clans.CURRENCY_CREATE_CLAN, 0.0f);
        int need = BrainOutServer.Settings.getPrice("createClan");

        if (amount >= need)
        {
            float update = amount - need;

            playerClient.resourceEvent(-need, Constants.Clans.CURRENCY_CREATE_CLAN, "purchase", "create-clan");
            getProfile().getStats().put(Constants.Clans.CURRENCY_CREATE_CLAN, update);
            getProfile().setClan(clanId, avatar);
            getProfile().setDirty();

            playerClient.sendUserProfile();
        }
        else
        {
            request.error(id, "MENU_NOT_ENOUGH_SKILLPOINTS");
            return;
        }

        JSONObject result = new JSONObject();
        result.put("clan_id", clanId);
        request.success(id, result);
    }
    
    public void receive(RequestMsg msg)
    {
        BrainOutServer.PostRunnable(() -> 
        {
            RequestHandler handler = requestHandlers.get(msg.method);

            if (handler != null)
            {
                JSONObject args;

                try
                {
                    args = new JSONObject(msg.args);
                }
                catch (JSONException ignored)
                {
                    return;
                }

                handler.handle(args, msg.id, PlayerHandlers.this.requestResponse);
            }
        });
    }


    public JSONObject generateNewParticipationProfile()
    {
        JSONObject participationProfile = new JSONObject();

        if (playerClient.getAvatar() != null && !playerClient.getAvatar().isEmpty())
        {
            participationProfile.put("avatar", playerClient.getAvatar());
        }

        participationProfile.put("name", playerClient.getName());
        if (playerClient.getAccessTokenCredential() != null)
            participationProfile.put("credential", playerClient.getAccessTokenCredential());
        participationProfile.put("level", playerClient.getLevel(Constants.User.LEVEL, 1));
        participationProfile.put("rating", playerClient.getRating());

        return participationProfile;
    }

    private void handleJoinClan(JSONObject args, int id, ClientRequest request)
    {
        if (!isClansUnlocked())
        {
            request.error(id, "Clans are locked for this player");
            return;
        }

        float amount = getProfile().getStats().get(Constants.Clans.CURRENCY_JOIN_CLAN, 0.0f);
        int need = BrainOutServer.Settings.getPrice("joinClan");

        if (amount < need)
        {
            request.error(id, "MENU_NOT_ENOUGH_NUCLEAR_MATERIAL");
            return;
        }

        if (getProfile().isParticipatingClan())
        {
            request.error(id, "Already in clan");
            return;
        }

        String clanId = args.optString("clan_id");

        if (clanId == null)
        {
            request.error(id, "bad_arguments");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        JSONObject notify = new JSONObject();
        if (playerClient.getAvatar() != null && !playerClient.getAvatar().isEmpty())
        {
            notify.put("avatar", playerClient.getAvatar());
        }

        notify.put("name", playerClient.getName());

        socialService.getGroup(playerClient.getAccessToken(), clanId,
            (service, request1, result, group) ->
        {
            if (result == Request.Result.success)
            {
                String avatar = group.getProfile() != null ? group.getProfile().optString("avatar") : null;

                socialService.joinGroup(playerClient.getAccessToken(),
                    clanId, generateNewParticipationProfile(), notify,
                    (service2, request2, result2) ->
                {
                    if (result2 == Request.Result.success)
                    {
                        BrainOutServer.PostRunnable(() ->
                        {
                            doJoinClassSuccess(clanId, avatar, id, request);
                        });
                    }
                    else
                    {
                        request.error(id, request2.toString());
                    }
                });
            }
            else
            {
                request.error(id, result.toString());
            }
        });
    }


    private void handleJoinClanEvent(JSONObject args, int id, ClientRequest request)
    {
        if (!isClansUnlocked())
        {
            request.error(id, "Clans are locked for this player");
            return;
        }

        if (!playerClient.isParticipatingClan())
        {
            request.error(id, "Not in clan");
            return;
        }

        int need = BrainOutServer.Settings.getPrice("participateClanEvent");

        String eventId = args.optString("event_id");

        if (eventId == null)
        {
            request.error(id, "bad_arguments");
            return;
        }

        EventService eventService = EventService.Get();
        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        if (eventService == null)
        {
            request.error(id, "No event service");
            return;
        }

        socialService.getGroup(playerClient.getAccessToken(),
            playerClient.getClanId(),
        (service, request1, result, group) ->
        {
            if (result != Request.Result.success)
            {
                request.error(id, result.toString());
                return;
            }

            JSONObject old_stats = group.getProfile().optJSONObject("stats");

            if (old_stats == null)
            {
                request.error(id, "MENU_NOT_ENOUGH_NUCLEAR_MATERIAL");
                return;
            }

            float amount = old_stats.optInt(Constants.Clans.CURRENCY_CLAN_PARTICIPATE, 0);
            if (amount < need)
            {
                request.error(id, "MENU_NOT_ENOUGH_NUCLEAR_MATERIAL");
                return;
            }

            SocialService.Group.Participant me = group.getParticipants().get(playerClient.getAccessTokenAccount());

            if (me == null)
            {
                request.error(id, "Not in clan");
                return;
            }

            if (!group.getOwner().equals(playerClient.getAccessTokenAccount()) && !me.getPermissions().contains(
                    Clan.Permissions.PARTICIPATE_EVENT))
            {
                request.error(id, "You don't have permissions to do that");
                return;
            }

            JSONObject updateProfile = new JSONObject();

            {
                JSONObject stats = new JSONObject();

                {
                    JSONObject currency = new JSONObject();

                    currency.put("@func", "--/0");
                    currency.put("@value", need);

                    stats.put(Constants.Clans.CURRENCY_CLAN_PARTICIPATE, currency);
                }

                updateProfile.put("stats", stats);
            }

            socialService.updateGroupProfile(playerClient.getAccessToken(), playerClient.getClanId(), updateProfile, null, true,
                (service2, request2, result2, updatedProfile) ->
            {
                if (result2 != Request.Result.success)
                {
                    request.error(id, "Request failed (2)");
                    return;
                }

                JSONObject leaderboardInfo = new JSONObject();
                JSONObject profile = new JSONObject();

                {
                    leaderboardInfo.put("display_name", playerClient.getClanName());

                    if (!playerClient.getClanAvatar().isEmpty())
                    {
                        profile.put("avatar", playerClient.getClanAvatar());
                    }

                    leaderboardInfo.put("getProfile()", getProfile());
                    leaderboardInfo.put("expire_in", "241920");
                }

                eventService.joinGroupEvent(playerClient.getAccessToken(), eventId,
                    playerClient.getClanId(), 0, leaderboardInfo,
                    (service3, request3, result3) ->
                {
                    if (result3 == Request.Result.success)
                    {
                        JSONObject msg = new JSONObject();

                        msg.put("sender_name", playerClient.getName());
                        msg.put("action", "clan_participated");

                        playerClient.getMessageSession().sendMessage("social-group", playerClient.getClanId(),
                            "clan_participated", msg);

                        request.success(id, new JSONObject());
                    }
                    else
                    {
                        if (Log.ERROR) Log.error("Failed to join group event " + eventId + " by group " +
                            playerClient.getClanId() + ": " + result3.toString());

                        // give it back

                        JSONObject giveBack = new JSONObject();

                        {
                            JSONObject stats = new JSONObject();

                            {
                                JSONObject currency = new JSONObject();

                                currency.put("@func", "++");
                                currency.put("@value", need);

                                stats.put(Constants.Clans.CURRENCY_CLAN_PARTICIPATE, currency);
                            }

                            giveBack.put("stats", stats);
                        }

                        socialService.updateGroupProfile(playerClient.getAccessToken(),
                            playerClient.getClanId(), giveBack,
                            (service4, request4, result4, updatedProfile1) ->
                        {

                        });

                        request.error(id, "Failed to join group event");
                    }
                });
            });
        });
    }

    public boolean isClansUnlocked()
    {
        if (getProfile() == null)
            return false;

        OwnableContent clanPass = BrainOutServer.ContentMgr.get(
                Constants.Other.CLAN_PASS,
                OwnableContent.class);

        return clanPass.getLockItem() == null || clanPass.getLockItem().isUnlocked(getProfile());
    }

    private void handleDonateClan(JSONObject args, int id, ClientRequest request)
    {
        if (!isClansUnlocked())
        {
            request.error(id, "Clans are locked for this player");
            return;
        }

        float have = getProfile().getStats().get(Constants.User.NUCLEAR_MATERIAL, 0.0f);

        int amount = args.optInt("amount", 0);

        if (amount == 0)
        {
            request.error(id, "bad_arguments");
            return;
        }

        if (have < amount)
        {
            request.error(id, "MENU_NOT_ENOUGH_NUCLEAR_MATERIAL");
            return;
        }

        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "Not in clan");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        String clanId = getProfile().getClanId();

        JSONObject notify = new JSONObject();
        if (playerClient.getAvatar() != null && !playerClient.getAvatar().isEmpty())
        {
            notify.put("avatar", playerClient.getAvatar());
        }

        notify.put("name", playerClient.getName());
        notify.put("action", "donated");
        notify.put("amount", amount);

        JSONObject profile = new JSONObject();

        {
            JSONObject stats = new JSONObject();
            JSONObject func = new JSONObject();

            func.put("@func", "++");
            func.put("@value", amount);

            stats.put(Constants.User.NUCLEAR_MATERIAL, func);
            profile.put("stats", stats);
        }

        socialService.updateGroupProfile(playerClient.getAccessToken(), clanId, profile, notify, true,
            (service, request1, result, updatedProfile) ->
        {
            if (result == Request.Result.success)
            {
                JSONObject participationProfile = new JSONObject();
                JSONObject stats = new JSONObject();

                JSONObject func = new JSONObject();

                func.put("@func", "++");
                func.put("@value", amount);

                stats.put(Constants.Stats.DONATED, func);

                participationProfile.put("stats", stats);

                socialService.updateMyGroupParticipation(
                        playerClient.getAccessToken(), clanId, participationProfile, null, true,
                        (service2, request2, result2, updatedProfile2) ->
                                BrainOutServer.PostRunnable(() ->
                                        doClassDonateSuccess(amount, id, request)));
            }
            else
            {
                request.error(id, result.toString());
            }
        });
    }

    private void doClassDonateSuccess(int amount, int id, ClientRequest request)
    {
        float have = getProfile().getStats().get(Constants.User.NUCLEAR_MATERIAL, 0.0f);
        float update = have - amount;

        playerClient.resourceEvent(-amount, Constants.User.NUCLEAR_MATERIAL, "purchase", "donate-clan");
        getProfile().getStats().put(Constants.User.NUCLEAR_MATERIAL, update);
        getProfile().setDirty();
        playerClient.sendUserProfile();

        JSONObject args1 = new JSONObject();
        request.success(id, args1);
    }

    private void handleSendResourceToMember(JSONObject args, int id, ClientRequest request)
    {
        if (!isClansUnlocked())
        {
            request.error(id, "Clans are locked for this player");
            return;
        }

        if (playerClient.getMessageSession() == null || !playerClient.getMessageSession().isOpen())
        {
            request.error(id, "Please do that in the lobby.");
            return;
        }

        int amount = args.optInt("amount", 0);
        String sendTo = args.optString("account_id");

        if (amount == 0 || sendTo == null)
        {
            request.error(id, "bad_arguments");
            return;
        }

        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "Not in clan");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        String clanId = getProfile().getClanId();
        String accountId = playerClient.getAccessTokenAccount();

        socialService.getGroup(playerClient.getAccessToken(), clanId, (service, request1, result, group) ->
        {
            if (result != Request.Result.success)
            {
                request.error(id, result.toString());
                return;
            }

            SocialService.Group.Participant sendToMember = group.getParticipants().get(sendTo);

            if (sendToMember == null)
            {
                request.error(id, "Not such member.");
                return;
            }

            String sendToName = sendToMember.getProfile().optString("name", "Unknown");
            String sendToAvatar = sendToMember.getProfile().optString("avatar", null);

            SocialService.Group.Participant me = group.getParticipants().get(accountId);

            if (me == null)
            {
                request.error(id, "Not in this fraction.");
                return;
            }

            if (!(group.getOwner().equals(accountId) || me.hasPermission(Clan.Permissions.SEND_RESOURCES)))
            {
                request.error(id, "Not authorized");
                return;
            }

            {
                JSONObject stats = group.getProfile().optJSONObject("stats");

                if (stats == null)
                {
                    request.error(id, "No stats");
                    return;
                }

                int have = stats.optInt(Constants.User.NUCLEAR_MATERIAL, 0);

                if (amount > have)
                {
                    request.error(id, "Not enough nuclear material");
                    return;
                }
            }

            JSONObject notify = new JSONObject();
            if (sendToAvatar != null && !sendToAvatar.isEmpty())
            {
                notify.put("avatar", sendToAvatar);
            }

            notify.put("sender_name", playerClient.getName());
            notify.put("receiver_name", sendToName);
            notify.put("action", "resources_sent");
            notify.put("currency", Constants.User.NUCLEAR_MATERIAL);
            notify.put("amount", amount);

            JSONObject profile = new JSONObject();

            {
                JSONObject stats = new JSONObject();
                JSONObject func = new JSONObject();

                func.put("@func", "--/0");
                func.put("@value", amount);

                stats.put(Constants.User.NUCLEAR_MATERIAL, func);
                profile.put("stats", stats);
            }

            socialService.updateGroupProfile(playerClient.getAccessToken(), clanId, profile, notify, true,
                (service2, request2, status_updated, group_updated) ->
            {
                if (status_updated != Request.Result.success)
                {
                    request.error(id, status_updated.toString());
                    return;
                }

                if (sendTo.equals(playerClient.getAccount()))
                {
                    // if we have sent to ourselves, don't bother with messages
                    getProfile().addStat(Constants.User.NUCLEAR_MATERIAL, amount, true);

                    playerClient.notify(NotifyAward.nuclearMaterial,
                            amount, NotifyReason.nuclearMaterialReceived,
                            NotifyMethod.message, null);
                    playerClient.sendUserProfile();
                    getProfile().setDirty();
                }
                else
                {
                    JSONObject message = new JSONObject();

                    {
                        message.put("sender_name", playerClient.getName());
                        message.put("currency", Constants.User.NUCLEAR_MATERIAL);
                        message.put("amount", amount);
                        message.put("clan_id", playerClient.getClanId());
                        message.put("clan_name", group.getName());
                    }

                    Set<String> flags = new HashSet<>();
                    flags.add("remove_delivered");

                    playerClient.getMessageSession().sendMessage("user", sendTo, "resources", message, flags);
                }

                JSONObject participationProfile = new JSONObject();

                {
                    JSONObject stats = new JSONObject();

                    {
                        JSONObject func = new JSONObject();
                        func.put("@func", "++");
                        func.put("@value", amount);
                        stats.put(Constants.Stats.RESOURCES_RECEIVED, func);
                    }

                    participationProfile.put("stats", stats);
                }

                socialService.updateGroupParticipation(
                        playerClient.getAccessToken(), clanId, sendTo, participationProfile, null, true,
                        (service3, request3, status3, updatedProfile) ->
                                BrainOutServer.PostRunnable(() ->
                                        doClassSendResourcesSuccess(id, request)));
            });
        });
    }

    private void handleChangeMemberPermissions(JSONObject args, int id, ClientRequest request)
    {
        if (!isClansUnlocked())
        {
            request.error(id, "Clans are locked for this player");
            return;
        }

        if (playerClient.getMessageSession() == null || !playerClient.getMessageSession().isOpen())
        {
            request.error(id, "Please do that in the lobby.");
            return;
        }

        JSONArray permissions = args.optJSONArray("permissions");
        String memberId = args.optString("account_id");

        if (permissions == null || memberId == null)
        {
            request.error(id, "bad_arguments");
            return;
        }

        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "Not in clan");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        HashSet<String> permissions_ = new HashSet<>();

        for (int i = 0, t = permissions.length(); i < t; i++)
        {
            permissions_.add(permissions.getString(i));
        }

        String clanId = getProfile().getClanId();

        socialService.updateGroupParticipationPermissions(
            playerClient.getAccessToken(), clanId, memberId, permissions_, Clan.Permissions.ROLE_DEFAULT, null,
            (service, request1, result) ->
        {
            if (result == Request.Result.success)
            {
                request.success(id, new JSONObject());
            }
            else
            {
                request.error(id, result.toString());
            }
        });
    }

    private void handleTransferOwnership(JSONObject args, int id, ClientRequest request)
    {
        if (!isClansUnlocked())
        {
            request.error(id, "Clans are locked for this player");
            return;
        }

        if (playerClient.getMessageSession() == null || !playerClient.getMessageSession().isOpen())
        {
            request.error(id, "Please do that in the lobby.");
            return;
        }

        String memberId = args.optString("account_id");

        if (memberId == null)
        {
            request.error(id, "bad_arguments");
            return;
        }

        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "Not in clan");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        String clanId = getProfile().getClanId();

        socialService.transferOwnership(
                playerClient.getAccessToken(), clanId, memberId, 0, null,
                (service, request2, result) ->
                {
                    if (result == Request.Result.success)
                    {
                        request.success(id, new JSONObject());
                    }
                    else
                    {
                        request.error(id, result.toString());
                    }
                });
    }

    private void doClassSendResourcesSuccess(int id, ClientRequest request)
    {
        request.success(id, new JSONObject());
    }

    private void handleInviteClanResponse(JSONObject args, int id, ClientRequest request)
    {
        String clanId = args.optString("clan_id");
        String messageId = args.optString("message_id");
        String key = args.optString("key");
        String method = args.optString("method");

        if (clanId == null || messageId == null || key == null || method == null)
        {
            request.error(id, "bad_arguments");
            return;
        }

        if (method.equals("accept"))
        {
            if (getProfile().isParticipatingClan())
            {
                request.error(id, "MENU_ALREADY_PARTICIPATING_IN_CLAN");
                return;
            }

            float amount = getProfile().getStats().get(Constants.Clans.CURRENCY_JOIN_CLAN, 0.0f);
            int need = BrainOutServer.Settings.getPrice("joinClan");

            if (amount < need)
            {
                request.error(id, "MENU_NOT_ENOUGH_NUCLEAR_MATERIAL");
                return;
            }
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        socialService.getGroup(playerClient.getAccessToken(), clanId,
                (service, request1, result, group) ->
                {
                    String avatar = group.getProfile().optString("avatar", "");

                    if (result == Request.Result.success)
                    {
                        JSONObject participationProfile = generateNewParticipationProfile();

                        if (method.equals("accept"))
                        {
                            socialService.acceptGroupInvitation(
                                    playerClient.getAccessToken(), clanId, participationProfile, participationProfile, key,
                                    (service1, request2, joinResult) ->
                                    {
                                        BrainOutServer.PostRunnable(() ->
                                        {
                                            deleteSocialMessage(messageId);

                                            if (joinResult == Request.Result.success)
                                            {
                                                doJoinClassSuccess(clanId, avatar, id, request);
                                            } else
                                            {
                                                request.error(id, joinResult.toString());
                                            }
                                        });
                                    });
                        }
                        else
                        {
                            socialService.rejectGroupInvitation(playerClient.getAccessToken(), clanId, participationProfile, key,
                                    (service1, request2, joinResult) ->
                                    {
                                        BrainOutServer.PostRunnable(() ->
                                        {
                                            deleteSocialMessage(messageId);

                                            if (joinResult == Request.Result.success)
                                            {
                                                request.success(id, new JSONObject());
                                            } else
                                            {
                                                request.error(id, joinResult.toString());
                                            }
                                        });
                                    });
                        }
                    }
                    else
                    {
                        request.error(id, result.toString());
                    }
                });
    }

    private void handleInviteToClan(JSONObject args, int id, ClientRequest request)
    {
        if (getProfile() == null || !getProfile().isParticipatingClan())
        {
            request.error(id, "You are not in the clan");
            return;
        }

        String accountId = args.optString("account_id");

        if (accountId == null)
        {
            request.error(id, "bad_arguments");
            return;
        }

        String clanId = getProfile().getClanId();

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        JSONObject notify = generateNewParticipationProfile();

        socialService.getGroup(playerClient.getAccessToken(), clanId,
            (service, request1, result, group) ->
        {
            if (result == Request.Result.success)
            {
                notify.put("group_id", clanId);
                notify.put("group_name", group.getName());
                notify.put("group_avatar", group.getProfile().optString("avatar", ""));

                socialService.inviteToGroup(playerClient.getAccessToken(), clanId, accountId, 0, null, notify,
                    (service2, request2, inviteResult, key) ->
                {
                    if (inviteResult == Request.Result.success)
                    {
                        request.success(id, new JSONObject());
                    }
                    else
                    {
                        request.error(id, inviteResult.toString());
                    }
                });
            }
            else
            {
                request.error(id, result.toString());
            }
        });
    }

    private void handleFreePlayPlayUpdateWeaponSkin(JSONObject args, int id, ClientRequest request)
    {
        int object = args.optInt("object", -1);
        String skin = args.optString("skin", null);

        PlayerData playerData = playerClient.getPlayerData();
        if (playerData == null)
        {
            request.error(id, "Not alive");
            return;
        }

        if (skin == null)
        {
            request.error(id, "No skin field.");
            return;
        }

        Content c = BrainOutServer.ContentMgr.get(skin);

        if (!(c instanceof Skin))
        {
            request.error(id, "Not a skin");
            return;
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (!(gameMode instanceof GameModeFree))
        {
            request.error(id, "Not in FreePlay mode");
            return;
        }

        ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());

        if (!(serverRealization instanceof ServerFreeRealization))
        {
            request.error(id, "Not in FreePlay mode");
            return;
        }

        if (playerData.getCurrentInstrument() != null &&
            playerData.getCurrentInstrument().isForceSelect())
        {
            request.error(id, "Locked");
            return;
        }

        ServerFreeRealization free = ((ServerFreeRealization) serverRealization);

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        ConsumableRecord record = free.playerUpdateWeaponSkin(
            playerClient, playerData, poc, object, ((Skin) c));

        if (record != null)
        {
            JSONObject result = new JSONObject();
            result.put("record", record.getId());
            request.success(id, result);
        }
        else
        {
            request.error(id, "Failed");
        }
    }

    private void handleLobbyStartShootingRange(JSONObject args, int id, ClientRequest request)
    {
        String key = args.optString("key", null);

        if (key == null)
        {
            request.error(id, "No key field.");
            return;
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (!(gameMode instanceof GameModeLobby))
        {
            request.error(id, "Not in Lobby mode");
            return;
        }

        ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());

        if (!(serverRealization instanceof ServerLobbyRealization))
        {
            request.error(id, "Not in Lobby mode");
            return;
        }

        ServerLobbyRealization lobby = ((ServerLobbyRealization) serverRealization);

        boolean hasEvent = false;
        String weapon = "";

        for (ObjectMap.Entry<Integer, ServerEvent> entry : playerClient.getOnlineEvents())
        {
            if (!entry.value.getEvent().isValid())
                continue;

            if (entry.value.getEvent().taskAction.equals(Constants.Other.SHOOTING_RANGE_ACTION))
            {
                hasEvent = true;
                weapon = entry.value.getEvent().taskData;
            }
        }

        if (!hasEvent)
        {
            request.error(id, "No event");
            return;
        }

        if (lobby.startShootingRange(playerClient, key, weapon))
        {
            request.success(id, new JSONObject());
        }
        else
        {
            request.error(id, "Failed");
        }
    }

    private void handleReactivate(JSONObject args, int id, ClientRequest request)
    {
        if (getProfile() == null)
        {
            request.error(id, "No getProfile()");
            return;
        }

        getProfile().setDeactivated(0);
        getProfile().setDirty(true);

        request.success(id, new JSONObject());
    }

    private void handleEditor2CreateMap(JSONObject args, int id, ClientRequest request)
    {
        PlayState ps = BrainOutServer.Controller.getPlayState();

        if (!(ps instanceof ServerPSEmpty))
        {
            request.error(id, "Not in Empty state");
            return;
        }

        ServerPSEmpty empty = ((ServerPSEmpty) ps);

        if (empty.getNextMode() != GameMode.ID.editor2)
        {
            request.error(id, "Not in correct editor");
            return;
        }

        if (getProfile() == null)
        {
            request.error(id, "No getProfile()");
            return;
        }

        Constants.Editor.MapSize selectedMapSize = null;
        Background selectedMapBackground;

        {
            String size = args.optString("size", null);
            if (size == null)
            {
                request.error(id, "Bad size");
                return;
            }


            for (Constants.Editor.MapSize mapSize : Constants.Editor.SIZES)
            {
                if (mapSize.getID().equals(size))
                {
                    selectedMapSize = mapSize;
                    break;
                }
            }
        }

        if (selectedMapSize == null)
        {
            request.error(id, "Bad size");
            return;
        }

        {
            String backgroundId = args.optString("background", null);
            if (backgroundId == null)
            {
                request.error(id, "Bad background");
                return;
            }

            selectedMapBackground = BrainOutServer.ContentMgr.get(backgroundId, Background.class);
        }

        if (selectedMapBackground == null)
        {
            request.error(id, "Bad background");
            return;
        }

        Constants.Editor.MapSize finalSelectedMapSize =
                selectedMapSize;

        BrainOutServer.PostRunnable(() ->
        {
            BrainOutServer.Controller.setMapSource(
                new Editor2MapSource("custom", finalSelectedMapSize, selectedMapBackground));

            BrainOutServer.PackageMgr.unloadPackages(true);
            BrainOutServer.Controller.next(null);
        });

        request.success(id, new JSONObject());
    }

    private void handleEditor2OpenMap(JSONObject args, int id, ClientRequest request)
    {
        PlayState ps = BrainOutServer.Controller.getPlayState();

        if (!(ps instanceof ServerPSEmpty))
        {
            request.error(id, "Not in Empty state");
            return;
        }

        ServerPSEmpty empty = ((ServerPSEmpty) ps);

        if (empty.getNextMode() != GameMode.ID.editor2)
        {
            request.error(id, "Not in correct editor");
            return;
        }

        if (getProfile() == null)
        {
            request.error(id, "No getProfile()");
            return;
        }

        int timeUpdated = args.optInt("time_updated", 0);

        String workshopId = args.optString("workshop_id", null);
        if (workshopId == null)
        {
            request.error(id, "Bad workshop item");
            return;
        }

        InputStream inputStream = SteamAPIUtil.DownloadWorkshopMap(workshopId, timeUpdated);

        if (inputStream == null)
        {
            request.error(id, "Failed to download workshop item");
            return;
        }

        ObjectMap<String, String> custom = new ObjectMap<>();

        empty.uploadMap(inputStream, success ->
        {
            if (success)
            {
                Map map = Map.GetDefault();

                if (map != null)
                {
                    map.setCustom("workshop-item", workshopId);
                }

                request.success(id, new JSONObject());
            }
            else
            {
                request.error(id, "Failed to download map");
            }
        }, "custom", custom);
    }

    private void handleSkipContract(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            String taskId = args.optString("id");

            Contract contract = BrainOut.ContentMgr.get(taskId, Contract.class);
            if (contract == null || contract.getLockItem() == null)
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            if (playerClient.getProfile().getStats().get("ru", 0.0f) < contract.getSkipPrice())
            {
                request.error(id, "MENU_NOT_ENOUGH_RU");
                return;
            }

            playerClient.getProfile().descreaseStat("ru", contract.getSkipPrice());
            contract.getLockItem().completeDiff(playerClient.getProfile());
            playerClient.sendUserProfile();
            BrainOutServer.PostRunnable(() -> request.success(id, new JSONObject()));
        });
    }

    private void handleBanPlayer(JSONObject args, int id, ClientRequest request)
    {
        if (playerClient.getRights() != PlayerRights.mod && playerClient.getRights() != PlayerRights.admin)
        {
            BrainOutServer.PostRunnable(() -> request.error(id, "bad_access"));
            return;
        }

        BrainOutServer.PostRunnable(() ->
        {
            String account = args.optString("account");
            String reason = args.optString("reason");

            Date expires;
            try
            {
                expires = Utils.DATE_FORMAT.parse(args.optString("expires"));
            }
            catch (ParseException e)
            {
                BrainOutServer.PostRunnable(() -> request.error(id, "bad_date"));
                return;
            }

            GameService gameService = GameService.Get();
            LoginService loginService = LoginService.Get();
            if (gameService == null || loginService == null)
            {
                request.error(id, "no_service");
                return;
            }

            gameService.issueABan(loginService.getCurrentAccessToken(),
                account, expires, reason, (service, r, result) ->
            {
                if (result == Request.Result.success)
                {
                    BrainOutServer.PostRunnable(() ->
                    {
                        request.success(id, new JSONObject());

                        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
                        {
                            if (!(entry.value instanceof PlayerClient))
                                continue;

                            if (account.equals(((PlayerClient) entry.value).getAccessTokenAccount()))
                            {
                                entry.value.kick("Banned");
                                break;
                            }
                        }
                    });
                }
                else
                {
                    request.error(id, result.toString());
                }
            });
        });
    }


    private int getSlotWeaponsAmount(String slot, ConsumableContainer inventory)
    {
        return inventory.queryRecordsOfClassAmount(
                (weapon, record) -> weapon.getSlot() != null &&
                        weapon.getSlot().getID().equals(slot), Weapon.class);
    }

    static IntSet MARKET_LOCK = new IntSet();

    private void withdrawMarketItem(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String marketName = args.optString("market");
            if (!"freeplay".equals(marketName))
            {
                request.error(id, "Unsupported market");
                return;
            }

            String item = args.optString("item");

            if ("realestate".equals(item))
            {
                request.error(id, "Cannot do that.");
                return;
            }

            if ("rsitem".equals(item))
            {
                request.error(id, "Cannot do that.");
                return;
            }

            JSONObject payload = args.optJSONObject("payload");
            int amount = args.optInt("amount", 1);

            if (item == null || amount <= 0 || payload == null)
            {
                request.error(id, "Bad item");
                return;
            }

            MarketService marketService = MarketService.Get();
            if (marketService == null)
            {
                request.error(id, "no_service");
                return;
            }

            PlayerData playerData = playerClient.getPlayerData();
            if (playerData == null)
            {
                request.error(id, "Not alive");
                return;
            }

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            if (poc == null)
            {
                request.error(id, "No poc");
                return;
            }

            ConsumableRecord futureRecord = MarketUtils.MarketObjectToConsumableRecord(item, payload, amount);
            if (futureRecord == null)
            {
                request.error(id, "Cannot allocate item.");
                return;
            }

            UniqueComponent u = futureRecord.getItem().getContent().getComponent(UniqueComponent.class);

            if (u != null)
            {
                futureRecord.setAmount(1);
                for (ObjectMap.Entry<Integer, ConsumableRecord> entry : poc.getConsumableContainer().getData())
                {
                    Content c = entry.value.getItem().getContent();

                    UniqueComponent uc = c.getComponent(UniqueComponent.class);

                    if (uc != null && uc.getCategory().equals(u.getCategory()))
                    {
                        request.error(id, "Only one such item can be taken");
                        return;
                    }
                }
            }

            if (futureRecord.getItem().getContent() instanceof ConsumableContent)
            {
                if (!((ConsumableContent) futureRecord.getItem().getContent()).isStacks())
                    futureRecord.setAmount(1);
            }

            if (futureRecord.getItem() instanceof InstrumentConsumableItem)
            {
                InstrumentData ici = ((InstrumentConsumableItem) futureRecord.getItem()).getInstrumentData();
                futureRecord.setAmount(1);
                if (ici instanceof WeaponData)
                {
                    Weapon weapon = ((WeaponData) ici).getWeapon();

                    if (weapon.getSlot() != null)
                    {
                        String slotId = weapon.getSlot().getID();

                        switch (slotId)
                        {
                            case "slot-primary":
                            {
                                int primary = getSlotWeaponsAmount("slot-primary", poc.getConsumableContainer());

                                if (primary >= 2)
                                {
                                    request.error(id, "Too much primary");
                                    return;
                                }

                                break;
                            }
                            case "slot-secondary":
                            {
                                int secondary = getSlotWeaponsAmount("slot-secondary",
                                        poc.getConsumableContainer());

                                if (secondary >= 1)
                                {
                                    request.error(id, "Too much secondary");
                                    return;
                                }

                                break;
                            }
                        }
                    }
                }
            }

            if (futureRecord.getItem() instanceof DecayConsumableItem) futureRecord.setAmount(1);

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            int finalAmount = futureRecord.getAmount();

            List<MarketService.MarketItemEntry> entries = new ArrayList<>();
            MarketService.MarketItemEntry e = new MarketService.MarketItemEntry(item, -finalAmount, payload);
            entries.add(e);

            MARKET_LOCK.add(playerClient.getId());

            marketService.updateMarketItems(marketName, entries, playerClient.getAccessToken(),
                (r, result) -> BrainOutServer.PostRunnable(() ->
            {
                MARKET_LOCK.remove(playerClient.getId());

                if (result != Request.Result.success)
                {
                    request.error(id, "Cannot obtain item");
                    return;
                }

                ConsumableContainer cnt = poc.getConsumableContainer();
                futureRecord.setId(cnt.newId());

                cnt.putConsumable(futureRecord.getAmount(), futureRecord.getItem(), futureRecord.getQuality());

                playerClient.store();
                playerClient.sendConsumable();

                playerClient.log("Withdrew " + finalAmount + " items " + item + " of " + payload.toString());

                request.success(id, new JSONObject());
            }));
        });
    }

    private void withdrawMarketRealEstateItem(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String mapName = args.optString("map", null);
            String key = args.optString("key", null);
            String recordId = args.optString("record", null);
            int amount = args.optInt("amount", -1);

            if (mapName == null || key == null || recordId == null || amount <= 0)
            {
                request.error(id, "Welp");
                return;
            }

            ServerFreeplayMap map = Map.Get(mapName, ServerFreeplayMap.class);
            if (map == null)
            {
                request.error(id, "Welp");
                return;
            }

            RealEstateInfo rs = map.getRealEstateInfo();
            if (rs == null)
            {
                request.error(id, "Welp");
                return;
            }

            if (!playerClient.getAccount().equals(rs.owner))
            {
                request.error(id, "Welp");
                return;
            }

            MarketService marketService = MarketService.Get();
            if (marketService == null)
            {
                request.error(id, "no_service");
                return;
            }

            PlayerData playerData = playerClient.getPlayerData();
            if (playerData == null)
            {
                request.error(id, "Not alive");
                return;
            }

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            if (poc == null)
            {
                request.error(id, "No poc");
                return;
            }

            RealEstateInfo.WithdrawItemFromObjectResult wd = rs.withdrawItemFromObject(key, recordId, amount);
            if (wd == null)
            {
                request.error(id, "Cannot withdraw");
                return;
            }

            ConsumableRecord futureRecord = wd.record;

            if (futureRecord.getItem().getContent() instanceof ConsumableContent)
            {
                if (!((ConsumableContent) futureRecord.getItem().getContent()).isStacks())
                    futureRecord.setAmount(1);
            }

            if (futureRecord.getItem() instanceof InstrumentConsumableItem)
            {
                InstrumentData ici = ((InstrumentConsumableItem) futureRecord.getItem()).getInstrumentData();
                futureRecord.setAmount(1);
                if (ici instanceof WeaponData)
                {
                    Weapon weapon = ((WeaponData) ici).getWeapon();

                    if (weapon.getSlot() != null)
                    {
                        String slotId = weapon.getSlot().getID();

                        switch (slotId)
                        {
                            case "slot-primary":
                            {
                                int primary = getSlotWeaponsAmount("slot-primary", poc.getConsumableContainer());

                                if (primary >= 2)
                                {
                                    request.error(id, "Too much primary");
                                    return;
                                }

                                break;
                            }
                            case "slot-secondary":
                            {
                                int secondary = getSlotWeaponsAmount("slot-secondary",
                                        poc.getConsumableContainer());

                                if (secondary >= 1)
                                {
                                    request.error(id, "Too much secondary");
                                    return;
                                }

                                break;
                            }
                        }
                    }
                }
            }

            if (futureRecord.getItem() instanceof DecayConsumableItem)
                futureRecord.setAmount(1);

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            int finalAmount = futureRecord.getAmount();

            List<MarketService.MarketItemEntry> entries = new ArrayList<>();
            // remove the original real estate
            entries.add(new MarketService.MarketItemEntry(rs.name, -1, wd.oldPayload.write()));
            // add a modified real estate
            entries.add(new MarketService.MarketItemEntry(rs.name, 1, wd.newPayload.write()));

            MARKET_LOCK.add(playerClient.getId());

            marketService.updateMarketItems("freeplay", entries, playerClient.getAccessToken(),
                (r, result) -> BrainOutServer.PostRunnable(() ->
            {
                MARKET_LOCK.remove(playerClient.getId());

                if (result != Request.Result.success)
                {
                    request.error(id, "Cannot obtain item");
                    return;
                }

                ConsumableContainer cnt = poc.getConsumableContainer();
                futureRecord.setId(cnt.newId());

                cnt.putConsumable(futureRecord.getAmount(), futureRecord.getItem(), futureRecord.getQuality());
                rs.payload = wd.newPayload;

                playerClient.store();
                playerClient.sendConsumable();

                playerClient.log("Withdrew " + finalAmount + " items " + futureRecord.getItem().getContent().getID() +
                        " from real estate " + rs.payload.masterMap + " key " + key);

                request.success(id, new JSONObject());
            }));
        });
    }

    private void destroyMarketRealEstateItem(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String mapName = args.optString("map", null);
            String key = args.optString("key", null);
            String recordId = args.optString("record", null);
            int amount = args.optInt("amount", -1);

            if (mapName == null || key == null || recordId == null || amount <= 0)
            {
                request.error(id, "Welp");
                return;
            }

            ServerFreeplayMap map = Map.Get(mapName, ServerFreeplayMap.class);
            if (map == null)
            {
                request.error(id, "Welp");
                return;
            }

            RealEstateInfo rs = map.getRealEstateInfo();
            if (rs == null)
            {
                request.error(id, "Welp");
                return;
            }

            if (!playerClient.getAccount().equals(rs.owner))
            {
                request.error(id, "Welp");
                return;
            }

            MarketService marketService = MarketService.Get();
            if (marketService == null)
            {
                request.error(id, "no_service");
                return;
            }

            PlayerData playerData = playerClient.getPlayerData();
            if (playerData == null)
            {
                request.error(id, "Not alive");
                return;
            }

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            if (poc == null)
            {
                request.error(id, "No poc");
                return;
            }

            RealEstateInfo.WithdrawItemFromObjectResult wd = rs.withdrawItemFromObject(key, recordId, amount);
            if (wd == null)
            {
                request.error(id, "Cannot withdraw");
                return;
            }

            ConsumableRecord futureRecord = wd.record;

            if (futureRecord.getItem().getContent() instanceof ConsumableContent)
            {
                if (!((ConsumableContent) futureRecord.getItem().getContent()).isStacks())
                    futureRecord.setAmount(1);
            }

            if (futureRecord.getItem() instanceof InstrumentConsumableItem)
            {
                InstrumentData ici = ((InstrumentConsumableItem) futureRecord.getItem()).getInstrumentData();
                futureRecord.setAmount(1);
                if (ici instanceof WeaponData)
                {
                    Weapon weapon = ((WeaponData) ici).getWeapon();

                    if (weapon.getSlot() != null)
                    {
                        String slotId = weapon.getSlot().getID();

                        switch (slotId)
                        {
                            case "slot-primary":
                            {
                                int primary = getSlotWeaponsAmount("slot-primary", poc.getConsumableContainer());

                                if (primary >= 2)
                                {
                                    request.error(id, "Too much primary");
                                    return;
                                }

                                break;
                            }
                            case "slot-secondary":
                            {
                                int secondary = getSlotWeaponsAmount("slot-secondary",
                                        poc.getConsumableContainer());

                                if (secondary >= 1)
                                {
                                    request.error(id, "Too much secondary");
                                    return;
                                }

                                break;
                            }
                        }
                    }
                }
            }

            if (futureRecord.getItem() instanceof DecayConsumableItem)
                futureRecord.setAmount(1);

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            int finalAmount = futureRecord.getAmount();

            List<MarketService.MarketItemEntry> entries = new ArrayList<>();
            // remove the original real estate
            entries.add(new MarketService.MarketItemEntry(rs.name, -1, wd.oldPayload.write()));
            // add a modified real estate
            entries.add(new MarketService.MarketItemEntry(rs.name, 1, wd.newPayload.write()));

            MARKET_LOCK.add(playerClient.getId());

            marketService.updateMarketItems("freeplay", entries, playerClient.getAccessToken(),
                (r, result) -> BrainOutServer.PostRunnable(() ->
            {
                MARKET_LOCK.remove(playerClient.getId());

                if (result != Request.Result.success)
                {
                    request.error(id, "Cannot obtain item");
                    return;
                }

                ConsumableContainer cnt = poc.getConsumableContainer();
                futureRecord.setId(cnt.newId());

                rs.payload = wd.newPayload;

                playerClient.log("Destroyed " + finalAmount + " items " + futureRecord.getItem().getContent().getID() +
                        " from real estate " + rs.payload.masterMap + " key " + key);

                request.success(id, new JSONObject());
            }));
        });
    }

    private void getMarketItemContainer(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String mapName = args.optString("map", null);
            String key = args.optString("key", null);

            if (mapName == null || key == null)
            {
                request.error(id, "Welp");
                return;
            }

            ServerFreeplayMap map = Map.Get(mapName, ServerFreeplayMap.class);
            if (map == null)
            {
                request.error(id, "Welp");
                return;
            }

            RealEstateInfo rs = map.getRealEstateInfo();
            if (rs == null)
            {
                request.error(id, "Welp");
                return;
            }

            if (!playerClient.getAccount().equals(rs.owner))
            {
                request.error(id, "Welp");
                return;
            }

            MarketService marketService = MarketService.Get();
            if (marketService == null)
            {
                request.error(id, "no_service");
                return;
            }

            PlayerData playerData = playerClient.getPlayerData();
            if (playerData == null)
            {
                request.error(id, "Not alive");
                return;
            }

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            if (poc == null)
            {
                request.error(id, "No poc");
                return;
            }

            RealEstateInfo.RealEstatePayload.ObjectAtLocation item = rs.payload.getItems().get(key);
            if (item == null)
            {
                request.error(id, "No such item");
                return;
            }

            RealEstateInfo.RealEstatePayload.ObjectAtLocation.ObjectContainer container = item.getContainer();

            request.success(id, container.write());
        });
    }

    private void destroyMarketItem(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String marketName = args.optString("market");
            if (!"freeplay".equals(marketName))
            {
                request.error(id, "Unsupported market");
                return;
            }

            String item = args.optString("item");
            JSONObject payload = args.optJSONObject("payload");
            int amount = args.optInt("amount", 1);

            if (item == null || amount <= 0 || payload == null)
            {
                request.error(id, "Bad item");
                return;
            }

            MarketService marketService = MarketService.Get();
            if (marketService == null)
            {
                request.error(id, "no_service");
                return;
            }

            PlayerData playerData = playerClient.getPlayerData();
            if (playerData == null)
            {
                request.error(id, "Not alive");
                return;
            }

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            if (poc == null)
            {
                request.error(id, "No poc");
                return;
            }

            ConsumableRecord futureRecord = MarketUtils.MarketObjectToConsumableRecord(item, payload, amount);
            if (futureRecord == null)
            {
                request.error(id, "Cannot allocate item.");
                return;
            }

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            int finalAmount = futureRecord.getAmount();

            List<MarketService.MarketItemEntry> entries = new ArrayList<>();
            MarketService.MarketItemEntry e = new MarketService.MarketItemEntry(item, -finalAmount, payload);
            entries.add(e);

            MARKET_LOCK.add(playerClient.getId());

            marketService.updateMarketItems(marketName, entries, playerClient.getAccessToken(),
                (r, result) -> BrainOutServer.PostRunnable(() ->
            {
                MARKET_LOCK.remove(playerClient.getId());

                if (result != Request.Result.success)
                {
                    request.error(id, "Cannot obtain item");
                    return;
                }

                playerClient.log("Destroyed " + finalAmount + " items " + item + " of " + payload.toString());

                request.success(id, new JSONObject());
            }));
        });
    }

    private void withdrawMarketRU(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String marketName = args.optString("market");
            if (!"freeplay".equals(marketName))
            {
                request.error(id, "Unsupported market");
                return;
            }

            int amount = args.optInt("amount", 1);

            if (amount <= 0)
            {
                request.error(id, "Bad amount");
                return;
            }

            MarketService marketService = MarketService.Get();
            if (marketService == null)
            {
                request.error(id, "no_service");
                return;
            }

            PlayerData playerData = playerClient.getPlayerData();
            if (playerData == null)
            {
                request.error(id, "Not alive");
                return;
            }

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            if (poc == null)
            {
                request.error(id, "No poc");
                return;
            }

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            List<MarketService.MarketItemEntry> entries = new ArrayList<>();
            MarketService.MarketItemEntry e = new MarketService.MarketItemEntry("ru", -amount, new JSONObject());
            entries.add(e);

            MARKET_LOCK.add(playerClient.getId());

            marketService.updateMarketItems(marketName, entries, playerClient.getAccessToken(),
                (r, result) -> BrainOutServer.PostRunnable(() ->
            {
                MARKET_LOCK.remove(playerClient.getId());

                if (result != Request.Result.success)
                {
                    request.error(id, "Cannot obtain item");
                    return;
                }

                playerClient.addStat("ru", amount);
                playerClient.notify(NotifyAward.ru, amount, NotifyReason.ruEarned, NotifyMethod.message, null);

                playerClient.store();
                playerClient.sendUserProfile();

                playerClient.log("Withdrew " + amount + " RU");

                request.success(id, new JSONObject());
            }));
        });
    }

    private void putMarketItem(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String marketName = args.optString("market");

            PersonalContainer personalContainer = BrainOutServer.ContentMgr.get("personal-container", PersonalContainer.class);
            if (personalContainer == null)
            {
                request.error(id, "No personal container.");
                return;
            }

            if (!"freeplay".equals(marketName))
            {
                request.error(id, "Unsupported market");
                return;
            }

            int recordId = args.optInt("id", -1);
            int amount = args.optInt("amount", 1);

            PlayerData playerData = playerClient.getPlayerData();
            if (playerData == null)
            {
                request.error(id, "Not alive");
                return;
            }

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            if (poc == null)
            {
                request.error(id, "No poc");
                return;
            }

            ConsumableRecord record = poc.getConsumableContainer().get(recordId);

            if (record == null || amount <= 0 || record.getAmount() < amount)
            {
                request.error(id, "Bad amount");
                return;
            }

            MarketService marketService = MarketService.Get();
            if (marketService == null)
            {
                request.error(id, "no_service");
                return;
            }

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            ConsumableItem item = record.getItem();

            if (item instanceof  InstrumentConsumableItem)
            {
                InstrumentData instrumentData = ((InstrumentConsumableItem)item).getInstrumentData();
                ServerWeaponComponentData swc = instrumentData.getComponent(ServerWeaponComponentData.class);
                if (swc != null)
                {
                    ObjectMap<Bullet, ServerWeaponComponentData.UnloadResult> ammo = swc.fullWeaponUloading();
                    for (ObjectMap.Entry<Bullet, ServerWeaponComponentData.UnloadResult> bulletEntry : ammo)
                    {
                        poc.getConsumableContainer().putConsumable(bulletEntry.value.amount,
                            bulletEntry.key.acquireConsumableItem(), bulletEntry.value.quality);
                    }
                }
            }

            MarketService.MarketItemEntry marketItem = MarketUtils.ConsumableRecordToMarketEntry(record);
            if (marketItem == null)
            {
                request.error(id, "Cannot allocate item.");
                return;
            }

            UserProfile profile = playerClient.getProfile();
            String itemCategory = MarketUtils.GetMarketItemCategory(marketItem.name);
            float maxWeight = MarketUtils.GetMaxMarketWeightForPlayer(personalContainer, profile, itemCategory);

            if (maxWeight == 0)
            {
                request.error(id, "No personal container.");
                return;
            }

            if (poc.getCurrentInstrumentRecord() == record)
            {
                ServerPlayerControllerComponentData pcc =
                    playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

                if (pcc != null)
                {
                    pcc.selectFirstInstrument(poc);
                }
            }

            poc.getConsumableContainer().decConsumable(record, amount);

            MARKET_LOCK.add(playerClient.getId());

            marketService.getMarketItems(marketName, playerClient.getAccessToken(),
                (r2, rs2, entries) -> BrainOutServer.PostRunnable(() ->
            {
                if (rs2 != Request.Result.success)
                {
                    MARKET_LOCK.remove(playerClient.getId());
                    poc.getConsumableContainer().putConsumable(amount, record.getItem(), record.getQuality());
                    request.error(id, "Cannot obtain items");
                    return;
                }

                ItemComponent itemComponent = record.getItem().getContent().getComponent(ItemComponent.class);
                if (itemComponent == null)
                {
                    MARKET_LOCK.remove(playerClient.getId());
                    poc.getConsumableContainer().putConsumable(amount, record.getItem(), record.getQuality());
                    request.error(id, "Item has no weight");
                    return;
                }

                float w = itemComponent.getWeight() * amount;

                for (MarketService.MarketItemEntry entry : entries)
                {
                    ConsumableRecord r = MarketUtils.MarketObjectToConsumableRecord(
                            entry.name, entry.payload, entry.amount);
                    if (r == null)
                        continue;
                    ItemComponent i = r.getItem().getContent().getComponent(ItemComponent.class);
                    if (i == null)
                        continue;
                    if (!itemCategory.equals(MarketUtils.GetMarketItemCategory(entry.name)))
                        continue;
                    w += i.getWeight() * r.getAmount();
                }

                if (w > maxWeight)
                {
                    MARKET_LOCK.remove(playerClient.getId());
                    poc.getConsumableContainer().putConsumable(amount, record.getItem(), record.getQuality());
                    request.error(id, "Weight exceeded");
                    return;
                }

                List<MarketService.MarketItemEntry> entries1 = new ArrayList<>();
                marketItem.amount = amount;
                entries1.add(marketItem);

                marketService.updateMarketItems(marketName, entries1, playerClient.getAccessToken(),
                    (r, result) -> BrainOutServer.PostRunnable(() ->
                {
                    MARKET_LOCK.remove(playerClient.getId());

                    if (result != Request.Result.success)
                    {
                        poc.getConsumableContainer().putConsumable(amount, record.getItem(), record.getQuality());
                        request.error(id, "Cannot obtain item");
                        return;
                    }

                    playerClient.store();
                    playerClient.sendConsumable();

                    playerClient.log("Added " + amount + " items " + marketItem.name +
                            " of " + marketItem.payload.toString());

                    request.success(id, new JSONObject());
                }));
            }));

        });
    }

    private void putMarketRealEstateItem(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String mapName = args.optString("map", null);
            String key = args.optString("key", null);

            if (mapName == null || key == null)
            {
                request.error(id, "No map or key");
                return;
            }

            ServerFreeplayMap map = Map.Get(mapName, ServerFreeplayMap.class);
            if (map == null)
            {
                request.error(id, "Couldn't find map");
                return;
            }

            RealEstateInfo rs = map.getRealEstateInfo();
            if (rs == null)
            {
                request.error(id, "No rs");
                return;
            }

            if (!playerClient.getAccount().equals(rs.owner))
            {
                request.error(id, "Not an owner");
                return;
            }

            int recordId = args.optInt("id", -1);
            int amount = args.optInt("amount", 1);

            PlayerData playerData = playerClient.getPlayerData();
            if (playerData == null)
            {
                request.error(id, "Not alive");
                return;
            }

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            if (poc == null)
            {
                request.error(id, "No poc");
                return;
            }

            ConsumableRecord record = poc.getConsumableContainer().get(recordId);

            if (record == null || amount <= 0 || record.getAmount() < amount)
            {
                request.error(id, "Bad amount");
                return;
            }

            MarketService marketService = MarketService.Get();
            if (marketService == null)
            {
                request.error(id, "no_service");
                return;
            }

            RealEstateInfo.PlaceItemIntoObjectResult pl = rs.placeItemIntoObject(key, record, amount);

            if (pl == null)
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            ConsumableItem item = record.getItem();

            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentData instrumentData = ((InstrumentConsumableItem)item).getInstrumentData();
                ServerWeaponComponentData swc = instrumentData.getComponent(ServerWeaponComponentData.class);
                if (swc != null)
                {
                    ObjectMap<Bullet, ServerWeaponComponentData.UnloadResult> ammo = swc.fullWeaponUloading();
                    for (ObjectMap.Entry<Bullet, ServerWeaponComponentData.UnloadResult> bulletEntry : ammo)
                    {
                        poc.getConsumableContainer().putConsumable(bulletEntry.value.amount,
                            bulletEntry.key.acquireConsumableItem(), bulletEntry.value.quality);
                    }
                }
            }

            if (poc.getCurrentInstrumentRecord() == record)
            {
                ServerPlayerControllerComponentData pcc =
                    playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

                if (pcc != null)
                {
                    pcc.selectFirstInstrument(poc);
                }
            }

            poc.getConsumableContainer().decConsumable(record, amount);

            List<MarketService.MarketItemEntry> entries = new LinkedList<>();

            // remove the original real estate
            entries.add(new MarketService.MarketItemEntry(rs.name, -1, pl.oldPayload.write()));
            // add a modified real estate
            entries.add(new MarketService.MarketItemEntry(rs.name, 1, pl.newPayload.write()));

            MARKET_LOCK.add(playerClient.getId());

            marketService.updateMarketItems("freeplay", entries, playerClient.getAccessToken(),
                (r1, result) -> BrainOutServer.PostRunnable(() ->
            {
                MARKET_LOCK.remove(playerClient.getId());

                if (result == Request.Result.success)
                {
                    rs.payload = pl.newPayload;

                    playerClient.store();
                    playerClient.sendConsumable();

                    playerClient.log("Placed " + amount + " items " + pl.marketItem.name +
                        " of " + pl.marketItem.payload.toString() + " into real estate " + rs.payload.masterMap +
                        " item " + key + " (" + record.getItem().getContent().getID() + ")");

                    request.success(id, new JSONObject());
                }
                else
                {
                    poc.getConsumableContainer().putConsumable(amount, record.getItem(), record.getQuality());

                    playerClient.store();
                    playerClient.sendConsumable();

                    request.error(id, result.toString());
                }
            }));
        });
    }

    private void destroyIntvenvoryItem(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            int recordId = args.optInt("id", -1);
            int amount = args.optInt("amount", 1);

            PlayerData playerData = playerClient.getPlayerData();
            if (playerData == null)
            {
                request.error(id, "Not alive");
                return;
            }

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            if (poc == null)
            {
                request.error(id, "No poc");
                return;
            }

            ConsumableRecord record = poc.getConsumableContainer().get(recordId);

            if (record == null || amount <= 0 || record.getAmount() < amount)
            {
                request.error(id, "Bad amount");
                return;
            }

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            ConsumableItem item = record.getItem();

            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentData instrumentData = ((InstrumentConsumableItem)item).getInstrumentData();
                ServerWeaponComponentData swc = instrumentData.getComponent(ServerWeaponComponentData.class);
                if (swc != null)
                {
                    ObjectMap<Bullet, ServerWeaponComponentData.UnloadResult> ammo = swc.fullWeaponUloading();
                    for (ObjectMap.Entry<Bullet, ServerWeaponComponentData.UnloadResult> bulletEntry : ammo)
                    {
                        poc.getConsumableContainer().putConsumable(bulletEntry.value.amount,
                            bulletEntry.key.acquireConsumableItem(),
                            bulletEntry.value.quality);
                    }
                }
            }

            poc.getConsumableContainer().decConsumable(record, amount);

            playerClient.store();
            playerClient.sendConsumable();

            playerClient.log("Destroyed " + amount + " items");

            request.success(id, new JSONObject());

        });
    }

    private void putMarketRU(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String marketName = args.optString("market");

            if (!"freeplay".equals(marketName))
            {
                request.error(id, "Unsupported market");
                return;
            }

            int amount = args.optInt("amount", 1);

            PlayerData playerData = playerClient.getPlayerData();
            if (playerData == null)
            {
                request.error(id, "Not alive");
                return;
            }

            int have = playerClient.getProfile().getInt("ru", 0);

            if (amount <= 0 || amount > have)
            {
                request.error(id, "Bad amount");
                return;
            }

            MarketService marketService = MarketService.Get();
            if (marketService == null)
            {
                request.error(id, "no_service");
                return;
            }

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            playerClient.getProfile().descreaseStat("ru", amount);

            MARKET_LOCK.add(playerClient.getId());

            List<MarketService.MarketItemEntry> entries = new ArrayList<>();
            MarketService.MarketItemEntry marketItem = new MarketService.MarketItemEntry(
                "ru", amount, new JSONObject());
            entries.add(marketItem);

            marketService.updateMarketItems(marketName, entries, playerClient.getAccessToken(),
                (r, result) -> BrainOutServer.PostRunnable(() ->
            {
                MARKET_LOCK.remove(playerClient.getId());

                if (result != Request.Result.success)
                {
                    playerClient.addStat("ru", amount);
                    request.error(id, "Cannot obtain RU");
                    return;
                }

                playerClient.store();
                playerClient.sendUserProfile();

                playerClient.log("Added " + amount + " RU into market");

                request.success(id, new JSONObject());
            }));
        });
    }

    private void newMarketOrder(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Achievement marketPass = BrainOutServer.ContentMgr.get("market-pass", Achievement.class);
            if (marketPass == null)
            {
                request.error(id, "Ugh. Sorry guys.");
                return;
            }

            if (!playerClient.getProfile().hasItem(marketPass))
            {
                request.error(id, "Market is not unlocked.");
                return;
            }

            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String marketName = args.optString("market");

            if (!"freeplay".equals(marketName))
            {
                request.error(id, "Unsupported market");
                return;
            }

            String item = args.optString("item", null);
            JSONObject payload = args.optJSONObject("payload");
            int price = args.optInt("price", 0);
            int amount = args.optInt("amount", 1);

            if (amount <= 0 || price <= 0 || item == null || payload == null)
            {
                request.error(id, "Bad amounts");
                return;
            }

            ConsumableRecord r = MarketUtils.MarketObjectToConsumableRecord(item, payload, amount);
            if (r == null)
            {
                request.error(id, "Unknown content.");
                return;
            }

            int pieces;

            if (r.getItem().getContent() instanceof Bullet)
            {
                pieces = ((Bullet) r.getItem().getContent()).getGood();

                if (amount % pieces != 0)
                {
                    request.error(id, "Bad amounts.");
                    return;
                }
            }
            else
            {
                pieces = 1;
            }

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            MARKET_LOCK.add(playerClient.getId());

            MarketService marketService = MarketService.Get();
            marketService.getMarketSettings(marketName, playerClient.getAccessToken(),
                (r1, result, settings) ->
            {
                int marketFee = settings.optInt("fee", 0);
                int marketFeeMinimum = settings.optInt("fee-minimum", 0);
                int maxPrice = settings.optInt("max-price", 50000);
                int maxAptPrice = settings.optInt("max-apt-price", maxPrice);
                int deadline = settings.optInt("deadline", 1209600);
                int maxOrdersPrice = settings.optInt("max-orders-price", 200000);
                int maxOrders = settings.optInt("max-orders", 20);

                int mp;
                if ("realestate".equals(item))
                {
                    mp = maxAptPrice;
                    maxOrdersPrice = maxAptPrice * 10;
                }
                else
                {
                    mp = maxPrice;
                }

                final int fmaxOrdersPrice = maxOrdersPrice;

                BrainOutServer.PostRunnable(() ->
                {
                    if (result != Request.Result.success)
                    {
                        MARKET_LOCK.remove(playerClient.getId());
                        request.error(id, "Cannot obtain market settings");
                        return;
                    }

                    int p = price * (amount / pieces);


                    if (p > mp)
                    {
                        MARKET_LOCK.remove(playerClient.getId());
                        request.error(id, "The price is too high.");
                        return;
                    }

                    int fee = Math.max(marketFee != 0 ? ((p * marketFee) / 100) : 0, marketFeeMinimum);

                    if (playerClient.getProfile().getStats().get("ru", 0f) < fee)
                    {
                        MARKET_LOCK.remove(playerClient.getId());
                        request.error(id, "Not enough ru for the fee.");
                        return;
                    }

                    marketService.listMyOrders(marketName, playerClient.getAccessToken(),
                        (rr, rs, myOrders) -> BrainOutServer.PostRunnable(() ->
                    {
                        if (rs != Request.Result.success)
                        {
                            MARKET_LOCK.remove(playerClient.getId());
                            request.error(id, "Cannot obtain market settings");
                            return;
                        }

                        if (myOrders.size() >= maxOrders)
                        {
                            MARKET_LOCK.remove(playerClient.getId());
                            request.error(id, "MENU_MARKET_TOO_MANY_ORDERS");
                            return;
                        }

                        int ordersPrice = p;
                        for (MarketService.MarketOrderEntry order : myOrders)
                        {
                            if (!("ru".equals(order.takeItem)))
                                continue;

                            if ("realestate".equals(order.giveItem))
                            {
                                continue;
                            }

                            ordersPrice += order.takeAmount * order.available;

                            if (ordersPrice >= fmaxOrdersPrice)
                            {
                                MARKET_LOCK.remove(playerClient.getId());
                                request.error(id, "MENU_MARKET_TOTAL_ORDER_COST_REACHED");
                                return;
                            }
                        }

                        playerClient.getProfile().descreaseStat("ru", fee);

                        int ordersAmount = amount / pieces;

                        Date d = new Date(Instant.now().plus(deadline, ChronoUnit.SECONDS).toEpochMilli());
                        JSONObject orderPayload = new JSONObject();

                        orderPayload.put("name", playerClient.getName());

                        if (playerClient.getAvatar() != null && !playerClient.getAvatar().isEmpty())
                        {
                            orderPayload.put("avatar", playerClient.getAvatar());
                        }

                        marketService.postOrder(marketName, item, pieces, payload,
                            "ru", price, new JSONObject(), ordersAmount, orderPayload,
                            d, playerClient.getAccessToken(),
                        (r2, result1, orderId, fulfilled) -> BrainOutServer.PostRunnable(() ->
                        {
                            MARKET_LOCK.remove(playerClient.getId());

                            if (result1 != Request.Result.success)
                            {
                                playerClient.getProfile().addStat("ru", fee, false);
                                request.error(id, "MENU_ERROR_TRY_AGAIN");
                            }
                            else
                            {
                                request.success(id, new JSONObject());
                            }
                        }));
                    }));
                });
            });
        });
    }

    private void fulfillMarketOrder(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Achievement marketPass = BrainOutServer.ContentMgr.get("market-pass", Achievement.class);
            if (marketPass == null)
            {
                request.error(id, "Ugh. Sorry guys.");
                return;
            }

            if (!playerClient.getProfile().hasItem(marketPass))
            {
                request.error(id, "Market is not unlocked.");
                return;
            }

            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String marketName = args.optString("market");

            if (!"freeplay".equals(marketName))
            {
                request.error(id, "MENU_MARKET_ORDER_CANNOT_BE_FULFILLED");
                return;
            }

            String orderId = args.optString("order_id", null);
            int amount = args.optInt("amount", 1);

            if (amount <= 0 || orderId == null)
            {
                request.error(id, "MENU_MARKET_ORDER_CANNOT_BE_FULFILLED");
                return;
            }

            PersonalContainer personalContainer = BrainOutServer.ContentMgr.get("personal-container", PersonalContainer.class);
            if (personalContainer == null)
            {
                request.error(id, "MENU_MARKET_ORDER_CANNOT_BE_FULFILLED");
                return;
            }

            String itemName = args.optString("item_name", null);

            UserProfile profile = playerClient.getProfile();
            String itemCategory = MarketUtils.GetMarketItemCategory(itemName);
            float maxWeight = MarketUtils.GetMaxMarketWeightForPlayer(personalContainer, profile,
                itemCategory);

            if (maxWeight == 0)
            {
                request.error(id, "MENU_MARKET_ORDER_CANNOT_BE_FULFILLED");
                return;
            }

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            MARKET_LOCK.add(playerClient.getId());

            MarketService marketService = MarketService.Get();

            marketService.getOrder(marketName, orderId, playerClient.getAccessToken(), (rq, result, order) ->
                BrainOutServer.PostRunnable(() ->
            {
                if (result != Request.Result.success)
                {
                    MARKET_LOCK.remove(playerClient.getId());
                    request.error(id, "MENU_MARKET_ORDER_CANNOT_BE_FULFILLED");
                    return;
                }

                ConsumableRecord record = MarketUtils.MarketObjectToConsumableRecord(order.giveItem,
                        order.givePayload, order.giveAmount);

                if (record == null)
                {
                    MARKET_LOCK.remove(playerClient.getId());
                    request.error(id, "MENU_MARKET_ORDER_CANNOT_BE_FULFILLED");
                    return;
                }

                marketService.getMarketItems(marketName, playerClient.getAccessToken(),
                    (r2, rs2, entries) -> BrainOutServer.PostRunnable(() ->
                {
                    if (rs2 != Request.Result.success)
                    {
                        MARKET_LOCK.remove(playerClient.getId());
                        request.error(id, "MENU_MARKET_ORDER_CANNOT_BE_FULFILLED");
                        return;
                    }

                    ItemComponent itemComponent = record.getItem().getContent().getComponent(ItemComponent.class);
                    if (itemComponent != null)
                    {
                        float w = itemComponent.getWeight() * order.giveAmount * amount;

                        for (MarketService.MarketItemEntry entry : entries)
                        {
                            ConsumableRecord r = MarketUtils.MarketObjectToConsumableRecord(
                                    entry.name, entry.payload, entry.amount);
                            if (r == null)
                                continue;
                            ItemComponent i = r.getItem().getContent().getComponent(ItemComponent.class);
                            if (i == null)
                                continue;
                            if (!itemCategory.equals(MarketUtils.GetMarketItemCategory(entry.name)))
                                continue;
                            w += i.getWeight() * r.getAmount();
                        }

                        if (w > maxWeight)
                        {
                            MARKET_LOCK.remove(playerClient.getId());
                            request.error(id, "MENU_MARKET_ORDER_WEIGHT_LIMIT");
                            return;
                        }
                    }

                    marketService.fulfillOrder(marketName, orderId, amount,  playerClient.getAccessToken(),
                        (r1, result1, order_id, fulfilled) ->
                    {
                        BrainOutServer.PostRunnable(() ->
                        {
                            MARKET_LOCK.remove(playerClient.getId());

                            if (result1 != Request.Result.success)
                            {
                                request.error(id, "MENU_MARKET_ORDER_CANNOT_BE_FULFILLED");
                            }
                            else
                            {
                                request.success(id, new JSONObject());
                            }
                        });
                    });
                }));
            }));


        });
    }

    private void assembleRealEstateItem(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "Locked");
                return;
            }

            String itemName = args.optString("item");
            if (itemName == null)
            {
                request.error(id, "No such item");
                return;
            }

            RealEstateItem rsItem = BrainOutServer.ContentMgr.get(itemName, RealEstateItem.class);
            if (rsItem == null)
            {
                request.error(id, "No such item");
                return;
            }

            RecipeComponent recipeComponent = rsItem.getComponent(RecipeComponent.class);
            if (recipeComponent == null)
            {
                request.error(id, "Cannot construct such item.");
                return;
            }

            if (recipeComponent.getRequiredStat() != null)
            {
                if (playerClient.getProfile().getInt(recipeComponent.getRequiredStat(), 0) <= 0)
                {
                    request.error(id, "Stat is not unlocked.");
                    return;
                }
            }

            float maxWeight = MarketUtils.GetMaxMarketWeightForPlayer(null, playerClient.getProfile(), "rs");

            List<MarketService.MarketItemEntry> itemsToUpdate = new LinkedList<>();

            for (ObjectMap.Entry<Resource, Integer> entry : recipeComponent.getRequiredItems())
            {
                ConsumableRecord r =
                    new ConsumableRecord(entry.key.acquireConsumableItem(), -entry.value, -1);

                MarketService.MarketItemEntry updateEntry =
                    MarketUtils.ConsumableRecordToMarketEntry(r);

                if (updateEntry == null)
                {
                    request.error(id, "Cannot allocate an update entry");
                    return;
                }

                itemsToUpdate.add(updateEntry);
            }

            ConsumableRecord newItemRecord =
                new ConsumableRecord(new RealEstateItemConsumableItem(rsItem), 1, -1);
            MarketService.MarketItemEntry newItemEntry =
                MarketUtils.ConsumableRecordToMarketEntry(newItemRecord);

            itemsToUpdate.add(newItemEntry);

            MARKET_LOCK.add(playerClient.getId());

            MarketService marketService = MarketService.Get();

            marketService.getMarketItems("freeplay", playerClient.getAccessToken(),
                (r2, rs2, entries) -> BrainOutServer.PostRunnable(() ->
            {
                if (rs2 != Request.Result.success)
                {
                    MARKET_LOCK.remove(playerClient.getId());
                    request.error(id, "MENU_MARKET_ORDER_CANNOT_BE_FULFILLED");
                    return;
                }

                ItemComponent itemComponent = rsItem.getComponent(ItemComponent.class);
                if (itemComponent != null)
                {
                    float w = itemComponent.getWeight();

                    for (MarketService.MarketItemEntry entry : entries)
                    {
                        ConsumableRecord r = MarketUtils.MarketObjectToConsumableRecord(
                                entry.name, entry.payload, entry.amount);
                        if (r == null)
                            continue;
                        ItemComponent i = r.getItem().getContent().getComponent(ItemComponent.class);
                        if (i == null)
                            continue;
                        if (!"rs".equals(MarketUtils.GetMarketItemCategory(entry.name)))
                            continue;
                        w += i.getWeight() * r.getAmount();
                    }

                    if (w > maxWeight)
                    {
                        MARKET_LOCK.remove(playerClient.getId());
                        request.error(id, "MENU_MARKET_ORDER_WEIGHT_LIMIT");
                        return;
                    }
                }

                marketService.updateMarketItems("freeplay", itemsToUpdate, playerClient.getAccessToken(),
                    (request1, result) -> BrainOutServer.PostRunnable(() ->
                {
                    if (result == Request.Result.success)
                    {
                        request.success(id, new JSONObject());
                    }
                    else
                    {
                        request.error(id, "Cannot construct and rs item");
                    }

                    MARKET_LOCK.remove(playerClient.getId());
                }));
            }));
        });
    }

    private void cancelMarketOrder(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (MARKET_LOCK.contains(playerClient.getId()))
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            String marketName = args.optString("market");

            if (!"freeplay".equals(marketName))
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            String orderId = args.optString("order_id", null);

            if (orderId == null)
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            PersonalContainer personalContainer = BrainOutServer.ContentMgr.get("personal-container", PersonalContainer.class);
            if (personalContainer == null)
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            String itemName = args.optString("item_name", null);
            if (itemName == null)
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            UserProfile profile = playerClient.getProfile();
            String itemCategory = MarketUtils.GetMarketItemCategory(itemName);
            float maxWeight = MarketUtils.GetMaxMarketWeightForPlayer(personalContainer, profile,
                itemCategory);

            if (maxWeight == 0)
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            if (playerClient.addMarketCooldown())
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            MARKET_LOCK.add(playerClient.getId());

            MarketService marketService = MarketService.Get();

            if (playerClient.getRights() == PlayerRights.mod || playerClient.getRights() == PlayerRights.admin)
            {
                marketService.deleteOrder(marketName, orderId, playerClient.getAccessToken(),
                    (r1, result1) ->
                {
                    BrainOutServer.PostRunnable(() ->
                    {
                        MARKET_LOCK.remove(playerClient.getId());

                        if (result1 != Request.Result.success)
                        {
                            request.error(id, "MENU_ERROR_TRY_AGAIN");
                        }
                        else
                        {
                            request.success(id, new JSONObject());
                        }
                    });
                });
                return;
            }

            marketService.getOrder(marketName, orderId, playerClient.getAccessToken(), (rq, result, order) ->
                BrainOutServer.PostRunnable(() ->
            {
                if (result != Request.Result.success)
                {
                    MARKET_LOCK.remove(playerClient.getId());
                    request.error(id, "MENU_ERROR_TRY_AGAIN");
                    return;
                }

                ConsumableRecord record = MarketUtils.MarketObjectToConsumableRecord(order.giveItem,
                    order.givePayload, order.giveAmount);

                if (record == null)
                {
                    MARKET_LOCK.remove(playerClient.getId());
                    request.error(id, "MENU_ERROR_TRY_AGAIN");
                    return;
                }

                marketService.getMarketItems(marketName, playerClient.getAccessToken(),
                    (r2, rs2, entries) -> BrainOutServer.PostRunnable(() ->
                {
                    if (rs2 != Request.Result.success)
                    {
                        MARKET_LOCK.remove(playerClient.getId());
                        request.error(id, "MENU_ERROR_TRY_AGAIN");
                        return;
                    }

                    ItemComponent itemComponent = record.getItem().getContent().getComponent(ItemComponent.class);
                    if (itemComponent != null)
                    {
                        float w = itemComponent.getWeight() * order.giveAmount * order.available;

                        for (MarketService.MarketItemEntry entry : entries)
                        {
                            ConsumableRecord r = MarketUtils.MarketObjectToConsumableRecord(
                                    entry.name, entry.payload, entry.amount);
                            if (r == null)
                                continue;
                            ItemComponent i = r.getItem().getContent().getComponent(ItemComponent.class);
                            if (i == null)
                                continue;
                            if (!itemCategory.equals(MarketUtils.GetMarketItemCategory(entry.name)))
                                continue;
                            w += i.getWeight() * r.getAmount();
                        }

                        if (w > maxWeight)
                        {
                            MARKET_LOCK.remove(playerClient.getId());
                            request.error(id, "MENU_MARKET_ORDER_WEIGHT_LIMIT");
                            return;
                        }
                    }


                    marketService.deleteOrder(marketName, orderId, playerClient.getAccessToken(),
                        (r1, result1) ->
                    {
                        BrainOutServer.PostRunnable(() ->
                        {
                            MARKET_LOCK.remove(playerClient.getId());

                            if (result1 != Request.Result.success)
                            {
                                request.error(id, "MENU_ERROR_TRY_AGAIN");
                            }
                            else
                            {
                                request.success(id, new JSONObject());
                            }
                        });
                    });
                }));
            }));


        });
    }


    private void handleRedeemContractReward(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            String groupId = args.optString("id");

            ContractGroup group = BrainOut.ContentMgr.get(groupId, ContractGroup.class);
            if (group == null)
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            if (getProfile().hasItem(group, false))
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            if (!group.isComplete(getProfile()))
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            ServerContractGroup sc = ((ServerContractGroup) group);

            if (!sc.getReward().apply(playerClient, true))
            {
                request.error(id, "MENU_ERROR_TRY_AGAIN");
                return;
            }

            getProfile().addItem(group, 1);
            playerClient.sendUserProfile();

            BrainOutServer.PostRunnable(() -> request.success(id, new JSONObject()));
        });
    }

    private void handlePurchaseOfflineItem(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            String itemId = args.optString("id");

            if (itemId == null)
            {
                request.error(id, "No such item");
                return;
            }

            int amount = args.optInt("amount", 1);

            StoreService fake = new StoreService(null, "");
            StoreService.Store store = fake.new Store("fake");
            store.parse(new JSONObject(Gdx.files.local("store.json").readString("UTF-8")));

            StoreService.Store.Item foundItem = null;

            for (StoreService.Store.Item item : store.getItems())
            {
                if (item.getId().equals(itemId))
                {
                    foundItem = item;
                    break;
                }
            }

            if (foundItem == null)
            {
                request.error(id, "No such item");
                return;
            }

            JSONObject offlinePrice = foundItem.getPublicPayload().optJSONObject("offline-price");

            if (offlinePrice == null)
            {
                request.error(id, "No such item");
                return;
            }

            String currency = offlinePrice.getString("currency");
            int price = offlinePrice.getInt("amount");

            int totalPrice = amount * price;

            float currentAmount = playerClient.getProfile().getStats().get(currency, 0.0f);

            if (currentAmount < totalPrice)
            {
                request.error(id, "Not enough currency");
                return;
            }

            playerClient.addStat(currency, -totalPrice);

            JSONObject privates = new JSONObject(Gdx.files.local("store-private.json").readString());
            JSONObject private_ = privates.getJSONObject(itemId);
            playerClient.applyOrderContents(private_, amount, itemId);

            request.success(id, new JSONObject());
        });
    }

    private void handlePurchaseOwnable(JSONObject args, int id, ClientRequest request)
    {
        BrainOutServer.PostRunnable(() ->
        {
            String itemId = args.optString("id");

            if (itemId == null)
            {
                request.error(id, "No such item");
                return;
            }

            OwnableContent ownableContent = BrainOutServer.ContentMgr.get(itemId, OwnableContent.class);

            if (ownableContent == null || ownableContent.getShopItem() == null)
            {
                request.error(id, "No such item");
                return;
            }

            if (ownableContent.isLocked(playerClient.getProfile()))
            {
                request.error(id, "MENU_LOCKED");
                return;
            }

            if (ownableContent.hasItem(playerClient.getProfile()))
            {
                request.error(id, "MENU_LOCKED");
                return;
            }

            Shop.ShopItem shopItem = ownableContent.getShopItem();

            float currentAmount = playerClient.getProfile().getStats().get(shopItem.getCurrency(), 0.0f);

            if (currentAmount < shopItem.getAmount())
            {
                request.error(id, "MENU_NOT_ENOUGH_RU");
                return;
            }

            playerClient.addStat(shopItem.getCurrency(), -shopItem.getAmount());
            playerClient.getProfile().addItem(ownableContent, 1);
            playerClient.gotOwnable(ownableContent, "purchase", ClientProfile.OnwAction.owned, 1);
            playerClient.sendUserProfile();

            request.success(id, new JSONObject());
        });
    }

    private void handleOfflineForceUnlock(JSONObject args, int id, ClientRequest request)
    {
        if (BrainOut.OnlineEnabled())
        {
            request.error(id, "Whoops.");
            return;
        }

        String contentId = args.optString("content");

        if (contentId == null)
        {
            request.error(id, "No such content");
            return;
        }

        OwnableContent cnt = BrainOutServer.ContentMgr.get(contentId, OwnableContent.class);

        if (cnt == null)
        {
            request.error(id, "No such content");
            return;
        }

        playerClient.gotOwnable(cnt, "force", ClientProfile.OnwAction.owned, 1);
        playerClient.getProfile().setDirty();
        playerClient.sendUserProfile();

        request.success(id, new JSONObject());
    }

    private void handleDeactivate(JSONObject args, int id, ClientRequest request)
    {
        if (getProfile() == null)
        {
            request.error(id, "No getProfile()");
            return;
        }

        long now = System.currentTimeMillis() / 1000L;

        getProfile().setDeactivated(now + 2592000);
        getProfile().setDirty(true);

        request.success(id, new JSONObject());
    }

    private void handleFreePlayPlayInstallUpgrade(JSONObject args, int id, ClientRequest request)
    {
        int object = args.optInt("object", -1);
        String key = args.optString("key", null);
        String upgrade = args.optString("upgrade", null);

        PlayerData playerData = playerClient.getPlayerData();
        if (playerData == null)
        {
            request.error(id, "Not alive");
            return;
        }

        if (upgrade == null)
        {
            request.error(id, "No upgrade field.");
            return;
        }

        if (key == null)
        {
            request.error(id, "No key field.");
            return;
        }

        Content c = BrainOutServer.ContentMgr.get(upgrade);

        if (!(c instanceof Upgrade))
        {
            request.error(id, "Not an upgrade");
            return;
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (!(gameMode instanceof GameModeFree))
        {
            request.error(id, "Not in FreePlay mode");
            return;
        }

        ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());

        if (!(serverRealization instanceof ServerFreeRealization))
        {
            request.error(id, "Not in FreePlay mode");
            return;
        }

        if (playerData.getCurrentInstrument() != null &&
            playerData.getCurrentInstrument().isForceSelect())
        {
            request.error(id, "Locked");
            return;
        }

        ServerFreeRealization free = ((ServerFreeRealization) serverRealization);

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        ConsumableRecord record =
            free.playerInstallWeaponUpgrade(playerClient, playerData, poc, object, key, ((Upgrade) c));

        if (record != null)
        {
            JSONObject result = new JSONObject();
            result.put("record", record.getId());
            request.success(id, result);
        }
        else
        {
            request.error(id, "Failed");
        }
    }

    private void handleFreePlayPlayWithPartnerAgain(JSONObject args, int id, ClientRequest request)
    {
        String region = args.optString("region");

        if (region == null)
        {
            request.error(id, "No region defined");
            return;
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (!(gameMode instanceof GameModeFree))
        {
            request.error(id, "Not in FreePlay mode");
            return;
        }

        ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());

        if (!(serverRealization instanceof ServerFreeRealization))
        {
            request.error(id, "Not in FreePlay mode");
            return;
        }

        ServerFreeRealization free = ((ServerFreeRealization) serverRealization);

        free.playWithPartnerAgain(playerClient, region, new ServerFreeRealization.PlayWithPartnerAgainCallback()
        {
            @Override
            public void success()
            {
                request.success(id, new JSONObject());
            }

            @Override
            public void failed(String reason)
            {
                request.error(id, reason);
            }
        });
    }

    private void handleSwitchProfileBadge(JSONObject args, int id, ClientRequest request)
    {
        String badgeId = args.optString("badge", null);

        ProfileBadge badge = BrainOutServer.ContentMgr.get(badgeId, ProfileBadge.class);

        if (badge == null || !badge.hasItem(getProfile()))
        {
            request.error(id, "Not owned");
            return;
        }

        BrainOutServer.PostRunnable(() ->
        {
            playerClient.getProfile().setSelection(Constants.User.PROFILE_BADGE, badge.getID());
            playerClient.getProfile().setDirty();
            playerClient.sendUserProfile();
            playerClient.sendRemotePlayers();

            request.success(id, new JSONObject());
        });
    }

    private void handleRespondToClanRequest(JSONObject args, int id, ClientRequest request)
    {
        if (getProfile() == null || !getProfile().isParticipatingClan())
        {
            request.error(id, "Not in clan");
            return;
        }

        String messageId = args.optString("message_id");
        String accountId = args.optString("account_id");
        String playerName = args.optString("player_name");
        String key = args.optString("key");
        String method = args.optString("method", "approve");
        String clanId = getProfile().getClanId();

        if (messageId == null || key == null || accountId == null || playerName == null)
        {
            request.error(id, "bad_arguments");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        socialService.getGroup(playerClient.getAccessToken(), clanId, (service, request1, result, group) ->
        {
            if (result == Request.Result.success)
            {
                JSONObject notify = new JSONObject();

                notify.put("name", playerName);
                notify.put("group_name", group.getName());
                notify.put("avatar", group.getProfile().optString("avatar", ""));

                if (method.equals("approve"))
                {
                    socialService.approveJoin(playerClient.getAccessToken(), clanId, accountId, key, 0, null, notify,
                        (service1, request2, resultStatus) ->
                    {
                        BrainOutServer.PostRunnable(() ->
                        {
                            switch (resultStatus)
                            {
                                case success:
                                case notFound:
                                {
                                    deleteSocialMessage(messageId);
                                    request.success(id, new JSONObject());

                                    break;
                                }
                                default:
                                {
                                    request.error(id, resultStatus.toString());

                                    break;
                                }
                            }
                        });
                    });
                }
                else
                {
                    socialService.rejectJoin(playerClient.getAccessToken(), clanId, accountId, key, notify,
                        (service1, request2, result2) ->
                    {
                        BrainOutServer.PostRunnable(() ->
                        {
                            switch (result2)
                            {
                                case success:
                                case notFound:
                                {
                                    deleteSocialMessage(messageId);
                                    request.success(id, new JSONObject());

                                    break;
                                }
                                default:
                                {
                                    request.error(id, result2.toString());

                                    break;
                                }
                            }
                        });
                    });
                }
            }
            else
            {
                request.error(id, result.toString());
            }
        });
    }

    private void deleteSocialMessage(String messageId)
    {
        if (playerClient.getMessageSession() == null)
            return;

        playerClient.getMessageSession().deleteMessage(messageId, null);
    }

    private void handleRequestJoinClan(JSONObject args, int id, ClientRequest request)
    {
        if (!isClansUnlocked())
        {
            request.error(id, "Clans are locked for this player");
            return;
        }

        float amount = getProfile().getStats().get(Constants.Clans.CURRENCY_JOIN_CLAN, 0.0f);
        int need = BrainOutServer.Settings.getPrice("joinClan");

        if (amount < need)
        {
            request.error(id, "MENU_NOT_ENOUGH_NUCLEAR_MATERIAL");
            return;
        }

        if (getProfile().isParticipatingClan())
        {
            request.error(id, "Already in clan");
            return;
        }

        String clanId = args.optString("clan_id");

        if (clanId == null)
        {
            request.error(id, "bad_arguments");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        JSONObject participationProfile = generateNewParticipationProfile();

        socialService.requestJoinGroup(playerClient.getAccessToken(), clanId, participationProfile, participationProfile,
                (service, request1, joinResult, key) ->
                {
                    if (joinResult == Request.Result.success)
                    {
                        BrainOutServer.PostRunnable(() ->
                        {
                            doJoinClassRequestSuccess(id, request);
                        });
                    }
                    else
                    {
                        request.error(id, joinResult.toString());
                    }
                });
    }

    private void handleChangeAvatarClan(JSONObject args, int id, ClientRequest request)
    {
        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "You are not in the clan");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        String url = args.optString("url", "");

        socialService.getMyGroupParticipant(playerClient.getAccessToken(), getProfile().getClanId(),
                (service, request1, result, participant, owner) ->
                {
                    if (result == Request.Result.success)
                    {
                        if (owner || participant.getPermissions().contains(Clan.Permissions.CHANGE_SUMMARY))
                        {
                            BrainOutServer.PostRunnable(() -> checkAvatar(url, new AvatarCheckCallback()
                            {
                                @Override
                                public void success()
                                {
                                    JSONObject update = new JSONObject();
                                    update.put("avatar", url);

                                    socialService.updateGroupProfile(playerClient.getAccessToken(), getProfile().getClanId(), update,
                                            (service2, request2, updateResult, updatedProfile) ->
                                            {
                                                BrainOutServer.PostRunnable(() ->
                                                {
                                                    if (updateResult == Request.Result.success)
                                                    {
                                                        getProfile().setClan(getProfile().getClanId(), url);
                                                        getProfile().setDirty();

                                                        request.success(id, new JSONObject());
                                                    }
                                                    else
                                                    {
                                                        request.error(id, updateResult.toString());
                                                    }
                                                });
                                            });
                                }

                                @Override
                                public void failed()
                                {
                                    request.error(id, "MENU_AVATAR_FAILED");
                                }
                            }));
                        }
                        else
                        {
                            request.error(id, "No permissions");
                        }
                    }
                    else
                    {
                        request.error(id, result.toString());
                    }
                });
    }

    private void handleChangeClanSummary(JSONObject args, int id, ClientRequest request)
    {
        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "You are not in the clan");
            return;
        }

        int price = BrainOutServer.Settings.getPrice("updateClan");

        if (playerClient.getStat(Constants.Clans.CURRENCY_UPDATE_CLAN, 0.0f) < price)
        {
            request.error(id, "MENU_NOT_ENOUGH_SKILLPOINTS");
            return;
        }

        String newName = args.optString("name", null);

        if (newName != null && !validateClanName(newName))
        {
            request.error(id, "MENU_BAD_CLAN_NAME");
            return;
        }

        newName = BrainOutServer.getInstance().validateText(newName);

        SocialService.Group.JoinMethod joinMethod;
        String joinMethodStr = args.optString("join_method", null);

        if (joinMethodStr != null)
        {
            try
            {
                joinMethod = SocialService.Group.JoinMethod.valueOf(joinMethodStr);
            }
            catch (IllegalArgumentException ignored)
            {
                request.error(id, "bad_arguments");
                return;
            }
        }
        else
        {
            joinMethod = null;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        String finalNewName = newName;

        checkGroupAccess(getProfile().getClanId(), Clan.Permissions.CHANGE_SUMMARY, new AccessCheckCallback()
        {
            @Override
            public void success(SocialService.Group.Participant participant)
            {
                socialService.updateGroupSummary(
                        playerClient.getAccessToken(), getProfile().getClanId(), finalNewName, joinMethod, null,
                        (service, request1, result) ->
                        {
                            BrainOutServer.PostRunnable(() ->
                            {
                                if (result == Request.Result.success)
                                {
                                    doChangeClanSummarySuccess(id, request);
                                }
                                else
                                {
                                    request.error(id, result.toString());
                                }
                            });
                        });
            }

            @Override
            public void failed()
            {
                request.error(id, "No permissions");
            }
        });
    }

    private String filterDescription(String description)
    {
        if (description == null || description.isEmpty() || description.length() < 3)
            return null;

        if (description.length() > 100)
            description = description.substring(0, 100);

        description = description.replace("\n", "").replace("\r", "")
                .replaceAll("(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?", "");

        return description;
    }

    private void handleChangeClanDescription(JSONObject args, int id, ClientRequest request)
    {
        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "You are not in the clan");
            return;
        }

        String description = filterDescription(args.optString("description", null));

        if (description == null)
        {
            request.error(id, "Bad clan description");
            return;
        }

        description = BrainOutServer.getInstance().validateText(description);

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        JSONObject updateProfile = new JSONObject();

        updateProfile.put("description", description);

        checkGroupAccess(getProfile().getClanId(), Clan.Permissions.CHANGE_SUMMARY, new AccessCheckCallback()
        {
            @Override
            public void success(SocialService.Group.Participant participant)
            {
                socialService.updateGroupProfile(
                        playerClient.getAccessToken(), getProfile().getClanId(), updateProfile,
                        (service, request1, result, updatedProfile) ->
                        {
                            BrainOutServer.PostRunnable(() ->
                            {
                                if (result == Request.Result.success)
                                {
                                    request.success(id, new JSONObject());
                                }
                                else
                                {
                                    request.error(id, result.toString());
                                }
                            });
                        });
            }

            @Override
            public void failed()
            {
                request.error(id, "No permissions");
            }
        });
    }

    private void handleEngageClanConflict(JSONObject args, int id, ClientRequest request)
    {
        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "You are not in the clan");
            return;
        }

        String clanId = args.optString("clan_id", null);
        JSONObject roomSettings = args.optJSONObject("room_settings");
        int conflictSize = args.optInt("conflict_size", 8);

        if (conflictSize < 4 || conflictSize > 8 || conflictSize % 2 != 0)
        {
            request.error(id, "Bad conflict size");
            return;
        }

        if (clanId == null)
        {
            request.error(id, "Bad clan ID");
            return;
        }

        if (roomSettings == null)
        {
            request.error(id, "Bad room settings");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        checkGroupAccess(getProfile().getClanId(), Clan.Permissions.ENGAGE_CONFLICT, new AccessCheckCallback()
        {
            @Override
            public void success(SocialService.Group.Participant participant)
            {
                JSONObject msg = new JSONObject();

                msg.put("group_id", playerClient.getClanId());
                msg.put("group_name", playerClient.getClanName());
                msg.put("group_avatar", playerClient.getClanAvatar());

                msg.put("room_settings", roomSettings);
                msg.put("conflict_size", conflictSize);

                Set<String> flags = new HashSet<>();
                flags.add("deletable");

                playerClient.getMessageSession().sendMessage("social-group",
                    clanId, "engage_conflict", msg, flags);

                request.success(id, new JSONObject());
            }

            @Override
            public void failed()
            {
                request.error(id, "No permissions");
            }
        });
    }

    private void handleCancelClanConflict(JSONObject args, int id, ClientRequest request)
    {
        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "You are not in the clan");
            return;
        }

        SocialService socialService = SocialService.Get();
        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        if (loginService == null)
        {
            request.error(id, "No login service");
            return;
        }

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        if (gameService == null)
        {
            request.error(id, "No game service");
            return;
        }

        String accountId = playerClient.getAccount();

        socialService.getGroup(playerClient.getAccessToken(), getProfile().getClanId(), (service, request1, result, group) ->
        {
            if (result != Request.Result.success)
            {
                request.error(id, result.toString());
                return;
            }

            SocialService.Group.Participant me = group.getParticipants().get(accountId);

            if (me == null)
            {
                request.error(id, "Not in this fraction.");
                return;
            }

            if (!(group.getOwner().equals(accountId) || me.hasPermission(Clan.Permissions.ENGAGE_CONFLICT)))
            {
                request.error(id, "Not authorized");
                return;
            }

            String conflictId = group.getProfile().optString("conflict");

            if (conflictId == null)
            {
                request.error(id, "No conflict");
                return;
            }

            JSONObject message = new JSONObject();
            message.put("closed-by", accountId);
            message.put("closed-by-name", playerClient.getName());

            gameService.closeParty(loginService.getCurrentAccessToken(), conflictId, message,
                    (service1, request2, result1) ->
                    {
                        if (result1 != Request.Result.success)
                        {
                            request.error(id, result1.toString());
                            return;
                        }

                        request.success(id, message);
                    });
        });
    }

    private void handleCheckClanConflict(JSONObject args, int id, ClientRequest request)
    {
        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "You are not in the clan");
            return;
        }

        SocialService socialService = SocialService.Get();
        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        if (loginService == null)
        {
            request.error(id, "No login service");
            return;
        }

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        if (gameService == null)
        {
            request.error(id, "No game service");
            return;
        }

        String accountId = playerClient.getAccount();

        socialService.getGroup(playerClient.getAccessToken(), getProfile().getClanId(),
            (service, request1, result, group) ->
        {
            if (result != Request.Result.success)
            {
                request.error(id, result.toString());
                return;
            }

            String conflictId = group.getProfile().optString("conflict");

            if (conflictId == null)
            {
                request.error(id, "No conflict");
                return;
            }

            JSONObject message = new JSONObject();
            message.put("closed-by", accountId);
            message.put("closed-by-name", playerClient.getName());

            gameService.getParty(loginService.getCurrentAccessToken(), conflictId,
                (service2, request2, status12, party) ->
            {
                if (status12 == Request.Result.notFound)
                {
                    JSONObject update = new JSONObject();

                    update.put("conflict", JSONObject.NULL);
                    update.put("conflict-with", JSONObject.NULL);

                    socialService.updateGroupProfile(
                        playerClient.getAccessToken(), getProfile().getClanId(), update,
                    (service3, request3, result3, updatedProfile) ->
                    {
                        if (result3 == Request.Result.success)
                        {
                            request.success(id, new JSONObject());
                        }
                        else
                        {
                            request.error(id, result3.toString());
                        }
                    });

                }
                else
                {
                    request.error(id, "Please refresh");
                }
            });
        });
    }

    private void handleEngageClanResult(JSONObject args, int id, ClientRequest request)
    {
        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "You are not in the clan");
            return;
        }

        String clanId = args.optString("group_id", null);
        JSONObject roomSettings = args.optJSONObject("room_settings");
        String method = args.optString("method", "reject");
        String messageId = args.optString("message_id");
        int conflictSize = args.optInt("conflict_size", 8);

        if (conflictSize < 4 || conflictSize > 8 || conflictSize % 2 != 0)
        {
            request.error(id, "Bad conflict size");
            return;
        }

        if (clanId == null)
        {
            request.error(id, "Bad faction ID");
            return;
        }

        if (messageId == null)
        {
            request.error(id, "Message ID");
            return;
        }

        if (roomSettings == null)
        {
            request.error(id, "Bad room settings");
            return;
        }

        SocialService socialService = SocialService.Get();
        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        if (socialService == null || loginService == null || gameService == null)
        {
            request.error(id, "No service");
            return;
        }

        if (Objects.equals(method, "reject"))
        {
            deleteSocialMessage(messageId);
            request.success(id, new JSONObject());
            return;
        }

        checkGroupAccess(getProfile().getClanId(), Clan.Permissions.ENGAGE_CONFLICT, new AccessCheckCallback()
        {
            @Override
            public void success(SocialService.Group.Participant participant)
            {
                JSONObject partySettings = new JSONObject();

                partySettings.put("clan-a", clanId);
                partySettings.put("clan-b", playerClient.getClanId());

                gameService.createParty(loginService.getCurrentAccessToken(),
                    "clans", partySettings, roomSettings, null,
                    conflictSize, null,
                    true, false, "conflict_closed",
                    (service, request1, status_, party) ->
                {
                    if (status_ != Request.Result.success)
                    {
                        request.error(id, status_.toString());
                        return;
                    }

                    String partyId = party.getId();

                    HashMap<String, JSONObject> profiles = new HashMap<>();

                    {
                        JSONObject profile = new JSONObject();
                        JSONObject conflict = new JSONObject();

                        conflict.put("@func", "not_exists");
                        conflict.put("@then", partyId);

                        profile.put("conflict", conflict);
                        profile.put("conflict-with", clanId);
                        profiles.put(playerClient.getClanId(), profile);
                    }

                    {
                        JSONObject profile = new JSONObject();
                        JSONObject conflict = new JSONObject();

                        conflict.put("@func", "not_exists");
                        conflict.put("@then", partyId);

                        profile.put("conflict", conflict);
                        profile.put("conflict-with", playerClient.getClanId());
                        profiles.put(clanId, profile);
                    }

                    socialService.updateGroupBatchProfiles(
                        loginService.getCurrentAccessToken(),
                        profiles, true,
                        (service2, request2, status, updatedProfiles) ->
                    {
                        deleteSocialMessage(messageId);

                        switch (status)
                        {
                            case success:
                            {
                                JSONObject result = new JSONObject();
                                result.put("party_id", partyId);

                                Set<String> flags = new HashSet<>();
                                flags.add("do_not_store");

                                {
                                    JSONObject msg = new JSONObject();
                                    msg.put("party_id", partyId);
                                    msg.put("group_id", playerClient.getClanId());

                                    playerClient.getMessageSession().sendMessage("social-group",
                                        clanId, "conflict_started", msg, flags);
                                }

                                {
                                    JSONObject msg = new JSONObject();
                                    msg.put("party_id", partyId);
                                    msg.put("group_id", clanId);

                                    playerClient.getMessageSession().sendMessage("social-group",
                                        playerClient.getClanId(), "conflict_started", msg, flags);
                                }

                                request.success(id, result);

                                break;
                            }
                            case conflict:
                            {
                                JSONObject message = new JSONObject();

                                gameService.closeParty(
                                    loginService.getCurrentAccessToken(),
                                    partyId,
                                    message,
                                    (service1, request3, status1) -> request.error(id,
                                            "Some of the factions are already in conflict."));
                                break;
                            }
                            default:
                            {
                                JSONObject message = new JSONObject();

                                gameService.closeParty(
                                    loginService.getCurrentAccessToken(),
                                    partyId,
                                    message,
                                    (service1, request3, status1) -> request.error(id,
                                            status.toString()));
                            }
                        }
                    });
                });
            }

            @Override
            public void failed()
            {
                request.error(id, "No permissions");
            }
        });
    }

    private void doChangeClanSummarySuccess(int id, ClientRequest request)
    {
        float amount = getProfile().getStats().get(Constants.Clans.CURRENCY_UPDATE_CLAN, 0.0f);
        int need = BrainOutServer.Settings.getPrice("updateClan");

        if (amount >= need)
        {
            float update = amount - need;

            playerClient.resourceEvent(-need, Constants.Clans.CURRENCY_UPDATE_CLAN, "purchase", "update-clan");
            getProfile().getStats().put(Constants.Clans.CURRENCY_UPDATE_CLAN, update);
            getProfile().setDirty();

            playerClient.sendUserProfile();

            request.success(id, new JSONObject());
        }
        else
        {
            request.error(id, "MENU_NOT_ENOUGH_SKILLPOINTS");
        }
    }

    private void handleFollowAvatarClan(JSONObject args, int id, ClientRequest request)
    {
        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "You are not in the clan");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        socialService.getGroupProfile(playerClient.getAccessToken(), getProfile().getClanId(),
            (service, request1, result, groupProfile, participant) ->
        {
            if (result == Request.Result.success && participant)
            {
                BrainOutServer.PostRunnable(() ->
                {
                    String url = groupProfile.optString("avatar", "");
                    getProfile().setClan(getProfile().getClanId(), url);
                    getProfile().setDirty();

                    JSONObject response = new JSONObject();
                    response.put("url", url);
                    request.success(id, response);
                });
            }
            else
            {
                request.error(id, request1.toString());
            }
        });
    }

    private void doJoinClassSuccess(String clanId, String avatar, int id, ClientRequest request)
    {
        float amount = getProfile().getStats().get(Constants.Clans.CURRENCY_JOIN_CLAN, 0.0f);
        int need = BrainOutServer.Settings.getPrice("joinClan");

        if (amount >= need)
        {
            float update = amount - need;

            playerClient.resourceEvent(-need, Constants.Clans.CURRENCY_JOIN_CLAN, "purchase", "join-clan");
            getProfile().getStats().put(Constants.Clans.CURRENCY_JOIN_CLAN, update);

            getProfile().setClan(clanId, avatar);
            getProfile().setDirty();

            playerClient.sendUserProfile();

            JSONObject args1 = new JSONObject();
            request.success(id, args1);
        }
        else
        {
            request.error(id, "MENU_NOT_ENOUGH_NUCLEAR_MATERIAL");
        }
    }

    private void doJoinClassRequestSuccess(int id, ClientRequest request)
    {
        float amount = getProfile().getStats().get(Constants.Clans.CURRENCY_JOIN_CLAN, 0.0f);
        int need = BrainOutServer.Settings.getPrice("joinClan");

        if (amount >= need)
        {
            float update = amount - need;

            playerClient.resourceEvent(-need, Constants.Clans.CURRENCY_JOIN_CLAN, "purchase", "join-clan");
            getProfile().getStats().put(Constants.Clans.CURRENCY_JOIN_CLAN, update);
            getProfile().setDirty();

            playerClient.sendUserProfile();

            JSONObject args1 = new JSONObject();
            request.success(id, args1);
        }
        else
        {
            request.error(id, "MENU_NOT_ENOUGH_NUCLEAR_MATERIAL");
        }
    }

    private void handleLeaveClan(JSONObject args, int id, ClientRequest request)
    {
        String clanId = getProfile().getClanId();

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        JSONObject notify = new JSONObject();
        if (playerClient.getAvatar() != null && !playerClient.getAvatar().isEmpty())
        {
            notify.put("avatar", playerClient.getAvatar());
        }
        notify.put("name", playerClient.getName());

        socialService.leaveGroup( playerClient.getAccessToken(), clanId, notify,
            (service, request1, result) ->
        {
            if (result == Request.Result.success)
            {
                BrainOutServer.PostRunnable(
                    () ->
                {
                    getProfile().leaveClan();
                    getProfile().setDirty();

                    playerClient.addStat("clans-left", 1);

                    playerClient.sendUserProfile();

                    JSONObject args1 = new JSONObject();
                    request.success(id, args1);
                });
            }
            else
            {
                request.error(id, result.toString());
            }
        });
    }


    private void handleKickClanMember(JSONObject args, int id, ClientRequest request)
    {
        if (!getProfile().isParticipatingClan())
        {
            request.error(id, "MENU_ERROR_NOT_IN_CLAN");
            return;
        }

        String clanId = getProfile().getClanId();
        String accountId = args.optString("account_id");
        String name = args.optString("name");

        if (accountId == null || name == null)
        {
            request.error(id, "bad_arguments");
            return;
        }

        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            request.error(id, "No social service");
            return;
        }

        JSONObject notify = new JSONObject();
        notify.put("kicked", true);
        notify.put("name", name);

        socialService.kickFromGroup(playerClient.getAccessToken(), clanId, accountId, notify,
                (service, request1, result) ->
                {
                    if (result == Request.Result.success)
                    {
                        request.success(id, new JSONObject());
                    }
                    else
                    {
                        request.error(id, result.toString());
                    }
                });
    }



    public boolean validateClanName(String clanName)
    {
        return !(clanName.length() <= 3 || clanName.length() >= 32);
    }

    private interface AccessCheckCallback
    {
        void success(SocialService.Group.Participant participant);
        void failed();
    }

    private void checkGroupAccess(String groupId, String access, AccessCheckCallback callback)
    {
        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            callback.failed();
            return;
        }

        socialService.getMyGroupParticipant(playerClient.getAccessToken(), groupId,
                (service, request, result, participant, owner) ->
                {
                    BrainOutServer.PostRunnable(() ->
                    {
                        if (result == Request.Result.success)
                        {
                            if (owner || participant.getPermissions().contains(access))
                            {
                                callback.success(participant);
                                return;
                            }
                        }

                        callback.failed();
                    });
                });
    }

    public interface ParticipationCheckCallback
    {
        void result(boolean yes);
    }

    public void checkGroupParticipation(String groupId, ParticipationCheckCallback callback)
    {
        SocialService socialService = SocialService.Get();

        if (socialService == null)
        {
            callback.result(false);
            return;
        }

        socialService.getMyGroupParticipant(playerClient.getAccessToken(), getProfile().getClanId(),
                (service, request, result, participant, owner) ->
                        callback.result(result == Request.Result.success));
    }


    public interface AvatarCheckCallback
    {
        void success();
        void failed();
    }

    public void checkAvatar(String avatar, AvatarCheckCallback checkCallback)
    {
        if (avatar.isEmpty())
        {
            checkCallback.success();
            return;
        }

        StaticService staticService = StaticService.Get();

        if (staticService == null)
        {
            checkCallback.failed();
            return;
        }

        URI a, b;

        try
        {
            a = new URI(avatar);
            b = new URI(staticService.getLocation());
        }
        catch (URISyntaxException e)
        {
            checkCallback.failed();
            return;
        }

        //if (a.getHost().endsWith("brainout.org") && a.getScheme().equals(b.getScheme()) &&
        //        a.getPort() == b.getPort())
        if (true)
        {
            Unirest.get(avatar).asBinaryAsync(new Callback<InputStream>()
            {
                @Override
                public void completed(HttpResponse<InputStream> httpResponse)
                {
                    BrainOutServer.PostRunnable(() ->
                    {
                        try
                        {
                            InputStream body = httpResponse.getBody();

                            if (body == null || body.available() > 1024 * 1024)
                            {
                                checkCallback.failed();
                            }
                            else
                            {
                                BufferedImage bf = ImageIO.read(body);

                                if (bf == null || bf.getWidth() > 128 || bf.getHeight() > 128)
                                {
                                    checkCallback.failed();
                                }
                                else
                                {
                                    checkCallback.success();
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            checkCallback.failed();
                        }
                    });
                }

                @Override
                public void failed(UnirestException e)
                {
                    BrainOutServer.PostRunnable(checkCallback::failed);
                }

                @Override
                public void cancelled()
                {
                    BrainOutServer.PostRunnable(checkCallback::failed);
                }
            });
        }
        else
        {
            checkCallback.failed();
        }
    }
}
