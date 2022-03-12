package com.desertkun.brainout.menu;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.Launcher;

public abstract class Menu extends Stage
{
    private Table content;

    public void init()
    {
        content = new Table();
        content.align(Align.center);
        content.setFillParent(true);

        initUI(content);

        addActor(content);
    }

    public abstract void initUI(Table content);

    public void release()
    {

    }

    public void attachBackground()
    {
        Image bg = new Image(Launcher.getRegion("bg-loading"));
        bg.setScaling(Scaling.fit);
        bg.setFillParent(true);

        addActor(bg);
    }
}
