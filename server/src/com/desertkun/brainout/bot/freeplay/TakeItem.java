package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.bot.EnemyNoticedCallback;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskDestroyBlocks;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.common.msg.server.InventoryItemMovedMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.AutoConvertConsumable;
import com.desertkun.brainout.content.components.UniqueComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class TakeItem extends Task
{
    protected final ItemData item;
    protected final int recordId;
    protected final EnemyNoticedCallback enemyNoticedCallback;
    protected final int takeAmount;
    protected float timer, complete;
    protected boolean reachedItem;
    protected FindItem.ItemTakenCallback itemTakenCallback;

    public TakeItem(TaskStack stack, ItemData item, ConsumableRecord record, int takeAmount,
                    EnemyNoticedCallback enemyNoticedCallback, FindItem.ItemTakenCallback itemTakenCallback)
    {
        super(stack);

        this.item = item;
        this.recordId = record.getId();
        this.enemyNoticedCallback = enemyNoticedCallback;
        this.takeAmount = takeAmount;
        this.itemTakenCallback = itemTakenCallback;
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.1f;

        if (enemyNoticedCallback != null && checkEnemy())
            return;

        // we have reached the item
        if (reachedItem && Vector2.dst2(item.getX(), item.getY(),
                getPlayerData().getX(), getPlayerData().getY()) < 3.0f * 3.0f)
        {
            takeOutTheItem();
            return;
        }

        followItem();
    }

    protected void takeOutTheItem()
    {
        ConsumableRecord record = item.getRecords().get(recordId);

        if (record == null)
        {
            pop();
            return;
        }

        pickUpRecordItem(item, record, takeAmount);
        pop();

        itemTakenCallback.found(record);
    }

    protected void pickUpRecordItem(ItemData itemData, ConsumableRecord record, int amount)
    {
        Map map = itemData.getMap();

        if (map == null)
            return;

        ConsumableItem consumableItem = record.getItem();

        Content content = consumableItem.getContent();

        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);

        poc.getConsumableContainer().updateWeight();

        ConsumableContainer.AcquiredConsumables took = itemData.getRecords().getConsumable(amount, record);

        if (took.amount > 0)
        {
            ConsumableItem putItem = record.getItem();

            AutoConvertConsumable auto = putItem.getContent().getComponent(AutoConvertConsumable.class);

            if (auto != null)
            {
                putItem = auto.getConvertTo().acquireConsumableItem();
            }

            poc.getConsumableContainer().putConsumable(took.amount, putItem, took.quality);

            BrainOutServer.PostRunnable(() -> BrainOutServer.Controller.getClients().sendTCP(
                new InventoryItemMovedMsg(getPlayerData(), content)));

            itemData.updated();

            if (itemData.getRecords().isEmpty() && itemData.isAutoRemove())
            {
                map.removeActive(itemData, true);
            }
        }
    }

    private void followItem()
    {
        if (!getController().isFollowing(item))
        {
            getController().follow(item,
                () -> {
                    reachedItem = true;
                    getController().stopFollowing();
                },
                this::failed,
                this::gotBlocksInOurWay);
        }
    }

    private void failed()
    {
        pop();
    }

    private void gotBlocksInOurWay(Queue<WayPointMap.BlockCoordinates> blocks)
    {
        if (getStack().getTasks().size > 0 &&
                getStack().getTasks().last() instanceof TaskDestroyBlocks)
            return;

        pushTask(new TaskDestroyBlocks(getStack(), blocks));
    }

    private boolean checkEnemy()
    {
        ActiveData enemy = checkForEnemies();

        if (enemy != null)
        {
            return enemyNoticedCallback.noticed(getStack(), enemy);
        }

        return false;
    }
}
