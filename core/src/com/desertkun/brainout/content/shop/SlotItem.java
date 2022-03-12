package com.desertkun.brainout.content.shop;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Layout;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.online.UserProfile;

public abstract class SlotItem extends OwnableContent
{
    private float price;
    protected Slot slot;
    private String category;
    private float index;
    private ObjectSet<String> tags;
    private Selection staticSelection;
    private Restriction restriction;
    private boolean enabled;

    public class Restriction implements Json.Serializable
    {
        public SlotItem item;
        public Slot slot;

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            this.item = ((SlotItem) BrainOut.ContentMgr.get(jsonData.getString("item")));
            this.slot = ((Slot) BrainOut.ContentMgr.get(jsonData.getString("slot")));
        }
    }

    public abstract class Selection implements Json.Serializable
    {
        public Selection()
        {
        }

        public SlotItem getItem()
        {
            return SlotItem.this;
        }

        @Override
        public void write(Json json)
        {
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
        }

        public void init(UserProfile userProfile)
        {

        }

        public void saveSelection(UserProfile profile, Selection selection, String layout)
        {
            Layout layout_ = BrainOut.ContentMgr.get(layout, Layout.class);
            String key = layout_ != null && !layout_.getKey().isEmpty() ?
                    "-" + layout_.getKey() : "";

            if (profile != null && selection != null && slot != null && selection.getItem() != null)
            {
                profile.setSelection(slot.getID() + key, selection.getItem().getID());
            }
        }

        public abstract void apply(ShopCart shopCart, PlayerData playerData, UserProfile profile, Slot slot, Selection selection);
    }

    public SlotItem()
    {
        this.price = 0;
        this.tags = new ObjectSet<>();
    }

    public float getIndex()
    {
        return index;
    }

    public Slot getSlot()
    {
        return slot;
    }

    public float getWeight()
    {
        return 0;
    }

    public float getPrice()
    {
        return price;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("restrict"))
        {
            restriction = new Restriction();
            restriction.read(json, jsonData.get("restrict"));
        }

        this.price = jsonData.getFloat("price", 0.0f);

        String slotName = jsonData.getString("slot", null);

        if (slotName != null)
        {
            this.slot = BrainOut.ContentMgr.get(slotName, Slot.class);

            this.category = jsonData.getString("category");

            if (jsonData.has("tags"))
            {
                JsonValue tags_ = jsonData.get("tags");

                if (tags_.isArray())
                {
                    for (JsonValue value : tags_)
                    {
                        tags.add(value.asString());
                    }
                } else
                {
                    tags.add(tags_.asString());
                }
            }

            index = jsonData.getFloat("index", 100);

            enabled = jsonData.getBoolean("enabled", true);

            if (enabled)
            {
                slot.registerItem(this);
            }
        }
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

    }

    public String getCategory()
    {
        return category;
    }

    public boolean hasTag(String tag)
    {
        return tags.contains(tag);
    }

    public Restriction getRestriction()
    {
        return restriction;
    }

    public abstract Selection getSelection();

    public Selection getStaticSelection()
    {
        if (staticSelection == null)
        {
            staticSelection = getSelection();
        }

        return staticSelection;
    }

    public boolean isEnabled()
    {
        return enabled;
    }
}
