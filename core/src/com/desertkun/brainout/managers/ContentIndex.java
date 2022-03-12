package com.desertkun.brainout.managers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.block.Block;

public class ContentIndex implements Json.Serializable
{
    private Array<String> contentToIndex;
    private ObjectMap<String, Integer> indexToContent;

    public ContentIndex()
    {
        contentToIndex = null;
        indexToContent = null;
    }

    public void fillUp(ContentManager contentManager)
    {
        if (contentToIndex != null && indexToContent != null)
        {
            // already filled up
            return;
        }

        // just make a reference
        contentToIndex = new Array<>();
        indexToContent = new ObjectMap<>();

        int id = 1;

        for (ObjectMap.Entry<String, Content> entry : contentManager.getItems())
        {
            Content c = entry.value;

            if (c instanceof Block || c instanceof Active)
            {
                indexToContent.put(entry.key, id++);
                contentToIndex.add(entry.key);
            }
        }
    }

    @Override
    public void write(Json json)
    {
        json.writeArrayStart("index");

        for (String content : contentToIndex)
        {
            json.writeValue(content);
        }

        json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (contentToIndex == null)
        {
            contentToIndex = new Array<>();
        }

        JsonValue indexValue = jsonData.get("index");

        if (indexValue.isArray())
        {
            int size = indexValue.size;

            contentToIndex.ensureCapacity(size);

            for (JsonValue value : indexValue)
            {
                contentToIndex.add(value.asString());
            }
        }
    }

    public int getIndex(Content content)
    {
        return indexToContent != null ? indexToContent.get(content.getID(), 0) : 0;
    }

    public Content getContent(int index)
    {
        if (index == 0)
        {
            return null;
        }

        String id = contentToIndex.get(index - 1);

        if (id == null)
            return null;

        return BrainOut.ContentMgr.get(id);
    }
}