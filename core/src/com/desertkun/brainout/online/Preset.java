package com.desertkun.brainout.online;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.content.shop.SlotItem;

public class Preset implements Json.Serializable
{
    public ObjectMap<String, String> defines;
    public ObjectSet<String> limitSlots;
    public ObjectMap<String, ObjectSet<String>> limitTags;

    public Preset()
    {
        this.defines = new ObjectMap<>();
        this.limitSlots = new ObjectSet<>();
        this.limitTags = new ObjectMap<>();
    }

    public boolean hasAnySlotLimited()
    {
        return limitSlots.size > 0;
    }

    public boolean hasTagsLimited(String slot)
    {
        return limitTags.containsKey(slot);
    }

    @Override
    public void write(Json json)
    {
        if (defines.size > 0)
        {
            json.writeObjectStart("defines");

            for (ObjectMap.Entry<String, String> entry : defines)
            {
                json.writeValue(entry.key, entry.value);
            }

            json.writeObjectEnd();
        }

        if (limitSlots.size > 0)
        {
            json.writeArrayStart("limit-slots");

            for (String limitSlot : limitSlots)
            {
                json.writeValue(limitSlot);
            }

            json.writeArrayEnd();
        }

        if (limitTags.size > 0)
        {
            json.writeObjectStart("limit-tags");

            for (ObjectMap.Entry<String, ObjectSet<String>> entry : limitTags)
            {
                json.writeArrayStart(entry.key);

                for (String s : entry.value)
                {
                    json.writeValue(s);
                }

                json.writeArrayEnd();
            }

            json.writeObjectEnd();
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        defines.clear();
        limitSlots.clear();
        limitTags.clear();

        if (jsonData.has("defines"))
        {
            JsonValue _defines = jsonData.get("defines");
            for (JsonValue define : _defines)
            {
                this.defines.put(define.name(), define.asString());
            }
        }

        if (jsonData.has("limit-slots"))
        {
            JsonValue _limitSlots = jsonData.get("limit-slots");
            for (JsonValue limitSlot : _limitSlots)
            {
                this.limitSlots.add(limitSlot.asString());
            }
        }

        if (jsonData.has("limit-tags"))
        {
            JsonValue _limitTagsSlots = jsonData.get("limit-tags");

            for (JsonValue limitTagsSlot : _limitTagsSlots)
            {
                String slotName = limitTagsSlot.name();
                ObjectSet<String> tags = new ObjectSet<>();

                for (JsonValue value : limitTagsSlot)
                {
                    tags.add(value.asString());
                }

                this.limitTags.put(slotName, tags);
            }
        }
    }

    public boolean isItemAllowed(SlotItem slotItem)
    {
        Slot slot = slotItem.getSlot();

        if (slot == null)
            return true;

        ObjectSet<String> tagLimit = limitTags.get(slot.getID());

        if (tagLimit == null || tagLimit.size == 0)
            return true;

        for (String tag : tagLimit)
        {
            if (slotItem.hasTag(tag))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isSlotAllowed(Slot slot)
    {
        if (!hasAnySlotLimited())
            return true;

        return limitSlots.contains(slot.getID());
    }

    public boolean isCategoryHasAllowedItem(Slot slot, Slot.Category category)
    {
        if (!isSlotAllowed(slot))
            return false;

        ObjectSet<String> tagLimit = limitTags.get(slot.getID());

        if (tagLimit == null || tagLimit.size == 0)
            return true;

        for (SlotItem slotItem : category.getItems())
        {
            for (String tag : tagLimit)
            {
                if (slotItem.hasTag(tag))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isTagAllowed(Slot slot, String tag)
    {
        ObjectSet<String> tagLimit = limitTags.get(slot.getID());

        if (tagLimit == null || tagLimit.size == 0)
            return true;

        return tagLimit.contains(tag);
    }
}