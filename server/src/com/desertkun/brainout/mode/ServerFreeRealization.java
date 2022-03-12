package com.desertkun.brainout.mode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskCrow;
import com.desertkun.brainout.bot.freeplay.TaskFreePlay;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.bot.freeplay.TaskZombie;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.common.msg.VoiceChatMsg;
import com.desertkun.brainout.common.msg.client.FreePlayChangeSkinMsg;
import com.desertkun.brainout.common.msg.SetFriendlyStatusMsg;
import com.desertkun.brainout.common.msg.client.FreePlayPlayAgain;
import com.desertkun.brainout.common.msg.client.SummaryReadyMsg;
import com.desertkun.brainout.common.msg.client.cards.*;
import com.desertkun.brainout.common.msg.server.*;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.components.ServerFreePartnerBotComponent;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.*;
import com.desertkun.brainout.content.consumable.*;
import com.desertkun.brainout.content.consumable.impl.*;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.content.shop.*;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.*;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.mode.freeplay.*;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.online.FreePlayContainer;
import com.desertkun.brainout.online.RoomSettings;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateEndGame;
import com.desertkun.brainout.playstate.ServerPSGame;
import com.desertkun.brainout.utils.FPUtils;
import com.desertkun.brainout.utils.MarketUtils;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.GameService;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.MarketService;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.StringBuilder;
import java.util.*;

public class ServerFreeRealization extends ServerRealization<GameModeFree>
{
    private Array<ActiveData> spawnPool;

    public class Party
    {
        private final String partyId;
        private Spawnable home;
        private final ObjectMap<String, PlayerClient> members;

        public Party(String partyId)
        {
            this.partyId = partyId;
            this.members = new ObjectMap<>();

            init();
        }

        public ObjectMap<String, PlayerClient> getMembers()
        {
            return members;
        }

        public void addMember(PlayerClient playerClient)
        {
            String account = playerClient.getAccount();

            if (account == null)
                return;

            synchronized (members)
            {
                members.put(account, playerClient);
            }

            if (Log.INFO) Log.info("New player joined party " + this.partyId + ": " + account);
        }

        public Spawnable getHome()
        {
            return home;
        }

        public void setHome(Spawnable home)
        {
            this.home = home;
        }

        public boolean removeMember(PlayerClient playerClient)
        {
            String account = playerClient.getAccount();

            synchronized (members)
            {
                if (account != null)
                {
                    members.remove(account);
                    if (Log.INFO) Log.info("Player left party " + this.partyId + ": " + account);
                }

                return members.size > 0;
            }
        }

        public void init()
        {
        }

        public void release()
        {
            if (Log.INFO) Log.info("Party " + this.partyId + " has been released");
        }
    }

    private ObjectMap<String, Party> parties;

    private ObjectMap<String, Array<ItemData>> crateItems = new ObjectMap<>();
    private ObjectSet<ItemData> tmp2 = new ObjectSet<>();
    private ObjectMap<String, Array<PlayWithPartnerAgainCallback>> ongoingPartyLookups;

    private Array<InstrumentSlotItem> primaryWeaponsPool;
    private Array<InstrumentSlotItem> secondaryWeaponsPool;
    private Array<InstrumentSlotItem> specialItemsPool;
    private Array<ConsumableToOwnableContent> containersPool;
    private Array<ConsumableContent> consumables;
    private Array<ConsumableContent> junk;
    private Array<Bullet> bulletPool;
    private Array<ActiveData> rain;
    private FreeplayDropoff dropOff;
    private Array<ActiveData> crickets;
    private ActiveData returnDoor;
    private SpawnPointData spawnPointData;
    //private SpawnBots spawnBots;

    private TimerTask alarm;
    private int playersAttended;
    private int exp;
    private Array<TimerTask> tasks;
    private float timeOfDayOffset;

    private TimerTask wavesSyncTimer;
    private ObjectMap<String, WaveWrapper> waves;

    public ServerFreeRealization(GameModeFree gameMode)
    {
        super(gameMode);

        waves = new ObjectMap<>();
        parties = new ObjectMap<>();
        ongoingPartyLookups = new ObjectMap<>();
        tasks = new Array<>();
        crickets = new Array<>();
        rain = new Array<>();

        parseSpecialItems();
        //parseSpawnBots();

        playersAttended = 0;
        timeOfDayOffset = 0;
    }

    private class WaveWrapper
    {
        private final String dimension;
        private ActiveData a, b;

        public WaveWrapper(String dimension, ActiveData a, ActiveData b)
        {
            this.dimension = dimension;
            this.a = a;
            this.b = b;
        }

        public ActiveData getA()
        {
            return a;
        }

        public ActiveData getB()
        {
            return b;
        }

        public float getDistance()
        {
            return Math.abs(b.getX() - a.getX());
        }

        public void setPower(float v)
        {
            if (a == null || b == null)
                return;

            WindComponentData acmp = a.getComponent(WindComponentData.class);
            WindComponentData bcmp = b.getComponent(WindComponentData.class);

            if (acmp == null || bcmp == null)
                return;

            acmp.setPower(v);
            bcmp.setPower(v);
        }

        public void release()
        {
            Map map = Map.Get(dimension);

            map.removeActive(a, true, true, false);
            map.removeActive(b, true, true, false);
        }

        public void updated()
        {
            a.updated();
            b.updated();
        }
    }

    /*
    private void parseSpawnBots()
    {
        JSONArray special = null;

        try
        {
            special = new JSONArray(Gdx.files.local("freeplay-bots.json").readString());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        if (special != null)
        {
            spawnBots = new SpawnBots(special);
        }
    }

     */


    private void parseSpecialItems()
    {
        {
            JSONObject special = null;

            try
            {
                special = new JSONObject(Gdx.files.local("freeplay-dropoff.json").readString());
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            if (special != null)
            {
                dropOff = new FreeplayDropoff(special);
            }
        }
    }

    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.allowed;
    }

    private ServerDeckOfCardsComponentData findDeck(CardMessage msg)
    {
        Map map = Map.Get(msg.d);
        if (map == null)
            return null;
        ActiveData activeData = map.getActiveData(msg.o);
        if (activeData == null)
            return null;
        return activeData.getComponent(ServerDeckOfCardsComponentData.class);
    }

    @SuppressWarnings("unused")
    public boolean received(final FreePlayPlayAgain msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            PlayerData me = messageClient.getPlayerData();
            if (me != null && me.isAlive())
                return;

            spawnPlayer(spawnPointData, messageClient);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final GetTable msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.listen(messageClient.getId());
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final LeaveTable msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.stopListening(messageClient.getId());
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final DiscardAllCards msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.discardAllCards(messageClient.getId());
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ResetTable msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.resetGame(messageClient.getId());
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final DiscardCard msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.discardCard(messageClient.getId(), msg.card);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final FlipCard msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.flipCard(messageClient.getId(), msg.card);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final JoinCards msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            if (deck.join(messageClient.getId(), msg.place))
            {
                ModePayload payload = messageClient.getModePayload();
                if (!(payload instanceof FreePayload))
                    return;
                FreePayload freePayload = ((FreePayload) payload);
                freePayload.questEvent(FreePlayItemUsedEvent.obtain(messageClient,
                    deck.getComponentObject().getContent(), 1));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final GiveCardToPlayerFromTable msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.giveCardToPlayerFromTable(messageClient.getId(), msg.card, msg.player, msg.animate);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final GiveCardToPlayerFromDeck msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.giveCardToPlayerFromDeck(messageClient.getId(), msg.player, msg.animate);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final TakeCardOffDeckOntoTable msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.takeCardOffDeckOntoTable(messageClient.getId(), msg.x, msg.y);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final MoveCardOnTable msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.moveCardOnTable(messageClient.getId(), msg.card, msg.x, msg.y, msg.animation);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PlaceCardOnTableFromHand msg)
    {
        final PlayerClient messageClient = getMessageClient();
        BrainOutServer.PostRunnable(() ->
        {
            ServerDeckOfCardsComponentData deck = findDeck(msg);
            if (deck == null)
                return;

            deck.placeCardOnTableFromHand(messageClient.getId(), msg.f, msg.x, msg.y);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SetFriendlyStatusMsg msg)
    {
        final PlayerClient messageClient = getMessageClient();
        final boolean friendly = msg.friendly;

        BrainOutServer.PostRunnable(() ->
        {
            ModePayload py = messageClient.getModePayload();
            if (!(py instanceof FreePayload))
                return;
            FreePayload fr = ((FreePayload) py);

            fr.setFriendly(friendly);
            messageClient.sendRemotePlayers(false);

        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final FreePlayChangeSkinMsg msg)
    {
        final PlayerClient messageClient = getMessageClient();

        BrainOutServer.PostRunnable(() ->
        {
            if (getGameMode().getPhase() != GameMode.Phase.game)
                return;

            changeSkin(messageClient, msg.object);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SummaryReadyMsg msg)
    {
        final PlayerClient messageClient = getMessageClient();

        BrainOutServer.PostRunnable(() ->
        {
            if (getGameMode().getPhase() != GameMode.Phase.game)
                return;

            messageClient.setReady(true);
            messageClient.updateRemotePlayers();
            messageClient.sendRemotePlayers(false);
        });

        return true;
    }

    private void initPools()
    {
        primaryWeaponsPool = BrainOutServer.ContentMgr.queryContent(InstrumentSlotItem.class,
            slot ->
        {
            if (RandomWeightComponent.Get(slot) == 0)
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

        specialItemsPool = BrainOutServer.ContentMgr.queryContent(InstrumentSlotItem.class,
            slot ->
        {
            if (RandomWeightComponent.Get(slot) == 0)
                return false;

            Instrument instrument = slot.getInstrument();

            if (instrument instanceof Weapon)
                return false;

            if (instrument.getDefaultSkin() == null)
                return false;

            if (slot.getSlot() == null)
                return false;

            if (!slot.getSlot().getID().equals("slot-special"))
                return false;

            return true;
        });

        bulletPool = BrainOutServer.ContentMgr.queryContent(Bullet.class,
            bullet -> RandomWeightComponent.Get(bullet) != 0);

        containersPool = BrainOutServer.ContentMgr.queryContent(ConsumableToOwnableContent.class,
            consumable -> RandomWeightComponent.Get(consumable) != 0);

        consumables = BrainOutServer.ContentMgr.queryContent(ConsumableContent.class,
            consumable -> {
                if (RandomWeightComponent.Get(consumable) == 0)
                    return false;

                return consumable.getClass() == ConsumableContent.class;
            });

        consumables.sort((o1, o2) ->
        {
            RandomWeightComponent w1 = o1.getComponent(RandomWeightComponent.class);
            RandomWeightComponent w2 = o2.getComponent(RandomWeightComponent.class);

            return (int)(w2.getWeight() - w1.getWeight());
        });

        if (Log.INFO)
        {
            Log.info("Random consumables sorted by weight:");
            Log.info("--------");

            for (Content content : consumables)
            {
                RandomWeightComponent weightComponent = content.getComponent(RandomWeightComponent.class);
                if (weightComponent == null)
                    return;
                Log.info(content.getID() + " = " + weightComponent.getWeight());
            }
            Log.info("--------");
        }

        junk = BrainOutServer.ContentMgr.queryContent(ConsumableContent.class,
            consumable -> consumable.getClass() == ConsumableContent.class &&
                consumable.getID().startsWith("consumable-item-junk-"));
    }

    private int getPrimaryWeaponsInConsumableContainer(ConsumableContainer cnt)
    {
        int have = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();
            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
                InstrumentData instrumentData = ici.getInstrumentData();
                if (instrumentData instanceof WeaponData)
                {
                    WeaponData weaponData = ((WeaponData) instrumentData);

                    Slot slot = weaponData.getWeapon().getSlot();
                    if (slot != null && slot.getID().equals("slot-primary"))
                    {
                        have += record.getAmount();
                    }
                }
            }
        }

        return have;
    }

    private int getConsumablesInConsumableContainer(ConsumableContainer cnt, String tag)
    {
        int have = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();

            Content c = item.getContent();

            if (c.getClass() == ConsumableContent.class)
            {
                ItemComponent itemComponent = c.getComponent(ItemComponent.class);
                boolean match = false;
                if (itemComponent != null)
                {
                    match = itemComponent.getTags(c) != null && itemComponent.getTags(c).contains(tag, false);
                    if (!match)
                        continue;
                }

                if (!match)
                {
                    RandomWeightComponent w = c.getComponent(RandomWeightComponent.class);

                    if (w != null)
                    {
                        if (w.getWeight() == 0)
                            continue;
                    }
                }

                have += record.getAmount();
            }
        }

        return have;
    }

    private int getConsumablesInConsumableContainerID(ConsumableContainer cnt, String id)
    {
        int have = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();

            Content c = item.getContent();

            if (!c.getID().startsWith(id))
                continue;

            if (c.getClass() == ConsumableContent.class)
            {
                RandomWeightComponent w = c.getComponent(RandomWeightComponent.class);

                if (w != null)
                {
                    if (w.getWeight() == 0)
                        continue;
                }

                have += record.getAmount();
            }
        }

        return have;
    }

    private int getJunkInConsumableContainer(ConsumableContainer cnt)
    {
        int have = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();

            if (item.getContent().getID().startsWith("consumable-item-junk"))
            {
                have += record.getAmount();
            }
        }

        return have;
    }

    private int getContainersInConsumableContainer(ConsumableContainer cnt)
    {
        int have = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();
            if (item.getContent() instanceof ConsumableToOwnableContent)
            {
                ConsumableToOwnableContent cc = ((ConsumableToOwnableContent) item.getContent());

                if (cc.getOwnableContent().getID().equals("case-daily"))
                    continue;

                have += record.getAmount();
            }
        }

        return have;
    }

    private int getSecondaryWeaponsInConsumableContainer(ConsumableContainer cnt)
    {
        int have = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();
            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
                InstrumentData instrumentData = ici.getInstrumentData();
                if (instrumentData instanceof WeaponData)
                {
                    WeaponData weaponData = ((WeaponData) instrumentData);

                    Slot slot = weaponData.getWeapon().getSlot();
                    if (slot != null && slot.getID().equals("slot-secondary"))
                    {
                        have += record.getAmount();
                    }
                }
            }
        }

        return have;
    }

    private int getSpecialInstrumentsInConsumableContainer(ConsumableContainer cnt)
    {
        int have = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();
            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
                InstrumentData instrumentData = ici.getInstrumentData();

                Slot slot = instrumentData.getInstrument().getSlot();
                if (slot != null && slot.getID().equals("slot-special"))
                {
                    have += record.getAmount();
                }
            }
        }

        return have;
    }

    private int getBulletsInConsumableContainer(ConsumableContainer cnt)
    {
        int have = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();
            if (item instanceof DefaultConsumableItem)
            {
                DefaultConsumableItem dci = ((DefaultConsumableItem) item);

                if (dci.getContent() instanceof Bullet)
                {
                    have += record.getAmount();
                }
            }
        }

        return have;
    }

    private int primaryWeaponsHave()
    {
        int have = 0;

        for (Map map : Map.All())
        {
            if (map.isSafeMap())
                continue;

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ItemData itemData = ((ItemData) activeData);
                have += getPrimaryWeaponsInConsumableContainer(itemData.getRecords());
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
            {
                PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

                if (poc == null)
                    continue;

                have += getPrimaryWeaponsInConsumableContainer(poc.getConsumableContainer());
            }
        }

        return have;
    }

    private int containersHave()
    {
        int have = 0;

        for (Map map : Map.All())
        {
            if (map.isSafeMap())
                continue;

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ItemData itemData = ((ItemData) activeData);
                have += getContainersInConsumableContainer(itemData.getRecords());
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
            {
                PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

                if (poc == null)
                    continue;

                have += getContainersInConsumableContainer(poc.getConsumableContainer());
            }
        }

        return have;
    }

    private int consumablesHave(String tag)
    {
        int have = 0;

        for (Map map : Map.All())
        {
            if (map.isSafeMap())
                continue;

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ItemData itemData = ((ItemData) activeData);
                have += getConsumablesInConsumableContainer(itemData.getRecords(), tag);
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
            {
                PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

                if (poc == null)
                    continue;

                have += getConsumablesInConsumableContainer(poc.getConsumableContainer(), tag);
            }
        }

        return have;
    }

    private int consumablesHaveID(String id)
    {
        int have = 0;

        for (Map map : Map.All())
        {
            if (map.isSafeMap())
                continue;

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ItemData itemData = ((ItemData) activeData);
                have += getConsumablesInConsumableContainerID(itemData.getRecords(), id);
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
            {
                PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

                if (poc == null)
                    continue;

                have += getConsumablesInConsumableContainerID(poc.getConsumableContainer(), id);
            }
        }

        return have;
    }

    private int secondaryWeaponsHave()
    {
        int have = 0;

        for (Map map : Map.All())
        {
            if (map.isSafeMap())
                continue;

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ItemData itemData = ((ItemData) activeData);
                have += getSecondaryWeaponsInConsumableContainer(itemData.getRecords());
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
            {
                PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

                if (poc == null)
                    continue;

                have += getSecondaryWeaponsInConsumableContainer(poc.getConsumableContainer());
            }
        }

        return have;
    }

    private int specialInstrumentsHave()
    {
        int have = 0;

        for (Map map : Map.All())
        {
            if (map.isSafeMap())
                continue;

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ItemData itemData = ((ItemData) activeData);
                have += getSpecialInstrumentsInConsumableContainer(itemData.getRecords());
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
            {
                PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

                if (poc == null)
                    continue;

                have += getSpecialInstrumentsInConsumableContainer(poc.getConsumableContainer());
            }
        }

        return have;
    }

    private int bulletsHave()
    {
        int have = 0;

        for (Map map : Map.All())
        {
            if (map.isSafeMap())
                continue;

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ItemData itemData = ((ItemData) activeData);
                have += getBulletsInConsumableContainer(itemData.getRecords());
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
            {
                PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

                if (poc == null)
                    continue;

                have += getBulletsInConsumableContainer(poc.getConsumableContainer());
            }
        }

        return have;
    }

    private int containersNeed()
    {
        return 2;
    }

    @Override
    public void onShuttingDown()
    {
        super.onShuttingDown();

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;
            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);

                if (playerClient.getProfile() != null)
                {
                    FreePlayContainer onHand = playerClient.getProfile().getContainers().get("h");
                    if (onHand != null)
                    {
                        onHand.setSaveEmpty(false);
                    }
                }
            }
        }
    }

    private int primaryWeaponsNeeded()
    {
        int containers = 0;

        for (Map map : Map.All())
        {
            if (map.isSafeMap())
            {
                continue;
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ServerItemComponentData ic = activeData.getComponent(ServerItemComponentData.class);

                if (ic == null)
                    continue;

                if (!ic.getTarget().equals("freeplay"))
                    continue;

                containers++;
            }
        }

        int weaponsNeeded = containers / 30;
        int players = countInitializedPlayers();
        weaponsNeeded += 8 + players;
        return weaponsNeeded;
    }

    protected int specialInstrumentsNeeded()
    {
        int instrumentsNeeded = countInitializedPlayers();
        return instrumentsNeeded / 2 + 1;
    }

    protected int secondaryWeaponsNeeded()
    {
        int containers = 0;

        for (Map map : Map.All())
        {
            if (map.isSafeMap())
            {
                continue;
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ServerItemComponentData ic = activeData.getComponent(ServerItemComponentData.class);

                if (ic == null)
                    continue;

                if (!ic.getTarget().equals("freeplay"))
                    continue;

                containers++;
            }
        }

        int players = countInitializedPlayers();
        int weaponsNeeded = containers / 48;
        weaponsNeeded += 4 + players;
        return weaponsNeeded;
    }

    private void processItems()
    {
        crateItems.clear();

        for (Map map : Map.All())
        {
            if (map.isSafeMap())
            {
                continue;
            }

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ItemData itemData = ((ItemData) activeData);

                ServerItemComponentData ic = activeData.getComponent(ServerItemComponentData.class);

                if (ic == null)
                    continue;

                if (!ic.getTarget().equals("freeplay"))
                    continue;

                String tag = itemData.tag;

                Array<ItemData> array = crateItems.get(tag, null);

                if (array == null)
                {
                    array = new Array<>();
                    crateItems.put(tag, array);
                }

                array.add(((ItemData) activeData));
            }
        }

        for (ObjectMap.Entry<String, Array<ItemData>> entry : crateItems)
        {
            entry.value.shuffle();
        }
    }

    public ItemData getRandomItem(String tag)
    {
        Array<ItemData> array = crateItems.get(tag);

        if (array == null || array.size == 0)
            return null;

        return array.random();
    }

    public Array<ItemData> getRandomItems(String tag)
    {
        return crateItems.get(tag);
    }

    private void generateSpecialItems()
    {
        if (FreePlayItemsContent.Get() != null)
        {
            if (Log.INFO) Log.info("Generating freeplay items");

            FreePlayItemsContent.Get().enforce(this::query);
        }
    }

    private static Array<ItemData> query_tmp = new Array<>();

    private Array<ItemData> query(String tag, boolean noPlayer)
    {
        if (noPlayer)
        {
            Array<ItemData> original = crateItems.get(tag);

            query_tmp.clear();

            for (ItemData itemData : original)
            {
                Map map = itemData.getMap();
                if (map == null)
                    continue;

                if (map.countActivesForTag(Constants.ActiveTags.PLAYERS) > 0)
                    continue;

                query_tmp.add(itemData);
            }

            if (query_tmp.size == 0)
            {
                return null;
            }

            return query_tmp;
        }
        else
        {
            return crateItems.get(tag);
        }
    }

    private void generateItems()
    {
        processItems();

        tmp2.clear();

        int primaryWeaponsNeeded = primaryWeaponsNeeded();
        int secondaryWeaponsNeeded = secondaryWeaponsNeeded();
        int containersNeeded = containersNeed();
        int primaryWeaponsHave = primaryWeaponsHave();
        int secondaryWeaponsHave = secondaryWeaponsHave();
        int containersHave = containersHave();

        if (Log.INFO) Log.info("Stats: prim need " + primaryWeaponsNeeded +
                " sec need " + secondaryWeaponsNeeded +
                " cnt need " + containersNeeded +
                " prim have " + primaryWeaponsHave +
                " sec have " + secondaryWeaponsHave +
                " cnt have "+ containersHave);

        {
            for (int i = primaryWeaponsHave; i < primaryWeaponsNeeded; i++)
            {
                ItemData itemData = getRandomItem("");

                if (itemData == null)
                    continue;

                PrimaryWeaponAndAmmo.generate(this, itemData.getRecords(), itemData.getDimension());
                tmp2.add(itemData);
            }
        }

        {
            for (int i = secondaryWeaponsHave; i < secondaryWeaponsNeeded; i++)
            {
                ItemData itemData = getRandomItem("");

                if (itemData == null)
                    continue;

                SecondaryWeaponAndAmmo.generate(this, itemData.getRecords(), itemData.getDimension());
                tmp2.add(itemData);
            }
        }

        {
            for (int i = containersHave; i < containersNeeded; i++)
            {
                ItemData itemData = getRandomItem("");

                if (itemData == null)
                    continue;

                Containers.generate(this, itemData.getRecords());
                tmp2.add(itemData);
            }
        }

        for (ItemData itemData : tmp2)
        {
            itemData.updated();
        }
    }

    private void generateConsumables()
    {
        int primaryWeaponsNeeded = primaryWeaponsNeeded();
        int secondaryWeaponsNeeded = secondaryWeaponsNeeded();

        {
            int ammoNeed = (primaryWeaponsNeeded + secondaryWeaponsNeeded);
            int ammoHave = bulletsHave() / 15;

            if (Log.INFO) Log.info("Ammo need " + ammoNeed + " have " + ammoHave);

            if (ammoNeed > ammoHave)
            {
                int add = ammoNeed - ammoHave;

                for (int i = 0; i < add; i++)
                {
                    ItemData itemData = getRandomItem("");

                    if (itemData == null)
                        continue;

                    RandomAmmo.generate(this, itemData.getRecords());
                    tmp2.add(itemData);
                }
            }
        }

        {
            int specialNeeded = specialInstrumentsNeeded();
            int specialHave = specialInstrumentsHave();

            if (Log.INFO) Log.info("special need " + specialNeeded + " have " + specialHave);

            if (specialNeeded > specialHave)
            {
                int add = specialNeeded - specialHave;

                for (int i = 0; i < add; i++)
                {
                    ItemData itemData = getRandomItem("");

                    if (itemData == null)
                        continue;

                    SpecialInstruments.generate(this, itemData.getRecords(), itemData.getDimension());
                    tmp2.add(itemData);
                }
            }
        }
    }

    private void generateNewItems()
    {
        if (Log.INFO) Log.info("--- Generating new items");

        tmp2.clear();

        processItems();

        if (Log.INFO)
        {
            StringBuilder b = new StringBuilder();

            for (ObjectMap.Entry<String, Array<ItemData>> entry : crateItems)
            {
                b.append(entry.value.size).append(" of '").append(entry.key).append("' ");
            }

            Log.info("Containers available: " + b.toString());
        }

        generateItems();
        generateConsumables();
        generateFireFuel();

        for (ItemData itemData : tmp2)
        {
            itemData.updated();
        }

        if (Log.INFO) Log.info("--- Generating new items done");
    }

    private void placeItemOnTheGround(String item)
    {
        ConsumableContent cnt = BrainOutServer.ContentMgr.get(item, ConsumableContent.class);
        Item dropItem = cnt.getComponent(ItemComponent.class).getDropItem();

        Array<ConsumableRecord> records = new Array<>();
        ConsumableRecord r = new ConsumableRecord(cnt.acquireConsumableItem(), 1, 0);
        QualityComponent q = cnt.getComponent(QualityComponent.class);
        if (q != null)
        {
            r.setQuality(q.pick());
        }
        records.add(r);

        Map map = Map.Get(dims.random());
        float x = MathUtils.random(64, map.getWidth() - 64);

        float y = map.getHeight() - 1;

        while (map.getBlockAt(x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND) == null)
        {
            y -= 1;

            if (y < 0)
            {
                return;
            }
        }

        y += 1;

        ServerMap.dropItem(map.getDimension(), dropItem, records, -1, x, y, 0);

        if (Log.INFO) Log.info("Placing item " + item + " on " + map.getDimension() + " at " + x + ", " + y);
    }

    private void ensureItemsOnTheGround(String item, int consumablesNeed)
    {
        int have = consumablesHaveID(item);

        if (consumablesNeed > have)
        {
            int add = consumablesNeed - have;

            for (int i = 0; i < add; i++)
            {
                placeItemOnTheGround(item);
            }
        }
    }

    private void generateFireFuel()
    {
        ensureItemsOnTheGround("consumable-item-boards", 20);
        ensureItemsOnTheGround("consumable-item-log", 20);
        ensureItemsOnTheGround("consumable-item-sticks", 20);
    }

    @Override
    public void init(PlayState.InitCallback callback)
    {
        super.init(callback);

        initPools();
        spawnDoors();
        generateNewItems();
        updateTimeOfDay(false);
        updateRain();
        spawnCrows();
        generateSpecialItems();
        changeSafeCode(false, false);

        scheduleTask(() -> updateTimeOfDay(true), 30000, 30000);
        scheduleTask(this::updateRain, 30000, 30000);
        scheduleTask(this::generateNewItems, 10000, 900000);
        scheduleTask(this::generateSpecialItems, 10000, 900000);
        scheduleTask(this::spendAMinute, 60000, 60000);
        scheduleTask(this::updateCrickets, 2000, 2000);
        scheduleTask(this::syncWaves, 5000, 5000);
        scheduleTask(this::spawnCrows, 2000, 2000);
        //scheduleTask(this::spawnZombies, 1000, 1000);

        spawnSnow();

        {
            long seconds = (System.currentTimeMillis() / 1000L) + (long)timeOfDayOffset;
            int secondsInADay = Constants.Core.SECONDS_IN_A_DAY;
            scheduleTask(() -> changeSafeCode(true, true), (secondsInADay - seconds % secondsInADay) * 1000, 1800000);
        }

        {
            for (RealEstateContent realEstateContent : BrainOutServer.ContentMgr.queryContent(RealEstateContent.class))
            {
                for (ServerMap m : Map.All(ServerMap.class))
                {
                    if (m.getDimension().startsWith(realEstateContent.getMap()))
                    {
                        m.setPersonalRequestOnly();
                    }
                }
            }
        }

        BrainOutServer.EventMgr.subscribe(Event.ID.enterPortal, this);
    }

    private void spawnSnow()
    {
        Block snow = BrainOutServer.ContentMgr.get("ground-snow", Block.class);

        for (String dim : raindims)
        {
            Map map = Map.Get(dim);

            for (int j = 0, t = map.getBlocks().getBlockHeight(); j < t; j++)
            {
                for (int i = 0, k = map.getBlocks().getBlockWidth(); i < k; i++)
                {
                    ChunkData chunk = map.getChunk(i, j);

                    if (chunk != null)
                    {
                        for (int j1 = 0; j1 < Constants.Core.CHUNK_SIZE; j1++)
                        {
                            for (int i1 = 0; i1 < Constants.Core.CHUNK_SIZE; i1++)
                            {
                                BlockData blockData = chunk.get(i1, j1, Constants.Layers.BLOCK_LAYER_FOREGROUND);

                                if (blockData != null && blockData.getCreator().getID().equals("ground") &&
                                    map.getBlock(i * Constants.Core.CHUNK_SIZE + i1, j * Constants.Core.CHUNK_SIZE + j1 + 1,
                                        Constants.Layers.BLOCK_LAYER_FOREGROUND) == null)
                                {
                                    chunk.set(i1, j1, snow.getBlock(), Constants.Layers.BLOCK_LAYER_FOREGROUND, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void changeSafeCode(boolean notify, boolean cleanup)
    {
        if (cleanup)
        {
            boolean hasAnyoneInTheBunker = false;

            Array<Map> bunkerMaps = new Array<>();

            for (Map map : Map.All())
            {
                if (!map.getDimension().startsWith("fr-part-bunker"))
                    continue;
                bunkerMaps.add(map);
            }

            for (Map map : bunkerMaps)
            {
                if (map.countActivesForTag(Constants.ActiveTags.PLAYERS, activeData -> true) > 0)
                {
                    hasAnyoneInTheBunker = true;
                    break;
                }
            }

            if (hasAnyoneInTheBunker)
            {
                if (Log.INFO) Log.info("Bunker refresh skipped; players inside");
            }
            else
            {
                // clean the bunker up

                for (Map map : bunkerMaps)
                {
                    map.countActivesForTag(Constants.ActiveTags.ITEM, activeData ->
                    {
                        ItemData itemData = ((ItemData) activeData);

                        itemData.getRecords().clear();

                        if (itemData instanceof RoundLockSafeData)
                        {
                            ServerRoundLockComponentData srlc =
                                    itemData.getComponent(ServerRoundLockComponentData.class);

                            srlc.lock();
                        }

                        itemData.updated();

                        return true;
                    });
                }
            }
        }

        StringBuilder code_ = new StringBuilder();
        for (int i = 0; i < 4; i++)
        {
            code_.append(Character.forDigit(MathUtils.random(1, 9), 10));
        }

        String code = code_.toString();

        for (Map map : Map.All())
        {
            map.countActivesForTag(Constants.ActiveTags.PORTAL, activeData ->
            {
                ServerSafeComponentData sf = activeData.getComponent(ServerSafeComponentData.class);
                if (sf == null)
                    return false;

                sf.setCode(code);

                return true;
            });
        }

        if (Log.INFO) Log.info("Bunker code: " + code);

        if (notify)
        {
            BrainOutServer.Controller.getClients().sendTCP(new FreePlayRadioMsg(code, 3));
        }

        if (cleanup)
        {
            generateNewItems();
        }
    }

    private void spendAMinute()
    {
        BrainOutServer.PostRunnable(() ->
        {
            for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
            {
                Client client = entry.value;
                if (client instanceof PlayerClient)
                {
                    PlayerClient playerClient = ((PlayerClient) client);
                    ModePayload payload = playerClient.getModePayload();
                    if (payload instanceof FreePayload)
                    {
                        playerClient.addStat("fp-minutes-spent", 1);

                        FreePayload freePayload = ((FreePayload) payload);
                        freePayload.questEvent(FreePlayMinuteSpent.obtain(playerClient));
                    }
                }
            }
        });
    }

    @Override
    public boolean needsDeploymentsCheck()
    {
        return true;
    }

    private void spawnCrows()
    {
        Player player = BrainOutServer.ContentMgr.get("player-crow", Player.class);
        Team team = BrainOutServer.ContentMgr.get("team-freeplay", Team.class);

        for (String dim : dims)
        {
            Map map = Map.Get(dim);

            int have = map.countActivesForID("player-crow");
            int need = 16;

            for (int i = have; i < need; i++)
            {
                boolean r = MathUtils.randomBoolean();
                ActiveData spawnFrom = map.getActiveNameIndex().get(r ? "crow-a" : "crow-b");
                ActiveData spawnTo = map.getActiveNameIndex().get(r ? "crow-b" : "crow-a");

                if (spawnFrom != null && spawnTo != null)
                {
                    PlayerData playerData = (PlayerData)player.getData(map.getDimension());
                    playerData.setTeam(team);

                    BotControllerComponentData botController = new BotControllerComponentData(playerData);
                    PlayerOwnerComponent ownerComponent = new PlayerOwnerComponent(playerData);
                    botController.getTasksStack().pushTask(new TaskCrow(botController.getTasksStack(), spawnFrom, spawnTo));
                    playerData.addComponent(botController);
                    playerData.addComponent(ownerComponent);
                    playerData.setPosition(
                        spawnFrom.getX() + MathUtils.random(-10.0f, 10.f),
                        spawnFrom.getY() + MathUtils.random(-10.0f, 10.f));
                    map.addActive(map.generateServerId(), playerData, true, true, ActiveData.ComponentWriter.TRUE);
                }
            }
        }
    }

    private static Array<String> ZOMBIES = new Array<>(new String[]{
        "player-zombie-1",
        "player-zombie-2",
        "player-zombie-3",
        "player-zombie-4",
        "player-zombie-5"
    });

    private void spawnZombies()
    {
        if (!getGameMode().isNight())
        {
            // zombies only spawn at night
            return;
        }

        Player player = BrainOutServer.ContentMgr.get(ZOMBIES.random(), Player.class);
        Team team = BrainOutServer.ContentMgr.get("team-freeplay", Team.class);

        for (String dim : dims)
        {
            Map map = Map.Get(dim);

            int have = map.countActivesForIDStartsWith("player-zombie-");
            int need = 6;

            if (have < need)
            {
                ActiveData spawnAt = spawnPool.random();

                if (spawnAt != null && spawnAt.getDimension().equals(map.getDimension()))
                {
                    PlayerData playerData = (PlayerData)player.getData(map.getDimension());
                    playerData.setTeam(team);

                    BotControllerComponentData botController = new BotControllerComponentData(playerData);
                    PlayerOwnerComponent poc = new PlayerOwnerComponent(playerData);
                    botController.getTasksStack().pushTask(new TaskZombie(botController.getTasksStack()));
                    playerData.addComponent(botController);
                    playerData.addComponent(poc);
                    playerData.addComponent(new PlayerRemoteComponent(playerData));
                    playerData.setPosition(
                        spawnAt.getX(),
                        spawnAt.getY());

                    map.addActive(map.generateServerId(), playerData, true, true, ActiveData.ComponentWriter.TRUE);

                    {
                        Weapon knife = BrainOutServer.ContentMgr.get("weapon-zombie-knife", Weapon.class);
                        WeaponData ki = knife.getData(map.getDimension());
                        ki.init();
                        InstrumentConsumableItem ici = new InstrumentConsumableItem(ki, map.getDimension());
                        ConsumableRecord r = new ConsumableRecord(
                            ici, 1, poc.getConsumableContainer().newId()
                        );

                        poc.getConsumableContainer().addRecord(r);
                        poc.setCurrentInstrument(r);
                        playerData.setCurrentInstrument(ki);
                    }

                    if (Log.INFO) Log.info("Spawned a zombie at " + map.getDimension());
                }
            }
        }
    }

    private void updateRain()
    {
        boolean raining = isRain();

        if (raining && rain.size == 0)
        {
            if (Log.INFO) Log.info("Creating rain");

            Active rainInside = BrainOutServer.ContentMgr.get("fr-ac-inside", Active.class);
            Active rainOutside = BrainOutServer.ContentMgr.get("fr-ac-outside", Active.class);


            for (Map map : Map.All())
            {
                if (raindims.contains(map.getDimension(), false))
                {
                    ActiveData ad = rainOutside.getData(map.getDimension());
                    ad.setLayer(Constants.Layers.ACTIVE_LAYER_1);
                    ad.setzIndex(1000);
                    map.addActive(map.generateServerId(), ad, true);
                    rain.add(ad);
                }
                else
                {
                    ActiveData ad = rainInside.getData(map.getDimension());
                    map.addActive(map.generateServerId(), ad, true);
                    rain.add(ad);
                }
            }
        }

        if (!raining && rain.size > 0)
        {
            if (Log.INFO) Log.info("Removing rain");

            for (ActiveData data : rain)
            {
                if (data == null || data.getMap() == null)
                {
                    continue;
                }
                
                data.getMap().removeActive(data, true);
            }

            rain.clear();
        }
    }

    static Array<String> raindims = new Array<>(new String[]{"default", "forest", "swamp2", "intro"});
    static Array<String> dims = new Array<>(new String[]{"default", "forest", "swamp2"});
    static Array<String> forest_dims = new Array<>(new String[]{"default", "forest", "swamp2"});

    private void generateCricket(String kind, Array<String> dims)
    {
        Active cricketContent = BrainOutServer.ContentMgr.get(kind, Active.class);

        Map map = Map.Get(dims.random());
        ActiveData cricket = cricketContent.getData(map.getDimension());

        do
        {
            cricket.setPosition(MathUtils.random(64, cricket.getMap().getWidth() - 64), 64);
        } while (map.getClosestActive(32, cricket.getX(), cricket.getY(), ActiveData.class,
                activeData -> activeData != cricket && activeData.getCreator().getID().equals(kind)) != null);

        map.addActive(map.generateServerId(), cricket, true, true);
        crickets.add(cricket);
    }

    private void updateCrickets()
    {
        float timeOfDay = getGameMode().getTimeOfDay();

        if (timeOfDay > 0.6f || timeOfDay < 0.1f)
        {
            if (crickets.size < 4)
            {
                generateCricket("freeplay-coyote", forest_dims);
            }
            else if (crickets.size < 8)
            {
                generateCricket("freeplay-fox", forest_dims);
            }
            else if (crickets.size < 40)
            {
                generateCricket("freeplay-cricket", dims);
            }
        }
        else
        {
            if (crickets.size > 0)
            {
                ActiveData cricket = crickets.pop();
                Map map = cricket.getMap();
                map.removeActive(cricket, true, true, false);
            }
        }
    }

    private void updateTimeOfDay(boolean update)
    {
        long seconds = (System.currentTimeMillis() / 1000L) + (long)timeOfDayOffset;
        int secondsInADay = Constants.Core.SECONDS_IN_A_DAY;
        long hour = seconds % secondsInADay;
        getGameMode().setTimeOfDay((float)hour / (float)secondsInADay);
        getGameMode().setTimeOfDayUpdateSpeed(1.0f / (float)secondsInADay);

        if (update)
            updated();
    }

    public boolean isRain()
    {
        long seconds = (System.currentTimeMillis() / 1000L) + (long)timeOfDayOffset;
        int rainCycle = Constants.Core.SECONDS_IN_A_DAY * 5;
        long hour = seconds % rainCycle;
        return hour < Constants.Core.SECONDS_IN_A_DAY;
    }

    public void setTimeOfDayOffset(int offset)
    {
        timeOfDayOffset = offset;
        updateTimeOfDay(true);
    }

    private void scheduleTask(Runnable run, long delay, long repeat)
    {
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(run);
            }
        };

        tasks.add(task);
        BrainOutServer.Timer.schedule(task, delay, repeat);
    }

    @Override
    public Task getBotStartingTask(TaskStack taskStack, BotClient client)
    {
        return new TaskFreePlay(taskStack, client);
    }

    @Override
    public Task getBotWarmupTask(TaskStack taskStack, BotClient client)
    {
        return new TaskFreePlay(taskStack, client);
    }

    public static String[] WAVE_DIMENSIONS = new String[]{"default", "forest", "swamp2"};
    private static final float WAVE_SPEED = 1.0f;

    private void generateWaves()
    {
        Active spot = BrainOutServer.ContentMgr.get("cold-wind", Active.class);

        for (String dimension : WAVE_DIMENSIONS)
        {
            Map map = Map.Get(dimension);

            if (map == null)
                continue;

            float center = MathUtils.random(64.0f, map.getWidth() - 64.0f);
            float distance = map.getWidth() / 2.0f + 128;

            ActiveData a = spot.getData(dimension);
            ActiveData b = spot.getData(dimension);

            a.setPosition(center - distance, Constants.Core.CHUNK_SIZE);
            b.setPosition(center + distance, Constants.Core.CHUNK_SIZE);

            a.setAngle(0);
            b.setAngle(180);

            WindComponentData a_ = a.getComponent(WindComponentData.class);
            WindComponentData b_ = b.getComponent(WindComponentData.class);

            a_.setMovement(WAVE_SPEED);
            b_.setMovement(- WAVE_SPEED);

            WaveWrapper wave = new WaveWrapper(dimension, a, b);
            waves.put(dimension, wave);

            map.addActive(map.generateServerId(), a, true, true);
            map.addActive(map.generateServerId(), b, true, true);

            if (Log.INFO) Log.info("Generated new wave " + dimension + ": from " +
                    wave.getA().getX() + " to " + wave.getB().getX());
        }
    }

    private int logCounter = 0;

    private void syncWaves()
    {
        if (getGameMode().getPhase() != GameMode.Phase.game)
            return;

        boolean hadAnyWave = false;
        logCounter++;

        boolean log = false;
        if (logCounter > 5)
        {
            logCounter = 0;
            log = true;
        }

        for (String dimension : WAVE_DIMENSIONS)
        {
            Map map = Map.Get(dimension);

            if (map == null)
                continue;

            WaveWrapper wave = waves.get(dimension);

            if (wave == null)
                continue;;

            hadAnyWave = true;

            float distance = wave.getDistance();

            if (distance < 32)
            {
                wave.release();
                waves.remove(dimension);
                if (Log.INFO) Log.info("Wave " + dimension + " has been disposed");
                continue;
            }
            else if (distance < 64)
            {
                float p = MathUtils.clamp((distance - 32.0f) / 32.0f, 0f, 1.0f);
                wave.setPower(p);
            }

            wave.updated();

            if (log)
            {
                if (Log.INFO) Log.info("Updated wave " + dimension + ": from " +
                        wave.getA().getX() + " to " + wave.getB().getX() + " of " + map.getWidth());
            }
        }

        if (!hadAnyWave)
        {
            generateWaves();
        }
    }

    private void spawnDoors()
    {
        for (Map map : Map.All())
        {
            returnDoor = map.getActiveForTag(Constants.ActiveTags.MARKER, activeData -> ((MarkerData) activeData).tag.equals("return-door"));
            if (returnDoor != null)
            {
                break;
            }
        }

        spawnPointData = (SpawnPointData)Map.Get("intro").getActiveForTag(Constants.ActiveTags.SPAWNABLE, activeData -> activeData instanceof SpawnPointData);

        for (ServerMap map : Map.All(ServerMap.class))
        {
            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.EXIT_DOOR, false))
            {
                ServerDoorSpawnerComponentData cmp =
                        activeData.getComponent(ServerDoorSpawnerComponentData.class);

                if (cmp != null)
                {
                    Active instanceClass = cmp.getContentComponent().getInstance();
                    ActiveData exitDoor = instanceClass.getData(map.getDimension());
                    exitDoor.setPosition(activeData.getX(), activeData.getY());
                    map.addActive(map.generateServerId(), exitDoor, true);
                }
            }
        }
    }

    @Override
    public void release()
    {
        super.release();

        if (alarm != null)
        {
            alarm.cancel();
            alarm = null;
        }

        /*
        if (wavesSyncTimer != null)
        {
            wavesSyncTimer.cancel();
            wavesSyncTimer = null;
        }
        */

        BrainOutServer.EventMgr.unsubscribe(Event.ID.enterPortal, this);
    }

    private void activateAlarm()
    {
        // sound the alarm
        playMusic("music-freeplay-alarm");

        exp = 300;

        BrainOutServer.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() ->
                {
                    explodeThings();
                });
            }
        }, 20 * 1000);
    }

    public boolean areThingsExploding()
    {
        return exp > 0;
    }

    public void explodeThings()
    {
        exp = 300;

        BrainOutServer.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() ->
                {
                    exp--;

                    if (exp > 0)
                    {
                        for (int i = 0; i < 2; i++)
                        {
                            for (ServerMap map : Map.All(ServerMap.class))
                            {
                                if (!map.getDimension().equals("default") && !map.getDimension().equals("forest") && !map.getDimension().equals("swamp2"))
                                    continue;

                                doExplode(map);
                            }
                        }
                    }
                    else
                    {
                        //scheduleAlarm();
                        cancel();
                    }
                });
            }
        }, 100, 100);
    }

    private void doExplode(ServerMap map)
    {
        if (map == null)
            return;

        ThrowableActive active = BrainOutServer.ContentMgr.get("explosive-freeplay-active", ThrowableActive.class);

        float x = MathUtils.random(0, map.getWidth());
        float y = 196;

        ActiveData activeData = active.getData(map.getDimension());

        activeData.setPosition(x + MathUtils.random(32), y + MathUtils.random(32));
        activeData.setAngle(MathUtils.random(240, 270));

        SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy != null)
        {
            phy.getSpeed().set(
                -MathUtils.random(60, 90),
                -MathUtils.random(400, 500)
            );
        }

        map.addActive(map.generateServerId(), activeData, true);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case enterPortal:
            {
                EnterPortalEvent e = ((EnterPortalEvent) event);

                for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
                {
                    Client cl = entry.value;
                    if (!(cl instanceof PlayerClient))
                        continue;
                    PlayerClient playerClient = ((PlayerClient) cl);
                    FreePayload payload = getPayload(playerClient);
                    if (payload == null)
                        continue;

                    payload.questEvent(e, false);
                }

                break;
            }
        }

        return super.onEvent(event);
    }

    private void scheduleAlarm()
    {
        alarm = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() -> activateAlarm());
            }
        };

        BrainOutServer.Timer.schedule(alarm, MathUtils.random(300 * 1000, 600 * 1000));
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
    }

    @Override
    protected void setupPhase()
    {
        try
        {
            setRoundTime();

            getGameMode().setPhase(GameMode.Phase.game);

            Team freePlayTeam = BrainOutServer.Controller.getTeams().get(0);

            spawnPool = new Array<>();

            for (ObjectMap.Entry<String, Map> entry : Map.AllEntries())
            {
                Map map = entry.value;

                map.getActivesForTag(Constants.ActiveTags.SPAWNABLE, spawnPool,
                        activeData -> activeData instanceof Spawnable && activeData.getTeam() == freePlayTeam);
            }

            spawnPool.shuffle();

            BrainOutServer.Controller.updateRoomSettings();

            //scheduleAlarm();

            /*
            if (spawnBots != null)
            {
                BrainOutServer.PostRunnable(() -> spawnBots.spawn());
            }
             */

            updated();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private int countInitializedPlayers()
    {
        int initialized = 0;

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            if (!(entry.value instanceof PlayerClient))
                continue;

            if (entry.value.isInitialized())
                initialized++;
        }

        return initialized;
    }

    @Override
    public void onClientReconnecting(PlayerClient playerClient, PlayerData playerData)
    {
        super.onClientReconnecting(playerClient, playerData);

        if (BrainOutServer.IsShuttingDown())
            return;

        FreePayload payload = getPayload(playerClient);
        if (payload != null)
        {
            if (payload.isExited())
                return;
        }

        if (playerClient.getProfile() == null)
            return;

        if (playerClient.getProfile().getContainers() == null)
            return;

        FreePlayContainer onHand = playerClient.getProfile().getContainers().get("h");
        if (onHand != null)
        {
            onHand.setSaveEmpty(true);
        }
    }

    @Override
    public void clientInitialized(Client client, boolean reconnected)
    {
        super.clientInitialized(client, reconnected);

        if (client instanceof PlayerClient)
        {
            if (((PlayerClient) client).isReconnected())
            {
                FreePlayContainer onHand = ((PlayerClient) client).getProfile().getContainers().get("h");
                if (onHand != null)
                {
                    onHand.setSaveEmpty(false);
                }

                return;
            }

            ClientProfile profile = ((PlayerClient) client).getProfile();
            if (profile != null)
            {
                long now = System.currentTimeMillis() / 1000L;
                int day = (int)(now / (24 * 60 * 60));

                int lastFreePlayDay = profile.getInt("last-fp-day", 0);
                if (lastFreePlayDay != 0)
                {
                    if (day > lastFreePlayDay)
                    {
                        int karma = profile.getInt("karma", 0);
                        if (karma < 0)
                        {
                            int passed = day - lastFreePlayDay;
                            karma = Math.min(karma + passed, 0);
                            profile.setInt("karma", karma);
                        }
                    }
                }

                profile.setInt("last-fp-day", day);
            }

        }

        if (client instanceof PlayerClient)
        {
            FreePlayContainer convert = ((PlayerClient) client).getProfile().getContainers().get("p");
            if (convert != null && convert.getItems().size > 0)
            {
                convertItemsToMarket(((PlayerClient) client), convert, "freeplay");
            }

            spawnPlayer(spawnPointData, client);

            FreePayload payload = getPayload(((PlayerClient) client));

            if (payload != null)
            {
                payload.setExited(true);
            }
        }
    }

    private void convertItemsToMarket(PlayerClient playerClient, FreePlayContainer convert, String market)
    {
        MarketService marketService = MarketService.Get();

        marketService.getMarketItems(market, playerClient.getAccessToken(),
            (request, result, entries) ->
        {
            if (result != Request.Result.success)
            {
                return;
            }

            if (entries.size() != 0)
            {
                BrainOutServer.PostRunnable(() ->
                {
                    playerClient.log("Personal container has been cleared b/c marked items were present.");

                    convert.clear();
                    convert.setSaveEmpty(true);
                });
            }
            else
            {
                BrainOutServer.PostRunnable(() ->
                {
                    List<MarketService.MarketItemEntry> newEntries = new ArrayList<>();

                    for (FreePlayContainer.ContainerItem item : convert.getItems())
                    {
                        ConsumableRecord r;
                        if (item instanceof FreePlayContainer.InstrumentContainerItem)
                        {
                            FreePlayContainer.InstrumentContainerItem ici =
                                ((FreePlayContainer.InstrumentContainerItem) item);

                            InstrumentData i = ici.instrument.instrument.getData("default");
                            i.setInfo(ici.instrument);

                            r = new ConsumableRecord(new InstrumentConsumableItem(i, "default"), 1, 0);
                        }
                        else if (item instanceof FreePlayContainer.ConsumableContainerItem)
                        {
                            FreePlayContainer.ConsumableContainerItem i =
                                ((FreePlayContainer.ConsumableContainerItem) item);

                            r = new ConsumableRecord(i.cnt.acquireConsumableItem(), i.amount, 0);
                        }
                        else
                        {
                            continue;
                        }

                        r.setQuality(item.quality);

                        MarketService.MarketItemEntry rr = MarketUtils.ConsumableRecordToMarketEntry(r);
                        
                        if (rr == null)
                            continue;

                        newEntries.add(rr);
                    }

                    marketService.updateMarketItems(market, newEntries, playerClient.getAccessToken(),
                        (request1, result1) ->
                    {
                        if (result1 == Request.Result.success)
                        {
                            BrainOutServer.PostRunnable(() ->
                            {
                                playerClient.log("Personal container trafserred OK");

                                convert.clear();
                                convert.setSaveEmpty(true);
                            });
                        }
                    });
                });
            }
        });
    }

    private Spawnable getAPlaceToSpawnAt(Client client)
    {
        spawnPool.shuffle();
        spawnPool.sort(Comparator.comparingInt(o -> spawnIndex(client, o)));

        return (Spawnable)spawnPool.first();
    }

    static Array<ActiveData> checkAt = new Array<>();

    private int spawnIndex(Client for_, ActiveData spawn)
    {
        Map map = spawn.getMap();
        if (map == null)
            return 0;

        if (!(spawn instanceof SpawnPointData))
            return 0;

        SpawnPointData ppp = ((SpawnPointData) spawn);

        checkAt.clear();
        map.countClosestActiveForTag(
            ppp.spawnRange, ppp.getSpawnX(), ppp.getSpawnY(), ActiveData.class,
            Constants.ActiveTags.SPAWNABLE, activeData ->
        {
            checkAt.add(activeData);
            return true;
        });

        int cnt = 0;

        for (ActiveData spawnCheck : checkAt)
        {
            cnt += map.countClosestActiveForTag(
                64, spawnCheck.getX(), spawnCheck.getY(), ActiveData.class,
                Constants.ActiveTags.PLAYERS, activeData ->
                {
                    Client clientTo = BrainOutServer.Controller.getClients().get(activeData.getOwnerId());
                    return activeData instanceof PlayerData && isEnemies(for_, clientTo);
                }
            );
        }

        checkAt.clear();

        return cnt;
    }

    @Override
    public void voiceChat(PlayerClient playerClient, short[] data, float volume)
    {
        PlayerData playerData = playerClient.getPlayerData();

        VoiceChatMsg msg;

        if (playerData != null)
        {
            msg = new VoiceChatMsg(playerClient.getId(), playerData.getId(), playerData.getDimensionId(), data, volume);
        }
        else
        {
            msg = new VoiceChatMsg(playerClient.getId(), -1, -1, data, volume);
        }

        for (Client anotherClient : BrainOutServer.Controller.getClients().values())
        {
            if (!(anotherClient instanceof PlayerClient))
                continue;
            
            if (isNeedToSendVoiceMsg(playerClient, playerData, anotherClient, msg))
            {
                ((PlayerClient)anotherClient).sendUDP(msg);
            }
        }
    }

    private boolean isNeedToSendVoiceMsg(PlayerClient from, PlayerData fromPD, Client to, VoiceChatMsg msg)
    {
        if (to == from)
            return false;

        if (to instanceof PlayerClient)
        {
            PlayerClient pl = ((PlayerClient) to);

            if (pl.getPartyId() != null && pl.getPartyId().equals(from.getPartyId()))
            {
                msg.volume = 1;
                return true;
            }
        }

        if (from.getPlayerData() != null && to.getPlayerData() != null)
        {
            PlayerData toPl = to.getPlayerData();

            if (FPUtils.isPlayersHasWalkietalkieContact(fromPD, toPl))
            {
                return true;
            }

            if (!toPl.getDimension().equals(fromPD.getDimension()))
                return false;

            float currentDistanceSqr = Vector2.dst2(fromPD.getX(), fromPD.getY(), toPl.getX(), toPl.getY());

            if (currentDistanceSqr < Constants.Voice.DISTANCE_SQR)
            {
                msg.volume *= Interpolation.pow4Out.apply(1.0f - currentDistanceSqr / Constants.Voice.DISTANCE_SQR);
                return true;
            }
        }

        return false;
    }

    @Override
    protected void warmUpComplete()
    {
    }

    public boolean playerEnter(PlayerData playerData, PlayerClient playerClient)
    {
        boolean isFirstLandingTeammate = true;

        FreePayload freePayload = getPayload(playerClient);
        if (freePayload == null) return false;

        HashSet<PlayerClient> playerParty = new HashSet<>();

        //add steam friends
        if (playerClient.getPartyId() != null)
        {
            Party party = parties.get(playerClient.getPartyId());
            if (party != null)
            {
                for (PlayerClient member : party.members.values())
                {
                    playerParty.add(member);
                }
            }
        }

        //add temporary friends
        if (freePayload.getFriends() != null)
        {
            IntSet.IntSetIterator iterator = freePayload.getFriends().getFriends().iterator();
            while (iterator.hasNext)
            {
                int id = iterator.next();
                if (BrainOutServer.Controller.getClients().containsKey(id))
                {
                    Client client = BrainOutServer.Controller.getClients().get(id);
                    if (client instanceof PlayerClient)
                    {
                        playerParty.add((PlayerClient)client);
                    }
                }
            }
        }

        if (playerParty.size() > 1)
        {
            //remove self from party list
            playerParty.remove(playerClient);

            for (PlayerClient friend : playerParty)
            {
                if (friend == playerClient)
                    continue;

                if (!friend.isAlive())
                    continue;

                PlayerData p = friend.getPlayerData();
                Map m = p.getMap();
                if (m == null)
                    continue;

                if (m.isSafeMap())
                    continue;

                isFirstLandingTeammate = false;
                break;
            }

            if (isFirstLandingTeammate)
            {
                for (PlayerClient friend : playerParty)
                {
                    FreePayload friendPayload = (FreePayload)friend.getModePayload();
                    friendPayload.updateTeamLandingTimer(playerClient.getId());
                    friend.sendTCP(new TeamLandingMsg(playerClient.getId(), System.currentTimeMillis()));
                }
            }
        }

        float spawnX;
        float spawnY;
        Map spawnAtMap;

        ActiveData moveTo = playerData;
        float x = playerData.getX();
        float y = playerData.getY();

        boolean isTeamLandingCorrect = false;

        if (freePayload.isTeamLandingTimer())
        {
            Client targetClient = BrainOutServer.Controller.getClients().get(freePayload.getTeamLandingTargetId());
            if (targetClient != null && targetClient.isAlive() && !Map.IsSafeMap(targetClient.getPlayerData().getDimension()))
            {
                moveTo = targetClient.getPlayerData();
                x = moveTo.getX();
                y = moveTo.getY();

                isTeamLandingCorrect = true;
            }
        }

        if (!isTeamLandingCorrect)
        {
            Spawnable spawnAt = getAPlaceToSpawnAt(playerClient);
            if (spawnAt == null)
                return false;

            spawnAtMap = Map.Get(spawnAt.getDimension());
            if (spawnAtMap == null)
                return false;

            spawnX = spawnAt.getSpawnX();
            spawnY = spawnAt.getSpawnY();

            Array<ActiveData> subpoints = new Array<>();

            for (ActiveData activeData: spawnAtMap.getActivesForTag(Constants.ActiveTags.SPAWNABLE, false))
            {
                if (Vector2.dst(activeData.getX(), activeData.getY(), spawnX, spawnY) <= 32)
                {
                    subpoints.add(activeData);
                }
            }

            if (subpoints.size == 0)
                return false;

            moveTo = subpoints.random();
            x = moveTo.getX();
            y = moveTo.getY();
        }


        SimplePhysicsComponentData pcd = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (pcd != null)
        {
            y -= (3.0 - pcd.getSize().y) / 2.0f;
        }

        playerClient.moveTo(moveTo.getDimension(), x, y);
        FreePayload payload = getPayload(playerClient);
        playerClient.sendTCP(new FreeEnterMsg());

        if (payload != null)
        {
            payload.setExited(false);
        }

        return true;
    }

    private void spawnPlayer(Spawnable at, Client player)
    {
        PlayerClient asPlayerClient = (player instanceof PlayerClient) ? ((PlayerClient) player) : null;

        if (asPlayerClient != null)
        {
            asPlayerClient.setReady(false);
            asPlayerClient.setAllowDrop(true);
        }

        BrainOutServer.PostRunnable(() ->
        {
            if (asPlayerClient != null)
            {
                asPlayerClient.updateRemotePlayers();
            }
            player.sendRemotePlayers(false);
        });

        ShopCart shopCart = player.getShopCart();
        shopCart.clear();

        if (asPlayerClient != null)
        {
            ClientProfile profile = asPlayerClient.getProfile();
            if (profile == null)
                return;

            Layout selectedLayout = profile.getLayout();

            if (selectedLayout == null)
            {
                selectedLayout = BrainOut.ContentMgr.get("layout-1", Layout.class);
            }

            SlotItem freePlayPlayer = BrainOutServer.ContentMgr.get("sl-pl-freeplay", SlotItem.class);
            Slot playerSlot = BrainOutServer.ContentMgr.get("slot-player", Slot.class);

            shopCart.initSelection(profile, selectedLayout, getGameMode().getPhase() == GameMode.Phase.warmUp);
            shopCart.selectItem(playerSlot, freePlayPlayer.getStaticSelection());
        }
        else
        {
            SlotItem freePlayPlayer = BrainOutServer.ContentMgr.get("sl-pl-freeplay", SlotItem.class);
            Slot playerSlot = BrainOutServer.ContentMgr.get("slot-player", Slot.class);

            Layout selectedLayout = BrainOut.ContentMgr.get("layout-1", Layout.class);
            shopCart.initSelection(null, selectedLayout, getGameMode().getPhase() == GameMode.Phase.warmUp);
            shopCart.selectItem(playerSlot, freePlayPlayer.getStaticSelection());

            if (player instanceof BotClient)
            {
                Slot playerSkinSlot = BrainOutServer.ContentMgr.get("slot-player-skin", Slot.class);
                PlayerSkinSlotItem playerSkin = ((BotClient) player).getRandomPlayerSkin();
                if (playerSkin != null)
                {
                    shopCart.selectItem(playerSkinSlot, playerSkin.getStaticSelection());
                }
            }
        }

        playersAttended++;

        if (!player.spawn(at))
        {
            return;
        }

        if (asPlayerClient != null)
        {
            PlayerData playerData = player.getPlayerData();
            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

            ClientProfile profile = asPlayerClient.getProfile();
            if (profile != null && poc != null)
            {
                FreePlayContainer h = profile.getContainers().get("h");
                if (h != null)
                {
                    ConsumableContainer cnt = poc.getConsumableContainer();
                    dumpFPToConsumableContainer(playerData.getDimension(), h, cnt);
                }
            }
        }

        if (getGameMode().isNight())
        {
            PlayerOwnerComponent poc = player.getPlayerData().getComponent(PlayerOwnerComponent.class);
            ConsumableContainer cc = poc.getConsumableContainer();

            DecayConsumableContent m = BrainOutServer.ContentMgr.get("consumable-item-matches-1", DecayConsumableContent.class);
            if (!cc.hasConsumable(m))
            {
                DecayConsumableItem matches = m.acquireConsumableItem();
                matches.setUses(1);
                cc.putConsumable(1, matches, 75);
            }

            {
                ConsumableContent sticks = BrainOutServer.ContentMgr.get("consumable-item-sticks", ConsumableContent.class);
                if (!cc.hasConsumable(sticks))
                {
                    cc.putConsumable(1, sticks.acquireConsumableItem(), 75);
                }
            }

            {
                ConsumableContent sticks = BrainOutServer.ContentMgr.get("consumable-item-toilet-paper", ConsumableContent.class);
                if (!cc.hasConsumable(sticks))
                {
                    cc.putConsumable(2, sticks.acquireConsumableItem(), 55);
                }
            }
        }

        if (getGameMode().getPhase() != GameMode.Phase.warmUp)
        {
            ModePayload payload = player.getModePayload();

            if (payload instanceof FreePayload)
            {
                ((FreePayload) payload).setupQuests();
            }
        }
    }

    private void dumpFPToConsumableContainer(String dimension, FreePlayContainer h, ConsumableContainer cnt)
    {
        for (FreePlayContainer.ContainerItem item : h.getItems())
        {
            if (item instanceof FreePlayContainer.InstrumentContainerItem)
            {
                FreePlayContainer.InstrumentContainerItem i =
                        ((FreePlayContainer.InstrumentContainerItem) item);

                InstrumentInfo info = i.instrument;

                InstrumentData instrumentData = info.instrument.getData(dimension);
                instrumentData.setSkin(info.skin);
                for (ObjectMap.Entry<String, Upgrade> upgrade : info.upgrades)
                {
                    instrumentData.getUpgrades().put(upgrade.key, upgrade.value);
                }

                if (instrumentData instanceof WeaponData)
                {
                    WeaponData weaponData = ((WeaponData) instrumentData);
                    WeaponData.WeaponLoad load = weaponData.setLoad(Constants.Properties.SLOT_PRIMARY,
                        i.getRounds(), i.getChambered());

                    if (weaponData.getWeapon().getPrimaryProperties().hasMagazineManagement() && i.getMagazines() != null)
                    {
                        load.magazines = new IntMap<>();

                        for (IntMap.Entry<FreePlayContainer.InstrumentContainerItem.Magazine> entry : i.getMagazines())
                        {
                            load.magazines.put(entry.key,
                                new WeaponData.WeaponLoad.Magazine(entry.value.amount, entry.value.quality));
                        }
                    }
                }

                instrumentData.init();

                cnt.putConsumable(1,
                    new InstrumentConsumableItem(instrumentData, dimension), item.quality);
            }
            else if (item instanceof FreePlayContainer.DecayConsumableContainerItem)
            {
                FreePlayContainer.DecayConsumableContainerItem i =
                        ((FreePlayContainer.DecayConsumableContainerItem) item);

                DecayConsumableItem di = new DecayConsumableItem(i.cnt);
                di.setUses(i.uses);
                cnt.putConsumable(1, di, item.quality);
            }
            else if (item instanceof FreePlayContainer.WalkietalkieContainerItem)
            {
                FreePlayContainer.WalkietalkieContainerItem i =
                        ((FreePlayContainer.WalkietalkieContainerItem) item);

                WalkietalkieConsumableItem wi = new WalkietalkieConsumableItem(i.cnt);
                wi.setFrequency(i.frequency);
                cnt.putConsumable(1, wi, item.quality);
            }
            else if (item instanceof FreePlayContainer.ConsumableContainerItem)
            {
                FreePlayContainer.ConsumableContainerItem i =
                        ((FreePlayContainer.ConsumableContainerItem) item);

                cnt.putConsumable(i.amount, i.cnt.acquireConsumableItem(), item.quality);
            }
        }
    }

    @Override
    public void onClientSpawn(Client client, PlayerData player)
    {
        super.onClientSpawn(client, player);

        KarmaComponentData kp = player.getComponent(KarmaComponentData.class);

        if (kp != null)
        {
            if (client instanceof BotClient)
            {
                kp.setKarma(-10);
            }
            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);
                kp.setKarma(playerClient.getProfile().getInt("karma", 0));
            }
        }

        if (client instanceof BotClient)
        {
            PlayerOwnerComponent poc = player.getComponent(PlayerOwnerComponent.class);

            if (poc != null)
            {
                String passportId = "freeplay-passport-" + MathUtils.random(1, 3);
                ConsumableContent cc = BrainOutServer.ContentMgr.get(passportId, ConsumableContent.class);

                ConsumableRecord record = new ConsumableRecord(cc.acquireConsumableItem(), 1,
                    poc.getConsumableContainer().newId());

                poc.getConsumableContainer().addRecord(record);

                if (MathUtils.randomBoolean())
                {
                    OtherConsumables.generate(this, poc.getConsumableContainer());
                }
                else if(MathUtils.randomBoolean())
                {
                    if (MathUtils.randomBoolean())
                    {
                        RandomAmmo.generate(this, poc.getConsumableContainer());
                    }
                }
            }
        }
    }


    @Override
    public boolean displaceBlocksUponSpawn()
    {
        return false;
    }

    @Override
    public boolean isDeathDropEnabled(PlayerData playerData)
    {
        return false;
    }

    @Override
    public boolean canDropConsumable(Client playerClient, ConsumableItem item)
    {
        return true;
    }

    @Override
    public boolean isFullDropEnabled(PlayerData playerData)
    {
        if (playerData.getPlayer().getID().startsWith("player-zombie-"))
        {
            return false;
        }

        return playerData.getDimension() != null && !Map.IsSafeMap(playerData.getDimension());
    }

    @Override
    public void newPlayerClient(PlayerClient playerClient)
    {
        if (playerClient.getPartyId() != null)
        {
            Party party = parties.get(playerClient.getPartyId());

            if (party == null)
            {
                party = new Party(playerClient.getPartyId());
                parties.put(playerClient.getPartyId(), party);
            }

            party.addMember(playerClient);
        }
    }

    @Override
    public boolean enableLoginPopup()
    {
        return false;
    }

    @Override
    public void clientReleased(Client client)
    {
        super.clientReleased(client);

        if (client instanceof PlayerClient)
        {
            PlayerClient playerClient = ((PlayerClient) client);

            FreePayload payload = getPayload(playerClient);

            if (payload != null)
            {
                PlayerData playerData = playerClient.getPlayerData();
                saveFPContainers(playerData, ((PlayerClient) client));
            }

            if (playerClient.getPartyId() != null)
            {
                Party party = parties.get(playerClient.getPartyId());

                if (party != null)
                {
                    if (!party.removeMember(playerClient))
                    {
                        party.release();
                        parties.remove(playerClient.getPartyId());
                    }
                    else
                    {
                        for (ObjectMap.Entry<String, PlayerClient> entry : party.members)
                        {
                            PlayerClient partner = entry.value;

                            partner.sendChatImportant("{MP_PARTNER_LEFT_FREEPLAY}");
                        }
                    }
                }
            }
        }

        // cleanup private items
        Array<ConsumableRecord> toRemove = new Array<>();
        Array<ServerMap> toRemoveMaps = new Array<>();

        for (ServerMap map : Map.All(ServerMap.class))
        {
            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ConsumableContainer records = ((ItemData) activeData).getRecords();
                for (ObjectMap.Entry<Integer, ConsumableRecord> entry : records.getData())
                {
                    ConsumableItem item = entry.value.getItem();
                    if (item.getPrivate() == client.getId())
                    {
                        toRemove.add(entry.value);
                    }
                }

                if (toRemove.size > 0)
                {
                    for (ConsumableRecord item : toRemove)
                    {
                        records.removeRecord(item);
                    }

                    toRemove.clear();
                }
            }

            if (client instanceof PlayerClient)
            {
                String account = ((PlayerClient) client).getAccount();
                if (map.suitableForPersonalRequestFor(account))
                {
                    map.getSuitableForPersonalRequests().remove(account);
                    if (map.getSuitableForPersonalRequests().size() == 0)
                    {
                        toRemoveMaps.add(map);
                    }
                }
            }
        }

        PlayState ps = BrainOutServer.Controller.getPlayState();

        for (ServerMap map : toRemoveMaps)
        {
            if (Log.INFO) Log.info("Unloading personal map: " + map.getDimension());

            BrainOutServer.Controller.getClients().sendTCP(new FreeDimensionMsg(map.getDimension()));
            map.dispose();

            if (ps instanceof ServerPSGame)
            {
                ((ServerPSGame) ps).getMaps().removeValue(map, true);
            }
        }
    }

    public void playerExit(PlayerData playerData, PlayerClient playerClient)
    {
        if (returnDoor == null)
            return;

        playerExit(returnDoor, playerData, playerClient);
    }

    public void playerExit(ActiveData returnDoor, PlayerData playerData, PlayerClient playerClient)
    {
        if (returnDoor == null)
            return;

        float x = returnDoor.getX(), y = returnDoor.getY();

        SimplePhysicsComponentData pcd = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (pcd != null)
        {
            y -= (3.0 - pcd.getSize().y) / 2.0f;
        }

        playerClient.moveTo(returnDoor.getDimension(), x, y);
        FreePayload payload = getPayload(playerClient);

        if (payload != null)
        {
            payload.setExited(true);
        }

        Map map = playerData.getMap();

        if (map == null)
            return;

        playerClient.addStat("freeplay-exited", 1);

        ServerFreeplayPlayerComponentData fp = playerData.getComponent(ServerFreeplayPlayerComponentData.class);

        if (fp != null)
            fp.fullRecovery();

    }

    private static ObjectSet<ConsumableRecord> toRemoveFP = new ObjectSet<>();

    public void progressQuestsFP(PlayerData playerData, PlayerClient playerClient)
    {
        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        FreePayload payload = getPayload(playerClient);

        toRemoveFP.clear();

        ObjectMap<OwnableContent, Integer> unlocked = new ObjectMap<>();
        ObjectMap<ConsumableContent, Integer> taken = new ObjectMap<>();

        ConsumableContainer cnt = poc.getConsumableContainer();

        int valuables = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();

            ItemLimitsComponent limits = item.getContent().getComponent(ItemLimitsComponent.class);
            if (limits != null && !limits.getLimits().passes(playerClient.getProfile()))
            {
                continue;
            }

            FreePlayAddStatExitComponent addStat = item.getContent().getComponent(FreePlayAddStatExitComponent.class);

            if (addStat != null)
            {
                playerClient.getProfile().addStat(addStat.getStat(), addStat.getAmount(), true);
                toRemoveFP.add(record);
            }
            else if (item.getContent().hasComponent(FreePlayValuableComponent.class))
            {
                FreePlayValuableComponent f = item.getContent().getComponent(FreePlayValuableComponent.class);
                int vv = f.getValue() * record.getAmount();
                valuables += vv;
                playerClient.addStat(Constants.Other.VALUABLES_ACTION, vv);

                toRemoveFP.add(record);
            }
            else if (item.getContent() instanceof ConsumableToOwnableContent)
            {
                ConsumableToOwnableContent consumable = ((ConsumableToOwnableContent) item.getContent());
                OwnableContent ownableContent = consumable.getOwnableContent();

                unlocked.put(ownableContent, unlocked.get(ownableContent, 0) + record.getAmount());

                playerClient.gotOwnable(ownableContent, "freeplay",
                        ClientProfile.OnwAction.owned, record.getAmount(), false);

                toRemoveFP.add(record);
            }
            else if (item.getContent() instanceof ConsumableContent)
            {
                if (item.getContent() instanceof ConsumableToStatContent)
                {
                    ConsumableToStatContent consumable = ((ConsumableToStatContent) item.getContent());
                    playerClient.addStat(consumable.getStat(), record.getAmount());
                    playerClient.addStat("freeplay-taken-" + consumable.getStat(), record.getAmount());
                    toRemoveFP.add(record);
                }

                ConsumableContent cc = (ConsumableContent) item.getContent();
                taken.put(cc, taken.get(cc, 0) + record.getAmount());
            }
        }

        ServerPlayerControllerComponentData pcc =
            playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

        for (ConsumableRecord record : toRemoveFP)
        {
            cnt.removeRecord(record);

            if (poc.getCurrentInstrumentRecord() == record)
            {
                pcc.selectFirstInstrument(poc);
            }
        }

        toRemoveFP.clear();

        if (taken.size > 0)
        {
            if (payload != null)
            {
                FreePlayItemsTakenOutEvent ev = FreePlayItemsTakenOutEvent.obtain(playerClient, taken);
                if (ev != null)
                {
                    payload.questEvent(ev, false);

                    for (ObjectMap.Entry<ConsumableContent, Integer> entry : ev.used)
                    {
                        cnt.getConsumable(entry.value, entry.key);
                    }

                    ev.free();
                }
            }
        }

        if (valuables > 0)
        {
            playerClient.storeEvents();
        }

        playerClient.sendUserProfile();
        playerClient.sendConsumable(cnt);
        sendSummaryInfo(playerClient, unlocked, true, valuables);

        playerClient.clearLocalStatsConsumableRecords();
    }

    private void saveFPContainers(PlayerData playerData, PlayerClient playerClient)
    {
        FreePayload payload = getPayload(playerClient);
        if (payload != null)
        {
            if (payload.isExited() || BrainOutServer.IsShuttingDown())
            {
                if (playerData != null)
                {
                    PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
                    if (poc == null)
                        return;

                    ConsumableContainer cnt = poc.getConsumableContainer();

                    FreePlayContainer fcnt = playerClient.getProfile().getContainers().get("h");
                    if (fcnt == null)
                    {
                        fcnt = new FreePlayContainer();
                        playerClient.getProfile().getContainers().put("h", fcnt);
                    }

                    dumpConsumableContainerToFP(playerClient, cnt, fcnt);
                }
            }
            else
            {
                removeItemsOnHand(playerClient);
            }
        }


        playerClient.addStat("freeplay-exited", 1);
        playerClient.setAllowDrop(false);
    }

    private void dumpConsumableContainerToFP(PlayerClient playerClient, ConsumableContainer cnt, FreePlayContainer fcnt)
    {
        fcnt.clear();

        if (cnt != null)
        {
            for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
            {
                ConsumableRecord record = entry.value;
                ConsumableItem item = record.getItem();

                if (item.getContent() instanceof ConsumableToOwnableContent)
                {
                    continue;
                }
                else if (item.getContent() instanceof ConsumableToStatContent)
                {
                    continue;
                }
                else if (item instanceof InstrumentConsumableItem)
                {
                    InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
                    if (ici.getInstrumentData() != null)
                    {
                        InstrumentData id = ici.getInstrumentData();

                        Instrument instrument = id.getInstrument();
                        if (instrument.getSlot() != null && instrument.getSlot().getID().equals("slot-melee"))
                        {
                            continue;
                        }

                        fcnt.addItem(new FreePlayContainer.InstrumentContainerItem(id, entry.value.getQuality()));
                    }
                }
                else if (item instanceof DecayConsumableItem)
                {
                    fcnt.addItem(new FreePlayContainer.DecayConsumableContainerItem(
                        ((DecayConsumableContent) item.getContent()),
                        ((DecayConsumableItem) item).getUses(), entry.value.getQuality()));
                }
                else if (item instanceof WalkietalkieConsumableItem)
                {
                    fcnt.addItem(new FreePlayContainer.WalkietalkieContainerItem(
                            ((Walkietalkie) item.getContent()),
                            ((WalkietalkieConsumableItem) item).getFrequency(), entry.value.getQuality()));
                }
                else if (item.getContent() instanceof ConsumableContent)
                {
                    fcnt.addItem(new FreePlayContainer.ConsumableContainerItem(
                            ((ConsumableContent) item.getContent()), record.getAmount(), entry.value.getQuality()));
                }
            }
        }
    }

    private int getPlayersAlive()
    {
        int count = 0;

        for (Map map : Map.All())
        {
            count += map.countActivesForTag(Constants.ActiveTags.PLAYERS,
                activeData ->
            {
                int owner = activeData.getOwnerId();

                Client client = BrainOutServer.Controller.getClients().get(owner);

                if (client == null || !client.isConnected())
                    return false;

                if (getGameMode().getPhase() == GameMode.Phase.game && client.isAlive())
                {
                    return false;
                }

                return true;
            });
        }

        return count;
    }

    public Party getParty(String partyId)
    {
        if (partyId == null)
            return null;

        return parties.get(partyId);
    }

    private FreePayload getPayload(PlayerClient playerClient)
    {
        ModePayload modePayload = playerClient.getModePayload();

        if (!(modePayload instanceof FreePayload))
            return null;

        return ((FreePayload) modePayload);
    }

    private void sendSummaryInfo(
        PlayerClient playerClient,
        ObjectMap<OwnableContent, Integer> unlocked,
        boolean alive, int valuables)
    {
        JSONObject summary = new JSONObject();

        FreePayload payload = getPayload(playerClient);

        if (payload == null)
            return;

        {
            JSONObject quests = new JSONObject();

            for (Quest quest : payload.getActiveQuests())
            {
                JSONObject q = new JSONObject();

                if (payload.hasUpdatedTasks(quest))
                {
                    JSONObject tasksProgress = new JSONObject();
                    payload.getUpdatedTasks(task -> tasksProgress.put(task.getId(),
                        task.getProgress(playerClient.getProfile(), playerClient.getAccount())), quest);
                    q.put("tasks-progress", tasksProgress);
                }

                if (payload.hasQuestJustCompleted(quest))
                {
                    q.put("completed", true);
                }

                quests.put(quest.getID(), q);
            }

            summary.put("quests", quests);
        }

        {
            JSONObject stats = new JSONObject();
            for (ObjectMap.Entry<String, Float> entry : playerClient.getLocalStats())
            {
                if (entry.value > 0)
                    stats.put(entry.key, entry.value);
            }

            if (valuables > 0)
            {
                stats.put("valuables", valuables);
            }

            summary.put("stats", stats);
        }
        {
            JSONObject unlocked_ = new JSONObject();

            if (unlocked != null)
            {
                for (ObjectMap.Entry<OwnableContent, Integer> entry : unlocked)
                {
                    unlocked_.put(entry.key.getID(), entry.value);
                }
            }

            summary.put("unlocked", unlocked_);
        }

        payload.resetUpdatedAndCompletedTasks();

        playerClient.sendTCP(new FreePlaySummaryMsg(summary, alive));

        if (playerClient.getProfile() != null)
        {
            playerClient.getProfile().setDirty();
        }
    }

    @Override
    public float calculateDamage(Team receiverTeam, Team senderTeam, int receiverId, int senderId, float dmg)
    {
        Client sender = BrainOutServer.Controller.getClients().get(senderId);
        Client receiver = BrainOutServer.Controller.getClients().get(receiverId);

        if (sender instanceof BotClient && receiver instanceof PlayerClient)
        {
            // bots make 1.5 less damage to players
            dmg /= 1.5f;
        }

        return super.calculateDamage(receiverTeam, senderTeam, receiverId, senderId, dmg);
    }

    @Override
    public void onClientReconnect(PlayerClient client, PlayerData playerData)
    {
        if (playerData != null && Map.IsSafeMap(playerData.getDimension()))
        {
            saveFPContainers(playerData, client);
        }
        else
        {
            removeItemsOnHand(client);
        }
    }

    @Override
    public void onClientDeath(Client client, Client killer, PlayerData playerData, InstrumentInfo info)
    {
        KarmaComponentData kmp = playerData.getComponent(KarmaComponentData.class);
        if (kmp != null)
        {
            int victimKarma = kmp.getKarma();

            if (killer instanceof PlayerClient)
            {
                PlayerClient killerClient = ((PlayerClient) killer);
                PlayerData killerData = killerClient.getPlayerData();

                int killerKarma;
                KarmaComponentData killerKmp = null;

                if (killerData != null)
                {
                    killerKmp = killerData.getComponent(KarmaComponentData.class);
                    killerKarma = killerKmp.getKarma();
                }
                else
                {
                    killerKarma = killerClient.getProfile().getInt("karma", 0);
                }

                if (victimKarma == 0)
                {
                    killerKarma = Math.max(killerKarma - 1, -10);
                }
                else if (victimKarma > 0)
                {
                    killerKarma = Math.max(killerKarma - 2, -10);
                }
                else
                {
                    int max;
                    if (client instanceof BotClient)
                    {
                        max = 3;
                    }
                    else
                    {
                        max = 10;
                    }
                    killerKarma = Math.min(killerKarma + 1, max);
                }

                killerClient.getProfile().setInt("karma", killerKarma);
                BrainOutServer.Controller.sendRemotePlayers(killerClient);

                if (killerKmp != null)
                {
                    killerKmp.setKarma(killerKarma);
                    killerKmp.updated(killerData);
                }
            }
        }

        if (client instanceof PlayerClient)
        {
            if (Map.IsSafeMap(playerData.getDimension()))
            {
                removeItemsOnHand(((PlayerClient) client));

                BrainOutServer.Timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        BrainOutServer.PostRunnable(() ->
                        {
                            spawnPlayer(spawnPointData, client);
                        });
                    }
                }, 3000);
            }
            else
            {
                FreePayload p = getPayload(((PlayerClient) client));
                if (p == null || !p.isExited())
                {
                    removeItemsOnHand(((PlayerClient) client));
                }

                BrainOutServer.PostRunnable(() ->
                {
                    sendSummaryInfo((PlayerClient) client, null, false, 0);
                });
            }
        }
    }



    private void removeItemsOnHand(PlayerClient client)
    {
        if (BrainOutServer.IsShuttingDown())
            return;

        ClientProfile up = client.getProfile();

        if (up != null && up.getContainers() != null)
        {
            FreePlayContainer ccc = up.getContainers().get("h");
            if (ccc != null)
            {
                ccc.clear();
            }
        }
    }

    @Override
    public void finished()
    {
    }

    private boolean isBotEnemy(ActiveData bot, Client to)
    {
        if ((to instanceof BotClient) && bot.getCreator().getID().equals("player-crow"))
        {
            // bots should not touch crows
            return false;
        }

        if (to.getPartyId() == null || to.getPartyId().equals(""))
            return true;

        Party party = parties.get(to.getPartyId());
        if (party == null)
            return true;

        ServerFreePartnerBotComponent quest = bot.getComponent(ServerFreePartnerBotComponent.class);

        if (quest == null)
            return true;

        return !quest.getPartyId().equals(to.getPartyId());
    }

    @Override
    public boolean isEnemiesActive(ActiveData a, ActiveData b)
    {
        if (a == null || b == null)
            return true;

        Client cA = BrainOutServer.Controller.getClients().getByActive(a);
        Client cB = BrainOutServer.Controller.getClients().getByActive(b);

        if (cA == null || cB == null)
        {
            BotControllerComponentData botA = a.getComponentWithSubclass(BotControllerComponentData.class);
            BotControllerComponentData botB = b.getComponentWithSubclass(BotControllerComponentData.class);

            if (botA != null && cB != null)
            {
                return isBotEnemy(a, cB);
            }
            else if (botB != null && cA != null)
            {
                return isBotEnemy(b, cA);
            }
            else
            {
                ServerFreePartnerBotComponent pA = a.getComponent(ServerFreePartnerBotComponent.class);
                ServerFreePartnerBotComponent pB = b.getComponent(ServerFreePartnerBotComponent.class);

                if (pA != null && pB != null)
                {
                    return !pA.getPartyId().equals(pB.getPartyId());
                }
                else
                {
                    return true;
                }
            }
        }

        if (cA instanceof BotClient && cB instanceof BotClient)
        {
            return false;
        }

        if (cA instanceof PlayerClient && cB instanceof PlayerClient)
        {
            PlayerClient a_ = ((PlayerClient) cA);
            PlayerClient b_ = ((PlayerClient) cB);

            if (a_.getPartyId() == null || b_.getPartyId() == null)
                return true;

            return !Objects.equals(a_.getPartyId(), b_.getPartyId());
        }

        return a != b;
    }

    @Override
    public boolean isEnemies(Client a, Client b)
    {
        if (a instanceof BotClient && b instanceof BotClient)
        {
            return false;
        }

        if (a instanceof PlayerClient && b instanceof PlayerClient)
        {
            PlayerClient a_ = ((PlayerClient) a);
            PlayerClient b_ = ((PlayerClient) b);

            if (a_.getModePayload() instanceof FreePayload && b_.getModePayload() instanceof FreePayload)
            {
                if (((FreePayload) a_.getModePayload()).getFriends() != null &&
                    (((FreePayload) a_.getModePayload()).getFriends() ==
                    ((FreePayload) b_.getModePayload()).getFriends()))
                {
                    return false;
                }
            }

            if (a_.getPartyId() == null || b_.getPartyId() == null)
                return true;

            return !Objects.equals(a_.getPartyId(), b_.getPartyId());
        }

        return a != b;
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
    public boolean hasFinishedTimer()
    {
        return false;
    }


    public InstrumentSlotItem getRandomPrimaryWeapon()
    {
        return getRandomItem(primaryWeaponsPool);
    }

    public InstrumentSlotItem getRandomSecondaryWeapon()
    {
        return getRandomItem(secondaryWeaponsPool);
    }

    public InstrumentSlotItem getRandomSpecialItem()
    {
        return getRandomItem(specialItemsPool);
    }

    public ConsumableToOwnableContent getRandomContainer()
    {
        return getRandomItem(containersPool);
    }

    public ConsumableContent getRandomConsumable()
    {
        return getRandomItem(consumables);
    }

    public ConsumableContent getRandomJunk()
    {
        return junk.random();
    }

    public Bullet getRandomBullet()
    {
        return getRandomItem(bulletPool);
    }

    @Override
    public void onClientDamaged(Client client, PlayerData playerData, String kind)
    {
        if (!playerData.isAlive())
            return;

        if ("bleeding".equals(kind))
            return;

        if ("cold".equals(kind))
            return;

        ServerFreeplayPlayerComponentData fp = playerData.getComponent(ServerFreeplayPlayerComponentData.class);

        if (fp == null)
            return;

        if (MathUtils.random(0, 2) == 0)
            return;

        if (fp.setBleeding(2, MathUtils.random(45, 75)))
        {
            BrainOutServer.PostRunnable(fp::sync);
        }
    }

    private <T extends Content> T getRandomItem(Array<T> items)
    {
        return RandomWeightComponent.GetRandomItem(items);
    }

    @Override
    public ModePayload newPlayerPayload(Client playerClient)
    {
        return new FreePayload(playerClient);
    }

    public interface PlayWithPartnerAgainCallback
    {
        void success();
        void failed(String reason);
    }

    @Override
    public void onUnknownPlayerDeath(PlayerData playerData, Client killer)
    {
        super.onUnknownPlayerDeath(playerData, killer);

        if (killer instanceof PlayerClient)
        {
            PlayerClient playerClient = ((PlayerClient) killer);
            ModePayload payload = playerClient.getModePayload();
            if (payload instanceof FreePayload)
            {
                FreePayload freePayload = ((FreePayload) payload);
                freePayload.questEvent(FreePlayEnemyOfKindKilledEvent.obtain(playerClient, playerData.getPlayer()));
            }
        }
    }

    public void playWithPartnerAgain(PlayerClient starter, String region, PlayWithPartnerAgainCallback callback)
    {
        String partyId = starter.getPartyId();

        if (partyId == null || partyId.isEmpty())
        {
            callback.failed("No party");
            return;
        }

        Party party = parties.get(partyId);

        if (party == null)
        {
            callback.failed("No such party");
            return;
        }

        Array<PlayWithPartnerAgainCallback> existing = ongoingPartyLookups.get(partyId);

        if (existing != null)
        {
            existing.add(callback);
            return;
        }

        PlayWithPartnerAgainCallback failed = new PlayWithPartnerAgainCallback()
        {

            @Override
            public void success()
            {
                //
            }

            @Override
            public void failed(String reason)
            {
                if (Log.ERROR) Log.error(reason);

                Array<PlayWithPartnerAgainCallback> existing = ongoingPartyLookups.get(partyId);
                if (existing != null)
                {
                    for (PlayWithPartnerAgainCallback callback : existing)
                    {
                        callback.failed(reason);
                    }
                }

                ongoingPartyLookups.remove(partyId);

                callback.failed(reason);
            }
        };

        existing = new Array<>();
        ongoingPartyLookups.put(partyId, existing);

        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        if (loginService == null || gameService == null)
        {
            failed.failed("No login or game service defined!");
            return;
        }

        RoomSettings roomSettings = new RoomSettings();
        roomSettings.setRegion(region, false);

        // we are looking for collecting only
        roomSettings.setWarmup("true");
        roomSettings.setMyLevelOnly(false);
        roomSettings.setLevel(-1);
        roomSettings.setNewbie(null);

        GameService.RoomsFilter filter = new GameService.RoomsFilter();
        GameService.RoomSettings settings = new GameService.RoomSettings();

        roomSettings.write(settings);
        roomSettings.write(filter);

        ArrayList<GameService.JoinMultiWrapper> wrappers = new ArrayList<>();

        for (ObjectMap.Entry<String, PlayerClient> entry : party.members)
        {
            PlayerClient playerClient = entry.value;

            if (!playerClient.isInitialized())
                continue;

            GameService.JoinMultiWrapper wrapper =
                new GameService.JoinMultiWrapper(
                    playerClient.getAccessToken(),
                    playerClient.getIP());

            wrappers.add(wrapper);
        }

        if (Log.INFO) Log.info("Joining party...");


        Array<PlayWithPartnerAgainCallback> finalExisting = existing;

        gameService.joinGameMulti(loginService.getCurrentAccessToken(), wrappers, "freeplay", filter, true, true,
            settings, new GameService.JoinGameMultiCallback()
        {
            @Override
            public void success(String roomId, HashMap<String, GameService.JoinMultiSlot> slots, String host, int[] ports, JSONObject settings)
            {
                BrainOutServer.PostRunnable(() ->
                {
                    for (PlayWithPartnerAgainCallback callback : finalExisting)
                    {
                        callback.success();
                    }

                    ongoingPartyLookups.remove(partyId);

                    partyStarted(party, roomId, slots, host, ports, settings);
                });
            }

            @Override
            public void fail(Request request, Request.Result result)
            {
                BrainOutServer.PostRunnable(() -> failed.failed("Failed to start a party: " + result.toString()));
            }
        });

        /*

        new GameService.JoinGameMultiCallback()
        {
            @Override
            public void success(
                    String roomId,
                    HashMap<String, GameService.JoinMultiSlot> slots,
                    String host,
                    int[] ports,
                    JSONObject settings)
            {
                locked = false;

                BrainOutServer.PostRunnable(
                        () -> {
                            callback.result(true);

                            partyStarted(roomId, slots, host, ports, settings);
                        });
            }

            @Override
            public void fail(Request request, Status status)
            {
                locked = false;

                callback.result(false);

                if (Log.ERROR) Log.error("Failed to start a party: " + status.toString());
            }

        */
    }

    private void partyStarted(Party party,
                              String roomId,
                              HashMap<String, GameService.JoinMultiSlot> slots,
                              String host, int[] ports,
                              JSONObject settings)
    {
        if (Log.INFO) Log.info("Party started (" + slots.size() + " slots)!");

        for (ObjectMap.Entry<String, PlayerClient> entry : party.members)
        {
            PlayerClient playerClient = entry.value;

            if (!playerClient.isInitialized())
                continue;

            GameService.JoinMultiSlot slot = slots.getOrDefault(playerClient.getAccount(), null);

            if (slot != null)
            {
                playerClient.log("Got slot: " + slot.key);

                playerClient.sendTCP(new PartyStartedMsg(
                    roomId, slot.key, host, ports, settings
                ));
            }
            else
            {
                playerClient.log("Got no slot in party.");
                playerClient.disconnect(DisconnectReason.badPlayer, "partyStarted: Got no slot in party.");
            }
        }
    }

    private int getJunkAmount(PlayerOwnerComponent poc)
    {
        ConsumableContent junk = BrainOutServer.ContentMgr.get("consumable-item-junk", ConsumableContent.class);
        return poc.getConsumableContainer().getAmount(junk);
    }

    private boolean havePaintItem(PlayerOwnerComponent poc)
    {
        ConsumableContent paint = BrainOutServer.ContentMgr.get("consumable-item-paint", ConsumableContent.class);

        ConsumableRecord r = poc.getConsumableContainer().getConsumable(paint);
        if (r == null || !(r.getItem() instanceof DecayConsumableItem))
        {
            return false;
        }

        ((DecayConsumableItem) r.getItem()).use(poc.getConsumableContainer(), r);

        return true;
    }

    public ConsumableRecord playerUpdateWeaponSkin(PlayerClient playerClient, PlayerData playerData,
                                                   PlayerOwnerComponent poc, int object, Skin skin)
    {
        ConsumableContainer cc = poc.getConsumableContainer();
        ConsumableRecord record = cc.get(object);

        if (record == null)
            return null;

        if (playerClient.getProfile() == null)
            return null;

        if (!(record.getItem() instanceof InstrumentConsumableItem))
            return null;

        if (!havePaintItem(poc))
            return null;

        InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());

        InstrumentData instrumentData = ici.getInstrumentData();

        if (instrumentData == null)
            return null;

        InstrumentSlotItem slotItem = instrumentData.getInstrument().getSlotItem();

        if (slotItem == null)
            return null;

        if (!slotItem.getSkins().contains(skin, true))
            return null;

        if (!skin.hasItem(playerClient.getProfile()))
            return null;

        instrumentData.setSkin(skin);

        cc.removeRecord(record);
        record.setId(cc.newId());
        cc.addRecord(record);

        playerClient.sendConsumable(cc);
        poc.setCurrentInstrument(record);
        poc.updated(playerData);
        playerClient.sendTCP(new ServerSelectInstrumentMsg(record));

        BrainOutServer.PostRunnable(() ->
            BrainOutServer.Controller.getClients().sendTCP(
                new RemoteUpdateInstrumentMsg(playerData, poc.getCurrentInstrument(), poc.getHookedInstrument())));

        BrainOutServer.Controller.getClients().sendUDP(
            new LaunchEffectMsg(playerData.getDimension(),
                playerData.getX(), playerData.getY(), "skin-owned-snd-effect"));

        ModePayload payload = playerClient.getModePayload();
        if (payload instanceof FreePayload)
        {
            FreePayload freePayload = ((FreePayload) payload);
            freePayload.questEvent(FreePlayItemPaintedEvent.obtain(playerClient, instrumentData));
        }

        return record;
    }

    public ConsumableRecord playerInstallWeaponUpgrade(PlayerClient playerClient,
                                                       PlayerData playerData,
                                                       PlayerOwnerComponent poc,
                                                       int object, String key, Upgrade upgrade)
    {
        ConsumableContent junk = BrainOutServer.ContentMgr.get("consumable-item-junk", ConsumableContent.class);

        ConsumableContainer cc = poc.getConsumableContainer();
        ConsumableRecord record = cc.get(object);

        Shop.ShopItem shopItem = upgrade.getShopItem();

        if (shopItem == null)
            return null;

        if (record == null)
            return null;

        if (playerClient.getProfile() == null)
            return null;

        if (!(record.getItem() instanceof InstrumentConsumableItem))
            return null;

        InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());

        int junkAmount = getJunkAmount(poc);

        InstrumentData instrumentData = ici.getInstrumentData();

        if (instrumentData == null)
            return null;

        InstrumentSlotItem slotItem = instrumentData.getInstrument().getSlotItem();

        if (slotItem == null)
            return null;

        if (!slotItem.getUpgrades().containsKey(key))
            return null;

        Array<Upgrade> line = slotItem.getUpgrades().get(key);

        if (!line.contains(upgrade, true))
            return null;

        int amount = getRequiredAmount(shopItem);

        if (junkAmount < amount)
            return null;

        cc.decConsumable(junk, amount);

        instrumentData.attachUpgrade(key, upgrade);

        cc.removeRecord(record);
        record.setId(cc.newId());
        cc.addRecord(record);

        playerClient.sendConsumable(cc);
        poc.setCurrentInstrument(record);
        poc.updated(playerData);
        playerClient.sendTCP(new ServerSelectInstrumentMsg(record));

        BrainOutServer.PostRunnable(() ->
            BrainOutServer.Controller.getClients().sendTCP(
                new RemoteUpdateInstrumentMsg(playerData, poc.getCurrentInstrument(), poc.getHookedInstrument())));

        BrainOutServer.Controller.getClients().sendUDP(
            new LaunchEffectMsg(playerData.getDimension(),
                playerData.getX(), playerData.getY(), "upgrade-installed-snd"));

        ModePayload payload = playerClient.getModePayload();
        if (payload instanceof FreePayload)
        {
            FreePayload freePayload = ((FreePayload) payload);
            freePayload.questEvent(FreePlayWeaponUpgradedEvent.obtain(playerClient));
        }

        return record;
    }

    private int getRequiredAmount(Shop.ShopItem shopItem)
    {
        if (shopItem.getCurrency().equals(Constants.User.GEARS))
            return 1;

        return shopItem.getAmount();
    }

    public void requestDropOff(Map map, float x, float y, String kind)
    {
        Active helicopter = BrainOutServer.ContentMgr.get("active-helicopter", Active.class);

        ActiveData activeData = helicopter.getData(map.getDimension());
        activeData.setPosition(map.getWidth() + 64, 100);
        activeData.setLayer(Constants.Layers.ACTIVE_LAYER_3);
        activeData.setzIndex(1000);

        DropOffComponentData drops = activeData.getComponent(DropOffComponentData.class);

        if (drops != null)
        {
            Array<FreeplayDropoff.Generator> generators = dropOff.generate(kind);
            int sz = generators.size;

            for (int i = -1; i < 2; i++)
            {
                float position = x + MathUtils.random(0.0f, 32.0f) - 16.0f + i * 64.0f;

                Array<FreeplayDropoff.Generator> gens = new Array<>();

                for (int j = 0, t = sz / 3; j < t; j++)
                {
                    if (generators.size <= 0)
                        break;

                    FreeplayDropoff.Generator gen = generators.pop();
                    gens.add(gen);
                }

                DropOffComponentData.DropOff off = new DropOffComponentData.DropOff(position, gens);
                drops.addDropOff(off);
            }
        }

        SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        phy.getSpeed().x = -40;

        map.addActive(map.generateServerId(), activeData, true);
    }

    private void changeSkin(PlayerClient playerClient, int object)
    {
        if (!playerClient.isAlive())
            return;

        PlayerData playerData = playerClient.getPlayerData();

        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return;

        ConsumableRecord record = poc.getConsumableContainer().get(object);

        if (record == null)
            return;

        ConsumableItem item = record.getItem();

        if (!(item instanceof PlayerSkinConsumableItem))
            return;

        PlayerSkinConsumableItem playerSkinItem = ((PlayerSkinConsumableItem) item);
        PlayerSkin playerSkin = playerSkinItem.getContent();

        ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return;

        if (progress.isRunning())
            return;

        playerClient.enablePlayer(false);

        progress.startNonCancellable(3, () ->
        {
            ModePayload payload = playerClient.getModePayload();
            if (payload instanceof FreePayload)
            {
                FreePayload freePayload = ((FreePayload) payload);
                freePayload.questEvent(FreePlayItemUsedEvent.obtain(playerClient, playerSkinItem.getContent(), 1));
            }

            PlayerAnimationComponentData pac = playerData.getComponent(PlayerAnimationComponentData.class);
            playerSkinItem.setPlayerSkin(pac.getSkin());
            pac.setSkin(playerSkin);
            pac.updated(playerData);

            playerClient.enablePlayer(true);
            playerClient.sendConsumable();
        });
    }

    public void flushItems()
    {
        for (Map map : Map.All())
        {
            if (map.isSafeMap())
                continue;

            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
            {
                if (!(activeData instanceof ItemData))
                    continue;

                ((ItemData) activeData).getRecords().clear();
                activeData.updated();
            }
        }
    }

    @Override
    public boolean spectatorsCanSeeEnemies()
    {
        return false;
    }

    @Override
    public boolean needRolesForBots()
    {
        return false;
    }
}
