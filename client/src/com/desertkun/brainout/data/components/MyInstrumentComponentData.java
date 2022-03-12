package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.MyInstrumentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.Event;

public class MyInstrumentComponentData<T extends MyInstrumentComponent> extends Component<T>
{
    public MyInstrumentComponentData(ComponentObject componentObject, T contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    public ActiveData getOwner()
    {
        InstrumentData instrumentData = ((InstrumentData) getComponentObject());

        if (instrumentData == null)
            return null;

        return instrumentData.getOwner();
    }

    public boolean isMyInstrument()
    {
        InstrumentData instrumentData = ((InstrumentData) getComponentObject());

        if (instrumentData == null)
            return false;

        if (instrumentData.getOwner() == null)
            return false;

        if (instrumentData.getOwner().getOwnerId() != BrainOutClient.ClientController.getMyId())
            return false;

        return true;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }
}
