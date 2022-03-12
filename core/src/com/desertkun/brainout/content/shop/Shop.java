package com.desertkun.brainout.content.shop;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.OwnableContent;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.shop.Shop")
public class Shop extends Content
{
    private Array<String> slotNames;

    private Array<Slot> slots;
    private ObjectMap<String, ShopItem> items;
    private Array<Slot> open;

    public class ShopItem implements Json.Serializable
    {
        private String currency;
        private int amount;

        public ShopItem()
        {
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            this.currency = jsonData.getString("currency");
            this.amount = jsonData.getInt("amount");
        }

        public int getAmount()
        {
            return amount;
        }

        public String getCurrency()
        {
            return currency;
        }
    }

    public Shop()
    {
        this.slots = new Array<>();
        this.slotNames = new Array<>();
        this.items = new ObjectMap<>();
        this.open = new Array<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        slotNames.clear();

        super.read(json, jsonData);

        if (jsonData.has("slots"))
        {
            for (JsonValue item: jsonData.get("slots"))
            {
                slotNames.add(item.asString());
            }
        }

        if (jsonData.has("items"))
        {
            for (JsonValue value : jsonData.get("items"))
            {
                ShopItem shopItem = new ShopItem();
                shopItem.read(json, value);

                this.items.put(value.name(), shopItem);
            }
        }

        if (jsonData.has("open"))
        {
            JsonValue openValue = jsonData.get("open");

            if (openValue.isString())
            {
                open.add(BrainOut.ContentMgr.get(openValue.asString(), Slot.class));
            }
            else if (openValue.isArray())
            {
                for (JsonValue value : openValue)
                {
                    open.add(BrainOut.ContentMgr.get(value.asString(), Slot.class));
                }
            }
        }
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        slots.clear();

        for (String item: slotNames)
        {
            Slot shopCategory = ((Slot) BrainOut.ContentMgr.get(item));
            slots.add(shopCategory);
        }
    }

    public Array<Slot> getSlots()
    {
        return slots;
    }

    public Array<Slot> getOpen()
    {
        return open;
    }

    public static Shop getInstance()
    {
        return (Shop)BrainOut.ContentMgr.get(Constants.Core.SHOP_ITEM);
    }

    public ShopItem getItem(OwnableContent content)
    {
        return items.get(content.getID());
    }

    public boolean isFree(OwnableContent content)
    {
        return items.get(content.getID()) == null;
    }
}
