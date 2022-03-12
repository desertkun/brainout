package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class ConsumableInventoryPanel extends InventoryPanel
{
    private static Array<ConsumableRecord> tmp = new Array<>();
    private final ConsumableContainer inventory;
    private final Filter filter;

    public interface Filter
    {
        boolean check(ConsumableRecord record);
    }

    public ConsumableInventoryPanel(InventoryDragAndDrop dragAndDrop,
                                    ConsumableContainer inventory,
                                    Filter filter)
    {
        super(dragAndDrop);

        this.inventory = inventory;
        this.filter = filter;

        process();
    }

    public ConsumableContainer getInventory()
    {
        return inventory;
    }

    private void process()
    {
        if (inventory == null)
            return;

        tmp.clear();

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : inventory.getData())
        {
            ConsumableRecord record = entry.value;

            if (!filter.check(record))
                continue;

            ConsumableItem item = record.getItem();

            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);

                Slot slot = ici.getInstrumentData().getInstrument().getSlot();
                if (slot != null)
                {
                    if (slot.getID().equals("slot-melee"))
                        continue;
                }
            }

            tmp.add(record);
        }

        tmp.sort((o1, o2) -> getItemRank(o2) - getItemRank(o1));

        for (ConsumableRecord record : tmp)
        {
            if (!displayRecord(record))
                continue;

            addItem(new InventoryRecord(record));
        }
    }

    public void refresh()
    {
        final float oldScrollY = getScrollPane().getScrollY();

        BrainOutClient.getInstance().postRunnable(() ->
        {
            clearItems();
            process();
            updated();

            validate();
            getScrollPane().setScrollY(oldScrollY);
        });
    }
}
