package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;

public class UIAnimation extends Actor
{
    private final AnimationState state;
    private final Skeleton skeleton;

    public UIAnimation(Animation animation)
    {
        state = new AnimationState(animation.getStateData());
        state.setTimeScale(animation.getTimeScale());

        skeleton = new Skeleton(animation.getSkeletonData());
    }

    @Override
    protected void positionChanged()
    {
        super.positionChanged();

        updateAnimation();
    }

    private void updateAnimation()
    {
        skeleton.setPosition(getX() + getWidth() / 2.0f, getY() + getHeight() / 2.0f);
    }

    @Override
    public void setScale(float scaleXY)
    {
        super.setScale(scaleXY);

        skeleton.getRootBone().setScale(scaleXY);
    }

    @Override
    protected void sizeChanged()
    {
        super.sizeChanged();

        updateAnimation();
    }

    public Skeleton getSkeleton()
    {
        return skeleton;
    }

    public AnimationState getState()
    {
        return state;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        batch.flush();
        if (clipBegin())
        {
            BrainOutClient.SkeletonRndr.draw(batch, skeleton);
            batch.flush();
            clipEnd();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        state.update(delta);
        state.apply(skeleton);

        BrainOutClient.SkeletonRndr.update(skeleton, null);
    }
}
