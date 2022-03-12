package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOutClient;

public class LoadingBlock extends Image
{
    private class BlockDrawable extends BaseDrawable implements Disposable
    {
        @Override
        public void draw(Batch batch, float x, float y, float width, float height)
        {
            batch.end();

            ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.setColor(getColor());
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();

            batch.begin();
        }


        @Override
        public float getMinWidth()
        {
            return 16;
        }

        public float getMinHeight()
        {
            return 16;
        }

        @Override
        public void dispose()
        {

        }
    }

    public LoadingBlock()
    {
        setDrawable(new BlockDrawable());
        setSize(16, 16);

        setColor(Color.BLACK);

        float duration = 0.05f;

        addAction(Actions.repeat(RepeatAction.FOREVER,
            Actions.sequence(
                Actions.color(new Color(Color.valueOf("000000")), duration),
                Actions.color(new Color(Color.valueOf("333333")), duration),
                Actions.color(new Color(Color.valueOf("AC7C00")), duration),
                Actions.color(new Color(Color.valueOf("F8B800")), duration),
                Actions.color(new Color(Color.valueOf("FFFFFF")), duration),
                Actions.color(new Color(Color.valueOf("F8B800")), duration),
                Actions.color(new Color(Color.valueOf("AC7C00")), duration),
                Actions.color(new Color(Color.valueOf("333333")), duration)
            )));
    }
}
