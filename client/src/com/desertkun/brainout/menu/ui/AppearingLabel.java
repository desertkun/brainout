package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AppearingLabel extends Label
{
    public AppearingLabel(final String text, Skin skin, String styleName, float delay, final Runnable each)
    {
        super(text, skin, styleName);

        addAction(Actions.sequence(
            Actions.repeat(text.length(), Actions.sequence(Actions.delay(delay), Actions.run(new Runnable()
            {
                private int counter = 0;

                @Override
                public void run()
                {
                    counter++;
                    setText(text.substring(0, counter) + "±");
                    each.run();
                }
            }))),
            Actions.run(new Runnable()
            {
                @Override
                public void run()
                {
                    setText(text);
                }
            })
        ));
    }
}
