package com.desertkun.brainout.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.SpriteWithBlocksComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;

public class ClientHighlightComponent extends Component
{
    private ActiveData highlight = null;
    private static ShapeRenderer HighlightRenderer = null;

    public ClientHighlightComponent()
    {
        super(null, null);
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    public void setHighlight(ActiveData highlight)
    {
        this.highlight = highlight;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (highlight != null)
        {
            SpriteWithBlocksComponentData spi = highlight.getComponent(SpriteWithBlocksComponentData.class);
            if (spi == null)
            {
                return;
            }
            SpriteWithBlocksComponent sp = spi.getContentComponent();

            if (HighlightRenderer == null)
            {
                HighlightRenderer = new ShapeRenderer();
            }

            batch.end();
            HighlightRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            HighlightRenderer.setTransformMatrix(batch.getTransformMatrix());

            HighlightRenderer.begin(ShapeRenderer.ShapeType.Filled);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            HighlightRenderer.setColor(0, 1, 0, 0.5f);

            HighlightRenderer.rect(highlight.getX(), highlight.getY(), sp.getWidth(), sp.getHeight());

            HighlightRenderer.end();

            batch.begin();
        }
    }

    @Override
    public int getZIndex()
    {
        return 1;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public int getLayer()
    {
        return 3;
    }
}
