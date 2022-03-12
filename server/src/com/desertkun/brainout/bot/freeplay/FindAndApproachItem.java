package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.EnemyNoticedCallback;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.MapDimensionsGraph;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class FindAndApproachItem extends Task
{
    private final ItemPredicate predicate;
    private final EnemyNoticedCallback enemyNoticedCallback;

    private class FoundItem
    {
        public ItemData activeData;
        public float distance;
        public ConsumableRecord record;
        public int amount;
    }

    public interface ItemPredicate
    {
        int matches(ItemData itemData, ConsumableRecord record);
        void notFound();
    }

    public FindAndApproachItem(TaskStack stack, ItemPredicate predicate,
                               EnemyNoticedCallback enemyNoticedCallback)
    {
        super(stack);

        this.predicate = predicate;
        this.enemyNoticedCallback = enemyNoticedCallback;
    }

    @Override
    protected void update(float dt)
    {
        Map myMap = getMap();
        if (myMap == null)
            return;

        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return;

        Vector2 in = new Vector2(), out = new Vector2();

        Array<FoundItem> foundItems = new Array<>();

        for (Map map : Map.All())
        {
            if (!filterMap(map))
                continue;

            if (map == myMap || MapDimensionsGraph.IsNeighbor(myMap, map))
            {
                for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.ITEM, false))
                {
                    if (!(activeData instanceof ItemData))
                        continue;

                    ItemData itemData = ((ItemData) activeData);

                    ConsumableRecord found = null;
                    int needAmount = 0;

                    for (ObjectMap.Entry<Integer, ConsumableRecord> entry : itemData.getRecords().getData())
                    {
                        ConsumableRecord record = entry.value;

                        needAmount = predicate.matches(itemData, record);
                        if (needAmount > 0)
                        {
                            found = record;
                            break;
                        }
                    }

                    if (found == null)
                        continue;

                    float distance;

                    if (map == myMap)
                    {
                        distance = Vector2.dst(playerData.getX(), playerData.getY(), activeData.getX(), activeData.getY());
                    }
                    else
                    {
                        if (!WayPointMap.FindDoors(playerData.getX(), playerData.getY(), myMap, map, in, out))
                            continue;

                        distance = Vector2.dst(playerData.getX(), playerData.getY(), in.x, in.y) +
                                Vector2.dst(out.x, out.y, activeData.getX(), activeData.getY());
                    }

                    FoundItem foundItem = new FoundItem();

                    foundItem.activeData = itemData;
                    foundItem.distance = distance;
                    foundItem.record = found;
                    foundItem.amount = needAmount;

                    foundItems.add(foundItem);
                }
            }
        }

        if (foundItems.size == 0)
        {
            pop();
            predicate.notFound();
            return;
        }

        foundItems.sort((o2, o1) -> (int) (o2.distance - o1.distance));
        FoundItem best = foundItems.first();
        popMeAndPushTask(new ApproachItem(getStack(),
            best.activeData, best.record, best.amount, enemyNoticedCallback));
    }

    private boolean filterMap(Map map)
    {
        if (BrainOutServer.Controller.getGameMode().isGameActive())
        {
            return !map.isSafeMap();
        }

        return map.isSafeMap();
    }
}
