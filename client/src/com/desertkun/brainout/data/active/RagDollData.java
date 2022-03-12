package com.desertkun.brainout.data.active;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.components.AnimationComponentData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.interfaces.FlippedAngle;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.DamagedEvent;
import com.desertkun.brainout.utils.Physics;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.RagDollData")
public class RagDollData extends ActiveData
{
    private AnimationComponentData animation;
    private Skeleton skeleton;
    private final SimplePhysicsComponentData phy;
    private final ObjectMap<String, DetachedBone> detachedBones;
    private float timer;
    private RagDollBone root;

    private ObjectMap<Bone, RagDollBone> ragDollBones;
    private ObjectMap<Bone, Array<Bone>> boneChilds;
    private World world;
    private Vector2 skeletonPos;
    private DamagedEvent lastHit;
    private boolean flipX;

    public class RagDollBone
    {
        public Bone bone;
        public Body body;
        public Fixture fixture;
        public Array<RagDollBone> child;
        public RagDollBone parent;
        public PolygonShape shape;

        public float originalRotation;

        public RagDollBone()
        {
            this.child = new Array<RagDollBone>();
        }

    }

    public abstract class DetachedBone
    {
        public abstract void update(float dt, Vector2 position, float angle);
    }

    public void attachDamage(DamagedEvent lastHit)
    {
        this.lastHit = lastHit;
    }

    private static Filter RAGDOLL_FILTER = new Filter();

    static
    {
        RAGDOLL_FILTER.categoryBits = 1 << Constants.Physics.CATEGORY_RAGDOLL;
        RAGDOLL_FILTER.maskBits = ~(1 << Constants.Physics.CATEGORY_RAGDOLL | 1 << Constants.Physics.CATEGORY_LIGHT);
    }

    public RagDollData(AnimationComponentData animation,
                       SimplePhysicsComponentData phy,
                       float timeToLive,
                       boolean flipX)
    {
        super(null, animation.getComponentObject().getDimension());

        this.animation = animation;
        this.skeleton = animation.getSkeleton();
        this.timer = timeToLive;
        this.phy = phy;
        this.flipX = flipX;
        this.detachedBones = new ObjectMap<>();

        setzIndex(10);
    }

    public void detachBone(String name, DetachedBone bone)
    {
        detachedBones.put(name, bone);
    }

    @Override
    public void init()
    {
        super.init();

        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        this.world = map.getPhysicWorld();
        this.skeletonPos = new Vector2(skeleton.getX(), skeleton.getY());

        this.ragDollBones = new ObjectMap<>();
        this.boneChilds = new ObjectMap<>();

        for (Bone child: skeleton.getBones())
        {
            Bone parent = child.getParent();

            if (parent != null)
            {
                Array<Bone> a = boneChilds.get(parent);
                if (a == null)
                {
                    a = new Array<Bone>();
                    boneChilds.put(parent, a);
                }

                a.add(child);
            }
        }

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;

        for (Slot slot: skeleton.getSlots())
        {
            if (slot.getAttachment() instanceof BoundingBoxAttachment)
            {
                BoundingBoxAttachment bb = ((BoundingBoxAttachment) slot.getAttachment());

                if (slot.getBone().getWorldScaleX() == 0 ||
                    slot.getBone().getWorldScaleY() == 0)
                {
                    throw new RuntimeException("Bone " + slot.getBone().getData().getName() +
                            " has zero bounding box.");
                }

                float v[] = new float[bb.getVertices().length];
                Physics.ComputeVertices(slot, bb, v);

                Bone bone = slot.getBone();

                Body body = world.createBody(def);
                body.setTransform(
                    (bone.getWorldX()) * Constants.Physics.SCALE,
                    (bone.getWorldY()) * Constants.Physics.SCALE,
                    0);
                body.setLinearVelocity(
                    phy.getSpeed().x * Constants.Physics.SCALE,
                    phy.getSpeed().y * Constants.Physics.SCALE);
                body.setGravityScale(Constants.Physics.SCALE);

                PolygonShape poly = new PolygonShape();
                poly.set(v);

                Fixture fixture = body.createFixture(poly, 0.25f);

                fixture.setFilterData(RAGDOLL_FILTER);
                fixture.setUserData(this);

                RagDollBone ragDollBone = new RagDollBone();

                ragDollBone.bone = bone;
                ragDollBone.body = body;
                ragDollBone.shape = poly;
                ragDollBone.fixture = fixture;
                ragDollBone.originalRotation = bone.getWorldRotation();

                bone.setLockInheritance(true);

                ragDollBones.put(bone, ragDollBone);
            }
        }

        root = ragDollBones.get(skeleton.getRootBone());

        iterate(skeleton.getRootBone(), null);

        for (RagDollBone bone: ragDollBones.values())
        {
            bone.shape.dispose();
        }

        if (lastHit != null && lastHit.content instanceof Bullet)
        {
            Bullet bullet = ((Bullet) lastHit.content);

            float posX = lastHit.x * Constants.Physics.SCALE, posY = lastHit.y * Constants.Physics.SCALE;

            float forceX = MathUtils.cosDeg(lastHit.angle) * bullet.getHitImpulse() * Constants.Physics.SCALE,
                forceY = MathUtils.sinDeg(lastHit.angle) * bullet.getHitImpulse() * Constants.Physics.SCALE;

            root.body.setLinearVelocity(forceX * 3.0f, forceY * 3.0f);
        }

        for (Slot slot: skeleton.getSlots())
        {
            Bone bone = slot.getBone();
            bone.detachParent();
        }
    }

    @Override
    public void release()
    {
        super.release();

        for (RagDollBone bone: ragDollBones.values())
        {
            world.destroyBody(bone.body);
        }

        animation = null;
        skeleton = null;
        ragDollBones.clear();
    }

    private void iterate(Bone bone, RagDollBone parentRagDoll)
    {
        RagDollBone ragDollBone = ragDollBones.get(bone);

        if (ragDollBone != null && parentRagDoll != null)
        {
            if (!detachedBones.containsKey(ragDollBone.bone.getData().getName()))
            {
                connect(ragDollBone, parentRagDoll,
                        bone.getWorldX(),
                        bone.getWorldY());
            }
        }

        Array<Bone> child = boneChilds.get(bone);
        if (child != null)
        {
            if (ragDollBone != null)
            {
                ragDollBone.parent = parentRagDoll;
                parentRagDoll = ragDollBone;
            }

            for (Bone ch: child)
            {
                iterate(ch, parentRagDoll);
            }
        }
    }

    private void connect(RagDollBone ragDollBone, RagDollBone parentRagDoll, float worldX, float worldY)
    {
        RevoluteJointDef jointDef = new RevoluteJointDef();

        jointDef.initialize(ragDollBone.body, parentRagDoll.body,
            new Vector2(worldX * Constants.Physics.SCALE, worldY * Constants.Physics.SCALE));
        jointDef.motorSpeed =  0;
        jointDef.enableLimit = true;

        jointDef.lowerAngle = (float)Math.toRadians(-60.0f);
        jointDef.upperAngle = (float)Math.toRadians(60.0f);

        jointDef.enableMotor = false;
        jointDef.collideConnected = false;

        world.createJoint(jointDef);
    }

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
    public void setX(float x)
    {
        //
    }

    @Override
    public void setY(float y)
    {
        //
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer -= dt;

        if (timer <= 0)
        {
            ClientMap map = ((ClientMap) getMap());

            if (map == null)
                return;

            map.removeActive(this, true);
        }

        Vector2 rootPosition = root.body.getPosition();

        skeleton.setPosition(rootPosition.x * Constants.Physics.SCALE_OF, rootPosition.y * Constants.Physics.SCALE_OF);

        for (RagDollBone bone: ragDollBones.values())
        {
            float a = (float)Math.toDegrees(bone.body.getAngle());
            Vector2 position = bone.body.getPosition();

            bone.bone.setRotation(FlippedAngle.getAngle(a + bone.originalRotation, flipX));
            bone.bone.setPosition(
                ((position.x - rootPosition.x)  * (flipX ? -1.0f : 1.0f))  * Constants.Physics.SCALE_OF,
                (position.y - rootPosition.y) * Constants.Physics.SCALE_OF);
        }

        BrainOutClient.SkeletonRndr.update(skeleton, null);
    }

    @Override
    public float getAngle()
    {
        return skeleton.getRootBone().getRotation();
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        BrainOutClient.SkeletonRndr.draw(batch, skeleton);
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
}
