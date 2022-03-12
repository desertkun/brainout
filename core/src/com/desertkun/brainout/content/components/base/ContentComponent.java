package com.desertkun.brainout.content.components.base;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

public abstract class ContentComponent implements Json.Serializable
{
    private Content content;

    public ContentComponent()
    {
        this.content = null;
    }

    public Content getContent()
    {
        return content;
    }

    public abstract Component getComponent(ComponentObject componentObject);

    public void setContent(Content content)
    {
        this.content = content;
    }

    public void completeLoad(AssetManager assetManager) {}

    public void loadContent(AssetManager assetManager) {}

    @SuppressWarnings("unchecked")
    public Class<? extends ContentComponent> getContentClass()
    {
        return (Class<? extends ContentComponent>)((Object)this).getClass();
    }
}
