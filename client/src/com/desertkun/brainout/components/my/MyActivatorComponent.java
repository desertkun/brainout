package com.desertkun.brainout.components.my;

import com.desertkun.brainout.data.components.ClientInstrumentActivatorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.ActivateInstrumentData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;

public class MyActivatorComponent extends Component
{
    private final ConsumableRecord consumableRecord;
    private State state;
    private boolean launching;
    private ClientInstrumentActivatorComponentData component;

    public enum State
    {
        idle,
        launching,
        launched
    }

    public MyActivatorComponent(ActivateInstrumentData instrumentData, ConsumableRecord consumableRecord)
    {
        super(instrumentData, null);

        this.state = State.idle;
        this.consumableRecord = consumableRecord;

        this.component = instrumentData.getComponent(ClientInstrumentActivatorComponentData.class);
    }

    @Override
    public void update(float dt)
    {
        switch (state)
        {
            case idle:
            {
                if (isLaunching())
                {
                    setState(State.launching);
                }

                break;
            }
            case launching:
            {
                if (!isLaunching())
                {
                    doLaunch();
                }

                break;
            }
            case launched:
            {
                if (!isLaunching())
                {
                    setState(State.idle);
                }
            }
        }
    }

    private void doLaunch()
    {
        component.activate(consumableRecord);

        setState(State.launched);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case beginLaunch:
                    {
                        beginLaunching();

                        return true;
                    }

                    case endLaunch:
                    {
                        endLaunching();

                        return true;
                    }
                }

                return false;
            }
        }

        return false;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public boolean isLaunching()
    {
        return launching;
    }

    private void endLaunching()
    {
        launching = false;
    }

    private void beginLaunching()
    {
        launching = true;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }
}
