package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.KillND;
import com.desertkun.brainout.common.msg.server.KillMsg;
import com.desertkun.brainout.common.msg.server.KilledByMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.ServerBotWeaponComponent;
import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.block.contact.CSPlatform;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.components.ServerPlayerColliderComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.consumable.impl.PlayerSkinConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.ConcreteBD;
import com.desertkun.brainout.data.block.NonContactBD;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;
import com.desertkun.brainout.playstate.special.SpecialGame;
import com.desertkun.brainout.server.ServerConstants;

import java.util.TimerTask;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerPlayerColliderComponent")
@ReflectAlias("data.components.ServerPlayerColliderComponentData")
public class ServerPlayerColliderComponentData
    extends PlayerActiveColliderComponentData<ServerPlayerColliderComponent>
{
    private final PlayerData playerData;
    private Array<ConsumableRecord> toUpdate;
    private static Vector2 TMP = new Vector2();

    private float checkTimer;
    private int damageCounter;

    public ServerPlayerColliderComponentData(PlayerData activeData,
        ServerPlayerColliderComponent serverPlayerColliderComponent)
    {
        super(activeData, serverPlayerColliderComponent);

        this.playerData = activeData;
        this.toUpdate = new Array<>();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case destroy:
            {
                destroy(((DestroyEvent) event));

                break;
            }
        }

        return super.onEvent(event);
    }

    private void destroy(DestroyEvent event)
    {
        PlayerData playerData = ((PlayerData) activeData);
        Client killer = null;

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
        {
            return;
        }

        ActiveData.LastHitInfo lastHit = activeData.getLastHitInfo();
        killer = BrainOutServer.Controller.getClients().get(lastHit.hitterId);

        int ownerId = activeData.getOwnerId();
        if (ownerId >= 0)
        {
            Client client = BrainOutServer.Controller.getClients().get(ownerId);

            if (client != null)
            {
                InstrumentInfo info = lastHit.instrument;
                if (killer != null && info != null)
                {
                    Instrument instrument = info.instrument;

                    float slowmo = 0;

                    boolean allow = true;
                    boolean trackStats = client instanceof PlayerClient && killer instanceof PlayerClient;

                    if (!trackStats)
                    {
                        PlayerClient who;

                        if (client instanceof PlayerClient)
                        {
                            who = ((PlayerClient) client);
                        }
                        else if (killer instanceof PlayerClient)
                        {
                            who = ((PlayerClient) killer);
                        }
                        else
                        {
                            // literally not possible
                            who = null;
                        }

                        trackStats = who != null && who.catTrackStatsWithBots();
                    }

                    if (gameMode.enableKillTracking())
                    {
                        if (killer != client && killer instanceof PlayerClient)
                        {
                            PlayerClient playerKiller = ((PlayerClient) killer);
                            allow = playerKiller.trackKill(client);
                        }
                    }

                    if (gameMode.countDeaths() && allow)
                    {
                        if (killer.getPlayerData() != null && info.skin != null)
                        {
                            if (Vector2.dst(playerData.getX(), playerData.getY(),
                                killer.getPlayerData().getX(), killer.getPlayerData().getY()) > 64)
                            {
                                if (trackStats)
                                {
                                    killer.addStat("longshots", 1);
                                    killer.addStat(ContentLockTree.GetComplexValue("longshots-from", instrument.getID()), 1);

                                    if (killer.getPlayerData() != null && killer.getPlayerData().getCustomAnimationSlots() != null)
                                    {
                                        String mask = killer.getPlayerData().getCustomAnimationSlots().get("mask");
                                        if (mask != null)
                                        {
                                            killer.addStat("longshots-mask-" + mask, 1);
                                        }
                                    }
                                }

                                float a = BrainOutServer.getInstance().getSettings().getPrice("longshot");
                                killer.addScore(a, trackStats);

                                // notify killer for the kill
                                killer.notify(NotifyAward.score, a, NotifyReason.enemyLongShot, NotifyMethod.message, null);
                            }
                        }

                        if (killer.getLastHitInfo() != null)
                        {
                            if (killer != client)
                            {
                                if (!killer.isAlive() && killer.getLastHitInfo().hitterId == client.getId())
                                {
                                    float a = BrainOutServer.getInstance().getSettings().getPrice("headtohead");

                                    if (a > 0)
                                    {
                                        killer.addScore(a, trackStats);
                                        client.addScore(a, trackStats);

                                        if (trackStats)
                                        {
                                            killer.addStat("head-to-head-kills", 1);
                                            client.addStat("head-to-head-kills", 1);
                                        }

                                        killer.notify(NotifyAward.score, a, NotifyReason.enemyHeadToHead, NotifyMethod.message, null);
                                        client.notify(NotifyAward.score, a, NotifyReason.enemyHeadToHead, NotifyMethod.message, null);
                                    }
                                }
                            }
                        }

                        if (killer != client && killer.isEnemy(client.getTeam()))
                        {
                            killer.registerKill(trackStats);
                        }

                        if (killer == client && instrument.getID().equals("instrument-c4"))
                        {
                            if (client instanceof PlayerClient)
                            {
                                PlayerClient playerClient = ((PlayerClient) client);

                                if (playerClient.isEgg())
                                {
                                    BrainOut.Timer.schedule(new TimerTask()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            client.addStat("we-are-legion", 1);
                                        }
                                    }, 5000);

                                    playerClient.setEgg(false);
                                }
                            }
                        }

                        if (killer != client && !client.isSpectator() && killer.isEnemy(client))
                        {
                            killer.setKills(killer.getKills() + 1);

                            if (trackStats)
                            {
                                switch (killer.getKillsWithoutDeath())
                                {
                                    case 5:
                                    {
                                        killer.addStat("kill-streaks-5", 1);
                                        break;
                                    }
                                    case 10:
                                    {
                                        killer.addStat("kill-streaks-10", 1);
                                        break;
                                    }
                                }

                                for (String tag : instrument.getInstrumentTags())
                                {
                                    killer.addStat(Instrument.getTagKillsStat(tag), 1);
                                }

                                if (lastHit.silent)
                                {
                                    killer.addStat(Constants.Stats.SILENT_KILLS, 1);
                                }
                            }

                            if (trackStats && client.isParticipatingClan() && killer.isParticipatingClan() &&
                                !client.getClanId().equals(killer.getClanId()))
                            {
                                client.addStat("clan-deaths", 1);
                                killer.addStat("clan-kills", 1);

                                killer.notify(NotifyAward.clanScore, 1, NotifyReason.clanEnemyKilled,
                                    NotifyMethod.message, null);

                                client.addClanStat(Constants.Stats.DEATHS, 1);
                                killer.addClanStat(Constants.Stats.KILLS, 1);
                            }

                            if (BrainOutServer.Controller.isFreePlay())
                            {
                                killer.addStat("freeplay-kills", 1);
                            }

                            if (killer.getPlayerData() != null && killer.getPlayerData().getCustomAnimationSlots() != null)
                            {
                                String mask = killer.getPlayerData().getCustomAnimationSlots().get("mask");
                                if (mask != null)
                                {
                                    killer.addStat("kills-mask-" + mask, 1);

                                    if (instrument.getSlot() != null && instrument.getSlot().getID().equals("slot-melee"))
                                    {
                                        killer.addStat("knife-kills-mask-" + mask, 1);
                                    }

                                    if (lastHit.kind == ActiveData.LastHitKind.headshot)
                                    {
                                        killer.addStat("headshots-mask-" + mask, 1);
                                    }

                                    if (killer.getKillStreak() == 2)
                                    {
                                        killer.addStat("doublekills-mask-" + mask, 1);
                                    }
                                }
                            }

                            if (trackStats)
                            {
                                killer.setKillsInAGame(killer.getKillsInAGame() + 1);
                                if (killer.getKillsInAGame() == 15)
                                {
                                    killer.addStat("kills-15", 1);
                                }

                                if (killer.getKillsInAGame() == 2)
                                {
                                    killer.addStat("kills-2", 1);
                                }

                                if (killer.getKillsInAGame() == 5)
                                {
                                    killer.addStat("kills-5", 1);
                                }

                                if (killer.getKillsInAGame() == 10)
                                {
                                    killer.addStat("kills-10", 1);
                                }

                                killer.addStat(Constants.Stats.KILLS, 1);
                                killer.updateEfficiency();
                                killer.addStat(instrument.getKillsStat(), 1);
                            }


                            if (trackStats && lastHit.bullet != null)
                            {
                                for (String tag : lastHit.bullet.getBulletTags())
                                {
                                    killer.addStat(Bullet.getTagKillsStat(tag), 1);
                                }

                                killer.addStat(lastHit.bullet.getKillsStat(), 1);
                            }

                            if (((ServerRealization) gameMode.getRealization()).awardScores())
                            {
                                if (instrument.getSlot() != null && instrument.getSlot().getID().equals("slot-melee"))
                                {
                                    if (BrainOutServer.PackageMgr.getDefine("primary", "ok").equals("disabled") &&
                                        BrainOutServer.PackageMgr.getDefine("secondary", "ok").equals("disabled") &&
                                        BrainOutServer.PackageMgr.getDefine("special", "ok").equals("disabled"))
                                    {
                                        killer.addStat("kills-knife-only", 1);
                                    }

                                    float a = BrainOutServer.getInstance().getSettings().getPrice("knife");
                                    if (a > 0)
                                    {
                                        killer.addScore(a, trackStats);

                                        killer.notify(NotifyAward.score, a, NotifyReason.knifeKill, NotifyMethod.message, null);
                                    }
                                }

                                switch (lastHit.kind)
                                {
                                    case headshot:
                                    {
                                        if (trackStats)
                                        {
                                            killer.addStat("headshots", 1);
                                            killer.addStat(ContentLockTree.GetComplexValue("headshots-from", instrument.getID()), 1);
                                        }

                                        float a = BrainOutServer.getInstance().getSettings().getPrice("headshot");

                                        if (a > 0)
                                        {
                                            killer.addScore(a, trackStats);

                                            // notify killer for the kill
                                            killer.notify(NotifyAward.score, a, NotifyReason.enemyHeadShot, NotifyMethod.message,
                                                    new KillND(client.getId()));
                                        }

                                        break;
                                    }
                                    case normal:
                                    {
                                        float a = BrainOutServer.getInstance().getSettings().getPrice("kill");

                                        if (a > 0)
                                        {
                                            killer.addScore(a, trackStats);

                                            // notify killer for the kill
                                            killer.notify(NotifyAward.score, a, NotifyReason.enemyKilled, NotifyMethod.message,
                                                    new KillND(client.getId()));
                                        }

                                        break;
                                    }
                                }
                            }
                        }

                        if (killer.getKillStreak() == 3)
                        {
                            slowmo = 1.0f;
                        }
                    }

                    ServerRealization realization = (ServerRealization)gameMode.getRealization();

                    KillMsg msg = new KillMsg(killer.getId(),
                            client.getId(), info,
                            lastHit.kind, slowmo);

                    // and everyone for kill list
                    for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
                    {
                        Client c = entry.value;

                        if (!(c instanceof PlayerClient))
                            continue;

                        if (realization.reportKillMessage(c, killer, client))
                        {
                            ((PlayerClient) c).sendTCP(msg);
                        }
                    }

                    if (slowmo > 0)
                    {
                        BrainOutServer.Controller.applySlowMo(slowmo);
                    }

                    BrainOutServer.EventMgr.sendDelayedEvent(KillEvent.obtain(killer, client, instrument));

                    if (killer instanceof PlayerClient)
                    {
                        ModePayload payload = killer.getModePayload();
                        if (payload instanceof FreePayload)
                        {
                            FreePayload freePayload = ((FreePayload) payload);
                            freePayload.questEvent(KillEvent.obtain(killer, client, instrument));
                        }
                    }

                    ActiveData killerData = killer.getPlayerData();

                    if (killerData != null)
                    {
                        if (client instanceof PlayerClient)
                        {
                            ((PlayerClient) client).sendTCP(new KilledByMsg(killerData, info));
                        }
                    }

                }

                PlayState playState = BrainOutServer.Controller.getPlayState();

                if (playState instanceof ServerPSGame)
                {
                    ServerPSGame psGame = ((ServerPSGame) playState);

                    psGame.doCheck();

                    for (SpecialGame game : psGame.getSpecialGames())
                    {
                        game.onClientDeath(client, playerData);
                    }
                }

                client.clearPlayerData();

                Client finalKiller = killer;
                BrainOutServer.PostRunnable(() -> client.onDeath(finalKiller, playerData, info));
            }
        }
        else
        {
            if (gameMode.getRealization() instanceof ServerRealization)
            {
                ServerRealization r = ((ServerRealization) gameMode.getRealization());
                r.onUnknownPlayerDeath(playerData, killer);
            }
        }

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc != null && event.ragdoll)
        {
            String fullDrop = BrainOutServer.Settings.getFullDrop();

            if (fullDrop == null)
            {
                if (BrainOutServer.Controller.isDropEnabled(playerData))
                {
                    ConsumableRecord current = poc.getCurrentInstrumentRecord();

                    if (current != null)
                    {
                        if (current.getItem() instanceof InstrumentConsumableItem)
                        {
                            InstrumentConsumableItem ici = ((InstrumentConsumableItem) current.getItem());
                            InstrumentData instrumentData = ici.getInstrumentData();

                            if (ici.getContent().isThrowable())
                            {
                                Array<ConsumableRecord> records = new Array<>();

                                ServerWeaponComponentData swc = instrumentData.getComponent(ServerWeaponComponentData.class);

                                if (swc != null)
                                {
                                    ConsumableContainer container = poc.getConsumableContainer();

                                    for (ServerWeaponComponentData.Slot slot : swc.getSlots().values())
                                    {
                                        if (slot.getBullet() != null)
                                        {
                                            ConsumableRecord bulletRecord = container.queryConsumable(slot.getBullet());

                                            if (bulletRecord != null)
                                            {
                                                ConsumableRecord r = new ConsumableRecord(
                                                    bulletRecord.getItem(), bulletRecord.getAmount(), 0);
                                                r.setQuality(bulletRecord.getQuality());
                                                records.add(r);
                                            }
                                        }
                                    }

                                }

                                ConsumableRecord r = new ConsumableRecord(ici, current.getAmount(), 0);
                                r.setQuality(current.getQuality());
                                records.add(r);

                                Item dropItem = null;

                                if (instrumentData.getInstrument().hasComponent(ItemComponent.class))
                                {
                                    ItemComponent itemComponent =
                                            instrumentData.getInstrument().getComponent(ItemComponent.class);

                                    dropItem = itemComponent.getDropItem();
                                }

                                ItemData itemData = ServerMap.dropItem(playerData.getDimension(),
                                    dropItem, records,
                                    ownerId, playerData.getX(), playerData.getY(),
                                    (float) Math.random() * 360f, ServerConstants.Drop.DROP_SPEED_DEATH);

                                if (itemData != null)
                                {
                                    if (killer != null)
                                    {
                                        itemData.setProperty("killer", String.valueOf(killer.getId()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());
                if (!serverRealization.isFullDropEnabled(playerData))
                    return;

                Item dropItem = BrainOutServer.ContentMgr.get(fullDrop, Item.class);
                Client client = BrainOutServer.Controller.getClients().get(ownerId);
                Array<ConsumableRecord> records = new Array<>();
                Array<Integer> toRemove = new Array<>();

                for (ObjectMap.Entry<Integer, ConsumableRecord> entry : poc.getConsumableContainer().getData())
                {
                    ConsumableRecord record = entry.value;
                    ConsumableItem item = record.getItem();

                    if (!serverRealization.canDropConsumable(client, item))
                        continue;

                    ItemComponent itemComponent = item.getContent().getComponent(ItemComponent.class);

                    if (itemComponent != null && itemComponent.getDropItem() == null)
                        continue;

                    if (item instanceof InstrumentConsumableItem)
                    {
                        InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);

                        if (!ici.getContent().isThrowable())
                        {
                            continue;
                        }

                        InstrumentData instrumentData = ici.getInstrumentData();

                        if (instrumentData == null)
                            continue;

                        ServerBotWeaponComponent bcmp = instrumentData.getComponent(ServerBotWeaponComponent.class);
                        if (bcmp != null)
                        {
                            instrumentData.removeComponent(bcmp, true);
                        }
                    }

                    records.add(record);
                    toRemove.add(entry.key);
                }

                for (Integer key : toRemove)
                {
                    poc.getConsumableContainer().getData().remove(key);
                }

                if (BrainOutServer.Controller.isFreePlay())
                {
                    if (client instanceof PlayerClient)
                    {
                        ConsumableContent dogTags =
                                BrainOutServer.ContentMgr.get("freeplay-dog-tags", ConsumableContent.class);

                        if (dogTags != null)
                        {
                            ConsumableRecord tags = null;

                            for (ConsumableRecord record : records)
                            {
                                if (record.getItem().getContent().getID().equals("freeplay-dog-tags"))
                                {
                                    tags = record;
                                    tags.setAmount(tags.getAmount() + 1);
                                    break;
                                }
                            }

                            if (tags == null)
                            {
                                tags = new ConsumableRecord(dogTags.acquireConsumableItem(), 1, 0);
                                records.add(tags);
                            }
                        }

                        PlayerAnimationComponentData pac = playerData.getComponent(PlayerAnimationComponentData.class);

                        if (pac != null)
                        {
                            PlayerSkin playerSkin = pac.getSkin();
                            records.add(new ConsumableRecord(new PlayerSkinConsumableItem(playerSkin), 1, -1));
                        }
                    }
                }

                if (records.size > 0)
                {
                    ServerMap.dropItem(playerData.getDimension(), dropItem, records,
                            ownerId, playerData.getX(), playerData.getY(),
                            270, ServerConstants.Drop.DROP_SPEED_DEATH);
                }
            }
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc != null)
        {
            ConsumableContainer cc = poc.getConsumableContainer();

            toUpdate.clear();

            for (ObjectMap.Entry<Integer, ConsumableRecord> entry: cc.getData())
            {
                ConsumableRecord record = entry.value;

                if (record.getItem() instanceof InstrumentConsumableItem)
                {
                    toUpdate.add(record);
                }
            }

            for (ConsumableRecord record: toUpdate)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
                InstrumentData instrumentData = ici.getInstrumentData();
                instrumentData.update(dt);
            }
        }

        Map map = getMap();

        checkTimer -= dt;

        if (checkTimer < 0)
        {
            checkTimer = 0.25f;

            TMP.set(0, 0);

            int blockX = (int)playerData.getX(), blockY = (int)playerData.getY();

            BlockData bodyBlock = map.getBlockAt(
                blockX, blockY, Constants.Layers.BLOCK_LAYER_FOREGROUND);

            boolean contact;

            if (bodyBlock != null)
            {
                if (bodyBlock instanceof NonContactBD)
                {
                    contact = false;
                }
                else if (bodyBlock.isConcrete() && ((ConcreteBD) bodyBlock).getContactShape() instanceof CSPlatform)
                {
                    contact = false;
                }
                else
                {
                    contact = bodyBlock.isContact(null, playerData.getX() % 1.0f,
                        playerData.getY() % 1.0f, TMP, TMP, 0, map, blockX, blockY);
                }
            }
            else
            {
                contact = false;
            }

            if (contact)
            {
                damageCounter += 1;
            }
            else
            {
                damageCounter = 0;
            }

            if (damageCounter > 10)
            {
                LaunchData l = playerData.getLaunchData();

                BrainOutServer.EventMgr.sendDelayedEvent(playerData, DamageEvent.obtain(
                    100.0f, -1, null, null,
                    l.getX(), l.getY(), MathUtils.random(360), Constants.Damage.DAMAGE_HIT));
            }
        }

    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
