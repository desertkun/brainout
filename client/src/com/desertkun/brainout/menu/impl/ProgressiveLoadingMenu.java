package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.LoadingBlock;

public class ProgressiveLoadingMenu extends Menu
{
    private final LoadingProgress loadingProgress;
    private final String loadingTitle;
    private Label loadintPrecentage;
    private Label loadingLabel;
    private Color loadingColor = Color.valueOf("F8B800");

    public static interface LoadingProgress
    {
        public float get();
    }

    public ProgressiveLoadingMenu(LoadingProgress loadingProgress, String loadingTitle)
    {
        this.loadingProgress = loadingProgress;
        this.loadingTitle = loadingTitle;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-loading");
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();
        data.align(Align.center | Align.bottom);

        if (BrainOutClient.Skin.has("title-small", Label.LabelStyle.class))
        {
            Table tbl = new Table();

            this.loadingLabel = new Label(loadingTitle, BrainOutClient.Skin, "title-messages-white");
            this.loadintPrecentage = new Label(loadingTitle, BrainOutClient.Skin, "title-messages-white");

            tbl.add(loadingLabel).pad(10);
            tbl.add(loadintPrecentage).width(50).pad(10).row();

            data.add(tbl).row();
        }

        ShapeRenderer rectangleBatch = BrainOutClient.ShapeRenderer;

        data.add(new Actor()
        {
            @Override
            public void draw(Batch batch, float parentAlpha)
            {
                super.draw(batch, parentAlpha);

                batch.end();

                rectangleBatch.begin(ShapeRenderer.ShapeType.Filled);

                rectangleBatch.setColor(Color.BLACK);
                rectangleBatch.rect(getX(), getY(), getWidth(), getHeight());

                rectangleBatch.setColor(loadingColor);
                rectangleBatch.rect(getX() + 2, getY() + 2, (getWidth() - 4) * loadingProgress.get(), getHeight() - 4);


                rectangleBatch.end();

                batch.begin();
            }

            @Override
            public float getWidth()
            {
                return Constants.Menu.LOADINGBAR_WIDTH;
            }

            @Override
            public float getHeight()
            {
                return Constants.Menu.LOADINGBAR_HEIGHT;
            }

        }).padBottom(32).row();

        renderLoadingBlock(data);

        return data;

    }

    protected void renderLoadingBlock(Table data)
    {
        data.add(new LoadingBlock()).expandX().right().pad(32).row();
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if (loadintPrecentage != null)
        {
            loadintPrecentage.setText("(" + (int)(loadingProgress.get() * 100) + "%)");
        }
    }

    public void setTitle(String title)
    {
        if (loadingLabel != null)
        {
            loadingLabel.setText(title);
        }
    }
}
