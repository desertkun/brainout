package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;

public class ActiveFilterComponentData extends Component
{
    private final Filter filter;

    public interface Filter
    {
        boolean filter(int owner);
    }

    public ActiveFilterComponentData(Filter filter)
    {
        super(null, null);

        this.filter = filter;
    }

    public boolean filters(int owner)
    {
        return filter.filter(owner);
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
