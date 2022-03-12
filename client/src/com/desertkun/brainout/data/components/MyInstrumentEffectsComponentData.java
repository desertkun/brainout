package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.client.ClientInstrumentEffectMsg;
import com.desertkun.brainout.common.msg.client.SimpleInstrumentActionMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.MyInstrumentEffectsComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.CustomInstrumentEffectEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.InstrumentActionEvent;

public class MyInstrumentEffectsComponentData extends MyInstrumentComponentData<MyInstrumentEffectsComponent>
{
    public MyInstrumentEffectsComponentData(ComponentObject componentObject,
                                            MyInstrumentEffectsComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case gameController:
            {
                GameControllerEvent e = ((GameControllerEvent) event);

                switch (e.action)
                {
                    case custom:
                    {
                        onCustomEffect(e.string);

                        break;
                    }
                }
            }
        }

        return super.onEvent(event);
    }

    private void onCustomEffect(String v)
    {
        if (!isMyInstrument())
            return;

        InstrumentData instrumentData = ((InstrumentData) getComponentObject());

        InstrumentEffectsComponentData effects =
            getComponentObject().getComponentWithSubclass(InstrumentEffectsComponentData.class);

        if (effects == null)
            return;

        ActiveData owner = getOwner();

        if (owner == null)
            return;

        PlayerOwnerComponent poc = owner.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return;

        String effectKey = "custom-" + v;

        if (!effects.getEffects().hasEffects(effectKey))
            return;

        BrainOut.EventMgr.sendDelayedEvent(owner,
            InstrumentActionEvent.obtain(Instrument.Action.hit, 0, 0));

        BrainOutClient.ClientController.sendUDP(
            new SimpleInstrumentActionMsg(poc.getCurrentInstrumentRecord(),
                Instrument.Action.hit));

        BrainOutClient.ClientController.sendUDP(new ClientInstrumentEffectMsg(
            owner, instrumentData, effectKey
        ));

        BrainOut.EventMgr.sendDelayedEvent(instrumentData,
            CustomInstrumentEffectEvent.obtain(effectKey));
    }
}
