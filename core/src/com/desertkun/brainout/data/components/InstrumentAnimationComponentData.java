package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.components.AnimationComponent;
import com.desertkun.brainout.content.components.InstrumentAnimationComponent;
import com.desertkun.brainout.content.components.InstrumentAnimationStates;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.BonePointData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.HookInstrumentEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("InstrumentAnimationComponent")
@ReflectAlias("data.components.InstrumentAnimationComponentData")
public class InstrumentAnimationComponentData<T extends InstrumentAnimationComponent> extends
    AnimationComponentData<T>
{
    protected final InstrumentData instrumentData;
    private final InstrumentAnimationComponent instrumentComponent;
    private InstrumentAnimationStates states;

    protected BonePointData instrumentLaunch;
    protected BonePointData launchPointData;
    private PlayerAnimationComponentData playerAnimation;
    private Skin skin;

    public InstrumentAnimationComponentData(InstrumentData instrumentData,
                                            T instrumentComponent)
    {
        super(instrumentData, instrumentComponent);

        this.instrumentData = instrumentData;
        this.instrumentComponent = instrumentComponent;

        InstrumentInfo instrumentInfo = instrumentData != null ? instrumentData.getInfo() : null;
        this.skin = instrumentInfo != null ? instrumentInfo.skin : null;

        this.states = new InstrumentAnimationStates(instrumentComponent.getStates());
    }

    protected void updateSkin(AnimationComponent animation, Skeleton skeleton)
    {
        if (skeleton == null)
            return;

        if (instrumentData != null && instrumentData.getInfo() != null && instrumentData.getInfo().skin != null)
        {
            skeleton.setSkin(instrumentData.getInfo().skin.getData());
            return;
        }

        if (animation.getSkin() != null )
        {
            skeleton.setSkin(skin != null ? animation.findSkin(skin.getData()) : animation.getSkin());
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);
    }

    @Override
    public void init()
    {
        super.init();

        if (instrumentData != null && instrumentData.getOwner() != null)
        {
            this.playerAnimation =
                instrumentData.getOwner().getComponent(PlayerAnimationComponentData.class);

            if (playerAnimation == null)
            {
                throw new RuntimeException("No PlayerAnimationComponentData on " +
                    instrumentData.getOwner().getCreator().getID());
            }

            this.instrumentLaunch = playerAnimation.getPrimaryBonePointData();
            launchPointData = getBone("fire-bone");
        }
    }

    public BonePointData getBone(String boneName)
    {
        Bone bone = getSkeleton().findBone(boneName);

        if (bone == null) return null;

        return new BonePointData(bone, instrumentLaunch);
    }

    public BonePointData getLaunchPointData()
    {
        return launchPointData;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case setInstrument:
            {
                Animation anim = getSkeleton().getData().findAnimation("animation");

                if (anim != null)
                {
                    AnimationState.TrackEntry track = getState().setAnimation(0, anim, false);
                    update(0);
                    track.setTrackEnd(0);
                }

                if (instrumentData != null)
                {
                    ActiveData owner = instrumentData.getOwner();

                    if (owner != null)
                    {
                        PlayerComponentData pc = owner.getComponentWithSubclass(PlayerComponentData.class);

                        if (pc != null)
                        {
                            pc.setInstrumentState(states);
                        }
                    }
                }

                attachTo(getAttachSlot(), playerAnimation);

                break;
            }
            case hookInstrument:
            {
                HookInstrumentEvent ev = ((HookInstrumentEvent) event);

                if (ev.selected == instrumentData)
                {
                    Bone hookBone = getSkeleton().findBone("hook-bone");

                    if (hookBone != null)
                    {
                        attachTo(PlayerAnimationComponentData.PLAYER_SLOT_HOOK, playerAnimation, hookBone);
                    }

                    Animation hook = getSkeleton().getData().findAnimation("hook");

                    if (hook != null)
                    {
                        getState().setAnimation(0, hook, false);
                    }
                    else
                    {
                        Animation anim = getSkeleton().getData().findAnimation("animation");

                        if (anim != null)
                        {
                            getState().setAnimation(0, anim, false);
                        }
                    }
                }
                else
                {
                    if (isAttached())
                    {
                        playerAnimation.removeAttachment(PlayerAnimationComponentData.PLAYER_SLOT_HOOK);
                    }
                }

                break;
            }
            case simple:
            {
                SimpleEvent e = ((SimpleEvent) event);

                if (e.getAction() == SimpleEvent.Action.deselected)
                {
                    deselected();

                    break;
                }

                break;
            }
        }

        return false;
    }

    protected void deselected()
    {
        if (playerAnimation != null)
        {
            playerAnimation.removeAttachment(getAttachSlot());
        }
    }

    protected String getAttachSlot()
    {
        switch (getContentComponent().getAttachTo())
        {
            case secondary:
            {
                return PlayerAnimationComponentData.PLAYER_SLOT_SECONDARY;
            }
            case primaryUpper:
            {
                return PlayerAnimationComponentData.PLAYER_SLOT_PRIMARY_UPPER;
            }
            case primary:
            default:
                return PlayerAnimationComponentData.PLAYER_SLOT_PRIMARY;
        }
    }

    public BonePointData getInstrumentLaunch()
    {
        return instrumentLaunch;
    }

    public Skin getSkin()
    {
        return skin;
    }

    public void setSkin(Skin skin)
    {
        this.skin = skin;
    }

    public InstrumentAnimationStates getStates()
    {
        return states;
    }
}
