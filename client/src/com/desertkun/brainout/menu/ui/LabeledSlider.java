package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.menu.Menu;

public class LabeledSlider extends Table
{
    private int value;

    public LabeledSlider(int value, int min, int max, int step)
    {
        this.value = value;

        Label amountTitle = new Label(String.valueOf(value) + " / " + max,
                BrainOutClient.Skin, "title-small");
        amountTitle.setAlignment(Align.center);
        amountTitle.setFillParent(true);
        amountTitle.setTouchable(Touchable.disabled);

        {
            final Slider slider = new Slider(min, max,
                    step, false, BrainOutClient.Skin, "slider-2");

            slider.setValue(value);

            slider.addListener(new ChangeListener()
            {
                @Override
                public void changed(ChangeEvent event, Actor actor)
                {
                    int newAmount = (int) slider.getValue();

                    if (newAmount == LabeledSlider.this.value)
                        return;

                    LabeledSlider.this.value = newAmount;
                    Menu.playSound(Menu.MenuSound.select);

                    onChanged(newAmount);
                    amountTitle.setText(String.valueOf(newAmount) + " / " + max);
                }
            });

            TextButton minusButton = new TextButton("-", BrainOutClient.Skin, "button-fill");
            TextButton plusButton = new TextButton("+", BrainOutClient.Skin, "button-fill");

            minusButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    slider.setValue(slider.getValue() - step);
                }
            });

            plusButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    slider.setValue(slider.getValue() + step);
                }
            });


            add(minusButton).size(24);
            add(slider).expandX().fillX();
            add(plusButton).size(24);

            addActor(amountTitle);
        }
    }

    protected void onChanged(int newValue) {}

    public int getValue()
    {
        return value;
    }
}
