package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Animation;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.AnimationComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.esotericsoftware.spine.Skin;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.AnimationComponent")
public class AnimationComponent extends ContentComponent
{
    private Animation animation;
    private Skin skin;
    private String skinName;
    private Array<String> playNames;
    private int zIndex;
    private boolean playLoop;

    public AnimationComponent()
    {
        animation = null;
        skin = null;
        playNames = null;
        zIndex = 0;
    }

    @Override
    public AnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new AnimationComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        animation = (Animation) BrainOut.ContentMgr.get(jsonData.getString("animation"));
        if (jsonData.has("skin"))
        {
            skinName = jsonData.getString("skin");
        }

        if (jsonData.has("play"))
        {
            playNames = json.readValue("play", Array.class, String.class, jsonData);
        }

        playLoop = jsonData.getBoolean("loop", true);

        if (jsonData.has("zIndex"))
        {
            zIndex = jsonData.getInt("zIndex");
        }
    }

    public void setAnimation(Animation animation)
    {
        this.animation = animation;
    }

    public void setSkin(String skinName)
    {
        this.skinName = skinName;
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (animation != null && skinName != null)
        {
            skin = findSkin(skinName);
        }
    }

    public Animation getAnimation()
    {
        return animation;
    }

    public Skin getSkin()
    {
        return skin;
    }

    public Skin findSkin(String skinName)
    {
        if (animation.getSkeletonData() == null)
            return null;

        return animation.getSkeletonData().findSkin(skinName);
    }

    public boolean isPlayLoop()
    {
        return playLoop;
    }

    public String getPlayNames()
    {
        if (playNames == null) return null;

        return playNames.size > 0 ? playNames.get(MathUtils.random(playNames.size - 1)) : null;
    }

    public int getzIndex()
    {
        return zIndex;
    }
}
