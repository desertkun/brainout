package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.desertkun.brainout.BrainOutClient;

public class ScrollTextArea extends ScrollPane
{
    public ScrollTextArea(String text, Skin skin, String styleName, String scrollStyleName)
    {
        super(new TextArea(text, skin, styleName), skin, scrollStyleName);

        setForceScroll(false, true);
        setFlickScroll(false);
        setOverscroll(false, true);

        TextArea wrapped = getTextArea();

        wrapped.setTextFieldListener((textField, c) ->
        {
            wrapped.setPrefRows(textField.getText().split("\n").length);

            ScrollTextArea.this.layout();
        });
    }

    public TextArea getTextArea()
    {
        return ((TextArea) getWidget());
    }
}
