package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.Effect;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientRadioReceiverComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientRadioReceiverComponent")
public class ClientRadioReceiverComponent extends ContentComponent
{
    private String backgroundName;
    private String activeName;
    private ObjectMap<String, String> characterNames;

    private Effect background;
    private Effect active;
    private ObjectMap<String, Effect> characters;

    public ClientRadioReceiverComponent()
    {
        characterNames = new ObjectMap<>();
        characters = new ObjectMap<>();
    }

    @Override
    public ClientRadioReceiverComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientRadioReceiverComponentData(((ActiveData) componentObject), this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonValue)
    {
        backgroundName = jsonValue.getString("background");
        activeName = jsonValue.getString("active");

        for (JsonValue value : jsonValue.get("characters"))
        {
            characterNames.put(value.name(), value.asString());
        }
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        background = BrainOutClient.ContentMgr.get(backgroundName, Effect.class);
        active = BrainOutClient.ContentMgr.get(activeName, Effect.class);

        for (ObjectMap.Entry<String, String> entry : characterNames)
        {
            characters.put(entry.key, BrainOutClient.ContentMgr.get(entry.value, Effect.class));
        }
    }

    public Effect getActive()
    {
        return active;
    }

    public Effect getBackground()
    {
        return background;
    }

    public ObjectMap<String, Effect> getCharacters()
    {
        return characters;
    }
}
