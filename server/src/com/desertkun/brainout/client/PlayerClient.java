package com.desertkun.brainout.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.Version;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.*;
import com.desertkun.brainout.common.msg.*;
import com.desertkun.brainout.common.msg.client.*;
import com.desertkun.brainout.common.msg.server.*;
import com.desertkun.brainout.components.DurabilityComponent;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.content.battlepass.BattlePass;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.*;
import com.desertkun.brainout.content.consumable.Walkietalkie;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.consumable.impl.OwnableConsumableItem;
import com.desertkun.brainout.content.consumable.impl.WalkietalkieConsumableItem;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.content.instrument.Box;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.*;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.*;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.data.instrument.*;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.mode.*;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.online.*;
import com.desertkun.brainout.playstate.*;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.server.ServerController;
import com.desertkun.brainout.utils.UnlockSubscription;
import com.desertkun.brainout.utils.VersionCompare;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import com.esotericsoftware.kryonet.Connection;
import org.anthillplatform.runtime.services.*;
import org.json.JSONObject;

import java.util.*;

public class PlayerClient extends Client
{
    private ClientProfile profile;
    private ContentLockTree.Subscription subscription;
    private LoginService.AccessToken accessToken;
    private String accessTokenAccount;
    private String accessTokenCredential;
    private Connection connection;
    private boolean egg;
    private ObjectMap<Integer, ServerEvent> events;
    private ObjectMap<String, Long> statUpdateThreshold;
    private ObjectMap<Integer, Integer> killRecord;
    private Array<String> friends;
    private ObjectMap<String, Float> clanParticipationProfileStats;
    private ObjectMap<String, Float> localStats;
    private boolean allowDrop;
    private boolean ready;
    private boolean mapDownloading;
    private Vector2 lastKnownPosition;
    private String lastKnownDimension;

    private float pongCheck;
    private long lastPong;
    private long firstPongC, lastPongC, firstPongNanoTime, lastPongNanoTime;
    private long[] pingMeasures;
    private long ping;
    private int currentPingMeasure;
    private float updatedSelectionMessages = 0;
    private float marketCooldown;

    private PlayerMessages playerMessages;
    private MessageService.MessageSession messageSession;
    private PlayerHandlers playerHandlers;

    private String key;
    private float reconnectTimer;
    private boolean reconnected;

    private boolean cheater;
    private float timeSyncCheck = 0;
    private float timeSyncDeadline = 30.0f;
    private float chatLimit = 0;

    private float autoKick;
    private boolean tookPartInWarmup;
    private String clanName;
    private String partyId;
    private boolean special;
    private String bp;
    private int currentlyWatching = -1;

    public interface OutgoingTCPMessage
    {
        Object serialize();
    }

    private Queue<OutgoingTCPMessage> outgoingTCPMessages;

    public static class Statistics
    {
        private long udp;
        private long tcp;

        private long sentUdp;
        private long sentTcp;

        private long sendUdpRate;
        private long sendTcpRate;

        private boolean collectPerClass;

        private ObjectMap<String, Integer> sentPerClass = new ObjectMap<>();

        private float timer;

        public long getSendTcpRate()
        {
            return sendTcpRate;
        }

        public long getSendUdpRate()
        {
            return sendUdpRate;
        }

        public long getSentUdp()
        {
            return sentUdp;
        }

        public long getSentTcp()
        {
            return sentTcp;
        }

        public void udp(int sent, String name)
        {
            this.udp += sent;
            this.sentUdp += sent;

            if (collectPerClass)
            {
                sentPerClass.put(name, sentPerClass.get(name, 0) + sent);
            }
        }

        public ObjectMap<String, Integer> getSentPerClass()
        {
            return sentPerClass;
        }

        public void tcp(int sent, String name)
        {
            this.tcp += sent;
            this.sentTcp += sent;

            if (collectPerClass)
            {
                sentPerClass.put(name, sentPerClass.get(name, 0) + sent);
            }
        }

        public void setCollectPerClass()
        {
            this.collectPerClass = true;
            sentPerClass.clear();
        }

        public void update(float dt)
        {
            timer -= dt;

            if (timer < 0)
            {
                timer = 5.0f;

                sendUdpRate = udp / 5;
                sendTcpRate = tcp / 5;
            }
        }
    }

    private Statistics statistics;

    public PlayerClient(int id, ServerController serverController)
    {
        super(id, serverController);

        this.statistics = new Statistics();

        this.egg = false;
        this.pingMeasures = new long[5];
        this.lastPongC = 0;
        this.firstPongNanoTime = 0;
        this.lastPong = System.currentTimeMillis();
        this.events = new ObjectMap<>();
        this.statUpdateThreshold = new ObjectMap<>();
        this.reconnectTimer = 0;
        this.reconnected = false;
        this.killRecord = new ObjectMap<>();
        this.localStats = new ObjectMap<>();
        this.allowDrop = true;
        this.ready = false;
        this.mapDownloading = false;
        this.outgoingTCPMessages = new Queue<>();
        this.playerHandlers = new PlayerHandlers(this);
        this.lastKnownPosition = new Vector2();
        this.lastKnownDimension = "default";
    }

    @Override
    public void reset()
    {
        super.reset();

        killRecord.clear();
        localStats.clear();
        currentPingMeasure = 0;
        mapDownloading = false;

        resetSubscriptions(profile);
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    /* =================================== CLIENT RECEIVERS =================================== */

    @SuppressWarnings("unused")
    public boolean received(final VoiceChatMsg msg)
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return true;

        BrainOutServer.PostRunnable(() ->
            ((ServerRealization) gameMode.getRealization()).voiceChat(this, msg.data, msg.volume),
            30);

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final RequestMsg msg)
    {
        playerHandlers.receive(msg);

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ChatSendMsg msg)
    {
        if (msg.text.toLowerCase().contains("we are legion"))
        {
            this.egg = true;
        }

        BrainOutServer.PostRunnable(() ->
        {
            String filtered = BrainOutServer.getInstance().validateText(msg.text);

            chatLimit += 1;

            if (chatLimit > 30)
            {
                addStat("spammer", 1);

                if (BrainOutServer.Controller.isLobby())
                {
                    addStat("lobby-spammer", 1);
                }
                else
                {
                    kick("chat limit");
                }
                return;
            }
            else
            {
                if (BrainOutServer.Controller.isLobby() && getStat("lobby-spammer", 0.0f) > 0.0f)
                {
                    return;
                }
            }

            ServerController controller = getServerController();

            GameMode gameMode = BrainOutServer.Controller.getGameMode();

            if (gameMode != null)
            {

                ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());

                switch (msg.mode)
                {
                    case everyone:
                    {
                        controller.getClients().sendTCP(
                                new ChatMsg(getName(), filtered, "server", ServerConstants.Chat.COLOR_INFO, getId()),
                                client -> serverRealization.allowChat(PlayerClient.this, client));

                        break;
                    }
                    case teamOnly:
                    {

                        controller.getClients().sendTCP(
                                new ChatMsg(getName(), filtered, "team", ServerConstants.Chat.COLOR_INFO, getId())
                                , client ->
                                        serverRealization.allowChat(PlayerClient.this, client) && (
                                                client == PlayerClient.this ||
                                                        !(gameMode.isEnemies(client.getTeam(), getTeam()) ||
                                                                ((ServerRealization) gameMode.getRealization()).isEnemies(client, this))));

                        break;
                    }
                    case clan:
                    {
                        if (profile != null && profile.isParticipatingClan())
                        {
                            sendClanMessage(filtered);
                        }

                        break;
                    }
                }
            }
            else
            {
                final boolean hasTeamWon;
                PlayState playState = controller.getPlayState();

                if (playState instanceof PlayStateEndGame)
                {
                    hasTeamWon = ((PlayStateEndGame) playState).getGameResult().hasTeamWon();
                }
                else
                {
                    hasTeamWon = false;
                }

                switch (msg.mode)
                {
                    case everyone:
                    {
                        controller.getClients().sendTCP(
                                new ChatMsg(getName(), filtered, "server", ServerConstants.Chat.COLOR_INFO, getId()));

                        break;
                    }
                    case teamOnly:
                    {
                        controller.getClients().sendTCP(
                                new ChatMsg(getName(), filtered, "team", ServerConstants.Chat.COLOR_INFO, getId())
                                , client -> client == PlayerClient.this || (hasTeamWon && !controller.isEnemies(client.getTeam(), getTeam())));

                        break;
                    }
                    case clan:
                    {
                        if (profile != null && profile.isParticipatingClan())
                        {
                            sendClanMessage(filtered);
                        }

                        break;
                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final DropConsumableMsg msg)
    {
        if (playerController == null) return false;

        if (getState() == State.spawned && getPlayerData() != null)
        {
            final Map map = getPlayerData().getMap();

            if (map == null)
                return true;

            BrainOutServer.PostRunnable(() ->
            {
                if (playerData == null)
                    return;

                PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

                if (poc != null)
                {
                    playerController.dropConsumable(msg.id, msg.angle, msg.amount);
                }
            });
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(PickUpItemMsg msg)
    {
        if (getState() == State.spawned && getPlayerData() != null)
        {
            final Map map = getPlayerData().getMap();

            if (map == null)
                return true;

            ActiveData item = map.getActiveData(msg.object);

            if (item instanceof ItemData)
            {
                final ItemData itemData = ((ItemData) item);

                BrainOutServer.PostRunnable(() ->
                {
                    if (playerData == null || playerController == null) return;

                    PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

                    if (poc != null)
                    {
                        playerController.pickUpItem(itemData);
                    }
                });
            }
            else
            {
                sendUDP(new UnknownActiveDataMsg(msg.object, map.getDimension()));
            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(ItemActionMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (playerData == null)
                return;

            Map map = playerData.getMap();

            ActiveData activeData = map.getActiveData(msg.object);

            if (activeData instanceof ItemData)
            {
                if (Vector2.dst2(playerData.getX(), playerData.getY(),
                    activeData.getX(), activeData.getY()) <= 100)
                {
                    BrainOutServer.Controller.getClients().sendTCP(msg);
                }
            }

        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(TakeRecordFromItemMsg msg)
    {
        if (getState() == State.spawned && getPlayerData() != null)
        {
            final Map map = playerData.getMap();

            if (map == null)
                return true;

            ActiveData item = map.getActiveData(msg.object);

            if (item instanceof ItemData)
            {
                final ItemData itemData = ((ItemData) item);

                if (itemData instanceof RoundLockSafeData && ((RoundLockSafeData) itemData).isLocked())
                {
                    return true;
                }

                BrainOutServer.PostRunnable(() ->
                {
                    if (playerData == null || playerController == null) return;

                    PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

                    if (poc != null)
                    {
                        ConsumableRecord record = itemData.getRecords().getData().get(msg.record);

                        if (record != null)
                        {
                            playerController.pickUpRecordItem(itemData, record, msg.amount);
                        }
                    }
                });
            }
            else
            {
                sendUDP(new UnknownActiveDataMsg(msg.object, map.getDimension()));
            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(PutRecordIntoItemMsg msg)
    {
        if (getState() == State.spawned && getPlayerData() != null)
        {
            final Map map = playerData.getMap();

            if (map == null)
                return true;

            ActiveData item = map.getActiveData(msg.object);

            if (item instanceof ItemData)
            {
                final ItemData itemData = ((ItemData) item);

                BrainOutServer.PostRunnable(() ->
                {
                    if (playerData == null || playerController == null) return;

                    PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

                    if (poc != null)
                    {
                        ConsumableRecord record = poc.getConsumableContainer().getData().get(msg.record);

                        if (record != null)
                        {
                            playerController.putRecordIntoItem(itemData, record, msg.amount);
                        }
                    }
                });
            }
            else
            {
                sendUDP(new UnknownActiveDataMsg(msg.object, map.getDimension()));
            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(GetProfileMsg msg)
    {
        sendTCP(new UserProfileMsg(profile));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(ClientDisconnect msg)
    {
        setDisconnectReason(msg.reason);

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(HelloMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            switch (getState())
            {
                case none:
                {
                    if (msg.version.equals(Version.VERSION))
                    {
                        if (BrainOut.OnlineEnabled())
                        {
                            if (!BrainOutServer.getInstance().hasOnlineController())
                            {
                                if (msg.accessToken == null)
                                {
                                    log("No key nor access token!");
                                    disconnect(DisconnectReason.forbidden, "No key nor access token");
                                    return;
                                }

                                log("No online controller, exchanging key in an old way!");
                                extend(msg.accessToken);
                                return;
                            }

                            setKey(msg.key);

                            LoginService loginService = LoginService.Get();

                            if (loginService == null)
                            {
                                log("No login service defined!");
                                disconnect(DisconnectReason.serverError, "No login service defined");
                                return;
                            }

                            log("Exchanging a key for a player...");

                            BrainOutServer.getInstance().onlineControllerJoined(
                                msg.key,
                                loginService.getCurrentAccessToken(),
                                ServerConstants.Online.EXTEND_SCOPES,
                                (success, token, account, credential, scopes, info) ->
                                {
                                    BrainOutServer.PostRunnable(() ->
                                    {
                                        if (success)
                                        {
                                            log("Exchanged a token: " + account + " / " + credential);
                                            login(token, account, credential, scopes, info);
                                        }
                                        else
                                        {
                                            logError("Failed to exchange a token");

                                            disconnect(DisconnectReason.forbidden, "Failed to exchange a token");
                                        }
                                    });
                                });

                        }
                        else
                        {
                            log("User is admin since online is disabled.");

                            setupOfflineProfile();
                            onlineInitialized(true);
                            setRights(PlayerRights.admin);
                            sendServerInfo();
                        }
                    }
                    else
                    {
                        // version don't match
                        sendTCP(new VersionMismatchMsg(Version.VERSION));
                    }

                    break;
                }
                default:
                {
                    log("Warning: bad status: " + getState().toString());
                    break;
                }
            }
        });

        return true;
    }

    private void setupOfflineProfile()
    {
        JSONObject defaultProfile = new JSONObject(Gdx.files.local("default-profile.json").readString("UTF-8"));
        setProfile(defaultProfile);
    }

    private void extend(String token)
    {
        LoginService loginService = LoginService.Get();

        if (loginService == null)
            throw new RuntimeException("No login service!");

        loginService.extend(
            loginService.newAccessToken(token),
            loginService.getCurrentAccessToken(),
            LoginService.Scopes.FromString(ServerConstants.Online.EXTEND_SCOPES),
        (service, request, result, accessToken, account, credential, scopes) ->
        {
            BrainOutServer.PostRunnable(() ->
            {
                if (result == Request.Result.success)
                {
                    log("Exchanged a token in an old way!");
                    log(accessToken.get());

                    JSONObject info = new JSONObject();
                    //info.put("multi_id", "fake-multi-" + (getId() >> 1));
                    login(accessToken, account, credential, scopes, info);
                }
                else
                {
                    logError("Failed to extend token: " + result.toString());
                    disconnect(DisconnectReason.forbidden, "Failed to extend token: " + result.toString());
                }
            });
        });
    }

    @SuppressWarnings("unused")
    public boolean received(SimpleMsg msg)
    {
        switch (msg.code)
        {
            case mapInited:
            {
                BrainOutServer.PostRunnable(this::mapInitialized);

                break;
            }
            case clientInited:
            {
                BrainOutServer.PostRunnable(this::clientInited);
                break;
            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(SelectInstrumentMsg selectInstrumentMsg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (playerData != null && playerController != null)
            {
                playerController.changeInstrument(selectInstrumentMsg.id);
            }
        });

        return true;
    }

    private long lag_detection = 0;

    @SuppressWarnings("unused")
    public boolean received(ClientInstrumentEffectMsg msg)
    {
        long now = System.currentTimeMillis();

        if (lag_detection == 0 || now - lag_detection > 2000)
        {
            lag_detection = now;
        }
        else
        {
            lag_detection += 40;

            if (lag_detection > now + 1000)
            {
                // drop
                return true;
            }
        }

        if (playerData != null && playerData.getId() == msg.object && playerController != null)
        {
            if (isCheater())
                return true;

            BrainOutServer.PostRunnable(() ->
            {
                if (playerData != null)
                {
                    ServerMap map = playerData.getMap(ServerMap.class);

                    if (map == null)
                        return;

                    ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());

                    boolean syncOthers = chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);

                    if (syncOthers)
                    {
                        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
                        if (poc == null)
                            return;

                        InstrumentData instrument = poc.getCurrentInstrument();

                        if (instrument != null && instrument.getInstrument().getID().equals(msg.instrument))
                        {
                            sendUDPExcept(new InstrumentEffectMsg(
                                    playerData, instrument, msg.effect
                            ));
                        }
                    }
                }
            });
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(ReliableReceivedMsg receivedMsg)
    {
        getServerController().getReliableManager().delivered(receivedMsg.messageId);

        return true;
    }
    @SuppressWarnings("unused")
    public boolean received(FriendListMsg msg)
    {
        this.friends = new Array<>(msg.friends);

        log("Received friends list.");

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final WeaponActionMsg action)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (playerData != null && playerController != null)
            {
                final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

                if (poc != null)
                {
                    ConsumableRecord record = poc.getConsumableContainer().get(action.recordId);

                    playerController.weaponAction(action.action, record, action.slot, action.slotB);
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final WeaponMagazineActionMsg action)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (playerData != null && playerController != null)
            {
                final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

                if (poc != null)
                {
                    ConsumableRecord record = poc.getConsumableContainer().get(action.recordId);

                    playerController.weaponMagazineAction(action.action, record, action.slot, action.magazineId);
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SimpleInstrumentActionMsg action)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (playerData != null && playerController != null)
            {
                Map map = playerData.getMap();

                if (map == null)
                    return;

                ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());

                if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
                    return;

                boolean syncToOthers = chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);

                final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
                final ServerPlayerControllerComponentData sp =
                    playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

                if (poc != null && sp != null)
                {
                    ConsumableRecord record = poc.getConsumableContainer().get(action.recordId);

                    if (record != null)
                    {
                        InstrumentData instrumentData = poc.getInstrument(record.getId());

                        if (instrumentData instanceof GrenadeData)
                        {
                            ServerGrenadeComponentData tc = instrumentData.getComponent(ServerGrenadeComponentData.class);
                            ThrowableActive throwActive = ((GrenadeData) instrumentData).getThrowActive();

                            if (throwActive != null && tc != null)
                            {
                                TimeToLiveComponent ttl = throwActive.getComponent(TimeToLiveComponent.class);

                                if (ttl != null)
                                {
                                    tc.cook(ttl.getTime());
                                }
                            }
                        }

                        if (syncToOthers)
                        {
                            sendUDPExcept(sp.generateInstrumentActionMessage(instrumentData, action.action));
                        }
                    }
                }
            }

        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final BadgeReadMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (profile != null)
            {
                profile.removeBadge(msg.id);
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(PongMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            lastPong = System.currentTimeMillis();
            lastPongNanoTime = System.nanoTime();
            lastPongC = msg.c;

            if (firstPongC == 0)
            {
                firstPongNanoTime = System.nanoTime();
                firstPongC = msg.c;
            }

            long time = System.currentTimeMillis() - msg.timeStamp;

            if (time > 0)
            {
                pingMeasures[currentPingMeasure] = time;
                currentPingMeasure++;
                if (currentPingMeasure >= pingMeasures.length)
                {
                    currentPingMeasure = 0;

                    this.ping = 0;
                    for (long l : pingMeasures)
                    {
                        this.ping += l;
                    }

                    this.ping /= pingMeasures.length;
                }

                if (Log.DEBUG) Log.debug("Client " + getName() + " ping: " + getPing());
            }
            else
            {
                this.ping = 0;
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final DisassembleTrophyMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (profile != null)
            {
                Trophy trophy = profile.getTrophy(msg.index);
                if (trophy != null)
                {
                    award(NotifyAward.techScore, trophy.getXp());
                    notify(NotifyAward.techScore, trophy.getXp(), NotifyReason.purchase, NotifyMethod.message, null);

                    addStat("disassembled-trophies", 1);

                    profile.removeTrophy(msg.index);
                    sendUserProfile();
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ActiveUpdateRequestMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (playerData == null)
                return;

            Map map = playerData.getMap();
            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.activeId);

            if (activeData != null)
            {
                sendTCP(new UpdatedActiveDataMsg(activeData, PlayerClient.this, getId()));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final UpdateOrderMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            updateOrder(msg.orderId, (success, store, orderId, currency, total, item) ->
            {
                sendTCP(new UpdateOrderResultMsg(success, store, orderId, currency, total, item));
            });
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final CreateNewOrderMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            log("New order received: " + msg.store + "/" + msg.item + " of " + msg.amount + " " + msg.currency +
                " with " + msg.component + " component");

            if (!BrainOutServer.Env.checkPurchaseLimit(this))
            {
                sendTCP(new NewOrderResultMsg(false, -1, "MENU_DAILY_LIMIT_EXCEEDED"));
                return;
            }

            HashMap<String, String> env = new HashMap<>();

            env.put("ip_address", getIP());

            for (CreateNewOrderMsg.OrderEnvironmentItem item : msg.env)
            {
                env.put(item.key, item.value);
            }

            if (!BrainOut.OnlineEnabled())
            {
                BrainOutServer.PostRunnable(() ->
                {
                    JSONObject privates = new JSONObject(Gdx.files.local("store-private.json").readString());
                    JSONObject private_ = privates.getJSONObject(msg.item);
                    applyOrderContents(private_, msg.amount, msg.item);
                });

                sendTCP(new NewOrderResultMsg(true, 0, ""));
                return;
            }

            createNewOrder(msg.store, msg.item, msg.amount, msg.currency, msg.component, env,
                (success, orderId) -> sendTCP(new NewOrderResultMsg(success, orderId, "")));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ContentActionMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (profile != null)
            {
                Content content = BrainOut.ContentMgr.get(msg.what);

                if (content == null) return;

                switch (msg.action)
                {
                    case purchase:
                    {
                        purchaseContent(content);

                        break;
                    }
                    case repair:
                    {
                        repairContent(content);

                        break;
                    }
                    case open:
                    {
                        openContent(content);

                        break;
                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final MarkMessageReadMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (messageSession == null)
                return;

            messageSession.markMessageAsRead(msg.messageId);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SelectCategoryMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Slot slot = BrainOutServer.ContentMgr.get(msg.slot, Slot.class);

            if (slot == null || profile == null)
                return;

            if (msg.category == null)
            {
                profile.removeSelection(slot.getCategorySelectionId());
            }
            else
            {
                Slot.Category category = slot.getCategory(msg.category);

                if (category != null)
                {
                    profile.setSelection(slot.getCategorySelectionId(), category.getId());
                }
            }

            profile.setDirty();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SelectTagMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Slot slot = BrainOutServer.ContentMgr.get(msg.slot, Slot.class);

            if (slot == null)
                return;

            if (msg.tag == null)
            {
                profile.removeSelection(slot.getTagSelectionId());
            }
            else
            {
                Slot.Tag tag = slot.getTag(msg.tag);

                if (tag != null)
                {
                    profile.setSelection(slot.getTagSelectionId(), tag.getId());
                }
            }

            profile.setDirty();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SelectFavoritesMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Slot slot = BrainOutServer.ContentMgr.get(msg.slot, Slot.class);

            if (slot == null)
                return;

            profile.setSelection(slot.getTagSelectionId(), Constants.Other.FAVORITES_TAG);
            profile.setDirty();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SetFavoriteMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Content content = BrainOutServer.ContentMgr.get(msg.content, Content.class);

            if (content == null)
                return;

            if (msg.fav)
            {
                profile.addFavorite(content.getID());
            }
            else
            {
                profile.removeFavorite(content.getID());
            }

            profile.setDirty();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ConsoleCommand consoleCommand)
    {
        BrainOutServer.PostRunnable(() ->
        {
            String result = getServerController().getConsole().execute(PlayerClient.this, consoleCommand.text);

            if (result != null)
            {
                sendTCP(new ChatMsg("terminal", result, "terminal", ServerConstants.Chat.COLOR_CONSOLE, -1));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ClientSyncMsg msg)
    {
        BrainOutServer.PostRunnable(this::updateClientSync);

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ForgiveKillMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Client client = getServerController().getClients().get(msg.client);

            if (client != null)
            {
                forgive(client);
            }
        });

        return true;
    }


    @SuppressWarnings("unused")
    public boolean received(final PlayerAimMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (playerController != null)
            {
                playerController.setAim(msg.aim);
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PlayerStateMsg msg)
    {
        BrainOutServer.PostRunnable(() -> {
            if (playerController != null)
            {
                playerController.setState(msg.state);
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PlayerMoveMsg msg)
    {
        if (playerController != null)
        {
            if (!playerController.move(msg.x, msg.y, msg.aimX, msg.aimY, msg.moveX, msg.moveY))
            {
                UdpMessage res = playerController.generateMoveMessage(false);

                if (res == null)
                    return true;

                sendUDP(res);
            }
        }

        return true;
    }

    public void moveTo(String dimension, float x, float y)
    {
        if (playerData == null)
        {
            log("Cannot moveTo");
            return;
        }

        playerData.setPosition(x, y);

        if (!dimension.equals(playerData.getDimension()))
        {
            Map map = Map.Get(dimension);
            int newId = map.generateServerId();
            playerData.setDimension(newId, dimension);
        }

        playerController.moveTo(x, y);
    }

    @SuppressWarnings("unused")
    public boolean received(final PlaceBlockMsg msg)
    {
        received((PlayerMoveMsg) msg);

        if (playerData != null && playerController != null)
        {
            BrainOutServer.PostRunnable(() ->
            {
                if (playerData == null) return;

                PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

                InstrumentData currentInstrument = poc.getCurrentInstrument();
                if (!(currentInstrument instanceof PlaceBlockData)) return;

                Block block;

                if (currentInstrument instanceof BoxData)
                {
                    block = ((Box) ((BoxData) currentInstrument).getPlaceBlock()).getBlock();
                }
                else
                {
                    Content blockContent = BrainOut.ContentMgr.get(msg.blockObject);

                    if (!(blockContent instanceof Block)) return;

                    block = ((Block) blockContent);
                }

                final ConsumableRecord record = poc.getConsumableContainer().get(msg.recordId);

                if (record != null)
                {
                    playerController.placeBlock(block, record, msg.layer, msg.placeX, msg.placeY);
                }
            });
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final RemoveBlockMsg msg)
    {
        received((PlayerMoveMsg) msg);

        BrainOutServer.PostRunnable(() ->
        {
            if (playerData != null && playerController != null)
            {
                PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

                final ConsumableRecord record = poc.getConsumableContainer().get(msg.recordId);

                playerController.removeBlock(record, msg.layer, msg.placeX, msg.placeY);
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PleaseSendActiveMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);
            if (map == null)
            {
                return;
            }
            ActiveData activeData = map.getActives().get(msg.o);
            if (activeData != null)
            {
                sendTCP(new NewActiveDataMsg(activeData, this, getId()));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final BulletLaunchMsg msg)
    {
        if (playerController == null) return true;

        final Bullet bullet = (Bullet) BrainOut.ContentMgr.get(msg.bullet);

        BrainOutServer.PostRunnable(() ->
        {
            PlayerData pd = hasBeenAliveRecently();

            if (pd != null)
            {
                if (pd.getTeam() instanceof SpectatorTeam)
                    return;

                PlayerOwnerComponent poc = pd.getComponent(PlayerOwnerComponent.class);

                if (poc == null)
                    return;

                ConsumableRecord record = poc.getCurrentInstrumentRecord();
                if (record == null || record.getId() != msg.recordId)
                {
                    record = playerController.changeInstrument(msg.recordId);
                }

                if (Vector2.dst2(msg.x, msg.y, pd.getX(), pd.getY()) > 4.0f * 4.0f)
                {
                    return;
                }

                if (playerController != null)
                {
                    SimplePhysicsComponentData phy =
                        pd.getComponentWithSubclass(SimplePhysicsComponentData.class);

                    ServerPhysicsSyncComponentData sync =
                        pd.getComponent(ServerPhysicsSyncComponentData.class);

                    if (phy != null && sync != null)
                    {
                        sync.sync(msg.activeX, msg.activeY, pd.getAngle());
                    }
                }

                if (record != null && record.getItem() instanceof InstrumentConsumableItem)
                {
                    InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
                    InstrumentData instrumentData = ici.getInstrumentData();
                    if (instrumentData != null)
                    {
                        Instrument instrument = instrumentData.getInstrument();

                        if (playerController.launchBullet(
                            pd, bullet, msg.slot, msg.x, msg.y, msg.angles, msg.bullets, msg.random))
                        {
                            addStat(instrument.getShotsStat(), msg.bullets);
                            addStat("shots", msg.bullets);
                        }
                    }
                }
            }
        }, BrainOutServer.MAX_QUEUE_SIZE);

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(PingMsg pingMsg)
    {
        sendUDP(new PongMsg(pingMsg, 0));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(CurrentlyWatchingMsg msg)
    {
        this.currentlyWatching = msg.ownerId;

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ActivateInstrumentMsg msg)
    {
        if (playerController == null || playerData == null) return true;

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        final ConsumableRecord record = poc.getConsumableContainer().get(msg.id);

        if (record == null)
            return true;

        BrainOutServer.PostRunnable(() ->
        {
            playerController.activateInstrument(record);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ActivateItemMsg msg)
    {
        if (playerController == null || playerData == null) return true;

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return true;

        final ConsumableRecord record =
            poc.getConsumableContainer().get(msg.id);

        if (record == null)
            return true;

        BrainOutServer.PostRunnable(() ->
        {
            playerController.activateItem(record);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ActivateActiveMsg msg)
    {
        if (playerController == null || playerData == null) return true;

        BrainOutServer.PostRunnable(() ->
        {
            if (playerData == null)
                return;

            Map map = playerData.getMap();
            if (map == null)
                return;

            final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            final ActiveData activeData = map.getActiveData(msg.id);

            if (activeData == null)
                return;

            if (Vector2.dst2(playerData.getX(), playerData.getY(), activeData.getX(), activeData.getY()) > 16 * 16)
                return;

            BrainOutServer.EventMgr.sendDelayedEvent(activeData, ActivateActiveEvent.obtain(this, playerData, msg.payload));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ChangeFrequencyMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (playerData == null)
                return;

            final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

            if (poc == null)
                return;

            Walkietalkie walkietalkie = BrainOutServer.ContentMgr.get("consumable-item-walkietalkie", Walkietalkie.class);

            if (walkietalkie == null)
                return;

            ConsumableRecord record = poc.getConsumableContainer().getConsumable(walkietalkie);

            if (record == null)
                return;

            WalkietalkieConsumableItem item = (WalkietalkieConsumableItem) record.getItem();
            item.setFrequency(msg.frequency);

            ServerPlayerControllerComponentData pcc =
                    playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

            if (pcc == null)
                return;

            pcc.consumablesUpdated();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final CancelPlayerProgressMsg msg)
    {
        if (playerController == null || playerData == null) return true;

        BrainOutServer.PostRunnable(() ->
        {
            if (playerData == null)
                return;

            final ActiveProgressComponentData pc = playerData.getComponent(ActiveProgressComponentData.class);

            if (pc == null)
                return;

            pc.cancel();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ThrowableLaunchMsg msg)
    {
        if (playerController == null || playerData == null) return true;

        BrainOutServer.PostRunnable(() ->
        {
            if (playerData == null)
                return;

            final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

            if (poc == null)
                return;

            final ConsumableRecord record = poc.getConsumableContainer().get(msg.recordId);

            playerController.launchThrowable(record, msg.x, msg.y, msg.angle);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final NotSpawnMsg msg)
    {
        BrainOutServer.PostRunnable(() -> BrainOutServer.Controller.cancelSpawn(PlayerClient.this));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PromoCodeMsg msg)
    {
        BrainOutServer.PostRunnable(() -> usePromo(msg.code));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ClaimOnlineEventRewardMsg msg)
    {
        BrainOutServer.PostRunnable(() -> claimReward(msg.eventId, msg.reward));

        return true;
    }


    @SuppressWarnings("unused")
    public boolean received(final UpdateSelectionsMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (!isInitialized())
            {
                sendTCP(new ErrorMsg(ErrorMsg.Code.notInitialized));
            }

            updatedSelectionMessages = Math.min(updatedSelectionMessages + 1.0f, 16.0f);

            if (updatedSelectionMessages > 8)
            {
                return;
            }

            if (profile == null)
                return;

            boolean resetPlayer = false;

            JsonReader reader = new JsonReader();
            Json json = new Json();
            ShopCart shopCart = getShopCart();

            for (SpawnMsg.Item item : msg.items)
            {
                Content cnt = BrainOut.ContentMgr.get(item.item);

                if (cnt instanceof SlotItem)
                {
                    SlotItem slotItem = ((SlotItem) cnt);

                    SlotItem.Selection selection = slotItem.getSelection();
                    JsonValue value = reader.parse(item.data);
                    selection.read(json, value);

                    boolean existing = shopCart.selectItem(slotItem.getSlot(), selection);

                    if (cnt instanceof CustomAnimationSlotItem)
                    {
                        if (!existing)
                        {
                            resetPlayer = true;
                        }
                    }

                    selection.saveSelection(profile, selection, msg.layout);
                }
            }

            Layout layout_ = BrainOut.ContentMgr.get(msg.layout, Layout.class);

            if (layout_ != null)
            {
                shopCart.setLayout(msg.layout);
                profile.setLayout(layout_);
            }

            GameMode gameMode = BrainOutServer.Controller.getGameMode();

            Map map = Map.Get(msg.d);

            if (map != null)
            {
                ActiveData activeData = map.getActiveData(msg.spawnAt);

                if (activeData instanceof Spawnable)
                {
                    Spawnable spawnable = ((Spawnable) activeData);

                    if (gameMode != null && gameMode.canSpawn(spawnable, getTeam()) && !isSpectator())
                    {
                        PlayerClient.this.spawnAt = spawnable;
                    }
                }
            }

            if (gameMode != null)
            {
                ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());
                serverRealization.onSelectionUpdated(PlayerClient.this);
            }

            if (playerData != null && resetPlayer)
            {
                kill(false, false);
            }

        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ChangeNameMsg msg)
    {
        if (profile != null)
        {
            BrainOutServer.PostRunnable(() ->
            {
                profile.setName(msg.name);
                profile.setDirty();

                sendUserProfile();

                profile.flush();
            });
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final KickPlayerMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            switch (getRights())
            {
                case admin:
                case mod:
                case owner:
                {
                    break;
                }
                default:
                {
                    return;
                }
            }

            Client client = BrainOutServer.Controller.getClients().get(msg.clientId);

            if (client != null)
            {
                client.kick("KickPlayerMsg");
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SwitchShootModeMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (playerData == null)
                return;

            InstrumentData inst = playerData.getCurrentInstrument();
            if (!(inst instanceof WeaponData))
                return;
            String weaponId = ((WeaponData) inst).getWeapon().getID();

            ServerWeaponComponentData swc = inst.getComponent(ServerWeaponComponentData.class);
            if (swc == null)
                return;

            ServerWeaponComponentData.Slot slot = swc.getSlot(msg.slot);
            if (slot == null)
                return;

            if (slot.getWeaponProperties().getShootModes().indexOf(msg.sm, false) < 0)
                return;

            profile.setPreferableShooMode(weaponId, msg.sm);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SpawnMsg spawnMsg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            if (!isInitialized())
            {
                sendTCP(new ErrorMsg(ErrorMsg.Code.notInitialized));
            }

            Map map = Map.Get(spawnMsg.d);
            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(spawnMsg.spawnAt);

            if (!(activeData instanceof Spawnable)) return;

            Spawnable spawnable = ((Spawnable) activeData);

            GameMode gameMode = BrainOutServer.Controller.getGameMode();

            if (gameMode == null) return;

            if (!gameMode.canSpawn(spawnable, getTeam()) && !isSpectator())
            {
                cantSpawn();

                return;
            }

            shopCart.clear();

            JsonReader reader = new JsonReader();
            Json json = new Json();

            if (!isSpectator())
            {
                for (SpawnMsg.Item item : spawnMsg.items)
                {
                    Content cnt = BrainOut.ContentMgr.get(item.item);

                    if (cnt instanceof SlotItem)
                    {
                        SlotItem slotItem = ((SlotItem) cnt);

                        SlotItem.Selection selection = slotItem.getSelection();
                        JsonValue value = reader.parse(item.data);
                        selection.read(json, value);

                        shopCart.selectItem(slotItem.getSlot(), selection);
                    }
                }
            }

            shopCart.setLayout(spawnMsg.layout);

            PlayerClient.this.spawnAt = spawnable;

            if (isValidSpawn())
            {
                boolean extraWave = spawnable instanceof FlagData;
                getServerController().respawn(PlayerClient.this, extraWave);
            } else
            {
                sendInvalidSpawn();
            }
        });

        return true;
    }

    public boolean received(final MapVotedMsg mapVotedMsg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            PlayState playState = getServerController().getPlayState();
            if (playState instanceof ServerPSEndGame)
            {
                ((ServerPSEndGame) playState).mapVoted(getId(), mapVotedMsg.voteMapId);
            }
        });

        return true;
    }

    /* =================================== CLIENT RECEIVERS =================================== */

    private void mapInitialized()
    {
        log("Map initialized!");

        lastPong = System.currentTimeMillis();

        if (getState() != State.mapInitialized)
        {
            PlayState playState = BrainOutServer.Controller.getPlayState();

            if (BrainOut.R.instanceOf(PlayStateGame.class, playState))
            {
                PlayStateGame game = ((PlayStateGame) playState);
                GameMode mode = game.getMode();

                if (mode != null)
                {
                    ServerRealization serverRealization = (ServerRealization) mode.getRealization();
                    serverRealization.clientMapInitialized(this);
                }
            }
        }

        if (getTeam() != null)
        {
            teamChanged(getTeam());
            setState(State.mapInitialized);
            return;
        }

        ClientList clients = getServerController().getClients();

        Team freeTeam = null;

        if (BrainOutServer.getInstance().isClanWar() && accessTokenAccount != null)
        {
            if (isParticipatingClan())
            {
                ServerController.ClanPartyMember member =
                    BrainOutServer.Controller.getClanPartyMember(accessTokenAccount);

                if (member == null)
                {
                    freeTeam = clients.getSpectatorTeam();
                }
                else
                {
                    freeTeam = BrainOutServer.Controller.getClanWarTeam(getClanId());

                    if (freeTeam == null)
                    {
                        if (Log.ERROR) Log.error("No such clan in clanwar: " + getClanId());

                        disconnect(DisconnectReason.kicked, "No such clan in clanwar: " + getClanId());
                        return;
                    }
                }
            }
            else
            {
                freeTeam = clients.getSpectatorTeam();
            }
        }

        if (freeTeam == null)
            freeTeam = clients.getFreeTeam(this);

        if (freeTeam != null)
        {
            if (clients.setClientTeam(this, freeTeam, false))
            {
                setState(State.mapInitialized);

                BrainOutServer.Controller.getClients().ensureBots();
            }
            else
            {
                sendTCP(new ErrorMsg(ErrorMsg.Code.wrongTeam));
            }
        }

        if (BrainOutServer.Settings.getZone() != null)
        {
            BrainOutServer.Controller.updateRoomSettings();
        }
    }

    private void clientInited()
    {
        if (getState() == State.mapInitialized)
        {
            updateRemotePlayers();
            sendRemotePlayers();

            if (this.profile != null)
            {
                sendTCP(new UserProfileMsg(this.profile));
            }

            if (isReconnected())
            {
                if (isAlive())
                {
                    setState(State.spawned);
                }
            }
            else
            {

                if (BrainOutServer.PackageMgr.getDefine("chat", "enabled").equals("enabled"))
                {
                    if (!(getTeam() instanceof SpectatorTeam) && !BrainOutServer.Controller.isFreePlay())
                    {
                        sendTCPExcept(
                                new ChatMsg("{MP_SERVER}", "{MP_PLAYER_CONNECTED," + getName() + "}",
                                        "server", ServerConstants.Chat.COLOR_INFO, -1)
                        );
                    }
                }

                String greetings = BrainOutServer.Settings.getGreetings();

                GameMode gameMode = BrainOutServer.Controller.getGameMode();

                if (gameMode != null && ((ServerRealization) gameMode.getRealization()).enableLoginPopup())
                {
                    if (greetings != null && !greetings.isEmpty())
                    {
                        showPopup("{MENU_HINT_WELCOME}", BrainOutServer.Settings.getGreetings());
                    }
                }
            }
        }

        PlayState playState = BrainOutServer.Controller.getPlayState();

        if (BrainOut.R.instanceOf(PlayStateGame.class, playState))
        {
            PlayStateGame game = ((PlayStateGame) playState);
            GameMode mode = game.getMode();

            if (mode != null)
            {
                ServerRealization serverRealization = (ServerRealization) mode.getRealization();
                serverRealization.clientInitialized(this, isReconnected());

                switch (mode.getPhase())
                {
                    case none:
                    {
                        switch (mode.getID())
                        {
                            case lobby:
                            case editor:
                            case editor2:
                            case free:
                            {
                                break;
                            }
                        }
                    }
                }

                serverRealization.checkWarmUp();
            }
        }

        completelyInitialized();

        if (BrainOut.R.instanceOf(PlayStateGame.class, playState))
        {
            PlayStateGame game = ((PlayStateGame) playState);
            GameMode mode = game.getMode();

            if (mode != null)
            {
                ServerRealization serverRealization = (ServerRealization) mode.getRealization();
                serverRealization.clientCompletelyInitialized(this);
            }
        }
    }

    private void login(LoginService.AccessToken token, String account, String credential,
                       Set<String> scopes, JSONObject info)
    {
        accessToken = token;
        accessTokenAccount = account;
        accessTokenCredential = credential;

        if (scopes.contains("game_root"))
        {
            log("Is admin now.");
            setRights(PlayerRights.admin);
        }

        if (scopes.contains("game_mod"))
        {
            log("Is mod now.");
            setRights(PlayerRights.mod);
        }

        if (scopes.contains("game_editor"))
        {
            log("Is editor now.");
            setRights(PlayerRights.editor);
        }

        if (info != null)
        {
            partyId = info.optString("party_id", info.optString("party:id", null));

            if (partyId != null)
            {
                log("Joined from Party: " + partyId);
            }
            else
            {
                partyId = info.optString("multi_id", info.optString("multi:id", null));

                if (partyId != null)
                {
                    log("Joined from Multi: " + partyId);
                }
            }
        }

        DiscoveryService discoveryService = DiscoveryService.Get();

        if (discoveryService == null)
        {
            disconnect(DisconnectReason.serverError, "No discovery service");
            return;
        }

        LoginService loginService = LoginService.Get();

        if (loginService == null)
            throw new RuntimeException("No login service!");

        if (BrainOut.OnlineEnabled() && BrainOutServer.Settings.isProductionMode())
        {
            for (Client client : BrainOutServer.Controller.getClients().values())
            {
                if (client instanceof PlayerClient && client != this)
                {
                    String clientAccount = ((PlayerClient) client).getAccount();
                    if (clientAccount != null && clientAccount.equals(getAccount()))
                    {
                        client.disconnect(DisconnectReason.badPlayer, "such account already exists");
                    }
                }
            }

        }

        log("Getting profile for account " + accessTokenAccount + "...");

        ProfileService profileService = ProfileService.Get();

        if (profileService == null)
            throw new RuntimeException("Failed to aquire profile service");

        profileService.getMyProfile(accessToken,
            (service, request, result, profile) ->
        {
            switch (result)
            {
                case success:
                {
                    log("Got the profile!");

                    BrainOutServer.PostRunnable(() ->
                    {
                        setProfile(profile);

                        onlineInitialized(true);
                    });

                    break;
                }
                case notFound:
                {
                    log("Player has no profile!");

                    BrainOutServer.PostRunnable(() ->
                    {
                        newProfile();
                        onlineInitialized(true);
                    });

                    break;
                }
                default:
                {
                    log("Failed to get user profile: " + result.toString());
                    disconnect(DisconnectReason.forbidden, "Failed to get user profile: " + result.toString());
                }
            }
        });
    }

    public void setSpecial()
    {
        this.special = true;
    }

    public boolean isSpecial()
    {
        return this.special;
    }

    public boolean isBrainPassActive()
    {
        return this.bp != null && getProfile().hasItem(BrainOut.ContentMgr.get(this.bp, BattlePass.class), false);
    }

    public void onlineInitialized(boolean newPlayerClient)
    {
        Medal ambassador = BrainOutServer.ContentMgr.get("medal-embassador", Medal.class);
        Medal skinMaker = BrainOutServer.ContentMgr.get("medal-skin-maker", Medal.class);
        Medal steamMedal = BrainOutServer.ContentMgr.get("medal-steam", Medal.class);

        if (ambassador != null && skinMaker != null && steamMedal != null)
        {
            if (profile != null && (profile.hasItem(ambassador) || profile.hasItem(skinMaker) ||
                    profile.hasItem(steamMedal)))
            {
                setSpecial();
            }
        }

        if (newPlayerClient)
        {
            GameMode gameMode = getServerController().getGameMode();

            if (gameMode != null)
            {
                ModePayload payload = ((ServerRealization) gameMode.getRealization()).newPlayerPayload(this);
                setModePayload(payload);
            }

            PlayState playState = BrainOutServer.Controller.getPlayState();

            if (BrainOut.R.instanceOf(PlayStateGame.class, playState))
            {
                PlayStateGame game = ((PlayStateGame) playState);
                GameMode mode = game.getMode();

                if (mode != null)
                {
                    ServerRealization serverRealization = (ServerRealization) mode.getRealization();
                    serverRealization.newPlayerClient(this);
                }
            }
        }

        sendServerInfo();
    }

    public void updateEvents()
    {
        updateEvents(null);
    }

    public void updateEvents(Runnable complete)
    {
        log("Fetching events...");

        if (!BrainOut.OnlineEnabled())
        {
            if (complete != null)
            {
                complete.run();
            }
            return;
        }

        EventService eventService = EventService.Get();

        if (eventService == null)
        {
            log("No event service!");

            if (complete != null)
            {
                complete.run();
            }
            return;
        }

        String groupContext = isParticipatingClan() ? getClanId() : null;

        eventService.getEvents(accessToken, groupContext, 172800, (service, request, result, events) ->
        {
            BrainOutServer.PostRunnable(() ->
            {
                if (result == Request.Result.success)
                {
                    processEvents(events);
                }
                else
                {
                    log("Failed to fetch events: " + result);
                }

                if (complete != null)
                {
                    complete.run();
                }
            });
        });
    }

    private ServerEvent parseEvent(EventService.Event from)
    {
        if (from.enabled)
        {
            if (from.category.equals("battle"))
            {
                return new ServerBattlePassEvent(this, from);
            }

            return new RegularServerEvent(this, from);
        }
        else
        {
            return null;
        }
    }

    private void processEvents(EventService.EventList events)
    {
        log("Received online events: " + events.size());

        Array<Integer> removeList = new Array<>();

        for (ServerEvent event : this.events.values())
        {
            event.setKeep(false);
        }

        for (EventService.Event event : events)
        {
            int id = event.id;

            if (event.category.equals("battle"))
            {
                bp = event.data.getString("battle-pass");
            }

            String version = event.data.optString("version", null);
            if (version != null)
            {
                VersionCompare v = new VersionCompare(version);
                VersionCompare current = new VersionCompare(Version.VERSION);

                if (current.compareTo(v) < 0)
                {
                    continue;
                }
            }

            ServerEvent serverEvent = this.events.get(id);

            if (serverEvent != null)
            {
                serverEvent.parse(event);
                serverEvent.setKeep(true);
            }
            else
            {
                serverEvent = parseEvent(event);
                if (serverEvent != null)
                {
                    serverEvent.setKeep(true);
                    this.events.put(id, serverEvent);
                }
            }
        }

        removeList.clear();

        for (ServerEvent event : this.events.values())
        {
            if (!event.isKeep())
            {
                removeList.add(event.getEvent().id);
            }
        }

        for (Integer id : removeList)
        {
            this.events.remove(id);
        }

        sendTCP(new OnlineEventsInfoMsg(events.write()));
        updateRemotePlayers(this);
    }

    public void sendServerInfo()
    {
        log("Sending Server Info");

        String ownerKey = BrainOutServer.Controller.addOwnerKey(getId());

        Preset currentPreset = BrainOutServer.Controller.getCurrentPreset();

        long now = System.currentTimeMillis() / 1000L;

        sendTCP(new ServerInfo(getServerController().getPlayState(),
            getId(),
            BrainOutServer.Settings.getLevels(),
            BrainOutServer.PackageMgr.getDefines(),
            BrainOutServer.Settings.getPrices(),
            profile,
            currentPreset,
            ownerKey,
            getPartyId(),
            BrainOutServer.Controller.getClients().getMaxPlayers(),
            now));

        sendTCP(new UpdateGlobalContentIndex(BrainOutServer.Controller.getContentIndex()));
    }

    private void openContent(Content content)
    {
        if (content instanceof Case)
        {
            Case gameCase = ((Case) content);

            int amount = profile.getItems().get(gameCase.getID(), 0);

            if (amount > 0)
            {
                amount--;

                if (!gameCase.applicable(profile))
                {
                    sendTCP(new CaseOpenResultMsg(CaseOpenResultMsg.Result.notApplicable, null));
                    return;
                }

                resourceEvent(-1, "case", "case-open", gameCase.getID());
                addStat("cases-opened", 1);
                addStat(gameCase.getID() + "-opened", 1);

                CaseData caseData = gameCase.getData();
                caseData.init();

                ServerCaseComponentData sccd = caseData.getComponent(ServerCaseComponentData.class);

                if (sccd != null)
                {
                    sccd.generate(this, profile);
                }

                profile.getItems().put(gameCase.getID(), amount);

                profile.setDirty();
                profile.flush();

                sendUserProfile();

                sendTCP(new CaseOpenResultMsg(CaseOpenResultMsg.Result.success, caseData));
            }
        }
    }

    private void repairContent(Content content)
    {
        DurabilityComponent dc = content.getComponentFrom(DurabilityComponent.class);

        if (dc != null)
        {
            float durability = dc.getDurability(profile);

            if (dc.isEnoughtToFix(durability))
            {
                int amount = profile.getInt(Constants.User.GEARS, 0);

                if (amount > 0)
                {
                    resourceEvent(-1, Constants.User.GEARS, "repair", content.getID());
                    designEvent(1, "gameplay", "repair-content", content.getID());

                    addStat("weapon-repair", 1);
                    addStat("weapon-action", 1);

                    profile.setInt(Constants.User.GEARS, amount - 1);
                    dc.setDurability(profile, durability + 1);

                    String rewardId = "purchase-" + Constants.User.GEARS;
                    float a = BrainOutServer.getInstance().getSettings().getPrice(rewardId);

                    if (a != 0)
                    {
                        award(NotifyAward.techScore, a);
                        notify(NotifyAward.techScore, a, NotifyReason.purchase, NotifyMethod.message, null);
                    }

                    profile.setDirty();

                    PlayerClient.this.notify(NotifyAward.ownable, 1, NotifyReason.contentRepaired,
                            NotifyMethod.fix, new ContentND(content));

                    sendUserProfile();
                }
            }
        }
    }

    private void purchaseContent(Content content)
    {
        if (content instanceof StoreSlotItem)
        {
            StoreSlotItem storeSlotItem = ((StoreSlotItem) content);
            Shop.ShopItem shopItem = storeSlotItem.getShopItem();

            if (!profile.checkLimit(storeSlotItem.getID()))
            {
                return;
            }

            if (shopItem != null)
            {
                int need = shopItem.getAmount();
                float amount = profile.getStats().get(shopItem.getCurrency(), 0.0f);

                if (amount >= need)
                {
                    float update = amount - need;

                    resourceEvent(-need, shopItem.getCurrency(), "purchase", storeSlotItem.getID());
                    profile.getStats().put(shopItem.getCurrency(), update);

                    ServerStoreItemComponent ssi = storeSlotItem.getComponentFrom(ServerStoreItemComponent.class);

                    if (ssi != null)
                    {
                        if (ssi.purchased(this))
                        {
                            profile.acquireLimit(storeSlotItem.getID(), storeSlotItem.getLimit());

                            profile.setDirty();
                            sendUserProfile();
                        }
                    }
                }
            }

            return;
        }


        if (content instanceof OwnableContent)
        {
            OwnableContent toGet = ((OwnableContent) content);

            if (!toGet.hasItem(profile))
            {
                if (toGet.isLocked(profile))
                {
                    return;
                }

                Shop.ShopItem shopItem = toGet.getShopItem();

                if (shopItem != null)
                {
                    int need = shopItem.getAmount();

                    float amount = profile.getStats().get(shopItem.getCurrency(), 0.0f);

                    if (amount >= need)
                    {
                        String rewardId = "purchase-" + shopItem.getCurrency();
                        float a = need * BrainOutServer.getInstance().getSettings().getPrice(rewardId);

                        if (a != 0)
                        {
                            award(NotifyAward.techScore, a);
                            notify(NotifyAward.techScore, a, NotifyReason.purchase, NotifyMethod.message, null);
                        }

                        float update = amount - need;

                        resourceEvent(-need, shopItem.getCurrency(), "purchase", toGet.getID());
                        profile.getStats().put(shopItem.getCurrency(), update);

                        resourceEvent(1, "item", "ownable", toGet.getID());
                        toGet.addItem(profile, 1);

                        log("Purchased: " + toGet.getID() + " for: " + shopItem.getCurrency() + " of " +
                            shopItem.getCurrency());

                        if (toGet instanceof Upgrade)
                        {
                            addStat("weapon-upgrade", 1);
                            addStat("weapon-action", 1);
                        }

                        profile.setDirty();

                        PlayerClient.this.notify(NotifyAward.ownable, amount, NotifyReason.gotOwnable,
                                NotifyMethod.install, new ContentND(toGet));

                        sendUserProfile();
                    }
                }
            }
        }
    }

    @Override
    protected ServerPlayerControllerComponentData newPlayerController()
    {
        if (BrainOutServer.Controller.isFreePlay())
        {
            return new ServerFreePlayPlayerControllerComponentData(playerData);
        }

        return super.newPlayerController();
    }

    @Override
    protected void updatePlayerSelection(ShopCart shopCart)
    {
        if (profile != null)
        {
            profile.setLayout(shopCart.getLayout() != null ? shopCart.getLayout() : "layout-1");
        }
    }

    @Override
    protected void applySelection(ShopCart shopCart, PlayerData playerData, Slot slot,
          SlotItem.Selection selection)
    {
        SlotItem item = selection.getItem();

        boolean have = profile == null || item.hasItem(profile);

        if (have)
        {
            selection.apply(shopCart, playerData, profile, slot, selection);

            if (selection instanceof InstrumentSlotItem.InstrumentSelection)
            {
                InstrumentSlotItem.InstrumentSelection isi =
                    ((InstrumentSlotItem.InstrumentSelection) selection);

                InstrumentInfo info = isi.getInfo();

                designEvent(1, "spawn-with-instrument", info.instrument.getID());
                if (slot != null)
                {
                    designEvent(1, "spawn-with-instrument-slot", slot.getID(), info.instrument.getID());
                }

                if (info.skin != info.instrument.getDefaultSkin())
                {
                    designEvent(1, "spawn-with-skin", info.skin.getID());
                }
                for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
                {
                    designEvent(1, "spawn-with-upgrade", entry.value.getID());
                }
            }

            designEvent(1, "spawn-with", selection.getItem().getID());
        }
    }

    @Override
    protected void readyToSpawn()
    {
        sendTCP(new SpawnRequestMsg(profile));
    }

    @Override
    public void release()
    {
        if (getTeam() != null && getAccount() != null)
        {
            ClientList.HistoryRecord record = BrainOutServer.Controller.getClients().newHistoryRecord(this);

            record.team = getTeam().getID();
            record.kicked |= getDisconnectReason() == DisconnectReason.kicked;

            GameMode gameMode = getServerController().getGameMode();

            if (gameMode != null && gameMode.isAboutToEnd())
            {
                if (getServerController().isRatingEnabled(true))
                {
                    // if the rating system is enabled,
                    // remove the reserve amount from
                    float reserve = removeRating(ServerConstants.Rating.PUNISHMENT);
                    record.ratingBuffer += reserve;
                }
            }

            if (BrainOutServer.Controller.getClients().size > 3)
            {
                PlayState playState = BrainOutServer.Controller.getPlayState();

                if (playState instanceof ServerPSGame && !BrainOutServer.Controller.isFreePlay())
                {
                    ServerPSGame psGame = ((ServerPSGame) playState);

                    switch (psGame.getPhase())
                    {
                        case game:
                        case aboutToEnd:
                        {
                            if (tookPartInWarmup)
                            {
                                log("Punishing for desert!");
                                addStat("total-deserts", 1);

                                float desertStat = getStat("case-standard-reward", 0);

                                if (desertStat < 5)
                                {
                                    addStat("case-standard-reward", 1);
                                    record.desertBuffer += 1;
                                }
                            }

                            break;
                        }
                    }
                }
            }
        }

        releasePlayerMessages();

        super.release();

        // save the profile. if we are in state of reconnect, the profile is already saved
        if (getState() != State.roconnectTimeout && isInitialized())
        {
            store();
        }

        if (BrainOutServer.getInstance().hasOnlineController() && getKey() != null)
        {
            BrainOutServer.getInstance().onlineControllerLeft(getKey(), success ->
            {
                if (success)
                {
                    log("Successfully left!");
                }
                else
                {
                    log("Failed to leave a player!");
                }
            });
        }

        BrainOutServer.PostRunnable(() ->
        {
            if (playerHandlers != null)
            {
                playerHandlers.dispose();
                playerHandlers = null;
            }

            if (BrainOutServer.Settings.getZone() != null)
            {
                BrainOutServer.Controller.updateRoomSettings();
            }

            if (subscription != null)
            {
                subscription.clear();
                subscription = null;
            }

            statistics = null;
        });

        log("Released!");
    }

    @Override
    public long getPing()
    {
        return ping;
    }

    @Override
    public void store()
    {
        log("Storing user profile!");

        storeEvents();

        if (profile != null && profile.isParticipatingClan() &&
            clanParticipationProfileStats != null && clanParticipationProfileStats.size > 0 &&
            accessToken != null)
        {
            postClanParticipation();
        }

        if (profile != null && profile.isLoaded())
        {
            profile.setDirty();
            profile.flush();
        }
    }

    public void storeEvents()
    {
        for (ServerEvent event : events.values())
        {
            event.store();
        }
    }

    private void postClanParticipation()
    {
        SocialService socialService = SocialService.Get();

        if (socialService != null)
        {
            JSONObject stats = new JSONObject();

            for (ObjectMap.Entry<String, Float> entry : clanParticipationProfileStats)
            {
                JSONObject func = new JSONObject();
                func.put("@func", "++");
                func.put("@value", entry.value);

                stats.put(entry.key, func);
            }

            JSONObject update = new JSONObject();

            {
                JSONObject func = new JSONObject();
                func.put("@func", "++");
                func.put("@value", 1);

                stats.put("revision", func);
            }

            update.put("stats", stats);

            socialService.updateMyGroupParticipation(
                accessToken,
                profile.getClanId(),
                update, null, true,
                (service, request, result, updatedProfile) -> {}
            );

            socialService.updateGroupProfile(
                accessToken,
                profile.getClanId(), update,
                (service, request, result, updatedProfile) ->
                {
                    JSONObject updatedStats = updatedProfile.optJSONObject("stats");

                    if (updatedStats != null)
                    {
                        float revision = (int)(float)updatedStats.optDouble("revision", 1);

                        if (revision % 10 == 1)
                        {
                            float kills = (float)updatedStats.optDouble(Constants.Stats.KILLS, 0);
                            float deaths = (float)updatedStats.optDouble(Constants.Stats.DEATHS, 0);

                            if (kills > 1500 && deaths > 0)
                            {
                                float efficiency = kills / deaths;

                                if (efficiency > 0.25)
                                {
                                    postClanTop100Leaderboard(efficiency);
                                }
                            }
                        }
                    }

                    if (result == Request.Result.success)
                    {
                        BrainOutServer.PostRunnable(() -> clanParticipationProfileStats.clear());
                    }
                }
            );
        }
    }

    private void postClanTop100Leaderboard(float efficiency)
    {
        if (clanName == null)
            return;

        LeaderboardService leaderboardService = LeaderboardService.Get();

        if (leaderboardService == null)
            return;

        JSONObject profile = new JSONObject();

        profile.put("avatar", this.profile.getClanAvatar());

        leaderboardService.postLeaderboard(
            accessToken,
            "clans100", "desc", efficiency, clanName,
            3600, profile, this.getClanId(), (service, request, result) -> {});
    }

    @Override
    public String getName()
    {
        if (profile == null)
        {
            return "Player " + getId();
        }

        return profile.getName();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        PlayerData playerData = getPlayerData();

        if (playerData != null)
        {
            lastKnownPosition.set(playerData.getX(), playerData.getY());
            lastKnownDimension = playerData.getDimension();
        }

        statistics.update(dt);

        syncCheck(dt);

        switch (getState())
        {
            case reconnect:
            {
                reconnectTimer -= dt;

                if (reconnectTimer <= 0)
                {
                    setState(State.roconnectTimeout);
                    BrainOutServer.PostRunnable(this::reconnectTimeout);
                }

                break;
            }
        }

        if (updatedSelectionMessages > 0)
        {
            updatedSelectionMessages -= dt;
        }

        if (marketCooldown > 0)
        {
            marketCooldown -= dt;
        }

        if (isInitialized() && !isAlive() && !isSpectator())
        {
            if (!BrainOutServer.Controller.isFreePlay())
            {
                if (autoKick > 0)
                {
                    autoKick -= dt;

                    if (autoKick <= 0)
                    {
                        kick("PlayerClient: autoKick");
                    }
                }
            }
        }
        else
        {
            updateAutoKick();
        }

        if (chatLimit > 0)
        {
            chatLimit -= dt;
        }

        if (profile != null)
        {
            profile.update(dt);
        }
    }

    @Override
    public float getEfficiency()
    {
        return getLevel(Constants.User.LEVEL, 1);
    }

    @Override
    public void addScore(float score, boolean addStat)
    {
        if (BrainOutServer.IsCustom())
        {
            // nothing applies here
            return;
        }

        super.addScore(score, addStat);

        if (profile == null) return;

        if (addStat)
            addStat(ServerConstants.Online.ProfileFields.SCORE, (int)score);
    }

    @Override
    public float addStat(String stat, float amount)
    {
        if (BrainOutServer.IsCustom())
        {
            // nothing applies here
            return 0;
        }

        localStats.put(stat, localStats.get(stat, 0.0f) + amount);

        if (profile == null) return 0;

        return profile.addStat(stat, amount, true);
    }

    @Override
    public float getStat(String stat, float def)
    {
        if (profile == null) return def;

        return profile.getStats().get(stat, def);
    }

    @Override
    public float setStat(String stat, float value)
    {
        if (BrainOutServer.IsCustom())
        {
            // nothing applies here
            return 0;
        }

        if (profile == null) return 0;

        return profile.setStat(stat, value);
    }

    public void gotOwnable(OwnableContent ownableContent, String reason,
                           ClientProfile.OnwAction ownAction, int amount)
    {
        gotOwnable(ownableContent, reason, ownAction, amount, true);
    }

    public void gotOwnable(OwnableContent ownableContent, String reason,
                           ClientProfile.OnwAction ownAction, int amount, boolean notify)
    {
        if (profile == null) return;

        switch (ownAction)
        {
            case owned:
            {
                ownableContent.addItem(profile, amount);
                log("Content owned: " + ownableContent.getID() + "; Amount: " + amount);

                resourceEvent(amount, "item", reason, ownableContent.getID());

                if (ownableContent.getLockItem() == null ||
                    ownableContent.getLockItem().isNotify())
                {
                    profile.addBadge(ownableContent.getID());

                    if (notify)
                    {
                        notify(NotifyAward.ownable, amount, NotifyReason.gotOwnable,
                                NotifyMethod.message, new ContentND(ownableContent));
                    }
                }

                PostOwnComponent poc = ownableContent.getComponentFrom(PostOwnComponent.class);
                if (poc != null)
                {
                    poc.owned(this, ownableContent);
                }

                break;
            }
            case unlocked:
            {
                log("Content unlocked: " + ownableContent.getID() + "; Amount: " + amount);
                designEvent(amount, "item-unlocked", ownableContent.getID());

                if (ownableContent.getLockItem() == null ||
                    ownableContent.getLockItem().isNotify())
                {
                    profile.addBadge(ownableContent.getID());

                    if (notify)
                    {
                        notify(NotifyAward.ownable, amount, NotifyReason.unlockedOwnable,
                                NotifyMethod.message, new ContentND(ownableContent));
                    }
                }

                break;
            }
        }

        ServerOwnableComponent soc = ownableContent.getComponentFrom(ServerOwnableComponent.class);

        if (soc != null)
        {
            soc.owned(this, ownableContent);
        }

        profile.setDirty();
    }

    public void newProfile()
    {
        setProfile(null);
    }


    public void setProfile(JSONObject profile)
    {
        this.profile = new ClientProfile(this, profile, this::gotOwnable);
        this.profile.init();
    }

    public ClientProfile getProfile()
    {
        return profile;
    }

    @Override
    public int getLevel(String kind, int def)
    {
        if (profile != null)
        {
            return profile.getLevel(kind, def);
        }

        return def;
    }

    @Override
    public void setName(String name)
    {
        if (this.profile == null) return;

        this.profile.setName(name);
        this.profile.setDirty();
    }

    public void sendUserProfile()
    {
        if (profile != null)
        {
            sendTCP(new UpdateUserProfile(profile));
        }
    }

    public void sendRightsUpdated()
    {
        sendTCP(new RightsUpdatedMsg(getRights()));
    }

    public boolean isEgg()
    {
        return egg;
    }

    public void setEgg(boolean egg) {
        this.egg = egg;
    }

    @Override
    public void onDeath(Client killer, PlayerData playerData, InstrumentInfo info)
    {
        super.onDeath(killer, playerData, info);

        setEgg(false);
    }

    public void statUpdated(String stat, float newValue, float added)
    {
        if (subscription == null)
            return;

        if (subscription.getPublisher(stat) == null)
        {
            subscription.addPublisher(stat, new UnlockSubscription.Publisher()
            {
                @Override
                public float getProperty()
                {
                    return profile.getStats().get(getName(), 0.0f);
                }
            });
        }

        subscription.update(stat);

        if (added > 0)
        {
            for (ServerEvent event : events.values())
            {
                event.statAdded(stat, added);
            }
        }
    }

    @Override
    public void completelyInitialized()
    {
        super.completelyInitialized();

        GameMode gameMode = getServerController().getGameMode();

        if (gameMode == null)
            return;

        lastPong = System.currentTimeMillis();
        mapDownloading = false;

        replayOutgoingTCPMessages();

        updateAutoKick();

        if (subscription != null)
        {
            BrainOutServer.ContentMgr.queryContentGen(Achievement.class, achievement ->
            {
                ContentLockTree.LockItem lockItem = achievement.getLockItem();

                if (lockItem == null)
                    return;

                subscription.addSubscriber(lockItem.getUnlockFor(),
                    new UnlockSubscription.Subscriber(lockItem.getParam())
                {
                    @Override
                    public void complete()
                    {
                        achievementCompleted(achievement);
                    }

                    @Override
                    public void update(float value)
                    {
                        // if achievement targets are high, don't update each time

                        Long prev = statUpdateThreshold.get(achievement.getID());

                        if (prev != null && prev > System.currentTimeMillis())
                        {
                            return;
                        }

                        statUpdateThreshold.put(achievement.getID(),
                                System.currentTimeMillis() + 5000);

                        statUpdated(achievement, value);
                    }
                });
            });

            subscription.init();
        }

        updateEvents();
        updateOrders();

        ClientList.HistoryRecord record = getServerController().getClients().getHistoryFor(this);

        if (record != null)
        {
            if (record.desertBuffer > 0)
            {
                addStat("case-standard-reward", record.desertBuffer);
                record.desertBuffer = 0;
            }

            if (record.ratingBuffer > 0 && getServerController().isRatingEnabled(true))
            {
                if (gameMode != null && gameMode.isGameActive())
                {
                    // return player's rating is case of disconnect
                    addRating(record.ratingBuffer);
                    record.ratingBuffer = 0;

                    sendUserProfile();
                }
            }
        }

        if (gameMode != null && gameMode.getPhase() == GameMode.Phase.warmUp)
        {
            setTookPartInWarmup(true);
        }

        listenOnlineMessages();

        if (!(getTeam() instanceof SpectatorTeam))
        {
            giveDailyContent();
        }

        if (profile != null && profile.isParticipatingClan())
        {
            SocialService socialService = SocialService.Get();

            if (socialService != null)
            {
                socialService.getGroup(accessToken, profile.getClanId(),
                    (service, request, result, group) ->
                {
                    if (result == Request.Result.success)
                    {
                        BrainOutServer.PostRunnable(() -> setClanInfo(group));
                    }
                });
            }
        }

        timeSyncDeadline = 30.0f;
        timeSyncCheck = 0.0f;

        if (BrainOutServer.getInstance().getAutoExec() != null)
        {
            for (String command : BrainOutServer.getInstance().getAutoExec().split(";"))
            {
                getServerController().getConsole().execute(PlayerClient.this, command);
            }
        }
    }

    private void setClanInfo(SocialService.Group group)
    {
        this.clanName = group.getName();

        JSONObject asJsonObject = group.getProfile().optJSONObject("conflict");
        if (asJsonObject != null)
        {
            doCleanupConflict(group.getId());
            return;
        }

        String conflictId = group.getProfile().optString("conflict", null);

        if (conflictId != null)
        {
            cleanupConflict(conflictId, group.getId());
        }
    }

    private void cleanupConflict(String conflictId, String clanId)
    {
        GameService gameService = GameService.Get();
        LoginService loginService = LoginService.Get();
        SocialService socialService = SocialService.Get();

        if (gameService != null && loginService != null && socialService != null)
        {
            gameService.getParty(loginService.getCurrentAccessToken(), conflictId,
                (service2, request2, status12, party) ->
            {
                if (status12 != Request.Result.success)
                {
                    doCleanupConflict(clanId);
                }
            });
        }
    }

    private void doCleanupConflict(String clanId)
    {
        SocialService socialService = SocialService.Get();

        JSONObject update = new JSONObject();

        update.put("conflict", JSONObject.NULL);
        update.put("conflict-with", JSONObject.NULL);

        socialService.updateGroupProfile(
            getAccessToken(), getProfile().getClanId(), update,
            (service3, request3, result3, updatedProfile) ->
        {
            if (result3 == Request.Result.success)
            {
                log("Successfully cleaned up clan conflict " + clanId);
            }
            else
            {
                log("Can not clean up clan conflict " + clanId + ": " + result3.toString());
            }
        });
    }

    private void updateAutoKick()
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();
        if (gameMode == null)
            return;

        if (!gameMode.isAutoKickEnabled())
            return;

        autoKick = ServerConstants.Spawn.AUTO_KICK;
    }

    private void giveDailyContent()
    {
        Map map = Map.GetDefault();

        if (map == null)
            return;

        GameMode gameMode = BrainOutServer.Controller.getGameMode();
        if (gameMode == null)
            return;

        switch (gameMode.getID())
        {
            case lobby:
            case free:
                return;
        }

        long lastDailyClaim = 0;

        String dailyContainer = BrainOutServer.Settings.getDailyContainer();

        if (dailyContainer == null || dailyContainer.isEmpty())
            return;

        if (profile == null)
            return;

        if (profile.getItems().get(dailyContainer, 0) > 0)
        {
            return;
        }

        if (profile.getStats().get(Constants.User.NUCLEAR_MATERIAL, 0.0f) >=
                Constants.DailyReward.MAX_DAILY_CONTAINERS)
        {
            return;
        }

        lastDailyClaim = profile.getLastDailyClaim();

        long now = System.currentTimeMillis() / 1000L;

        if (now <= lastDailyClaim)
            return;

        OwnableContent dailyContainerContent = BrainOutServer.ContentMgr.get(
            BrainOutServer.Settings.getDailyContainer(),  OwnableContent.class);

        if (dailyContainerContent == null)
            return;

        String dailyContentId = "unlock-daily-content-drop-item";

        Item item = BrainOutServer.ContentMgr.get(dailyContentId, Item.class);

        if (item == null)
            return;

        Array<ConsumableRecord> records = new Array<>();

        records.add(new ConsumableRecord(new OwnableConsumableItem(dailyContainerContent), 1, 0));

        ActiveData activeData = map.getRandomActiveForTag(Constants.ActiveTags.SPAWNABLE);

        if (activeData == null)
            return;

        boolean foundOne = false;

        for (Map map_ : Map.All())
        {
            for (ObjectMap.Entry<Integer, ActiveData> active : map_.getActives())
            {
                ActiveData activeData_ = active.value;

                if (!activeData_.getCreator().getID().equals(dailyContentId))
                    continue;

                ActiveFilterComponentData filter = activeData_.getComponent(ActiveFilterComponentData.class);

                if (filter == null)
                    continue;

                if (filter.filters(getId()))
                {
                    foundOne = true;
                    break;
                }
            }

            if (foundOne)
                break;
        }

        if (foundOne)
            return;

        ItemData itemData = ServerMap.dropItem(map.getDimension(), item, records,
            getId(), activeData.getX(), activeData.getY(), activeData.getAngle(), 0);

        if (itemData != null)
        {
            itemData.addComponent(new ActiveFilterComponentData(owner -> owner == getId()));

            if (profile != null)
            {
                ServerPickupCallbackComponentData ci = itemData.getComponent(ServerPickupCallbackComponentData.class);

                if (ci != null)
                {
                    ci.setCallback((Client client) ->
                    {
                        long until = System.currentTimeMillis() / 1000L + 64800;

                        gotOwnable(dailyContainerContent, "pickup",
                                ClientProfile.OnwAction.owned, 1);

                        profile.setLastDailyClaim(until);
                        profile.setDirty();
                    });
                }
            }
        }
    }

    private void achievementCompleted(Achievement achievement)
    {
        sendTCP(new AchievementCompletedMsg(achievement.getID()));
    }

    private void statUpdated(Achievement achievement, float value)
    {
        sendTCP(new StatUpdatedMsg(achievement.getLockItem().getUnlockFor(), value));
    }

    private void listenOnlineMessages()
    {
        if (!BrainOut.OnlineEnabled())
            return;

        if (!BrainOutServer.Controller.isLobby() && !BrainOutServer.Controller.isFreePlay())
            return;

        if (messageSession != null)
        {
            if (messageSession.isOpen())
            {
                log("Already listening for online messages, skipping!");
                return;
            }
        }

        MessageService messageService = MessageService.Get();

        if (messageService != null && accessToken != null)
        {
            if (playerMessages == null)
            {
                playerMessages = new PlayerMessages(this);

                initPlayerMessages();
            }

            messageSession = messageService.session(accessToken, playerMessages);
        }
    }

    private void initPlayerMessages()
    {
        playerMessages.addHandler("event_tournament_result",
            (recipientClass, recipientKey, messageId, time, sender, gamespace, payload, flags) ->
        {
            if (flags == null || !flags.contains("server"))
                return true;

            JSONObject eventInfo = payload.optJSONObject("event");
            float score = ((float) payload.optDouble("score"));
            int rank = payload.optInt("rank");

            if (eventInfo != null)
            {
                onlineEventCompleteCallback(eventInfo, score, rank);
            }

            return true;
        });

        playerMessages.addHandler("resources",
            (recipientClass, recipientKey, messageId, time, sender, gamespace, payload, flags) ->
        {
            if (flags == null || !flags.contains("server"))
                return true;

            if (profile == null)
                return true;

            String currency = payload.optString("currency");

            if (currency == null)
                return true;

            int amount = payload.optInt("amount", 0);

            if (amount <= 0)
                return true;

            BrainOutServer.PostRunnable(() ->
            {
                profile.addStat(currency, amount, true);

                if (currency.equals(Constants.User.NUCLEAR_MATERIAL))
                {
                    PlayerClient.this.notify(NotifyAward.nuclearMaterial,
                            amount, NotifyReason.nuclearMaterialReceived,
                            NotifyMethod.message, null);
                }

                sendUserProfile();
                profile.setDirty();
            });

            return true;
        });

        playerMessages.addHandler("group_request_rejected",
            (recipientClass, recipientKey, messageId, time, sender, gamespace, payload, flags) ->
        {
            if (flags == null || !flags.contains("server"))
                return true;

            if (profile == null)
            {
                return true;
            }

            giveBackGroupRequestPayment();

            return false;
        });

        playerMessages.addHandler("kicked",
            (recipientClass, recipientKey, messageId, time, sender, gamespace, payload, flags) ->
        {
            if (profile == null)
            {
                return true;
            }

            if (!recipientClass.equals("user") || !recipientKey.equals(accessTokenAccount))
            {
                return true;
            }

            if (!profile.isParticipatingClan())
            {
                return true;
            }

            playerHandlers.checkGroupParticipation(profile.getClanId(), yes ->
            {
                if (!yes)
                {
                    BrainOutServer.PostRunnable(() ->
                    {
                        profile.leaveClan();
                        profile.setDirty();

                        sendUserProfile();
                    });
                }
            });

            return false;
        });

        playerMessages.addHandler("group_request_approved",
                (recipientClass, recipientKey, messageId, time, sender, gamespace, payload, flags) ->
        {
            if (flags == null || !flags.contains("server"))
                return true;

            if (profile == null)
            {
                return true;
            }

            if (profile.isParticipatingClan())
            {
                giveBackGroupRequestPayment();
                return true;
            }

            String groupId = payload.optString("group_id");
            String groupAvatar = payload.optString("avatar");

            if (groupId == null || groupAvatar == null)
            {
                return true;
            }

            profile.setClan(groupId, groupAvatar);
            sendUserProfile();

            return false;
        });

        playerMessages.addHandler("order_completed",
                (recipientClass, recipientKey, messageId, time, sender, gamespace, payload, flags) ->
        {
            String take_item = payload.optString("take_item", "");
            int take_amount = payload.optInt("take_amount", 1);

            if (take_item.equals("ru"))
            {
                addStat("market-items-sold", 1);
                addStat("market-sold-total", take_amount);
            }

            return false;
        });
    }

    private void giveBackGroupRequestPayment()
    {
        String currency = Constants.Clans.CURRENCY_JOIN_CLAN;
        float amount = BrainOutServer.Settings.getPrice("joinClan");

        if (amount > 0)
        {
            addStat(currency, amount);
            sendUserProfile();
        }
    }

    private void onlineEventCompleteCallback(JSONObject eventInfo, float score, int rank)
    {
        EventService.Event event = new EventService.Event();
        event.read(eventInfo);

        ServerEvent onlineEvent = new RegularServerEvent(this, event);

        log("Event " + event.id + " completed. " +
            "Rank: " + rank + "; " +
            "Score: " + score + "; " +
            "Rewards total: " + onlineEvent.getEvent().tournamentRewards.size + ";");

        setStat("last-completed-event", event.id);
        addStat("events-completed", 1);

        for (Event.EventTournamentReward reward : onlineEvent.getEvent().tournamentRewards)
        {
            if (reward.isMatches(rank))
            {
                ((ServerReward) reward.reward).apply(this);

                log("Got reward: " + reward);
            }
        }

        showPopup(
            "{MENU_HINT_EVENT_COMPLETED}",
                "<img>event-completed</img><br/> " +
                "<img>" + onlineEvent.getEvent().icon + "</img><br/> " +
                "<loc style=\"title-small\" padRight=\"4\">MENU_HINT_EVENT_YOU_TOOK</loc> " +
                "<text style=\"title-yellow\" padLeft=\"4\">" + rank + "</text>" +
                "<loc style=\"title-small\" padLeft=\"4\">MENU_HINT_EVENT_N_PLACE</loc>");

    }

    public void profileInited(ClientProfile profile)
    {
        /*
        ContractGroupQueue contractGroupQueue = BrainOutServer.ContentMgr.get("contracts", ContractGroupQueue.class);
        if (contractGroupQueue != null)
        {
            boolean done = false;

            for (ContractGroup group : contractGroupQueue.getQueue())
            {
                if (group.isComplete(profile))
                {
                    continue;
                }

                for (Contract task : group.getTasks())
                {
                    if (task.getLockItem().isUnlocked(profile))
                    {
                        continue;
                    }

                    // start the first contract after the last unlocked one automatically
                    task.getLockItem().startDiff(profile);

                    //resetSubscriptions(profile);
                    done = true;
                    break;
                }

                if (done)
                {
                    break;
                }
            }
        }

         */

        resetSubscriptions(profile);
    }

    private void resetSubscriptions(ClientProfile profile)
    {
        ContentLockTree lockTree = ContentLockTree.getInstance();

        if (lockTree == null)
            return;

        if (profile == null)
            return;

        Shop shop = Shop.getInstance();

        if (subscription != null)
        {
            subscription.clear();
        }

        subscription = lockTree.subscribe();

        for (ObjectMap.Entry<String, Float> entry : profile.getStats())
        {
            final String id = entry.key;
            subscription.addPublisher(id, new UnlockSubscription.Publisher()
            {
                @Override
                public float getProperty()
                {
                    return profile.getStats().get(id, 0.0f);
                }
            });
        }

        for (ObjectMap.Entry<String, Content> entry : BrainOut.ContentMgr.getItems())
        {
            Content content = entry.value;

            if (content instanceof OwnableContent)
            {
                OwnableContent ownableContent = ((OwnableContent) content);

                if (lockTree.hasLock(ownableContent))
                {
                    if (!profile.hasItem(ownableContent, false))
                    {
                        ItemDependencyComponent dep = content.getComponent(ItemDependencyComponent.class);
                        if (dep != null)
                        {
                            if (dep.satisfied(profile))
                            {
                                gotOwnable(ownableContent, "dependency", ClientProfile.OnwAction.owned, 1);
                            }
                            else
                            {
                                for (OwnableContent item : dep.getItems())
                                {
                                    if (!item.hasItem(profile))
                                    {
                                        subscription.addContentSubscriber(item, content1 ->
                                        {
                                            if (dep.satisfied(profile))
                                            {
                                                gotOwnable(ownableContent, "dependency", ClientProfile.OnwAction.owned, 1);
                                                sendUserProfile();
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }

                    if (shop.isFree(ownableContent))
                    {
                        if (!profile.hasItem(ownableContent, false) && canSubscribeOn(ownableContent, profile))
                        {
                            subscription.addContentSubscriber(ownableContent, profile::itemUnlocked);
                        }
                    }
                    else
                    {
                        if (!profile.hasItem(ownableContent, false) && ownableContent.isLocked(profile))
                        {
                            subscription.addContentSubscriber(ownableContent, profile::itemUnlocked);
                        }
                    }
                }
            }

            if (content instanceof Contract)
            {
                Contract contract = ((Contract) content);
                if (contract.getLockItem().hasDiffStarted(profile) && !contract.getLockItem().isUnlocked(profile))
                {
                    subscription.addSubscriber(contract.getLockItem().getUnlockFor(),
                        new UnlockSubscription.Subscriber(0)
                    {
                        @Override
                        public int getTarget()
                        {
                            return contract.getLockItem().getUnlockDiffValue(profile, 0) +
                                contract.getLockItem().getParam();
                        }

                        @Override
                        public void complete()
                        {
                            // contract has been completed

                            PlayerClient.this.notify(NotifyAward.ownable, 1, NotifyReason.gotOwnable,
                                NotifyMethod.message, new ContentND(contract));

                            ContractGroupQueue contracts = ContractGroupQueue.Get();

                            ContractGroup group = contract.getGroup();
                            if (group.isComplete(getProfile()))
                            {
                                int idx = contracts.getQueue().indexOf(group, true);
                                if (idx < contracts.getQueue().size - 1)
                                {
                                    ContractGroup nextGroup = contracts.getQueue().get(idx + 1);
                                    nextGroup.startFirstGroup(profile);
                                }
                            }
                            else
                            {
                                int idx = group.getTasks().indexOf(contract, true);
                                if (idx < group.getTasks().size - 1)
                                {
                                    Contract nextContract = group.getTasks().get(idx + 1);
                                    nextContract.getLockItem().startDiff(profile);
                                }
                            }

                            profile.setDirty();

                            BrainOutServer.PostRunnable(() -> resetSubscriptions(profile));
                        }
                    });
                }
            }

            if (content instanceof CustomAnimationSlotItem)
            {
                ContentProgressComponent cpc = content.getComponent(ContentProgressComponent.class);

                if (cpc != null)
                {
                    float v = profile.getStats().get(cpc.getStat(), 0.0f);

                    if (v < cpc.getValue())
                    {
                        subscription.addSubscriber(cpc.getStat(), new UnlockSubscription.Subscriber(cpc.getValue())
                        {
                            @Override
                            public void complete()
                            {
                                ServerStoreItemComponent ssi = content.getComponentFrom(ServerStoreItemComponent.class);

                                if (ssi != null)
                                {
                                    ssi.purchased(PlayerClient.this);
                                }
                            }
                        });
                    }
                }
            }

            if (content instanceof Weapon)
            {
                Weapon weapon = ((Weapon) content);
                final String weaponId = weapon.getID();
                String skillStat = weapon.getSkillStat();
                float skillLevel = profile.getStats().get(skillStat, 0.0f);

                int level = 1;

                for (Weapon.Skill skill : weapon.getSkills())
                {
                    final int nextLevel = level;

                    if (skillLevel < level)
                    {
                        subscription.addSubscriber(weapon.getKillsStat(), new UnlockSubscription.Subscriber(skill.getKills())
                        {
                            @Override
                            public void complete()
                            {
                                Weapon w = BrainOutServer.ContentMgr.get(weaponId, Weapon.class);
                                if (w == null)
                                    return;

                                designEvent(1, "weapon-skills", w.getID(), "level-" + nextLevel);

                                if (w.getDefaultSkin() != null)
                                {
                                    PlayerClient.this.notify(NotifyAward.weaponSkills,
                                            nextLevel, NotifyReason.newSkillLevel,
                                            NotifyMethod.message, new SkillsND(w.getDefaultSkin().getID()));

                                    profile.addBadge(w.getID());
                                }

                                profile.setStatTo(skillStat, nextLevel, true);
                            }
                        });
                    }

                    level += 1;
                }
            }
        }

        subscribeLevel(Constants.User.LEVEL, ServerConstants.Online.ProfileFields.SCORE);
        subscribeLevel(Constants.User.TECH_LEVEL, ServerConstants.Online.ProfileFields.TECH_SCORE);
    }

    private boolean canSubscribeOn(OwnableContent ownableContent, ClientProfile profile)
    {
        ContentLockTree.LockItem lockItem = ownableContent.getLockItem();

        if (lockItem == null)
            return false;

        return !lockItem.hasDiff() || lockItem.hasDiffStarted(profile);
    }

    private void subscribeLevel(String kind, String scoreField)
    {
        Levels levels = BrainOutServer.Controller.getLevels(kind);

        int playerLevel = getLevel(kind, 1);

        for (Levels.Level level : levels.getLevels())
        {
            if (playerLevel <= level.number && level.hasNextLevel())
            {
                subscription.addSubscriber(scoreField,
                new UnlockSubscription.Subscriber(level.score)
                {
                    @Override
                    public void complete()
                    {
                        final Levels.Level next = level.getNextLevel();

                        if (next != null)
                        {
                            designEvent(1, "level-complete", kind, "level-" + next.number);

                            if (next.number % 5 == 0)
                            {
                                progressionEvent(1, "Complete", "level", kind, "level-" + next.number);
                            }

                            if (level.skillpoints > 0)
                            {
                                resourceEvent(level.skillpoints, Constants.User.SKILLPOINTS,
                                    "new-level", kind);
                                profile.addStat(Constants.User.SKILLPOINTS, level.skillpoints, true);
                            }
                            if (level.gears > 0)
                            {
                                resourceEvent(level.gears, Constants.User.GEARS,
                                    "new-level", kind);
                                profile.addStat(Constants.User.GEARS, level.gears, true);
                            }

                            profile.setStatTo(kind, next.number, true);

                            PlayerClient.this.notify(NotifyAward.level, next.number, NotifyReason.levelUp,
                                NotifyMethod.message, new LevelND(kind), true);

                            if (next.skillpoints > 0)
                            {
                                PlayerClient.this.notify(NotifyAward.skillpoints,
                                        next.skillpoints, NotifyReason.skillPointsEarned,
                                        NotifyMethod.message, null);
                            }

                            if (next.gears > 0)
                            {
                                PlayerClient.this.notify(NotifyAward.gears,
                                        next.gears, NotifyReason.gearsEarned,
                                        NotifyMethod.message, null);
                            }

                            profile.setDirty(true);
                        }
                    }
                });
            }
        }
    }

    public LoginService.AccessToken getAccessToken()
    {
        return accessToken;
    }

    public String getAccessTokenAccount()
    {
        return accessTokenAccount;
    }

    public String getAccessTokenCredential()
    {
        return accessTokenCredential;
    }

    public void setConnection(Connection connection)
    {
        this.connection = connection;
    }

    public Connection getConnection()
    {
        return connection;
    }

    public int sendTCP(Object object)
    {
        if (connection == null) return -1;

        int sent;

        try
        {
            sent = connection.sendTCP(object);
        }
        catch (Exception e)
        {
            log("TCP Error: " + e.toString());
            return 0;
        }

        statistics.tcp(sent, object.getClass().getName());
        return sent;
    }

    public int sendUDP(UdpMessage object)
    {
        if (connection == null) return -1;

        int sent = connection.sendUDP(object);
        statistics.udp(sent, object.getClass().getName());
        return sent;
    }

    public int sendReliableUDP(ReliableBody object)
    {
        getServerController().getReliableManager().deliver(object, this);

        return 0;
    }

    public void sendInvalidSpawn()
    {
        sendTCP(new SimpleMsg(SimpleMsg.Code.invalidSpawn));
    }

    @Override
    public void sendConsumable()
    {
        if (playerData != null)
        {
            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            ConsumableContainer consumableContainer = poc.getConsumableContainer();
            sendConsumable(consumableContainer);
        }
    }

    public void sendConsumable(ConsumableContainer consumableContainer)
    {
        sendTCP(new ConsumablesUpdateMsg(consumableContainer));
    }

    public void sendLoadAmmoMsg(int weaponId, int magazineId, int bulletsId, int ammoCount) {
        sendTCP(new LoadAmmoMsg(weaponId, magazineId, bulletsId, ammoCount));
    }

    @Override
    public void notify(NotifyAward notifyAward, float amount, NotifyReason reason,
                       NotifyMethod method, NotifyData data, boolean priority)
    {
        if (!priority)
        {
            BrainOutServer.PostRunnable(() ->
                sendTCP(new NotifyMsg(notifyAward, amount, reason, method, data)));

            return;
        }

        sendTCP(new NotifyMsg(notifyAward, amount, reason, method, data));
    }

    @Override
    public boolean setSpectator(boolean spectator)
    {
        if (!super.setSpectator(spectator))
            return false;

        sendTCP(new SpectatorFlagMsg(spectator));
        return true;
    }

    public void updateRemotePlayers()
    {
        Array<RemoteClientsMsg.RemotePlayer> remotePlayers = new Array<>();

        for (ObjectMap.Entry<Integer, Client> entry: getServerController().getClients())
        {
            if (entry.value.isTeamSelected())
            {
                Client player = entry.value;

                remotePlayers.add(
                    new RemoteClientsMsg.RemotePlayer(
                        entry.key, player.getName(), player.getAvatar(), player.getClanAvatar(), player.getClanId(),
                        player.getTeam(), player.getRights(), player.getInfo()));
            }
        }

        sendTCP(new RemoteClientsMsg(remotePlayers));
    }


    public void updateRemotePlayers(Client player)
    {
        Array<RemoteClientsMsg.RemotePlayer> remotePlayers = new Array<>();

        remotePlayers.add(
            new RemoteClientsMsg.RemotePlayer(
            player.getId(), player.getName(), player.getAvatar(), player.getClanAvatar(), player.getClanId(),
            player.getTeam(), player.getRights(), player.getInfo()));

        sendTCP(new RemoteClientsMsg(remotePlayers));
    }

    @Override
    public JSONObject getInfo()
    {
        JSONObject info = super.getInfo();

        if (getAccessToken() != null)
        {
            info.put("account", getAccount());
            info.put("credential", getAccessTokenCredential());
        }

        if (partyId != null)
        {
            info.put("party", partyId);
        }

        if (ready)
        {
            info.put("ready", true);
        }

        if (getProfile() != null)
        {
            String badge = getProfile().getSelection(Constants.User.PROFILE_BADGE);
            if (badge != null)
            {
                info.put(Constants.User.PROFILE_BADGE, badge);
            }
        }

        if (isSpecial())
        {
            info.put("special", true);
        }

        if (isBrainPassActive())
        {
            info.put("bp", true);
        }

        return info;
    }

    @Override
    public boolean isConnected()
    {
        if (connection == null)
            return false;

        return getConnection().isConnected();
    }

    @Override
    public String getAvatar()
    {
        if (profile == null)
            return "";

        return profile.getAvatar();
    }

    @Override
    public String getClanAvatar()
    {
        if (profile == null)
            return "";

        return profile.getClanAvatar();
    }

    @Override
    public String getClanId()
    {
        if (profile == null)
            return "";

        return profile.getClanId();
    }

    @Override
    public boolean isParticipatingClan()
    {
        return profile != null && profile.isParticipatingClan();
    }

    @Override
    protected void cantSpawn()
    {
        sendTCP(new ErrorMsg(ErrorMsg.Code.cantSpawn));
        teamChanged(getTeam());
    }

    @Override
    public void disconnect(DisconnectReason reason, String message)
    {
        log("Disconnecting: " + reason + " message " + message);
        setDisconnectReason(reason);

        sendTCP(new ServerDisconnect(reason));

        try
        {
            BrainOut.Timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    if (connection != null)
                    {
                        connection.close();
                    }
                }
            }, 1000);
        }
        catch (IllegalStateException ignored)
        {
        }
    }

    @Override
    public void nextRespawnIn(float time)
    {
        sendTCP(new RespawnTimeMsg(time));
    }

    public void forgive(Client client)
    {
        if (killedBy.contains(client))
        {
            killedBy.remove(client);
            client.decFriendlyKills();

            client.forgivedBy(this);
        }
    }

    @Override
    protected void forgivedBy(Client client)
    {
        sendTCP(new ChatMsg("{MP_SERVER}", "{MP_PLAYER_FORGIVE_YOU," + client.getName() + "}", "server",
                ServerConstants.Chat.COLOR_INFO, -1));
    }

    @Override
    public void showPopup(String title, String data)
    {
        sendTCP(new PopupMsg(title, data));
    }

    @Override
    public void teamChanged(Team team)
    {
        if (team != null)
        {
            log("Team changed: " + team.getID());
        }

        sendTCP(new TeamChanged(team));
    }

    public String getAccount()
    {
        return accessTokenAccount;
    }

    private boolean usePromo(JSONObject data)
    {
        if (profile == null)
            return false;

        Promo promo = new Promo(data);
        if (promo.apply(this))
        {
            sendUserProfile();
            getProfile().setDirty();
            return true;
        }

        return false;
    }

    public void claimReward(int eventId, int rewardIndex)
    {
        if (BrainOut.OnlineEnabled())
        {
            ServerEvent event = findEvent(eventId);

            if (event != null)
            {
                event.claim(rewardIndex, success ->
                    sendTCP(new ClaimOnlineEventResultMsg(eventId, rewardIndex, success)));
            }
            else
            {
                sendTCP(new ClaimOnlineEventResultMsg(eventId, rewardIndex, false));
            }
        }
    }

    public void usePromo(String promoCode)
    {
        if (BrainOut.OnlineEnabled())
        {
            ProfileService profileService = ProfileService.Get();

            if (profileService == null)
            {
                sendTCP(new PromoCodeResultMsg(PromoCodeResultMsg.Result.error));
                return;
            }

            PromoService promoService = PromoService.Get();

            if (promoService == null)
            {
                sendTCP(new PromoCodeResultMsg(PromoCodeResultMsg.Result.error));
                return;
            }

            promoService.usePromoCode(accessToken, promoCode,
                (service, request, result, promo) ->
            {
                if (result == Request.Result.success)
                {
                    if (usePromo(promo))
                    {
                        sendTCP(new PromoCodeResultMsg(PromoCodeResultMsg.Result.success));
                    }
                    else
                    {
                        sendTCP(new PromoCodeResultMsg(PromoCodeResultMsg.Result.codeIsNotValid));
                    }
                }
                else
                {
                    sendTCP(new PromoCodeResultMsg(PromoCodeResultMsg.Result.codeIsNotValid));
                }
            });
        }
        else
        {
            sendTCP(new PromoCodeResultMsg(PromoCodeResultMsg.Result.error));
        }
    }

    public void designEvent(float value, String... keys)
    {
        //sendTCP(new AnalyticsEventMsg(AnalyticsEventMsg.Kind.design, value, keys));
    }

    public void progressionEvent(int value, String... keys)
    {
        //sendTCP(new AnalyticsEventMsg(AnalyticsEventMsg.Kind.progression, value, keys));
    }

    public void resourceEvent(int amount, String currency, String itemType, String itemId)
    {
        //sendTCP(new AnalyticsResourceEventMsg(currency, itemType, itemId, amount));
    }

    public ServerPlayerControllerComponentData getServerPlayerController()
    {
        return playerController;
    }

    public ServerEvent findEvent(int id)
    {
        return events.get(id);
    }

    public float getRating()
    {
        return getStat(Constants.Stats.RATING, 0f);
    }

    public void addRating(float rating)
    {
        addStat(Constants.Stats.RATING, rating);
    }

    public float removeRating(float rating)
    {
        float oldRating = getRating();

        if (oldRating >= rating)
        {
            float newRating = Math.max(0, oldRating - rating);
            setStat(Constants.Stats.RATING, newRating);

            return oldRating - newRating;
        }
        else
        {
            setStat(Constants.Stats.RATING, 0);
            return Math.max(0, oldRating);
        }
    }

    @Override
    public boolean onDisconnect()
    {
        releasePlayerMessages();

        if (getDisconnectReason() == DisconnectReason.connectionError)
        {
            log("Disconnected because of error, waiting for reconnect");

            GameMode gameMode = BrainOutServer.Controller.getGameMode();
            if (gameMode != null && gameMode.getRealization() instanceof ServerRealization)
            {
                ((ServerRealization) gameMode.getRealization()).onClientReconnect(this, getPlayerData());
            }

            store();

            reconnectTimer = Constants.Connection.RECONNECT_TIME_OUT;
            setState(State.reconnect);

            if (gameMode != null && gameMode.getRealization() instanceof ServerRealization)
            {
                ((ServerRealization) gameMode.getRealization()).onClientReconnecting(this, getPlayerData());
            }

            store();

            setConnection(null);

            return false;
        }

        return super.onDisconnect();
    }

    private void releasePlayerMessages()
    {
        if (playerMessages != null)
        {
            playerMessages.release();
            playerMessages = null;
        }

        if (messageSession != null)
        {
            messageSession.close();
            messageSession = null;
        }
    }

    @Override
    protected String getLogHeader()
    {
        String header = super.getLogHeader();

        if (BrainOut.OnlineEnabled())
        {
            if (accessTokenAccount != null)
            {
                header += " @" + accessTokenAccount + " ";
            }
        }

        Connection c = connection;
        if (c != null)
        {
            header += " c." + c.getID() + " ";
        }

        return header;
    }

    private void reconnectTimeout()
    {
        log("Reconnect timeout, removing client.");

        kill();

        BrainOutServer.Controller.getClients().releaseClient(this);
        BrainOutServer.Controller.checkIfIsEmpty();
    }

    public void reconnected()
    {
        this.reconnected = true;

        log("Reconnected!");

        timeSyncCheck = 0;
        timeSyncDeadline = 30.0f;

        pongCheck = 5.0f;
        lastPong = 0;
        firstPongC = 0;
        lastPongC = 0;
        firstPongNanoTime = 0;
        lastPongNanoTime = 0;

        setState(State.none);

        onlineInitialized(false);
    }

    public boolean isReconnected()
    {
        return reconnected;
    }

    public Statistics getStatistics()
    {
        return statistics;
    }

    public void sendChat(String message)
    {
        sendTCP(new ChatMsg("{MP_SERVER}", message, "server", ServerConstants.Chat.COLOR_INFO, -1));
    }

    public void sendChatImportant(String message)
    {
        sendTCP(new ChatMsg("{MP_SERVER}", message, "server", ServerConstants.Chat.COLOR_IMPORTANT, -1));
    }

    public interface NewOrderCallback
    {
        void result(boolean success, long orderId);
    }

    public interface UpdateOrderCallback
    {
        void result(boolean success, String store, long orderId, String currency, int total, String item);
    }

    public void createNewOrder(
        String store,
        String item,
        int amount,
        String currency,
        String component,
        HashMap<String, String> environment,
        final NewOrderCallback callback)
    {
        StoreService storeService = StoreService.Get();

        if (storeService == null)
        {
            callback.result(false, -1);
            return;
        }

        storeService.newOrder(
            accessToken, store,
            item, amount, currency, component, environment,
            (service, request, result, orderId) ->
        {
            BrainOutServer.PostRunnable(() ->
                callback.result(result == Request.Result.success, orderId));
        });
    }

    public void updateOrders()
    {
        if (!BrainOut.OnlineEnabled())
            return;

        StoreService storeService = StoreService.Get();

        if (storeService == null)
        {
            return;
        }

        storeService.updateOrders(accessToken,
            (service, request, result, store, responseOrderId, currency, total,
             publicPayload, privatePayload, amount, item) ->
        {
            BrainOutServer.PostRunnable(() ->
            {
                if (result == Request.Result.success)
                {
                    BrainOutServer.PostRunnable(() ->
                    {
                        log("Order " + responseOrderId + " succeeded: " +
                                store + "/" + item + " amount " + amount + " total " + total + " of " +
                                currency);

                        log(privatePayload.toString(4));

                        applyOrderContents(privatePayload, amount, item);
                    });
                }
            });
        });
    }

    public void updateOrder(long orderId, final UpdateOrderCallback callback)
    {
        StoreService storeService = StoreService.Get();

        if (storeService == null)
        {
            callback.result(false, "", orderId, "", 0, "");
            return;
        }

        storeService.updateOrder(accessToken, orderId,
            (service, request, result, store, responseOrderId, currency, total,
             publicPayload, privatePayload, amount, item) ->
        {
            BrainOutServer.PostRunnable(() ->
            {
                if (result == Request.Result.success)
                {
                    BrainOutServer.PostRunnable(() ->
                    {
                        log("Order " + orderId + " succeeded: " +
                                store + "/" + item + " amount " + amount + " total " + total + " of " +
                                currency);
                        log(privatePayload.toString());
                        applyOrderContents(privatePayload, amount, item);
                        callback.result(true, store, responseOrderId, currency, total, item);
                    });
                }
                else
                {
                    BrainOutServer.PostRunnable(() ->
                    {
                        log("Updating order " + orderId + " failed: " + result.toString());
                        callback.result(false, "", orderId, "", 0, "");
                    });
                }
            });
        });
    }

    public void applyOrderContents(JSONObject contents, int amount, String itemId)
    {
        ServerRewards reward = new ServerRewards(contents, amount);

        if (reward.apply(this))
        {
            BrainOutServer.PostRunnable(() ->
            {
                addStat(itemId + "-purchased", amount);
                BrainOutServer.Env.confirmPurchase(PlayerClient.this);
                sendUserProfile();
                getProfile().setDirty();
                getProfile().flush();
            });
        }
    }

    private void syncCheck(float dt)
    {
        if (cheater)
            return;

        pongCheck -= dt;

        if (pongCheck > 0)
        {
            return;
        }

        pongCheck = 0.5f;

        if (BrainOutServer.Controller.isLobby() || BrainOutServer.Controller.isFreePlay())
            return;

        if (!isInitialized() || isReconnecting())
            return;

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return;

        if (gameMode.getID() == GameMode.ID.editor || gameMode.getID() == GameMode.ID.editor2)
            return;

        if (!gameMode.isGameActive())
            return;

        if (lastPong != 0)
        {
            long passed = System.currentTimeMillis() - lastPong;

            if (passed > 30000)
            {
                cheater = true;
                log("Kicking because player haven't sent pong for a while");
                disconnect(DisconnectReason.kicked, "Kicking because player haven't sent pong for a while");
                return;
            }
        }

        if (lastPongC > 0 && firstPongNanoTime > 0)
        {
            long now = System.nanoTime();
            // if the last pong has been received within 1.5 seconds
            if (Math.abs(now - lastPongNanoTime) < 1500000000)
            {
                long passedHere = (now - firstPongNanoTime);
                long passedThere = (lastPongC - firstPongC);

                // and the timelines has changed more than 2 seconds
                if (passedThere - passedHere > 2000000000)
                {
                    cheater = true;
                    disconnect(DisconnectReason.kicked, "syncCheck: cheater flag triggered");
                }
            }

        }

        /*
        if (cheater)
            return;

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return;

        if (!isInitialized() || isReconnecting())
            return;

        if (timeSyncCheck > 0)
        {
            timeSyncCheck -= dt;
        }

        timeSyncDeadline -= dt;

        if (timeSyncDeadline < 0)
        {
            markAsCheater();
            return;
        }

        if (timeSyncCheck > Constants.TimeSync.PERIOD * 2.25f)
        {
            log("Marked as cheater!");

            timeSyncCheck = 0;
            markAsCheater();
        }
        */
    }

    private void updateClientSync()
    {
        if (cheater)
            return;

        if (!isInitialized() || isReconnecting())
            return;

        timeSyncDeadline = 30.0f;
        timeSyncCheck = Math.max(timeSyncCheck + Constants.TimeSync.PERIOD - 0.02f, .0f);
    }

    private void markAsCheater()
    {
        if (cheater)
            return;

        if (profile != null)
        {
            addStat("cheats-v4", 1);
        }
        else
        {
            setSpectator(true);
            kill();
        }

        cheater = true;
    }

    @Override
    public boolean isCheater()
    {
        return cheater;
    }

    public String getIP()
    {
        if (!isConnected())
            return null;

        if (getConnection().getRemoteAddressTCP() == null)
            return null;

        if (getConnection().getRemoteAddressTCP().getAddress() == null)
            return null;

        return getConnection().getRemoteAddressTCP().getAddress().getHostName();
    }

    @Override
    public boolean isSocialFriendOf(Client client)
    {
        if (!(client instanceof PlayerClient))
            return false;

        PlayerClient playerClient = ((PlayerClient) client);

        if (profile != null && profile.isParticipatingClan() &&
                playerClient.getProfile() != null && playerClient.getProfile().isParticipatingClan()
                && profile.getClanId().equals(playerClient.getProfile().getClanId()))
        {
            return true;
        }

        String credential = getAccessTokenCredential();

        if (credential == null)
            return false;

        return friends != null && friends.indexOf(credential, false) >= 0;
    }

    @Override
    public boolean spawn(Spawnable spawnAt)
    {
        boolean result = super.spawn(spawnAt);

        if (result)
        {
            updateAutoKick();
        }

        return result;
    }

    public boolean isTookPartInWarmup()
    {
        return tookPartInWarmup;
    }

    public void setTookPartInWarmup(boolean tookPartInWarmup)
    {
        if (this.tookPartInWarmup == tookPartInWarmup)
            return;

        if (tookPartInWarmup)
        {
            log("Took part in warmup!");
        }

        this.tookPartInWarmup = tookPartInWarmup;
    }

    public boolean trackKill(Client other)
    {
        int id = other.getId();

        Integer value = killRecord.get(id, null);
        if (value != null)
        {
            if (value >= ServerConstants.Farming.MAX_KILLS_IN_A_ROW)
            {
                return false;
            }
            else
            {
                killRecord.put(id, value + 1);
            }
        }
        else
        {
            if (killRecord.size >= ServerConstants.Farming.PLAYERS_TO_STORE)
            {
                Integer toRemove = null;
                int min = 99999;

                for (ObjectMap.Entry<Integer, Integer> entry : killRecord)
                {
                    if (entry.value < min)
                    {
                        toRemove = entry.key;
                        min = entry.value;
                    }
                }

                if (toRemove != null)
                {
                    killRecord.remove(toRemove);
                }
            }

            killRecord.put(id, 1);
        }

        return true;
    }

    public MessageService.MessageSession getMessageSession()
    {
        return messageSession;
    }

    public boolean sendSocialMessage(String messageType, String recipientClass, String recipientKey,
                                     String messageId, Date time, String sender, JSONObject payload, Set<String> flags)
    {
        Log.info("Got message: " + messageId);
        sendTCP(new SocialMsg(messageType, recipientClass, recipientKey, messageId, sender, payload, time));

        return true;
    }

    private void sendClanMessage(String text)
    {
        if (profile == null || messageSession == null || accessToken == null)
            return;

        if (!profile.isParticipatingClan())
            return;

        JSONObject msg = new JSONObject();
        msg.put("name", getName());
        msg.put("text", text);

        if (accessTokenCredential != null)
            msg.put("credential", accessTokenCredential);

        messageSession.sendMessage("social-group", profile.getClanId(), "clan_chat", msg);
    }

    public void onMessagesOpen()
    {
        MessageService messageService = MessageService.Get();

        if (messageService == null || accessToken == null)
            return;

        ArrayList<MessageService.Message> messages = new ArrayList<>();
        ArrayList<MessageService.LastReadMessage> lastReadMessages = new ArrayList<>();

        messageService.getMessages(messages, lastReadMessages, 0, 200, accessToken,
            (replyTo, status) ->
        {
            if (status == Request.Result.success)
            {
                SocialMsg[] toSend = new SocialMsg[messages.size()];
                SocialBatchMsg.LastReadMsg[] lastReadMsgs = new SocialBatchMsg.LastReadMsg[lastReadMessages.size()];

                for (int i = 0; i < messages.size(); i++)
                {
                    MessageService.Message msg = messages.get(i);
                    toSend[i] = new SocialMsg(msg.type, msg.recipientClass, msg.recipient,
                        msg.uuid, msg.sender, msg.payload, msg.time);
                }

                for (int i = 0; i < lastReadMessages.size(); i++)
                {
                    MessageService.LastReadMessage msg = lastReadMessages.get(i);

                    lastReadMsgs[i] = new SocialBatchMsg.LastReadMsg(msg.recipientClass, msg.recipient,
                        msg.uuid, msg.time);
                }

                sendTCP(new SocialBatchMsg(toSend, lastReadMsgs));
            }
            else
            {
                if (Log.ERROR) Log.error("Failed to receive user messages: " + status.toString());
            }
        });
    }

    public void socialMessageDeleted(String messageId, String sender)
    {
        sendTCP(new SocialDeletedMsg(messageId, sender));
    }

    public void socialMessageUpdated(String messageId, String sender, JSONObject payload)
    {
        sendTCP(new SocialUpdatedMsg(messageId, sender, payload));
    }

    public float addClanStat(String stat, float value)
    {
        if (profile == null)
            return 0;

        if (!profile.isParticipatingClan())
            return 0;

        if (clanParticipationProfileStats == null)
        {
            clanParticipationProfileStats = new ObjectMap<>();
        }

        float v = clanParticipationProfileStats.get(stat, 0.0f) + value;
        clanParticipationProfileStats.put(stat, v);
        return v;
    }

    public String getClanName()
    {
        return clanName;
    }

    public String getPartyId()
    {
        return partyId;
    }

    @Override
    public void enablePlayer(boolean enable)
    {
        if (playerData == null)
            return;

        super.enablePlayer(enable);
        sendTCP(new ActivateOwnerComponentMsg(enable));
    }

    public ObjectMap<String, Float> getLocalStats()
    {
        return localStats;
    }

    public void clearLocalStatsConsumableRecords()
    {
        for (String key : localStats.keys())
        {
            if (key == null)
                continue;

            if (key.startsWith("parts-of-") || key.startsWith("blueprint-"))
            {
                localStats.remove(key);
            }
        }
    }

    public boolean isAllowDrop()
    {
        return allowDrop;
    }

    public void setAllowDrop(boolean allowDrop)
    {
        this.allowDrop = allowDrop;
    }

    public void setReady(boolean ready)
    {
        this.ready = ready;
    }

    public boolean isReady()
    {
        return ready;
    }

    public void setMapDownloading()
    {
        this.mapDownloading = true;
        this.outgoingTCPMessages.clear();
    }

    public boolean isMapDownloading()
    {
        return mapDownloading;
    }

    public void addOutgoingTCPMessage(OutgoingTCPMessage message)
    {
        outgoingTCPMessages.addLast(message);
    }

    private void replayOutgoingTCPMessages()
    {
        if (outgoingTCPMessages.size == 0)
            return;

        for (OutgoingTCPMessage message : outgoingTCPMessages)
        {
            sendTCP(message.serialize());
        }

        log("Replayed  " + outgoingTCPMessages.size + " outgoing TCP messages.");

        outgoingTCPMessages.clear();
    }

    public boolean addMarketCooldown()
    {
        return addMarketCooldown(3);
    }

    public boolean addMarketCooldown(int a)
    {
        if (marketCooldown > 15)
        {
            return true;
        }

        marketCooldown += a;
        return false;
    }

    public ObjectMap<Integer, ServerEvent> getOnlineEvents()
    {
        return events;
    }

    public Vector2 getLastKnownPosition()
    {
        return lastKnownPosition;
    }

    public String getLastKnownDimension()
    {
        return lastKnownDimension;
    }

    public int getCurrentlyWatching()
    {
        return currentlyWatching;
    }

    public boolean wasReleased()
    {
        return playerHandlers == null;
    }
}
