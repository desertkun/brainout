package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.LabeledSlider;

public abstract class AmountMenu extends Menu
{
    protected int max;
    private int amount;
    protected Label title;
    private Table sliderHolder;

    public AmountMenu(int proffered, int max)
    {
        this.max = max;
        this.amount = proffered;
    }

    public abstract void approve(int amount);
    public abstract void cancel();

    protected String getTitle()
    {
        return L.get("MENU_CUSTOM_AMOUNT");
    }

    protected void renderContentAboveSlider(Table data) {}

    @Override
    public Table createUI()
    {
        Table root = new Table(BrainOutClient.Skin);

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            title = new Label(getTitle(), BrainOutClient.Skin, "title-yellow");

            header.add(title).expandX().center();

            root.add(header).height(32).expandX().fillX().row();
        }

        Table data = new Table(BrainOutClient.Skin);
        data.setBackground("form-border-red");

        renderContentAboveSlider(data);

        sliderHolder = new Table();
        refreshSlider();
        data.add(sliderHolder).row();

        root.add(data).minWidth(530).expandX().fillX().row();

        {
            Table buttons = new Table();

            {
                TextButton close = new TextButton(L.get("MENU_CLOSE"), BrainOutClient.Skin, "button-default");

                close.addListener(new ClickListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.back);
                        pop();
                        cancel();
                    }
                });

                buttons.add(close).uniformX().expandX().fillX().height(64);
            }

            {
                TextButton apply = new TextButton(L.get("MENU_APPLY"), BrainOutClient.Skin, "button-green");

                apply.addListener(new ClickListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        pop();
                        approve(amount);
                    }
                });

                buttons.add(apply).uniformX().expandX().fillX().height(64).row();
            }

            root.add(buttons).expandX().fillX().colspan(2).row();
        }

        return root;
    }

    protected void refreshSlider()
    {
        sliderHolder.clearChildren();

        if (amount > max)
        {
            amount = max;
        }

        {
            LabeledSlider slider = new LabeledSlider(this.amount, 1, this.max, 1)
            {
                @Override
                protected void onChanged(int newValue)
                {
                    AmountMenu.this.amount = newValue;
                }
            };

            sliderHolder.add(slider).width(256).pad(32).row();
        }
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        if (keyCode == Input.Keys.ENTER)
        {
            Menu.playSound(MenuSound.select);
            pop();
            approve(amount);

            return true;
        }

        return super.keyDown(keyCode);
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
