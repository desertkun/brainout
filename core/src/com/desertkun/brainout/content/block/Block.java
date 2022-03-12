package com.desertkun.brainout.content.block;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.block.BlockData;

public abstract class Block extends ConsumableContent
{
    protected boolean static_;
    protected BlockData instance;

    private float resist;
    private String contactId;
    private Array<String> contactTo;
    private boolean hasContact;
    private boolean generatePhysics;
    private boolean canBeSeenTrough;

    public Block()
    {
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        static_ = jsonData.getBoolean("static", false);

        resist = jsonData.getFloat("resist", 0);
        hasContact = jsonData.getBoolean("hasContact", true);

        if (jsonData.has("contactId"))
        {
            contactId = jsonData.getString("contactId");
        }

        if (jsonData.has("contactTo"))
        {
            contactTo = json.readValue(Array.class, String.class, jsonData.get("contactTo"));
        }

        generatePhysics = jsonData.getBoolean("generatePhysics", true);
        canBeSeenTrough = jsonData.getBoolean("canBeSeenTrough", !(this instanceof Concrete));
    }

    public boolean isCanBeSeenTrough()
    {
        return canBeSeenTrough;
    }

    @Override
    public void dispose()
    {
        instance = null;
    }

    public abstract BlockData getBlock();

    public float getResist()
    {
        return resist;
    }

    public String getContactId()
    {
        return contactId;
    }

    public Array<String> getContactTo()
    {
        return contactTo;
    }

    public int getDefaultLayer()
    {
        return Constants.Layers.BLOCK_LAYER_FOREGROUND;
    }

    public boolean canPlace(Map map, int placeX, int placeY)
    {
        return true;
    }

    public boolean doGeneratePhysics()
    {
        return generatePhysics;
    }

    public boolean hasContact()
    {
        return hasContact;
    }

    public boolean isStatic()
    {
        return static_;
    }
}
