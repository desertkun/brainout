package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.ContractGroupQueue")
public class ContractGroupQueue extends Content
{
    private Queue<ContractGroup> queue;

    public ContractGroupQueue()
    {
        queue = new Queue<>();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        for (JsonValue value : jsonData.get("queue"))
        {
            queue.addLast(BrainOut.ContentMgr.get(value.asString(), ContractGroup.class));
        }
    }

    public static ContractGroupQueue Get()
    {
        return BrainOut.ContentMgr.get("contracts", ContractGroupQueue.class);
    }

    public Queue<ContractGroup> getQueue()
    {
        return queue;
    }
}
