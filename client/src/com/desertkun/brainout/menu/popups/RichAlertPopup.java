package com.desertkun.brainout.menu.popups;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.menu.ui.RichLabel;

public class RichAlertPopup extends AlertPopup
{
    private final String title;

    public RichAlertPopup(String title, String text)
    {
        super(text);

        this.title = title;
    }

    @Override
    protected String getTitleLabelStyle()
    {
        return "title-yellow";
    }

    @Override
    protected String getTitleBackgroundStyle()
    {
        return "form-red";
    }

    @Override
    protected String getContentBackgroundStyle()
    {
        return "form-border-red";
    }

    @Override
    public String getTitle()
    {
        return BrainOut.LocalizationMgr.parse(title);
    }

    @Override
    protected void initContent(Table data)
    {
        final RichLabel text = new RichLabel(this.text, BrainOutClient.Skin, "title-medium");
        data.add(text).pad(16).center().expand().fill().row();
    }

    @Override
    protected float getFade()
    {
        return Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE;
    }

    @Override
    protected void renderButton(Table buttons, TextButton btn)
    {
        buttons.add(btn).size(192, 64).pad(16);
    }
}
