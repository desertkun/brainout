package com.desertkun.brainout.content.shop;

import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Layout;
import com.desertkun.brainout.content.active.Player;

import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.shop.ShopCart")
public class ShopCart
{
    private ArrayMap<Slot, SlotItem.Selection> items;

    private float sumPrice;
    private float sumWeight;
    private String layout;

    public ShopCart()
    {
        this.items = new ArrayMap<>();

        this.sumWeight = 0;
        this.sumPrice = 0;
    }

    public void addDefaultItems()
    {
        Shop shop = Shop.getInstance();

        if (shop != null)
        {
            for (Slot slot : shop.getSlots())
            {
                selectItem(slot, slot.getDefaultItem().getSelection());
            }
        }
    }

    public boolean selectItem(Slot slot, SlotItem.Selection selection)
    {
        boolean existing = items.get(slot) == selection;
        items.put(slot, selection);
        return existing;
    }

    public void removeItem(Slot slot)
    {
        items.removeKey(slot);
    }

    public void init()
    {
        refresh();
    }

    public void clear()
    {
        items.clear();

        refresh();
    }

    public void refresh()
    {
        this.sumWeight = 0;
        this.sumPrice = 0;

        for (ObjectMap.Entry<Slot, SlotItem.Selection> contentItem: items)
        {
            calculateItem(contentItem.value);
        }
    }

    private void calculateItem(SlotItem.Selection selection)
    {
        SlotItem item = selection.getItem();

        sumPrice += item.getPrice();
        sumWeight += item.getWeight();
    }

    public float getSumPrice()
    {
        return sumPrice;
    }

    public float getSumWeight()
    {
        return sumWeight;
    }

    public ArrayMap<Slot, SlotItem.Selection> getItems()
    {
        return items;
    }

    public SlotItem.Selection getItem(Slot slot)
    {
        return items.get(slot);
    }

    public Player getPlayer()
    {
        for (ObjectMap.Entry<Slot, SlotItem.Selection> entry : getItems())
        {
            if (BrainOut.R.instanceOf(PlayerSlot.class, entry.key))
            {
                PlayerSlotItem.PlayerSlotSelection selection = ((PlayerSlotItem.PlayerSlotSelection) entry.value);
                PlayerSlotItem item = selection.getItem();
                return item.getPlayer();
            }
        }

        return null;
    }

    public boolean checkRestrictions()
    {
        boolean result = true;

        for (ObjectMap.Entry<Slot, SlotItem.Selection> entry : getItems())
        {
            SlotItem item = entry.value.getItem();

            if (item != null && item.getRestriction() != null)
            {
                SlotItem.Restriction r = item.getRestriction();

                SlotItem.Selection selection = getItem(r.slot);

                if (selection == null || selection.getItem() != r.item)
                {
                    selectItem(entry.key, entry.key.getDefaultItem().getSelection());
                    result = false;
                }
            }
        }

        return result;
    }

    public void setLayout(String layout)
    {
        this.layout = layout;
    }

    public String getLayout()
    {
        return layout;
    }

    public void initStaticSelection(UserProfile userProfile, Layout layout)
    {
        initSelection(userProfile, layout, true);
    }

    public void initSelection(UserProfile userProfile, Layout layout, boolean static_)
    {
        if (layout == null)
            return;

        setLayout(layout.getID());

        clear();
        addDefaultItems();

        if (userProfile == null)
            return;

        Shop shop = Shop.getInstance();

        for (ObjectMap.Entry<Slot, SlotItem.Selection> entry : getItems())
        {
            Slot slot = entry.key;
            String key = (layout != null && !layout.getKey().isEmpty()) ?
                    slot.getID() + "-" + layout.getKey() : slot.getID();

            String s = userProfile.getSelection(key);

            if (s != null)
            {
                SlotItem slotItem = ((SlotItem) BrainOut.ContentMgr.get(s));

                if (slotItem == null)
                    continue;

                if (!shop.getSlots().contains(slotItem.getSlot(), true))
                    continue;

                SlotItem.Selection selection = static_ ? slotItem.getStaticSelection() : slotItem.getSelection();
                selection.init(userProfile);
                selectItem(slot, selection);
            }
        }
    }
}
