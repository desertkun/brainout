package com.desertkun.brainout.mode.freeplay;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.consumable.impl.PlayerSkinConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.InstrumentSkin;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.esotericsoftware.minlog.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class FreeplayItems
{
    private Array<TrackItem> trackItems;
    private ObjectMap<String, TrackItem> currentExclusives;
    private ObjectMap<String, Array<TrackItem>> exclusives;

    public interface ItemQuery
    {
        Array<ItemData> query(String tag, boolean noPlayer);
    }

    public static abstract class TrackItem
    {
        public TrackItem(JSONObject data)
        {
            this.amount = data.optInt("amount", 1);
            this.tag = data.optString("tag", "");
        }

        public String getTag()
        {
            return tag;
        }

        public int getAmount()
        {
            return amount;
        }

        private int find(Array<ItemData> items)
        {
            int cnt = 0;

            if (!noPlayer)
            {
                for (Map map : Map.All())
                {
                    if (map.isSafeMap())
                    {
                        continue;
                    }

                    for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
                    {
                        PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

                        if (poc == null)
                            continue;

                        for (ConsumableRecord record : poc.getConsumableContainer().getData().values())
                        {
                            if (!"".equals(tag))
                            {
                                if (!tag.equals(record.getTag()))
                                {
                                    continue;
                                }
                            }

                            if (matches(record.getItem()))
                            {
                                cnt += record.getAmount();
                            }
                        }
                    }
                }

                // discount stuff players have, by two
                cnt /= 2;
            }

            if (items == null)
            {
                return cnt;
            }

            for (ItemData item : items)
            {
                for (ObjectMap.Entry<Integer, ConsumableRecord> entry : item.getRecords().getData())
                {
                    ConsumableRecord record = entry.value;

                    if (matches(record.getItem()))
                    {
                        cnt += record.getAmount();
                    }
                }
            }

            return cnt;
        }

        public boolean isEnough(ItemQuery query)
        {
            Array<ItemData> items = query.query(this.tag, noPlayer);
            if (items == null)
                return true;
            return find(items) >= getAmount();
        }

        public void enforce(ItemQuery query)
        {
            Array<ItemData> items = query.query(this.tag, noPlayer);
            if (items == null)
                return;
            int have = find(items);
            if (getAmount() > have)
            {
                create(items, getAmount() - have);
            }
        }

        protected abstract boolean matches(ConsumableItem item);
        public abstract void create(Array<ItemData> items, int need);

        private int amount;
        private boolean noPlayer;
        private String tag;

        public void setNoPlayer(boolean noPlayer)
        {
            this.noPlayer = noPlayer;
        }

        public boolean isNoPlayer()
        {
            return noPlayer;
        }
    }

    public static class ConsumableTrackItem extends TrackItem
    {
        private ConsumableContent item;
        private IntArray groups;

        public ConsumableTrackItem(JSONObject data, ConsumableContent item)
        {
            super(data);

            this.item = item;

            if (data.has("group"))
            {
                this.groups = new IntArray();
                JSONArray g = data.getJSONArray("group");
                for (int i = 0, t = g.length(); i < t; i++)
                {
                    this.groups.add(g.getInt(i));
                }
                this.groups.sort();
            }
        }

        public ConsumableContent getItem()
        {
            return item;
        }

        @Override
        protected boolean matches(ConsumableItem item)
        {
            return item.getContent() == this.item;
        }

        @Override
        public void create(Array<ItemData> items, int need)
        {
            if (groups == null)
            {
                // spread them around
                for (int i = 0; i < need; i++)
                {
                    ItemData createAt = items.random();
                    createAt(createAt, 1);
                    createAt.updated();
                }
            }
            else
            {
                while (need > 0)
                {
                    int max = -1;
                    for (int i = this.groups.size - 1; i >= 0; i--)
                    {
                        int sz = this.groups.get(i);
                        if (sz <= need)
                        {
                            max = i;
                            break;
                        }
                    }

                    ItemData createAt = items.random();

                    if (max >= 0)
                    {
                        int sz = this.groups.get(MathUtils.random(0, max));
                        createAt(createAt, sz);
                        createAt.updated();
                        need -= sz;
                    }
                    else
                    {
                        createAt(createAt, need);
                        createAt.updated();
                        return;
                    }
                }
            }
        }

        private void createAt(ItemData createAt, int amount)
        {
            if (Log.INFO) Log.info("Generated " + amount + " of " + this.item.getID());
            ConsumableRecord record =
                createAt.getRecords().putConsumable(amount, this.item.acquireConsumableItem());
            if (record != null && !"".equals(getTag()))
            {
                record.setTag(getTag());
            }
        }
    }

    public static class InstrumentTrackItem extends TrackItem
    {
        private Instrument instrument;
        private InstrumentSkin skin;
        public ObjectMap<String, Upgrade> upgrades;
        private Array<Bonus> bonuses;

        private static class Bonus
        {
            private ConsumableContent cnt;
            private int amount;
        }

        public InstrumentTrackItem(JSONObject data, Instrument instrument)
        {
            super(data);

            this.instrument = instrument;

            JSONArray bonus = data.optJSONArray("bonus");
            if (bonus != null)
            {
                this.bonuses = new Array<>();
                for (int i = 0, t = bonus.length(); i < t; i++)
                {
                    JSONObject b = bonus.getJSONObject(i);
                    Bonus bb = new Bonus();
                    bb.cnt = BrainOutServer.ContentMgr.get(b.getString("item"), ConsumableContent.class);
                    bb.amount = b.getInt("amount");
                    this.bonuses.add(bb);
                }
            }

            if (data.has("skin"))
            {
                this.skin = BrainOutServer.ContentMgr.get(data.getString("skin"), InstrumentSkin.class);
            }

            if (data.has("upgrades"))
            {
                this.upgrades = new ObjectMap<>();
                JSONObject up = data.getJSONObject("upgrades");

                for (String key : up.keySet())
                {
                    Upgrade upgrade = BrainOutServer.ContentMgr.get(up.getString(key), Upgrade.class);

                    if (upgrade == null)
                        continue;

                    this.upgrades.put(key, upgrade);
                }
            }
        }

        @Override
        protected boolean matches(ConsumableItem item)
        {
            if (!(item instanceof InstrumentConsumableItem))
            {
                return false;
            }

            InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
            return ici.getInstrumentData().getInstrument() == this.instrument;
        }

        @Override
        public void create(Array<ItemData> items, int need)
        {
            if (Log.INFO) Log.info("Generated " + need + " of " + this.instrument.getID());

            for (int i = 0; i < need; i++)
            {
                ItemData createAt = items.random();

                InstrumentData instrumentData = instrument.getData(createAt.getDimension());
                instrumentData.setSkin(skin == null ? instrument.getDefaultSkin() : skin);

                if (upgrades != null)
                {
                    for (ObjectMap.Entry<String, Upgrade> entry : upgrades)
                    {
                        instrumentData.getUpgrades().put(entry.key, entry.value);
                    }
                }

                ConsumableRecord record = createAt.getRecords().putConsumable(1,
                    new InstrumentConsumableItem(instrumentData, createAt.getDimension()));
                if (record != null && !"".equals(getTag()))
                {
                    record.setTag(getTag());
                }

                if (bonuses != null)
                {
                    for (Bonus bonus : bonuses)
                    {
                        createAt.getRecords().putConsumable(bonus.amount, bonus.cnt.acquireConsumableItem());
                    }
                }

                createAt.updated();
            }
        }
    }

    public static class SkinTrackItem extends TrackItem
    {
        private PlayerSkin skin;

        public SkinTrackItem(JSONObject data, PlayerSkin skin)
        {
            super(data);

            this.skin = skin;
        }

        @Override
        protected boolean matches(ConsumableItem item)
        {
            if (!(item instanceof PlayerSkinConsumableItem))
            {
                return false;
            }
            PlayerSkinConsumableItem psci = ((PlayerSkinConsumableItem) item);
            return psci.getContent() == skin;
        }

        @Override
        public void create(Array<ItemData> items, int need)
        {
            if (Log.INFO) Log.info("Generated " + need + " of " + this.skin.getID());

            for (int i = 0; i < need; i++)
            {
                ItemData createAt = items.random();
                ConsumableRecord record =
                    createAt.getRecords().putConsumable(1, new PlayerSkinConsumableItem(skin));
                if (record != null && !"".equals(getTag()))
                {
                    record.setTag(getTag());
                }

                createAt.updated();
            }
        }
    }

    public FreeplayItems(JSONArray items)
    {
        this.trackItems = new Array<>();
        this.currentExclusives = new ObjectMap<>();
        this.exclusives = new ObjectMap<>();

        for (int i = 0, t = items.length(); i < t; i++)
        {
            JSONObject itm = items.getJSONObject(i);
            String item = itm.getString("item");
            String exclusive = itm.optString("exclusive", "");
            boolean noPlayer = itm.optBoolean("no_player", false);
            Content content = BrainOutServer.ContentMgr.get(item, Content.class);

            TrackItem trackItem;

            if (content instanceof Instrument)
            {
                trackItem = new InstrumentTrackItem(itm, ((Instrument) content));
            }
            else if (content instanceof ConsumableContent)
            {
                trackItem = new ConsumableTrackItem(itm, ((ConsumableContent) content));
            }
            else if (content instanceof PlayerSkin)
            {
                trackItem = new SkinTrackItem(itm, ((PlayerSkin) content));
            }
            else
            {
                continue;
            }

            trackItem.setNoPlayer(noPlayer);

            if (exclusive.isEmpty())
            {
                this.trackItems.add(trackItem);
            }
            else
            {
                Array<TrackItem> a = this.exclusives.get(exclusive);
                if (a == null)
                {
                    a = new Array<>();
                    this.exclusives.put(exclusive, a);
                }

                a.add(trackItem);
            }
        }
    }

    public void enforce(ItemQuery query)
    {
        for (ObjectMap.Entry<String, Array<TrackItem>> entry : this.exclusives)
        {
            String exclusive = entry.key;
            TrackItem current = this.currentExclusives.get(exclusive);
            if (current != null && current.isEnough(query))
            {
                continue;
            }

            // time for a new one!
            current = this.exclusives.get(exclusive).random();
            Array<ItemData> items = query.query(current.tag, current.noPlayer);
            if (items == null)
            {
                this.exclusives.remove(exclusive);
                continue;
            }
            current.create(items, 1);
            this.currentExclusives.put(exclusive, current);
        }

        for (TrackItem item : this.trackItems)
        {
            item.enforce(query);
        }
    }
}
