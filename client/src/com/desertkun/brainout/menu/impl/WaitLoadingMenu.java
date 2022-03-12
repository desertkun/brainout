package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.LoadingBlock;

public class WaitLoadingMenu extends Menu
{
    private final String loadingTitle;
    private final boolean showImage;
    private Label loadingLabel;

    public WaitLoadingMenu(String loadingTitle)
    {
        this.loadingTitle = loadingTitle;
        this.showImage = false;
    }

    public WaitLoadingMenu(String loadingTitle, boolean showImage)
    {
        this.loadingTitle = loadingTitle;
        this.showImage = showImage;
    }

    @Override
    public boolean popIfFocusOut()
    {
        return true;
    }

    @Override
    public boolean lockRender()
    {
        return showImage;
    }

    @Override
    protected TextureRegion getBackground()
    {
        if (!showImage)
            return null;

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
        data.align(Align.right | Align.bottom);

        if (BrainOutClient.Skin.has("title-small", Label.LabelStyle.class))
        {
            this.loadingLabel = new Label(loadingTitle, BrainOutClient.Skin, "title-messages-white");
            data.add(loadingLabel).pad(10);
        }

        data.add(new LoadingBlock()).pad(32);

        return data;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(0.5f, getBatch());

        super.render();
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
