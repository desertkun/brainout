package com.desertkun.brainout.content.quest.task;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.KillEvent;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.KillWith")
public class ServerKillWith extends KillWith implements ServerTask
{
    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case kill:
            {
                KillEvent ev = ((KillEvent) event);

                if (ev.killer instanceof PlayerClient)
                {
                    PlayerClient playerClient = ((PlayerClient) ev.killer);
                    if (ev.instrument != null)
                    {
                        if (getCategory() != null)
                        {
                            InstrumentSlotItem slotItem = ev.instrument.getSlotItem();
                            if (slotItem != null && slotItem.getCategory() != null)
                            {
                                if (slotItem.getCategory().equals(getCategory()))
                                {
                                    ServerTask.Trigger(this, playerClient, 1);
                                }
                            }
                        }
                        else if (getWeapon() != null)
                        {
                            if (getWeapon() == ev.instrument)
                            {
                                ServerTask.Trigger(this, playerClient, 1);
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void started(ServerFreeRealization free, PlayerClient playerClient)
    {
    }
}
