package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.http.ContentClient;
import com.desertkun.brainout.client.states.map.CSMapLoad;
import com.desertkun.brainout.common.msg.ItemActionMsg;
import com.desertkun.brainout.common.msg.VoiceChatMsg;
import com.desertkun.brainout.common.msg.client.*;
import com.desertkun.brainout.common.msg.server.*;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.components.WeaponSlotComponent;
import com.desertkun.brainout.components.my.MyWeaponComponent;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.BlockEffectsComponent;
import com.desertkun.brainout.content.components.InventoryMoveSoundComponent;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.content.quest.task.Task;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.ShopCart;
import com.desertkun.brainout.content.shop.SlotItem;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.*;
import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.data.interfaces.StepPointData;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeRealization;
import com.desertkun.brainout.online.*;
import com.desertkun.brainout.playstate.ClientPSGame;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateGame;
import com.desertkun.brainout.utils.Compressor;
import com.esotericsoftware.minlog.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.List;


public class CSGame extends ControllerState
{
    private Array<Team> teams;
    private Array<Map> tmp, tmp2;

    private Spawnable lastSpawnPoint;
    private ShopCart shopCart;
    private State state;
    private PlayerData playerData;
    private Json json;
    private boolean inited;
    private boolean spectator;
    private float speed;

    public enum State
    {
        // spawn
        spawningMenu,
        spawned
    }

    public CSGame(Team team, PlayerData playerData)
    {
        this.shopCart = new ShopCart();
        this.teams = new Array<>();

        this.inited = false;
        this.json = new Json();
        this.spectator = false;
        this.tmp = new Array<>();
        this.tmp2 = new Array<>();

        BrainOut.R.tag(json);

        setTeam(team);

        if (playerData != null)
        {
            onSpawned(playerData);
        }
    }

    /* =================================== SERVER RECEIVERS =================================== */

    @SuppressWarnings("unused")
    public boolean received(final ClientsInfo clientPing)
    {
        Gdx.app.postRunnable(() -> getController().updatePingInfo(clientPing.info));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(PingMsg pingMsg)
    {
        Gdx.app.postRunnable(() -> getController().sendUDP(new PongMsg(pingMsg, System.nanoTime())));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SlowmoMsg msg)
    {
        Gdx.app.postRunnable(() -> BrainOutClient.ClientController.applySlowMo(msg.slowmo));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final FreePlaySummaryMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            try
            {
                JSONObject o = new JSONObject(msg.summary);
                BrainOutClient.EventMgr.sendEvent(FreePlaySummaryEvent.obtain(o, msg.alive));
            }
            catch (JSONException ignored) {}
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final KilledByMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData by = map.getActiveData(msg.activeId);

            InstrumentInfo info = new InstrumentInfo();
            info.parse(msg.infoPart);

            BrainOut.EventMgr.sendEvent(KilledByEvent.obtain(by, info));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SpawnRequestMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            if (msg.data != null)
            {
                String decompressed = Compressor.DecompressToString(msg.data);

                if (decompressed == null)
                    return;

                JSONObject o;

                try
                {
                    o = new JSONObject(decompressed);
                }
                catch (JSONException ignored)
                {
                    return;
                }

                getController().getUserProfile().read(o);
                updateUserProfile();
            }

            beginSpawn();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final UpdateUserProfile msg)
    {
        Gdx.app.postRunnable(() ->
        {
            String decompressed = Compressor.DecompressToString(msg.data);

            if (decompressed == null)
                return;

            JSONObject o = new JSONObject(decompressed);
            getController().getUserProfile().read(o);

            updateUserProfile();

            BrainOutClient.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.userProfileUpdated));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final FreePlayRadioMsg msg)
    {
        BrainOutClient.EventMgr.sendDelayedEvent(FreePlayRadioEvent.obtain(msg.message, msg.repeat));
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final QuestTaskProgress msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Quest q = BrainOutClient.ContentMgr.get(msg.quest, Quest.class);
            if (q == null)
                return;

            Task task = q.getTasks().get(msg.task);
            if (task == null)
                return;

            task.setProgress(BrainOutClient.ClientController.getUserProfile(), msg.progress);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SpectatorFlagMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            spectator = msg.s;
            BrainOutClient.EventMgr.sendEvent(SpectatorFlagEvent.obtain(msg.s));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final SimpleMsg simpleMsg)
    {
        Gdx.app.postRunnable(() ->
        {
            switch (simpleMsg.code)
            {
                case invalidSpawn:
                {
                    invalidSpawn();

                    break;
                }

                case updateSpawn:
                {
                    updateSpawn();

                    break;
                }
            }
        });

        return true;
    }

    private void updateSpawn()
    {
        BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.updateSpawn));
    }

    private void invalidSpawn()
    {
        BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.invalidSpawn));
    }

    @SuppressWarnings("unused")
    public boolean received(PopupMsg msg)
    {
        BrainOut.EventMgr.sendDelayedEvent(PopupEvent.obtain(msg.title, msg.data));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(ModeWillFinishInMsg msg)
    {
        BrainOut.EventMgr.sendDelayedEvent(ModeWillFinishInEvent.obtain(msg.time));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final NewActiveDataMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            String data;

            if (msg.compressed)
            {
                data = new String(Compressor.Decompress(msg.data));
            }
            else
            {
                data = new String(msg.data);
            }

            ActiveData activeData = map.newActiveData(msg.id, data, true);

            if (activeData instanceof PlayerData)
            {
                int ownerId = activeData.getOwnerId();
                RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(ownerId);

                if (remoteClient != null)
                {
                    remoteClient.setInfoBoolean("dead", false);
                    BrainOutClient.EventMgr.sendDelayedEvent(
                            SimpleEvent.obtain(SimpleEvent.Action.playersStatsUpdated));
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PromoCodeResultMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            BrainOutClient.EventMgr.sendDelayedEvent(PromoCodeResultEvent.obtain(msg.result));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ClaimOnlineEventResultMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            BrainOutClient.EventMgr.sendDelayedEvent(ClaimOnlineEventResultEvent.obtain(
                msg.eventId, msg.rewardIndex, msg.success));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ItemActionMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            if (activeData != null)
            {
                BrainOutClient.EventMgr.sendDelayedEvent(activeData,
                    ItemActionEvent.obtain(msg.action));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PlayMusicMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Music music = BrainOutClient.ContentMgr.get(msg.music, Music.class);

            if (music == null)
                return;

            music.playInSoundChannel();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final UpdateActiveAnimationMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            if (activeData instanceof AnimationData)
            {
                ActiveAnimationComponentData anim = activeData.getComponent(ActiveAnimationComponentData.class);

                if (anim != null)
                {
                    for (int i = 0, t = msg.animation.length; i < t; i++)
                    {
                        anim.getState().setAnimation(i, msg.animation[i], msg.loop);
                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final WatchAnimationMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.o);

            if (activeData instanceof AnimationData)
            {
                ActiveAnimationComponentData c = activeData.getComponent(ActiveAnimationComponentData.class);

                if (c != null)
                {
                    Map.SetWatcher(c.getBoneWatcher(msg.b));

                    com.desertkun.brainout.content.effect.Effect effect =
                        BrainOutClient.ContentMgr.get(msg.eff, com.desertkun.brainout.content.effect.Effect.class);

                    ((ClientMap) map).addEffect(effect, c.getLaunchData());
                }

            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final InventoryItemMovedMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);
            Content content = BrainOutClient.ContentMgr.get(msg.content);

            if (content == null)
                return;

            if (activeData != null)
            {
                InventoryMoveSoundComponent snd = content.getComponent(InventoryMoveSoundComponent.class);

                if (snd == null)
                {
                    String effectId = (content instanceof Weapon) ? "move-weapon-snd" : "move-other-snd";

                    SoundEffect sound = BrainOutClient.ContentMgr.get(effectId, SoundEffect.class);

                    if (sound != null)
                    {
                        map.addEffect(sound, new PointLaunchData(
                            activeData.getX(), activeData.getY(), 0, activeData.getDimension()));
                    }
                }
                else
                {
                    snd.play(activeData);
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final CaseOpenResultMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            CaseData gameCaseData = null;

            switch (msg.result)
            {
                case success:
                {
                    Content content = BrainOutClient.ContentMgr.get(msg.id);

                    if (content instanceof Case)
                    {
                        Case gameCase = ((Case) content);
                        gameCaseData = gameCase.getData();

                        JsonValue value = new JsonReader().parse(msg.data);
                        gameCaseData.read(new Json(), value);
                        gameCaseData.init();
                    }

                    break;
                }
            }

            BrainOutClient.EventMgr.sendDelayedEvent(CaseOpenResultEvent.obtain(msg.result, gameCaseData));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final UpdatedActiveDataMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.id);

            if (activeData != null)
            {
                map.updateActiveData(msg.id, msg.data, true);

                BrainOutClient.EventMgr.sendEvent(activeData,
                        ActiveActionEvent.obtain(activeData, ActiveActionEvent.Action.updated));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PlayerWoundedMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            if (!(activeData instanceof PlayerData))
                return;

            PlayerData playerData = ((PlayerData) activeData);

            if (msg.wounded)
            {
                playerData.setState(Player.State.wounded);
                playerData.setWounded(true);
            }
            else
            {
                playerData.setState(Player.State.normal);
                playerData.setWounded(false);
            }

            int ownerId = playerData.getOwnerId();
            RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(ownerId);
            if (remoteClient != null)
            {
                remoteClient.setInfoBoolean("wounded", msg.wounded);

                BrainOutClient.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.playersStatsUpdated));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final UpdatedComponentMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            //Log.info(msg.id + " d " + msg.data);

            map.updateActiveDataComponent(msg.id, msg.data, msg.clazz, true);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final DeleteActiveDataMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.id);

            if (activeData instanceof PlayerData)
            {
                int ownerId = activeData.getOwnerId();
                RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(ownerId);

                if (remoteClient != null)
                {
                    remoteClient.setInfoBoolean("dead", true);
                    BrainOutClient.EventMgr.sendDelayedEvent(
                            SimpleEvent.obtain(SimpleEvent.Action.playersStatsUpdated));
                }
            }

            map.removeActiveData(msg.id, msg.ragdoll);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final RemoteUpdateInstrumentMsg msg)
    {
        ClientMap map = Map.Get(msg.d, ClientMap.class);

        if (map == null)
            return false;

        final ActiveData activeData = map.getActiveData(msg.object);

        if (activeData instanceof PlayerData)
        {
            if (Log.INFO) Log.info("Other player switching instrument: " + activeData.getOwnerId());

            Gdx.app.postRunnable(() ->
            {
                PlayerRemoteComponent prc = activeData.getComponent(PlayerRemoteComponent.class);

                if (prc != null)
                {
                    if (msg.current != null)
                    {
                        InstrumentData instrumentData;

                        try
                        {
                            instrumentData = map.newInstrumentData(msg.current);
                        }
                        catch (IllegalStateException ignored)
                        {
                            return;
                        }

                        if (instrumentData == null)
                            return;

                        prc.setCurrentInstrument(instrumentData);
                        instrumentData.init();
                    }
                    else
                    {
                        prc.setCurrentInstrument(null);
                    }

                    if (msg.hooked != null)
                    {
                        InstrumentData hookedData = map.newInstrumentData(msg.hooked);
                        if (hookedData == null) return;
                        prc.setHookedInstrument(hookedData);
                        hookedData.init();
                    }
                    else
                    {
                        prc.setHookedInstrument(null);
                    }
                }
                else
                {
                    PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

                    if (poc != null)
                    {
                        BrainOut.EventMgr.sendDelayedEvent(
                            SimpleEvent.obtain(SimpleEvent.Action.instrumentUpdated));

                        if (msg.current == null)
                        {
                            poc.setCurrentInstrument(null);
                        }
                    }
                }

            });

        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(ServerActiveChangeDimensionMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap oldMap = Map.Get(msg.o, ClientMap.class);
            ClientMap newMap = Map.Get(msg.n, ClientMap.class);

            if (newMap == null && oldMap == null)
            {
                return;
            }

            if (newMap == null)
            {
                // changed dimension into unknown map, just remove the object
                oldMap.removeActiveData(msg.object, false);
                return;
            }

            if (oldMap == null)
            {
                // we don't know that actor (probably from unknown location)
                BrainOutClient.ClientController.sendTCP(new PleaseSendActiveMsg(msg.n, msg.newObject));
                return;
            }

            ActiveData activeData = oldMap.getActiveData(msg.object);

            if (activeData != null)
            {
                activeData.setPosition(msg.x, msg.y);
                activeData.setAngle(msg.angle);
                activeData.setDimension(msg.newObject, msg.n);

                CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
                if (csGame != null && activeData == csGame.getPlayerData() &&
                    BrainOutClient.ClientController.getPlayState().getID() == PlayState.ID.game)
                {
                    GameModeRealization r = ((PlayStateGame) BrainOutClient.ClientController.getPlayState()).getMode().getRealization();
                    if (r instanceof ClientRealization)
                    {
                        r.currentPlayerDimensionChanged(activeData);
                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(ServerActiveMoveMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            if (activeData != null)
            {
                ClientPhysicsSyncComponentData sync = activeData.getComponent(ClientPhysicsSyncComponentData.class);

                if (sync != null)
                {
                    sync.sync(msg.x, msg.y, msg.angle, msg.speedX, msg.speedY);
                }
                else
                {
                    SimplePhysicsComponentData cmp = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);
                    activeData.setPosition(msg.x, msg.y);
                    if (activeData instanceof PointData)
                    {
                        activeData.setAngle(msg.angle);
                    }

                    if (cmp != null)
                    {
                        cmp.getSpeed().set(msg.speedX, msg.speedY);
                    }
                }

            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(StepMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            Block contact = BrainOutClient.ClientController.getContentFromIndex(msg.i, Block.class);
            if (contact == null)
                return;

            BlockEffectsComponent e = contact.getComponent(BlockEffectsComponent.class);
            if (e == null)
                return;

            e.getEffects().launchEffects("step", new StepPointData(msg.v, msg.p, msg.d));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(ServerPlayerMoveMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            if (activeData != null)
            {
                ClientPhysicsSyncComponentData sync = activeData.getComponent(ClientPhysicsSyncComponentData.class);

                if (sync != null && !msg.enforce)
                {
                    sync.sync(msg.x, msg.y, msg.angle, msg.speedX, msg.speedY);

                    PlayerComponentData pcd = activeData.getComponentWithSubclass(PlayerComponentData.class);

                    if (pcd != null && activeData.getOwnerId() != BrainOutClient.ClientController.getMyId())
                    {
                        pcd.getMousePosition().set(msg.aimX, msg.aimY);
                    }
                }
                else
                {
                    SimplePhysicsComponentData cmp = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);
                    activeData.setPosition(msg.x, msg.y);



                    if (activeData.getOwnerId() != BrainOutClient.ClientController.getMyId())
                    {
                        if (activeData instanceof PointData)
                        {
                            activeData.setAngle(msg.angle);
                        }
                    }

                    if (cmp != null)
                    {
                        cmp.getSpeed().set(msg.speedX, msg.speedY);
                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final UserProfileMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            String decompressed = Compressor.DecompressToString(msg.data);
            if (decompressed == null)
                return;

            JSONObject o = new JSONObject(decompressed);
            getController().getUserProfile().read(o);
            updateUserProfile();
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(OtherPlayerAimMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            if (activeData instanceof PlayerData)
            {
                PlayerData playerData = ((PlayerData) activeData);

                ClientPlayerComponent cmp = playerData.getComponent(ClientPlayerComponent.class);

                if (cmp != null)
                {
                    cmp.setAim(msg.aim);
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(OtherPlayerStateMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            if (activeData instanceof PlayerData)
            {
                PlayerData playerData = ((PlayerData) activeData);

                ClientPlayerComponent cmp = playerData.getComponent(ClientPlayerComponent.class);

                if (cmp != null)
                {
                    cmp.setState(msg.state);
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final AnalyticsResourceEventMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            //Analytics.EventResource(msg.amount, msg.currency, msg.itemType, msg.itemId);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final AnalyticsEventMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            switch (msg.kind)
            {
                case design:
                {
                    //Analytics.EventDesign(msg.value, msg.keys);
                    break;
                }
                case progression:
                {
                    //Analytics.EventProgression((int)msg.value, msg.keys);
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final LaunchEffectMsg msg)
    {
        final Effect effect = ((Effect) BrainOutClient.ContentMgr.get(msg.effect));

        if (effect == null)
            return true;

        String dimension = Map.FindDimension(msg.d);

        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            effect.getSet().launchEffects(new PointLaunchData(msg.x, msg.y, 0, dimension));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final CustomPlayerAnimationMsg msg)
    {
        ClientMap map = Map.Get(msg.d, ClientMap.class);

        if (map == null)
            return true;

        ActiveData activeData = map.getActiveData(msg.object);

        if (activeData == null)
            return true;

        Gdx.app.postRunnable(() ->
        {
            if (msg.animationName != null)
            {
                PlayerComponentData pcd = activeData.getComponent(PlayerComponentData.class);

                if (pcd != null)
                {
                    pcd.playCustomHandAnimation(msg.animationName, false);
                }
            }

            if (msg.effect != null)
            {
                final Effect effect = ((Effect) BrainOutClient.ContentMgr.get(msg.effect));

                if (effect != null)
                {
                    effect.getSet().launchEffects(new PointLaunchData(
                        activeData.getX(), activeData.getY(), 0, map.getDimension()));
                }
            }

        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(InstrumentEffectMsg msg)
    {
        ClientMap map = Map.Get(msg.d, ClientMap.class);

        if (map == null)
            return true;

        ActiveData activeData = map.getActiveData(msg.object);

        Content instrumentContent = BrainOutClient.ContentMgr.get(msg.instrumentId);

        if (instrumentContent instanceof Instrument)
        {
            Instrument instrument = (Instrument)instrumentContent;

            String effect = msg.effect;

            if (activeData instanceof PlayerData)
            {
                PlayerData playerData = ((PlayerData) activeData);

                if (playerData.getCurrentInstrument() == null)
                {
                    BrainOutClient.ClientController.sendUDP(new ActiveUpdateRequestMsg(playerData));

                    return true;
                }

                BrainOut.EventMgr.sendDelayedEvent(playerData,
                    CustomInstrumentEffectEvent.obtain(effect));

            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(OtherPlayerBulletLaunch msg)
    {
        ClientMap map = Map.Get(msg.d, ClientMap.class);

        if (map == null)
            return true;

        ActiveData activeData = map.getActiveData(msg.object);

        Content instrumentContent = BrainOutClient.ContentMgr.get(msg.instrumentId);

        if (instrumentContent instanceof Instrument)
        {
            Instrument instrument = (Instrument)instrumentContent;

            Content content = BrainOutClient.ContentMgr.get(msg.bulletId);

            if (content instanceof Bullet)
            {
                Bullet bullet = (Bullet) content;

                if (activeData != null && activeData instanceof PlayerData)
                {
                    PlayerData playerData = ((PlayerData) activeData);

                    ClientPhysicsSyncComponentData sync = activeData.getComponent(ClientPhysicsSyncComponentData.class);

                    if (sync != null)
                    {
                        sync.sync(msg.activeX, msg.activeY);
                    }

                    if (playerData.getCurrentInstrument() == null)
                    {
                        BrainOutClient.ClientController.sendUDP(new ActiveUpdateRequestMsg(playerData));

                        return true;
                    }

                    BrainOut.EventMgr.sendDelayedEvent(playerData,
                        LaunchBulletEvent.obtain(bullet, msg.ownerId,
                            msg.slot, msg.x, msg.y, msg.angles, msg.silent));

                }
            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(OtherPlayerInstrumentLaunch msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            Content instrumentContent = BrainOutClient.ContentMgr.get(msg.instrumentId);

            if (instrumentContent instanceof Instrument)
            {
                Instrument instrument = (Instrument) instrumentContent;

                if (activeData instanceof PlayerData)
                {
                    PlayerData playerData = ((PlayerData) activeData);

                    if (playerData.getCurrentInstrument() == null)
                        return;

                    // if we shoot from the same instrument
                    if (playerData.getCurrentInstrument().getInstrument() == instrument)
                    {
                        InstrumentData instrumentData = playerData.getCurrentInstrument();
                        PlaceAnimationComponentData d = instrumentData.getComponent(PlaceAnimationComponentData.class);

                        if (d != null)
                        {
                            BrainOut.EventMgr.sendEvent(playerData.getCurrentInstrument(),
                                    LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.shoot, d.getLaunchPointData()));
                        }

                        // todo: add launched for place component
                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(OtherPlayerInstrumentActionMsg msg)
    {
        received((ServerPlayerMoveMsg)msg);

        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            Content content = BrainOutClient.ContentMgr.get(msg.weaponId);

            if (content instanceof Instrument)
            {
                Instrument instrument = (Instrument) content;

                if (activeData instanceof PlayerData)
                {
                    PlayerData playerData = ((PlayerData) activeData);
                    // if we remove from the same instrument
                    if (playerData.getCurrentInstrument() != null &&
                            playerData.getCurrentInstrument().getContent() == instrument)
                    {
                        BrainOut.EventMgr.sendEvent(playerData,
                            InstrumentActionEvent.obtain(msg.action, msg.data0, msg.data1));
                    }
                    else
                    {
                        // if not, ask to refresh the instrument
                        // todo: ask to refresh instrument
                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(OtherPlayerMagazineActionMsg msg)
    {
        received((ServerPlayerMoveMsg)msg);

        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            Content content = BrainOutClient.ContentMgr.get(msg.weaponId);

            if (content instanceof Instrument)
            {
                Instrument instrument = (Instrument) content;

                if (activeData instanceof PlayerData)
                {
                    PlayerData playerData = ((PlayerData) activeData);


                    // if we remove from the same instrument
                    if (playerData.getCurrentInstrument() != null &&
                            playerData.getCurrentInstrument().getContent() == instrument)
                    {
                        if (playerData.getCurrentInstrument().getInstrument() instanceof Weapon)
                        {
                            Weapon weapon = ((Weapon) playerData.getCurrentInstrument().getInstrument());

                            switch (msg.action)
                            {
                                case loadOne:
                                {
                                    BrainOut.EventMgr.sendDelayedEvent(playerData,
                                        InstrumentActionEvent.obtain(Instrument.Action.loadMagazineRound,
                                            weapon.getPrimaryProperties().getMagazineAddRoundTime(), 1));
                                    break;
                                }
                            }
                        }

                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ConsumablesUpdateMsg upd)
    {
        if (playerData == null) return true;

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            Gdx.app.postRunnable(() -> {
                ConsumableContainer cc = poc.getConsumableContainer();

                cc.read(json, new JsonReader().parse(upd.data));

                BrainOut.EventMgr.sendEvent(SimpleEvent.obtain(SimpleEvent.Action.consumablesUpdated));
                BrainOut.EventMgr.sendEvent(SimpleEvent.obtain(SimpleEvent.Action.instrumentUpdated));
            });

        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final LoadAmmoMsg upd)
    {
        if (playerData == null) return true;

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            BrainOut.EventMgr.sendDelayedEvent(AmmoLoadedEvent.obtain(upd.weaponId, upd.magazineId, upd.bulletsId, upd.ammoCount));
        }

        return true;
    }


    @SuppressWarnings("unused")
    public boolean received(final ServerSelectInstrumentMsg upd)
    {
        if (playerData == null)
            return true;

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            Gdx.app.postRunnable(() -> {
                ConsumableContainer cc = poc.getConsumableContainer();
                poc.setCurrentInstrument(upd.instrumentId);
            });

        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ServerActiveVisibilityMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.object);

            if (activeData != null)
            {
                ClientPhysicsSyncComponentData sync = activeData.getComponent(ClientPhysicsSyncComponentData.class);

                if (sync != null)
                {
                    sync.forceSync(msg.x, msg.y, msg.angle, msg.speedX, msg.speedY);
                }
                else
                {
                    SimplePhysicsComponentData cmp = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);
                    activeData.setPosition(msg.x, msg.y);
                    if (activeData instanceof PointData)
                    {
                        activeData.setAngle(msg.angle);
                    }

                    if (cmp != null)
                    {
                        cmp.getSpeed().set(msg.speedX, msg.speedY);
                    }
                }

                activeData.setVisible(msg.v);

                if (msg.v)
                {
                    AnimationComponentData anim = activeData.getComponentWithSubclass(AnimationComponentData.class);
                    if (anim != null)
                    {
                        anim.setInvalidatedLocation();
                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final WeaponInfoMsg msg)
    {
        if (playerData == null) return true;

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            Gdx.app.postRunnable(() -> {
                InstrumentData instrumentData = poc.getInstrument(msg.weaponId);
                if (instrumentData instanceof WeaponData)
                {
                    MyWeaponComponent myWeaponComponent = instrumentData.getComponent(MyWeaponComponent.class);
                    if (myWeaponComponent != null)
                    {
                        WeaponSlotComponent slot = myWeaponComponent.getSlot(msg.slot);
                        if (slot != null)
                        {
                            if (msg.magazines != null)
                            {
                                slot.clearMagazines();

                                for (WeaponInfoMsg.MagazineInfo info : msg.magazines)
                                {
                                    slot.setMagazine(info.id, info.rounds, info.quality);
                                }
                            }

                            slot.updateInfo(msg.rounds, msg.roundsQuality, msg.ch, msg.chQuality, msg.forceReset, msg.stuckIn);
                        }
                    }
                }
            });

        }

        return true;
    }
    @SuppressWarnings("unused")
    public boolean received(final PullRequiredMsg msg)
    {
        if (playerData == null) return true;

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            Gdx.app.postRunnable(() ->
            {
                InstrumentData instrumentData = poc.getInstrument(msg.weaponId);
                if (instrumentData instanceof WeaponData)
                {
                    MyWeaponComponent myWeaponComponent = instrumentData.getComponent(MyWeaponComponent.class);
                    if (myWeaponComponent != null)
                    {
                        WeaponSlotComponent slot = myWeaponComponent.getSlot(msg.slot);

                        if (slot != null)
                        {
                            slot.pullRequired();
                        }
                    }
                }
            });

        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(BlockDestroyMsg msg)
    {
        final int x = msg.x,
                  y = msg.y,
                  layer = msg.layer;
        final String dimension = Map.FindDimension(msg.d);

        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(dimension, ClientMap.class);

            if (map == null)
                return;

            final BlockData blockData = map.getBlock(x, y, layer);

            if (blockData != null)
            {
                // empty data
                BrainOut.EventMgr.sendEvent(blockData, DestroyBlockEvent.obtain(map, x, y, layer, true));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(SetGameSpeedMsg msg)
    {
        setSpeed(msg.speed);

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(RespawnTimeMsg msg)
    {
        BrainOut.EventMgr.sendDelayedEvent(RespawnTimeEvent.obtain(msg.time));

        return true;
    }
    @SuppressWarnings("unused")
    public boolean received(final BlockAddMsg msg)
    {
        final int x = msg.x,
                  y = msg.y,
                  layer = msg.layer;

        final String dimension = Map.FindDimension(msg.d),
                     data = msg.data;

        Gdx.app.postRunnable(() ->
        {
            BlockData.CURRENT_DIMENSION = dimension;
            BlockData.CURRENT_X = x;
            BlockData.CURRENT_Y = y;
            BlockData.CURRENT_LAYER = layer;

            ClientMap map = Map.Get(dimension, ClientMap.class);

            if (map == null)
                return;

            BlockData blockData = map.newBlockData(data, false);

            if (blockData == null)
                return;

            map.setBlock(x, y, blockData, layer, true, false);

            blockData.init();

            BrainOut.EventMgr.sendEvent(blockData, LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.destroy,
                new PointLaunchData(x + 0.5f, y + 0.5f, 0, dimension)));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(UnknownActiveDataMsg msg)
    {
        ClientMap map = Map.Get(msg.d, ClientMap.class);

        if (map == null)
            return false;

        // otherPlayerPickUpMsg
        final ActiveData activeData = map.getActiveData(msg.object);

        if (activeData != null)
        {
            Gdx.app.postRunnable(() -> map.removeActive(activeData, true));
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(ActiveDamageMsg msg)
    {
        ClientMap map = Map.Get(msg.d, ClientMap.class);

        if (map == null)
            return false;

        ActiveData activeData = map.getActiveData(msg.activeData);

        if (activeData != null)
        {
            Content content = BrainOutClient.ContentMgr.get(msg.content);

            BrainOut.EventMgr.sendDelayedEvent(activeData,
                DamagedEvent.obtain(activeData, msg.newHealth,
                    msg.x, msg.y, msg.angle,
                    content, msg.kind));

            int ownerId = activeData.getOwnerId();
            RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(ownerId);

            if (remoteClient != null)
            {
                remoteClient.setInfoFloat("hp", msg.newHealth);
                BrainOutClient.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.playersStatsUpdated));
            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(HitConfirmMsg hitMsg)
    {
        BrainOut.EventMgr.sendDelayedEvent(
            HitConfirmEvent.obtain(hitMsg.collider, hitMsg.d,
                hitMsg.obj, hitMsg.x, hitMsg.y, hitMsg.dmg));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(KillMsg killMsg)
    {
        Gdx.app.postRunnable(() ->
        {
            RemoteClient killer = getController().getRemoteClients().get(killMsg.killerId);
            RemoteClient victim = getController().getRemoteClients().get(killMsg.victimId);

            Instrument instrument = BrainOutClient.ContentMgr.get(killMsg.weapon, Instrument.class);

            if (instrument != null)
            {
                Skin skin = BrainOutClient.ContentMgr.get(killMsg.weaponSkin, Skin.class);

                if (killer != null && victim != null && skin != null)
                {
                    BrainOut.EventMgr.sendEvent(KillEvent.obtain(killer, victim, instrument, skin, killMsg.kind));
                }
            }

            if (killMsg.slowmo != 0)
            {
                getController().applySlowMo(killMsg.slowmo);
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final NotifyMsg notifyMsg)
    {
        Gdx.app.postRunnable(() -> BrainOut.EventMgr.sendEvent(NotifyEvent.obtain(notifyMsg.notifyAward,
                notifyMsg.amount, notifyMsg.reason,
                notifyMsg.method, notifyMsg.data)));

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final VoiceChatMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            float volume = 1;

            RemoteClient remoteClient = msg.id >= 0 ?
                BrainOutClient.ClientController.getRemoteClients().get(msg.id) : null;
            ActiveData playerData = msg.object >= 0 && map != null ? map.getActiveData(msg.object) : null;

            if (BrainOutClient.ClientController.getMyRemoteClient() == null)
                return;

            if (remoteClient == null || remoteClient.getPartyId() == null ||
                !remoteClient.getPartyId().equals(BrainOutClient.ClientController.getMyRemoteClient().getPartyId()))
            {
                volume = msg.volume;
            }

            Event ev = VoiceEvent.obtain(remoteClient, playerData);

            if (ev != null)
            {
                if (playerData != null)
                {
                    BrainOutClient.EventMgr.sendEvent(playerData, ev, false);
                }

                BrainOutClient.EventMgr.sendEvent(ev, false);

                ev.free();
            }

            BrainOutClient.Voice.playAudioData(volume, msg.data);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ActiveReceivedConsumableMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            ClientMap map = Map.Get(msg.d, ClientMap.class);

            if (map == null)
                return;

            ActiveData activeData = map.getActiveData(msg.activeId);

            if (activeData != null)
            {
                BrainOut.EventMgr.sendDelayedEvent(activeData,
                        ActiveReceivedContentEvent.obtain(msg.entityReceived, msg.amount));
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final TeamChanged teamChanged)
    {
        Gdx.app.postRunnable(() -> {
            Team team = (Team) BrainOut.ContentMgr.get(teamChanged.teamId);
            setTeam(team);
            setLastSpawnPoint(null);
        });

        return true;
    }

    /* ======================================================================================== */


    @Override
    public ID getID()
    {
        return ID.game;
    }

    @Override
    public void init()
    {
        //Analytics.EventSessionStart();
        BrainOutClient.Voice.setSendCallback((data, trigger) ->
        {
            BrainOutClient.ClientController.sendUDP(new VoiceChatMsg(data, BrainOutClient.Voice.getMicrophoneVolume()));

            if (trigger)
            {
                Gdx.app.postRunnable(() ->
                {
                    RemoteClient myRemoteClient = BrainOutClient.ClientController.getMyRemoteClient();

                    CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
                    PlayerData myPlayerData = csGame != null ? csGame.getPlayerData() : null;

                    Event ev = VoiceEvent.obtain(myRemoteClient, myPlayerData);

                    if (ev != null)
                    {
                        if (myPlayerData != null)
                        {
                            BrainOutClient.EventMgr.sendEvent(myPlayerData, ev, false);
                        }

                        BrainOutClient.EventMgr.sendEvent(ev, false);

                        ev.free();
                    }
                });
            }
        });

        PlayState playState = getController().getPlayState();

        if (playState instanceof ClientPSGame)
        {
            ClientPSGame game = ((ClientPSGame) getController().getPlayState());

            if (game.getTeamNames() != null)
            {
                for (String teamName : game.getTeamNames())
                {
                    teams.add((Team) BrainOut.ContentMgr.get(teamName));
                }
            }

            getController().getRemoteClients().clear();

            for (ObjectMap.Entry<String, String> entry : getController().getLevelsNames())
            {
                getController().setLevels(entry.key, (Levels) BrainOutClient.ContentMgr.get(entry.value));
            }

            shopCart.addDefaultItems();

            Gdx.app.postRunnable(() -> {
                // init the game mode at last
                game.initMode((success) ->
                        getController().sendTCP(new SimpleMsg(SimpleMsg.Code.clientInited)));
            });

            inited = true;
        }
    }

    @Override
    public void release()
    {
        if (inited)
        {
            inited = false;

            BrainOutClient.Voice.resetSendCallback();
            //Analytics.EventSessionEnd();

            Map.Dispose();

            BrainOut.EventMgr.sendDelayedEvent(MyPlayerSetEvent.obtain(null));

            getController().getPlayState().release();
        }
    }

    private void beginSpawn()
    {
        setState(State.spawningMenu);
    }

    public void teamSelected()
    {
        BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.teamSelected));
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        BrainOut.EventMgr.sendDelayedEvent(CSGameEvent.obtain(this, state));

        this.state = state;
    }

    public void setTeam(Team team)
    {
        getController().setTeam(team);

        BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.teamUpdated));
    }

    public void changeTeam(Team team, SocialController.RequestCallback callback)
    {
        JSONObject args = new JSONObject();
        args.put("team", team.getID());
        BrainOutClient.SocialController.sendRequest("change_team", args, callback);
    }

    public ShopCart getShopCart() {
        return shopCart;
    }

    public Spawnable getLastSpawnPoint()
    {
        return lastSpawnPoint;
    }

    public void setLastSpawnPoint(Spawnable spawnPoint)
    {
        this.lastSpawnPoint = spawnPoint;
    }

    private void updateUserProfile()
    {
        UserProfile userProfile = getController().getUserProfile();

        BrainOutClient.SocialController.userProfileUpdated(userProfile);

        for (ObjectMap.Entry<String, Content> entry : BrainOutClient.ContentMgr.getItems())
        {
            if (entry.value instanceof SlotItem)
            {
                SlotItem slotItem = ((SlotItem) entry.value);

                SlotItem.Selection selection = slotItem.getStaticSelection();

                if (selection instanceof InstrumentSlotItem.InstrumentSelection)
                {
                    InstrumentSlotItem.InstrumentSelection i = ((InstrumentSlotItem.InstrumentSelection) selection);

                    i.init(userProfile);
                }
            }
        }
    }

    public void saveSelection(ShopCart shopCart)
    {
        getController().sendTCP(new UpdateSelectionsMsg(lastSpawnPoint, shopCart));
    }

    public void spawnAt(Spawnable spawnAt)
    {
        lastSpawnPoint = spawnAt;

        if (spawnAt == null) return;

        getController().sendTCP(new SpawnMsg(spawnAt, shopCart));
    }

    public void cancelSpawn()
    {
        getController().sendTCP(new NotSpawnMsg());
    }

    public void onSpawned(PlayerData playerData)
    {
        setState(State.spawned);

        setPlayerData(playerData);
    }

    public Array<Team> getTeams()
    {
        return teams;
    }

    public PlayerData getPlayerData()
    {
        return playerData;
    }

    public void setPlayerData(PlayerData playerData)
    {
        this.playerData = playerData;

        BrainOut.EventMgr.sendDelayedEvent(MyPlayerSetEvent.obtain(playerData));
    }

    public void executeConsole(String text)
    {
        getController().sendTCP(new ConsoleCommand(text));
    }

    @Override
    public void render(Batch batch, RenderContext renderContext)
    {
        ClientMap map = Map.GetWatcherMap(ClientMap.class);

        if (map != null)
        {
            map.render(batch, renderContext);
        }
    }

    @Override
    public void postRender()
    {
        ClientMap map = Map.GetWatcherMap(ClientMap.class);

        if (map != null)
        {
            map.postRender();
        }

        GameMode gameMode = getController().getGameMode();

        if (gameMode != null)
        {
            ((ClientRealization) gameMode.getRealization()).postRender();
        }
    }

    @Override
    public void preRender()
    {
        ClientMap map = Map.GetWatcherMap(ClientMap.class);

        if (map != null)
        {
            map.preRender();
        }
    }

    @Override
    public void update(float dt)
    {
        tmp.clear();
        Map.All().toArray(tmp);

        for (Map map : tmp)
        {
            map.update(dt);
        }
    }

    public boolean isSpectator()
    {
        return spectator || getController().getTeam() instanceof SpectatorTeam;
    }

    public float getSpeed()
    {
        return speed;
    }

    public void setSpeed(float speed)
    {
        this.speed = speed;

        tmp2.clear();

        for (ObjectMap.Entry<String, Map> entry : new ObjectMap.Entries<>(Map.AllEntries()))
        {
            tmp2.add(entry.value);
        }

        for (Map map : tmp2)
        {
            map.setSpeed(speed);
        }
    }

    public Team getTeam()
    {
        return getController().getTeam();
    }

}
