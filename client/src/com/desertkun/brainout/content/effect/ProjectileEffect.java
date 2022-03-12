package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.ProjectileEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.ProjectileEffect")
public class ProjectileEffect extends Effect
{
    private TextureAtlas.AtlasRegion projectile;
    private float alphaBefore;
    private float ttl;
    private String projectileName;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        projectileName = jsonData.getString("projectile");

        if (jsonData.has("alphaBefore"))
        {
            alphaBefore = jsonData.getFloat("alphaBefore");
        }

        ttl = jsonData.getFloat("ttl", 0);
    }

    public float getTtl()
    {
        return ttl;
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (projectileName != null)
        {
            this.projectile = BrainOutClient.getRegion(projectileName);
        }
    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new ProjectileEffectData(this, launchData);
    }

    public TextureAtlas.AtlasRegion getProjectile()
    {
        return projectile;
    }

    public float getAlphaBefore() {
        return alphaBefore;
    }
}
