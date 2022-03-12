package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Authors")
public class Authors extends Content {

    private ObjectMap<String, String[]> authors;

    public Authors()
    {
        authors = new ObjectMap<String, String[]>();
    }

    public ObjectMap<String, String[]> getAuthors()
    {
        return authors;
    }

    @Override
    public void write(Json json) {

    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        JsonValue content = jsonData.get("content");
        for (JsonValue section : content)
        {
            authors.put(section.name, section.asStringArray());
        }
    }
}
