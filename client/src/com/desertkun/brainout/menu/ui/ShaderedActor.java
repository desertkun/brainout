package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.desertkun.brainout.content.Shader;

public class ShaderedActor extends WidgetGroup
{
    private final Shader shader;

    public ShaderedActor(Actor child, Shader shader)
    {
        this.shader = shader;

        addActor(child);
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        if (!shader.isLoaded())
            return;

        batch.setShader(shader.getShaderProgram());
        _draw(batch, parentAlpha);
        batch.setShader(null);
    }

    protected void _draw(Batch batch, float parentAlpha)
    {
        super.draw(batch, parentAlpha);
    }
}
