package com.desertkun.brainout.mode.freeplay;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.consumable.impl.PlayerSkinConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import org.json.JSONArray;
import org.json.JSONObject;

public class ItemsCollection
{
    private String tag;
    private int amount;
    private Array<ItemsCollection.Item> items;

    public abstract class Item
    {
        public Item(JSONObject data)
        {

        }

        public Item(JsonValue data)
        {

        }

        public abstract void generate(ConsumableContainer container, String dimension);
        public abstract void generate(ConsumableContainer container, String dimension, ActiveData owner);
        public abstract void generate(Array<ConsumableRecord> records, String dimension);
    }

    public class DefaultItem extends ItemsCollection.Item
    {
        private ConsumableContent item;
        private int amount;

        public DefaultItem(JSONObject data)
        {
            super(data);

            amount = data.optInt("amount", 1);
            item = BrainOutServer.ContentMgr.get(data.optString("item"), ConsumableContent.class);
        }

        public DefaultItem(JsonValue data)
        {
            super(data);

            amount = data.getInt("amount", 1);
            item = BrainOutServer.ContentMgr.get(data.getString("item"), ConsumableContent.class);
        }

        @Override
        public void generate(ConsumableContainer container, String dimension)
        {
            container.putConsumable(amount, item.acquireConsumableItem());
        }

        @Override
        public void generate(ConsumableContainer container, String dimension, ActiveData owner)
        {
            container.putConsumable(amount, item.acquireConsumableItem());
        }

        @Override
        public void generate(Array<ConsumableRecord> records, String dimension)
        {
            records.add(new ConsumableRecord(item.acquireConsumableItem(), amount, -1));
        }
    }

    public class PlayerSkinItem extends ItemsCollection.Item
    {
        private PlayerSkin item;
        private int amount;

        public PlayerSkinItem(JSONObject data)
        {
            super(data);

            amount = data.optInt("amount", 1);
            item = BrainOutServer.ContentMgr.get(data.optString("item"), PlayerSkin.class);
        }

        public PlayerSkinItem(JsonValue data)
        {
            super(data);

            amount = data.getInt("amount", 1);
            item = BrainOutServer.ContentMgr.get(data.getString("item"), PlayerSkin.class);
        }

        @Override
        public void generate(ConsumableContainer container, String dimension, ActiveData owner)
        {
            container.putConsumable(amount, new PlayerSkinConsumableItem(item));
        }

        @Override
        public void generate(ConsumableContainer container, String dimension)
        {
            container.putConsumable(amount, new PlayerSkinConsumableItem(item));
        }

        @Override
        public void generate(Array<ConsumableRecord> records, String dimension)
        {
            records.add(new ConsumableRecord(new PlayerSkinConsumableItem(item), amount, -1));
        }
    }

    public class InstrumentItem extends ItemsCollection.Item
    {
        private InstrumentInfo info;

        public InstrumentItem(JSONObject data)
        {
            super(data);

            info = new InstrumentInfo();
            info.instrument = BrainOutServer.ContentMgr.get(data.optString("item"), Instrument.class);

            if (data.has("skin"))
            {
                info.skin = BrainOutServer.ContentMgr.get(data.optString("skin"), Skin.class);
            }
            else
            {
                info.skin = info.instrument.getDefaultSkin();
            }

            if (data.has("upgrades"))
            {
                JSONObject u = data.optJSONObject("upgrades");

                for (String group : u.keySet())
                {
                    Upgrade upgrade = BrainOutServer.ContentMgr.get(u.optString(group), Upgrade.class);

                    if (upgrade == null)
                        continue;

                    info.upgrades.put(group, upgrade);
                }
            }
        }

        public InstrumentItem(JsonValue data)
        {
            super(data);

            info = new InstrumentInfo();
            info.instrument = BrainOutServer.ContentMgr.get(data.getString("item"), Instrument.class);

            if (data.has("skin"))
            {
                info.skin = BrainOutServer.ContentMgr.get(data.getString("skin"), Skin.class);
            }
            else
            {
                info.skin = info.instrument.getDefaultSkin();
            }

            if (data.has("upgrades"))
            {
                for (JsonValue group : data.get("upgrades"))
                {
                    Upgrade upgrade = BrainOutServer.ContentMgr.get(group.asString(), Upgrade.class);

                    if (upgrade == null)
                        continue;

                    info.upgrades.put(group.name(), upgrade);
                }
            }
        }

        @Override
        public void generate(ConsumableContainer container, String dimension)
        {
            InstrumentData instrumentData = info.instrument.getData(dimension);
            instrumentData.setSkin(info.skin);

            for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
            {
                instrumentData.getUpgrades().put(entry.key, entry.value);
            }

            container.putConsumable(1, new InstrumentConsumableItem(instrumentData, dimension));
        }

        @Override
        public void generate(ConsumableContainer container, String dimension, ActiveData owner)
        {
            InstrumentData instrumentData = info.instrument.getData(dimension);
            instrumentData.setSkin(info.skin);
            instrumentData.setOwner(owner);

            for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
            {
                instrumentData.getUpgrades().put(entry.key, entry.value);
            }

            container.putConsumable(1, new InstrumentConsumableItem(instrumentData, dimension));
        }

        @Override
        public void generate(Array<ConsumableRecord> records, String dimension)
        {
            InstrumentData instrumentData = info.instrument.getData(dimension);
            instrumentData.setSkin(info.skin);

            for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
            {
                instrumentData.getUpgrades().put(entry.key, entry.value);
            }

            records.add(new ConsumableRecord(new InstrumentConsumableItem(instrumentData, dimension), 1, -1));
        }
    }

    public String getTag()
    {
        return tag;
    }

    public int getAmount()
    {
        return amount;
    }

    public Array<ItemsCollection.Item> getItems()
    {
        return items;
    }

    public ItemsCollection(JsonValue data)
    {
        items = new Array<>();

        amount = data.getInt("amount", 1);
        tag = data.getString("tag", "");

        if (data.has("items"))
        {
            for (JsonValue item : data.get("items"))
            {
                String kind = item.getString("kind", "default");

                ItemsCollection.Item newItem;

                switch (kind)
                {
                    case "instrument":
                    {
                        newItem = new ItemsCollection.InstrumentItem(item);
                        break;
                    }
                    case "skin":
                    {
                        newItem = new ItemsCollection.PlayerSkinItem(item);
                        break;
                    }
                    default:
                    {
                        newItem = new ItemsCollection.DefaultItem(item);
                        break;
                    }
                }

                items.add(newItem);
            }
        }
    }

    public ItemsCollection(JSONObject data)
    {
        items = new Array<>();

        amount = data.optInt("amount", 1);
        tag = data.optString("tag", "");

        if (data.has("items"))
        {
            JSONArray items_ = data.optJSONArray("items");

            for (int i = 0, t = items_.length(); i < t; i++)
            {
                JSONObject item = items_.optJSONObject(i);

                if (item == null)
                    continue;

                String kind = item.optString("kind", "default");

                ItemsCollection.Item newItem;

                switch (kind)
                {
                    case "instrument":
                    {
                        newItem = new ItemsCollection.InstrumentItem(item);
                        break;
                    }
                    case "skin":
                    {
                        newItem = new ItemsCollection.PlayerSkinItem(item);
                        break;
                    }
                    default:
                    {
                        newItem = new ItemsCollection.DefaultItem(item);
                        break;
                    }
                }

                items.add(newItem);
            }
        }
    }

    public void generate(ConsumableContainer container, String dimension, ActiveData owner)
    {
        for (ItemsCollection.Item item : items)
        {
            item.generate(container, dimension, owner);
        }
    }

    public void generate(ConsumableContainer container, String dimension)
    {
        for (ItemsCollection.Item item : items)
        {
            item.generate(container, dimension);
        }
    }

    public void generate(ItemData itemData)
    {
        for (ItemsCollection.Item item : items)
        {
            item.generate(itemData.getRecords(), itemData.getDimension());
        }
    }

    public void generate(Array<ConsumableRecord> records, String dimension)
    {
        for (ItemsCollection.Item item : items)
        {
            item.generate(records, dimension);
        }
    }
}
