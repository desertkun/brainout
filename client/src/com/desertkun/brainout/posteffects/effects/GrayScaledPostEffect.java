package com.desertkun.brainout.posteffects.effects;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GrayScaledPostEffect extends PostEffect
{
    private float value;

    public GrayScaledPostEffect()
    {
        super("shader-grayscaled");

        this.value = 1.0f;
    }

    public GrayScaledPostEffect(String shader)
    {
        super(shader);

        this.value = 1.0f;
    }

    public void setValue(float value)
    {
        this.value = value;
    }

    @Override
    protected void declareUniform(ShaderProgram shaderProgram)
    {
        super.declareUniform(shaderProgram);

        shaderProgram.setUniformf("value", value);
    }
}
