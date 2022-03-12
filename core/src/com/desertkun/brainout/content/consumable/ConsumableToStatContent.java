package com.desertkun.brainout.content.consumable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.ConsumableToStatContent")
public class ConsumableToStatContent extends ConsumableContent
{
    private String stat;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        stat = jsonData.getString("stat");
    }

    public String getStat()
    {
        return stat;
    }
}
