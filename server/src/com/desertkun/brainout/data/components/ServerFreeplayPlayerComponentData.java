package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.ActiveDamageMsg;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.common.msg.server.PlayerWoundedMsg;
import com.desertkun.brainout.common.msg.server.RemoteUpdateInstrumentMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.ServerFreeplayPlayerComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.mode.GameModeFree;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;

public class ServerFreeplayPlayerComponentData extends Component<ServerFreeplayPlayerComponent>
{
    private final PlayerData playerData;
    private final Map.Predicate radioactivePredicate, windPredicate;
    private float cnt;
    private float tmpTimer;
    private float syncAnyWay;

    private float woundedCheckTimer;
    private float godTimer;
    private float hungerCheckTask;

    private int tries;

    private FreeplayPlayerComponentData fp;
    private SimplePhysicsComponentData phy;
    private float bleedingTime;
    private float bleedingCounter;
    private float bleedingIntensity;

    private float thirstTimer;
    private Vector2 prevPosition;
    private float passedDistance;
    private int swampTimer = 0;
    private float brokenBonesDelay;

    public ServerFreeplayPlayerComponentData(PlayerData playerData,
                                             ServerFreeplayPlayerComponent contentComponent)
    {
        super(playerData, contentComponent);

        this.playerData = playerData;
        this.thirstTimer = 0;

        this.prevPosition = new Vector2();
        this.radioactivePredicate = activeData -> activeData.getComponent(RadioactiveComponentData.class) != null;
        this.windPredicate = activeData -> activeData.getComponent(WindComponentData.class) != null;

        this.tries = contentComponent.getTries();
    }

    private boolean checkWounded()
    {
        if (!playerData.isAlive())
            return false;

        if (!playerData.isWounded())
            return false;

        if (hasNotWoundedPartyMembers())
            return false;

        BrainOut.EventMgr.sendEvent(playerData, DestroyEvent.obtain());
        return true;
    }

    private boolean wound()
    {
        if (!hasNotWoundedPartyMembers())
            return false;

        if (tries <= 0)
            return false;

        if (playerData.isWounded())
            return false;

        HealthComponentData hcp = playerData.getComponentWithSubclass(HealthComponentData.class);

        if (hcp == null)
            return false;

        tries--;

        hcp.setGod(true);
        hcp.setHealth(getContentComponent().getWoundHealth());

        BrainOutServer.Controller.getClients().sendTCP(
                new ActiveDamageMsg(
                        playerData,
                        hcp.getHealth(),
                        playerData.getX(), playerData.getY(), 0,
                        null, "wounded"));

        TemperatureComponentData tcd = playerData.getComponent(TemperatureComponentData.class);
        tcd.setFreezing(0);

        playerData.setState(Player.State.wounded);
        playerData.setWounded(true);

        BrainOutServer.Controller.getClients().sendTCP(new PlayerWoundedMsg(playerData));

        PlayerBoostersComponentData boosters = playerData.getComponent(PlayerBoostersComponentData.class);

        if (boosters != null)
        {
            if (boosters.hasBooster("bleeding"))
                boosters.removeBooster("bleeding");

            boosters.setBooster("bleeding", 2, 9999);
        }

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc != null)
        {
            poc.setCurrentInstrument(null);

            BrainOutServer.Controller.getClients().sendTCP(
                new RemoteUpdateInstrumentMsg(playerData, poc.getCurrentInstrument(), poc.getHookedInstrument()));
        }

        godTimer = getContentComponent().getGodMode();

        return true;
    }

    @Override
    public void init()
    {
        super.init();

        fp = playerData.getComponent(FreeplayPlayerComponentData.class);

        this.phy = getComponentObject().getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (this.fp == null || this.phy == null)
            return;

        generateHunger();
        generateThirst();
    }

    private void generateThirst()
    {
        thirstTimer = fp.getContentComponent().getThirstTime();
    }

    private void generateHunger()
    {
        passedDistance = fp.getContentComponent().getHungerDistance();
        prevPosition.set(phy.getX(), phy.getY());
    }

    private void checkMovement()
    {
        if (fp.isHungry())
            return;

        float passedNow = prevPosition.dst(phy.getX(), phy.getY());
        prevPosition.set(phy.getX(), phy.getY());

        if (passedNow > 20 || passedNow < 1)
        {
            return;
        }

        if (passedDistance > 0)
        {
            passedDistance -= passedNow;

            if (passedDistance <= 0)
            {
                fp.consumeHunger(1);
                fp.updated(playerData, this::ownerOnly);

                passedDistance = 0;
            }
        }
        else
        {
            generateHunger();
        }

        prevPosition.set(phy.getX(), phy.getY());
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        Map map = playerData.getMap();

        if (map == null)
            return;

        if ( ! map.isSafeMap())
        {
            woundedCheckTimer -= dt;
            if (woundedCheckTimer < 0)
            {
                woundedCheckTimer = 1.0f;
                if (checkWounded())
                {
                    return;
                }
            }

            hungerCheckTask -= dt;
            if (hungerCheckTask < 0)
            {
                hungerCheckTask = 0.5f;
                checkMovement();
            }

            if (godTimer > 0)
            {
                godTimer -= dt;
                if (godTimer <= 0)
                {
                    HealthComponentData hcp = playerData.getComponentWithSubclass(HealthComponentData.class);

                    if (hcp != null)
                    {
                        hcp.setGod(false);
                    }
                }
            }

            if (bleedingTime > 0)
            {
                bleedingTime -= dt;
                bleedingCounter -= dt;

                if (bleedingCounter < 0)
                {
                    bleedingCounter = bleedingIntensity;

                    if (!playerData.isWounded())
                    {
                        HealthComponentData h = getComponentObject().getComponent(HealthComponentData.class);

                        if (h != null)
                        {
                            h.damage((DamageEvent) DamageEvent.obtain(1, playerData.getId(), null, null,
                                    playerData.getX(), playerData.getY(), 270, "bleeding"));
                        }
                    }
                }

                if (bleedingTime <= 0)
                {
                    stopBleeding();
                }
            }

            if (brokenBonesDelay > 0)
            {
                brokenBonesDelay -= dt;
            }

            thirstTimer -= dt;

            boolean sync = false;

            if (thirstTimer < 0)
            {
                fp.consumeThirst(1);
                fp.updated(playerData, this::ownerOnly);

                generateThirst();
                sync = true;
            }

            tmpTimer -= dt;
            if (tmpTimer < 0)
            {
                tmpTimer = 1.0f;
                sync |= updateTemperature();
            }

            if (sync)
            {
                fp.updated(playerData, this::ownerOnly);
            }
        }

        syncAnyWay -= dt;
        if (syncAnyWay < 0)
        {
            syncAnyWay = 20f;
            fp.sync();
        }

        cnt -= dt;

        if (cnt < 0)
        {
            cnt = 1.0f;

            updateSwamp();

            if (!hasRadioDefence())
            {
                ActiveData closest = map.getClosestActiveForTag(64, playerData.getX(), playerData.getY(),
                        ActiveData.class, Constants.ActiveTags.RADIOACTIVE, radioactivePredicate);

                if (closest != null)
                {
                    RadioactiveComponentData rad = closest.getComponent(RadioactiveComponentData.class);
                    float f = Interpolation.fade.apply(rad.func(playerData.getX(), playerData.getY(), 0.5f)) * rad.getPower();

                    if (f > 0)
                    {
                        fp.addRadio(f);
                        fp.updated(playerData, this::ownerOnly);
                    }
                }
            }

            if (map != null)
            {
                ActiveData closest = map.getClosestActiveForTag(64, playerData.getX(), playerData.getY(),
                        ActiveData.class, Constants.ActiveTags.WIND, windPredicate);

                if (closest != null)
                {
                    WindComponentData wind = closest.getComponent(WindComponentData.class);
                    float f = Interpolation.fade.apply(wind.func(playerData.getX(), playerData.getY(), 0.5f)) * wind.getPower();

                    if (f > 0)
                    {
                        ActiveData closestFire =
                            playerData.getMap().getClosestActiveForTag(8, playerData.getX(), playerData.getY(), ActiveData.class,
                            Constants.ActiveTags.CAMP_FIRE, (activeData) -> true);

                        if (closestFire == null)
                        {
                            fp.removeTemperature(f);
                            fp.updated(playerData, this::ownerOnly);
                        }
                    }
                }
            }

            if (fp.hasRadioMax())
            {
                BrainOutServer.EventMgr.sendDelayedEvent(playerData, DamageEvent.obtain(
                    1000.0f, -1, null, null, playerData.getX(), playerData.getY(),
                        MathUtils.random(360), "radio"));
            }
        }
    }

    private boolean updateTemperature()
    {
        if (playerData == null)
            return false;

        ActiveData closestFire =
            playerData.getMap().getClosestActiveForTag(8, playerData.getX(), playerData.getY(), ActiveData.class,
            Constants.ActiveTags.CAMP_FIRE, (activeData) -> true);

        boolean insideBuilding =
        !(
            playerData.getMap().getDimension().equals("default") ||
            playerData.getMap().getDimension().equals("forest") ||
            playerData.getMap().getDimension().equals("swamp2")
        );

        boolean fireNearby = closestFire != null;
        boolean sync;

        if (fireNearby)
        {
            sync = fp.setTemperature(fp.getTemperature() + 10.0f);
        }
        else if (((GameModeFree) BrainOutServer.Controller.getGameMode()).isNight())
        {
            if (insideBuilding)
            {
                sync = fp.setTemperature(fp.getTemperature() - 0.2f);
            }
            else
            {
                sync = fp.setTemperature(fp.getTemperature() - 0.4f);
            }
        }
        else
        {
            sync = fp.setTemperature(fp.getTemperature() + 0.4f);
        }

        updateFreezing();

        return sync;
    }

    private void updateFreezing()
    {
        TemperatureComponentData tmp = playerData.getComponent(TemperatureComponentData.class);

        if (tmp != null)
        {
            if (fp.getTemperature() <= 5)
            {
                HealthComponentData h = playerData.getComponentWithSubclass(HealthComponentData.class);

                if (h.getHealth() > 50 && !h.isGod())
                {
                    float sub = Math.min(5, h.getHealth() - 50);
                    if (sub > 0)
                    {
                        tmp.setFreezing(tmp.getFreezing() + sub);
                        tmp.updated(playerData);

                        BrainOut.EventMgr.sendDelayedEvent(playerData, DamageEvent.obtain(
                                sub, -1, null, null, playerData.getX(), playerData.getY(), 0, "cold")
                        );
                    }
                }
            }
            else if (!fp.isCold() && tmp.getFreezing() > 0)
            {
                HealthComponentData h = playerData.getComponentWithSubclass(HealthComponentData.class);

                if (!h.isMaxHealth())
                {
                    float sub = Math.min(5, h.getInitHealth() - h.getHealth());
                    if (sub > 0)
                    {
                        h.setHealth(Math.min(h.getInitHealth(), h.getHealth() + sub));
                        tmp.setFreezing(tmp.getFreezing() - sub);
                        tmp.updated(playerData, this::ownerOnly);
                        h.updated(playerData, this::ownerOnly);
                    }
                }
            }
        }
    }

    public void fullRecovery()
    {
        TemperatureComponentData tcd = playerData.getComponent(TemperatureComponentData.class);
        tcd.setFreezing(0);
        tcd.updated(playerData, this::ownerOnly);

        HealthComponentData h = playerData.getComponentWithSubclass(HealthComponentData.class);
        h.setHealth(h.getInitHealth());
        h.updated(playerData, this::ownerOnly);

        bleedingCounter = 0;
        bleedingTime = 0;

        fp.fullRecover();
    }

    private boolean ownerOnly(int owner)
    {
        return owner == playerData.getOwnerId();
    }

    private void updateSwamp()
    {
        FreeplayPlayerComponentData fp = playerData.getComponent(FreeplayPlayerComponentData.class);
        if (fp == null || !fp.isSwamp())
        {
            swampTimer = 0;
            return;
        }

        int ownerId = playerData.getOwnerId();

        Client myClient = BrainOutServer.Controller.getClients().get(ownerId);
        if (!(myClient instanceof PlayerClient))
            return;

        PlayerClient myPlayerClient = ((PlayerClient) myClient);

        myPlayerClient.moveTo(playerData.getDimension(), playerData.getX(), playerData.getY() - 0.1f);
        swampTimer++;

        if (getContentComponent().getSwampEffect() != null && swampTimer % 4 == 0)
        {
            BrainOutServer.Controller.getClients().sendUDP(
                new LaunchEffectMsg(playerData.getDimension(), playerData.getX(), playerData.getY(),
                        getContentComponent().getSwampEffect()));
        }

        if (swampTimer > 28)
        {
            BrainOutServer.EventMgr.sendEvent(playerData, DestroyEvent.obtain());
            swampTimer = 0;
        }
    }

    private boolean hasRadioDefence()
    {
        if (playerData != null)
        {
            PlayerBoostersComponentData bst = playerData.getComponent(PlayerBoostersComponentData.class);

            if (bst != null)
            {
                PlayerBoostersComponentData.Booster radx = bst.getBooster("radx");

                if (radx != null && radx.valid())
                {
                    return true;
                }
            }
        }

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
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case onZeroHealth:
            {
                return wound();
            }

            case activeActivateData:
            {
                ActivateActiveEvent ev = ((ActivateActiveEvent) event);
                return activate(ev.client, ev.playerData);
            }
            case physicsContact:
            {
                if (fp == null)
                    return false;

                PhysicsContactEvent ev = ((PhysicsContactEvent) event);
                contact(ev);

                break;
            }
        }

        return false;
    }

    private boolean activate(Client client, PlayerData activator)
    {
        if (activateSwamp(client, activator))
            return true;

        if (activateWounded(client, activator))
            return true;

        return false;
    }

    private boolean activateSwamp(Client myClient, PlayerData activator)
    {
        if (!(getComponentObject() instanceof PlayerData))
            return false;

        if (!playerData.isAlive())
            return false;

        FreeplayPlayerComponentData fp = playerData.getComponent(FreeplayPlayerComponentData.class);
        if (fp == null)
            return false;

        if (!fp.isSwamp())
            return false;

        if (!activator.isAlive())
            return false;

        if (activator.isWounded())
            return false;

        if (!(myClient instanceof PlayerClient))
            return false;

        PlayerClient myPlayerClient = ((PlayerClient) myClient);

        String myPartyId = myPlayerClient.getPartyId();

        if (myPartyId == null || myPartyId.isEmpty())
            return false;

        int otherOwnerId = activator.getOwnerId();

        Client otherClient = BrainOutServer.Controller.getClients().get(otherOwnerId);

        if (!(otherClient instanceof PlayerClient))
            return false;

        PlayerClient otherPlayerClient = ((PlayerClient) otherClient);

        if (!myPartyId.equals(otherPlayerClient.getPartyId()))
            return false;

        ActiveProgressComponentData progress = activator.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return false;

        otherPlayerClient.enablePlayer(false);

        {
            SimplePhysicsComponentData phy = activator.getComponentWithSubclass(SimplePhysicsComponentData.class);

            if (phy != null)
            {
                phy.getSpeed().set(0, 0);
                otherPlayerClient.getServerPlayerController().sendPlayerData(false, 0);
            }
        }

        progress.startCancellable(getContentComponent().getReviveTime(),
        () -> {
            otherPlayerClient.enablePlayer(true);
            myPlayerClient.enablePlayer(true);

            SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);
            if (phy != null)
            {
                phy.setEnabled(true);
                phy.updated(playerData);
            }

            myPlayerClient.moveTo(activator.getDimension(), activator.getX(), activator.getY());


            fp.setSwamp(false);
            fp.sync();
        },
        () -> {
            otherPlayerClient.enablePlayer(true);
        });

        return true;
    }

    private boolean activateWounded(Client myClient, PlayerData activator)
    {
        if (!(getComponentObject() instanceof PlayerData))
            return false;

        if (!playerData.isAlive())
            return false;

        if (!playerData.isWounded())
            return false;

        if (!activator.isAlive())
            return false;

        if (activator.isWounded())
            return false;

        if (!(myClient instanceof PlayerClient))
            return false;

        PlayerClient myPlayerClient = ((PlayerClient) myClient);

        String myPartyId = myPlayerClient.getPartyId();

        int otherOwnerId = activator.getOwnerId();

        Client otherClient = BrainOutServer.Controller.getClients().get(otherOwnerId);

        if (!(otherClient instanceof PlayerClient))
            return false;

        PlayerClient otherPlayerClient = ((PlayerClient) otherClient);

        if (!((FreePayload) otherPlayerClient.getModePayload()).isFriend(myClient))
        {
            if (myPartyId == null || !myPartyId.equals(otherPlayerClient.getPartyId()))
                return false;
        }

        ActiveProgressComponentData progress = activator.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return false;

        myPlayerClient.enablePlayer(false);
        otherPlayerClient.enablePlayer(false);

        {
            SimplePhysicsComponentData phy = activator.getComponentWithSubclass(SimplePhysicsComponentData.class);

            if (phy != null)
            {
                phy.getSpeed().set(0, 0);
                otherPlayerClient.getServerPlayerController().sendPlayerData(false, 0);
            }
        }

        {
            SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            if (phy != null)
            {
                phy.getSpeed().set(0, 0);
                myPlayerClient.getServerPlayerController().sendPlayerData(false, 0);
            }
        }

        progress.startCancellable(getContentComponent().getReviveTime(),
        () -> {
            myPlayerClient.enablePlayer(true);
            otherPlayerClient.enablePlayer(true);

            ModePayload payload = otherPlayerClient.getModePayload();
            if (payload instanceof FreePayload)
            {
                FreePayload freePayload = ((FreePayload) payload);
                freePayload.questEvent(FreePlayPartnerRevivedEvent.obtain(otherPlayerClient, myPlayerClient));
            }

            HealthComponentData hcp = playerData.getComponentWithSubclass(HealthComponentData.class);

            if (hcp != null)
            {
                PlayerBoostersComponentData boosters = playerData.getComponent(PlayerBoostersComponentData.class);

                if (boosters != null)
                {
                    if (boosters.hasBooster("bleeding"))
                        boosters.removeBooster("bleeding");

                    boosters.setBooster("bleeding", 5, 25);
                }

                hcp.setHealth(getContentComponent().getRestoredHealth());

                TemperatureComponentData tcd = playerData.getComponent(TemperatureComponentData.class);
                tcd.setFreezing(0);
                tcd.updated(playerData, this::ownerOnly);

                BrainOutServer.Controller.getClients().sendTCP(
                        new ActiveDamageMsg(
                                playerData,
                                hcp.getHealth(),
                                playerData.getX(), playerData.getY(), 0,
                                null, "unwounded"));
            }

            playerData.setState(Player.State.normal);
            playerData.setWounded(false);

            BrainOutServer.Controller.getClients().sendTCP(
                    new PlayerWoundedMsg(playerData));

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            ServerPlayerControllerComponentData ctl =
                playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

            if (poc != null && ctl != null)
            {
                ctl.selectFirstInstrument(poc);
                ctl.updateAttachments();
                ctl.consumablesUpdated();
            }

        },
        () -> {
            myPlayerClient.enablePlayer(true);
            otherPlayerClient.enablePlayer(true);
        });

        return true;
    }


    private boolean hasNotWoundedPartyMembers()
    {
        int ownerId = playerData.getOwnerId();

        Client myClient = BrainOutServer.Controller.getClients().get(ownerId);

        if (!(myClient instanceof PlayerClient))
            return false;

        PlayerClient myPlayerClient = ((PlayerClient) myClient);

        String myPartyId = myPlayerClient.getPartyId();

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (!(client instanceof PlayerClient))
                continue;

            PlayerClient playerClient = ((PlayerClient) client);

            if (playerClient == myPlayerClient)
                continue;

            if (playerClient.getModePayload() == null)
                continue;

            if (!((FreePayload) playerClient.getModePayload()).isFriend(myClient))
            {
                String partyId = playerClient.getPartyId();

                if (partyId == null || !partyId.equals(myPartyId))
                    continue;
            }

            if (!playerClient.isAlive())
                continue;

            PlayerData playerData = playerClient.getPlayerData();

            if (playerData == null)
                continue;

            if (playerData.isWounded())
                continue;

            return true;
        }

        return false;
    }

    public boolean setBleeding(float intensity, float time)
    {
        if (brokenBonesDelay > 0 && fp.isBleeding())
            return false;

        bleedingTime = Math.max(time, bleedingTime);
        bleedingCounter = 0;

        if (bleedingIntensity != 0)
        {
            bleedingIntensity = Math.min(bleedingIntensity, intensity);
        }
        else
        {
            bleedingIntensity = intensity;
        }

        fp.setBleeding(true);
        return true;
    }

    public void sync()
    {
        fp.sync();;
    }

    public boolean isBleeding()
    {
        return bleedingTime > 0;
    }

    public void stopBleeding()
    {
        bleedingCounter = 0;
        bleedingTime = 0;
        fp.setBleeding(false);
        fp.updated(playerData);
    }

    private void contact(PhysicsContactEvent ev)
    {
        if (brokenBonesDelay > 0)
            return;

        brokenBonesDelay = 1.0f;

        float speed = ev.speed.len();

        if (speed < getContentComponent().getBonesSpeed())
            return;

        float damage = (speed - getContentComponent().getBonesSpeed()) * getContentComponent().getBonesDamage();

        ActiveData playerData = ev.activeData;

        BrainOut.EventMgr.sendDelayedEvent(playerData, DamageEvent.obtain(
            damage, -1, null, null, playerData.getX(), playerData.getY(), 0, "fall")
        );

        if (getContentComponent().getBonesEffect() != null && !getContentComponent().getBonesEffect().isEmpty())
        {
            BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
                playerData.getDimension(), playerData.getX(), playerData.getY(), getContentComponent().getBonesEffect()
            ));
        }

        fp.setBonesBroken(true);

        setBleeding(getContentComponent().getBonesBleedingIntensity(),
            getContentComponent().getBonesBleedingDuration());

        fp.sync();
    }
}
