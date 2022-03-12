package com.desertkun.brainout.data.block;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.content.block.Concrete;
import com.desertkun.brainout.content.block.contact.ContactShape;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.block.ConcreteBD")
public class ConcreteBD extends BlockData
{
    private final ContactShape contactShape;

    public ConcreteBD(Concrete creator)
    {
        super(creator);

        this.contactShape = creator.getContactShape();
    }

    @Override
    public boolean onEvent(Event event)
    {
        if (event.getID() == Event.ID.damageBlock)
        {
            return super.onEvent(event);
        }

        return super.onEvent(event);
    }

    public ContactShape getContactShape()
    {
        return contactShape;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);
    }

    @Override
    public boolean isContact(
        ContactPayload payload,
        float x, float y, Vector2 speed, Vector2 impulse, Vector2 moveForce,
        float reduce, Map map, int blockX, int blockY)
    {
        return contactShape.isContact(payload, x, y, speed, impulse,
            this, map, blockX, blockY, moveForce, reduce);
    }

    @Override
    public boolean isFixture()
    {
        return false;
    }

    @Override
    public LaunchData calculateContact(
        LaunchData launchFrom, LaunchData launchTo,
        boolean in, Map map, int blockX, int blockY)
    {
        return contactShape.calculateContact(
            this, map, blockX, blockY, launchFrom, launchTo, in);
    }

    @Override
    public boolean isConcrete()
    {
        return true;
    }
}
