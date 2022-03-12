package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.menu.Menu;

public class RewardCounter extends Table
{
    private int i;

    public RewardCounter(int amount, String postFix)
    {
        final Label amountTitle = new Label("0" + postFix, BrainOutClient.Skin, "title-messages-white");
        amountTitle.setAlignment(Align.center);

        int multiplier = MathUtils.clamp(
                (int)(((float)amount / 12.5f) / ClientConstants.Menu.Notify.APPEARANCE), 2, 50);

        i = 0;

        amountTitle.addAction(Actions.sequence(
            Actions.repeat(amount / multiplier, Actions.sequence(
                Actions.delay(0.0025f),
                Actions.run(() ->
                {
                    i += multiplier;
                    amountTitle.setText("+" + i + postFix);

                    if (i % (multiplier * 2) == 0)
                    {
                        Menu.playSound(Menu.MenuSound.character);
                    }
                })
            )),
            Actions.run(() -> amountTitle.setText("+" + amount + postFix))
        ));

        add(amountTitle).expandX().fillX().row();
    }
}
