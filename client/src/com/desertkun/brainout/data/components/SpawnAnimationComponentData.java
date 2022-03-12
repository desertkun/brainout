package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.content.components.SpawnAnimationComponent;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.RenderContext;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("SpawnAnimationComponent")
@ReflectAlias("data.components.SpawnAnimationComponentData")
public class SpawnAnimationComponentData extends AnimationComponentData<SpawnAnimationComponent>
{
    private boolean enabled;

    public SpawnAnimationComponentData(ComponentObject componentObject, SpawnAnimationComponent spawnAnimationComponent)
    {
        super(componentObject, spawnAnimationComponent);

        this.enabled = false;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (enabled)
        {
            super.render(batch, context);
        }
    }

    @Override
    public void update(float dt)
    {
        if (enabled)
        {
            super.update(dt);
        }
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
