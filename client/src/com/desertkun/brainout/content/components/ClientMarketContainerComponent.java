package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ClientMarketContainerComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;

@Reflect("content.components.ClientMarketContainerComponent")
public class ClientMarketContainerComponent extends ContentComponent
{
    private LocalizedString title;
    private String category;

    public ClientMarketContainerComponent()
    {
        this.title = new LocalizedString();
    }

    @Override
    public ClientMarketContainerComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientMarketContainerComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        title.set(jsonData.getString("title"));
        category = jsonData.getString("category", "default");
    }

    public String getCategory()
    {
        return category;
    }

    public LocalizedString getTitle()
    {
        return title;
    }
}
