package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;

public class Notify extends Table
{
    private Label title;

    public Notify(final String title, boolean positive, final int awardData)
    {
        this(title, positive, awardData, "");
    }

    public Notify(final String title, boolean positive, final int awardData, final String postFix)
    {
        align(Align.center);

        setBackground(new TextureRegionDrawable(
            BrainOutClient.getRegion(positive ? "notice-background" : "notice-background-negative")));

        if (title != null)
        {
            this.title = new Label(title, BrainOutClient.Skin, "title-messages-white");
            this.title.setAlignment(Align.center);
            add(this.title).pad(4, 4, 0, 4).expandX().fillX().row();
        }

        if (awardData != 0)
        {
            Actor counter = new RewardCounter(awardData, postFix);
            add(counter).expandX().fillX().row();
        }
    }

    public Label getTitle()
    {
        return title;
    }
}
