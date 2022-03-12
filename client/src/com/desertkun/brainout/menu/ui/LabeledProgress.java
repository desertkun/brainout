package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;

public class LabeledProgress extends Table
{
    public LabeledProgress(Skin skin, String progressStyle, int min, int max, int have, int need)
    {
        ProgressBar scoreBar = new ProgressBar(min, max,
                1, false, skin, progressStyle);

        scoreBar.setValue(have);

        add(scoreBar).expand().fill().pad(1, 0, 0, 0).row();

        Label scoreValue = new Label(String.valueOf(have) + " / " + need,
                BrainOutClient.Skin, "title-small");

        scoreValue.setAlignment(Align.center);
        scoreValue.setFillParent(true);

        addActor(scoreValue);
    }
}
