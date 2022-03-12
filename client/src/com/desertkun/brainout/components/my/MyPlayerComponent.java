package com.desertkun.brainout.components.my;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.common.msg.client.*;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.WeaponSlotComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.InstrumentAimComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlyingTextData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.effect.DamageEffectData;
import com.desertkun.brainout.data.instrument.*;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.graphics.CenterSprite;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeRealization;

import java.util.Comparator;

public class MyPlayerComponent extends PlayerControllerComponentData
{
    private Vector2 screenCenter, prevWatchPos;

    private float hitTimer, watchUpdateTimer, updateDirectionTimer;
    private float prevHealth;
    private int posX, posY;

    private int currentSlot, previousSlot;
    private String currentMode, previousMode;
    private CenterSprite hitSprite;

    private boolean sitFlag, runFlag, squatFlag;
    private CenterSprite hitSpriteBody;
    private CenterSprite hitSpriteHead;
    private boolean controller;

    public MyPlayerComponent(final PlayerData playerData)
    {
        super(playerData, null);

        hitTimer = 0;
        watchUpdateTimer = 0;
        screenCenter = new Vector2();
        prevWatchPos = new Vector2();
        previousSlot = -1;
        currentSlot = 0;
        currentMode = Constants.Properties.SLOT_PRIMARY;
        previousMode = null;
    }

    @Override
    protected void sendAim(boolean aim)
    {
        BrainOutClient.ClientController.sendUDP(new PlayerAimMsg(aim));

        BrainOut.EventMgr.sendEvent(PlayerAimEvent.obtain(aim));
    }

    @Override
    protected void sendState(Player.State state)
    {
        BrainOutClient.ClientController.sendUDP(new PlayerStateMsg(state));
    }

    @Override
    protected void sendPlayerData(boolean spectatorsOnly, int priority)
    {
        PlayerData playerData = getPlayerData();

        PlayerComponentData pcd = playerData.getComponent(ClientPlayerComponent.class);

        BrainOutClient.ClientController.sendUDP(new PlayerMoveMsg(
            playerData.getX(),
            playerData.getY(),
            getOriginalDirection(),
            pcd.getMousePosition().x, pcd.getMousePosition().y));
    }

    @Override
    protected void updateMoveDirection()
    {
        super.updateMoveDirection();

        //this.getMoveDirection().scl(1.5f);
    }

    private void updatePlayerData()
    {
        BrainOut.EventMgr.sendDelayedEvent(MyPlayerSetEvent.obtain(getPlayerData()));
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case componentUpdated:
            {
                ComponentUpdatedEvent ev = ((ComponentUpdatedEvent) event);

                if (ev.component instanceof PlayerBoostersComponentData)
                {
                    BrainOutClient.EventMgr.sendDelayedEvent(
                        SimpleEvent.obtain(SimpleEvent.Action.playerInfoUpdated));
                }

                break;
            }
            case consumable:
            {
                PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);

                if (poc == null)
                    return false;

                ConsumableEvent consumableEvent = ((ConsumableEvent) event);
                ConsumableRecord record = consumableEvent.record;

                switch (consumableEvent.action)
                {
                    case added:
                    {
                        initRecord(record);

                        if (poc.getCurrentInstrument() == null && record.getItem() instanceof InstrumentConsumableItem)
                        {
                            switchInstrument(record, null);
                        }

                        break;
                    }

                    case removed:
                    {
                        if (record == poc.getCurrentInstrumentRecord())
                        {
                            switchInstrument(null, null);
                        }

                        break;
                    }
                }

                break;
            }

            case selectPreviousSlot:
            {
                if (currentSlot >= 0 && previousSlot >= 0)
                {
                    if (selectSlot(previousSlot, previousMode))
                    {
                        int tmp = previousSlot;
                        previousSlot = currentSlot;
                        currentSlot = tmp;

                        String tmp2 = previousMode;
                        previousMode = currentMode;
                        currentMode = tmp2;
                    }
                }

                break;
            }

            case selectSlot:
            {
                SelectSlotEvent e = ((SelectSlotEvent) event);

                if (selectSlot(e.slot, e.mode))
                {
                    previousSlot = currentSlot;
                    currentSlot = e.slot;

                    previousMode = currentMode;
                    currentMode = e.mode;

                    return true;
                }

                break;
            }

            case gameController:
            {
                GameControllerEvent gcEvent = (GameControllerEvent) event;

                switch (gcEvent.action)
                {
                    case switchWeapon:
                    {
                        switchInstrument();
                        updatePlayerData();

                        break;
                    }

                    case move:
                    {
                        setMoveDirection(gcEvent.data);

                        break;
                    }

                    case aim:
                    {
                        controller = gcEvent.flag;
                        setAimDirection(gcEvent.data, false);

                        break;
                    }

                    case absoluteAim:
                    {
                        setAimDirection(gcEvent.data, true);

                        break;
                    }

                    case dropInstrument:
                    {
                        dropInstrument();

                        break;
                    }

                    case dropAmmo:
                    {
                        dropAmmo();

                        break;
                    }

                    case beginSit:
                    {
                        squatFlag = false;

                        if (state == Player.State.run && runFlag)
                        {
                            setPositionMode(PositionMode.crouch);
                        }
                        else
                        {
                            setPositionMode(PositionMode.sit);
                        }

                        sitFlag = true;

                        return true;
                    }

                    case squat:
                    {
                        if (BrainOutClient.ClientController.isFreePlay()) {
                            if (state == Player.State.squat) {
                                setPositionMode(PositionMode.normal);
                                squatFlag = false;
                            } else {
                                setPositionMode(PositionMode.squat);
                                squatFlag = true;
                            }
                        }


                        return true;
                    }

                    case endSit:
                    {
                        squatFlag = false;

                        setPositionMode(PositionMode.normal);
                        sitFlag = false;

                        return true;
                    }

                    case beginRun:
                    {
                        squatFlag = false;

                        if (state == Player.State.sit)
                        {
                            setPositionMode(PositionMode.crouch);
                        }
                        else
                        {
                            setRun(true);
                        }

                        runFlag = true;

                        return true;
                    }

                    case endRun:
                    {
                        squatFlag = false;

                        if (state == Player.State.crawl && sitFlag)
                        {
                            setPositionMode(PositionMode.sit);
                        }
                        else
                        {
                            setRun(false);
                        }

                        runFlag = false;

                        return true;
                    }

                    case switchZoom:
                    {
                        switchZoom();

                        return true;
                    }

                    case activate:
                    {
                        if (activate())
                        {
                            return true;
                        }

                        InstrumentData currentInstrument = getPlayerData().getCurrentInstrument();
                        if (currentInstrument != null)
                        {
                            if (currentInstrument.onEvent(event))
                            {
                                return true;
                            }
                        }
                    }
                }

                break;
            }

            case hitConfirmed:
            {
                HitConfirmEvent e = ((HitConfirmEvent) event);

                showHitMarker(e.collider, e.x, e.y, e.dmg, e.d);

                break;
            }

            case damaged:
            {
                DamagedEvent damaged = (DamagedEvent) event;

                // someone hit us
                if (damaged.data == getPlayerData())
                {
                    showDamageMarker(damaged.x, damaged.y, damaged.angle);

                    if (damaged.content instanceof Bullet)
                    {
                        Bullet bullet = ((Bullet) damaged.content);

                        ClientMap map = ((ClientMap) getMap());

                        if (map != null)
                            map.shake(bullet.getHitShake());
                    }

                    updatePlayerData();
                }

                break;
            }

            case ammoLoaded:
            {
                PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);

                ConsumableContainer container = poc.getConsumableContainer();

                AmmoLoadedEvent ale = ((AmmoLoadedEvent) event);

                ConsumableRecord bullets = container.get(ale.bulletsId);

                if (bullets != null && bullets.getItem().getContent() instanceof Bullet) {

                    if (bullets.getAmount() - ale.ammoCount > 0)
                    {
                        bullets.setAmount(bullets.getAmount() - ale.ammoCount);
                    }
                    else if (bullets.getAmount() - ale.ammoCount <= 0)
                    {
                        container.removeRecord(bullets);
                    }

                    container.updateWeight();

                    BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.consumablesUpdated));
                }

                break;
            }
        }

        return false;
    }

    private ClientActiveActivatorComponentData getClosestActivator()
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return null;

        Map map = getMap();

        if (map == null)
            return null;

        ActiveData closestActive = map.getClosestActive(16,
            getPlayerData().getX(), getPlayerData().getY(),
            ActiveData.class, activeData ->
        {
            if (activeData == playerData)
                return false;

            ClientActiveActivatorComponentData aa =
                    activeData.getComponentWithSubclass(ClientActiveActivatorComponentData.class);
            if (aa == null)
                return false;

            return aa.test(playerData);
        });

        if (closestActive == null)
            return null;

        return closestActive.getComponentWithSubclass(ClientActiveActivatorComponentData.class);
    }

    private boolean activateClosestActive()
    {
        ClientActiveActivatorComponentData activator = getClosestActivator();

        return activator != null && activator.activate(getPlayerData());
    }

    private boolean activate()
    {
        ActiveProgressVisualComponentData pvc = getPlayerData().getComponent(ActiveProgressVisualComponentData.class);

        if (pvc != null)
        {
            if (pvc.isActive() && pvc.isCancellable())
            {
                BrainOutClient.ClientController.sendTCP(new CancelPlayerProgressMsg());
                return true;
            }
        }

        return activateClosestActive();
    }

    private boolean selectSlot(int slot, String mode)
    {
        if (getPlayerData().isWounded())
            return false;

        if (!isEnabled())
            return false;

        if (slot >= ClientConstants.Inventory.SLOTS.size)
            return false;

        String slotName = ClientConstants.Inventory.SLOTS.get(slot);
        Slot slotContent = ((Slot) BrainOutClient.ContentMgr.get(slotName));

        if (slotContent == null)
            return false;

        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return false;

        if (poc.getCurrentInstrumentRecord() == null)
            return false;

        PlayerComponentData pc = getPlayerData().getComponent(PlayerComponentData.class);
        if (pc != null && pc.isPlayingCustomAnimation())
        {
            return false;
        }

        ConsumableRecord current = poc.getCurrentInstrumentRecord();
        ConsumableRecord next = poc.getNextInstrumentForSlot(slotContent, current);

        if (current != null && current == next)
        {
            if (currentMode != null && currentMode.equals(mode))
                return false;

            if (current.getItem() instanceof InstrumentConsumableItem)
            {
                InstrumentData instrumentData = ((InstrumentConsumableItem) current.getItem()).getInstrumentData();
                MyWeaponComponent mwc = instrumentData.getComponent(MyWeaponComponent.class);
                if (mwc != null && mwc.getSlots() != null && mode != null)
                {
                    if (!mwc.getSlots().containsKey(mode))
                        return false;
                }
            }
        }

        return next != null && selectRecord(next, mode);
    }

    public boolean selectRecord(ConsumableRecord record, String mode)
    {
        boolean result = true;

        if (record.getItem() instanceof InstrumentConsumableItem)
        {
            InstrumentConsumableItem item = ((InstrumentConsumableItem) record.getItem());

            ConsumableContent.SelectKind selectKind = item.getContent().getSelectKind();

            switch (selectKind)
            {
                case selectable:
                {
                    switchInstrument(record, mode);
                    updatePlayerData();

                    result = true;
                    break;
                }
                case canBeActivated:
                {
                    BrainOut.EventMgr.sendDelayedEvent(item.getInstrumentData(),
                            ActivateInstrumentEvent.obtain(record));

                    result = false;
                    break;
                }
                case disabled:
                {
                    // ignore
                    result = false;
                    break;
                }
            }
        }

        updateMoveDirection();

        return result;
    }

    private void switchZoom()
    {
        ClientPlayerComponent cpc = getPlayerData().getComponent(ClientPlayerComponent.class);

        if (cpc.getScale() == 1.0f)
            cpc.setScale(0.75f);
        else if (cpc.getScale() == 0.75f)
            cpc.setScale(0.5f);
        else cpc.setScale(1.0f);
    }

    private void initRecord(ConsumableRecord record)
    {
        if (record.getItem() instanceof InstrumentConsumableItem)
        {
            initInstrument(((InstrumentConsumableItem) record.getItem()).getInstrumentData(), record);
        }
    }

    private boolean dropAmmo()
    {
        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return false;

        if (poc.getCurrentInstrument() != null)
        {
            InstrumentData instrumentData = poc.getCurrentInstrument();
            if (instrumentData instanceof WeaponData)
            {
                WeaponData weaponData = ((WeaponData) instrumentData);
                MyWeaponComponent myWeaponComponent = weaponData.getComponent(MyWeaponComponent.class);
                if (myWeaponComponent != null)
                {
                    WeaponSlotComponent slot = myWeaponComponent.getCurrentSlot();

                    if (slot == null || slot.getBullet() == null)
                        return false;

                    if (slot.getBullet().isDropable())
                    {
                        ConsumableRecord record = poc.getConsumableContainer().getConsumable(slot.getBullet());

                        if (record != null)
                        {
                            return dropItem(record, slot.getBullet().getDropAtOnce(),
                                    getPlayerData().getAngle());
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean dropInstrument()
    {
        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return false;

        if (poc.getCurrentInstrument() != null)
        {
            return dropItem(poc.getCurrentInstrumentRecord(), 1,
                    getPlayerData().getAngle());
        }

        return false;
    }

    private void setAimDirection(Vector2 mousePos, boolean absolute)
    {
        PlayerData playerData = getPlayerData();

        ClientPlayerComponent pcd = playerData.getComponent(ClientPlayerComponent.class);

        Vector2 pointPos = pcd.getMousePosition();

        if (playerData.getCurrentInstrument() != null)
        {
            ClientPlayerComponent cpc = playerData.getComponent(ClientPlayerComponent.class);
            float maxAimDist = 0;

            if (playerData.getCurrentInstrument() != null &&
                playerData.getCurrentInstrument().getInstrument().hasComponent(InstrumentAimComponent.class))
            {
                InstrumentAimComponent aim = playerData.getCurrentInstrument().getInstrument().getComponent(InstrumentAimComponent.class);
                maxAimDist = aim.getAimDistance();
            }
            else
            if (playerData.getCurrentInstrument() instanceof WeaponData)
            {
                WeaponData weaponData = ((WeaponData) playerData.getCurrentInstrument());
                MyWeaponComponent myWeaponComponent = weaponData.getComponent(MyWeaponComponent.class);

                if (myWeaponComponent != null)
                {
                    WeaponSlotComponent slot = myWeaponComponent.getCurrentSlot();

                    if (slot == null)
                        return;

                    maxAimDist = slot.getAimDistance().asFloat();
                }
            }

            WeaponAnimationComponentData cwcd =
                playerData.getCurrentInstrument().getComponent(WeaponAnimationComponentData.class);

            if (cwcd != null)
            {
                screenCenter.set(cwcd.getInstrumentLaunch().getX(), cwcd.getInstrumentLaunch().getY());
            }

            ClientMap.getMouseScale(mousePos.x, -mousePos.y, tmp);

            tmp.scl(BrainOutClient.ClientSett.getMouseSensitivity() * ClientConstants.Player.SENSITIVITY_MULTIPLIER);
            if (absolute)
            {
                pointPos.set(tmp.x, tmp.y);
            }
            else
            {
                pointPos.add(tmp.x, tmp.y);
            }
            tmp.set(pointPos);

            if (playerData.getCurrentInstrument() instanceof PlaceBlockData)
            {
                PlaceBlockData pb = ((PlaceBlockData) playerData.getCurrentInstrument());
                tmp.clamp(0, pb.getPlaceBlock().getMaxDistance());
            }

            posX = (int)(playerData.getX() + tmp.x);
            posY = (int)(playerData.getY() + tmp.y);

            float h = (Math.min(BrainOutClient.getWidth(), BrainOutClient.getHeight()) / Constants.Graphics.RES_SIZE) * 0.4f;

            pointPos.clamp(0, maxAimDist + h);
        }
        else
        {
            ClientMap.getMouseScale(mousePos.x, -mousePos.y, tmp);

            tmp.scl(BrainOutClient.ClientSett.getMouseSensitivity() * ClientConstants.Player.SENSITIVITY_MULTIPLIER);
            pointPos.add(tmp.x, tmp.y);
            tmp.set(pointPos);

            float h = (Math.min(BrainOutClient.getWidth(), BrainOutClient.getHeight()) / Constants.Graphics.RES_SIZE) * 0.4f;

            pointPos.clamp(0, 20 + h);
        }

        playerData.setAngle(pointPos.angleDeg());
    }

    public boolean isControllerUsed()
    {
        return controller;
    }

    private void showDamageMarker(float x, float y, float angle)
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        PlayerData playerData = getPlayerData();

        DamageEffectData ded = new DamageEffectData(new PointLaunchData(
                playerData.getX(), playerData.getY(), angle, playerData.getDimension()),
                "damage-marker", ClientConstants.Client.DAMAGE_TIMER);

        map.addEffect(ded);
    }

    private void showHitMarker(String collider, float x, float y, int dmg, int d)
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        PlayerData playerData = getPlayerData();

        if (d >= 0 && collider != null && collider.equals("block"))
        {
            FlyingTextData flyingTextData = new FlyingTextData(String.valueOf(dmg), x, y,
                Map.FindDimension(d), "title-ingame-red");

            map.addActive(map.generateClientId(), flyingTextData, true);
        }

        boolean head = collider != null && collider.equals("head");
        hitSprite = head ? hitSpriteHead : hitSpriteBody;
        hitTimer = ClientConstants.Client.HIT_TIMER;
        String markerId = head ? "hit-marker-head-snd" : "hit-marker-snd";
        SoundEffect soundEffect = (SoundEffect)BrainOut.ContentMgr.get(markerId);
        map.addEffect(soundEffect, playerData.getLaunchData());
    }

    @Override
    public void init()
    {
        super.init();

        updatePlayerData();

        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
        BrainOut.EventMgr.subscribe(Event.ID.damaged, this);
        BrainOut.EventMgr.subscribe(Event.ID.hitConfirmed, this);
        BrainOut.EventMgr.subscribe(Event.ID.ammoLoaded, this);

        switchInstrument();

        ClientPlayerComponent cpc = getPlayerData().getComponent(ClientPlayerComponent.class);

        if (cpc != null)
        {
            hitSpriteBody = new CenterSprite(BrainOutClient.getRegion("hit-marker"), cpc.getMouseLaunchData());
            hitSpriteHead = new CenterSprite(BrainOutClient.getRegion("hit-marker-head"), cpc.getMouseLaunchData());

            this.hitSprite = hitSpriteBody;
            cpc.setDisplayAim(true);
            cpc.setLerpAim(false);
        }

        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            Gdx.app.postRunnable(() ->
                    updateInstrument(poc.getCurrentInstrument(), null));
        }
    }

    @Override
    public void release()
    {
        ClientMap clientMap = ((ClientMap) getMap());

        if (clientMap != null)
            clientMap.getComponents().removeComponent(this, false);

        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.damaged, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.hitConfirmed, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.ammoLoaded, this);

        super.release();
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        if (hitTimer > 0 && hitSprite != null)
        {
            hitSprite.setAlpha(hitTimer / ClientConstants.Client.HIT_TIMER);
            hitSprite.draw(batch);
        }
    }

    public void switchInstrument()
    {
        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);

        if (poc == null) return;

        Array<ConsumableRecord> records = poc.getConsumableContainer().queryRecords(
            record -> record.getItem() instanceof InstrumentConsumableItem
        );

        records.sort(Comparator.comparingInt(InstrumentConsumableItem::SortRecords));

        if (records.size == 0)
        {
            switchInstrument(null, null);

            return;
        }

        int index = records.indexOf(poc.getCurrentInstrumentRecord(), true);

        if (index == -1)
        {
            switchInstrument(records.get(0), null);
        }
        else
        {
            if (index >= records.size - 1)
            {
                switchInstrument(records.get(0), null);
            }
            else
            {
                switchInstrument(records.get(index + 1), null);
            }
        }

        updateInstrument(poc.getCurrentInstrument(), null);
    }

    public void switchInstrument(ConsumableRecord switchTo, String mode)
    {
        PlayerData playerData = getPlayerData();
        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc.getCurrentInstrument() != null)
        {
            if (poc.getCurrentInstrument().isForceSelect())
            {
                return;
            }

            if (poc.getCurrentInstrument() instanceof WeaponData)
            {
                WeaponData weaponData = ((WeaponData) poc.getCurrentInstrument());

                MyWeaponComponent mwp = weaponData.getComponent(MyWeaponComponent.class);
                if (mwp != null)
                {
                    switch(mwp.getCurrentSlot().getState())
                    {
                        case reloadingBoth:
                        {
                            // can't escape this
                            return;
                        }
                        case loadMagazineRound:
                        {
                            mwp.getCurrentSlot().stopLoadingBullets();
                            BrainOutClient.ClientController.sendTCP(new SelectInstrumentMsg(switchTo));
                            return;
                        }
                    }
                }
            }
        }

        poc.setCurrentInstrument(switchTo);

        if (switchTo == null)
        {
            selectFirstInstrument(poc);
        }

        playerData.setCurrentInstrument(poc.getCurrentInstrument());
        BrainOutClient.ClientController.sendTCP(new SelectInstrumentMsg(switchTo));
        updateInstrument(poc.getCurrentInstrument(), mode);
    }

    private void updateInstrument(InstrumentData instrument, String mode)
    {
        ClientPlayerComponent cpc = getPlayerData().getComponent(ClientPlayerComponent.class);

        if (cpc != null)
        {
            cpc.updateInstrument(instrument, mode);
        }

        updateMoveDirection();
    }

    @Override
    protected boolean isEnabled()
    {
        PlayerData playerData = getPlayerData();

        if (playerData != null)
        {
            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

            if (poc != null)
            {
                return poc.isEnabled();
            }
        }

        return super.isEnabled();
    }

    @Override
    public void update(float dt)
    {
        if (!isEnabled())
            return;

        if (hitTimer > 0)
        {
            hitTimer -= dt;
        }

        PlayerData playerData = getPlayerData();

        PlayerStatsComponentData hcd = playerData.getComponent(PlayerStatsComponentData.class);

        if (hcd != null)
        {
            if (hcd.getHealthValue() != prevHealth)
            {
                prevHealth = hcd.getHealthValue();

                BrainOut.EventMgr.sendEvent(SimpleEvent.obtain(SimpleEvent.Action.playerInfoUpdated));
            }
        }

        super.update(dt);

        ClientPlayerComponent cpc = playerData.getComponent(ClientPlayerComponent.class);

        if (cpc != null)
        {
            // check if we not anymore "don't wanna sit but can't"
            if ((state != Player.State.sit) && (cpc.getState() == Player.State.sit) && !hasTopContact())
            {
                SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

                // add impulse so physics will detect head (since standing up is moving up)
                phy.addImpulse(0, 0.01f);

                cpc.setState(Player.State.normal);
                cpc.updateBottomAnimation();
                updateMoveDirection();
            }

            updateDirectionTimer -= dt;

            if (updateDirectionTimer < 0)
            {
                updateDirectionTimer = 0.25f;

                updateMoveDirection();
            }

            if (watchUpdateTimer < 0)
            {
                if (prevWatchPos.dst(cpc.getWatchX(), cpc.getWatchY()) > 1)
                {
                    prevWatchPos.set(cpc.getWatchX(), cpc.getWatchY());

                    BrainOutClient.ClientController.setWatchingPoint(prevWatchPos);
                }

                watchUpdateTimer = 1.0f;
            }
            else
            {
                watchUpdateTimer -= dt;
            }
        }
    }

    public void initInstrument(InstrumentData id, ConsumableRecord record)
    {
        if (id instanceof WeaponData)
        {
            WeaponData wd = ((WeaponData) id);

            if (wd.getComponent(MyWeaponComponent.class) == null)
            {
                MyWeaponComponent mwc = new MyWeaponComponent(wd, record);
                wd.addComponent(mwc);
                mwc.init();
            }
        }

        if (id instanceof ActivateInstrumentData)
        {
            ActivateInstrumentData td = ((ActivateInstrumentData) id);

            MyActivatorComponent mdc = new MyActivatorComponent(td, record);
            td.addComponent(mdc);
            mdc.init();
        }

        if (id instanceof GrenadeData)
        {
            GrenadeData td = ((GrenadeData) id);

            MyGrenadeComponent mdc = new MyGrenadeComponent(td, record);
            td.addComponent(mdc);
            mdc.init();
        }

        if (id instanceof ThrowableInstrumentData)
        {
            ThrowableInstrumentData td = ((ThrowableInstrumentData) id);

            MyThrowableComponent mdc = new MyThrowableComponent(td, record);
            td.addComponent(mdc);
            mdc.init();
        }

        if (id instanceof BoxData)
        {
            BoxData pd = ((BoxData) id);

            MyBoxComponent mdc = new MyBoxComponent(pd, record);
            pd.addComponent(mdc);
            mdc.init();
        }
        else
        if (id instanceof PlaceBlockData)
        {
            PlaceBlockData pd = ((PlaceBlockData) id);

            MyPlaceComponent mdc = new MyPlaceComponent(pd, record);
            pd.addComponent(mdc);
            mdc.init();
        }

        if (id.getInstrument().isForceSelect())
        {
            switchInstrument(record, Constants.Properties.SLOT_PRIMARY);
        }
    }

    public boolean dropItem(ConsumableRecord cnt, int amount, float angle)
    {
        PlayerData playerData = getPlayerData();

        if (playerData == null)
            return false;

        GameMode gameMode = BrainOutClient.ClientController.getGameMode();

        if (gameMode != null)
        {
            GameModeRealization realization = gameMode.getRealization();

            if (realization instanceof ClientRealization)
            {
                if (!((ClientRealization) realization).canDropConsumable(cnt))
                    return false;
            }
        }

        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return false;

        ChunkData chunk = map.getChunkAt(((int) playerData.getX()), ((int) playerData.getY()));

        if (chunk != null)
        {
            if (chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
                return false;
        }

        Content content = cnt.getItem().getContent();

        if (!(content instanceof ConsumableContent))
        {
            return false;
        }

        if (BrainOutClient.ClientController.isFreePlay())
        {
            if (content instanceof Weapon)
            {
                Weapon weapon = ((Weapon) content);

                if (weapon.getSlot() != null && weapon.getSlot().getID().equals("slot-melee"))
                {
                    return false;
                }
            }
        }
        else
        {
            if (!((ConsumableContent) content).isThrowable())
                return false;
        }

        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);

        ConsumableContainer container = poc.getConsumableContainer();
        amount = Math.min(amount, container.getAmount(cnt.getItem()));
        container.decConsumable(cnt, amount);
        selectFirstInstrument(poc);

        BrainOutClient.ClientController.sendTCP(new DropConsumableMsg(cnt, amount, angle));

        return true;
    }

    private void selectFirstInstrument(PlayerOwnerComponent poc)
    {
        if (getPlayerData().isWounded())
            return;

        if (getPlayerData().getCurrentInstrument() != null &&
            getPlayerData().getCurrentInstrument().isForceSelect())
        {
            return;
        }

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

        records.sort(Comparator.comparingInt(InstrumentConsumableItem::SortRecords));

        for (ConsumableRecord r: records)
        {
            if (r != record && r.getItem() instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) r.getItem());

                if (((Instrument) ici.getContent()).getSlot() == instrumentSlot)
                {
                    poc.setCurrentInstrument(r);
                    selectRecord(r, Constants.Properties.SLOT_PRIMARY);
                    return;
                }
            }
        }

        for (ConsumableRecord r: records)
        {
            poc.setCurrentInstrument(r);
            selectRecord(r, Constants.Properties.SLOT_PRIMARY);
            break;
        }
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    public int getPosX()
    {
        return posX;
    }

    public int getPosY()
    {
        return posY;
    }

    @Override
    protected float getInstrumentSpeedCoef(InstrumentData current)
    {
        float result = super.getInstrumentSpeedCoef(current);

        if (current instanceof WeaponData)
        {
            WeaponData wd = ((WeaponData) current);
            MyWeaponComponent myWeaponComponent = wd.getComponent(MyWeaponComponent.class);

            if (myWeaponComponent != null)
            {
                WeaponSlotComponent slot = myWeaponComponent.getSlot(Constants.Properties.SLOT_PRIMARY);
                result *= slot.getSpeedCoef();
            }
        }

        return result;
    }
}
