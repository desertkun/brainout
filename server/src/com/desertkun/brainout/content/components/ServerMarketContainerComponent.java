package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.PersonalContainer;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerMarketContainerComponent")
public class ServerMarketContainerComponent extends ContentComponent implements WithTag
{
    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
    }

    public String getMarket()
    {
        return ((PersonalContainer) getContent()).getMarket();
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.MARKET_CONTAINER);
    }
}
