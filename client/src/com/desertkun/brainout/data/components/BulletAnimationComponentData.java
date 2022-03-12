package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.ActiveIconComponent;
import com.desertkun.brainout.content.components.BulletAnimationComponent;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.Animable;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("BulletAnimationComponent")
@ReflectAlias("data.components.BulletAnimationComponentData")
public class BulletAnimationComponentData extends AnimationComponentData<BulletAnimationComponent>
{
    public BulletAnimationComponentData(BulletData bulletData,
                                        BulletAnimationComponent bulletAnimationComponent)
    {
        super(bulletData, bulletAnimationComponent);
    }

    @Override
    public void init()
    {
        super.init();

        BulletData bulletData = ((BulletData) getComponentObject());

        attachTo(new Animable()
        {
            @Override
            public float getX()
            {
                return bulletData.getX();
            }

            @Override
            public float getY()
            {
                return bulletData.getY();
            }

            @Override
            public float getAngle()
            {
                return bulletData.getAngle();
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        });
    }
}
