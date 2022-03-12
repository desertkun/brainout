package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.DefaultConsumableItem;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayItemsTakenOutEvent;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;

@Reflect("content.quest.task.TakeOutItem")
public class ServerTakeOutItem extends TakeOutItem implements ServerTask
{
    private boolean generatePrivately = false;
    private String generateTag = "";
    private int generateAmount = 1;

    @Override
    protected void readTask(JsonValue jsonData)
    {
        super.readTask(jsonData);

        if (jsonData.has("generate-privately"))
        {
            generatePrivately = jsonData.getBoolean("generate-privately", false);
        }

        if (jsonData.has("generate-tag"))
        {
            generateTag = jsonData.getString("generate-tag", "");
        }

        if (jsonData.has("generate-amount"))
        {
            generateAmount = jsonData.getInt("generate-amount", 1);
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayItemTakenOut:
            {
                FreePlayItemsTakenOutEvent ev = ((FreePlayItemsTakenOutEvent) event);

                int am = ev.items.get(getItem(), 0);

                int used = ServerTask.Trigger(this, ev.player, am);
                if (used > 0)
                {
                    ev.used.put(getItem(), used);
                }
            }
        }

        return false;
    }

    @Override
    public void started(ServerFreeRealization free, PlayerClient playerClient)
    {
        if (generatePrivately)
        {
            for (int i = 0; i < generateAmount; i++)
            {
                ItemData itemData = free.getRandomItem(generateTag);

                if (itemData == null)
                    return;

                ConsumableContainer cnt = itemData.getRecords();

                ConsumableItem consumableItem = new DefaultConsumableItem(getItem());
                consumableItem.setPrivate(playerClient.getId());

                playerClient.log("Spawned private item " + getItem().getID() + " for task " + getId() + " tag " +
                        generateTag);

                cnt.putConsumable(1, consumableItem);

                itemData.updated();
            }
        }
    }
}
