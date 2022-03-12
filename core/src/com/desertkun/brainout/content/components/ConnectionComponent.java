package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.ConnectionComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import java.util.Random;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ConnectionComponent")
public class ConnectionComponent extends ContentComponent implements Json.Serializable
{
    public static final String TAG = "NEIGH";
    public static final int NEIGHBORS_AMOUNT = 16;
    private static final RandomXS128 random = new RandomXS128();

    private String stickId;
    private Array<String> stickTo;
    private ObjectMap<Integer, Array<AtlasRegion>> regions;

    private Array<Array<String>> arrayData;
    private ObjectMap<Integer, Array<String>> mapData;

    private ConnectStrategy strategy;
    private boolean nine;

    public enum ConnectStrategy
    {
        random,
        square
    }

    public ConnectionComponent()
    {
        regions = new ObjectMap<>();
        stickId = "";
        stickTo = null;
        strategy = ConnectStrategy.random;
        nine = false;
    }

    public boolean isNine()
    {
        return nine;
    }

    @Override
    public void write(Json json)
    {

    }

    public String getStickId()
    {
        return stickId;
    }

    public Array<String> getStickTo()
    {
        return stickTo;
    }

    public AtlasRegion getRegion(int currentX, int currentY, int neighborId)
    {
        Array<AtlasRegion> region = regions.get(neighborId);

        if (region == null)
        {
            return null;
        }

        if (region.size > 0)
        {
            int idx;

            switch (strategy)
            {
                case square:
                {
                    int square = (int)Math.sqrt(region.size);

                    idx = currentX % square + (currentY % square) * square;

                    break;
                }
                case random:
                default:
                {
                    // same result every time
                    random.setSeed(((long)currentY << 32) | (long)currentX);
                    idx = random.nextInt(region.size);

                    break;
                }
            }

            return region.get(idx);
        }

        return null;
    }

    protected int getNeighborsAmount()
    {
        return NEIGHBORS_AMOUNT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonValue)
    {
        stickId = jsonValue.getString("stickId");

        if (jsonValue.has("stickTo"))
        {
            JsonValue stickTo_ = jsonValue.get("stickTo");

            if (stickTo_.isArray())
            {
                stickTo = json.readValue(Array.class, String.class, stickTo_);
            }
            else
            {
                stickTo = new Array<>();
                stickTo.add(stickTo_.asString());
            }
        }


        if (jsonValue.has("data"))
        {
            JsonValue data = jsonValue.get("data");

            if (data.isArray())
            {
                arrayData = new Array<>();
                for (JsonValue value : data)
                {
                    Array<String> a = new Array<>();
                    arrayData.add(a);

                    if (value.isArray())
                    {
                        for (JsonValue v : value)
                        {
                            a.add(v.asString());
                        }
                    }
                    if (value.isString())
                    {
                        a.add(value.asString());
                    }
                }
            }
            else if (data.isObject())
            {
                mapData = new ObjectMap<>();
                for (JsonValue value : data)
                {
                    Array<String> a = new Array<>();
                    mapData.put(Integer.valueOf(value.name()), a);
                    if (value.isArray())
                    {
                        for (JsonValue v : value)
                        {
                            a.add(v.asString());
                        }
                    }
                    if (value.isString())
                    {
                        a.add(value.asString());
                    }
                }
            }
        }

        if (jsonValue.has("strategy"))
        {
            strategy = json.readValue(ConnectStrategy.class, jsonValue.get("strategy"));
        }

        if (jsonValue.has("nine"))
        {
            nine = jsonValue.getBoolean("nine");
        }
    }


    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (arrayData != null && arrayData.size == NEIGHBORS_AMOUNT)
        {
            int i = 0;

            for (Array<String> array : arrayData)
            {
                Array<AtlasRegion> region = new Array<AtlasRegion>();

                for (String item : array)
                {
                    region.add(BrainOut.getInstance().getTextureRegion(item));
                }

                regions.put(i++, region);
            }
        }
        else
        if (mapData != null)
        {
            for (ObjectMap.Entry<Integer, Array<String>> entry : mapData)
            {
                Array<AtlasRegion> region = new Array<AtlasRegion>();

                for (String item : entry.value)
                {
                    region.add(BrainOut.getInstance().getTextureRegion(item));
                }

                regions.put(entry.key, region);

            }
        }
        else
        {
            throw new IllegalArgumentException("Expected to getByIndex " + NEIGHBORS_AMOUNT + " neighbors");
        }
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ConnectionComponentData(componentObject, this);
    }
}
