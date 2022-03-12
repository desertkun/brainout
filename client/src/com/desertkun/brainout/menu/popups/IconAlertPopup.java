package com.desertkun.brainout.menu.popups;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.desertkun.brainout.L;

public class IconAlertPopup extends AlertPopup
{
    private final TextureRegion icon;

    public IconAlertPopup(String text, TextureRegion icon)
    {
        super(text);

        this.icon = icon;
    }

    @Override
    protected void initContent(Table data)
    {
        super.initContent(data);

        data.add(new Image(icon)).pad(10).row();
    }
}
