package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.desertkun.brainout.content.Shader;

public class ShaderedImage extends Image
{
    private final Shader shader;

    public ShaderedImage(Skin skin, String image, Shader shader)
    {
        super(skin, image);

        this.shader = shader;
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
