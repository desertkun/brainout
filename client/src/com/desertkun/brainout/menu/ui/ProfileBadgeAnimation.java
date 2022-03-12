package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.desertkun.brainout.content.Animation;

public class ProfileBadgeAnimation extends UIAnimation
{
    public ProfileBadgeAnimation(Animation animation)
    {
        super(animation);

        getState().setAnimation(0, "animation", true);
        setSize(384, 112);
        setTouchable(Touchable.disabled);
        getSkeleton().getRootBone().setScale(16);
    }

    public ProfileBadgeAnimation(Animation animation, int w, int h)
    {
        super(animation);

        getState().setAnimation(0, "animation", true);
        setSize(w, h);
        setTouchable(Touchable.disabled);
        getSkeleton().getRootBone().setScale(16);
    }
}
