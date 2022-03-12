package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.SpriteData;
import com.desertkun.brainout.data.components.SpriteComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.SpriteComponent")
public class SpriteComponent extends ContentComponent
{
    private TextureAtlas.AtlasRegion sprite;

    @Override
    public SpriteComponentData getComponent(ComponentObject componentObject)
    {
        return new SpriteComponentData((SpriteData)componentObject, this) ;
    }

    @Override
    public void write(Json json)
    {
        //
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
    }
}
