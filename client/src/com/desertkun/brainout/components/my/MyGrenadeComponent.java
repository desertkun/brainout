package com.desertkun.brainout.components.my;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.common.msg.client.SimpleInstrumentActionMsg;
import com.desertkun.brainout.common.msg.client.WeaponActionMsg;
import com.desertkun.brainout.content.components.TimeToLiveComponent;
import com.desertkun.brainout.content.instrument.Grenade;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.instrument.GrenadeData;
import com.desertkun.brainout.data.instrument.ThrowableInstrumentData;
import com.desertkun.brainout.events.InstrumentActionEvent;

public class MyGrenadeComponent extends MyThrowableComponent
{
    private final GrenadeData grenadeData;

    public MyGrenadeComponent(GrenadeData grenadeData,
                              ConsumableRecord consumableRecord)
    {
        super(grenadeData, consumableRecord);

        this.grenadeData = grenadeData;
    }

    private void pullThePin()
    {
        ActiveData owner = grenadeData.getOwner();

        if (owner == null)
            return;

        Map map = owner.getMap();

        if (map == null)
            return;

        ChunkData chunk = map.getChunkAt((int)owner.getX(), (int)owner.getY());

        if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
            return;

        Instrument.Action action = Instrument.Action.cock;

        BrainOut.EventMgr.sendDelayedEvent(grenadeData.getOwner(), InstrumentActionEvent.obtain(action, 0, 0));

        BrainOutClient.ClientController.sendUDP(
            new SimpleInstrumentActionMsg(getConsumableRecord(),
                Instrument.Action.cock));
    }

    @Override
    protected void thrown()
    {
        grenadeData.setForceSelect(false);
    }

    @Override
    protected void activated()
    {
        super.activated();

        pullThePin();

        grenadeData.setForceSelect(true);
    }

}
