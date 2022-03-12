package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ItemStatsComponentData;
import com.desertkun.brainout.data.components.PlayerStatsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ItemStatsComponent")
public class ItemStatsComponent extends ActiveStatsComponent
{
    private boolean showEmpty;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ItemStatsComponentData((ItemData)componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        showEmpty = jsonData.getBoolean("showEmpty", true);
    }

    public boolean isShowEmpty()
    {
        return showEmpty;
    }
}
