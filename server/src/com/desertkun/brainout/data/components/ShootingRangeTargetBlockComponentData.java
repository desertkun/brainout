package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.ShootingRangeTargetBlockComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ShootingRangeTargetBlockComponent")
@ReflectAlias("data.components.ShootingRangeTargetBlockComponentData")
public class ShootingRangeTargetBlockComponentData extends Component<ShootingRangeTargetBlockComponent>
{
    private String group;

    public ShootingRangeTargetBlockComponentData(ComponentObject componentObject,
                                                 ShootingRangeTargetBlockComponent contentComponent)
    {
        super(componentObject, contentComponent);

        group = "";
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public String getGroup()
    {
        return group;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
