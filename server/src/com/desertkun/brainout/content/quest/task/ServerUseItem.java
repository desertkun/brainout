package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.DefaultConsumableItem;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayItemUsedEvent;
import com.desertkun.brainout.events.FreePlayItemsTakenOutEvent;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.UseItem")
public class ServerUseItem extends UseItem implements ServerTask
{
    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayItemUsed:
            {
                FreePlayItemUsedEvent ev = ((FreePlayItemUsedEvent) event);

                if (getItems().contains(ev.item, true))
                {
                    ServerTask.Trigger(this, ev.player, ev.amount);
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
