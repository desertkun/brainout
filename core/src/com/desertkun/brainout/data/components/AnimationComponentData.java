package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Animation;
import com.desertkun.brainout.content.components.AnimationComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.Animable;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.AnimationActionEvent;
import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.SkeletonAttachment;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("AnimationComponent")
@ReflectAlias("data.components.AnimationComponentData")
public class AnimationComponentData<T extends AnimationComponent> extends Component<T>
{
    private Skeleton skeleton;
    protected AnimationState state;
    private final float rootBoneRotation;
    private Animable attachToPosition;
    private LaunchData launchData;
    private AnimationComponentData attached;
    private boolean invalidatedLocation;

    public AnimationComponentData(final ComponentObject componentObject, T animation)
    {
        super(componentObject, animation);

        this.invalidatedLocation = false;

        Animation anim = animation.getAnimation();

        if (anim.getSkeletonData() == null)
        {
            this.skeleton = null;
            this.state = null;
            this.rootBoneRotation = 0;
            return;
        }
        else
        {
            this.skeleton = new Skeleton(anim.getSkeletonData());
        }

        this.state = new AnimationState(anim.getStateData());

        this.launchData = new LaunchData()
        {
            @Override
            public float getX()
            {
                return skeleton.getX();
            }

            @Override
            public float getY()
            {
                return skeleton.getY();
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public String getDimension()
            {
                return getComponentObject().getDimension();
            }

            @Override
            public boolean getFlipX()
            {
                return skeleton.getFlipX();
            }
        };

        state.addListener(new AnimationState.AnimationStateAdapter()
        {
            @Override
            public void event(AnimationState.TrackEntry entry, com.esotericsoftware.spine.Event event)
            {
                animationEvent(event.getData().getName(), event.getString());
            }
        });

        playInitAnimations(animation);

        attachToPosition = null;
        state.setTimeScale(anim.getTimeScale());
        rootBoneRotation = skeleton.getRootBone().getRotation();

    }

    protected void playInitAnimations(T animation)
    {
        String play = animation.getPlayNames();
        if (play != null)
        {
            state.setAnimation(0, play, animation.isPlayLoop());
        }
    }

    private void animationEvent(String kind, String payload)
    {
        BrainOut.EventMgr.sendDelayedEvent(getComponentObject(),
            AnimationActionEvent.obtain(kind, payload));
    }

    protected void updateSkin(AnimationComponent animation, Skeleton skeleton)
    {
        if (animation.getSkin() != null)
        {
            skeleton.setSkin(animation.getSkin());
        }
    }

    public LaunchData getLaunchData()
    {
        return launchData;
    }

    @Override
    public void init()
    {
        super.init();

        updateSkin();
    }

    public void updateSkin()
    {
        updateSkin(getContentComponent(), skeleton);
    }

    public void processLocations()
    {
        if (attached != null)
        {
            getSkeleton().setFlipX(false);
        }
        else
        if (attachToPosition != null)
        {
            skeleton.setPosition(attachToPosition.getX(), attachToPosition.getY());
            skeleton.getRootBone().setRotation(rootBoneRotation + attachToPosition.getAngle());
            skeleton.setFlipX(attachToPosition.getFlipX());
        }
    }

    private float animCollect = 0;

    @Override
    public void update(float dt)
    {
        if (skeleton == null)
            return;

        boolean server = BrainOut.IsServer();

        if (server)
        {
            // update animations once in 0.5 s on the server

            animCollect += dt;
            if (animCollect > 0.25f)
            {
                dt = animCollect;
                animCollect = 0;
            }
            else
            {
                return;
            }
        }

        super.update(dt);

        state.update(dt);

        processLocations();

        try
        {
            state.apply(skeleton);
        }
        catch (NullPointerException | ArrayIndexOutOfBoundsException e)
        {
            // just ignore this shit
        }

        if (attached == null)
        {
            BrainOut.SkeletonRndr.update(skeleton, null);
        }
    }

    public void updateTransform()
    {
        try
        {
            state.apply(skeleton);
        }
        catch (NullPointerException | ArrayIndexOutOfBoundsException e)
        {
            // just ignore this shit
        }

        skeleton.updateWorldTransform();
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (skeleton == null)
            return;

        if (invalidatedLocation)
        {
            invalidatedLocation = false;
            update(0);
        }

        super.render(batch, context);

        if (BrainOut.SkeletonRndr != null && attached == null)
        {
            BrainOut.SkeletonRndr.draw(batch, skeleton);
        }
    }

    public void setInvalidatedLocation()
    {
        this.invalidatedLocation = true;
    }

    public AnimationState getState()
    {
        return state;
    }

    public Skeleton getSkeleton()
    {
        return skeleton;
    }

    public void attachTo(Animable attachTo)
    {
        this.attachToPosition = attachTo;

        update(0);
    }

    public void removeAttachment(String slotName)
    {
        Log.info("Remove attachment " + slotName);

        if (getSkeleton() == null)
            return;

        Slot slot = getSkeleton().findSlot(slotName);

        if (slot != null && slot.getAttachment() != null)
        {
            slot.setAttachment(null);
        }
    }

    public void attachTo(String slotName, AnimationComponentData componentData)
    {
        attachTo(slotName, componentData, null);
    }

    public boolean isAttached()
    {
        return attached != null;
    }

    public void attachTo(String slotName, AnimationComponentData componentData, Bone hookTo)
    {
        if (skeleton == null)
            return;

        if (componentData == null || componentData.getSkeleton() == null)
            return;

        Log.info("Attaching to " + slotName);

        Slot slot = componentData.getSkeleton().findSlot(slotName);

        if (slot != null)
        {
            SkeletonAttachment asAttachment;
            asAttachment = new SkeletonAttachment(getSkeleton().getData().getName())
            {
                @Override
                public void draw(Batch batch)
                {
                    getComponentObject().render(batch, null);
                }
            };

            asAttachment.setHookTo(hookTo);
            asAttachment.setSkeleton(getSkeleton());

            this.attached = componentData;
            slot.setAttachment(asAttachment);
        }
    }

    public AnimationComponentData getAttachedTo()
    {
        return attached;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public int getZIndex()
    {
        return getContentComponent().getzIndex();
    }

    public void detach()
    {
        //
    }
}
