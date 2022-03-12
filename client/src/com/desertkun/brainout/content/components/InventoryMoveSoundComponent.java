package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;

@Reflect("content.components.InventoryMoveSoundComponent")
public class InventoryMoveSoundComponent extends ContentComponent
{
    private Array<String> soundsNames;
    private Array<SoundEffect> sounds;

    public InventoryMoveSoundComponent()
    {
        soundsNames = new Array<>();
        sounds = new Array<>();
    }

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
        if (jsonData.has("sounds"))
        {
            JsonValue sounds = jsonData.get("sounds");

            if (sounds.isArray())
            {
                for (JsonValue soundName : sounds)
                {
                    String name = soundName.asString();

                    if (name == null)
                        continue;

                    this.soundsNames.add(name);
                }
            }

            if (sounds.isString())
            {
                String name = sounds.asString();

                if (name != null)
                {
                    this.soundsNames.add(name);
                }
            }

        }
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        for (String soundName : soundsNames)
        {
            SoundEffect sound = BrainOutClient.ContentMgr.get(soundName, SoundEffect.class);
            if (sound == null)
            {
                Log.error(this.getClass().getName() + ": can't get sound " + soundName);
                continue;
            }

            this.sounds.add(sound);
        }
    }

    public void play(ActiveData at)
    {
        if (sounds.size == 0)
            return;

        ClientMap map = at.getMap(ClientMap.class);

        if (map == null)
            return;

        map.addEffect(sounds.random(), new PointLaunchData(at.getX(), at.getY(), 0, map.getDimension()));
    }

    public Array<SoundEffect> getSounds()
    {
        return sounds;
    }
}
