package com.desertkun.brainout.data.interfaces;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface Renderable
{
    void render(Batch batch, RenderContext context);
    boolean hasRender();

    int getZIndex();
    int getLayer();
}
