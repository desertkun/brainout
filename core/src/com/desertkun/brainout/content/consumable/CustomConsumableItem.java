package com.desertkun.brainout.content.consumable;

import com.desertkun.brainout.content.Content;
import org.json.JSONObject;

public class CustomConsumableItem extends ConsumableItem
{
    private final Content content;
    private String type;
    private JSONObject payload;

    public CustomConsumableItem(String type, JSONObject payload, Content content)
    {
        this.type = type;
        this.payload = payload;
        this.content = content;
    }

    public String getType()
    {
        return type;
    }

    public JSONObject getPayload()
    {
        return payload;
    }

    @Override
    public Content getContent()
    {
        return content;
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        if (!(item instanceof CustomConsumableItem))
            return false;

        return payload.equals(((CustomConsumableItem) item).payload);
    }
}
