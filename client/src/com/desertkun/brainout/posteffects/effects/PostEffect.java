package com.desertkun.brainout.posteffects.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.Shader;

public class PostEffect implements Disposable
{
    private final ShaderProgram shaderProgram;
    private final Vector2 screenSize;

    public PostEffect(String shader)
    {
        shaderProgram = ((Shader) BrainOutClient.ContentMgr.get(shader)).getShaderProgram();

        screenSize = new Vector2(BrainOutClient.getWidth(), BrainOutClient.getHeight());
    }

    public void begin(Batch batch)
    {
        batch.setShader(shaderProgram);

        update(Gdx.graphics.getDeltaTime());

        declareUniform(shaderProgram);
    }

    public int getDownscale()
    {
        return 1;
    }


    public void end(Batch batch)
    {
        batch.setShader(null);
    }

    @Override
    public void dispose()
    {
        shaderProgram.dispose();
    }

    public ShaderProgram getShaderPogram()
    {
        return shaderProgram;
    }

    public void update(float dt)
    {

    }

    protected void declareUniform(ShaderProgram shaderProgram)
    {
        shaderProgram.setUniformf("screen", screenSize);
    }
}
