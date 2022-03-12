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

@Reflect("content.quest.task.TakeOutRandomItem")
public class ServerTakeOutRandomItem extends TakeOutRandomItem implements ServerTask
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
    protected long getCurrentTime()
    {
        return System.currentTimeMillis() / 1000L;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayItemTakenOut:
            {
                FreePlayItemsTakenOutEvent ev = ((FreePlayItemsTakenOutEvent) event);

                int am = ev.items.get(getItem(ev.player.getAccount()), 0);

                int used = ServerTask.Trigger(this, ev.player, am);
                if (used > 0)
                {
                    ev.used.put(getItem(ev.player.getAccount()), used);
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

                ConsumableItem consumableItem = new DefaultConsumableItem(getItem(playerClient.getAccount()));
                consumableItem.setPrivate(playerClient.getId());

                playerClient.log("Spawned private item " + getItem(playerClient.getAccount()).getID() +
                    " for task " + getId() + " tag " +
                    generateTag);

                cnt.putConsumable(1, consumableItem);

                itemData.updated();
            }
        }
    }
}
