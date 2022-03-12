package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOutClient;

public class StatisticsChart extends Table implements Disposable
{
    public static final float ACCURACY = 0.05f;
    private final float maxValue;

    private final Label maxLabel;
    private final Label avgLabel;

    private float[] data;
    private int current;
    private final ChartSource source;
    private float counter;

    private float max;
    private float avg;

    @Override
    public void dispose()
    {

    }

    public interface ChartSource
    {
        float getValue();
    }

    public StatisticsChart(int dataSize, float maxValue, String title, ChartSource source)
    {
        this.data = new float[dataSize];
        this.source = source;
        this.maxValue = maxValue;

        this.maxLabel = new Label("", BrainOutClient.Skin, "chat-console");
        this.avgLabel = new Label("", BrainOutClient.Skin, "chat-console");

        Label p = new Label(title, BrainOutClient.Skin, "chat-console");
        p.setAlignment(Align.center);

        add(p).expandX().fillX().colspan(2).row();

        align(Align.top);
        add().expand().colspan(2).row();

        add(maxLabel).pad(4).left();
        add(avgLabel).pad(4).right();
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        counter += delta;

        while (counter >= ACCURACY)
        {
            counter -= ACCURACY;

            add(source.getValue());

            maxLabel.setText(String.valueOf((int)(getMaxValue() * 100.0f) / 100.0f));
            avgLabel.setText(String.valueOf((int)(getAverageValue() * 100.0f) / 100.0f));
        }
    }

    private void add(float data)
    {
        this.data[current] = data;
        current++;
        if (current >= this.data.length)
        {
            current = 0;
        }

        this.max = _getMaxValue();
        this.avg = _getAverageValue();
    }

    public float getMaxValue()
    {
        return max;
    }

    public float getAverageValue()
    {
        return avg;
    }

    private float _getAverageValue()
    {
        float sum = 0;

        for (float value: data)
        {
            sum += value;
        }

        return sum / data.length;
    }

    private float _getMaxValue()
    {
        float max = 0;

        for (float value: data)
        {
            if (value > max)
                max = value;
        }

        return max;
    }

    @Override
    protected void drawChildren(Batch batch, float parentAlpha)
    {
        batch.end();

        ShapeRenderer renderer = BrainOutClient.ShapeRenderer;
        renderer.setTransformMatrix(batch.getTransformMatrix());
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        renderer.setColor(0, 0, 0, 0.25f);
        renderer.rect(getX(), getY(), getWidth(), getHeight());

        renderer.end();

        renderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        if (max > maxValue)
        {
            renderer.setColor(1, 0, 0, 0.6f);
        }
        else
        {
            renderer.setColor(0, 1, 0, 0.6f);
        }

        float df = getWidth() / data.length;
        float h = getHeight();
        int c = data.length;
        float _max = Math.max(maxValue, max);

        for (int i = current; i < c; i++)
        {
            float now = data[i];
            float next = data[i >= c - 1 ? 0 : i + 1];

            float x1 = getX() + df * (i - current);
            float y1 = getY() + (now  / _max) * h;

            float x2 = getX() + df * (i - current + 1);
            float y2 = getY() + (next  / _max) * h;

            renderer.line(x1, y1, x2, y2);
        }

        for (int i = 0; i < current - 1; i++)
        {
            float now = data[i];
            float next = data[i + 1];

            float x1 = getX() + df * (i + c - current);
            float y1 = getY() + (now  / _max) * h;

            float x2 = getX() + df * (i + c - current + 1);
            float y2 = getY() + (next  / _max) * h;

            renderer.line(x1, y1, x2, y2);
        }

        renderer.end();

        batch.begin();

        super.drawChildren(batch, parentAlpha);
    }
}
