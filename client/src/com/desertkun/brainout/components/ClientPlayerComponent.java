package com.desertkun.brainout.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.components.my.MyPlayerComponent;
import com.desertkun.brainout.components.my.MyWeaponComponent;
import com.desertkun.brainout.content.Animation;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.ReplaceSlotComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Renderable;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateGame;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.SkeletonAttachment;

@Reflect("content.components.ClientPlayerComponent")
public class ClientPlayerComponent extends PlayerComponentData implements Watcher
{
    private static final Vector2 tmp = new Vector2();

    private final RemoteClient remoteClient;
    private boolean watchAllowZoom;

    private float scale;

    private final LaunchData markerLaunchData;
    private Vector2 pointPosRotated, pointPosLerp, aimOffset, aimOffsetFolowing;

    private Skeleton aimAnimation;
    private AnimationState aimState;
    private AnimationState.TrackEntry aimEntry;

    private ObjectMap<String, Attachment> originalAttachments;

    private boolean displayAim;
    private boolean hideInterfaceMode;
    private boolean lerpAim;

    public ClientPlayerComponent(PlayerData playerData, RemoteClient remoteClient)
    {
        super(playerData, null);

        this.pointPosRotated = new Vector2();
        this.pointPosLerp = new Vector2();
        this.aimOffset = new Vector2();
        this.aimOffsetFolowing = new Vector2();
        this.remoteClient = remoteClient;
        this.scale = 1.0f;
        this.displayAim = false;
        this.lerpAim = true;

        this.originalAttachments = new ObjectMap<>();

        this.markerLaunchData = new LaunchData()
        {
            @Override
            public float getX()
            {
                return playerData.getX() + getMouseOffsetX();
            }

            @Override
            public float getY()
            {
                return playerData.getY() + getMouseOffsetY();
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public String getDimension()
            {
                return playerData.getDimension();
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        };
    }

    public RemoteClient getRemoteClient()
    {
        return remoteClient;
    }

    public LaunchData getMouseLaunchData()
    {
        return markerLaunchData;
    }

    public float getMouseOffsetX()
    {
        return (lerpAim ? pointPosLerp.x : pointPosRotated.x);
    }

    public float getMouseOffsetY()
    {
        return (lerpAim ? pointPosLerp.y : pointPosRotated.y);
    }

    public Vector2 getMouseOffset()
    {
        return lerpAim ? pointPosLerp : pointPosRotated;
    }

    @Override
    public void setDisplayAim(boolean displayAim)
    {
        this.displayAim = displayAim;
    }

    public void setHideInterfaceMode(boolean mode)
    {
        this.hideInterfaceMode = mode;
    }

    public void setLerpAim(boolean lerpAim)
    {
        this.lerpAim = lerpAim;
    }

    public void setWatchAllowZoom(boolean allowZoom)
    {
        this.watchAllowZoom = allowZoom;
    }

    @Override
    public float getWatchX()
    {
        return playerData.getX() + aimOffsetFolowing.x;
    }

    @Override
    public float getWatchY()
    {
        return playerData.getY() + aimOffsetFolowing.y;
    }

    @Override
    public boolean allowZoom()
    {
        return watchAllowZoom;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    public static Renderable AimRenderable()
    {
        return new Renderable()
        {
            @Override
            public void render(Batch batch, RenderContext context)
            {
                Watcher watcher = Map.GetWatcher();
                if (watcher == null)
                    return;

                if (!(watcher instanceof ClientPlayerComponent))
                    return;

                ((ClientPlayerComponent) watcher).renderAim(batch, context);
            }

            @Override
            public boolean hasRender()
            {
                return true;
            }

            @Override
            public int getZIndex()
            {
                return 0;
            }

            @Override
            public int getLayer()
            {
                return 0;
            }
        };
    }

    public void renderAim(Batch batch, RenderContext context)
    {
        if (displayAim && !hideInterfaceMode && aimState != null)
        {
            aimState.apply(aimAnimation);
            aimAnimation.updateWorldTransform();

            batch.setColor(Color.WHITE);
            BrainOutClient.SkeletonRndr.draw(batch, aimAnimation);
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        ClientMap clientMap = getMap(ClientMap.class);

        if (clientMap.isPhysicsDebuggingEnabled())
        {
            batch.end();
            BrainOutClient.ShapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            BrainOutClient.ShapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            ColliderComponentData cc = getComponentObject().getComponent(ColliderComponentData.class);

            if (cc != null)
            {
                for (ObjectMap.Entry<String, ColliderComponentData.Collider> entry : cc.getColliders())
                {
                    ColliderComponentData.Collider collider = entry.value;

                    float x = cc.getPosition().x + collider.x1,
                            y = cc.getPosition().y + collider.y1;

                    float w = collider.x2 - collider.x1,
                            h = collider.y2 - collider.y1;

                    BrainOutClient.ShapeRenderer.rect(
                        x, y, w, h
                    );
                }
            }

            BrainOutClient.ShapeRenderer.end();
            batch.begin();
        }
    }

    public Vector2 getAimOffset()
    {
        return aimOffset;
    }

    public float getScale()
    {
        return scale;
    }

    @Override
    public String getDimension()
    {
        return playerData.getDimension();
    }

    public void setScale(float scale)
    {
        this.scale = scale;
    }

    @Override
    protected float processPlayerAngle(float origAngle)
    {
        if (playerData.getCurrentInstrument() instanceof WeaponData)
        {
            WeaponData weaponData = ((WeaponData) playerData.getCurrentInstrument());
            MyWeaponComponent mwc = weaponData.getComponent(MyWeaponComponent.class);

            if (mwc != null)
            {
                WeaponSlotComponent slot = mwc.getCurrentSlot();

                if (slot != null)
                {
                    origAngle += slot.getAngleOffset();
                }
            }
        }

        return origAngle;
    }

    @Override
    public void init()
    {
        super.init();

        Gdx.app.postRunnable(() ->
        {
            Animation aimMark = (Animation)BrainOut.ContentMgr.get("anim-aim");

            if (aimMark == null || aimMark.getSkeletonData() == null)
                return;

            aimAnimation = new Skeleton(aimMark.getSkeletonData());
            aimState = new AnimationState(aimMark.getStateData());

            aimEntry = aimState.setAnimation(0, "aim", false);
            aimEntry.setTimeScale(0);
        });

        PlayerAnimationComponentData anim = playerData.getComponent(PlayerAnimationComponentData.class);
        if (anim != null)
        {
            anim.getState().addListener(new AnimationState.AnimationStateAdapter()
            {
                @Override
                public void event(AnimationState.TrackEntry entry, com.esotericsoftware.spine.Event event)
                {
                    if (!event.getData().getName().equals("effect"))
                        return;

                    ClientPlayerComponent.this.onAnimationEffect(event.getString());
                }
            });
        }

        updateBadge();

        resetOriginalAttachments();

        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
        BrainOut.EventMgr.subscribe(Event.ID.gameController, this);
    }

    private void onAnimationEffect(String custom)
    {
        PlayerEffectsComponentData eff = playerData.getComponent(PlayerEffectsComponentData.class);
        PlayerAnimationComponentData anim = playerData.getComponent(PlayerAnimationComponentData.class);

        if (eff == null || anim == null)
            return;

        BrainOutClient.EventMgr.sendEvent(eff, LaunchEffectEvent.obtain(
            LaunchEffectEvent.Kind.custom,
            anim.getPrimaryLaunchData(),
            custom
        ));
    }

    @Override
    public void release()
    {
        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.gameController, this);

        super.release();
    }

    private void updateBadge()
    {
        PlayerAnimationComponentData anim = playerData.getComponent(PlayerAnimationComponentData.class);

        ClientController CC = BrainOutClient.ClientController;

        CSGame game = CC.getState(CSGame.class);

        if (anim != null && game != null)
        {
            PlayState ps = CC.getPlayState();
            if (ps instanceof PlayStateGame)
            {
                if (playerData == game.getPlayerData())
                {
                    anim.setEnemy(false);
                }
                else
                {
                    GameMode gameMode = ((PlayStateGame) ps).getMode();

                    boolean isEnemies = gameMode.isEnemies(playerData.getTeam(), game.getTeam()) ||
                        ((ClientRealization) gameMode.getRealization()).isEnemies(
                        CC.getRemoteClients().get(playerData.getOwnerId()), CC.getMyRemoteClient());

                    anim.setEnemy(isEnemies);
                }
            }
        }
    }

    @Override
    protected boolean canSetAim()
    {
        if (playerData == null)
            return false;

        InstrumentData instrument = playerData.getCurrentInstrument();

        if (instrument == null)
            return false;

        MyWeaponComponent mwc = instrument.getComponent(MyWeaponComponent.class);

        if (mwc == null)
            return true;

        WeaponSlotComponent slot = mwc.getCurrentSlot();

        if (slot == null)
            return false;

        WeaponSlotComponent.State state = slot.getState();

        if (state == WeaponSlotComponent.State.cocked || state == WeaponSlotComponent.State.cocking
            || state == WeaponSlotComponent.State.reloading || state == WeaponSlotComponent.State.loadMagazineRound
            || state == WeaponSlotComponent.State.reloadingBoth)
        {
            return false;
        }

        return true;
    }

    public void resetOriginalAttachments()
    {
        originalAttachments.clear();

        PlayerAnimationComponentData pac = getComponentObject().getComponent(PlayerAnimationComponentData.class);

        if (pac != null && pac.getSkeleton() != null)
        {
            for (Slot slot : pac.getSkeleton().getSlots())
            {
                String name = slot.getData().getName();

                if (name.equals(PlayerAnimationComponentData.PLAYER_SLOT_PRIMARY) ||
                    name.equals(PlayerAnimationComponentData.PLAYER_SLOT_SECONDARY) ||
                    name.equals("hook"))
                {
                    continue;
                }

                slot.setToSetupPose();
                originalAttachments.put(name, slot.getAttachment());
            }
        }

        updateAttachments();
    }

    public void updateAttachments()
    {
        PlayerAnimationComponentData anim = playerData.getComponent(PlayerAnimationComponentData.class);

        if (anim == null || anim.getSkeleton() == null)
            return;

        for (ObjectMap.Entry<String, Attachment> entry : originalAttachments)
        {
            Slot slot = anim.getSkeleton().findSlot(entry.key);

            if (slot == null)
                continue;

            if (slot.getAttachment() instanceof SkeletonAttachment)
                continue;

            slot.setAttachment(entry.value);
        }

        Player player = playerData.getPlayer();

        if (player.hasComponent(ReplaceSlotComponent.class) &&
                playerData.getComponent(PlayerAnimationComponentData.class) != null)
        {
            ReplaceSlotComponent rsc = player.getComponent(ReplaceSlotComponent.class);
            rsc.upgradeSkeleton(anim.getSkeleton());
        }

        if (playerData.getCustomAnimationSlots() != null)
        {
            for (ObjectMap.Entry<String, String> entry : playerData.getCustomAnimationSlots())
            {
                ReplaceSlotComponent.UpgradeSkeleton(anim.getSkeleton(), entry.key, entry.value);
            }
        }

        /*
        InstrumentData currentInstrument = playerData.getCurrentInstrument();

        if (currentInstrument != null && currentInstrument.getContent().hasComponent(ReplaceSlotComponent.class) &&
            playerData.getComponent(PlayerAnimationComponentData.class) != null)
        {
            ReplaceSlotComponent rsc = currentInstrument.getContent().getComponent(ReplaceSlotComponent.class);
            rsc.upgradeSkeleton(anim.getSkeleton());
        }
        */

        for (ObjectMap.Entry<String, String> entry : anim.getAttachments())
        {
            if (anim.getSkeleton().findSlot(entry.key) != null &&
                anim.getSkeleton().getAttachment(entry.key, entry.value) != null)
            {
                anim.getSkeleton().setAttachment(entry.key, entry.value);
            }
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                SimpleEvent simpleEvent = ((SimpleEvent) event);

                if (simpleEvent.getAction() == null)
                    return false;

                switch (simpleEvent.getAction())
                {
                    case teamUpdated:
                    {
                        updateBadge();

                        break;
                    }
                    case skinUpdated:
                    {
                        resetOriginalAttachments();

                        break;
                    }
                }

                break;
            }
            case componentUpdated:
            {
                updateAttachments();

                break;
            }
            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {

                    case hideInterface:
                    {
                        setHideInterfaceMode(gcEvent.flag);
                        break;
                    }
                }
                break;
            }
        }

        return super.onEvent(event);
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (playerData.getCurrentInstrument() != null)
        {
            playerData.getCurrentInstrument().update(dt);
        }

        if (playerData.getHookedInstrument() != null)
        {
            AnimationComponentData anim =
                playerData.getHookedInstrument().getComponentWithSubclass(AnimationComponentData.class);

            if (anim != null)
            {
                anim.update(dt);
            }
        }

        Vector2 pointPos = getMousePosition();

        if (pointPosRotated != null && aimEntry != null)
        {
            if (playerData.getCurrentInstrument() != null)
            {
                InstrumentData instrumentData = playerData.getCurrentInstrument();

                InstrumentAnimationComponentData acd =
                        instrumentData.getComponentWithSubclass(InstrumentAnimationComponentData.class);

                if (instrumentData instanceof WeaponData)
                {
                    MyWeaponComponent mywc = instrumentData.getComponent(MyWeaponComponent.class);
                    if (mywc != null)
                    {
                        WeaponSlotComponent slot = mywc.getCurrentSlot();

                        if (slot != null)
                        {
                            pointPosRotated.rotate(slot.getAngleOffset());
                        }
                    }

                    WeaponAnimationComponentData cwc = instrumentData.getComponent(WeaponAnimationComponentData.class);

                    if (cwc != null)
                    {
                        LaunchData l = cwc.getLaunchPointData();

                        pointPosRotated.set(l.getX() - playerData.getX(), l.getY() - playerData.getY());
                        pointPosRotated.add(pointPos);
                    }
                    else
                    {
                        pointPosRotated.set(pointPos);
                    }
                }
                else if (acd != null)
                {
                    LaunchData l = acd.getLaunchPointData();

                    pointPosRotated.set(l.getX() - playerData.getX(), l.getY() - playerData.getY());
                    pointPosRotated.add(pointPos);
                }
                else
                {
                    pointPosRotated.set(pointPos);
                }
            }
            else
            {
                pointPosRotated.set(pointPos);
            }
        }

        MyPlayerComponent mpc = playerData.getComponent(MyPlayerComponent.class);
        if (mpc != null)
        {
            float h = (Math.min(BrainOutClient.getWidth(),
                BrainOutClient.getHeight()) / Constants.Graphics.RES_SIZE) * 0.4f;

            if (pointPos.len() < h)
            {
                aimOffset.set(0, 0);
                setAim(false);
            }
            else
            {
                tmp.set(pointPos);
                tmp.nor().scl(pointPos.len() - h);

                setAim(true);
                aimOffset.set(tmp);
            }
        }

        if (aimEntry != null)
        {
            if (playerData.getCurrentInstrument() != null && playerData.getCurrentInstrument() instanceof WeaponData)
            {
                MyWeaponComponent mwc = playerData.getCurrentInstrument().getComponent(MyWeaponComponent.class);
                if (mwc != null)
                {
                    WeaponSlotComponent slot = mwc.getCurrentSlot();
                    aimEntry.setTrackTime(MathUtils.clamp(slot.getCurrentAccuracy(), 0.0f, 1.0f) * aimEntry.getAnimationEnd() * 0.9f);
                }
                else
                {
                    aimEntry.setTrackTime(0);
                }
            }
            else
            {
                aimEntry.setTrackTime(0);
            }

            aimState.update(dt);
            aimAnimation.setPosition(markerLaunchData.getX(), markerLaunchData.getY());
            aimAnimation.getRootBone().setRotation(playerData.getAngle());
        }

        float folowingSpeed = dt * ClientConstants.Aiming.AIM_FOLLOWING_SPEED;

        if (mpc != null && mpc.isControllerUsed())
        {
            folowingSpeed *= 4;
        }

        float len2 = aimOffset.len2();

        if (len2 > aimOffsetFolowing.len2() && len2 > ClientConstants.Aiming.AIM_ZOOMING_IN_MIN_DISTANCE *
                ClientConstants.Aiming.AIM_ZOOMING_IN_MIN_DISTANCE)
        {
            float len = aimOffset.len() - ClientConstants.Aiming.AIM_ZOOMING_IN_MIN_DISTANCE;
            folowingSpeed /= MathUtils.clamp(ClientConstants.Aiming.AIM_ZOOMING_IN_COEFFICIENT *
                            (len / ClientConstants.Aiming.AIM_ZOOMING_BLOCKS_SMOOTH),
                    1.0f, ClientConstants.Aiming.AIM_ZOOMING_IN_COEFFICIENT);
        }

        aimOffsetFolowing.lerp(aimOffset, folowingSpeed);

        if (lerpAim)
        {
            float dst = pointPosLerp.dst(pointPosRotated);
            float coef = Interpolation.exp10Out.apply(MathUtils.clamp(dst / 16.0f, 0, 1.0f));

            pointPosLerp.lerp(pointPosRotated, MathUtils.clamp(coef * dt * 2.0f, 0, 1.0f));
        }
    }

    public void updateInstrument(InstrumentData instrument, String mode)
    {
        if (aimAnimation == null)
            return;

        if (instrument != null)
        {
            MyWeaponComponent mwc = instrument.getComponent(MyWeaponComponent.class);

            if (mwc != null)
            {
                WeaponSlotComponent slot = mwc.switchWeaponSlot(mode);

                if (slot instanceof ClientWeaponSlotComponent)
                {
                    aimAnimation.setSkin(((ClientWeaponSlotComponent) slot).getAimMarker().asString());
                }
                else
                {
                    aimAnimation.setSkin("standard");
                }
            }
            else
            {
                aimAnimation.setSkin("standard");
            }
        }
    }

}
