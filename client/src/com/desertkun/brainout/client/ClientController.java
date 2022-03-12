package com.desertkun.brainout.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.settings.ClientSettings;
import com.desertkun.brainout.common.ReflectionReceiver;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.common.msg.*;
import com.desertkun.brainout.common.msg.client.*;
import com.desertkun.brainout.common.msg.server.*;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.battlepass.BattlePassTaskData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.gs.LoadingState;
import com.desertkun.brainout.mode.ClientGameRealization;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.*;
import com.desertkun.brainout.client.states.*;
import com.desertkun.brainout.playstate.ClientPSGame;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateGame;
import com.desertkun.brainout.utils.VersionCompare;
import org.anthillplatform.runtime.services.BlogService;
import org.anthillplatform.runtime.services.EventService;
import org.anthillplatform.runtime.services.GameService;
import com.esotericsoftware.minlog.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class ClientController extends Controller
{
    private ControllerState state;
    private ReflectionReceiver receiver;
    private NetworkConnection serverConnection;
    private String serverLocation;
    private String currentPartyId;

    private ObjectMap<String, Levels> levels;
    private Array<ClientEvent> onlineEvents;
    private ObjectMap<String, Integer> prices;

    private String[] contentIndex;
    private ObjectMap<String, Integer> contentIndexMap;

    private ObjectMap<Integer, RemoteClient> remoteClients;
    private UserProfile userProfile;
    private DisconnectReason disconnectReason;
    private ClientSettings settings;
    private int myId;
    private int serverHttpPort;
    private String lastServerLocation;
    private int serverTcpPort;
    private int serverUdpPort;
    private String key;
    private String myRegion;
    private String myAccount;
    private Array<RegionWrapper> regions;
    private Preset preset;

    private ObjectMap<String, String> levelsNames;

    private Team team;
    private JSONObject profile;
    private Batch batch;
    private TerminalLog terminal;
    private boolean lobby;
    private float dt = 0;
    private String ownerKey;
    private int maxPlayers;
    private BlogService.BlogEntriesList blogEntries;
    private long localTimeDiff;

    private boolean screenshotMode;

    public void switchScreenshotMode()
    {
        screenshotMode = !screenshotMode;
        BrainOut.EventMgr.sendDelayedEvent(GameControllerEvent.obtain(GameControllerEvent.Action.hideInterface, screenshotMode));
    }

    public static class RegionWrapper
    {
        public final GameService.Region region;

        public RegionWrapper(GameService.Region region)
        {
            this.region = region;
        }

        @Override
        public String toString()
        {
            if (region.settings != null)
            {
                String name = region.settings.optString("name", null);
                if (name != null)
                {
                    return L.get(name);
                }
            }

            return region.toString();
        }
    }

    public ClientController(BrainOut brainOut, ClientSettings settings)
    {
        super(brainOut);

        this.contentIndex = null;
        this.contentIndexMap = new ObjectMap<>();

        this.state = null;
        this.remoteClients = new ObjectMap<>();
        this.prices = new ObjectMap<>();
        this.receiver = new ReflectionReceiver();
        this.levels = null;
        this.userProfile = new UserProfile();
        this.disconnectReason = DisconnectReason.connectionError;
        this.terminal = new TerminalLog();
        this.levelsNames = new ObjectMap<>();
        this.levels = new ObjectMap<>();
        this.onlineEvents = new Array<>();
        this.regions = new Array<>();

        this.myId = -1;

        this.settings = settings;

        this.screenshotMode = false;

        Log.INFO = true;
    }

    @Override
    public <T extends Content> T getContentFromIndex(int index, Class<T> clazz)
    {
        if (contentIndex == null)
        {
            return null;
        }

        if (index >= contentIndex.length)
        {
            return null;
        }
        return BrainOutClient.ContentMgr.get(contentIndex[index], clazz);
    }

    @Override
    public int getContentIndexFor(Content c)
    {
        return contentIndexMap.get(c.getID(), -1);
    }

    @Override
    public boolean isServer()
    {
        return false;
    }

    @Override
    public boolean isOnlineEnabled()
    {
        return !BrainOutClient.getInstance().offline;
    }

    /* =================================== SERVER RECEIVERS =================================== */

    public boolean received(final ErrorMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Log.error("Got error: " + msg.code.toString());
        });

        return true;
    }

    public boolean received(final UpdateGlobalContentIndex msg)
    {
        Gdx.app.postRunnable(() ->
        {
            contentIndex = msg.content;
            contentIndexMap.clear();
            int i = 0;
            for (String c : contentIndex)
            {
                contentIndexMap.put(c, i);
                i++;
            }
            if (Log.INFO) Log.info("Received new content index (" + contentIndex.length + " items)");
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(RemoveRemoteClientMsg remoteCliemtMsg)
    {
        removeRemoteClient(remoteCliemtMsg.id);

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(RemoteClientsMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            for (RemoteClientsMsg.RemotePlayer player: msg.players)
            {
                JSONObject info = null;

                try
                {
                    info = new JSONObject(player.info);
                }
                catch (Exception ignored)
                {
                    continue;
                }

                addRemoteClient(
                    player.id, player.name, player.avatar, player.clanAvatar, player.clanId,
                    player.team, player.rights, info);
            }

            BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.teamUpdated));
            BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.playersStatsUpdated));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(NewRemoteClientMsg msg)
    {
        JSONObject info = null;

        try
        {
            info = new JSONObject(msg.info);
        }
        catch (Exception ignored)
        {
            return true;
        }

        addRemoteClient(
            msg.id,
            msg.name,
            msg.avatar,
            msg.clanAvatar,
            msg.clanId,
            msg.team,
            msg.rights,
            info);

        return true;
    }

    public void removeRemoteClient(int id)
    {
        RemoteClient remoteClient = getRemoteClients().remove(id);

        if (remoteClient != null)
        {
            BrainOutClient.EventMgr.sendDelayedEvent(RemoteClientLeft.obtain(remoteClient));
        }
    }

    @SuppressWarnings("unused")
    public boolean received(final ActivateOwnerComponentMsg msg)
    {
        try
        {
            CSGame game = getState(CSGame.class);

            if (game != null)
            {
                PlayerData playerData = game.getPlayerData();
                if (playerData != null)
                {
                    PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

                    if (poc != null)
                    {
                        poc.setEnabled(msg.enable);
                    }
                }
            }
        }
        catch (Exception ignored)
        {
            //
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final OnlineEventsInfoMsg msg)
    {
        String data = msg.data;

        Gdx.app.postRunnable(() ->
        {
            if (BrainOut.OnlineEnabled())
            {
                try
                {
                    EventService.EventList events = new EventService.EventList();
                    events.read(new JSONObject(data));
                    processEvents(events);
                }
                catch (Exception ignored)
                {
                    //
                }
            }
        });

        return true;
    }
    @SuppressWarnings("unused")
    public boolean received(final BattlePassTaskProgressUpdateMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientBattlePassEvent bp = null;

            for (ClientEvent event : BrainOutClient.ClientController.getOnlineEvents())
            {
                if (!(event instanceof ClientBattlePassEvent))
                {
                    continue;
                }

                if (event.getEvent().id == msg.ev)
                {
                    bp = ((ClientBattlePassEvent) event);
                    break;
                }
            }

            if (bp == null)
                return;

            if (bp.getData().getTasks().size <= msg.idx)
                return;

            BattlePassTaskData ttd = bp.getData().getTasks().get(msg.idx);

            ttd.setCommittedProgress(msg.cm);
            ttd.setUncommittedProgress(msg.unc);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final AchievementCompletedMsg msg)
    {
        BrainOutClient.EventMgr.sendDelayedEvent(AchievementCompletedEvent.obtain(msg.achievementId));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final StatUpdatedMsg msg)
    {
        BrainOutClient.EventMgr.sendDelayedEvent(StatUpdatedEvent.obtain(msg.statId, msg.value));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final RightsUpdatedMsg msg)
    {
        RemoteClient remoteClient = getMyRemoteClient();
        if (remoteClient != null)
        {
            remoteClient.setRights(msg.rights);
        }

        BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.rightsUpdated));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final OnlineEventUpdated msg)
    {
        if (BrainOut.OnlineEnabled())
        {
            Gdx.app.postRunnable(() ->
            {
                for (ClientEvent clientEvent : getOnlineEvents())
                {
                    if (clientEvent.getEvent().id == msg.event)
                    {
                        clientEvent.getEvent().score = msg.score;
                        BrainOutClient.EventMgr.sendDelayedEvent(OnlineEventUpdatedEvent.obtain(clientEvent));
                        break;
                    }
                }
            });
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PlayStateChangedMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            setPlayState(msg.id);

            Json json = new Json();
            BrainOut.R.tag(json);

            getPlayState().read(json, new JsonReader().parse(msg.data));
            getPlayState().init(null);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PlayStateUpdatedMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            PlayState playState = getPlayState();

            if (playState.getID() != msg.id)
            {
                setPlayState(msg.id);

                Json json = new Json();
                BrainOut.R.tag(json);

                getPlayState().read(json, new JsonReader().parse(msg.data));
                getPlayState().init(null);
            }
            else
            {
                Json json = new Json();
                BrainOut.R.tag(json);

                getPlayState().read(json, new JsonReader().parse(msg.data));

                BrainOut.EventMgr.sendDelayedEvent(PlayStateUpdatedEvent.obtain());
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(ChatMsg chatMsg)
    {
        Gdx.app.postRunnable((Runnable) () ->
        {
            if (chatMsg.isTerminal())
            {
                getTerminal().add(chatMsg.text);
            }

            if (isLobby() && "server".equals(chatMsg.key))
            {
                int senderId = chatMsg.senderID;

                RemoteClient sender = BrainOutClient.ClientController.getRemoteClients().get(senderId);
                if (sender == null)
                    return;

                for (Map map : Map.All())
                {
                    ActiveData activeData = map.getActiveForTag(Constants.ActiveTags.PLAYERS,
                            activeData1 -> activeData1.getOwnerId() == senderId);

                    if (activeData != null)
                    {
                        BrainOut.EventMgr.sendEvent(activeData, ChatEvent.obtain(chatMsg));
                        break;
                    }
                }
            }

            BrainOut.EventMgr.sendEvent(ChatEvent.obtain(chatMsg));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(FreeDimensionMsg msg)
    {
        String d = msg.d;

        Gdx.app.postRunnable(() ->
        {
            Map map = Map.Get(d);

            if (map == null)
            {
                return;
            }

            if (Log.INFO) Log.info("Disposing remote map " + d);

            map.dispose();
            Map.UnregisterDimension(d);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(SocialMsg msg)
    {
        JSONObject payload;

        try
        {
            payload = new JSONObject(msg.payload);
        }
        catch (JSONException ignored)
        {
            return true;
        }

        SocialMessages messages = BrainOutClient.SocialController.getMessages();

        messages.addMessage(
            msg.messageType,
            msg.recipientClass,
            msg.recipientKey,
            msg.messageId,
            msg.sender,
            payload,
            msg.time,
            true,
            true);

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(SocialDeletedMsg msg)
    {
        SocialMessages messages = BrainOutClient.SocialController.getMessages();

        messages.deleteMessage(
            msg.messageId,
            msg.sender);

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(SocialUpdatedMsg msg)
    {
        JSONObject payload;

        try
        {
            payload = new JSONObject(msg.payload);
        }
        catch (JSONException ignored)
        {
            return true;
        }

        SocialMessages messages = BrainOutClient.SocialController.getMessages();

        messages.updateMessage(
            msg.messageId,
            msg.sender,
            payload);

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(SocialBatchMsg msg)
    {
        SocialMessages messages = BrainOutClient.SocialController.getMessages();

        messages.clearMessages();

        for (SocialBatchMsg.LastReadMsg message : msg.lastReadMessages)
        {
            messages.setLastReadMessage(message.recipientClass, message.recipientKey,
                message.time, message.messageId);
        }

        for (SocialMsg message : msg.messages)
        {
            JSONObject payload;

            try
            {
                payload = new JSONObject(message.payload);
            }
            catch (JSONException ignored)
            {
                continue;
            }

            messages.addMessage(
                message.messageType,
                message.recipientClass,
                message.recipientKey,
                message.messageId,
                message.sender,
                payload,
                message.time,
                false,
                false);
        }

        Gdx.app.postRunnable(() ->
            BrainOutClient.EventMgr.sendDelayedEvent(
                SimpleEvent.obtain(SimpleEvent.Action.socialMessagesReceived)));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(RequestSuccessMsg msg)
    {
        return BrainOutClient.SocialController.requestSuccess(msg);
    }

    @SuppressWarnings("unused")
    public boolean received(RequestErrorMsg msg)
    {
        return BrainOutClient.SocialController.requestError(msg);
    }

    @SuppressWarnings("unused")
    public boolean received(NewOrderResultMsg msg)
    {
        BrainOutClient.EventMgr.sendDelayedEvent(NewOrderResultEvent.obtain(msg.success, msg.reason));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(UpdateOrderResultMsg msg)
    {
        orderUpdated(msg.store, msg.orderId, msg.currency, msg.total, msg.item, msg.success);

        return true;
    }

    /* ======================================================================================== */

    public Color getColorOf(Team team)
    {
        if (getTeam() instanceof SpectatorTeam && team != null)
        {
            return team.getColor();
        }

        return isEnemies(team, getTeam()) ?
                ClientConstants.Menu.KillList.ENEMY_COLOR : ClientConstants.Menu.KillList.FRIEND_COLOR;
    }

    public Color getColorOf(ActiveData other)
    {
        if (getTeam() instanceof SpectatorTeam && team != null)
        {
            return team.getColor();
        }

        CSGame game = getState(CSGame.class);

        if (game == null)
            return getColorOf(other.getTeam());

        PlayState ps = getPlayState();
        if (ps instanceof PlayStateGame)
        {
            if (((PlayStateGame) ps).getMode().getRealization() instanceof ClientGameRealization)
            {
                ClientGameRealization r = ((ClientGameRealization) ((PlayStateGame) ps).getMode().getRealization());
                Color c = r.getColorOf(other);
                if (c != null)
                    return c;
            }
        }

        return isEnemies(getMyRemoteClient(), getRemoteClients().get(other.getOwnerId())) ?
            ClientConstants.Menu.KillList.ENEMY_COLOR : ClientConstants.Menu.KillList.FRIEND_COLOR;
    }

    public boolean isEnemies(RemoteClient a, RemoteClient b)
    {
        if (a == b || a == null || b == null)
        {
            return false;
        }

        PlayState ps = getPlayState();

        if (ps instanceof PlayStateGame)
        {
            PlayStateGame playStateGame = ((PlayStateGame) ps);
            return playStateGame.getMode().isEnemies(a.getTeam(), b.getTeam())
                    ||
                ((playStateGame.getMode().getRealization() instanceof ClientGameRealization) &&
                ((ClientGameRealization) playStateGame.getMode().getRealization()).isEnemies(a, b));
        }
        else
        {
            return true;
        }
    }

    public Color getColorOf(RemoteClient remoteClient)
    {
        return getColorOf(remoteClient, true, true);
    }

    public Color getColorOf(RemoteClient remoteClient, boolean includeFriends, boolean allowSpecialColors)
    {
        if (remoteClient == null)
            return ClientConstants.Menu.KillList.ENEMY_COLOR;

        if (allowSpecialColors && BrainOut.OnlineEnabled())
        {
            if (remoteClient.isBrainPass())
            {
                return ClientConstants.Menu.KillList.BRAIN_PASS_COLOR;
            }

            if (remoteClient.getRights() != null && remoteClient.getRights() != PlayerRights.none)
            {
                return ClientConstants.Menu.KillList.ADMIN_COLOR;
            }

            if (remoteClient.isSpecial())
            {
                return ClientConstants.Menu.KillList.SPECIAL_COLOR;
            }
        }

        if (getTeam() instanceof SpectatorTeam)
        {
            return getColorOf(remoteClient.getTeam());
        }

        PlayState ps = getPlayState();
        if (ps instanceof PlayStateGame)
        {
            PlayStateGame playStateGame = ((PlayStateGame) ps);
            if (playStateGame.getMode().getRealization() instanceof ClientGameRealization)
            {
                ClientGameRealization r = ((ClientGameRealization) (playStateGame.getMode().getRealization()));
                Color c = r.getColorOf(remoteClient);
                if (c != null)
                    return c;
            }
        }

        if (remoteClient.getId() == getMyId())
        {
            return ClientConstants.Menu.KillList.MY_COLOR;
        }

        if (allowSpecialColors && BrainOut.OnlineEnabled())
        {
            if (includeFriends && isMyFriend(remoteClient))
            {
                return ClientConstants.Menu.KillList.CLAN_COLOR;
            }
        }

        return getColorOf(remoteClient.getTeam());
    }

    public Team getTeam()
    {
        return team;
    }

    @SuppressWarnings("unchecked")
    public  <T extends ControllerState> T getState(Class<T> classOf)
    {
        if (classOf.equals(state.getClass()))
        {
            return (T) state;
        }

        return null;
    }

    public void addRemoteClient(int id, String name, String avatar, String clanAvatar, String clanId,
                                String teamId, PlayerRights rights, JSONObject info)
    {
        Team team = ((Team) BrainOut.ContentMgr.get(teamId));

        RemoteClient remoteClient = getRemoteClients().get(id);

        if (remoteClient == null)
        {
            remoteClient = new RemoteClient(id, name, avatar, clanAvatar, clanId, team, rights, info);
            getRemoteClients().put(id, remoteClient);

            BrainOutClient.EventMgr.sendDelayedEvent(NewRemoteClientEvent.obtain(remoteClient));
        }
        else
        {
            remoteClient.setName(name);
            remoteClient.setTeam(team);
            remoteClient.setAvatar(avatar);
            remoteClient.setClanAvatar(clanAvatar);
            remoteClient.setClanId(clanId);
            remoteClient.setRights(rights);
            remoteClient.setInfo(info);

            BrainOutClient.EventMgr.sendDelayedEvent(RemoteClientUpdatedEvent.obtain(remoteClient));
        }
    }

    public ControllerState getState()
    {
        return state;
    }

    public boolean received(Object object)
    {
        if (object instanceof ReliableUdpMessage)
        {
            ReliableUdpMessage m = ((ReliableUdpMessage) object);

            // notice we have received it
            sendUDP(new ReliableReceivedMsg(m.id));

            // and process the body
            return execute(m.body);
        }

        if (object instanceof ModeMessage)
        {
            ModeMessage modeMessage = ((ModeMessage) object);


            PlayState playState = getPlayState();
            if (playState != null)
            {
                playState.received(BrainOutClient.Network, modeMessage);
            }
        }

        return execute(object) || receiver.received(object, this);
    }

    public boolean execute(Object object)
    {
        return state != null && getState().received(object);
    }

    public void onConnect(NetworkConnection connection)
    {
        this.serverConnection = connection;
        this.serverLocation = connection.getHost();

        if (this.serverLocation == null)
        {
            disconnect(DisconnectReason.connectionError);
            return;
        }

        this.disconnectReason = DisconnectReason.connectionError;
    }

    public void disconnect(final DisconnectReason reason, long wait)
    {
        sendTCP(new ClientDisconnect(reason));

        try
        {
            Thread.sleep(wait);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        serverConnection.close();
    }

    public void disconnect(final DisconnectReason reason, final Runnable sent)
    {
        sendTCP(new ClientDisconnect(reason));

        BrainOut.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Gdx.app.postRunnable(() ->
                {
                    disconnect(reason);
                    sent.run();
                });
            }
        }, 1000);
    }

    public void onDisconnect()
    {
        if (disconnectReason == DisconnectReason.pleaseReconnect)
        {
            doConnect(-1, null);

            return;
        }

        this.serverConnection = null;

        BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.disconnect));
    }

    public void connect(String serverLocation, int tcp, int udp, int http, String key,
                        boolean lobby, int reconnect, Runnable onConnectionFailed)
    {
        this.serverHttpPort = http;
        this.serverLocation = serverLocation;
        this.lastServerLocation = serverLocation;
        this.serverTcpPort = tcp;
        this.serverUdpPort = udp;
        this.key = key;
        this.lobby = lobby;

        doConnect(reconnect, onConnectionFailed);
    }

    private void doConnect(int reconnect, Runnable onConnectionFailed)
    {
        Gdx.app.postRunnable(() ->
        {
            if (!(BrainOutClient.getInstance().topState() instanceof LoadingState))
            {
                BrainOutClient.getInstance().switchState(new LoadingState());
            }

            setState(new CSConnecting(serverLocation, serverTcpPort, serverUdpPort,
                key, reconnect, onConnectionFailed));
        });
    }

    public void reconnect(Runnable onConnectionFailed)
    {
        disconnect(DisconnectReason.reconnect);
        connect(lastServerLocation, serverTcpPort, serverUdpPort,
                serverHttpPort, key, lobby, -1, onConnectionFailed);
    }

    public void disconnect(DisconnectReason reason)
    {
        if (isConnected())
        {
            setDisconnectReason(reason);
            serverConnection.close();
        }
    }

    public void sendTCP(Object object)
    {
        if (serverConnection != null)
            serverConnection.sendTCP(object);
    }

    public void sendUDP(UdpMessage object)
    {
        if (serverConnection != null)
            serverConnection.sendUDP(object);
    }


    public void setState(ControllerState state)
    {
        BrainOut.EventMgr.sendDelayedEvent(ClientControllerEvent.obtain(this, state));

        if (this.state != null)
        {
            this.state.release();
        }

        this.state = state;

        if (this.state != null)
        {
            Log.info("Switching state to: " + state.toString());

            this.state.init();
        }
    }

    public boolean isConnected()
    {
        return serverConnection != null;
    }

    public String getServerLocation()
    {
        return serverLocation;
    }

    public int getMyId()
    {
        return myId;
    }

    public RemoteClient getMyRemoteClient()
    {
        try
        {
            return getRemoteClients().get(myId);
        }
        catch (ArrayIndexOutOfBoundsException ignored)
        {
            return null;
        }
    }

    public void setId(int id)
    {
        this.myId = id;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (state != null)
        {
            state.update(dt);
        }

        syncCheck(dt);
    }

    private void syncCheck(float dt)
    {
        if (!isConnected())
            return;

        GameMode mode = getGameMode();

        if (mode == null)
            return;

        this.dt += dt;

        if (this.dt > Constants.TimeSync.PERIOD)
        {
            sendTCP(new ClientSyncMsg());

            this.dt = 0;
        }
    }

    public ClientSettings getSettings()
    {
        return settings;
    }

    public ObjectMap<Integer, RemoteClient> getRemoteClients()
    {
        return remoteClients;
    }

    public boolean isMyFriend(RemoteClient remoteClient)
    {
        if (remoteClient == null)
            return false;

        if (remoteClient.getClanId().isEmpty())
            return false;

        if (userProfile == null)
            return false;

        if (!userProfile.isParticipatingClan())
            return false;

        return userProfile.getClanId().equals(remoteClient.getClanId());
    }

    public void updatePingInfo(ClientsInfo.PingInfo[] infos)
    {
        for (ClientsInfo.PingInfo info: infos)
        {
            RemoteClient remoteClient = remoteClients.get(info.id);

            if (remoteClient != null)
            {
                remoteClient.setPing(info.ping);
                remoteClient.setScore(info.score);
                remoteClient.setDeaths(info.deaths);
                remoteClient.setKills(info.kills);
                remoteClient.setLevel(info.level);
                remoteClient.setRights(info.rights);

                if (info.team != null)
                {
                    remoteClient.setTeam(BrainOut.ContentMgr.get(info.team, Team.class));
                }
            }
        }

        BrainOut.EventMgr.sendEvent(SimpleEvent.obtain(SimpleEvent.Action.pingUpdated));
    }

    public void render(Batch batch, RenderContext renderContext)
    {
        this.batch = batch;

        if (state != null)
        {
            state.render(batch, renderContext);
        }
    }

    public void preRender()
    {
        if (state != null)
        {
            state.preRender();
        }
    }

    public void postRender()
    {
        if (state != null)
        {
            state.postRender();
        }
    }

    public int getServerTcpPort()
    {
        return serverTcpPort;
    }

    public int getServerUdpPort()
    {
        return serverUdpPort;
    }

    public int getServerHttpPort()
    {
        return serverHttpPort;
    }

    public void clear()
    {
        //
    }

    public void setPlayState(PlayState.ID playState, String playData)
    {
        setPlayState(playState);

        Json json = new Json();
        BrainOut.R.tag(json);
        JsonReader jsonReader = new JsonReader();

        getPlayState().read(json, jsonReader.parse(playData));
        getPlayState().init(null);
    }

    public void initOnline()
    {
        initOnline(new CSPrivacyPolicy());
    }

    public void initOnline(ControllerState state)
    {
        setState(state);
    }

    public Levels getLevels(String kind)
    {
        return levels.get(kind);
    }

    public String getLevelsName(String kind)
    {
        return levelsNames.get(kind);
    }

    public ObjectMap<String, String> getLevelsNames()
    {
        return levelsNames;
    }

    public void setLevels(String kind, Levels value)
    {
        levels.put(kind, value);
    }

    public void setLevelsName(String kind, String name)
    {
        levelsNames.put(kind, name);
    }

    public UserProfile getUserProfile()
    {
        return userProfile;
    }

    public DisconnectReason getDisconnectReason()
    {
        return disconnectReason;
    }

    public void setDisconnectReason(DisconnectReason disconnectReason)
    {
        this.disconnectReason = disconnectReason;
    }

    public void sendChat(String text, String key)
    {
        ChatSendMsg.Mode mode;

        switch (key)
        {
            case "team":
            {
                mode = ChatSendMsg.Mode.teamOnly;
                break;
            }
            case "clan":
            {
                mode = ChatSendMsg.Mode.clan;
                break;
            }
            default:
            {
                mode = ChatSendMsg.Mode.everyone;
            }
        }

        sendTCP(new ChatSendMsg(text, mode));
    }

    public void setTeam(Team team)
    {
        this.team = team;
    }

    public void setProfile(JSONObject profile)
    {
        this.profile = profile;
    }

    public Batch getBatch()
    {
        return batch;
    }

    public void init()
    {
        initClientDefines();
    }

    public TerminalLog getTerminal()
    {
        return terminal;
    }

    public void setWatchingPoint(float x, float y)
    {
        BrainOutClient.ClientController.sendUDP(new WatchPointMsg(x, y));
    }

    public void setWatchingPoint(Vector2 position)
    {
        BrainOutClient.ClientController.sendUDP(new WatchPointMsg(position));
    }

    private ClientEvent parseEvent(EventService.Event from)
    {
        if ("battle".equals(from.category))
        {
            return new ClientBattlePassEvent(from);
        }

        return new RegularClientEvent(from);
    }

    private void processEvents(EventService.EventList events)
    {
        this.onlineEvents.clear();

        for (EventService.Event event : events)
        {
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

            ClientEvent newEvent = parseEvent(event);

            if (newEvent != null)
            {
                this.onlineEvents.add(newEvent);
            }
        }

        BrainOutClient.EventMgr.sendDelayedEvent(OnlineEventsUpdatedEvent.obtain(this.onlineEvents));
    }

    public Array<ClientEvent> getOnlineEvents()
    {
        return onlineEvents;
    }

    public boolean isRatingEnabled()
    {
        return BrainOutClient.PackageMgr.getDefine("rating", "disabled").equals("enabled");
    }

    public String getKey()
    {
        return key;
    }

    public NetworkConnection getServerConnection()
    {
        return serverConnection;
    }

    public void createNewOrder(
        String store,
        String item,
        int amount,
        String currency,
        String component,
        HashMap<String, String> environment)
    {
        sendTCP(new CreateNewOrderMsg(store, item, amount, currency, component, environment));
    }

    public void updateOrder(long orderId)
    {
        sendTCP(new UpdateOrderMsg(orderId));
    }

    private void orderUpdated(String store, long orderId, String currency, int total,
                              String item, boolean success)
    {
        if (success)
        {
            //Analytics.EventBusiness(currency, total, store, orderId, "store", item);
        }
    }

    public void kickPlayer(RemoteClient client)
    {
        sendTCP(new KickPlayerMsg(client.getId()));
    }

    public void setMyRegion(String myRegion)
    {
        this.myRegion = myRegion;
    }

    public String getMyRegion()
    {
        return myRegion;
    }

    public Array<RegionWrapper> getRegions()
    {
        return regions;
    }

    public void setRegions(List<GameService.Region> regions)
    {
        this.regions.clear();

        for (GameService.Region region : regions)
        {
            this.regions.add(new RegionWrapper(region));
        }
    }

    public void initClientDefines()
    {
        BrainOutClient.Env.initClientDefines(BrainOutClient.PackageMgr);
        BrainOutClient.PackageMgr.setDefine("blood", BrainOutClient.ClientSett.hasBlood() ? "enabled" : "disabled");
        BrainOutClient.PackageMgr.setDefine("language", BrainOutClient.LocalizationMgr.getCurrentLanguage());
    }

    public void setPrice(String name, int value)
    {
        this.prices.put(name, value);
    }

    public int getPrice(String name, int def)
    {
        return this.prices.get(name, def);
    }

    public boolean isLobby()
    {
        PlayState ps = getPlayState();
        return ps instanceof ClientPSGame &&
            ((ClientPSGame) ps).getMode() != null &&
            ((ClientPSGame) ps).getMode().getID() == GameMode.ID.lobby;
    }

    public boolean isFreePlay()
    {
        PlayState ps = getPlayState();
        return ps instanceof ClientPSGame &&
            ((ClientPSGame) ps).getMode() != null &&
            ((ClientPSGame) ps).getMode().getID() == GameMode.ID.free;
    }

    public boolean canSeePlayerList()
    {
        GameMode gameMode = getGameMode();

        if (gameMode == null)
        {
            return false;
        }

        if (((ClientRealization) gameMode.getRealization()).forcePlayerList())
        {
            return true;
        }

        return gameMode.canSeePlayerList();
    }

    public boolean canSeeExchangeMenu()
    {
        GameMode gameMode = getGameMode();
        return gameMode != null && gameMode.canSeeExchangeMenu();
    }

    public void openExchangeMenu(PlayerData myPlayerData)
    {
        GameMode gameMode = getGameMode();
        if (gameMode != null)
        {
            gameMode.openExchangeMenu(myPlayerData);
        }
    }

    public String getOwnerKey()
    {
        return ownerKey;
    }

    public void setOwnerKey(String ownerKey)
    {
        this.ownerKey = ownerKey;
    }

    public String getCurrentPartyId()
    {
        return currentPartyId;
    }

    public void setCurrentPartyId(String currentPartyId)
    {
        this.currentPartyId = currentPartyId;
    }

    public void setMaxPlayers(int maxPlayers)
    {
        this.maxPlayers = maxPlayers;
    }

    public int getMaxPlayers()
    {
        return maxPlayers;
    }

    public void setPreset(Preset preset)
    {
        this.preset = preset;
    }

    public Preset getPreset()
    {
        return preset;
    }

    public String getMyAccount()
    {
        if (!BrainOut.OnlineEnabled())
            return "1";

        return myAccount;
    }

    public void setMyAccount(String myAccount)
    {
        this.myAccount = myAccount;
    }

    public void setBlogEntries(BlogService.BlogEntriesList blogEntries)
    {
        this.blogEntries = blogEntries;
    }

    public BlogService.BlogEntriesList getBlogEntries()
    {
        return blogEntries;
    }

    public long getServerTime()
    {
        return (System.currentTimeMillis() / 1000L) + this.localTimeDiff;
    }

    @Override
    public long getCurrentTime()
    {
        return getServerTime();
    }

    public void setServerTime(long time)
    {
        this.localTimeDiff = time - (System.currentTimeMillis() / 1000L);
    }
}
