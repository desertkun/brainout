package com.desertkun.brainout.data.containers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;

public class ComponentObjectContainer extends ComponentObject implements RenderUpdatable
{
    public ComponentObjectContainer(String dimension)
    {
        super(null, dimension);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }
}
