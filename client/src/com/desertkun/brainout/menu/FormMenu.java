package com.desertkun.brainout.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;

public class FormMenu extends Menu
{
    public FormMenu()
    {
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();
        data.setSkin(BrainOutClient.Skin);
        data.setBackground(formBorderStyle());
        data.align(Align.center);

        return data;
    }

    protected String formBorderStyle()
    {
        return "form-default";
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE, getBatch());

        super.render();
    }
}
