package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.PlayerPhysicsComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("playerphy")
@ReflectAlias("data.components.PlayerPhysicsComponentData")
public class PlayerPhysicsComponentData extends SimplePhysicsComponentData{
    public PlayerPhysicsComponentData(ActiveData activeData, PlayerPhysicsComponent physicsComponent) {
        super(activeData, physicsComponent);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        float speed = getSpeed().len() * dt;

        if (speed >= 1)
        {
            float MAX_SPEED = 0.99f;
            float dragCoof = MAX_SPEED / speed;

            getSpeed().scl(dragCoof);
        }
    }
}
