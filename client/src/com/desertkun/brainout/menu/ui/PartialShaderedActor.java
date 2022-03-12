package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.content.Shader;

public class PartialShaderedActor extends ShaderedActor
{
    private final int parts;
    private final int of;
    private final InstrumentIcon icon;
    private final String animationName;

    public PartialShaderedActor(InstrumentIcon instrumentIcon, Shader shader, int parts, int of)
    {
        this("parts", instrumentIcon, shader, parts, of);
    }

    public PartialShaderedActor(String animationName,
                                InstrumentIcon instrumentIcon, Shader shader, int parts, int of)
    {
        super(instrumentIcon, shader);

        this.animationName = animationName;
        this.icon = instrumentIcon;
        this.parts = parts;
        this.of = of;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        icon.resetAnimation();

        super.draw(batch, parentAlpha);

        icon.animateTo(animationName, parts, of);

        _draw(batch, parentAlpha);
    }
}
