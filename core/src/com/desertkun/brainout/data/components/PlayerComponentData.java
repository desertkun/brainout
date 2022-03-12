package com.desertkun.brainout.data.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.InstrumentAnimationStates;
import com.desertkun.brainout.content.components.PlayerComponent;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.*;
import com.desertkun.brainout.events.*;
import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("PlayerComponent")
@ReflectAlias("data.components.PlayerComponentData")
public class PlayerComponentData extends Component<PlayerComponent> implements
        Animable, AnimationState.AnimationStateListener
{
    public static final int ANIMATION_BOTTOM = 0;
    public static final int ANIMATION_TOP = 1;

    private enum BottomAnimationState
    {
        stay,
        sit,
        squat,
        moveForward,
        moveBackward,
        runForward,
        runBackward,
        sitForward,
        sitBackward,
        wounded,
        woundedForward,
        woundedBackward,
        fly,
        crouch,
        crouchForward,
        crouchBackward,
    }

    private Vector2 lastPosition;
    private Bone handsBone;
    private Bone headBone;
    private float bottomContactAccumulator;

    private PlayerAnimationComponentData animation;
    protected final PlayerData playerData;
    private AimState aim;

    private InstrumentAnimationStates states;

    private InstrumentAnimationStates.State topAnimationState;
    private BottomAnimationState bottomAnimationState;
    private float trigger;
    private Vector2 mousePosition;
    private boolean woundedRight;
    private float woundedTimer;
    private boolean forceDisplayAim = true;

    enum FetchAfter
    {
        none,
        after,
        afterSecondary
    }

    public PlayerComponentData(PlayerData playerData,
                               PlayerComponent contentComponent)
    {
        super(playerData, contentComponent);

        this.playerData = playerData;
        this.aim = AimState.none;
        this.lastPosition = new Vector2();
        this.mousePosition = new Vector2(10, 0);

        this.topAnimationState = InstrumentAnimationStates.State.hold;
        this.bottomAnimationState = BottomAnimationState.stay;
        this.bottomContactAccumulator = 0;

        this.trigger = 0;
    }

    public enum AimState
    {
        none,
        aim
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

    public boolean isAim()
    {
        return aim == AimState.aim;
    }

    public AimState getAim()
    {
        return aim;
    }

    public Player.State getState()
    {
        return playerData.getState();
    }

    public void setState(Player.State state)
    {
        playerData.setState(state);
    }

    @Override
    public void init()
    {
        super.init();

        animation = playerData.getComponent(PlayerAnimationComponentData.class);
        animation.attachTo(this);

        handsBone = animation.getSkeleton().findBone("hands");
        headBone = animation.getSkeleton().findBone("head");

        setBottomAnimation("stay", getStayAnimationTimeScale());

        BrainOut.EventMgr.subscribe(Event.ID.instrumentAction, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOut.EventMgr.unsubscribe(Event.ID.instrumentAction, this);
    }

    public void setInstrumentState(InstrumentAnimationStates states)
    {
        this.states = states;

        if (!isPlayingCustomAnimation())
        {
            updateAim();
        }
    }

    private void updateHandAnimations(
            InstrumentAnimationStates.State state,
            boolean loop,
            boolean listen)
    {
        updateHandAnimations(state, loop, listen, 0.0f);
    }

    private void resetHandAnimations()
    {
        animation.getState().clearTrack(ANIMATION_TOP);
    }

    private void updateHandAnimations(
        InstrumentAnimationStates.State state,
        boolean loop,
        boolean listen,
        float timeToPlay)
    {
        if (states != null)
        {
            String animationName = states.getAnimation(state);

            if (animationName != null)
            {
                switch (state)
                {
                    case addRound:
                    {
                        BrainOut.EventMgr.sendDelayedEvent(playerData.getCurrentInstrument(),
                            InstrumentAnimationActionEvent.obtain(state.toString(), playerData.getLaunchData()));
                        break;
                    }
                    default:
                    {
                        BrainOut.EventMgr.sendDelayedEvent(playerData.getCurrentInstrument(),
                            InstrumentAnimationActionEvent.obtain(state.toString()));
                    }
                }

                Animation anim = animation.getSkeleton().getData().findAnimation(animationName);

                if (anim != null)
                {
                    try
                    {
                        AnimationState.TrackEntry animationTop = animation.getState().setAnimation(ANIMATION_TOP,
                                anim, loop);

                        if (timeToPlay != 0)
                        {
                            animationTop.setTimeScale(
                                (animationTop.getAnimationEnd() / timeToPlay) /
                                    animation.getContentComponent().getAnimation().getTimeScale()
                            );
                        }
                        else
                        {
                            animationTop.setTimeScale(
                                1.0f / animation.getContentComponent().getAnimation().getTimeScale());
                        }

                        if (listen)
                        {
                            lastInterruptedTop = null;
                            animationTop.setListener(this);
                        }
                    }
                    catch (NullPointerException | IllegalStateException e)
                    {
                        // just ignore it
                    }
                }
            }
        }

        this.topAnimationState = state;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case instrumentAction:
            {
                InstrumentActionEvent actionEvent = ((InstrumentActionEvent) event);

                processAction(actionEvent);

                break;
            }
            case setInstrument:
            {
                SetInstrumentEvent ev = ((SetInstrumentEvent) event);
                InstrumentData instrumentData = ev.selected;

                if (instrumentData != null)
                {
                    InstrumentAnimationComponentData iac =
                        instrumentData.getComponentWithSubclass(InstrumentAnimationComponentData.class);

                    if (iac != null)
                    {
                        resetHandAnimations();
                        setInstrumentState(iac.getStates());
                    }
                }

                if (!isPlayingCustomAnimation())
                {
                    updateAim();
                    updateBottomAnimation();
                }

                break;
            }
            case resetInstrument:
            {
                resetHandAnimations();

                break;
            }
        }

        return false;
    }

    private void processAction(InstrumentActionEvent action)
    {
        if (isPlayingCustomAnimation())
            return;

        if (isWounded())
            return;

        if (action == null || action.action == null)
            return;

        switch (action.action)
        {
            case hit:
            {
                updateHandAnimations(isAim()
                    ? InstrumentAnimationStates.State.aimHit
                    : InstrumentAnimationStates.State.hit, false, false);

                break;
            }
            case reload:
            {
                animateReload(InstrumentAnimationStates.State.reload, action.data0, true);
                break;
            }
            case fetch:
            {
                animateReload(InstrumentAnimationStates.State.fetch, action.data0, true);
                break;
            }
            case reloadSecondary:
            {
                animateReload(InstrumentAnimationStates.State.reloadSecondary, action.data0, true);
                break;
            }
            case fetchSecondary:
            {
                animateReload(InstrumentAnimationStates.State.fetchSecondary, action.data0, true);
                break;
            }
            case reloadBoth:
            {
                animateReload(InstrumentAnimationStates.State.reloadBoth, action.data0, true);
                break;
            }
            case cock:
            {
                animateCocking(true, action.data0);

                BrainOut.EventMgr.sendDelayedEvent(playerData.getCurrentInstrument(),
                        InstrumentAnimationActionEvent.obtain("cock"));
                break;
            }
            case reset:
            {
                animateReset(action.data0);
                break;
            }
            case cockSecondary:
            {
                animateCocking(false, action.data0);

                break;
            }
            case buildUp:
            {
                updateHandAnimations(InstrumentAnimationStates.State.buildUp, false, true);

                break;
            }
            case loadMagazineRound:
            {
                setDisplayAim(false);
                animateReload(InstrumentAnimationStates.State.addRound, action.data0, true);
                break;
            }
        }
    }

    public void setDisplayAim(boolean displayAim) {}

    public void playCustomHandAnimation(String animationName, boolean loop)
    {
        if (states != null)
        {
            if (animationName != null)
            {
                Animation anim = animation.getSkeleton().getData().findAnimation(animationName);

                if (anim != null)
                {
                    try
                    {
                        AnimationState.TrackEntry animationTop =
                            animation.getState().setAnimation(ANIMATION_TOP,
                                    anim, loop);

                        lastInterruptedTop = null;
                        animationTop.setListener(this);
                        animationTop.setTimeScale(0.5f);
                    }
                    catch (NullPointerException | IllegalStateException e)
                    {
                        // just ignore it
                    }
                }
            }
        }

        this.topAnimationState = InstrumentAnimationStates.State.custom;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        float origAngle = playerData.getAngle();

        origAngle = processPlayerAngle(origAngle);

        while (origAngle > 180) origAngle -= 360;
        while (origAngle < -180) origAngle += 360;

        boolean sectorA = origAngle > 90,
                sectorB = origAngle < -90;
        boolean right = sectorA || sectorB;

        if (right) {
            if (sectorA) {
                origAngle = -origAngle + 180f;
            } else {
                origAngle = -origAngle - 180;
            }
        }

        if (isWounded())
        {
            if (handsBone != null)
                handsBone.setRotation(0);

            if (woundedRight != right)
            {
                woundedTimer += dt;

                if (woundedTimer > 1.0f)
                {
                    woundedRight = right;
                    woundedTimer = 0;

                    animation.getSkeleton().setFlipX(right);
                }
            }
            else
            {
                if (headBone != null)
                    headBone.setRotation(MathUtils.clamp(origAngle, -90, 0));

                woundedTimer = 0;
            }
        }
        else
        {
            if (isCrouching())
            {
                float c = MathUtils.clamp(origAngle, -90, 20);

                if (handsBone != null)
                    handsBone.setRotation(c);

                if (headBone != null)
                    headBone.setRotation(c);
            }
            else
            {
                if (handsBone != null)
                    handsBone.setRotation(origAngle);

                if (headBone != null)
                    headBone.setRotation(origAngle);
            }

            animation.getSkeleton().setFlipX(right);
        }

        // update moving animation
        BottomAnimationState newState;

        SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy == null)
            return;

        boolean hasBottomContact = phy.hasContact(SimplePhysicsComponentData.Contact.bottom);
        boolean hasFixture = phy.hasFixture();

        if (hasBottomContact)
        {
            bottomContactAccumulator = 0.1f;
        }
        else
        {
            bottomContactAccumulator -= dt;
        }

        hasBottomContact = bottomContactAccumulator > 0;

        trigger -= dt;

        if (trigger < 0)
        {
            if (Math.abs(lastPosition.x - getX()) > 0.05f)
            {
                if (hasBottomContact || hasFixture)
                {
                    switch (getState())
                    {
                        case sit:
                        {
                            if (getX() > lastPosition.x != getFlipX())
                            {
                                newState = BottomAnimationState.sitForward;
                            }
                            else
                            {
                                newState = BottomAnimationState.sitBackward;
                            }

                            break;
                        }
                        case crawl:
                        {
                            if (getX() > lastPosition.x != getFlipX())
                            {
                                newState = BottomAnimationState.crouchForward;
                            }
                            else
                            {
                                newState = BottomAnimationState.crouchBackward;
                            }

                            break;
                        }
                        case wounded:
                        {
                            if (getX() > lastPosition.x != getFlipX())
                            {
                                newState = BottomAnimationState.woundedForward;
                            }
                            else
                            {
                                newState = BottomAnimationState.woundedBackward;
                            }

                            break;
                        }
                        case run:
                        {
                            if (getX() > lastPosition.x != getFlipX())
                            {
                                newState = BottomAnimationState.runForward;
                            }
                            else
                            {
                                newState = BottomAnimationState.runBackward;
                            }

                            break;
                        }
                        case normal:
                        default:
                        {
                            if (getX() > lastPosition.x != getFlipX())
                            {
                                newState = BottomAnimationState.moveForward;
                            }
                            else
                            {
                                newState = BottomAnimationState.moveBackward;
                            }

                            break;
                        }
                    }
                }
                else
                {

                    switch (getState())
                    {
                        case wounded:
                        {
                            newState = BottomAnimationState.wounded;

                            break;
                        }

                        default:
                        {
                            newState = BottomAnimationState.fly;
                        }
                    }
                }
            }
            else
            {
                switch (getState())
                {
                    case sit:
                    {
                        newState = BottomAnimationState.sit;

                        break;
                    }
                    case squat:
                    {
                        newState = BottomAnimationState.squat;

                        break;
                    }
                    case crawl:
                    {
                        newState = BottomAnimationState.crouch;

                        break;
                    }
                    case wounded:
                    {
                        newState = BottomAnimationState.wounded;

                        break;
                    }
                    case normal:
                    default:
                    {
                        newState = BottomAnimationState.stay;

                        break;
                    }
                }
            }

            if (newState != bottomAnimationState)
            {
                bottomAnimationState = newState;

                updateBottomAnimation();
            }

            lastPosition.set(getX(), getY());
            trigger = 0.05f;
        }
    }

    protected boolean isWounded()
    {
        return bottomAnimationState == BottomAnimationState.wounded ||
            bottomAnimationState == BottomAnimationState.woundedForward ||
            bottomAnimationState == BottomAnimationState.woundedBackward;
    }

    protected boolean isCrouching()
    {
        return bottomAnimationState == BottomAnimationState.crouch ||
            bottomAnimationState == BottomAnimationState.crouchBackward ||
            bottomAnimationState == BottomAnimationState.crouchForward;
    }

    protected float processPlayerAngle(float origAngle)
    {
        return origAngle;
    }

    private float getStayAnimationTimeScale()
    {
        PlayerAnimationComponentData pl = playerData.getComponent(PlayerAnimationComponentData.class);
        if (pl != null && pl.getContentComponent() != null)
        {
            return pl.getContentComponent().getStayAnimationTimeScale().getValue();
        }

        return 1;
    }

    public void updateBottomAnimation()
    {
        float speedScale = playerData.getSpeedCoef();

        if (playerData.getCurrentInstrument() instanceof WeaponData)
        {
            Weapon weapon = ((WeaponData) playerData.getCurrentInstrument()).getWeapon();
            speedScale *= weapon.getSpeedCoef();
        }

        switch (bottomAnimationState)
        {
            case stay:
            {
                setBottomAnimation("stay", getStayAnimationTimeScale());
                break;
            }
            case sit:
            {
                setBottomAnimation("sit");
                break;
            }
            case squat:
            {
                setBottomAnimation("sit2");
                break;
            }
            case moveForward:
            {
                setBottomAnimation("move-forward", speedScale);
                break;
            }
            case moveBackward:
            {
                setBottomAnimation("move-backward", speedScale);

                break;
            }
            case sitForward:
            {
                setBottomAnimation("sit-forward", speedScale);
                break;
            }
            case sitBackward:
            {
                setBottomAnimation("sit-backward", speedScale);

                break;
            }
            case runForward:
            {
                setBottomAnimation("run-forward", speedScale);
                break;
            }
            case runBackward:
            {
                setBottomAnimation("run-backward", speedScale);

                break;
            }
            case fly:
            {
                setBottomAnimation("fly");

                break;
            }
            case wounded:
            {
                setBottomAnimation("wounded");
                break;
            }
            case woundedForward:
            {
                setBottomAnimation("wounded-forward");
                break;
            }
            case woundedBackward:
            {
                setBottomAnimation("wounded-backward");
                break;
            }
            case crouch:
            {
                setBottomAnimation("crouch");
                break;
            }
            case crouchForward:
            {
                setBottomAnimation("crouch-forward");
                break;
            }
            case crouchBackward:
            {
                setBottomAnimation("crouch-backward");
                break;
            }
        }
    }

    private void setBottomAnimation(String anim)
    {
        setBottomAnimation(anim, 1);
    }

    private void setBottomAnimation(String anim, float timeScale)
    {
        Animation a = animation.getSkeleton().getData().findAnimation(anim);

        if (a != null)
        {
            try
            {
                AnimationState.TrackEntry trackEntry =
                        animation.getState().setAnimation(ANIMATION_BOTTOM, a, true);

                trackEntry.setEventThreshold(0);
                trackEntry.setTimeScale(timeScale / animation.getContentComponent().getAnimation().getTimeScale());
            }
            catch (NullPointerException | IllegalStateException | ArrayIndexOutOfBoundsException e)
            {
                // just ignore it
            }
        }
    }

    @Override
    public float getX()
    {
        return playerData.getX();
    }

    @Override
    public float getY()
    {
        return playerData.getY();
    }

    @Override
    public float getAngle()
    {
        return 0; // don't want to consumableContainer is rotate
    }

    public boolean getFlipX()
    {
        return animation.getSkeleton().getFlipX();
    }

    public void setAim(boolean aim)
    {
        if (topAnimationState == InstrumentAnimationStates.State.reload ||
            topAnimationState == InstrumentAnimationStates.State.fetch ||
            topAnimationState == InstrumentAnimationStates.State.addRound ||
            topAnimationState == InstrumentAnimationStates.State.custom)
        {
            return;
        }

        if (!canSetAim())
        {
            return;
        }

        AimState toSet;
        if (aim)
        {
            toSet = AimState.aim;
        }
        else
        {
            toSet = AimState.none;
        }

        if (this.aim != toSet)
        {
            this.aim = toSet;
            updateAim();
        }
    }

    protected boolean canSetAim()
    {
        return true;
    }

    public void setForceDisplayAim(boolean forceDisplayAim)
    {
        this.forceDisplayAim = forceDisplayAim;
    }

    public void updateAim()
    {
        setDisplayAim(forceDisplayAim);

        if (isWounded())
        {
            resetHandAnimations();
        }
        else
        {
            if (isPlayingCustomAnimation())
            {
                return;
            }

            switch (getAim())
            {
                case aim:
                {
                    updateHandAnimations(InstrumentAnimationStates.State.aim, true, false);
                    break;
                }
                case none:
                {
                    updateHandAnimations(InstrumentAnimationStates.State.hold, true, false);
                }
            }
        }
    }

    private void animateReload(InstrumentAnimationStates.State state, float time, boolean listen)
    {
        if (isPlayingCustomAnimation())
            return;

        if (isWounded())
            return;

        updateHandAnimations(state, false, listen, time);
    }

    private void animateCocking(boolean primary, float time)
    {
        if (isPlayingCustomAnimation())
        {
            return;
        }

        if (isWounded())
            return;

        if (primary)
        {
            updateHandAnimations(InstrumentAnimationStates.State.cock, false, true, time);
        }
        else
        {
            updateHandAnimations(InstrumentAnimationStates.State.cockSecondary, false, true, time);
        }
    }

    private void animateReset(float time)
    {
        if (isPlayingCustomAnimation())
        {
            return;
        }

        if (isWounded())
            return;

        updateHandAnimations(InstrumentAnimationStates.State.reset, false, true, time);
    }

    public boolean isPlayingCustomAnimation()
    {
        return topAnimationState == InstrumentAnimationStates.State.custom;
    }

    @Override
    public void event(AnimationState.TrackEntry entry, com.esotericsoftware.spine.Event event)
    {
        //
    }

    private AnimationState.TrackEntry lastInterruptedTop = null;

    @Override
    public void interrupt(AnimationState.TrackEntry entry)
    {
        switch (entry.getTrackIndex())
        {
            case ANIMATION_TOP:
            {
                lastInterruptedTop = entry;
                break;
            }
        }
    }

    @Override
    public void complete(AnimationState.TrackEntry entry)
    {
        switch (entry.getTrackIndex())
        {
            case ANIMATION_TOP:
            {
                if (lastInterruptedTop == entry)
                {
                    return;
                }

                if (isWounded())
                {
                    resetHandAnimations();
                }
                else
                {
                    switch (topAnimationState)
                    {
                        case aimHit:
                        {
                            updateHandAnimations(InstrumentAnimationStates.State.aim, true, false);

                            break;
                        }
                        case hit:
                        {
                            updateHandAnimations(InstrumentAnimationStates.State.hold, true, false);

                            break;
                        }
                        case reloadSecondary:
                        case reloadBoth:
                        case reload:
                        case fetch:
                        case pull:
                        case buildUp:
                        {
                            updateAim();
                            break;
                        }
                        case custom:
                        {
                            customAnimationComplete();
                            break;
                        }
                    }
                }

                break;
            }
        }
    }

    public void customAnimationComplete()
    {
        resetHandAnimations();
        topAnimationState = InstrumentAnimationStates.State.aim;
        updateAim();

        if (playerData.getCurrentInstrument() != null)
        {
            BrainOut.EventMgr.sendDelayedEvent(playerData.getCurrentInstrument(),
                SetInstrumentEvent.obtain(playerData.getCurrentInstrument(), playerData));
        }
    }

    @Override
    public void start(AnimationState.TrackEntry entry)
    {
        //
    }

    @Override
    public void end(AnimationState.TrackEntry entry)
    {
        //
    }

    @Override
    public void dispose(AnimationState.TrackEntry entry)
    {

    }

    public Vector2 getMousePosition()
    {
        return mousePosition;
    }
}
