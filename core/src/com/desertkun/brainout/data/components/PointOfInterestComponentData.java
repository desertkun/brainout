package com.desertkun.brainout.data.components;

import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.PointOfInterestComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;

public class PointOfInterestComponentData extends Component<PointOfInterestComponent> implements WithTag
{
    public PointOfInterestComponentData(ComponentObject componentObject, PointOfInterestComponent contentComponent)
    {
        super(componentObject, contentComponent);
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

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.POINT_OF_INTEREST);
    }
}
