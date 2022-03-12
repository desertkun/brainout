package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.ActiveIconComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.Animable;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ActiveIconComponent")
@ReflectAlias("data.components.ActiveIconComponentData")
public class ActiveIconComponentData extends AnimationComponentData<ActiveIconComponent>
{
    private final float roteteByX;
    private final float offsetY;

    public ActiveIconComponentData(ComponentObject componentObject, ActiveIconComponent activeIcon)
    {
        super(componentObject, activeIcon);

        this.roteteByX = activeIcon.getRotateByX();
        this.offsetY = activeIcon.getOffsetY();
    }

    @Override
    public void init()
    {
        super.init();

        final SimplePhysicsComponentData phy = getComponentObject().getComponentWithSubclass(SimplePhysicsComponentData.class);

        attachTo(new Animable()
        {
            @Override
            public float getX()
            {
                return phy.getX();
            }

            @Override
            public float getY()
            {
                return phy.getY() + offsetY;
            }

            @Override
            public float getAngle()
            {
                return phy.getAngle() + (roteteByX != 0 ? (-360.0f * ((phy.getX() % roteteByX) / roteteByX)) : 0);
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        });
    }
}
