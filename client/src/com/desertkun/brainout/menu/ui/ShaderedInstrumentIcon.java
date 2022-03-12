package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.content.Shader;
import com.desertkun.brainout.data.instrument.InstrumentInfo;

public class ShaderedInstrumentIcon extends InstrumentIcon
{
    private final Shader shader;

    public ShaderedInstrumentIcon(Shader shader, InstrumentInfo info, float instrumentScale, boolean dynamic)
    {
        super(info, instrumentScale, dynamic);

        this.shader = shader;
    }

    @Override
    protected void drawChildren(Batch batch, float parentAlpha)
    {
        if (!shader.isLoaded())
            return;

        batch.setShader(shader.getShaderProgram());
        super.drawChildren(batch, parentAlpha);
        batch.setShader(null);
    }
}
