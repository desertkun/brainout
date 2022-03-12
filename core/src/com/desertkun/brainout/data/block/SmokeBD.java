package com.desertkun.brainout.data.block;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.content.block.Smoke;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.block.SmokeBD")
public class SmokeBD extends NonContactBD
{
    public SmokeBD(Smoke creator)
    {
        super(creator);
    }

    @Override
    public boolean isContact(ContactPayload payload, float x, float y, Vector2 speed, Vector2 impulse, Vector2 moveForce, float reduce, Map map, int blockX, int blockY)
    {
        return true;
    }
}
