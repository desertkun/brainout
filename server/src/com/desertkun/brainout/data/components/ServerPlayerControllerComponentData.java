package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.ClientList;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.common.msg.client.WeaponActionMsg;
import com.desertkun.brainout.common.msg.client.WeaponMagazineActionMsg;
import com.desertkun.brainout.common.msg.server.*;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.*;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.DecayConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.PlaceBlock;
import com.desertkun.brainout.content.instrument.ThrowableInstrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.ThrowableActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.instrument.*;
import com.desertkun.brainout.data.interfaces.BonePointData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeRealization;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.utils.GrenadeUtils;
import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.spine.AnimationState;

import java.util.Comparator;
import java.util.TimerTask;

@Reflect("ServerPlayerControllerComponent")
@ReflectAlias("data.components.ServerPlayerControllerComponentData")
public class ServerPlayerControllerComponentData extends PlayerControllerComponentData
{
    private final PlayerData playerData;
    private final ClientList clients;
    private Vector2 movePos;
    private int clientId;
    private boolean enabled;
    private boolean hadBottomContact;
    private float smokeCheck;

    private static ObjectMap<String, String> tmp = new ObjectMap<>();

    public ServerPlayerControllerComponentData(PlayerData playerData)
    {
        super(playerData, null);

        this.playerData = playerData;
        this.clients = BrainOutServer.Controller.getClients();
        this.movePos = new Vector2();
        this.enabled = true;
        this.clientId = -1;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    protected boolean isValidForDeadPlayer(PlayerClient client)
    {
        return false;
    }

    @Override
    protected boolean applyMoveDirection()
    {
        return false;
    }

    public Client getClient()
    {
        return BrainOutServer.Controller.getClients().get(clientId);
    }

    public boolean validPlayer(Client client, int priority)
    {
        if (client.getId() == playerData.getOwnerId())
            return false;

        if (!(client instanceof PlayerClient))
            return false;

        PlayerClient playerClient = ((PlayerClient) client);

        Map map = playerData.getMap();
        if (map == null)
            return false;

        if (playerClient.getLastKnownDimension() != null)
        {
            if (!playerClient.getLastKnownDimension().equals(map.getDimension()))
                return false;
        }

        ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());
        if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.hideOthers))
            return false;

        PlayerData clientPlayer = client.getPlayerData();

        int properDistance;
        switch (priority)
        {
            case 0:
            {
                properDistance = 192;
                break;
            }
            case 1:
            {
                properDistance = 64;
                break;
            }
            case 2:
            default:
            {
                properDistance = 32;
                break;
            }
        }

        ServerTeamVisibilityComponentData tm = playerData.getComponent(ServerTeamVisibilityComponentData.class);
        if (tm != null)
        {
            if (isValidForDeadPlayer(((PlayerClient) client)))
            {
                return true;
            }

            if (tm.isVisibleTo(((PlayerClient) client)))
            {
                if (clientPlayer == null ||
                        Vector2.dst2(playerData.getX(), playerData.getY(), clientPlayer.getX(), clientPlayer.getY()) <= properDistance * properDistance)
                {
                    return true;
                }

                PlayerComponentData poc = clientPlayer.getComponent(PlayerComponentData.class);
                if (poc != null)
                {
                    Vector2 wp = poc.getMousePosition();
                    if (Vector2.dst2(playerData.getX(), playerData.getY(),
                        clientPlayer.getX() + wp.x, clientPlayer.getY() + wp.y) <= properDistance * properDistance)
                    {
                        return true;
                    }
                }
            }
        }
        else
        {
            if (clientPlayer != null)
            {
                if (Vector2.dst2(playerData.getX(), playerData.getY(), clientPlayer.getX(), clientPlayer.getY()) <= properDistance * properDistance)
                {
                    return true;
                }

                PlayerComponentData poc = clientPlayer.getComponent(PlayerComponentData.class);

                if (poc != null)
                {
                    Vector2 wp = poc.getMousePosition();
                    if (Vector2.dst2(playerData.getX(), playerData.getY(),
                        clientPlayer.getX() + wp.x, clientPlayer.getY() + wp.y) <= properDistance * properDistance)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean validDistance(Client client)
    {
        return movePos.dst2(client.getWatchingPoint()) < ServerConstants.Clients.MAX_UPDATE_MOVEMENT_DISTANCE_SQR;
    }

    private boolean validDimension(Client client)
    {
        if (playerData == null || client.getPlayerData() == null)
        {
            return true;
        }

        return client.getPlayerData().getDimension().equals(playerData.getDimension());
    }

    public void moveTo(float x, float y)
    {
        movePos.set(x, y);

        this.sendPlayerDataTCPIncludingOwner();
    }

    @Override
    public void sendPlayerData(boolean spectatorsOnly, int priority)
    {
        movePos.set(playerData.getX(), playerData.getY());
        UdpMessage move = generateMoveMessage(false);
        if (move == null)
            return;

        if (spectatorsOnly)
        {
            clients.sendUDPExcept(move, clientId, client ->
            {
                if (!(client instanceof PlayerClient))
                    return false;

                return isValidForDeadPlayer((PlayerClient)client);
            });
        }
        else
        {
            clients.sendUDPExcept(move, clientId, client -> validPlayer(client, priority));
        }
    }

    public void sendPlayerDataTCPIncludingOwner()
    {
        movePos.set(playerData.getX(), playerData.getY());
        UdpMessage move = generateMoveMessage(true);
        if (move == null)
            return;

        clients.sendTCP(move);
    }

    public OtherPlayerInstrumentActionMsg generateInstrumentActionMessage(InstrumentData weaponData, Instrument.Action action)
    {
        SimplePhysicsComponentData cmp = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);
        if (cmp == null)
            return null;

        PlayerComponentData pcd = playerData.getComponent(PlayerComponentData.class);
        Vector2 speed = cmp.getSpeed();

        if (weaponData == null)
            return null;

        return new OtherPlayerInstrumentActionMsg(
                playerData.getId(), movePos.x, movePos.y,
                speed.x, speed.y, playerData.getAngle(), playerData.getDimension(),
                pcd.getMousePosition().x, pcd.getMousePosition().y,
                weaponData, action);
    }

    public OtherPlayerMagazineActionMsg generateMagazineActionMessage(
            InstrumentData weaponData, WeaponMagazineActionMsg.Action action)
    {
        SimplePhysicsComponentData cmp = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);
        if (cmp == null)
            return null;

        PlayerComponentData pcd = playerData.getComponent(PlayerComponentData.class);
        Vector2 speed = cmp.getSpeed();

        return new OtherPlayerMagazineActionMsg(
                playerData.getId(), movePos.x, movePos.y,
                speed.x, speed.y, playerData.getAngle(), playerData.getDimension(),
                pcd.getMousePosition().x, pcd.getMousePosition().y,
                weaponData, action);
    }

    public UdpMessage generateMoveMessage(boolean enforce)
    {
        PlayerData pl = playerData;
        if (pl == null)
            return null;

        SimplePhysicsComponentData cmp = pl.getComponentWithSubclass(SimplePhysicsComponentData.class);
        if (cmp == null)
            return null;

        PlayerComponentData pcd = pl.getComponent(PlayerComponentData.class);
        Vector2 speed = cmp.getSpeed();

        return new ServerPlayerMoveMsg(
                playerData.getId(), movePos.x, movePos.y,
                speed.x, speed.y, playerData.getAngle(), playerData.getDimension(),
                pcd.getMousePosition().x, pcd.getMousePosition().y, enforce);
    }

    @Override
    protected void sendAim(boolean aim)
    {
        sendUDPExceptDistance(new OtherPlayerAimMsg(playerData, aim));
    }

    @Override
    protected void sendState(Player.State state)
    {
        sendUDPExceptDistance(new OtherPlayerStateMsg(playerData, state));
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
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
    public void update(float dt)
    {
        super.update(dt);

        // update bottom contact

        SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy == null)
            return;

        boolean hasBottomContact = phy.hasContact(SimplePhysicsComponentData.Contact.bottom);

        if (hasBottomContact != hadBottomContact)
        {
            if (hasBottomContact)
            {
                if (playerData.getPlayer().isSteps())
                {
                    launchBottomContact();
                }
            }

            hadBottomContact = hasBottomContact;
        }

        smokeCheck -= dt;

        if (smokeCheck < 0)
        {
            smokeCheck = 0.5f;

            Map map = playerData.getMap();
            if (map != null)
            {
                checkSmoke(map);
            }
        }
    }

    private void checkSmoke(Map map)
    {
        BlockData blockAt = map.getBlockAt(playerData.getX(), playerData.getY(), Constants.Layers.BLOCK_LAYER_UPPER);

        if (blockAt == null)
            return;

        if (blockAt.getCreator().isCanBeSeenTrough())
            return;

        ServerTeamVisibilityComponentData stv = playerData.getComponent(ServerTeamVisibilityComponentData.class);

        if (stv != null)
        {
            stv.hide();
        }
    }

    // ========================================== PUBLIC =====================================================

    public void setAim(boolean aim)
    {
        PlayerComponentData pcd = playerData.getComponentWithSubclass(PlayerComponentData.class);

        if (pcd == null)
            return;

        if (aim && isWounded())
            return;

        pcd.setAim(aim);
    }

    public void setState(Player.State state)
    {
        this.state = state;

        PlayerComponentData pcd = playerData.getComponentWithSubclass(PlayerComponentData.class);

        if (pcd == null)
            return;

        switch (state)
        {
            case crawl:
            {
                if (BrainOut.PackageMgr.getDefine("crawl", "").equals("disabled"))
                {
                    return;
                }

                break;
            }
        }

        if (isWounded())
        {
            state = Player.State.wounded;
        }

        pcd.setState(state);
    }

    public boolean move(float x, float y, float aimX, float aimY, float moveX, float moveY)
    {
        PlayerData pl = playerData;
        if (pl == null)
            return false;

        if (!isEnabled())
            return true;

        PlayerComponentData pcd = pl.getComponent(PlayerComponentData.class);

        if (pcd == null)
            return false;

        pcd.getMousePosition().set(aimX, aimY);

        float angle = pcd.getMousePosition().angleDeg();

        ServerPhysicsSyncComponentData sync = pl.getComponent(ServerPhysicsSyncComponentData.class);

        if (sync != null)
        {
            setMoveDirection(moveX, moveY);

            return sync.sync(x, y, angle);
        }

        return false;
    }

    public boolean launchBullet(PlayerData pd, final Bullet bullet, Bullet.BulletSlot slot,
        final float x, final float y, final float[] angles, int bulletsAmount, int random)
    {
        if (isWounded())
            return false;

        if (!isEnabled())
            return false;

        Client client = getClient();
        if (client == null)
            return false;

        Map map = pd.getMap();

        if (map == null)
            return false;

        ChunkData chunk = map.getChunkAt((int)pd.getX(), (int)pd.getY());

        if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
            return false;

        boolean syncToOthers = chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);

        if (bullet != null)
        {
            final PlayerOwnerComponent poc = pd.getComponent(PlayerOwnerComponent.class);

            InstrumentData currentInstrument = poc.getCurrentInstrument();

            if (currentInstrument instanceof WeaponData)
            {
                final WeaponData weaponData = ((WeaponData) currentInstrument);

                final ServerWeaponComponentData sw = currentInstrument.getComponent(ServerWeaponComponentData.class);

                if (sw != null)
                {
                    if (sw.getContentComponent().isUsedEvent())
                    {
                        if (client instanceof PlayerClient)
                        {
                            PlayerClient playerClient = ((PlayerClient) client);
                            ModePayload payload = playerClient.getModePayload();
                            if (payload instanceof FreePayload)
                            {
                                FreePayload freePayload = ((FreePayload) payload);
                                freePayload.questEvent(FreePlayItemUsedEvent.obtain(playerClient,
                                    ((WeaponData) currentInstrument).getWeapon(), 1));
                            }
                        }
                    }

                    bulletsAmount = MathUtils.clamp(bulletsAmount, 1,
                        weaponData.getWeapon().getPrimaryProperties().getBulletAtLaunch());

                    ServerWeaponComponentData.Slot wslot = sw.getSlot(slot.name());

                    if (wslot != null)
                    {
                        if (bullet != wslot.getBullet())
                        {
                            return false;
                        }

                        switch (wslot.launch(angles, bulletsAmount, random))
                        {
                            case success:
                            {
                                WeaponAnimationComponentData wac =
                                        weaponData.getComponent(WeaponAnimationComponentData.class);

                                BonePointData lp = wac.getLaunchPointData();

                                // TODO: apply distance validation here

                                boolean silent = wslot.isSilent();

                                BulletThrowableComponent throwableComponent =
                                        bullet.getComponent(BulletThrowableComponent.class);

                                for (float angle : angles)
                                {
                                    // offset angles by half a block
                                    float x_ = x - MathUtils.cosDeg(angle) * 0.5f;
                                    float y_ = y - MathUtils.sinDeg(angle) * 0.5f;

                                    LaunchData launchAt = new PointLaunchData(x_, y_, angle, map.getDimension());

                                    BulletData bulletData = bullet.getData(launchAt, wslot.getDamage(),
                                        getComponentObject().getDimension());

                                    if (bulletData == null) return false;

                                    bulletData.setOwnerId(pd.getOwnerId());
                                    bulletData.setPlayerData(pd);
                                    bulletData.setInstrumentInfo(weaponData.getInfo());

                                    map.addBullet(bulletData);

                                    if (throwableComponent != null)
                                    {
                                        ThrowableActive thr = throwableComponent.getThrowActive();

                                        ThrowableActiveData activeData = thr.getData(map.getDimension());

                                        activeData.setPosition(x_, y_);
                                        activeData.setOwnerId(pd.getOwnerId());
                                        activeData.setAngle(angle);
                                        activeData.setLaunchedBy(weaponData.getInfo());
                                        activeData.setTeam(pd.getTeam());

                                        if (!syncToOthers)
                                        {
                                            // if the chunk has the hideOthers flag, sync the active data ONLY to the player who has
                                            // launched it

                                            ActiveFilterComponentData acf = new ActiveFilterComponentData(
                                                    owner -> owner == clientId);

                                            activeData.addComponent(acf);
                                        }

                                        SimplePhysicsComponentData phy =
                                                activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);
                                        SimplePhysicsComponentData playerPhy =
                                                playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

                                        phy.getSpeed().set(
                                            throwableComponent.getThrowPower() * MathUtils.cosDeg(angle),
                                            throwableComponent.getThrowPower() * MathUtils.sinDeg(angle)
                                        );

                                        map.addActive(map.generateServerId(), activeData, true);
                                    }

                                }

                                if (!syncToOthers)
                                {
                                    // in  case of hideOthers flag, do not sync the shooting to other players
                                    return true;
                                }

                                sendUDPExceptDimension(new OtherPlayerBulletLaunch(pd, x, y,
                                        pd.getX(), pd.getY(), angles,
                                        weaponData, bullet, slot, silent));

                                ServerTeamVisibilityComponentData stv = pd.getComponent(ServerTeamVisibilityComponentData.class);

                                if (stv != null && !wslot.isSilent())
                                {
                                    stv.show();
                                }

                                return true;
                            }
                            case pullRequired:
                            {
                                sendUDP(new PullRequiredMsg(poc.getCurrentInstrumentRecord().getId(), slot.name()));
                                break;
                            }
                            default:
                            {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public void activateInstrument(ConsumableRecord record)
    {
        if (record.getItem() instanceof InstrumentConsumableItem)
        {
            InstrumentConsumableItem item = ((InstrumentConsumableItem) record.getItem());

            InstrumentData instrumentData = item.getInstrumentData();

            BrainOutServer.EventMgr.sendDelayedEvent(instrumentData,
                    ActivateInstrumentEvent.obtain(record));
        }
    }

    public void activateItem(ConsumableRecord record)
    {
        Client client = getClient();
        if (client == null)
            return;

        SimplePhysicsComponentData spc = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);
        if (spc != null && !spc.hasAnyContact() && !spc.hasFixture())
            return;
        
        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        ConsumableItem item = record.getItem();
        ServerItemActivatorComponent activator =
            item.getContent().getComponentFrom(ServerItemActivatorComponent.class);

        if (activator == null)
            return;

        if (activator.activate(((PlayerClient) client), playerData, record.getQuality()))
        {
            if (item instanceof DecayConsumableItem)
            {
                ((DecayConsumableItem) item).use(poc.getConsumableContainer(), record);
                consumablesUpdated();
            }
            else
            {
                if (poc.getConsumableContainer().getConsumable(1, record).amount > 0)
                {
                    consumablesUpdated();
                }
            }
        }
    }

    public ConsumableRecord changeInstrument(int id)
    {
        if (playerData == null)
            return null;

        if (isWounded())
            return null;

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            if (poc.getCurrentInstrument() != null)
            {
                if (poc.getCurrentInstrument().getInstrument().isForceSelect())
                {
                    return null;
                }
            }

            ConsumableRecord record = poc.setCurrentInstrument(id);
            if (record != null)
            {
                final PlayerRemoteComponent prc = playerData.getComponent(PlayerRemoteComponent.class);
                prc.setCurrentInstrument(poc.getCurrentInstrument());
                prc.setHookedInstrument(poc.getHookedInstrument());

                BrainOutServer.PostRunnable(() ->
                {
                    if (poc.getCurrentInstrument() != null)
                    {
                        Client client = getClient();

                        Instrument instrument = poc.getCurrentInstrument().getInstrument();

                        if (client != null && instrument.hasComponent(ServerSelectFreeplayEventComponent.class))
                        {
                            if (client instanceof PlayerClient)
                            {
                                PlayerClient playerClient = ((PlayerClient) client);
                                ModePayload payload = playerClient.getModePayload();
                                if (payload instanceof FreePayload)
                                {
                                    FreePayload freePayload = ((FreePayload) payload);
                                    freePayload.questEvent(FreePlayItemUsedEvent.obtain(playerClient, instrument, 1));
                                }
                            }
                        }

                        BrainOutServer.Controller.getClients().sendTCP(
                                new RemoteUpdateInstrumentMsg(playerData,
                                        poc.getCurrentInstrument(), poc.getHookedInstrument()));
                    }
                });

                updateAttachments();

                return record;
            }
        }

        return null;
    }

    public boolean launchThrowable(final ConsumableRecord record, float x, float y, float angle)
    {
        if (!isEnabled())
            return false;

        if (isWounded())
            return false;

        ServerMap map = playerData.getMap(ServerMap.class);

        if (map == null)
            return false;

        ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());

        if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
            return false;

        boolean syncToOthers = chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null || record == null) return false;

        if (poc.getInstrument(record.getId()) != poc.getCurrentInstrument())
        {
            // if due to udp we lost the switch instrument message
            if (changeInstrument(record.getId()) == null)
            {
                return false;
            }
        }

        if (!(record.getItem() instanceof InstrumentConsumableItem))
        {
            return false;
        }

        InstrumentData instrument = ((InstrumentConsumableItem) record.getItem()).getInstrumentData();

        if (instrument instanceof ThrowableInstrumentData)
        {
            final ThrowableInstrumentData throwableInstrumentData = ((ThrowableInstrumentData) instrument);


            ThrowableInstrument thrIn = throwableInstrumentData.getInstrument();
            ThrowableActive thr = thrIn.getThrowActive();

            if (thr != null)
            {
                ServerGrenadeComponentData tc = throwableInstrumentData.
                    getComponent(ServerGrenadeComponentData.class);

                if (tc != null && !tc.isCooked()) return false;

                ThrowableActiveData activeData = thr.getData(map.getDimension());

                activeData.setPosition(x, y);
                activeData.setOwnerId(playerData.getOwnerId());
                activeData.setLaunchedBy(throwableInstrumentData.getInfo());
                activeData.setTeam(playerData.getTeam());

                GrenadeUtils.getGrenadeOutOfWall(map, x, y, playerData, activeData);

                SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);
                SimplePhysicsComponentData playerPhy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

                if (tc != null)
                {
                    TimeToLiveComponentData ttld = activeData.getComponent(TimeToLiveComponentData.class);
                    if (ttld != null)
                    {
                        ttld.setTime(tc.getTimeLeft());
                    }
                }

                phy.getSpeed().set(
                        thrIn.getThrowPower() * MathUtils.cosDeg(angle),
                        thrIn.getThrowPower() * MathUtils.sinDeg(angle)
                );

                phy.getSpeed().add(playerPhy.getSpeed());

                if (tc != null)
                {
                    tc.cancel();
                }

                poc.getConsumableContainer().getConsumable(1, record);
                consumablesUpdated();

                if (record.getAmount() == 0)
                {
                    BrainOutServer.Timer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            BrainOutServer.PostRunnable(() -> selectFirstInstrument(poc));
                        }
                    }, 200);
                }

                if (!syncToOthers)
                {
                    // if the chunk has the hideOthers flag, sync the active data ONLY to the player who has
                    // launched it

                    ActiveFilterComponentData acf = new ActiveFilterComponentData(
                            owner -> owner == clientId);

                    activeData.addComponent(acf);
                }

                map.addActive(map.generateServerId(), activeData, true);

                if (syncToOthers)
                {
                    sendUDPExcept(new OtherPlayerInstrumentLaunch(playerData,
                            throwableInstrumentData));
                }

                return true;
            }
        }

        return false;
    }

    public void selectFirstInstrument(PlayerOwnerComponent poc)
    {
        if (isWounded())
            return;

        Slot instrumentSlot = null;

        ConsumableRecord record = poc.getCurrentInstrumentRecord();

        if (record != null)
        {
            if (record.getItem() instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
                Instrument instrument = (Instrument) ici.getInstrumentData().getContent();

                if (instrument != null)
                {
                    instrumentSlot = instrument.getSlot();

                }
            }
        }

        Array<ConsumableRecord> records = poc.getConsumableContainer().queryRecords(
            r -> r.getItem() instanceof InstrumentConsumableItem
        );

        for (ConsumableRecord r: records)
        {
            if (r != record && r.getItem() instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) r.getItem());

                if (((Instrument) ici.getContent()).getSlot() == instrumentSlot)
                {
                    poc.setCurrentInstrument(r);
                    instrumentSelected(r);

                    changeInstrument(r.getId());

                    return;
                }
            }
        }

        records.sort(Comparator.comparingInt(InstrumentConsumableItem::SortRecords));

        for (ConsumableRecord r: records)
        {
            poc.setCurrentInstrument(r);
            instrumentSelected(r);

            changeInstrument(r.getId());

            break;
        }
    }

    public ItemData dropConsumable(int recordId, float angle, int amount)
    {
        Client client = getClient();
        if (client == null)
            return null;

        if (isWounded())
            return null;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return null;

        ConsumableRecord record = poc.getConsumableContainer().get(recordId);
        if (record == null || record.getAmount() < amount)
            return null;

        {
            Map map = getMap();

            if (map == null)
                return null;

            ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());

            if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
                return null;
        }

        ConsumableItem item = record.getItem();

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode != null)
        {
            GameModeRealization realization = gameMode.getRealization();

            if (realization instanceof ServerRealization)
            {
                if (!((ServerRealization) realization).canDropConsumable(((PlayerClient) client), item))
                    return null;
            }
        }

        boolean currentInstrument = false;

        Item dropItem = null;
        if (item.getContent().hasComponent(ItemComponent.class))
        {
            dropItem = item.getContent().getComponent(ItemComponent.class).getDropItem();
        }

        if (item instanceof InstrumentConsumableItem)
        {
            InstrumentData instrumentData = ((InstrumentConsumableItem) item).getInstrumentData();
            currentInstrument = instrumentData == playerData.getCurrentInstrument();
            Instrument instrument = instrumentData.getInstrument();

            ServerWeaponComponentData swc = instrumentData.getComponent(ServerWeaponComponentData.class);
            if (swc != null)
            {
                for(ServerWeaponComponentData.Slot slot : swc.getSlots().values())
                {
                    slot.checkCancelReloading();
                }
            }

            if (instrument.hasComponent(ItemComponent.class))
            {
                dropItem = instrument.getComponent(ItemComponent.class).getDropItem();
            }
        }

        AutoConvertConsumable auto = item.getContent().getComponent(AutoConvertConsumable.class);

        if (auto != null)
        {
            item = auto.getConvertTo().acquireConsumableItem();
        }

        Array<ConsumableRecord> records = new Array<>();
        ConsumableRecord r = new ConsumableRecord(item, amount, 0);
        r.setQuality(record.getQuality());
        records.add(r);

        ItemData itemData = ServerMap.dropItem(playerData.getDimension(), dropItem, records, playerData.getOwnerId(),
                playerData.getX(), playerData.getY(), angle);

        poc.getConsumableContainer().decConsumable(record, amount);

        BrainOutServer.Controller.getClients().sendTCP(
                new InventoryItemMovedMsg(playerData, item.getContent()));

        if (currentInstrument)
        {
            selectFirstInstrument(poc);
        }

        updateAttachments();
        consumablesUpdated();

        return itemData;
    }

    public boolean placeBlock(Block block, ConsumableRecord record, int layer, int placeX, int placeY)
    {
        if (!isEnabled())
            return false;

        if (isWounded())
            return false;

        Map map = getMap();

        if (map == null)
            return false;

        {
            ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());

            if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
                return false;
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode != null && !gameMode.isGameActive())
        {
            return false;
        }

        for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
        {
            if (activeData.getTeam() instanceof SpectatorTeam)
                continue;

            if (activeData instanceof PlayerData)
            {
                SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

                if (phy != null && phy.overlapsBlock(placeX, placeY))
                {
                    return false;
                }
            }
        }

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        InstrumentData currentInstrument = poc.getCurrentInstrument();

        if (!(currentInstrument instanceof PlaceBlockData)) return false;

        boolean allowed = currentInstrument instanceof BoxData || poc.getConsumableContainer().hasConsumable(block);

        if (allowed)
        {
            if (poc.getInstrument(record.getId()) != poc.getCurrentInstrument())
            {
                // if due to udp we lost the switch instrument message
                if (changeInstrument(record.getId()) == null)
                {
                    return false;
                }
            }
            PlaceBlockData placeBlockData = ((PlaceBlockData) currentInstrument);
            float maxDist = placeBlockData.getPlaceBlock().getMaxDistance() + 1;

            // check distance
            if (Vector2.dst(playerData.getX(), playerData.getY(), placeX, placeY) <= maxDist)
            {
                boolean found = false;

                // check if we have some near blocks
                for (int j = -ServerConstants.Blocks.PLACE_MIN_DISTANCE; j <= ServerConstants.Blocks.PLACE_MIN_DISTANCE; j++)
                {
                    for (int i = -ServerConstants.Blocks.PLACE_MIN_DISTANCE; i <= ServerConstants.Blocks.PLACE_MIN_DISTANCE; i++)
                    {
                        if (map.getBlock(placeX + i, placeY + j, layer) != null)
                        {
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) return false;

                if (map.getBlock(placeX, placeY, layer) != null)
                {
                    return false;
                }

                BlockData blockData = block.getBlock();
                if (!map.setBlock(placeX, placeY, blockData, layer, true))
                {
                    return false;
                }

                TeamComponentData tcd = blockData.getComponent(TeamComponentData.class);
                if (tcd != null)
                {
                    tcd.setTeam(playerData.getTeam());
                }

                if (currentInstrument instanceof BoxData)
                {
                    poc.removeConsumable(poc.getCurrentInstrumentRecord());

                    selectFirstInstrument(poc);
                }
                else
                {
                    poc.getConsumableContainer().decConsumable(block);
                }

                consumablesUpdated();
            }

            sendUDPExceptDistance(new OtherPlayerInstrumentLaunch(playerData, currentInstrument));

            return true;
        }

        return false;
    }

    public boolean removeBlock(ConsumableRecord record, int layer, int x, int y)
    {
        if (!isEnabled())
            return false;

        Map map = getMap();

        if (map == null)
            return false;

        {
            ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());

            if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
                return false;
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode != null && !gameMode.isGameActive())
        {
            return false;
        }

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc.getInstrument(record.getId()) != poc.getCurrentInstrument())
        {
            // if due to udp we lost the switch instrument message
            if (changeInstrument(record.getId()) == null)
            {
                return false;
            }
        }

        InstrumentData currentInstrument = poc.getCurrentInstrument();

        if (!(currentInstrument instanceof PlaceBlockData)) return false;

        PlaceBlockData placeBlockData = ((PlaceBlockData) currentInstrument);
        float maxDist = placeBlockData.getPlaceBlock().getMaxDistance() + 1;

        // check distance
        if (Vector2.dst(playerData.getX(), playerData.getY(), x, y) <= maxDist)
        {
            BlockData blockAt = map.getBlock(x, y, layer);

            // if there is nothing to remove
            if (blockAt == null) return false;

            PlaceBlock placeBlock = placeBlockData.getPlaceBlock();

            DropComponentData dcd = blockAt.getComponent(DropComponentData.class);

            // if there is no way to drop
            if (dcd != null)
            {
                // there is no chance to drop, so don't destroy
                if (!dcd.getContentComponent().hasChance(placeBlock.getID())) return false;
            }

            BrainOut.EventMgr.sendEvent(blockAt,
                DamageEvent.obtain(placeBlock.getDamage(), playerData.getId(),
                    currentInstrument.getInfo(), null,
                    x + 0.5f, y + 0.5f, 0,
                    Constants.Damage.DAMAGE_HIT));

            sendUDPExceptDistance(new OtherPlayerInstrumentLaunch(playerData, currentInstrument));

            return true;
        }

        return false;
    }

    private ConsumableRecord getUniqueItem(String category, ConsumableContainer inventory)
    {
        // best is because of worst weapons have bigger 'weight'
        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : inventory.getData())
        {
            Content c = entry.value.getItem().getContent();

            UniqueComponent uc = c.getComponent(UniqueComponent.class);

            if (uc != null && uc.getCategory().equals(category))
            {
                return entry.value;
            }
        }

        return null;
    }

    private ConsumableRecord getWorstWeapon(String slot, ConsumableContainer inventory)
    {
        // best is because of worst weapons have bigger 'weight'
        return inventory.queryBestRecord(record ->
        {
            Content content = record.getItem().getContent();

            if (content instanceof Weapon)
            {
                Weapon weapon = ((Weapon) content);

                if (weapon.getSlot() != null && !weapon.getSlot().getID().equals(slot))
                    return -1;

                if (weapon.getSlotItem() == null)
                    return 0;

                return RandomWeightComponent.Get(weapon.getSlotItem());
            }

            return -1;
        });
    }

    @Override
    public void init()
    {
        super.init();

        updateAttachments();

        if (playerData.getPlayer().isSteps())
        {
            PlayerAnimationComponentData animation = playerData.getComponent(PlayerAnimationComponentData.class);

            animation.getState().addListener(new AnimationState.AnimationStateAdapter()
            {
                @Override
                public void event(AnimationState.TrackEntry entry, com.esotericsoftware.spine.Event event)
                {
                    if (!event.getData().getName().equals("step"))
                        return;

                    if (playerData.getState() == Player.State.sit)
                        return;

                    if (playerData.getState() == Player.State.crawl)
                        return;

                    launchBottomContact();
                }
            });
        }
    }

    public void updateAttachments()
    {
        PlayerAnimationComponentData anim = playerData.getComponent(PlayerAnimationComponentData.class);

        if (anim == null)
            return;

        ObjectMap<String, String> a = anim.getAttachments();

        tmp.clear();
        tmp.putAll(a);

        a.clear();

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            for (ObjectMap.Entry<Integer, ConsumableRecord> entry : poc.getConsumableContainer().getData())
            {
                Content c = entry.value.getItem().getContent();

                ConsumableItem item = entry.value.getItem();
                if (item instanceof InstrumentConsumableItem)
                {
                    InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);

                    if (ici.getInstrumentData() != playerData.getCurrentInstrument())
                    {
                        continue;
                    }
                }

                ReplaceSlotComponent rsc = c.getComponent(ReplaceSlotComponent.class);

                if (rsc != null)
                {
                    a.putAll(rsc.getReplace());
                }
            }
        }

        if (!tmp.equals(a))
        {
            anim.updated(playerData);
        }
    }

    private int getSlotWeaponsAmount(String slot, ConsumableContainer inventory)
    {
        return inventory.queryRecordsOfClassAmount(
            (weapon, record) -> weapon.getSlot() != null &&
                weapon.getSlot().getID().equals(slot), Weapon.class);
    }

    public boolean pickUpRecordItem(ItemData itemData, ConsumableRecord record, int amount)
    {
        if (isWounded())
            return false;

        float w = 0;

        Map map = itemData.getMap();

        if (map == null)
            return false;

        ConsumableItem consumableItem = record.getItem();

        if (consumableItem.isPrivate() && consumableItem.getPrivate() != clientId)
        {
            return false;
        }

        Client client = getClient();
        if (client == null)
            return false;

        Content c = consumableItem.getContent();

        {
            QuestOnlyComponent qc = c.getComponent(QuestOnlyComponent.class);
            if (qc != null)
            {
                if (client.getModePayload() instanceof FreePayload)
                {
                    FreePayload freePayload = ((FreePayload) client.getModePayload());

                    if (!freePayload.isQuestActive(qc.getQuest()))
                    {
                        return false;
                    }
                }
            }
        }

        Content content = consumableItem.getContent();

        /*
        if (content.hasComponent(ItemComponent.class))
        {
            ItemComponent itemComponent = content.getComponent(ItemComponent.class);
            w = itemComponent.getWeight();
        }
        */

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        ConsumableRecord changeTo = null;
        //float maxWeight = playerData.getPlayer().getMaxOverweight();

        poc.getConsumableContainer().updateWeight();

        //float weightLeft = maxWeight - poc.getConsumableContainer().getWeight();

        UniqueComponent u = content.getComponent(UniqueComponent.class);

        if (u != null)
        {
            changeTo = getUniqueItem(u.getCategory(), poc.getConsumableContainer());
        }
        else if (BrainOutServer.Controller.isFreePlay() && (content instanceof Weapon))
        {
            Weapon weapon = ((Weapon) content);

            if (weapon.getSlot() != null)
            {
                String slotId = weapon.getSlot().getID();

                switch (slotId)
                {
                    case "slot-primary":
                    {
                        int primary = getSlotWeaponsAmount("slot-primary",
                                poc.getConsumableContainer());

                        if (primary >= 2)
                        {
                            changeTo = getWorstWeapon("slot-primary",
                                    poc.getConsumableContainer());
                        }
                        break;
                    }
                    case "slot-secondary":
                    {
                        int secondary = getSlotWeaponsAmount("slot-secondary",
                            poc.getConsumableContainer());

                        if (secondary >= 1)
                        {
                            changeTo = getWorstWeapon("slot-secondary",
                                poc.getConsumableContainer());
                        }

                        break;
                    }
                }
            }
        }

        ConsumableContainer.AcquiredConsumables took = itemData.getRecords().getConsumable(amount, record);

        if (took.amount > 0)
        {
            ConsumableItem putItem = record.getItem();

            AutoConvertConsumable auto = putItem.getContent().getComponent(AutoConvertConsumable.class);

            if (auto != null)
            {
                putItem = auto.getConvertTo().acquireConsumableItem();
            }

            ConsumableRecord newRecord = poc.getConsumableContainer().putConsumable(took.amount, putItem, took.quality);
            if (newRecord != null)
            {
                newRecord.setTag(record.getTag());
            }

            if (changeTo != null)
            {
                took = poc.getConsumableContainer().getConsumable(1, changeTo);
                if (took.amount > 0)
                {
                    ConsumableRecord r = itemData.getRecords().putConsumable(took.amount, changeTo.getItem(), took.quality);
                    if (r != null)
                    {
                        r.setWho(clientId);
                    }
                }
            }

            updateAttachments();

            BrainOutServer.Controller.getClients().sendTCP(
                new InventoryItemMovedMsg(playerData, content));

            consumablesUpdated();
            itemData.updated();

            if (changeTo == poc.getCurrentInstrumentRecord())
            {
                selectFirstInstrument(poc);
            }

            if (itemData.getRecords().isEmpty() && itemData.isAutoRemove())
            {
                map.removeActive(itemData, true);
            }
        }

        return true;
    }

    private void launchBottomContact()
    {
        if (playerData == null)
            return;

        SimplePhysicsComponentData.ContactData contact =
            playerData.getComponentWithSubclass(SimplePhysicsComponentData.class).
            getContact(SimplePhysicsComponentData.Contact.bottom);

        if (contact.valid())
        {
            Map map = playerData.getMap();

            if (map == null)
                return;

            ChunkData chunk = map.getChunkAt(contact.x, contact.y);

            if (chunk == null)
                return;

            BlockData contactBlock = map.getBlock(contact.x, contact.y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

            if (contactBlock == null)
                return;

            if (chunk.hasFlag(ChunkData.ChunkFlag.hideOthers))
            {
                Client player = BrainOutServer.Controller.getClients().get(playerData.getOwnerId());

                if (player instanceof PlayerClient)
                {
                    step(contactBlock, map, contact.x, contact.y, playerData.getDimensionId(), ((PlayerClient) player));
                }
            }
            else
            {
                step(contactBlock, map, contact.x, contact.y, playerData.getDimensionId());
            }

            BrainOutServer.EventMgr.sendEvent(
                contact.block, StepEvent.obtain(null, playerData));
        }
    }

    private void step(BlockData contact, Map map, int x, int y, int dimension)
    {
        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            if (!(entry.value instanceof PlayerClient))
                continue;
            step(contact, map, x, y, dimension, ((PlayerClient) entry.value));
        }
    }

    private void step(BlockData contact, Map map, int x, int y, int dimension, PlayerClient playerClient)
    {
        if (contact == null)
            return;

        float watcherX, watcherY;

        PlayerData watcher = playerClient.getPlayerData();

        if (watcher != null && watcher.isAlive())
        {
            if (watcher.getDimensionId() != dimension)
                return;

            watcherX = watcher.getX();
            watcherY = watcher.getY();
        }
        else
        {
            int watching = playerClient.getCurrentlyWatching();

            if (watching != -1)
            {
                Client watchingAt = BrainOutServer.Controller.getClients().get(watching);

                if (watchingAt != null)
                {
                    PlayerData watchingAtPlayerData = watchingAt.getPlayerData();

                    if (watchingAtPlayerData != null && watchingAtPlayerData.isAlive())
                    {
                        watcherX = watchingAtPlayerData.getX();
                        watcherY = watchingAtPlayerData.getY();
                    }
                    else
                    {
                        return;
                    }
                }
                else
                {
                    return;
                }
            }
            else
            {
                return;
            }
        }

        float d = Constants.Sound.SOUND_HEAR_DIST * 0.25f;
        float v = (Vector2.len(watcherX - x, watcherY - y) / d);
        v = Interpolation.circleIn.apply(MathUtils.clamp(1.0f - v, 0.0f, 1.0f));
        float pan = (float) Math.sqrt(Math.abs(watcherX - x) / d);
        if (x < watcherX) pan = -pan;

        int i = BrainOutServer.Controller.getContentIndexFor(contact.getCreator());

        if (i >= 0)
        {
            playerClient.sendUDP(
                new StepMsg(i, v, pan, map.getDimensionId())
            );
        }
    }

    public boolean putRecordIntoItem(ItemData itemData, ConsumableRecord record, int amount)
    {
        if (isWounded())
            return false;

        ConsumableItem item = record.getItem();

        if (item.isPrivate() && item.getPrivate() != clientId)
        {
            return false;
        }

        Client client = getClient();
        if (client == null)
            return false;

        Content c = item.getContent();

        {
            QuestOnlyComponent qc = c.getComponent(QuestOnlyComponent.class);
            if (qc != null)
            {
                if (client.getModePayload() instanceof FreePayload)
                {
                    FreePayload freePayload = ((FreePayload) client.getModePayload());

                    if (!freePayload.isQuestActive(qc.getQuest()))
                    {
                        return false;
                    }
                }
            }
        }

        Item content = ((Item) itemData.getContent());

        if ( !content.checkFilter(itemData.getRecords(), record.getItem().getContent()) )
        {
            return false;
        }

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        ConsumableContainer cc = poc.getConsumableContainer();

        boolean currentInstrument = false;

        if (item instanceof InstrumentConsumableItem)
        {
            InstrumentData instrumentData = ((InstrumentConsumableItem) item).getInstrumentData();
            currentInstrument = instrumentData == playerData.getCurrentInstrument();
            Instrument instrument = instrumentData.getInstrument();

            if (!instrument.isThrowable() && !BrainOutServer.Controller.isFreePlay())
                return false;
        }

        if (!record.getItem().splits() && amount != record.getAmount())
        {
            return false;
        }

        if (itemData.getCreator().hasComponent(MaxWeightComponent.class))
        {
            MaxWeightComponent mx = itemData.getCreator().getComponent(MaxWeightComponent.class);

            if (mx != null)
            {
                ItemComponent itemComponent = record.getItem().getContent().getComponent(ItemComponent.class);
                if (itemComponent != null)
                {
                    itemData.getRecords().updateWeight();

                    if (itemData.getRecords().getWeight() + itemComponent.getWeight() * amount > mx.getMaxWeight())
                    {
                        return false;
                    }
                }
            }
        }

        ConsumableContainer.AcquiredConsumables took = cc.getConsumable(amount, record);

        poc.getConsumableContainer().updateWeight();

        updateAttachments();

        if (took.amount > 0)
        {
            BrainOutServer.Controller.getClients().sendTCP(
                new InventoryItemMovedMsg(playerData, item.getContent()));

            ConsumableRecord newRecord = itemData.getRecords().putConsumable(took.amount, record.getItem(), took.quality);
            if (newRecord != null)
            {
                newRecord.setWho(getClient().getId());
                newRecord.setTag(record.getTag());
            }

            if (currentInstrument)
            {
                selectFirstInstrument(poc);
            }

            consumablesUpdated();
            itemData.updated();
        }

        return true;
    }

    public void pickUpItem(ItemData itemData)
    {
        Map map = itemData.getMap();

        if (map == null)
            return;

        /*
        float w = itemData.getRecords().getWeight();

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        // if it's enough weight
        if (w + poc.getConsumableContainer().getWeight() <= playerData.getPlayer().getMaxOverweight())
        {
            if (BrainOutServer.EventMgr.sendEvent(itemData, EarnEvent.obtain(playerData)))
            {
                BrainOutServer.Controller.getMap().removeActive(itemData, true);
            }
        }
        */

        if (BrainOutServer.EventMgr.sendEvent(itemData, EarnEvent.obtain(playerData)))
        {
            updateAttachments();
            map.removeActive(itemData, true);
        }
    }

    // =======================================================================================================

    public void instrumentSelected(ConsumableRecord instrument)
    {
        Client client = getClient();

        if (client instanceof PlayerClient)
        {
            ((PlayerClient) client).sendTCP(new ServerSelectInstrumentMsg(instrument));
        }
    }

    public void consumablesUpdated()
    {
        Client client = getClient();

        if (client != null)
        {
            client.sendConsumable();
        }
    }

    public void ammoLoading(int weaponId, int magazineId, int bulletsId, int ammoCount)
    {
        Client client = getClient();

        if (client != null && client instanceof PlayerClient)
        {
            ((PlayerClient) client).sendLoadAmmoMsg(weaponId, magazineId, bulletsId, ammoCount);
        }
    }

    private void sendUDPExcept(UdpMessage msg)
    {
        clients.sendUDPExcept(msg, clientId);
    }

    private void sendUDPExceptDimension(UdpMessage msg)
    {
        clients.sendUDPExcept(msg, clientId, this::validDimension);
    }

    private void sendUDPExceptDistance(UdpMessage msg)
    {
        clients.sendUDPExcept(msg, clientId, this::validDistance);
    }

    private void sendUDP(UdpMessage msg)
    {
        clients.sendUDP(msg);
    }

    private void sendTCPExcept(Object msg)
    {
        clients.sendTCPExcept(msg, clientId);
    }

    public void sendTCP(Object msg)
    {
        clients.sendTCP(msg);
    }

    public void setClient(Client client)
    {
        this.clientId = client.getId();
    }

    public boolean weaponAction(
        final  WeaponActionMsg.Action action, final ConsumableRecord record,
        String slot, String slotB)
    {
        if (playerData == null || record == null)
            return false;

        if (isWounded())
            return false;

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return false;

        ServerMap map = playerData.getMap(ServerMap.class);

        if (map == null)
            return false;

        ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());

        if (action == WeaponActionMsg.Action.buildUp && chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
            return false;

        boolean syncOthers = chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);

        InstrumentData instrumentData = poc.getInstrument(record.getId());

        if (instrumentData instanceof WeaponData)
        {
            final WeaponData weaponData = ((WeaponData) instrumentData);
            ServerWeaponComponentData sw = weaponData.getComponent(ServerWeaponComponentData.class);

            if (sw != null)
            {
                BrainOutServer.PostRunnable(() ->
                {
                    ServerWeaponComponentData.Slot wslot = sw.getSlot(slot);

                    if (wslot != null)
                    {
                        switch (action)
                        {
                            case buildUp:
                            {
                                if (wslot.buildUp())
                                {
                                    if (syncOthers)
                                    {
                                        sendPlayerData(false, 2);
                                        sendUDPExceptDistance(generateInstrumentActionMessage(weaponData,
                                            Instrument.Action.buildUp));
                                    }
                                }

                                break;
                            }
                            case cock:
                            {
                                Instrument.Action waction;
                                if (wslot.getSlot().equals(Constants.Properties.SLOT_PRIMARY))
                                {
                                    waction = Instrument.Action.cock;
                                }
                                else
                                {
                                    waction = Instrument.Action.cockSecondary;
                                }

                                if (syncOthers)
                                {
                                    sendUDPExceptDistance(generateInstrumentActionMessage(weaponData, waction));
                                }

                                break;
                            }
                            case load:
                            {
                                if (wslot.load(poc, false, true))
                                {
                                    consumablesUpdated();

                                    if (syncOthers)
                                    {
                                        Instrument.Action waction;
                                        if (wslot.getSlot().equals(Constants.Properties.SLOT_PRIMARY))
                                        {
                                            waction = Instrument.Action.reload;
                                        }
                                        else
                                        {
                                            waction = Instrument.Action.reloadSecondary;
                                        }

                                        OtherPlayerInstrumentActionMsg msg =
                                                generateInstrumentActionMessage(weaponData, waction);

                                        if (msg != null)
                                        {
                                            msg.setDataFloat(wslot.getReloadTime().asFloat(), wslot.getFetchTime().asFloat());
                                            clients.sendUDP(msg, client -> validPlayer(client, 1));
                                        }
                                    }
                                }

                                break;
                            }
                            case fetch:
                            {
                                if (wslot.fetch(true))
                                {
                                    if (syncOthers)
                                    {
                                        Instrument.Action waction;
                                        if (wslot.getSlot().equals(Constants.Properties.SLOT_PRIMARY))
                                        {
                                            waction = Instrument.Action.fetch;
                                        }
                                        else
                                        {
                                            waction = Instrument.Action.fetchSecondary;
                                        }

                                        OtherPlayerInstrumentActionMsg msg =
                                                generateInstrumentActionMessage(weaponData, waction);

                                        msg.setDataFloat(wslot.getReloadTime().asFloat(), wslot.getFetchTime().asFloat());

                                        clients.sendUDP(msg, client -> validPlayer(client, 1));
                                    }
                                }

                                break;
                            }
                            case loadBoth:
                            {
                                ServerWeaponComponentData.Slot wslotB = sw.getSlot(slotB);
                                if (wslotB != null)
                                {
                                    if (wslot.loadBoth(poc, false, true) &&
                                        wslotB.loadBoth(poc, false, true))
                                    {
                                        consumablesUpdated();

                                        if (syncOthers)
                                        {
                                            OtherPlayerInstrumentActionMsg msg =
                                                generateInstrumentActionMessage(weaponData,
                                                    Instrument.Action.reloadBoth);

                                            msg.setDataFloat(wslot.getReloadBothTime().asFloat(), wslot.getFetchTime().asFloat());

                                            clients.sendUDP(msg, client -> validPlayer(client, 1));
                                        }
                                    }
                                }


                                break;
                            }
                            case unload:
                            {
                                if (wslot.unload(poc))
                                {
                                    consumablesUpdated();
                                }

                                break;
                            }
                        }
                    }

                });
            }
        }

        return false;
    }


    public boolean weaponMagazineAction(
        final WeaponMagazineActionMsg.Action action, final ConsumableRecord record,
        String slot, int magazineId)
    {
        if (playerData == null || record == null)
            return false;

        if (isWounded())
            return false;

        final PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return false;

        ServerMap map = playerData.getMap(ServerMap.class);

        if (map == null)
            return false;

        ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());

        boolean syncOthers = chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);

        InstrumentData instrumentData = poc.getInstrument(record.getId());

        if (instrumentData instanceof WeaponData)
        {
            final WeaponData weaponData = ((WeaponData) instrumentData);
            ServerWeaponComponentData sw = weaponData.getComponent(ServerWeaponComponentData.class);

            if (sw != null)
            {
                BrainOutServer.PostRunnable(() ->
                {
                    ServerWeaponComponentData.Slot wslot = sw.getSlot(slot);

                    if (wslot != null)
                    {
                        switch (action)
                        {
                            case loadOne:
                            {
                                ConsumableRecord bullets = poc.getConsumableContainer().getConsumable(wslot.getBullet());
                                int bulletsId = -1;

                                if (bullets != null)
                                {
                                    bulletsId = bullets.getId();
                                }

                                if (wslot.loadMagazineBullet(poc, magazineId))
                                {
                                    int weaponId =  record.getId();

                                    ammoLoading(weaponId, magazineId, bulletsId, 1);

                                    if (syncOthers)
                                    {
                                        sendUDPExceptDistance(generateMagazineActionMessage(weaponData, action));
                                    }
                                }

                                break;
                            }
                            case unloadAll:
                            {
                                if (wslot.unloadMagazineBullets(poc, magazineId))
                                {
                                    consumablesUpdated();

                                    if (syncOthers)
                                    {
                                        sendUDPExceptDistance(generateMagazineActionMessage(weaponData, action));
                                    }
                                }

                                break;
                            }
                        }
                    }

                });
            }
        }

        return false;
    }
}
