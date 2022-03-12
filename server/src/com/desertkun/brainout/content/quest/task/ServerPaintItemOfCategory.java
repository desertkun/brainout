package com.desertkun.brainout.content.quest.task;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.content.shop.SlotItem;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayItemActivatedEvent;
import com.desertkun.brainout.events.FreePlayItemPaintedEvent;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.PaintItemOfCategory")
public class ServerPaintItemOfCategory extends PaintItemOfCategory implements ServerTask
{
    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayItemPainted:
            {
                FreePlayItemPaintedEvent ev = ((FreePlayItemPaintedEvent) event);

                Instrument instrument = ev.instrumentData.getInstrument();
                if (instrument != null)
                {
                    SlotItem slot = instrument.getSlotItem();
                    if (slot != null && getCategory().equals(slot.getCategory()))
                    {
                        ServerTask.Trigger(this, ev.player, 1);
                    }
                }

            }
        }

        return false;
    }

    @Override
    public void started(ServerFreeRealization free, PlayerClient playerClient)
    {
        //
    }
}
