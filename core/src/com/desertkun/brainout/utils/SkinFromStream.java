package com.desertkun.brainout.utils;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;

import java.io.InputStream;

public class SkinFromStream extends Skin
{
    public void load(JsonValue jsonValue)
    {
        getJsonLoader(null).readValue(Skin.class, jsonValue);
    }

    public void load(InputStream stream)
    {
        try {
            getJsonLoader(null).fromJson(Skin.class, stream);
        } catch (SerializationException ex) {
            throw new SerializationException("Error reading file from stream! ", ex);
        }
    }
}
