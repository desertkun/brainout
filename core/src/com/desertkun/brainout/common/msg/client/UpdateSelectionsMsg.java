package com.desertkun.brainout.common.msg.client;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.shop.ShopCart;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.content.shop.SlotItem;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.interfaces.Spawnable;

public class UpdateSelectionsMsg
{
    public static class Item
    {
        public String item;
        public String data;
    }

    public Item[] items;
    public String layout;

    public int spawnAt;
    public int d;

    public UpdateSelectionsMsg() {}
    public UpdateSelectionsMsg(Spawnable spawnPointData, ShopCart shopCart)
    {
        if (spawnPointData != null)
        {
            this.spawnAt = ((ActiveData) spawnPointData).getId();
            this.d = Map.GetDimensionId(spawnPointData.getDimension());
        }
        else
        {
            this.spawnAt = -1;
            this.d = -1;
        }

        Array<SpawnMsg.Item> items = new Array<>();

        Json json = new Json();

        for (ObjectMap.Entry<Slot, SlotItem.Selection> contentItem: shopCart.getItems())
        {
            SpawnMsg.Item i = new SpawnMsg.Item();
            SlotItem.Selection selection = contentItem.value;

            i.item = selection.getItem().getID();
            i.data = json.toJson(selection);

            items.add(i);
        }

        this.items = items.toArray(Item.class);
        this.layout = shopCart.getLayout();
    }
}
