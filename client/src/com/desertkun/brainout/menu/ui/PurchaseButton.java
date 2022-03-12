package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.utils.MenuUtils;

public class PurchaseButton extends Button
{
    private final Label titleLabel;

    public PurchaseButton(String title, Skin skin, String style, int price, String currency)
    {
        super(skin, style);

        {
            titleLabel = new Label(title, BrainOutClient.Skin, "title-small");

            add(titleLabel).padRight(32).padLeft(16);
        }

        {
            add(new Label(String.valueOf(price), BrainOutClient.Skin, "title-small")).padRight(2);
            add(new Image(BrainOutClient.Skin, MenuUtils.getStatIcon(currency))).padRight(16);
        }
    }

    @Override
    public void setDisabled(boolean isDisabled)
    {
        super.setDisabled(isDisabled);

        titleLabel.setStyle(BrainOutClient.Skin.get(isDisabled ? "title-gray" : "title-small", Label.LabelStyle.class));
    }
}
