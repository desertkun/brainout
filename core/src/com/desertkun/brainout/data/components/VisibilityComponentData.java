package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;

public abstract class VisibilityComponentData<TContent extends ContentComponent> extends Component<TContent>
{
    public VisibilityComponentData(ComponentObject componentObject, TContent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    public abstract boolean isVisibleTo(int to);
}
