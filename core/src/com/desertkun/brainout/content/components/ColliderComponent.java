package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ColliderComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ColliderComponent")
public class ColliderComponent extends ContentComponent
{
    public class Collider implements Json.Serializable
    {
        public float x;
        public float y;
        public float width;
        public float height;
        public float damageCoefficient;

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            x = jsonData.getFloat("x", 0);
            y = jsonData.getFloat("y", 0);
            width = jsonData.getFloat("width");
            height = jsonData.getFloat("height");
            damageCoefficient = jsonData.getFloat("damageCoefficient", 1);
        }
    }

    private ObjectMap<String, Collider> colliders;

    public ColliderComponent()
    {
        colliders = new ObjectMap<String, Collider>();
    }

    @Override
    public ColliderComponentData getComponent(ComponentObject componentObject)
    {
        return new ColliderComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        JsonValue collidersValue = jsonData.get("colliders");

        if (collidersValue != null && collidersValue.isObject())
        {
            for (JsonValue coll : collidersValue)
            {
                Collider collider = new Collider();
                collider.read(json, coll);
                colliders.put(coll.name(), collider);
            }
        }
    }

    public ObjectMap<String, Collider> getColliders()
    {
        return colliders;
    }
}
