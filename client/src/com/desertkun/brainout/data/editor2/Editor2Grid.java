package com.desertkun.brainout.data.editor2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.data.Editor2Map;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.interfaces.RenderContext;

public class Editor2Grid extends ActiveData
{
    private boolean gridEnabled;

    private final Color background;
    private final Color lines;
    private final Color dashes;

    public Editor2Grid(String dimension)
    {
        super(null, dimension);

        background = new Color(0x2c2c2cff);
        lines = new Color(0x606060ff);
        dashes = new Color(0x60606055);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        Editor2Map map = Map.Get(getDimension(), Editor2Map.class);

        if (map == null || map.isPreviewMode())
            return;

        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
        if (csGame != null)
        {
            if (csGame.getPlayerData() != null)
            {
                // no grid while spawned
                return;
            }
        }

        if (gridEnabled)
        {
            renderGrid(batch, context);
        }
    }

    private void renderGrid(Batch batch, RenderContext context)
    {
        Editor2Map map = Map.Get(getDimension(), Editor2Map.class);

        if (map == null)
            return;

        int w = map.getWidth(), h = map.getHeight();

        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glLineWidth(2);

        ShapeRenderer shapes = BrainOutClient.ShapeRenderer;

        shapes.setProjectionMatrix(map.getProjectionMatrix());

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(background);
        shapes.rect(0, 0, w, h);

        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);

        shapes.setProjectionMatrix(map.getProjectionMatrix());

        shapes.setColor(dashes);

        for (int j = 1; j <= h; j += 2) {
            shapes.line(0, j, w, j);
        }

        for (int j = 1; j <= w; j += 2) {
            shapes.line(j, 0, j, h);
        }

        shapes.setColor(lines);

        for (int j = 0; j <= h; j += 2) {
            shapes.line(0, j, w, j);
        }

        for (int j = 0; j <= w; j += 2) {
            shapes.line(j, 0, j, h);
        }

        shapes.setColor(lines);

        for (int j = 0; j <= h; j += 2) {
            shapes.line(0, j, w, j);
        }

        for (int j = 0; j <= w; j += 2) {
            shapes.line(j, 0, j, h);
        }

        shapes.setColor(0.5f, 1, 0.5f, 0.5f);

        int bw = map.getBlocks().getBlockWidth(), bh = map.getBlocks().getBlockHeight();

        for (int j = 0; j <= bh; j++) {
            shapes.line(0, j * Constants.Core.CHUNK_SIZE,
                    bw * Constants.Core.CHUNK_SIZE, j * Constants.Core.CHUNK_SIZE);
        }

        for (int j = 0; j <= bw; j++) {
            shapes.line(j * Constants.Core.CHUNK_SIZE, 0,
                    j * Constants.Core.CHUNK_SIZE, bh * Constants.Core.CHUNK_SIZE);
        }

        shapes.end();

        batch.begin();
    }

    public boolean isGridEnabled()
    {
        return gridEnabled;
    }

    public void setGridEnabled(boolean gridEnabled)
    {
        this.gridEnabled = gridEnabled;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public int getZIndex()
    {
        return 1;
    }

    @Override
    public int getLayer()
    {
        return 0;
    }

    @Override
    public float getX()
    {
        return 0;
    }

    @Override
    public float getY()
    {
        return 0;
    }

    @Override
    public void setX(float x)
    {

    }

    @Override
    public void setY(float y)
    {

    }

    @Override
    public float getAngle()
    {
        return 0;
    }
}
