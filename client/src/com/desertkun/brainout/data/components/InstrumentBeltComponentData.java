package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.InstrumentBeltComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.BonePointData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.OwnerChangedEvent;
import com.desertkun.brainout.events.SetInstrumentEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;

@Reflect("InstrumentBeltComponent")
@ReflectAlias("data.components.InstrumentBeltComponentData")
public class InstrumentBeltComponentData extends Component<InstrumentBeltComponent>
{
    private final InstrumentData instrumentData;
    private InstrumentAnimationComponentData instrumentComponent;
    private boolean generated;
    private String generatedDimension;

    private BonePointData boneA;
    private BonePointData boneB;

    private Body bodyA, bodyB;
    private Vector2 tmp = new Vector2();

    private Body[] bodies;
    private TextureRegion sprite;
    private boolean active;

    private static Filter BELT_FILTER = new Filter();

    static
    {
        BELT_FILTER.categoryBits = 1 << Constants.Physics.CATEGORY_BELT;
        BELT_FILTER.maskBits = ~(1 << Constants.Physics.CATEGORY_LIGHT);
    }

    public InstrumentBeltComponentData(InstrumentData instrumentData,
                                       InstrumentBeltComponent contentComponent)
    {
        super(instrumentData, contentComponent);

        this.instrumentData = instrumentData;

        this.generated = false;
    }

    @Override
    public void init()
    {
        super.init();

        this.instrumentComponent = instrumentData.getComponentWithSubclass(InstrumentAnimationComponentData.class);
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
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        if (generated)
        {
            Body prevBody = bodyA;

            for (Body body : bodies)
            {
                renderChain(batch, prevBody, body);
                prevBody = body;
            }

            renderChain(batch, prevBody, bodyB);
        }
    }

    private void renderChain(Batch batch, Body a, Body b)
    {
        Vector2 posA = a.getTransform().getPosition();
        Vector2 posB = b.getTransform().getPosition();

        tmp.set(posA);
        tmp.sub(posB);

        float angle = tmp.angleDeg();

        tmp.set(posA);
        tmp.add(posB);
        tmp.scl(0.5f);

        float w = Vector2.dst(posA.x, posA.y, posB.x, posB.y) +
                  2.0f / Constants.Graphics.RES_SIZE;
        float h = sprite.getRegionHeight() / Constants.Graphics.RES_SIZE;

        batch.draw(sprite, tmp.x - w / 2.0f, tmp.y - h / 2.0f, w / 2.0f, h / 2.0f, w, h, 1, 1, angle);
    }

    @Override
    public void release()
    {
        super.release();

        free();
    }

    private void free()
    {
        if (generated)
        {
            generated = false;

            ClientMap map = Map.Get(generatedDimension, ClientMap.class);

            if (map == null)
                return;

            World world = map.getPhysicWorld();

            if (world == null)
                return;

            world.destroyBody(bodyA);
            world.destroyBody(bodyB);

            for (Body body : bodies)
            {
                world.destroyBody(body);
            }
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (instrumentData == null || instrumentData.getOwner() == null)
        {
            if (generated)
            {
                free();
            }
            return;
        }

        Map map = getMap();

        if (map == null)
            return;

        if (generated)
        {
            if (!generatedDimension.equals(map.getDimension()))
            {
                free();
                generate();
            }

            updateTransform();
        }

        boolean isVisible = instrumentData.getOwner().isVisible();
        boolean should = active && isVisible;

        if (should != generated)
        {
            if (should)
            {
                generate();
            }
            else
            {
                free();
            }
        }
    }

    private void updateTransform()
    {
        if (!generated)
            return;

        bodyA.setTransform(boneA.getX(), boneA.getY(), 0);
        bodyB.setTransform(boneB.getX(), boneB.getY(), 0);
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
            case ownerChanged:
            {
                OwnerChangedEvent e = ((OwnerChangedEvent) event);

                if (e.newOwner == null)
                {
                    setActive(false);
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
        }

        return false;
    }

    private void setActive(boolean active)
    {
        this.active = active;

        if (!active)
        {
            update(0);
        }
    }

    private BonePointData findBone(String name)
    {
        BonePointData bone = instrumentComponent.getBone(name);

        if (bone == null && instrumentData.getOwner() != null)
        {
            PlayerAnimationComponentData pac = instrumentData.getOwner().getComponent(PlayerAnimationComponentData.class);

            if (pac != null)
            {
                Bone bone_ = pac.getSkeleton().findBone(name);

                if (bone_ == null)
                    return null;

                return new BonePointData(bone_, pac.getLaunchData());
            }
        }

        return bone;
    }

    private void generate()
    {
        if (instrumentComponent == null)
            return;

        if (!generated)
        {
            this.boneA = findBone(getContentComponent().getAttachBoneA());
            this.boneB = findBone(getContentComponent().getAttachBoneB());

            ClientMap map = ((ClientMap) getMap());

            if (boneA == null || boneB == null || map == null)
                return;

            float length = Vector2.dst(boneA.getX(), boneA.getY(), boneB.getX(), boneB.getY());

            if (length < 1)
                return;

            World world = map.getPhysicWorld();

            if (world == null)
                return;

            generated = true;
            generatedDimension = map.getDimension();

            this.sprite = getContentComponent().getCellSprite();

            int cellsCount = getContentComponent().getCellsCount();
            float cellLength = length / cellsCount;

            Vector2 position = new Vector2(),
                    cellSize = new Vector2(),
                    prevPos = new Vector2(),
                    nextPos = new Vector2(),
                    tmp = new Vector2();

            position.set(boneA.getX(), boneA.getY());

            cellSize.set(boneB.getX(), boneB.getY());
            cellSize.sub(boneA.getX(), boneA.getY());
            cellSize.nor().scl(cellLength);

            BodyDef staticBody = new BodyDef();
            staticBody.type = BodyDef.BodyType.StaticBody;
            staticBody.allowSleep = false;

            BodyDef dynamicBody = new BodyDef();
            dynamicBody.type = BodyDef.BodyType.DynamicBody;
            dynamicBody.allowSleep = false;

            this.bodyA = world.createBody(staticBody);
            this.bodyB = world.createBody(staticBody);

            updateTransform();

            Vector2 halfSize = new Vector2(cellSize);
            halfSize.scl(0.5f);

            Body prevBody = bodyA;

            this.bodies = new Body[cellsCount];

            tmp.set(halfSize).scl(getContentComponent().getTension() - 1.0f);

            CircleShape shape = new CircleShape();
            shape.setRadius(halfSize.len());

            for (int i = 0; i < cellsCount; i++)
            {
                Body body = world.createBody(dynamicBody);
                body.setTransform(position.x, position.y, 0);
                body.setLinearDamping(0.5f);
                body.setAngularDamping(0.5f);

                bodies[i] = body;

                Fixture fixture = body.createFixture(shape, 0.05f);
                fixture.setSensor(true);
                fixture.setRestitution(0.f);
                fixture.setFilterData(BELT_FILTER);

                prevPos.set(position).add(tmp);
                nextPos.set(position).sub(tmp);

                connectRev(world, prevBody, body, prevPos, nextPos);

                position.add(cellSize);

                prevBody = body;
            }

            position.set(boneB.getX(), boneB.getY());
            connectRev(world, prevBody, bodyB, position, position);
        }
    }

    private void connectRev(World world, Body a, Body b, Vector2 locationA, Vector2 locationB)
    {

        RevoluteJointDef jointDef = new RevoluteJointDef();

        jointDef.initialize(a, b, locationA);

        jointDef.localAnchorA.set(a.getLocalPoint(locationA));
        jointDef.localAnchorB.set(b.getLocalPoint(locationB));

        world.createJoint(jointDef);
    }
}
