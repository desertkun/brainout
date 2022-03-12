package com.desertkun.brainout.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.active.RealEstateItem;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.components.RealEstateItemContainerComponent;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import org.anthillplatform.runtime.services.MarketService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RealEstateInfo
{
    public String owner;
    public String name;
    public RealEstatePayload payload;

    public static class RealEstatePayload
    {
        public String content;
        public String id;
        public String location;
        public String masterMap;
        private ObjectMap<String, ObjectAtLocation> items;

        public RealEstatePayload(String masterMap)
        {
            this.masterMap = masterMap;
        }

        public ObjectMap<String, ObjectAtLocation> getItems()
        {
            if (items == null)
            {
                items = new ObjectMap<>();
            }

            return items;
        }

        private boolean doesColludeWithOtherObjects(
            String map, int x, int y, int layer, RealEstateItem item)
        {
            SpriteWithBlocksComponent sb = item.getComponent(SpriteWithBlocksComponent.class);
            if (sb == null)
            {
                return true;
            }
            int w = sb.getWidth(), h = sb.getHeight();

            for (ObjectAtLocation o : items.values())
            {
                if (!map.equals(o.map))
                    continue;

                if (o.item == null)
                    continue;

                if (o.layer != layer)
                    continue;

                SpriteWithBlocksComponent s = o.item.getComponent(SpriteWithBlocksComponent.class);
                if (s == null)
                    continue;

                int sw = s.getWidth(), sh = s.getHeight();

                if (x + w > o.x && x < o.x + sw &&
                        y + h > o.y && y < o.y + sh)
                {
                    return true;
                }
            }

            return false;
        }

        public static class ObjectAtLocation
        {
            public static class ObjectContainer
            {
                public ObjectMap<String, MarketService.MarketItemEntry> items;

                public ObjectContainer()
                {
                    items = new ObjectMap<>();
                }

                public ObjectContainer copy()
                {
                    ObjectContainer copy = new ObjectContainer();
                    for (ObjectMap.Entry<String, MarketService.MarketItemEntry> entry : items)
                    {
                        copy.items.put(entry.key, new MarketService.MarketItemEntry(
                            entry.value.name,
                            entry.value.amount,
                            entry.value.payload
                        ));
                    }

                    return copy;
                }

                public float getTotalWeight()
                {
                    float w = 0;

                    for (MarketService.MarketItemEntry entry : items.values())
                    {
                        ConsumableRecord r =
                            MarketUtils.MarketObjectToConsumableRecord(entry.name, entry.payload, entry.amount);
                        if (r == null)
                            continue;
                        ItemComponent itemComponent = r.getItem().getContent().getComponent(ItemComponent.class);
                        if (itemComponent == null)
                            continue;
                        w += itemComponent.getWeight() * r.getAmount();
                    }

                    return w;
                }

                public boolean read(JSONObject o)
                {
                    for (String key : o.keySet())
                    {
                        JSONObject entryObject = o.optJSONObject(key);
                        if (entryObject == null)
                        {
                            return false;
                        }

                        String entryName = entryObject.optString("n", null);
                        if (entryName == null)
                        {
                            return false;
                        }

                        int entryAmount = entryObject.optInt("a", 1);
                        JSONObject entryPayload = entryObject.optJSONObject("p");

                        this.items.put(key, new MarketService.MarketItemEntry(
                            entryName, entryAmount, entryPayload
                        ));
                    }

                    return true;
                }

                public JSONObject write()
                {
                    JSONObject o = new JSONObject();

                    for (ObjectMap.Entry<String, MarketService.MarketItemEntry> entry : items)
                    {
                        JSONObject entryObject = new JSONObject();
                        entryObject.put("n", entry.value.name);
                        if (entry.value.payload != null)
                        {
                            entryObject.put("p", entry.value.payload);
                        }
                        if (entry.value.amount != 1)
                        {
                            entryObject.put("a", entry.value.amount);
                        }
                        o.put(entry.key, entryObject);
                    }

                    return o;
                }
            }

            private String masterMap;
            private String map;
            public String originalKey;
            public int x;
            public int y;
            public int layer;
            public RealEstateItem item;
            private ObjectContainer container;

            public ObjectAtLocation(String originalKey, String masterMap, String map, int layer, int x, int y)
            {
                this.originalKey = originalKey;
                this.masterMap = masterMap;
                this.map = map;
                this.layer = layer;
                this.x = x;
                this.y = y;
            }

            public float getTotalWeight()
            {
                return container != null ? container.getTotalWeight() : 0;
            }

            public String getMap()
            {
                return masterMap + map;
            }

            public ObjectContainer getContainer()
            {
                if (container == null)
                {
                    container = new ObjectContainer();
                }

                return container;
            }

            public ObjectAtLocation copy()
            {
                ObjectAtLocation copy = new ObjectAtLocation(originalKey, masterMap, map, layer, x, y);
                copy.item = item;
                copy.container = container != null ? container.copy() : null;
                return copy;
            }

            public boolean read(JSONObject o)
            {
                String itemName = o.optString("i", null);
                if (itemName == null)
                    return false;
                item = BrainOut.ContentMgr.get(itemName, RealEstateItem.class);
                if (item == null)
                    return false;

                JSONObject container = o.optJSONObject("c");
                if (container != null)
                {
                    this.container = new ObjectContainer();
                    return this.container.read(container);
                }

                return true;
            }

            public JSONObject write()
            {
                JSONObject o = new JSONObject();
                o.put("i", item.getID());
                if (container != null)
                {
                    JSONObject w = container.write();
                    if (w.length() > 0)
                    {
                        o.put("c", w);
                    }
                }
                return o;
            }
        }

        public boolean read(JSONObject o)
        {
            content = o.optString("c", null);
            id = o.optString("id", null);
            location = o.optString("l", null);

            if (content == null || id == null || location == null)
            {
                return false;
            }

            JSONObject items = o.optJSONObject("i");
            if (items != null)
            {
                if (this.items == null)
                {
                    this.items = new ObjectMap<>();
                }
                else
                {
                    this.items.clear();
                }

                for (String key : items.keySet())
                {
                    Matcher matcher = KEY_PATTERN.matcher(key);
                    if (!matcher.matches())
                    {
                        return false;
                    }

                    String map = matcher.group(1);
                    int layer;
                    int x;
                    int y;
                    try
                    {
                        layer = Integer.parseInt(matcher.group(2));
                        x = Integer.parseInt(matcher.group(3));
                        y = Integer.parseInt(matcher.group(4));
                    }
                    catch (NumberFormatException e)
                    {
                        return false;
                    }

                    ObjectAtLocation obj = new ObjectAtLocation(key, masterMap, map, layer, x, y);

                    JSONObject item = items.optJSONObject(key);
                    if (item == null)
                        return false;
                    if (!obj.read(item))
                        return false;
                    this.items.put(key, obj);
                }
            }

            return true;
        }

        public RealEstatePayload copy()
        {
            RealEstatePayload copy = new RealEstatePayload(masterMap);

            copy.content = content;
            copy.id = id;
            copy.location = location;

            if (items != null)
            {
                copy.items = new ObjectMap<>();
                for (ObjectMap.Entry<String, ObjectAtLocation> entry : items)
                {
                    copy.items.put(entry.key, entry.value.copy());
                }
            }

            return copy;
        }

        public JSONObject write()
        {
            JSONObject o = new JSONObject();
            o.put("c", content);
            o.put("id", id);
            o.put("l", location);

            if (items != null && items.notEmpty())
            {
                JSONObject i = new JSONObject();
                o.put("i", i);

                for (ObjectMap.Entry<String, ObjectAtLocation> entry : items)
                {
                    i.put(entry.key, entry.value.write());
                }
            }

            return o;
        }
    }

    public RealEstateInfo(String masterMap, JSONObject payload)
    {
        this.payload = new RealEstatePayload(masterMap);
        this.payload.read(payload);
    }

    public RealEstateInfo()
    {
        this.payload = new RealEstatePayload(null);
    }

    public void updatePayload(JSONObject newPayload)
    {
        this.payload = new RealEstatePayload(payload.masterMap);
        this.payload.read(newPayload);
    }

    public static Pattern KEY_PATTERN = Pattern.compile("^it(.*?)-([0-9]+)x([0-9]+)y([0-9]+)$");

    public static String GenerateKey(String map, int layer, int x, int y)
    {
        return "it" + map + "-" + layer + "x" + x + "y" + y;
    }

    public void write(Json json)
    {
        json.writeValue("owner", owner);
        json.writeValue("name", name);
        json.writeValue("payload", payload.write().toString());
    }

    private String stripMap(String map)
    {
        if (map.startsWith(payload.masterMap))
        {
            return map.substring(payload.masterMap.length());
        }

        return map;
    }

    private int getItemLayer(RealEstateItem item)
    {
        SpriteWithBlocksComponent sp = item.getComponent(SpriteWithBlocksComponent.class);
        if (sp != null)
        {
            return sp.getBlocksLayer();
        }

        return 1;
    }

    public static class PlaceItemIntoObjectResult
    {
        public RealEstatePayload oldPayload;
        public RealEstatePayload newPayload;
        public MarketService.MarketItemEntry marketItem;
    }

    public PlaceItemIntoObjectResult placeItemIntoObject(
        String rsItemKey, ConsumableRecord record, int amount)
    {
        MarketService.MarketItemEntry marketItem = MarketUtils.ConsumableRecordToMarketEntry(record);
        if (marketItem == null)
        {
            return null;
        }

        marketItem.amount = amount;

        RealEstatePayload copy = payload.copy();

        RealEstatePayload.ObjectAtLocation obj = copy.items.get(rsItemKey);
        if (obj == null)
        {
            return null;
        }

        RealEstateItem item = obj.item;

        RealEstateItemContainerComponent containerComponent =
            item.getComponent(RealEstateItemContainerComponent.class);
        if (containerComponent == null)
        {
            return null;
        }

        if (containerComponent.getWeightLimit() > 0)
        {
            ItemComponent itemComponent = record.getItem().getContent().getComponent(ItemComponent.class);

            if (itemComponent != null)
            {
                if (obj.getTotalWeight() + itemComponent.getWeight() * amount > containerComponent.getWeightLimit())
                {
                    return null;
                }
            }
        }

        if (containerComponent.getItemLimit() > 0)
        {
            if (obj.getContainer().items.size >= containerComponent.getItemLimit())
            {
                return null;
            }
        }

        if (containerComponent.getTagLimit() != null)
        {
            ItemComponent itemComponent = record.getItem().getContent().getComponent(ItemComponent.class);

            if (itemComponent == null || itemComponent.getTags(record.getItem().getContent()) == null)
            {
                return null;
            }

            if (!(itemComponent.getTags(record.getItem().getContent()).contains(containerComponent.getTagLimit(), false)))
            {
                return null;
            }
        }

        int nextId = 0;
        boolean stacked = false;

        for (ObjectMap.Entry<String, MarketService.MarketItemEntry> entry : obj.getContainer().items)
        {
            ConsumableRecord itm = MarketUtils.MarketObjectToConsumableRecord(entry.value.name,
                entry.value.payload, entry.value.amount);

            if (itm != null)
            {
                if (itm.getItem().stacks(record.getItem()))
                {
                    stacked = true;
                    entry.value.amount += amount;
                    break;
                }
            }

            try
            {
                int i = Integer.parseInt(entry.key);
                if (nextId < i)
                {
                    nextId = i;
                }
            }
            catch (NumberFormatException ignore) {}
        }

        if (!stacked)
        {
            nextId++;

            obj.container.items.put(String.valueOf(nextId), marketItem);
        }

        PlaceItemIntoObjectResult r = new PlaceItemIntoObjectResult();
        r.oldPayload = payload;
        r.newPayload = copy;
        r.marketItem = marketItem;

        return r;
    }

    public static class WithdrawItemFromObjectResult
    {
        public RealEstatePayload oldPayload;
        public RealEstatePayload newPayload;
        public ConsumableRecord record;
    }

    public WithdrawItemFromObjectResult withdrawItemFromObject(
        String rsItemKey, String recordId, int amount)
    {
        RealEstatePayload copy = payload.copy();

        RealEstatePayload.ObjectAtLocation obj = copy.getItems().get(rsItemKey);
        if (obj == null)
        {
            return null;
        }

        RealEstateItem item = obj.item;

        RealEstateItemContainerComponent containerComponent =
            item.getComponent(RealEstateItemContainerComponent.class);
        if (containerComponent == null)
        {
            return null;
        }

        RealEstatePayload.ObjectAtLocation.ObjectContainer cont = obj.getContainer();
        MarketService.MarketItemEntry contAtRecord = cont.items.get(recordId);
        if (contAtRecord == null)
        {
            return null;
        }

        if (contAtRecord.amount < amount)
        {
            return null;
        }

        if (contAtRecord.amount > amount)
        {
            contAtRecord.amount -= amount;
        }
        else
        {
            cont.items.remove(recordId);
        }

        WithdrawItemFromObjectResult r = new WithdrawItemFromObjectResult();
        r.oldPayload = payload;
        r.newPayload = copy;
        r.record = MarketUtils.MarketObjectToConsumableRecord(
            contAtRecord.name, contAtRecord.payload, amount
        );

        return r;
    }

    public static class PlaceObjectResult
    {
        public RealEstatePayload oldPayload;
        public RealEstatePayload newPayload;
        public String newItemKey;
    }

    public PlaceObjectResult placeObject(String map, int x, int y, RealEstateItem item, JSONObject itemPayload)
    {
        int layer = getItemLayer(item);
        map = stripMap(map);

        if (payload.doesColludeWithOtherObjects(map, x, y, layer, item))
        {
            return null;
        }

        RealEstatePayload copy = payload.copy();
        String newKey = GenerateKey(map, layer, x, y);

        RealEstatePayload.ObjectAtLocation loc = new RealEstatePayload.ObjectAtLocation(
            newKey, payload.masterMap, map, layer, x, y);

        loc.item = item;

        if (itemPayload != null)
        {
            loc.read(itemPayload);
        }

        copy.getItems().put(newKey, loc);

        PlaceObjectResult r = new PlaceObjectResult();
        r.oldPayload = payload;
        r.newPayload = copy;
        r.newItemKey = newKey;

        return r;
    }

    public static class ExtrudedObject
    {
        public RealEstatePayload newPayload;
        public JSONObject extruded;
    }

    public ExtrudedObject removeObject(String rsKey, String map, int x, int y, RealEstateItem item)
    {
        int layer = getItemLayer(item);
        map = stripMap(map);

        if (payload.items == null || payload.items.isEmpty())
        {
            return null;
        }

        RealEstatePayload copy = payload.copy();
        RealEstatePayload.ObjectAtLocation obj = copy.getItems().get(rsKey);

        if (!map.equals(obj.map))
            return null;

        if (layer != obj.layer)
            return null;

        if (x != obj.x || y != obj.y)
            return null;

        if (obj.item != item)
            return null;

        if (!obj.getContainer().items.isEmpty())
        {
            // we cannot remove an object that has payload
            return null;
        }

        copy.getItems().remove(rsKey);

        ExtrudedObject eo = new ExtrudedObject();

        eo.extruded = new JSONObject();
        eo.extruded.put("c", item.getID());
        eo.extruded.put("k", item.getKind());
        eo.newPayload = copy;

        return eo;
    }

    public void read(Json json, JsonValue jsonValue)
    {
        owner = jsonValue.getString("owner", "");
        name = jsonValue.getString("name", "");

        payload = new RealEstatePayload(null);

        JSONObject p;
        p = new JSONObject(jsonValue.getString("payload"));
        payload.read(p);
    }
}
