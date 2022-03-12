package com.desertkun.brainout.components.my;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.client.ThrowableLaunchMsg;
import com.desertkun.brainout.content.components.ClientThrowableComponent;
import com.desertkun.brainout.data.components.ClientThrowableComponentData;
import com.desertkun.brainout.data.components.PlayerAnimationComponentData;
import com.desertkun.brainout.data.components.ThrowableAnimationComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.ThrowableInstrumentData;
import com.desertkun.brainout.data.interfaces.FlippedLaunchData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.LaunchEffectEvent;

public class MyThrowableComponent extends Component
{
    private final ThrowableInstrumentData instrumentData;
    private final ConsumableRecord consumableRecord;
    protected State state;
    private boolean launching;

    private ClientThrowableComponentData component;
    protected float timer;

    public enum State
    {
        idle,
        launching,
        launched
    }

    public ConsumableRecord getConsumableRecord()
    {
        return consumableRecord;
    }

    public MyThrowableComponent(ThrowableInstrumentData instrumentData, ConsumableRecord consumableRecord)
    {
        super(instrumentData, null);

        this.instrumentData = instrumentData;
        this.state = State.idle;
        this.consumableRecord = consumableRecord;

        this.component = instrumentData.getComponent(ClientThrowableComponentData.class);
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
                    activated();
                }

                break;
            }
            case launching:
            {
                timer -= dt;

                if (timer < 0 && !isLaunching())
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

    protected void activated()
    {
        setState(State.launching);
        timer = getTimer();
    }

    protected float getTimer()
    {
        return 0;
    }

    protected void doLaunch()
    {
        ThrowableAnimationComponentData tac =
            instrumentData.getComponent(ThrowableAnimationComponentData.class);

        launch(tac.getLaunchPointData());

        setState(State.launched);
    }

    private LaunchData getPlayerData()
    {
        if (instrumentData.getOwner() != null)
        {
            return instrumentData.getOwner().getComponent(PlayerAnimationComponentData.class).getPrimaryLaunchData();
        }

        return null;
    }

    private void launch(LaunchData launchData)
    {
        thrown();

        ThrowableAnimationComponentData tac =
                instrumentData.getComponent(ThrowableAnimationComponentData.class);

        BrainOutClient.ClientController.sendUDP(
            new ThrowableLaunchMsg(launchData, consumableRecord));

        BrainOut.EventMgr.sendDelayedEvent(getComponentObject(),
            LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.shoot,
                    tac.getLaunchPointData()));
    }

    protected void thrown()
    {

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
