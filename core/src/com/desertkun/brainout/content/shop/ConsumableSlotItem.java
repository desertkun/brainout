package com.desertkun.brainout.content.shop;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.online.UserProfile;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.shop.ConsumableSlotItem")
public class ConsumableSlotItem extends SlotItem
{
    private Array<ItemCargo> cargo;
    public static class ItemCargo implements Json.Serializable
    {
        private Content content;
        private int amount;

        public Content getContent()
        {
            return content;
        }

        public int getAmount()
        {
            return amount;
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            this.content = BrainOut.ContentMgr.get(jsonData.getString("id", ""));
            this.amount = jsonData.getInt("amount", 0);
        }
    }

    public class ConsumableSelection extends Selection
    {
        @Override
        public void apply(ShopCart shopCart, PlayerData playerData, UserProfile profile, Slot slot, Selection selection)
        {
            PlayerOwnerComponent ownerComponent = playerData.getComponent(PlayerOwnerComponent.class);

            for (ItemCargo cargo: getCargo())
            {
                int amount = cargo.getAmount();
                Content content = cargo.getContent();

                if (content instanceof Instrument)
                {
                    Instrument instrument = ((Instrument) content);
                    InstrumentData instrumentData = instrument.getData(playerData.getDimension());
                    instrumentData.setSkin(instrument.getDefaultSkin());

                    ownerComponent.getConsumableContainer().putConsumable(amount,
                        new InstrumentConsumableItem(instrumentData, playerData.getDimension()));
                }
                else if (content instanceof ConsumableContent)
                {
                    ownerComponent.getConsumableContainer().putConsumable(amount,
                        ((ConsumableContent) content).acquireConsumableItem());
                }
            }

            if (profile != null)
            {
                saveSelection(profile, selection, profile.getLayout() != null ? profile.getLayout().getID() : "layout-1");
            }
        }
    }

    @Override
    public float getWeight()
    {
        float weight = 0;

        for (ItemCargo item: cargo)
        {
            if (item.getContent() == null)
                continue;

            if (item.getContent().hasComponent(ItemComponent.class))
            {
                ItemComponent itemComponent = item.getContent().getComponent(ItemComponent.class);

                weight += itemComponent.getWeight() * item.getAmount();
            }
        }

        return weight;
    }

    public ConsumableSlotItem()
    {
        this.cargo = new Array<ItemCargo>();
    }

    public Array<ItemCargo> getCargo()
    {
        return cargo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.cargo = json.readValue("items", Array.class, ItemCargo.class, jsonData);
    }

    @Override
    public Selection getSelection()
    {
        return new ConsumableSelection();
    }
}
