package com.desertkun.brainout.data.components;

import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;

public class ServerDestroyCallbackComponentData extends Component
{
    public interface Callback
    {
        void destroyed(ComponentObject componentObject);
    }

    private final Callback callback;

    public ServerDestroyCallbackComponentData(ComponentObject componentObject, Callback callback)
    {
        super(componentObject, null);

        this.callback = callback;
    }

    @Override
    public void release()
    {
        this.callback.destroyed(getComponentObject());

        super.release();
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
