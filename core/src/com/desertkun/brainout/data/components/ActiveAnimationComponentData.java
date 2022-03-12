package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.ActiveAnimationComponent;
import com.desertkun.brainout.content.components.AnimationComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.AnimationData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.*;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.spine.Bone;

@Reflect("ActiveAnimationComponent")
@ReflectAlias("data.components.ActiveAnimationComponentData")
public class ActiveAnimationComponentData extends AnimationComponentData<ActiveAnimationComponent>
{
    public ActiveAnimationComponentData(ComponentObject componentObject, ActiveAnimationComponent animation)
    {
        super(componentObject, animation);
    }


    public BonePointData getBone(String boneName)
    {
        Bone bone = getSkeleton().findBone(boneName);

        if (bone == null) return null;

        return new BonePointData(bone, null);
    }

    public Watcher getBoneWatcher(String boneName)
    {
        BonePointData b = getBone(boneName);

        AnimationData animationData = ((AnimationData) getComponentObject());

        return new Watcher()
        {
            @Override
            public float getWatchX()
            {
                return animationData.getX() +  b.getX();
            }

            @Override
            public float getWatchY()
            {
                return animationData.getY() + b.getY();
            }

            @Override
            public boolean allowZoom()
            {
                return false;
            }

            @Override
            public float getScale()
            {
                return 1;
            }

            @Override
            public String getDimension()
            {
                return animationData.getDimension();
            }
        };
    }

    @Override
    public void init()
    {
        super.init();

        if (getComponentObject() instanceof AnimationData)
        {

            final AnimationData activeData = ((AnimationData) getComponentObject());

            attachTo(new Animable()
            {
                @Override
                public float getX()
                {
                    return activeData.getX();
                }

                @Override
                public float getY()
                {
                    return activeData.getY();
                }

                @Override
                public float getAngle()
                {
                    return 0;
                }

                @Override
                public boolean getFlipX()
                {
                    return activeData.isFlipX();
                }
            });
        }
        else
        {
            final ActiveData activeData = ((ActiveData) getComponentObject());

            attachTo(new Animable() {
                @Override
                public float getX()
                {
                    return activeData.getX();
                }

                @Override
                public float getY()
                {
                    return activeData.getY();
                }

                @Override
                public float getAngle()
                {
                    return 0;
                }

                @Override
                public boolean getFlipX()
                {
                    return false;
                }
            });
        }
    }
}
