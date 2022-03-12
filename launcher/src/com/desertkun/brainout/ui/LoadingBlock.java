package com.desertkun.brainout.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;

public class LoadingBlock extends Actor
{
    private ShapeRenderer shapeRenderer;

    public LoadingBlock()
    {
        setSize(16, 16);

        shapeRenderer = new ShapeRenderer();
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

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        super.draw(batch, parentAlpha);

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        shapeRenderer.setColor(getColor());
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());

        shapeRenderer.end();

        batch.begin();
    }
}