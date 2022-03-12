package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.content.components.BlockAnimationComponent;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.Animable;

import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("BlockAnimationComponent")
@ReflectAlias("data.components.BlockAnimationComponentData")
public class BlockAnimationComponentData extends AnimationComponentData<BlockAnimationComponent> implements Animable
{
    public BlockAnimationComponentData(ComponentObject componentObject, BlockAnimationComponent animation)
    {
        super(componentObject, animation);
    }

    @Override
    public void init()
    {
        super.init();

        attachTo(this);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);
    }

    @Override
    public float getX()
    {
        return BlockData.CURRENT_X + 0.5f;
    }

    @Override
    public float getY()
    {
        return BlockData.CURRENT_Y + 0.5f;
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
}
