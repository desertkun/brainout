package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.InstrumentBeltComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentBeltComponent")
public class InstrumentBeltComponent extends ContentComponent
{
    private String attachBoneA;
    private String attachBoneB;
    private float tension;
    private String cellSpriteName;
    private TextureRegion cellSprite;
    private int cellsCount;

    @Override
    public InstrumentBeltComponentData getComponent(ComponentObject componentObject)
    {
        return new InstrumentBeltComponentData((InstrumentData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.attachBoneA = jsonData.getString("attachBoneA");
        this.attachBoneB = jsonData.getString("attachBoneB");

        this.cellsCount = jsonData.getInt("cells");
        this.tension = jsonData.getFloat("tension");
        this.cellSpriteName = jsonData.getString("cellSprite");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        this.cellSprite = BrainOutClient.getRegion(cellSpriteName);
    }

    public String getAttachBoneA()
    {
        return attachBoneA;
    }

    public String getAttachBoneB()
    {
        return attachBoneB;
    }

    public float getTension()
    {
        return tension;
    }

    public int getCellsCount()
    {
        return cellsCount;
    }

    public TextureRegion getCellSprite()
    {
        return cellSprite;
    }
}
