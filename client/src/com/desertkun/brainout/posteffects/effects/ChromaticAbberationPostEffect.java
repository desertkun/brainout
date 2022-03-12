package com.desertkun.brainout.posteffects.effects;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ChromaticAbberationPostEffect extends PostEffect
{
    private float time;

    public ChromaticAbberationPostEffect()
    {
        super("shader-abberation");

        this.time = 0;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (time < 1)
        {
            time += dt * 4.0f;
        }
    }

    @Override
    protected void declareUniform(ShaderProgram shaderProgram)
    {
        super.declareUniform(shaderProgram);

        shaderProgram.setUniformf("time", time);
    }
}
