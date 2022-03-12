package com.desertkun.brainout.client;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.RandomWeightComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.*;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.BotControllerComponentData;
import com.desertkun.brainout.data.components.ServerPhysicsSyncComponentData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.online.Preset;
import com.desertkun.brainout.server.ServerController;
import org.json.JSONObject;

public class BotClient extends Client
{
    private float spawnTimer;
    private Array<InstrumentSlotItem> primaryWeaponsPool;
    private Array<InstrumentSlotItem> secondaryWeaponsPool;
    private Array<PlayerSkinSlotItem> playerSkinsPool;
    private Role role;
    private String name;

    public BotClient(int id, ServerController serverController)
    {
        super(id, serverController);

        role = Role.unset;

        primaryWeaponsPool = new Array<>();
        secondaryWeaponsPool = new Array<>();
        playerSkinsPool = new Array<>();

        name = "";
    }

    public enum Role
    {
        unset,
        protect,
        assault,
        hunter
    }

    public Role getRole()
    {
        return role;
    }

    public BotClient setRole(Role role)
    {
        this.role = role;

        log("Changed role to: " + role.toString());

        return this;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        makeSureIsSpawned(dt);
    }

    public void init()
    {
        initName();
        initMap();
        initPools();

        BrainOutServer.PostRunnable(this::sendRemotePlayers);

        log("Bot initialized!");
    }

    public JSONObject getInfo()
    {
        JSONObject info = new JSONObject();
        info.put("bot", true);
        return info;
    }

    private void initName()
    {
        while (true)
        {
            String name = BrainOutServer.Env.getBotNames().random();

            boolean badOne = false;

            for (ObjectMap.Entry<Integer, Client> client : BrainOutServer.Controller.getClients())
            {
                if (client.value.getName().equals(name))
                {
                    badOne = true;
                    break;
                }
            }

            if (badOne)
                continue;

            this.name = name;

            break;
        }
    }

    private void initMap()
    {
        completelyInitialized();

        if (isInitialized())
            return;

        mapInitialized();

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return;

        ServerRealization serverRealization = (ServerRealization) gameMode.getRealization();

        if (serverRealization != null)
        {
            serverRealization.clientInitialized(this, false);
            serverRealization.checkWarmUp();

            if (serverRealization.needRolesForBots())
            {
                updateRoles();
            }
        }
    }

    private void updateRoles()
    {
        ObjectMap<Role, Array<BotClient>> botRoles = new ObjectMap<>();

        int totalCount = 0;

        for (ObjectMap.Entry<Integer, Client> entry : getServerController().getClients())
        {
            Client client = entry.value;

            if (!(client instanceof BotClient))
                continue;

            BotClient botClient = ((BotClient) client);

            if (BrainOutServer.Controller.isEnemies(client.getId(), this.getId()))
                continue;

            Array<BotClient> q = botRoles.get(botClient.getRole());

            if (q == null)
            {
                q = new Array<>();
                botRoles.put(botClient.getRole(), q);
            }

            q.add(botClient);
            totalCount++;
        }

        int needProtect, needAssault, needHunter;

        switch (totalCount)
        {
            case 1:
            {
                needAssault = 1;
                needProtect = 0;
                needHunter = 0;
                break;
            }
            case 2:
            {
                needAssault = 1;
                needProtect = 1;
                needHunter = 0;
                break;
            }
            case 3:
            {
                needAssault = 2;
                needProtect = 1;
                needHunter = 0;
                break;
            }
            case 4:
            default:
            {
                needAssault = totalCount - 2;
                needProtect = 1;
                needHunter = 1;
                break;
            }
        }
        Array<BotClient> unset = botRoles.get(Role.unset);
        if (unset == null)
        {
            unset = new Array<>();
            botRoles.put(Role.unset, unset);
        }

        // release those who don't fit
        {
            Array<BotClient> q = botRoles.get(Role.protect);
            if (q != null && q.size > needProtect)
            {
                for (int i = needProtect; i < q.size; i++)
                    unset.add(q.get(i).setRole(Role.unset));

                q.setSize(needProtect);
            }
        }
        {
            Array<BotClient> q = botRoles.get(Role.assault);
            if (q != null && q.size > needAssault)
            {
                for (int i = needAssault; i < q.size; i++)
                    unset.add(q.get(i).setRole(Role.unset));

                q.setSize(needAssault);
            }
        }
        {
            Array<BotClient> q = botRoles.get(Role.hunter);
            if (q != null && q.size > needHunter)
            {
                for (int i = needHunter; i < q.size; i++)
                    unset.add(q.get(i).setRole(Role.unset));

                q.setSize(needHunter);
            }
        }

        // add to those who need
        {
            Array<BotClient> q = botRoles.get(Role.protect);
            if (q == null || q.size < needProtect)
            {
                int cnt = q != null ? q.size : 0;
                for (int i = cnt; i < needProtect; i++)
                    if (unset.size > 0)
                        unset.pop().setRole(Role.protect);
            }
        }
        {
            Array<BotClient> q = botRoles.get(Role.assault);
            if (q == null || q.size < needAssault)
            {
                int cnt = q != null ? q.size : 0;
                for (int i = cnt; i < needAssault; i++)
                    if (unset.size > 0)
                        unset.pop().setRole(Role.assault);
            }
        }
        {
            Array<BotClient> q = botRoles.get(Role.hunter);
            if (q == null || q.size < needHunter)
            {
                int cnt = q != null ? q.size : 0;
                for (int i = cnt; i < needHunter; i++)
                    if (unset.size > 0)
                        unset.pop().setRole(Role.hunter);
            }
        }
    }

    public InstrumentSlotItem getRandomPrimaryWeapon()
    {
        return getRandomItem(primaryWeaponsPool);
    }

    public InstrumentSlotItem getRandomSecondaryWeapon()
    {
        return getRandomItem(secondaryWeaponsPool);
    }

    public PlayerSkinSlotItem getRandomPlayerSkin()
    {
        return getRandomItem(playerSkinsPool);
    }

    private void initPools()
    {
        Preset preset = BrainOutServer.Controller.getCurrentPreset();

        primaryWeaponsPool = BrainOutServer.ContentMgr.queryContent(InstrumentSlotItem.class,
            slot ->
        {
            if (RandomWeightComponent.Get(slot) == 0)
                return false;

            if (preset != null && !preset.isItemAllowed(slot))
                return false;

            Instrument instrument = slot.getInstrument();

            if (!(instrument instanceof Weapon))
                return false;

            Weapon weapon = ((Weapon) instrument);

            if (slot.getSlot() == null)
                return false;

            if (slot.getDefaultSkin() == null)
                return false;

            if (weapon.getPrimaryProperties().isUnlimited())
                return false;

            if (!slot.getSlot().getID().equals("slot-primary"))
                return false;

            return true;
        });

        secondaryWeaponsPool = BrainOutServer.ContentMgr.queryContent(InstrumentSlotItem.class,
            slot ->
        {
            if (RandomWeightComponent.Get(slot) == 0)
                return false;

            if (preset != null && !preset.isItemAllowed(slot))
                return false;

            Instrument instrument = slot.getInstrument();

            if (!(instrument instanceof Weapon))
                return false;

            Weapon weapon = ((Weapon) instrument);

            if (slot.getSlot() == null)
                return false;

            if (slot.getDefaultSkin() == null)
                return false;

            if (weapon.getPrimaryProperties().isUnlimited())
                return false;

            if (!slot.getSlot().getID().equals("slot-secondary"))
                return false;

            return true;
        });

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

    private <T extends Content> T getRandomItem(Array<T> items)
    {
        return RandomWeightComponent.GetRandomItem(items);
    }

    private void mapInitialized()
    {
        setState(State.mapInitialized);
    }

    protected float spawnCheckTimer()
    {
        return 1f;
    }

    private void makeSureIsSpawned(float dt)
    {
        spawnTimer -= dt;

        if (spawnTimer > 0)
            return;

        spawnTimer = spawnCheckTimer();

        if (isAlive())
            return;

        if (BrainOutServer.Controller.isQueuedForSpawning(this))
            return;

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return;

        ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());

        Array<Spawnable> spawnables = new Array<>();

        for (Map map : Map.All())
        {
            if (!isMapOkayToSpawn(map))
                continue;

            map.countActivesForTag(Constants.ActiveTags.SPAWNABLE, activeData ->
            {
                if (!(activeData instanceof Spawnable))
                    return false;

                Spawnable asSpawnable = ((Spawnable) activeData);

                if (!asSpawnable.canSpawn(getTeam()))
                    return false;

                spawnables.add(asSpawnable);

                return true;
            });
        }

        if (spawnables.size == 0)
            return;

        Spawnable spawnable = serverRealization.chooseBotSpawnPoint(spawnables);

        if (spawnable ==  null)
            return;

        setupShopCart();

        setSpawnAt(spawnable);

        boolean extraWave = spawnable instanceof FlagData;
        getServerController().respawn(this, extraWave);

        log("Respawning myself");
    }

    protected boolean isMapOkayToSpawn(Map map)
    {
        if (map.isSafeMap())
            return false;
        return true;
    }

    @Override
    protected void sendServerChat(String header, String message, Color color)
    {
        //
    }

    @Override
    public void reset()
    {
        super.reset();

        BrainOutServer.PostRunnable(this::initMap);
    }

    @Override
    protected void applySelection(ShopCart shopCart, PlayerData playerData,
                                  Slot slot, SlotItem.Selection selection)
    {
        selection.apply(shopCart, playerData, null, slot, selection);
    }

    @Override
    protected void beforeInit(PlayerData playerData)
    {
        super.beforeInit(playerData);

        BotControllerComponentData bot = new BotControllerComponentData(getPlayerData());
        getPlayerData().addComponent(bot);

        ServerPhysicsSyncComponentData sync = getPlayerData().getComponent(ServerPhysicsSyncComponentData.class);
        if (sync != null)
        {
            getPlayerData().removeComponent(sync);
        }

        PlayerOwnerComponent ownerComponent = playerData.getComponent(PlayerOwnerComponent.class);
        PlayerRemoteComponent remoteComponent = playerData.getComponent(PlayerRemoteComponent.class);

        ServerPlayerControllerComponentData ctl =
            playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);
        if (ctl != null)
        {
            ctl.selectFirstInstrument(ownerComponent);
        }

        remoteComponent.setCurrentInstrument(ownerComponent.getCurrentInstrument());
        playerData.setCurrentInstrument(ownerComponent.getCurrentInstrument());

    }

    protected String getBotContent()
    {
        return "sl-pl-bot";
    }

    protected void setupShopCart()
    {
        getShopCart().clear();
        getShopCart().addDefaultItems();

        Slot playerSlot = BrainOutServer.ContentMgr.get("slot-player", Slot.class);
        PlayerSlotItem botSlotItem = BrainOutServer.ContentMgr.get(getBotContent(), PlayerSlotItem.class);

        if (playerSlot != null && botSlotItem != null)
        {
            getShopCart().selectItem(playerSlot, botSlotItem.getStaticSelection());
        }

        Slot primarySlot = BrainOutServer.ContentMgr.get("slot-primary", Slot.class);
        InstrumentSlotItem primaryWeapon = getRandomPrimaryWeapon();
        if (primaryWeapon != null)
        {
            getShopCart().selectItem(primarySlot, primaryWeapon.getStaticSelection());
        }

        Slot secondarySlot = BrainOutServer.ContentMgr.get("slot-secondary", Slot.class);
        InstrumentSlotItem secondaryWeapon = getRandomSecondaryWeapon();
        if (secondaryWeapon != null)
        {
            getShopCart().selectItem(secondarySlot, secondaryWeapon.getStaticSelection());
        }

        Slot playerSkinSlot = BrainOutServer.ContentMgr.get("slot-player-skin", Slot.class);
        PlayerSkinSlotItem playerSkin = getRandomPlayerSkin();
        if (playerSkin != null)
        {
            getShopCart().selectItem(playerSkinSlot, playerSkin.getStaticSelection());
        }
    }
}
