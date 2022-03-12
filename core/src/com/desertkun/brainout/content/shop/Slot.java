package com.desertkun.brainout.content.shop;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.data.interfaces.WithBadge;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;

import java.util.Comparator;

@Reflect("content.shop.Slot")
public class Slot extends Content implements WithBadge
{
    private Array<SlotItem> registerItems;
    private OrderedMap<String, Category> categories;
    private OrderedMap<String, Tag> tags;

    private SlotItem defaultItem;
    private float width;
    private boolean visible;
    private boolean stack;

    public class Category implements Json.Serializable, WithBadge
    {
        private Array<SlotItem> items;
        private LocalizedString title;
        private String id;

        public Category()
        {
            this.items = new Array<>();
            this.title = new LocalizedString();
        }

        public Array<SlotItem> getItems()
        {
            return items;
        }

        public String getId()
        {
            return id;
        }

        public LocalizedString getTitle()
        {
            return title;
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            id = jsonData.getString("id");
            title.set(jsonData.getString("title"));

            if (jsonData.has("items"))
            {
                JsonValue items = jsonData.get("items");

                for (JsonValue item_ : items)
                {
                    SlotItem slotItem = BrainOut.ContentMgr.get(item_.asString(), SlotItem.class);

                    if (slotItem == null)
                        continue;

                    this.items.add(slotItem);
                }
            }
        }

        @Override
        public boolean hasBadge(UserProfile profile, Involve involve)
        {
            switch (involve)
            {
                case withChild:
                case childOnly:
                {
                    for (SlotItem item : items)
                    {
                        if (item.hasBadge(profile, involve))
                        {
                            return true;
                        }
                    }

                    break;
                }
            }

            return false;
        }

        @Override
        public String getBadgeId()
        {
            return null;
        }

        public void completeLoad(AssetManager assetManager)
        {
            this.items.sort(Comparator.comparingDouble(SlotItem::getIndex));
        }

        public void registerItem(SlotItem slotItem)
        {
            this.items.add(slotItem);
        }

        public void clear()
        {
            this.items.clear();
        }
    }

    public class Tag implements Json.Serializable
    {
        private LocalizedString title;
        private String id;

        public Tag()
        {
            this.title = new LocalizedString();
        }

        public String getId()
        {
            return id;
        }

        public LocalizedString getTitle()
        {
            return title;
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            id = jsonData.getString("id");
            title.set(jsonData.getString("title"));
        }
    }

    public ObjectMap.Values<Category> getCategories()
    {
        return categories.values();
    }

    public Category getCategory(String id)
    {
        return categories.get(id);
    }

    public ObjectMap.Values<Tag> getTags()
    {
        return tags.values();
    }

    public boolean hasTag(String tag)
    {
        return tags.containsKey(tag);
    }

    public boolean hasTags()
    {
        return tags.size > 0;
    }

    public Tag getTag(String id)
    {
        return tags.get(id);
    }

    public Slot()
    {
        this.defaultItem = null;
        this.categories = new OrderedMap<>();
        this.tags = new OrderedMap<>();
        this.registerItems = new Array<>();
        this.visible = true;
        this.stack = false;
    }

    @Override
    public void dispose()
    {
        super.dispose();

        tags.clear();

        for (ObjectMap.Entry<String, Category> entry : categories)
        {
            entry.value.clear();
        }

        categories.clear();
    }

    public SlotItem getDefaultItem()
    {
        return defaultItem;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        width = jsonData.getFloat("width", 150);
        visible = jsonData.getBoolean("visible", true);

        if (jsonData.has("categories"))
        {
            JsonValue categories = jsonData.get("categories");

            if (categories.isArray())
            {
                for (JsonValue cat: categories)
                {
                    Category category = new Category();
                    category.read(json, cat);

                    this.categories.put(category.getId(), category);
                }
            }
        }

        if (jsonData.has("tags"))
        {
            JsonValue tags = jsonData.get("tags");

            if (tags.isArray())
            {
                for (JsonValue tag_: tags)
                {
                    Tag tag = new Tag();
                    tag.read(json, tag_);

                    this.tags.put(tag.getId(), tag);
                }
            }
        }

        stack = jsonData.getBoolean("stack", stack);

        String itemId = jsonData.getString("default", null);
        if (itemId == null)
            return;

        defaultItem = BrainOut.ContentMgr.get(itemId, SlotItem.class);
    }

    public void registerItem(SlotItem slotItem)
    {
        registerItems.add(slotItem);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        for (SlotItem slotItem : registerItems)
        {
            String categoryName = slotItem.getCategory();

            if (categoryName == null)
                continue;

            Category category = categories.get(categoryName);

            if (category == null)
                continue;

            category.registerItem(slotItem);
        }

        registerItems.clear();

        for (ObjectMap.Entry<String, Category> category : categories)
        {
            category.value.completeLoad(assetManager);
        }
    }

    public float getWidth()
    {
        return width;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public String getCategorySelectionId()
    {
        return getID() + "-category";
    }

    public String getTagSelectionId()
    {
        return getID() + "-tag";
    }

    @Override
    public boolean hasBadge(UserProfile profile, Involve involve)
    {
        switch (involve)
        {
            case withChild:
            case childOnly:
            {
                for (ObjectMap.Entry<String, Category> entry : categories)
                {
                    Category category = entry.value;

                    if (category.hasBadge(profile, involve))
                    {
                        return true;
                    }
                }

                break;
            }
        }

        return false;
    }

    @Override
    public String getBadgeId()
    {
        return null;
    }

    public boolean isStack()
    {
        return stack;
    }
}
