package com.desertkun.brainout.data.components;

import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.InstrumentHealthComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.DamageEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("InstrumentHealthComponent")
@ReflectAlias("data.components.InstrumentHealthComponentData")
public class InstrumentHealthComponentData extends HealthComponentData<InstrumentHealthComponent>
{
    private final InstrumentData instrumentData;

    public InstrumentHealthComponentData(InstrumentData instrumentData,
                                         InstrumentHealthComponent healthComponent)
    {
        super(instrumentData, healthComponent);

        this.instrumentData = instrumentData;
    }

    @Override
    protected void onZeroHealth(DamageEvent e)
    {
        ActiveData playerData = instrumentData.getOwner();

        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        ServerPlayerControllerComponentData pcc =
            playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

        if (poc != null && pcc != null)
        {
            ConsumableRecord record = poc.findRecord(instrumentData);

            if (record != null && record.getAmount() > 0)
            {
                poc.getConsumableContainer().removeRecord(record, true);

                pcc.selectFirstInstrument(poc);
                pcc.consumablesUpdated();
            }
        }
    }
}
