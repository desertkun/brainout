package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;

public class ButtonProgressBar extends Table
{
    public ButtonProgressBar(int value, int of, Skin skin, String styleName)
    {
        ProgressBar partsProgress = new ProgressBar(
                0, of, 1, false, skin, styleName
        );

        partsProgress.setValue(value);

        align(Align.bottom);
        setTouchable(Touchable.disabled);
        setFillParent(true);
        add(partsProgress).padBottom(1).expandX().fillX().row();
    }
}
