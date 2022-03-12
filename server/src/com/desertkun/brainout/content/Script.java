package com.desertkun.brainout.content;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Script")
public class Script extends Content
{
    private String fileName;

    public Script()
    {
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.fileName = jsonData.getString("file");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        FileHandle fileHandle = BrainOut.PackageMgr.getFile(fileName);

        if (fileHandle.exists())
        {

            int a = 0;
        }
    }
}
