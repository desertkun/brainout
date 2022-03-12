package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;

public class BadgeButton extends Button
{
    public BadgeButton(String icon, int amount, boolean checkable)
    {
        super(BrainOutClient.Skin, getStyleName(checkable));

        init(BrainOutClient.getRegion(icon), amount);
    }

    private static String getStyleName(boolean checkable)
    {
        return checkable ? "button-inventory-checkable" : "button-inventory";
    }

    public BadgeButton(TextureRegion icon, int amount, boolean checkable)
    {
        super(BrainOutClient.Skin, getStyleName(checkable));

        init(icon, amount);
    }

    public BadgeButton()
    {
        super(BrainOutClient.Skin, getStyleName(false));

        init(null, 0);
    }

    private void init(TextureRegion region, int amount)
    {
        if (region != null)
        {
            Image image = new Image(region);
            image.setFillParent(true);
            image.setScaling(Scaling.none);
            addActor(image);
        }

        if (amount > 0)
        {
            Table valueContainer = new Table();
            valueContainer.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("button-tab-checked")));

            Label statValue = new Label(String.valueOf(amount), BrainOutClient.Skin, "title-small");
            statValue.setAlignment(Align.center);
            valueContainer.add(statValue);

            CornerButtons cornerButtons = new CornerButtons();
            cornerButtons.setFillParent(true);
            cornerButtons.setCorner(CornerButtons.Corner.topRight, valueContainer, 32, 32);
            cornerButtons.init();

            addActor(cornerButtons);
        }
    }
}
