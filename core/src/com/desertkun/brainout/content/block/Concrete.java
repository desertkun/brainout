package com.desertkun.brainout.content.block;


import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.block.contact.*;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.ConcreteBD;
import com.desertkun.brainout.data.containers.ChunkData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.block.Concrete")
public class Concrete extends Block
{
    private ContactStrategy contactStrategy;
    private boolean editor;

    public enum ContactStrategy
    {
        block,
        triangle,
        platform,
        triangleNoBullets,
        combined,
        teamLimit
    }

    private static ContactShape[] CONTACT_SHAPES;

    static
    {
        CONTACT_SHAPES = new ContactShape[]
        {
            // ContactStrategy.block
            new CSBlock(),
            // ContactStrategy.triangle
            new CSTriangle(),
            // ContactStrategy.platform
            new CSPlatform(),
            // ContactStrategy.triangleNoBullets
            new CSTriangleNoBullets(),
            // ContactStrategy.combined
            new CSCombined(),
            // ContactStrategy.teamLimit
            new CSTeamLimit()
        };
    }

    public Concrete()
    {
        contactStrategy = ContactStrategy.block;
    }

    @Override
    public BlockData getBlock()
    {
        if (static_)
        {
            if (instance == null)
            {
                instance = new ConcreteBD(this);
            }

            return instance;
        }

        return new ConcreteBD(this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        static_ = jsonData.getBoolean("static", false);

        if (jsonData.has("contactStrategy"))
        {
            contactStrategy = ContactStrategy.valueOf(jsonData.getString("contactStrategy"));
        }

        editor = jsonData.getBoolean("editor", true);
    }

    public ContactShape getContactShape()
    {
        return CONTACT_SHAPES[contactStrategy.ordinal()];
    }

    @Override
    public boolean isEditorSelectable()
    {
        return editor;
    }
}
