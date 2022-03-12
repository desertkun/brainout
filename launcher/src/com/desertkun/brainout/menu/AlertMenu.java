package com.desertkun.brainout.menu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.Launcher;

public class AlertMenu extends Menu
{
    private final String text;
    private final String buttonText;
    private final Runnable clicked;

    public AlertMenu(String text, String buttonText, Runnable clicked)
    {
        this.text = text;
        this.buttonText = buttonText;
        this.clicked = clicked;
    }

    @Override
    public void init()
    {
        attachBackground();

        super.init();
    }

    @Override
    public void initUI(Table content)
    {
        Label errorText = new Label(text, Launcher.SKIN, "title-default");
        errorText.setWrap(true);
        errorText.setAlignment(Align.center);

        content.add(errorText).width(300).pad(32).row();

        TextButton textButton = new TextButton(buttonText, Launcher.SKIN, "button-default");

        textButton.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                clicked.run();
            }
        });

        content.add(textButton).width(128).height(32).row();
    }
}
