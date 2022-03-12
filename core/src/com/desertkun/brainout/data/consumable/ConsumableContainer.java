package com.desertkun.brainout.data.consumable;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.QualityComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.ConsumableEvent;

public class ConsumableContainer implements Json.Serializable, ConsumableHolder
{
    private final ActiveData owner;
    private OrderedMap<Integer, ConsumableRecord> data;
    private ObjectMap<String, ObjectMap<Integer, ConsumableRecord>> categories;
    private float weight;
    private int lastId;
    private boolean inited;

    public ConsumableContainer(ActiveData owner)
    {
        this.data = new OrderedMap<>();
        this.categories = new ObjectMap<>(2);
        this.weight = 0;
        this.lastId = 0;
        this.owner = owner;
        this.inited = false;
    }

    public ConsumableRecord getConsumable(ConsumableContent type)
    {
        for (ConsumableRecord record: data.values())
        {
            ConsumableItem item = record.getItem();

            if (item.getContent() == type)
            {
                if (record.getAmount() > 0)
                {
                    return record;
                }
            }
        }

        return null;
    }

    public ObjectMap<Integer, ConsumableRecord> getCategory(String category)
    {
        return categories.get(category);
    }

    public ObjectMap<Integer, ConsumableRecord> acquireCategory(String category)
    {
        ObjectMap<Integer, ConsumableRecord> c = categories.get(category);

        if (c == null)
        {
            c = new ObjectMap<>();
            categories.put(category, c);
        }

        return c;
    }

    public int getTotalAmount(ConsumableContent content)
    {
        int amount = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : data)
        {
            if (entry.value.getItem().getContent() == content)
            {
                amount += entry.value.getAmount();
            }
        }

        return amount;
    }

    public int getTotalAmount()
    {
        int amount = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : data)
        {
            amount += entry.value.getAmount();
        }

        return amount;
    }

    @Override
    public int put(ConsumableItem item, int amount, int quality, String tag, boolean checkDirection)
    {
        putConsumable(amount, item, quality);

        return amount;
    }

    @Override
    public RequireResult require(int amount, String tag)
    {
        int id = Integer.valueOf(tag);
        ConsumableRecord record = get(id);

        AcquiredConsumables got = getConsumable(amount, record);
        if (record != null && record.getItem().getContent() instanceof ConsumableContent)
        {
            ConsumableContent consumableContent = ((ConsumableContent) record.getItem().getContent());
            if (got.amount > 0 && consumableContent.isThrowable())
            {
                return new RequireResult(record.getItem(), got.amount, got.quality);
            }
        }

        return new RequireResult(null, 0, -1);
    }

    public boolean isEmpty()
    {
        return data.size == 0;
    }

    public interface Predicate
    {
        boolean isValid(ConsumableRecord record);
    }

    public Array<ConsumableRecord> queryRecords(Predicate predicate)
    {
        Array<ConsumableRecord> array = new Array<>();

        for (ConsumableRecord record: data.values())
        {
            if (predicate.isValid(record))
            {
                array.add(record);
            }
        }

        return array;
    }

    public ConsumableRecord queryConsumable(ConsumableContent consumableContent)
    {
        for (ConsumableRecord record: data.values())
        {
            if (record.getItem().getContent() == consumableContent)
            {
                return record;
            }
        }

        return null;
    }

    public ConsumableRecord getConsumable(ConsumableItem type)
    {
        for (ConsumableRecord record: data.values())
        {
            if (record.getItem() == type)
            {
                if (record.getAmount() > 0)
                {
                    return record;
                }
            }
        }

        return null;
    }

    public boolean hasConsumable(ConsumableContent type)
    {
        return getConsumable(type) != null;
    }

    public boolean hasConsumable(ConsumableItem type)
    {
        for (ConsumableRecord record: data.values())
        {
            ConsumableItem item = record.getItem();

            if (item == type)
            {
                if (record.getAmount() > 0)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static class AcquiredConsumables
    {
        public int amount;
        public int quality;

        public AcquiredConsumables(int amount, int quality)
        {
            this.amount = amount;
            this.quality = quality;
        }
    }

    public AcquiredConsumables getConsumable(int amount, ConsumableRecord record)
    {
        if (record != null)
        {
            int have = record.getAmount();
            int dec = Math.min(amount, have);

            have -= dec;

            if (have > 0)
            {
                record.setAmount(have);
            }
            else
            {
                record.setAmount(0);
                removeRecord(record);
            }

            return new AcquiredConsumables(dec, record.getQuality());
        }

        return new AcquiredConsumables(0, -1);
    }

    public AcquiredConsumables getConsumable(int amount, ConsumableContent type)
    {
        // lol
        return getConsumable(amount, getConsumable(type));
    }

    public int newId()
    {
        return lastId++;
    }

    public ConsumableRecord putConsumable(int amount, ConsumableItem item)
    {
        return putConsumable(amount, item, -1);
    }

    public ConsumableRecord putConsumable(int amount, ConsumableItem item, int quality)
    {
        QualityComponent q = item.getContent().getComponent(QualityComponent.class);

        for (ConsumableRecord record: data.values())
        {
            // if we succeeded to stack
            if (record.getItem().stacks(item) &&
                ((q != null) == record.getItem().getContent().hasComponent(QualityComponent.class)))
            {
                record.getItem().stackWith(item, amount);

                if (q != null && record.hasQuality())
                {
                    int oldQuality = record.getQuality();
                    int newQuality = quality == -1 ? q.pick() : quality;

                    int qq = oldQuality * record.getAmount() + newQuality * amount;
                    int avg = qq / (record.getAmount() + amount);

                    record.setQuality(avg);
                }

                record.setAmount(record.getAmount() + amount);

                return record;
            }
        }

        ConsumableRecord record = new ConsumableRecord(item, amount, newId());

        if (quality == -1)
        {
            if (q != null)
            {
                record.setQuality(q.pick());
            }
            else if (item.hasAutoQuality())
            {
                record.setQuality(item.pickAutoQuality());
            }
        }
        else
        {
            record.setQuality(quality);
        }

        addRecord(record);

        updateWeight();

        return record;
    }

    public void init()
    {
        for (Integer entry: data.orderedKeys())
        {
            data.get(entry).init();
        }

        inited = true;
    }

    public void release()
    {
        Array<ConsumableRecord> toRemove = new Array<ConsumableRecord>();
        for (ObjectMap.Entry<Integer, ConsumableRecord> entry: data)
        {
            toRemove.add(entry.value);
        }

        for (ConsumableRecord id: toRemove)
        {
            id.release();
        }
    }

    public OrderedMap<Integer, ConsumableRecord> getData()
    {
        return data;
    }

    public void decConsumable(ConsumableContent type)
    {
        decConsumable(type, 1);
    }

    public void decConsumable(ConsumableRecord record, int amount)
    {
        if (record != null)
        {
            int have = record.getAmount();
            have -= amount;

            if (have > 0)
            {
                record.setAmount(have);
            }
            else
            {
                removeRecord(record);
            }

            updateWeight();
        }
    }

    public void addRecord(ConsumableRecord record)
    {
        if (record == null || record.getItem() == null) return;

        record.getItem().setOwner(owner);
        if (inited)
        {
            record.init();
        }

        data.put(record.getId(), record);

        if (record.getItem().getContent() instanceof ConsumableContent)
        {
            ConsumableContent cc = ((ConsumableContent) record.getItem().getContent());

            if (cc.hasCategory())
            {
                ObjectMap<Integer, ConsumableRecord> c = acquireCategory(cc.getCategory());
                c.put(record.getId(), record);
            }
        }

        BrainOut.EventMgr.sendDelayedEvent(owner, ConsumableEvent.obtain(record, ConsumableEvent.Action.added));
    }

    public void removeRecord(ConsumableRecord record, boolean release)
    {
        if (record.getItem().getContent() instanceof ConsumableContent)
        {
            ConsumableContent cc = ((ConsumableContent) record.getItem().getContent());

            if (cc.hasCategory())
            {
                ObjectMap<Integer, ConsumableRecord> c = getCategory(cc.getCategory());
                if (c != null)
                {
                    c.remove(record.getId());

                    if (c.size == 0)
                    {
                        categories.remove(cc.getCategory());
                    }
                }
            }
        }

        BrainOut.EventMgr.sendDelayedEvent(owner, ConsumableEvent.obtain(record, ConsumableEvent.Action.removed));

        if (release)
        {
            record.release();
        }

        data.remove(record.getId());
    }

    public void removeRecord(ConsumableRecord record)
    {
        removeRecord(record, true);
    }

    public void decConsumable(ConsumableItem type, int amount)
    {
        decConsumable(getConsumable(type), amount);
    }

    public void decConsumable(ConsumableContent type, int amount)
    {
        decConsumable(getConsumable(type), amount);
    }

    @Override
    public void write(Json json)
    {
        json.writeObjectStart("records");

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry: data)
        {
            json.writeValue(String.valueOf(entry.key), entry.value);
        }

        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        Array<Integer> toRemove = new Array<Integer>();
        Array<Integer> found = new Array<Integer>();

        if (jsonData.has("records"))
        {
            for (JsonValue it: jsonData.get("records"))
            {
                int id = Integer.valueOf(it.name());
                found.add(id);

                if (data.get(id) == null)
                {
                    ConsumableRecord record = new ConsumableRecord();
                    record.setId(id);
                    record.read(json, it);

                    if (!record.isValid())
                        continue;

                    addRecord(record);
                }
                else
                {
                    ConsumableRecord record = data.get(id);
                    record.read(json, it);

                    if (!record.isValid())
                    {
                        toRemove.add(record.getId());
                    }
                }

            }

            for (ObjectMap.Entry<Integer, ConsumableRecord> entry: data)
            {
                if (found.indexOf(entry.key, false) < 0)
                {
                    toRemove.add(entry.key);
                }
            }

            for (int id: toRemove)
            {
                removeRecord(data.get(id));
            }

            updateWeight();
        }
    }

    public void updateWeight()
    {
        weight = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry: data)
        {
            ConsumableRecord record = entry.value;
            Content cnt = record.getItem().getContent();

            if (cnt.hasComponent(ItemComponent.class))
            {
                ItemComponent itemComponent = cnt.getComponent(ItemComponent.class);
                weight += itemComponent.getWeight() * record.getAmount();
            }
        }
    }

    public void clear()
    {
        data.clear();
        categories.clear();
    }

    public int getIndex(ConsumableItem content)
    {
        ConsumableRecord record = getConsumable(content);

        if (record == null)
        {
            return  -1;
        }

        return data.orderedKeys().indexOf(record.getId(), false);
    }

    public int getIndex(ConsumableContent content)
    {
        ConsumableRecord record = getConsumable(content);

        if (record == null)
        {
            return  -1;
        }

        return data.orderedKeys().indexOf(record.getId(), false);
    }

    public ConsumableRecord get(int index)
    {
        return data.get(index);
    }

    public ConsumableRecord getByIndex(int index)
    {
        return data.get(data.orderedKeys().get(index));
    }

    public int size() {
        return data.size;
    }

    public int getAmount(ConsumableItem item)
    {
        int amount = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : data)
        {
            if (entry.value.getItem() == item)
                amount += entry.value.getAmount();
        }

        return amount;
    }

    public int getAmount(ConsumableContent content)
    {
        int amount = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : data)
        {
            if (entry.value.getItem().getContent() == content)
                amount += entry.value.getAmount();
        }

        return amount;
    }

    public float getWeight()
    {
        return weight;
    }

    public interface ConsumableRecordWeightPredicate
    {
        long getWeight(ConsumableRecord record);
    }

    public ConsumableRecord queryBestRecord(ConsumableRecordWeightPredicate predicate)
    {
        long bestValue = -1;
        ConsumableRecord bestRecord = null;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : getData())
        {
            long v = predicate.getWeight(entry.value);

            if (v > bestValue)
            {
                bestValue = v;
                bestRecord = entry.value;
            }
        }

        return bestRecord;
    }

    public interface ConsumableRecordOfClassPredicate<T extends Content>
    {
        boolean check(T content, ConsumableRecord record);
    }

    public <T extends Content> int queryRecordsOfClassAmount(
            ConsumableRecordOfClassPredicate<T> predicate, Class<T> tClass)
    {
        int amount = 0;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : getData())
        {
            Content content = entry.value.getItem().getContent();

            @SuppressWarnings("unchecked") T cast = (T)content;

            if (BrainOut.R.instanceOf(tClass, content))
            {
                if (predicate.check(cast, entry.value))
                {
                    amount += entry.value.getAmount();
                }
            }
        }

        return amount;
    }
}
