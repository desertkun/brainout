package com.desertkun.brainout.mode;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.msg.server.ShootingRangeCompletedMsg;
import com.desertkun.brainout.common.msg.server.ShootingRangeHitMsg;
import com.desertkun.brainout.common.msg.server.ShootingRangeStartedMsg;
import com.desertkun.brainout.common.msg.server.ShootingRangeWarmupMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Layout;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.ShootingRange;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.shop.*;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.ShootingRangeData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.events.BlockHitConfirmationEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.mode.payload.LobbyPayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.online.RegularServerEvent;
import com.desertkun.brainout.online.ServerEvent;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateEndGame;

import java.util.TimerTask;

public class ServerLobbyRealization extends ServerRealization<GameModeLobby> implements EventReceiver,
        LobbyPayload.ShootingRangeCompletedCallback
{
    private ObjectMap<String, TargetGroup> targetGroups;
    private Array<TimerTask> targets;

    public ServerLobbyRealization(GameModeLobby gameMode)
    {
        super(gameMode);

        this.targetGroups = new ObjectMap<>();
    }

    public class TargetGroup
    {
        private final String group;
        private Array<ActiveData> spawners;
        private BlockData currentBlock;
        private int currentBlockX, currentBlockY;
        private Block closedBlock;
        private String currentDimension;

        public TargetGroup(String group)
        {
            this.group = group;
            this.spawners = new Array<>();
            this.currentBlock = null;
        }

        public void setup()
        {
            for (ActiveData spawner : spawners)
            {
                TargetBlocksSpawnerComponentData sp = spawner.getComponent(TargetBlocksSpawnerComponentData.class);

                if (sp == null)
                    continue;

                Map map = spawner.getMap();

                if (map == null)
                    continue;

                Block block = sp.getClosedBlock();

                int x = (int)spawner.getX(), y = (int)spawner.getY();

                BlockData newBlock = block.getBlock();

                map.setBlock(x, y, newBlock,
                    Constants.Layers.BLOCK_LAYER_FOREGROUND, true);
            }
        }

        public void spawn()
        {
            if (currentBlock != null && closedBlock != null)
            {
                Map map = Map.Get(currentDimension);

                if (map != null)
                {
                    BlockData closed = closedBlock.getBlock();
                    map.setBlock(currentBlockX, currentBlockY, closed, Constants.Layers.BLOCK_LAYER_FOREGROUND, true, 300);
                }

                currentBlock = null;
                closedBlock = null;

                return;
            }

            ActiveData random = spawners.random();

            if (random == null)
            {
                return;
            }

            TargetBlocksSpawnerComponentData sp = random.getComponent(TargetBlocksSpawnerComponentData.class);

            if (sp == null)
            {
                spawners.removeValue(random, true);
                spawn();
                return;
            }

            Block block = sp.getOpenedBlock();

            int x = (int)random.getX(), y = (int)random.getY();

            Map map = random.getMap();

            if (map == null)
                return;

            currentBlock = block.getBlock();
            currentDimension = random.getDimension();
            currentBlockX = x;
            currentBlockY = y;
            closedBlock = sp.getClosedBlock();

            ShootingRangeTargetBlockComponentData bcb =
                currentBlock.getComponent(ShootingRangeTargetBlockComponentData.class);

            if (bcb != null)
            {
                bcb.setGroup(group);
            }

            map.setBlock(x, y, currentBlock, Constants.Layers.BLOCK_LAYER_FOREGROUND, true);
        }

        public void addSpawner(ActiveData spawner)
        {
            this.spawners.add(spawner);
        }
    }

    @Override
    public ModePayload newPlayerPayload(Client playerClient)
    {
        return new LobbyPayload(playerClient);
    }

    @Override
    public boolean needWayPoints()
    {
        return false;
    }

    @Override
    public void init(PlayState.InitCallback callback)
    {
        for (Map map : Map.All())
        {
            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.TARGET_SPAWNER, false))
            {
                TargetBlocksSpawnerComponentData sp = activeData.getComponent(TargetBlocksSpawnerComponentData.class);

                if (sp == null)
                    continue;

                TargetGroup tg = targetGroups.get(sp.getTargetGroup());

                if (tg == null)
                {
                    tg = new TargetGroup(sp.getTargetGroup());
                    targetGroups.put(sp.getTargetGroup(), tg);
                }

                tg.addSpawner(activeData);
            }

        }

        targets = new Array<>();

        setupTargets();

        for (ObjectMap.Entry<String, TargetGroup> group : targetGroups)
        {
            group.value.setup();
        }

        BrainOutServer.EventMgr.subscribe(Event.ID.blockHitConfirmation, this);

        super.init(callback);
    }

    private void setupTargets()
    {
        setupTarget("primary", 2000, 1200);
        setupTarget("secondary", 2000, 1000);
        setupTarget("sniper", 1500, 1500);
        setupTarget("base", 1500, 2000);
    }

    private void spawnTargets(String name)
    {
        TargetGroup targetGroup = targetGroups.get(name);

        if (targetGroup == null)
            return;

        targetGroup.spawn();
    }

    private void setupTarget(String name, int delay, int period)
    {
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() -> spawnTargets(name));
            }
        };

        BrainOutServer.Timer.schedule(task, delay, period);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutServer.EventMgr.unsubscribe(Event.ID.blockHitConfirmation, this);

        for (TimerTask task : targets)
        {
            task.cancel();
        }
    }

    @Override
    public void clientInitialized(Client client, boolean reconnected)
    {
        super.clientInitialized(client, reconnected);

        if (client instanceof PlayerClient)
        {
            PlayerClient playerClient = ((PlayerClient) client);

            playerClient.sendUserProfile();

            respawn(playerClient);
        }
    }

    private void respawn(PlayerClient player)
    {
        ActiveData spawnable = null;

        for (Map map : Map.All())
        {
            spawnable = map.getRandomActiveForTag(Constants.ActiveTags.SPAWNABLE);

            if (spawnable instanceof Spawnable)
                break;
        }

        if (!(spawnable instanceof Spawnable))
            return;

        Spawnable asSpawnable = ((Spawnable) spawnable);

        ShopCart shopCart = player.getShopCart();
        shopCart.clear();

        ClientProfile profile = player.getProfile();
        if (profile == null)
            return;

        Layout selectedLayout = profile.getLayout();

        if (selectedLayout == null)
        {
            selectedLayout = BrainOut.ContentMgr.get("layout-1", Layout.class);
        }

        shopCart.initSelection(profile, selectedLayout, false);

        player.setSpawnAt(asSpawnable);
        player.doSpawn();
    }

    @Override
    public void onClientDeath(Client client, Client killer, PlayerData playerData, InstrumentInfo info)
    {
        if (client instanceof PlayerClient)
        {
            PlayerClient playerClient = ((PlayerClient) client);

            ModePayload modePayload = playerClient.getModePayload();

            if (!(modePayload instanceof LobbyPayload))
                return;

            LobbyPayload lobbyPayload = ((LobbyPayload) modePayload);

            if (lobbyPayload.isInTargetPracticing())
            {
                lobbyPayload.resetPracticing();
            }

            BrainOutServer.Timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    BrainOutServer.PostRunnable(() -> respawn(((PlayerClient) client)));
                }
            }, 100);
        }
        else
        {
            super.onClientDeath(client, killer, playerData, info);
        }
    }

    @Override
    public void onSelectionUpdated(PlayerClient playerClient)
    {
        fillUpSelection(playerClient);
    }

    private void fillUpSelection(PlayerClient playerClient)
    {
        ModePayload modePayload = playerClient.getModePayload();

        if (!(modePayload instanceof LobbyPayload))
            return;

        LobbyPayload lobbyPayload = ((LobbyPayload) modePayload);

        if (lobbyPayload.isInTargetPracticing())
            return;

        if (!playerClient.isAlive())
            return;

        PlayerData playerData = playerClient.getPlayerData();

        if (playerData == null)
            return;

        PlayerAnimationComponentData pac = playerData.getComponent(PlayerAnimationComponentData.class);
        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        ServerPlayerControllerComponentData pcc =
            playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

        if (poc == null || pcc == null || pac == null)
            return;

        ConsumableContainer cc = poc.getConsumableContainer();
        cc.clear();

        ShopCart shopCart = playerClient.getShopCart();

        Shop shop = Shop.getInstance();

        for (ObjectMap.Entry<Slot, SlotItem.Selection> slotItem : shopCart.getItems())
        {
            if (!shop.getSlots().contains(slotItem.key, true))
                continue;

            applySelection(playerClient, shopCart, playerData, slotItem.key, slotItem.value);
        }

        Slot playerSkinSlot = BrainOutServer.ContentMgr.get("slot-player-skin", Slot.class);

        if (playerSkinSlot != null)
        {
            SlotItem.Selection selection = shopCart.getItem(playerSkinSlot);

            if (selection instanceof PlayerSkinSlotItem.PlayerSkinSlotSelection)
            {
                PlayerSkinSlotItem.PlayerSkinSlotSelection skin =
                        ((PlayerSkinSlotItem.PlayerSkinSlotSelection) selection);

                pac.setSkin(skin.getItem().getSkin());
            }
        }

        pcc.consumablesUpdated();
        pcc.selectFirstInstrument(poc);
        pac.updated(playerData);

        playerClient.getProfile().setDirty();
        playerClient.sendUserProfile();
    }

    private void applySelection(PlayerClient playerClient, ShopCart shopCart,
                                PlayerData playerData, Slot slot, SlotItem.Selection selection)
    {
        UserProfile profile = playerClient.getProfile();

        SlotItem item = selection.getItem();

        boolean have = profile == null || item.hasItem(profile);

        if (have)
        {
            selection.apply(shopCart, playerData, profile, slot, selection);
        }
    }

    @Override
    public boolean isComplete(PlayStateEndGame.GameResult gameResult)
    {
        return false;
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
    }

    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public SpawnMode canSpawn(Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case blockHitConfirmation:
            {
                BlockHitConfirmationEvent ev = ((BlockHitConfirmationEvent) event);

                confirmBlockHit(ev);

                break;
            }
        }

        return super.onEvent(event);
    }

    public boolean startShootingRange(PlayerClient playerClient, String key, String weapon)
    {
        if (!playerClient.isAlive())
            return false;

        PlayerData playerData = playerClient.getPlayerData();

        if (playerData == null)
            return false;

        ActiveData activeData = null;

        for (Map map : Map.All())
        {
            activeData = map.getActiveForTag(Constants.ActiveTags.SHOOTING_RANGE,
                activeData1 ->
            {
                if (!(activeData1.getContent() instanceof ShootingRange))
                    return false;

                ShootingRange shootingRange = ((ShootingRange) activeData1.getContent());

                return shootingRange.getID().equals(key);
            });

            if (activeData != null)
                break;
        }

        if (!(activeData instanceof ShootingRangeData))
            return false;

        PlayerAnimationComponentData pac = playerData.getComponent(PlayerAnimationComponentData.class);
        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        ServerPlayerControllerComponentData pcc =
            playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

        if (poc == null || pcc == null || pac == null)
            return false;

        ModePayload modePayload = playerClient.getModePayload();

        if (!(modePayload instanceof LobbyPayload))
            return false;

        LobbyPayload lobbyPayload = ((LobbyPayload) modePayload);

        if (lobbyPayload.isInTargetPracticing())
            return false;

        ShootingRangeData shootingRangeData = ((ShootingRangeData) activeData);

        ShootingRange shootingRange = ((ShootingRange) shootingRangeData.getCreator());

        if (shootingRange == null)
            return false;

        if (!shootingRange.hasWeapon(weapon))
            return false;

        lobbyPayload.setCurrentTarget(shootingRange.getGroup());
        lobbyPayload.setCurrentWeapon(weapon);

        ConsumableContainer cc = poc.getConsumableContainer();
        cc.clear();

        ShootingRange.WeaponPreset preset = shootingRange.getWeapons().get(weapon);

        for (ObjectMap.Entry<ConsumableContent, Integer> item : preset.items)
        {
            cc.putConsumable(item.value, item.key.acquireConsumableItem());
        }

        InstrumentInfo info = preset.info;
        InstrumentData data = info.instrument.getData(playerData.getDimension());
        data.setSkin(preset.info.skin);

        for (ObjectMap.Entry<String, Upgrade> entry : preset.info.upgrades)
        {
            data.getUpgrades().put(entry.key, entry.value);
        }

        cc.putConsumable(1, new InstrumentConsumableItem(data, playerData.getDimension()));
        cc.init();

        playerClient.moveTo(shootingRangeData.getDimension(), shootingRangeData.getX(), shootingRangeData.getY());

        BrainOutServer.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() ->
                {
                    playerClient.enablePlayer(false);

                    playerClient.log("Starting shooting range group " + shootingRange.getGroup() + " weapon " + weapon);

                    pcc.consumablesUpdated();
                    pcc.selectFirstInstrument(poc);
                    pac.updated(playerData);

                    final int WARMUP_TIME = 7;

                    TimerTask warmup = new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            BrainOutServer.PostRunnable(() ->
                            {
                                int time = shootingRange.getTime();

                                lobbyPayload.startPracticing(shootingRangeData, time, ServerLobbyRealization.this);

                                playerClient.sendTCP(new ShootingRangeStartedMsg(time));
                                playerClient.enablePlayer(true);
                                playerClient.log("Shooting range go!");
                            });
                        }
                    };

                    BrainOutServer.Timer.schedule(warmup, WARMUP_TIME * 1000L);
                    playerClient.sendTCP(new ShootingRangeWarmupMsg(WARMUP_TIME));
                });
            }
        }, 1000);



        return true;
    }

    @Override
    public boolean canDropConsumable(Client playerClient, ConsumableItem item)
    {
        return false;
    }

    private void confirmBlockHit(BlockHitConfirmationEvent ev)
    {
        ActiveData sender = ev.sender;

        if (sender == null)
            return;

        if (ev.block == null)
            return;

        ShootingRangeTargetBlockComponentData sr = ev.block.getComponent(ShootingRangeTargetBlockComponentData.class);

        if (sr == null)
            return;

        Client client = BrainOutServer.Controller.getClients().get(sender.getOwnerId());

        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        ModePayload modePayload = playerClient.getModePayload();

        if (!(modePayload instanceof LobbyPayload))
            return;

        LobbyPayload lobbyPayload = ((LobbyPayload) modePayload);

        if (!lobbyPayload.isInTargetPracticing())
            return;

        if (!lobbyPayload.getCurrentTarget().equals(sr.getGroup()))
            return;

        int hits = lobbyPayload.hit();
        playerClient.sendTCP(new ShootingRangeHitMsg(hits));
    }

    @Override
    public void checkWarmUp()
    {
        // do noting
    }

    @Override
    public void shootingRangeComplete(LobbyPayload lobbyPayload, String currentTarget, String currentWeapon, int hits)
    {
        Client client = lobbyPayload.getPlayerClient();

        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        playerClient.sendTCP(new ShootingRangeCompletedMsg(hits));
        playerClient.log("Shooting range complete, hits: " + hits);

        fillUpSelection(playerClient);

        for (ObjectMap.Entry<Integer, ServerEvent> entry : playerClient.getOnlineEvents())
        {
            ServerEvent event = entry.value;

            if (!event.getEvent().isValid())
                continue;

            if (!(event instanceof RegularServerEvent))
            {
                continue;
            }

            RegularServerEvent r = ((RegularServerEvent) event);

            if (!r.taskAction.equals(Constants.Other.SHOOTING_RANGE_ACTION))
                continue;

            if (!r.taskData.equals(currentWeapon))
                continue;

            r.addScoreMaximum(hits);

            playerClient.notify(NotifyAward.none, 0,
                NotifyReason.shootingRangeCompleted, NotifyMethod.message, null);

            break;
        }
    }

    @Override
    public boolean shootingRangeWatchdog(LobbyPayload playerClient, float initialX, float initialY)
    {
        PlayerData playerData = playerClient.getPlayerClient().getPlayerData();

        if (playerData == null)
            return false;

        if (Vector2.dst2(playerData.getX(), playerData.getY(), initialX, initialY) > 100)
        {
            return false;
        }

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return false;

        ConsumableContainer coc = poc.getConsumableContainer();

        boolean hasAmmo = false;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : coc.getData())
        {
            ConsumableItem item = entry.value.getItem();

            if (item.getContent() instanceof Bullet)
            {
                hasAmmo = true;
                break;
            }

            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);

                ServerWeaponComponentData se = ici.getInstrumentData().getComponent(ServerWeaponComponentData.class);

                for (ObjectMap.Entry<String, ServerWeaponComponentData.Slot> slotEntry : se.getSlots())
                {
                    if (slotEntry.value.getRounds() > 0)
                    {
                        hasAmmo = true;
                        break;
                    }
                }

                if (hasAmmo)
                {
                    break;
                }
            }
        }

        return hasAmmo;
    }

    @Override
    public boolean isFullDropEnabled(PlayerData playerData)
    {
        return false;
    }

    @Override
    public boolean isDeathDropEnabled(PlayerData playerData)
    {
        return false;
    }

    @Override
    public boolean needRolesForBots()
    {
        return false;
    }
}
