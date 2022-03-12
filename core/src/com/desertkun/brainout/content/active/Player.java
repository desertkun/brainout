package com.desertkun.brainout.content.active;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.Player")
public class Player extends Active
{
    private float maxWeight;
    private float maxOverweight;
    private PlayerSkin defaultSkin;

    public enum State
    {
        normal,
        sit,
        squat,
        run,
        wounded,
        crawl
    }

    public class PlayerState implements Json.Serializable
    {
        public class ColliderState implements Json.Serializable
        {
            public float x1;
            public float y1;
            public float x2;
            public float y2;

            @Override
            public void write(Json json)
            {

            }

            @Override
            public void read(Json json, JsonValue jsonData)
            {
                if (jsonData.has("x1"))
                    x1 = jsonData.getFloat("x1");
                if (jsonData.has("y1"))
                    y1 = jsonData.getFloat("y1");
                if (jsonData.has("x2"))
                    x2 = jsonData.getFloat("x2");
                if (jsonData.has("y2"))
                    y2 = jsonData.getFloat("y2");
            }
        }

        public float width;
        public float height;
        public Vector2 speed;
        public ObjectMap<String, ColliderState> colliders;

        public PlayerState()
        {
            colliders = new ObjectMap<String, ColliderState>();
        }

        @Override
        public void write(Json json) {}

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            this.width = jsonData.getFloat("width", this.width);
            this.height = jsonData.getFloat("height", this.height);

            if (jsonData.has("speed"))
            {
                JsonValue speedValue = jsonData.get("speed");

                this.speed = new Vector2();
                speed.x = speedValue.getFloat("x");
                speed.y = speedValue.getFloat("y");
            }

            if (jsonData.has("collider"))
            {
                JsonValue c = jsonData.get("collider");

                if (c.isObject())
                {
                    for (JsonValue collider : c)
                    {
                        ColliderState colliderState = new ColliderState();
                        colliderState.read(json, collider);

                        colliders.put(collider.name(), colliderState);
                    }
                }
            }
        }
    }

    private float jumpForce;
    private boolean flying;
    private boolean steps;

    private ObjectMap<State, PlayerState> states;

    public Player()
    {
        this.states = new ObjectMap<>();

        this.maxWeight = 1000;
        this.maxOverweight = 1000;
        this.jumpForce = 0;
        this.flying = false;
        this.steps = true;
    }

    @Override
    public ActiveData getData(String dimension)
    {
        return new PlayerData(this, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        maxWeight = jsonData.getFloat("maxWeight", this.maxWeight);
        maxOverweight = jsonData.getFloat("maxOverweight", this.maxOverweight);
        jumpForce = jsonData.getFloat("jumpForce", this.jumpForce);
        flying = jsonData.getBoolean("flying", this.flying);
        steps = jsonData.getBoolean("steps", this.steps);

        if (jsonData.has("defaultSkin"))
        {
            defaultSkin = BrainOut.ContentMgr.get(jsonData.getString("defaultSkin"), PlayerSkin.class);
        }

        JsonValue statesValue = jsonData.get("states");

        if (statesValue != null && statesValue.isObject())
        {
            for (JsonValue stateItem: statesValue)
            {
                State stateId = State.valueOf(stateItem.name());

                if (states.get(stateId) == null)
                {
                    PlayerState newPlayerState = new PlayerState();
                    states.put(stateId, newPlayerState);
                }

                states.get(stateId).read(json, stateItem);
            }
        }

    }

    public float getMaxWeight()
    {
        return maxWeight;
    }

    public float getMaxOverweight()
    {
        return maxOverweight;
    }

    public float getJumpForce()
    {
        return jumpForce;
    }

    public PlayerState getState(State state)
    {
        return states.get(state);
    }

    public boolean isFlying()
    {
        return flying;
    }

    @Override
    public boolean isEditorSelectable()
    {
        return false;
    }

    public boolean isSteps()
    {
        return steps;
    }

    public PlayerSkin getDefaultSkin()
    {
        return defaultSkin;
    }
}
