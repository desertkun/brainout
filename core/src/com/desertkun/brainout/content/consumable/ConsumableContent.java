package com.desertkun.brainout.content.consumable;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.consumable.impl.DefaultConsumableItem;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.ConsumableContent")
public class ConsumableContent extends Content
{
    private final DefaultConsumableItem defaultItem;
    private boolean throwable;
    private SelectKind selectKind;
    private boolean stacks;
    private String category;

    public enum SelectKind
    {
        selectable,
        disabled,
        canBeActivated
    }

    public ConsumableContent()
    {
        this.defaultItem = new DefaultConsumableItem(this);
        this.throwable = true;
        this.selectKind = SelectKind.selectable;
        this.stacks = true;
        this.category = null;
    }

    public ConsumableItem acquireConsumableItem()
    {
        return getDefaultItem();
    }

    protected DefaultConsumableItem getDefaultItem()
    {
        return defaultItem;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("throwable"))
        {
            throwable = jsonData.getBoolean("throwable");
        }

        if (jsonData.has("selectKind"))
        {
            selectKind = SelectKind.valueOf(jsonData.getString("selectKind"));
        }

        if (jsonData.has("stacks"))
        {
            stacks = jsonData.getBoolean("stacks");
        }

        if (jsonData.has("category"))
        {
            category = jsonData.getString("category");
        }
    }

    public boolean hasCategory()
    {
        return category != null;
    }

    public String getCategory()
    {
        return category;
    }

    public boolean isStacks()
    {
        return stacks;
    }

    public boolean isThrowable()
    {
        return throwable;
    }

    public SelectKind getSelectKind()
    {
        return selectKind;
    }
}
