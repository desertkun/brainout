package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.SimplePhysicsComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("SimplePhysicsComponent")
@ReflectAlias("data.components.SimplePhysicsComponentData")
public class SimplePhysicsComponentData extends Component<SimplePhysicsComponent> implements Json.Serializable
{
    public enum Contact
    {
        bottom,
        top,
        left,
        right,

        none
    }

    public static class PhysicsPayload implements BlockData.ContactPayload
    {
        public Contact contact;
        public ActiveData activeData;
    }

    private final ActiveData activeData;
    private final Vector2 halfSize;
    private final Vector2 size;
    private final Vector2 force;
    private final Vector2 speed_;
    private float contactGap;
    private boolean enabled;

    private static Vector2 AXIS[] = new Vector2[]
    {
        new Vector2(0, -1.0f),
        Vector2.Y,
        new Vector2(-1.0f, 0),
        Vector2.X,
        Vector2.Zero
    };

    public class ContactData
    {
        public BlockData block;
        public int x;
        public int y;

        public ContactData()
        {
        }

        public boolean valid()
        {
            return block != null;
        }

        public void set(BlockData blockData, int x, int y)
        {
            this.block = blockData;
            this.x = x;
            this.y = y;
        }

        public void clear()
        {
            this.block = null;
        }
    }

    private ContactData contacts[];

    private static Vector2 moveForce = new Vector2();
    private static Vector2 prevSpeed = new Vector2();
    private static PhysicsPayload phyPayload = new PhysicsPayload();

    private Vector2 speed;
    private boolean fixture;
    private boolean canFix;

    public SimplePhysicsComponentData(
        ActiveData activeData,
        SimplePhysicsComponent physicsComponent)
    {
        super(activeData, physicsComponent);

        this.activeData = activeData;
        this.fixture = false;
        this.canFix = true;
        this.enabled = true;

        this.speed = new Vector2();
        this.speed_ = new Vector2();
        this.size = new Vector2();
        this.force = new Vector2();
        this.halfSize = new Vector2();
        this.contactGap = physicsComponent.getContactGap();

        this.contacts = new ContactData[]{
            new ContactData(),
            new ContactData(),
            new ContactData(),
            new ContactData()
        };

        setSize(getContentComponent().getSize().x, getContentComponent().getSize().y);
    }

    public void setSize(float width, float height)
    {
        size.set(width, height);
        halfSize.set(width / 2f, height / 2f);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case addImpulse:
            {
                AddImpulseEvent impulseEvent = (AddImpulseEvent)event;
                addImpulse(impulseEvent.forceX, impulseEvent.forceY);

                return true;
            }

            case setSpeed:
            {
                SetSpeedEvent speedEvent = (SetSpeedEvent)event;
                setSpeed(speedEvent.speed, speedEvent.coef);

                break;
            }
        }

        return false;
    }

    private void setSpeed(Vector2 speed, Vector2 coef)
    {
        this.speed.set(
            Interpolation.linear.apply(this.speed.x, speed.x, coef.x),
            Interpolation.linear.apply(this.speed.y, speed.y, coef.y)
        );
    }

    public boolean hasAnyContact()
    {
        for (ContactData contact : contacts)
        {
            if (contact.valid())
            {
                return true;
            }
        }

        return false;
    }

    public void applyForce(float x, float y)
    {
        this.force.add(x, y);
    }

    public void applyForce(Vector2 force)
    {
        this.force.add(force);
    }

    public void addPosition(float dt)
    {
        if (speed_.y <= 0)
        {
            checkCollisions(0.5f - halfSize.x, -halfSize.y,
                    1, 0, (int) size.x, Contact.bottom, true);
        }
        else
        {
            contacts[Contact.bottom.ordinal()].clear();
        }

        if (speed_.y >= 0)
        {
            checkCollisions(0.5f - halfSize.x, halfSize.y,
                    1, 0, (int) size.x, Contact.top, true);
        }
        else
        {
            contacts[Contact.left.ordinal()].clear();
        }

        if (speed_.x <= 0)
        {
            checkCollisions(-halfSize.x, 0.5f - halfSize.y,
                    0, 1, (int) size.y, Contact.left, true);
        }
        else
        {
            contacts[Contact.left.ordinal()].clear();
        }

        if (speed_.x >= 0)
        {
            checkCollisions(halfSize.x, 0.5f - halfSize.y,
                    0, 1, (int) size.y, Contact.right, true);
        }
        else
        {
            contacts[Contact.right.ordinal()].clear();
        }

        if (hasFixture())
        {
            float fixtureSpeed = getContentComponent().getFixtureSpeed();
            speed_.scl(fixtureSpeed);
        }

        activeData.setPosition(
            getX() + speed_.x * dt,
            getY() + speed_.y * dt
        );
    }

    public Vector2 getActualSpeed()
    {
        return speed_;
    }

    public boolean overlapsBlock(int x, int y)
    {
        float positionX = getX(), positionY = getY();

        int aX = (int)(positionX - halfSize.x), bX = (int)(positionX + halfSize.x),
            aY = (int)(positionY - halfSize.y), bY = (int)(positionY + halfSize.y);

        return (x >= aX) && (x <= bX) && (y >= aY) && (y <= bY);
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean checkCollisions(
            float x, float y, int addX, int addY, int amount, Contact contact, boolean validateSpeed)
    {
        float resAddX = getX() + x, resAddY = getY() + y;
        Map map = getMap();

        if (map == null)
            return false;

        // todo: fix shitcode
        moveForce.set(speed.x * (1 - addX), speed.y * (1 - addY));

        for (int i = 0; i < amount; i++)
        {
            int blockX = (int)resAddX, blockY = (int)resAddY;

            BlockData blockData = map.getBlockAt(blockX, blockY, Constants.Layers.BLOCK_LAYER_FOREGROUND);

            if (blockData != null)
            {
                Vector2 axis = AXIS[contact.ordinal()];

                // check if speed vector and contact vector are looking the same way
                if (axis.dot(speed) > 0 || (!validateSpeed))
                {
                    phyPayload.contact = contact;
                    phyPayload.activeData = activeData;

                    prevSpeed.set(speed);

                    if (blockData.isContact(phyPayload,
                            resAddX % 1, resAddY % 1, speed, moveForce, moveForce,
                            getContentComponent().getReduce(), map, blockX, blockY))
                    {
                        if (contact != Contact.none)
                        {
                            contacts[contact.ordinal()].set(blockData, blockX, blockY);

                            if (getContentComponent().isSticky())
                            {
                                speed.set(0, 0);
                            }
                            else
                            {
                                activeData.setPosition(getX() + moveForce.x, getY() + moveForce.y);
                            }
                        }

                        prevSpeed.sub(speed);

                        if (prevSpeed.len2() > contactGap * contactGap)
                        {
                            BrainOut.EventMgr.sendDelayedEvent(activeData,
                                PhysicsContactEvent.obtain(prevSpeed, activeData));
                        }

                        return true;
                    }
                }
            }

            resAddX += addX;
            resAddY += addY;
        }

        if (contact != Contact.none)
        {
            contacts[contact.ordinal()].clear();
        }

        return false;
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("sx", speed.x);
        json.writeValue("sy", speed.y);
        json.writeValue("en", enabled);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        speed.x = jsonData.getFloat("sx");
        speed.y = jsonData.getFloat("sy");
        enabled = jsonData.getBoolean("en", true);
    }

    public float getX()
    {
        return activeData.getX();
    }

    public float getAngle()
    {
        return activeData.getAngle();
    }

    public float getY()
    {
        return activeData.getY();
    }

    public void setX(float x)
    {
        activeData.setX(x);
    }

    public void setY(float y)
    {
        activeData.setY(y);
    }

    static final float STEPS = 0.03f;

    @Override
    public void update(float dt)
    {
        if (!enabled)
            return;

        while (dt >= STEPS)
        {
            updatePhysics(STEPS);
            dt -= STEPS;
        }

        updatePhysics(dt);
    }

    private boolean calculateFixture()
    {
        Map map = getMap();

        if (map == null)
            return false;

        int sizeX = (int)(halfSize.x * 2.0f),
            sizeY = (int)(halfSize.y * 2.0f);

        float x_ = activeData.getX() - halfSize.x, y_ = activeData.getY() - halfSize.y;

        for (int j = 0; j <= sizeY; j++)
        {
            float y = y_ + j;

            for (int i = 0; i <= sizeX; i++)
            {
                float x = x_ + i;

                BlockData blockData = map.getBlockAt(x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

                if (blockData != null)
                {
                    if (canFix && getContentComponent().isFixable() && blockData.isFixture())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void updatePhysics(float dt)
    {
        fixture = calculateFixture();

        if (getContentComponent().isSticky() && hasAnyContact())
        {
            return;
        }

        speed_.set(speed);

        if (!force.isZero())
        {
            speed_.add(force);
            force.set(0, 0);
        }

        addPosition(MathUtils.clamp(dt, 0.f, 0.25f));

        if (!hasFixture())
        {
            addImpulse(0, -getContentComponent().getMass() * dt * Constants.Core.GRAVITY);
        }

        if (getContentComponent().getFriction() != 0 && hasContact(Contact.bottom))
        {
            getSpeed().x *= (1 - getContentComponent().getFriction());
        }

        float speedLimit = getContentComponent().getSpeedLimit();

        if (speedLimit > 0)
        {
            if (getSpeed().len2() >= speedLimit * speedLimit)
            {
                getSpeed().nor().scl(speedLimit);
            }
        }

        if (activeData != null)
        {
            if (getContentComponent().isRotateBySpeed() && !speed.isZero(0.1f))
            {
                activeData.setAngle(speed.angleDeg());
            }
        }
    }

    public void addImpulse(float x, float y)
    {
        speed.add(x, y);
    }

    public Vector2 getSpeed()
    {
        return speed;
    }

    public boolean hasContact(Contact contact)
    {
        return contacts[contact.ordinal()].valid();
    }

    public boolean hasFixture()
    {
        return fixture;
    }

    public ContactData getContact(Contact contact)
    {
        return contacts[contact.ordinal()];
    }

    public Vector2 getSize()
    {
        return size;
    }

    public Vector2 getHalfSize()
    {
        return halfSize;
    }

    public ActiveData getActiveData()
    {
        return activeData;
    }

    public void setCanFix(boolean canFix)
    {
        this.canFix = canFix;
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
}
