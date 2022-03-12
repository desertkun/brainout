package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.components.interfaces.UpgradeComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.ConsumableSlotItem;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.AddItemsComponent")
public class AddItemsComponent extends ContentComponent implements UpgradeComponent
{
    private Array<ConsumableSlotItem.ItemCargo> cargo;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.cargo = json.readValue("items", Array.class, ConsumableSlotItem.ItemCargo.class, jsonData);
    }

    public Array<ConsumableSlotItem.ItemCargo> getCargo()
    {
        return cargo;
    }



    @Override
    public void upgrade(InstrumentData instrumentData)
    {
        if (BrainOutServer.Controller.isFreePlay()) return;

        ActiveData owner = instrumentData.getOwner();

        if (owner == null)
            return;

        PlayerOwnerComponent ownerComponent = owner.getComponent(PlayerOwnerComponent.class);

        for (ConsumableSlotItem.ItemCargo cargo: getCargo())
        {
            int amount = cargo.getAmount();
            Content content = cargo.getContent();

            if (content instanceof Instrument)
            {
                Instrument instrument = ((Instrument) content);
                InstrumentData newData = instrument.getData(instrumentData.getDimension());
                newData.setSkin(instrument.getDefaultSkin());

                ownerComponent.getConsumableContainer().putConsumable(amount,
                    new InstrumentConsumableItem(newData, instrumentData.getDimension()));
            }
            else if (content instanceof ConsumableContent)
            {
                ownerComponent.getConsumableContainer().putConsumable(amount,
                    ((ConsumableContent) content).acquireConsumableItem());
            }
        }
    }

    @Override
    public boolean pre()
    {
        return true;
    }
}
