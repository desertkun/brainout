package com.desertkun.brainout.mode.freeplay;

import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.impl.DefaultConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.ConsumableSlotItem;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.esotericsoftware.minlog.Log;

public class SpecialInstruments
{
    public static void generate(ServerFreeRealization free, ConsumableContainer cnt, String dimension)
    {
        InstrumentSlotItem slot = free.getRandomSpecialItem();

        if (slot == null)
            return;

        Instrument instrument = slot.getInstrument();
        if (Log.INFO) Log.info("Generated instrument " + instrument.getID());
        InstrumentData instrumentData = instrument.getData(dimension);

        // skin
        {
            instrumentData.setSkin(instrument.getDefaultSkin());
        }

        cnt.putConsumable(1, new InstrumentConsumableItem(instrumentData, dimension));

        for (ConsumableSlotItem.ItemCargo cargo : slot.getCargo())
        {
            if (cargo.getContent() instanceof ConsumableContent)
            {
                cnt.putConsumable(cargo.getAmount(),
                    new DefaultConsumableItem(((ConsumableContent) cargo.getContent())));
            }
        }
    }
}
