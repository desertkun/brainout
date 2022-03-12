package com.desertkun.brainout.menu.tutorial;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.RichLabel;

public class RichTutorialMenu extends TutorialMenu
{
    private final String text;

    public RichTutorialMenu(String text, Runnable closed)
    {
        super(closed);

        this.text = text;
    }

    @Override
    protected void drawFade()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_TRIPLE, getBatch());
    }

    @Override
    public void onInit()
    {
        super.onInit();

        Table parent = new Table();
        parent.setFillParent(true);

        RichLabel label = new RichLabel(text, BrainOutClient.Skin, "title-small");
        label.setTouchable(Touchable.disabled);

        parent.add(label).row();

        TextButton ok = new TextButton(L.get("MENU_OK"), BrainOutClient.Skin, "button-default");

        ok.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                close();
            }
        });

        parent.add(ok).size(192, 48).pad(32).row();

        addActor(parent);
    }
}
