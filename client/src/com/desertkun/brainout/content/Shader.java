package com.desertkun.brainout.content;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Shader")
public class Shader extends Content
{
    private String vs;
    private String fs;

    private ShaderProgram shaderProgram;
    private boolean loaded;

    public ShaderProgram getShaderProgram()
    {
        return shaderProgram;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.vs = jsonData.getString("vs");
        this.fs = jsonData.getString("fs");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        FileHandle fileVS = BrainOutClient.PackageMgr.getFile(vs);
        FileHandle fileFS = BrainOutClient.PackageMgr.getFile(fs);

        if (fileVS == null || fileFS == null)
            return;

        try
        {
            shaderProgram = new ShaderProgram(fileVS, fileFS);
            loaded = true;
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
            loaded = false;
        }
    }

    public boolean isLoaded()
    {
        return loaded;
    }

    @Override
    public void dispose()
    {
        if (shaderProgram != null)
        {
            shaderProgram.dispose();
        }
    }
}
