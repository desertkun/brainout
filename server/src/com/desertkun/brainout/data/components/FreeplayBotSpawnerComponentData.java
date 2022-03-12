package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.*;
import com.desertkun.brainout.bot.freeplay.HuntAround;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.components.ServerFreePartnerBotComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.FreeplayBotSpawnerComponent;
import com.desertkun.brainout.content.components.RandomWeightComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.PlayerSkinSlotItem;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.BotSpawnerData;
import com.desertkun.brainout.data.active.MarkerData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.mode.freeplay.OtherConsumables;
import com.desertkun.brainout.mode.freeplay.PrimaryWeaponAndAmmo;
import com.desertkun.brainout.mode.freeplay.RandomAmmo;
import com.desertkun.brainout.mode.freeplay.SecondaryWeaponAndAmmo;
import com.desertkun.brainout.online.Preset;
import com.esotericsoftware.minlog.Log;

public class FreeplayBotSpawnerComponentData extends Component<FreeplayBotSpawnerComponent>
{
    private final BotSpawnerData spawner;
    private final ObjectSet<ActiveData> spawned;
    private float checkTimer;

    private static Queue<ActiveData> ToRemove = new Queue<>();
    private static Queue<ActiveData> SpawnPoints = new Queue<>();
    private static Array<ActiveData> Candidates = new Array<>();

    private Array<PlayerSkinSlotItem> playerSkinsPool;

    public FreeplayBotSpawnerComponentData(BotSpawnerData spawnerData,
                                           FreeplayBotSpawnerComponent contentComponent)
    {
        super(spawnerData, contentComponent);

        this.spawner = spawnerData;
        this.checkTimer = 10;
        this.spawned = new ObjectSet<>();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        checkTimer -= dt;

        if (checkTimer < 0)
        {
            checkTimer = 10;
            check();
        }
    }

    private void check()
    {
        Map map = getMap();
        if (map == null)
            return;

        if (!getContentComponent().isEnabled())
            return;

        int alive = 0;

        for (ActiveData activeData : spawned)
        {
            if (!activeData.isAlive())
            {
                ToRemove.addLast(activeData);
                continue;
            }

            alive++;
        }

        if (ToRemove.size > 0)
        {
            for (ActiveData key : ToRemove)
            {
                spawned.remove(key);
            }

            ToRemove.clear();
        }

        if (alive < spawner.targetAmount)
        {
            spawn(spawner.targetAmount - alive, map);
        }
        else
        {
            checkTimer = 30;
        }
    }

    private void spawn(int amount, Map map)
    {
        SpawnPoints.clear();

        map.getListActivesGap(spawner.distance * spawner.distance,
                spawner.getX(), spawner.getY(), MarkerData.class, activeData ->
        {
            if (!spawner.tag.equals(((MarkerData) activeData).tag))
                return false;
            SpawnPoints.addLast(activeData);
            return false;
        });

        Candidates.clear();

        ObjectMap.Values<ActiveData> players = map.getActivesForTag(Constants.ActiveTags.PLAYERS, false);

        for (ActiveData spawnPoint : SpawnPoints)
        {
            boolean good = true;

            for (ActiveData player : players)
            {
                if (player.getOwnerId() < 0)
                    continue;

                float dst = Vector2.dst(player.getX(), player.getY(), spawnPoint.getX(), spawnPoint.getY());
                if (dst > 64)
                    continue;

                if (dst < 32 || spawnPoint.isVisible(player))
                {
                    good = false;
                    break;
                }
            }

            if (good)
            {
                Candidates.add(spawnPoint);
            }
        }

        if (Candidates.size > 0)
        {
            Candidates.shuffle();

            for (int i = 0; i < amount; i++)
            {
                if (Candidates.size == 0)
                    break;

                ActiveData spawnAt = Candidates.pop();
                spawn(spawnAt.getX(), spawnAt.getY(), map);
            }
        }

        SpawnPoints.clear();
        Candidates.clear();
    }

    @Override
    public void init()
    {
        super.init();

        initPools();
    }

    private void initPools()
    {
        Preset preset = BrainOutServer.Controller.getCurrentPreset();

        playerSkinsPool = BrainOutServer.ContentMgr.queryContent(PlayerSkinSlotItem.class,
            slot ->
        {
            if (RandomWeightComponent.Get(slot) == 0)
                return false;

            if (preset != null && !preset.isItemAllowed(slot))
                return false;

            return true;
        });
    }

    public PlayerSkinSlotItem getRandomPlayerSkin()
    {
        return getRandomItem(playerSkinsPool);
    }

    private <T extends Content> T getRandomItem(Array<T> items)
    {
        return RandomWeightComponent.GetRandomItem(items);
    }

    private void spawn(float spawnX, float spawnY, Map map)
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();
        if (!(gameMode.getRealization() instanceof ServerFreeRealization))
            return;

        ServerFreeRealization free = ((ServerFreeRealization) gameMode.getRealization());

        Player player = BrainOutServer.ContentMgr.get("player-freeplay", Player.class);
        Team team = BrainOutServer.ContentMgr.get("team-freeplay", Team.class);

        PlayerData playerData = (PlayerData)player.getData(map.getDimension());

        playerData.setTeam(team);

        BotControllerComponentData botController = new BotControllerComponentData(playerData);
        PlayerOwnerComponent ownerComponent = new PlayerOwnerComponent(playerData);
        PlayerRemoteComponent remoteComponent = new PlayerRemoteComponent(playerData);

        botController.getTasksStack().pushTask(new TaskCreator(botController.getTasksStack(),
            (stack, creator) -> {
                creator.pushTask(new HuntAround(stack, new IntSet()));
            }));

        playerData.addComponent(new ServerFreePartnerBotComponent(spawner.groupId));
        playerData.addComponent(botController);
        playerData.addComponent(ownerComponent);
        playerData.addComponent(remoteComponent);

        playerData.setPosition(spawnX, spawnY);
        playerData.setAngle(180);

        PlayerAnimationComponentData pac = playerData.getComponent(PlayerAnimationComponentData.class);
        pac.setSkin(getRandomPlayerSkin().getSkin());

        PrimaryWeaponAndAmmo.generate(free, ownerComponent.getConsumableContainer(), map.getDimension(), playerData);

        switch (MathUtils.random(3))
        {
            case 0:
            {
                RandomAmmo.generate(free, ownerComponent.getConsumableContainer());
                break;
            }
            case 1:
            {
                SecondaryWeaponAndAmmo.generate(free, ownerComponent.getConsumableContainer(), map.getDimension(), playerData);
                break;
            }
            default:
            {
                OtherConsumables.generate(free, ownerComponent.getConsumableContainer());
                break;
            }
        }

        {
            Weapon knife = BrainOutServer.ContentMgr.get("weapon-knife", Weapon.class);
            WeaponData weaponData = knife.getData(map.getDimension());
            ownerComponent.getConsumableContainer().putConsumable(1,
                new InstrumentConsumableItem(weaponData, map.getDimension()));
        }

        botController.selectFirstInstrument(ownerComponent);
        remoteComponent.setCurrentInstrument(ownerComponent.getCurrentInstrument());
        map.addActive(map.generateServerId(), playerData, true, true, ActiveData.ComponentWriter.TRUE);

        spawned.add(playerData);

        if (Log.INFO) Log.info("Spawned new bot at " + map.getDimension());
    }

    @Override
    public void release()
    {
        super.release();

        playerSkinsPool.clear();
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
