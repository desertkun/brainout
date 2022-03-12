package com.desertkun.brainout.menu.popups;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Popup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.RichLabel;

public class RichConfirmationPopup extends ConfirmationPopup
{
    public RichConfirmationPopup(String text)
    {
        super(text);
    }

    public RichConfirmationPopup()
    {
        super("");
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
    protected void initContent(Table data)
    {
        final RichLabel text = new RichLabel(this.text, BrainOutClient.Skin, "title-medium");
        data.add(text).pad(16).center().expand().fill().row();
    }
}
