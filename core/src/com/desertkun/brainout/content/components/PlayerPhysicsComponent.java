package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.PlayerPhysicsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.PlayerPhysicsComponent")
public class PlayerPhysicsComponent extends SimplePhysicsComponent {

    public PlayerPhysicsComponent()
    {
        super();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new PlayerPhysicsComponentData((ActiveData)componentObject, this);
    }
}
