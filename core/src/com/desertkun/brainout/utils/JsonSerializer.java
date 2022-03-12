package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class JsonSerializer implements Json.Serializable
{
    private final Json.Serializable owner;
    private final AdditionalData additionalData;

    public static interface AdditionalData
    {
        public void write(Json json);
        public void read(Json json, JsonValue jsonData);
    }

    public JsonSerializer(Json.Serializable owner, AdditionalData additionalData)
    {
        this.owner = owner;
        this.additionalData = additionalData;
    }

    @Override
    public void write(Json json)
    {
        owner.write(json);
        additionalData.write(json);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        owner.read(json, jsonData);
        additionalData.read(json, jsonData);
    }
}
