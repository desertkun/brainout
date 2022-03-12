package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.BulletEffectsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BulletEffectsComponent")
public class BulletEffectsComponent extends ContentComponent
{
    private EffectSetGroup effects;
    private JsonValue effectsValue;

    public BulletEffectsComponent()
    {
        effects = new EffectSetGroup(new String[]{
            "launch",
            "shell",
            "fly",
            "hit"
        });
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new BulletEffectsComponentData((BulletData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        effectsValue = jsonData;
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        effects.read(effectsValue);
        effectsValue = null;
    }

    public EffectSetGroup getEffects()
    {
        return effects;
    }
}
