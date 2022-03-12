package com.desertkun.brainout.editor.data;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;

public class EditorGrid implements RenderUpdatable
{
    private final String dimension;
    private boolean enabled;

    public EditorGrid(String dimension)
    {
        this.dimension = dimension;
        this.enabled = false;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        if (enabled)
        {
            Map map = Map.Get(dimension);

            if (map == null)
                return;

            batch.end();

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl20.glLineWidth(1);

            ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setProjectionMatrix(map.getProjectionMatrix());

            {
                shapeRenderer.setColor(1, 1, 1, 0.1f);

                int w = map.getWidth(), h = map.getHeight();

                for (int j = 0; j <= h; j++) {
                    shapeRenderer.line(0, j, w, j);
                }

                for (int j = 0; j <= w; j++) {
                    shapeRenderer.line(j, 0, j, h);
                }
            }

            {
                shapeRenderer.setColor(0.5f, 1, 0.5f, 0.5f);

                int bw = map.getBlocks().getBlockWidth(), bh = map.getBlocks().getBlockHeight();

                for (int j = 0; j <= bh; j++) {
                    shapeRenderer.line(0, j * Constants.Core.CHUNK_SIZE,
                        bw * Constants.Core.CHUNK_SIZE, j * Constants.Core.CHUNK_SIZE);
                }

                for (int j = 0; j <= bw; j++) {
                    shapeRenderer.line(j * Constants.Core.CHUNK_SIZE, 0,
                        j * Constants.Core.CHUNK_SIZE, bh * Constants.Core.CHUNK_SIZE);
                }
            }

            shapeRenderer.end();

            batch.begin();
        }
    }

    @Override
    public void update(float dt)
    {

    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public int getZIndex()
    {
        return 30;
    }

    public void switchEnabled()
    {
        setEnabled(!isEnabled());
    }

    @Override
    public int getLayer()
    {
        return 0;
    }
}

