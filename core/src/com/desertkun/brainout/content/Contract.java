package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Contract")
public class Contract extends OwnableContent
{
    private int skipPrice;
    private ContractGroup group;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        group = BrainOut.ContentMgr.get(jsonData.getString("group"), ContractGroup.class);
        group.register(this);
        skipPrice = jsonData.getInt("skipPrice", 10000);
    }

    public ContractGroup getGroup()
    {
        return group;
    }

    public int getSkipPrice()
    {
        return skipPrice;
    }
}
