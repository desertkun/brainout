package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.ShieldComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.OwnerChangedEvent;
import com.desertkun.brainout.events.SetInstrumentEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.utils.Physics;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;

public class ShieldComponentData extends Component<ShieldComponent>
{
    private final InstrumentData instrumentData;
    private boolean active;
    private Body physicsBody;
    private String generatedDimension;
    private Slot animationSlot;
    private boolean flipX;
    private float angleOffset;

    private static Filter SHIELD_FILTER = new Filter();

    static
    {
        SHIELD_FILTER.categoryBits = 1 << Constants.Physics.CATEGORY_SHIELDS;
        SHIELD_FILTER.maskBits = ~(1 << Constants.Physics.CATEGORY_OBJECT | 1 << Constants.Physics.CATEGORY_RAGDOLL);
    }

    public ShieldComponentData(InstrumentData instrumentData,
                               ShieldComponent contentComponent)
    {
        super(instrumentData, contentComponent);

        this.instrumentData = instrumentData;
        this.active = false;
    }

    @Override
    public void init()
    {
        super.init();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case setInstrument:
            {
                SetInstrumentEvent e = ((SetInstrumentEvent) event);

                if (e.playerData == instrumentData.getOwner())
                {
                    setActive(instrumentData == e.selected);
                }
                break;
            }
            case simple:
            {
                SimpleEvent e = ((SimpleEvent) event);

                if (e.getAction() == SimpleEvent.Action.deselected)
                {
                    setActive(false);
                    break;
                }

                break;
            }
            case ownerChanged:
            {
                OwnerChangedEvent e = ((OwnerChangedEvent) event);

                if (e.newOwner == null)
                {
                    setActive(false);
                }
            }
        }

        return false;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        Map map = getMap();

        boolean generated = physicsBody != null && animationSlot != null;

        if (generated)
        {
            InstrumentAnimationComponentData iac =
                    instrumentData.getComponentWithSubclass(InstrumentAnimationComponentData.class);

            if (getContentComponent().getSlot() == null || getContentComponent().getSlot().isEmpty())
                return;

            if (flipX != iac.getLaunchPointData().getFlipX() || !map.getDimension().equals(generatedDimension))
            {
                reset(iac);
            }
            else
            {
                Bone bone = animationSlot.getBone();

                float a = flipX ? (angleOffset - bone.getWorldRotation()) : (bone.getWorldRotation() - angleOffset);

                physicsBody.setTransform(
                    (bone.getWorldX()) * Constants.Physics.SCALE,
                    (bone.getWorldY()) * Constants.Physics.SCALE,
                    MathUtils.degreesToRadians * a);
            }
        }

        boolean isVisible = instrumentData.getOwner().isVisible();
        boolean should = active && isVisible;

        if (should != generated)
        {
            if (should)
            {
                InstrumentAnimationComponentData iac =
                        instrumentData.getComponentWithSubclass(InstrumentAnimationComponentData.class);

                generate(iac);
            }
            else
            {
                dispose();
            }
        }
    }

    private void setActive(boolean active)
    {
        if (active == this.active)
            return;

        this.active = active;

        if (active)
        {
            InstrumentAnimationComponentData iac =
                    instrumentData.getComponentWithSubclass(InstrumentAnimationComponentData.class);

            if (getContentComponent().getSlot() == null || getContentComponent().getSlot().isEmpty())
                return;

            Skeleton skeleton = iac.getSkeleton();

            if (skeleton == null)
                return;

            Slot slot = skeleton.findSlot(getContentComponent().getSlot());

            if (slot == null)
                return;

            if (slot.getAttachment() instanceof BoundingBoxAttachment)
            {
                animationSlot = slot;

                generate(iac);
            }
        }
        else
        {
            dispose();
        }
    }

    private void dispose()
    {
        if (physicsBody != null)
        {
            Map map = Map.Get(generatedDimension);

            if (map == null)
                return;

            World world = map.getPhysicWorld();
            if (world != null)
            {
                world.destroyBody(physicsBody);
            }

            physicsBody = null;
        }
    }

    private void reset(InstrumentAnimationComponentData iac)
    {
        dispose();
        generate(iac);
    }

    private boolean generate(InstrumentAnimationComponentData iac)
    {
        Map map = getMap();

        if (map == null)
            return false;

        World world = map.getPhysicWorld();

        if (world == null)
            return false;

        BoundingBoxAttachment bb = ((BoundingBoxAttachment) animationSlot.getAttachment());

        if (animationSlot.getBone().getWorldScaleX() == 0 ||
                animationSlot.getBone().getWorldScaleY() == 0)
        {
            return false;
        }

        Bone bone = animationSlot.getBone();

        float v[] = new float[bb.getVertices().length];
        Physics.ComputeVertices(animationSlot, bb, v);

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;

        angleOffset = bone.getWorldRotation();
        flipX = bone.getWorldFlipX();

        physicsBody = world.createBody(def);
        physicsBody.setTransform(
            (bone.getWorldX()) * Constants.Physics.SCALE,
            (bone.getWorldY()) * Constants.Physics.SCALE,
            0);
        physicsBody.setLinearVelocity(0, 0);
        physicsBody.setGravityScale(0);
        physicsBody.setUserData(this);
        physicsBody.setFixedRotation(true);

        generatedDimension = map.getDimension();

        PolygonShape poly = new PolygonShape();
        poly.set(v);

        Fixture physicsBodyFixture = physicsBody.createFixture(poly, 0.25f);
        physicsBodyFixture.setUserData(this);

        physicsBodyFixture.setFilterData(SHIELD_FILTER);


        return true;
    }

    @Override
    public void release()
    {
        super.release();

        dispose();
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
}
