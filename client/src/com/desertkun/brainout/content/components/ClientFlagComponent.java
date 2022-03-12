package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.components.ClientFlagComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientFlagComponent")
public class ClientFlagComponent extends ContentComponent
{
    private TextureAtlas.AtlasRegion flagOursIcon;
    private TextureAtlas.AtlasRegion flagEnemyIcon;
    private TextureAtlas.AtlasRegion flagNoneIcon;

    private String flagOursIconName;
    private String flagEnemyIconName;
    private String flagNoneIconName;

    @Override
    public ClientFlagComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientFlagComponentData((FlagData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        flagOursIcon = BrainOutClient.getRegion(flagOursIconName);
        flagEnemyIcon = BrainOutClient.getRegion(flagEnemyIconName);
        flagNoneIcon = BrainOutClient.getRegion(flagNoneIconName);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        flagOursIconName = jsonData.getString("flagOursIcon");
        flagEnemyIconName = jsonData.getString("flagEnemyIcon");
        flagNoneIconName = jsonData.getString("flagNoneIcon");
    }

    public TextureAtlas.AtlasRegion getFlagOursIcon()
    {
        return flagOursIcon;
    }

    public TextureAtlas.AtlasRegion getFlagEnemyIcon()
    {
        return flagEnemyIcon;
    }

    public TextureAtlas.AtlasRegion getFlagNoneIcon()
    {
        return flagNoneIcon;
    }
}
