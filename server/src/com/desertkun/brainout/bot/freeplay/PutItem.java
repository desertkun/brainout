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
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class PutItem extends Task
{
    private final ConsumableRecord record;
    private final ItemData itemData;
    private final int putAmount;
    private final EnemyNoticedCallback enemyNoticedCallback;
    private float timer;
    private boolean reachedItem;
    private Runnable done;

    public PutItem(TaskStack stack, ItemData itemData, ConsumableRecord record,
                   int putAmount, EnemyNoticedCallback enemyNoticedCallback,
                   Runnable done)
    {
        super(stack);
        this.record = record;
        this.itemData = itemData;
        this.putAmount = putAmount;
        this.enemyNoticedCallback = enemyNoticedCallback;
        this.done = done;
    }

    protected void putTheItemIn()
    {
        put();
        pop();
        this.done.run();
    }

    protected void put()
    {
        Map map = itemData.getMap();

        if (map == null)
            return;

        ConsumableItem consumableItem = this.record.getItem();
        Content content = consumableItem.getContent();
        if (!(content instanceof ConsumableContent))
            return;

        ConsumableContent cc = ((ConsumableContent) content);

        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);
        ConsumableContainer.AcquiredConsumables took = poc.getConsumableContainer().getConsumable(putAmount, this.record);

        if (took.amount < putAmount)
        {
            return;
        }

        itemData.getRecords().putConsumable(putAmount, cc.acquireConsumableItem(), took.quality);
        poc.getConsumableContainer().updateWeight();

        BrainOutServer.PostRunnable(() -> BrainOutServer.Controller.getClients().sendTCP(
            new InventoryItemMovedMsg(getPlayerData(), cc)));

        itemData.updated();
    }

    private void followItem()
    {
        if (!getController().isFollowing(itemData))
        {
            getController().follow(itemData,
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
        if (reachedItem && Vector2.dst2(itemData.getX(), itemData.getY(),
                getPlayerData().getX(), getPlayerData().getY()) < 3.0f * 3.0f)
        {
            putTheItemIn();
            return;
        }

        followItem();
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
