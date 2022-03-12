package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.LabeledSlider;

public class SendResourcesToClanMenu extends Menu
{
    private final int max;
    private final Callback callback;
    private int amount;

    public interface Callback
    {
        void approve(int amount);
        void cancel();
    }

    public SendResourcesToClanMenu(int max, Callback callback)
    {
        this.max = max;
        this.callback = callback;
        this.amount = 1;
    }

    @Override
    public Table createUI()
    {
        Table root = new Table(BrainOutClient.Skin);

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("MENU_CLAN_DONATE"), BrainOutClient.Skin, "title-yellow");

            header.add(title).expandX().center();

            root.add(header).height(32).expandX().fillX().row();
        }

        Table data = new Table(BrainOutClient.Skin);
        data.setBackground("form-border-red");

        {
            Label description = new Label(L.get("MENU_CLAN_DONATE_AMOUNT"), BrainOutClient.Skin, "title-small");
            description.setWrap(true);
            description.setAlignment(Align.center);

            data.add(description).expandX().fillX().pad(32).padBottom(16).row();
        }

        {
            Image nuclearMaterial = new Image(BrainOutClient.Skin, "icon-clan-resources");
            nuclearMaterial.setScaling(Scaling.none);

            data.add(nuclearMaterial).size(32).center().pad(12).row();
        }

        {
            LabeledSlider slider = new LabeledSlider(this.amount, 1, this.max, 1)
            {
                @Override
                protected void onChanged(int newValue)
                {
                    SendResourcesToClanMenu.this.amount = newValue;
                }
            };

            data.add(slider).width(256).padBottom(32).row();
        }

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
                        callback.cancel();
                    }
                });

                buttons.add(close).uniformX().expandX().fillX().height(64);
            }

            {
                TextButton donate = new TextButton(L.get("MENU_CLAN_DONATE"), BrainOutClient.Skin, "button-green");

                donate.addListener(new ClickListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        pop();
                        callback.approve(amount);
                    }
                });

                buttons.add(donate).uniformX().expandX().fillX().height(64).row();
            }

            root.add(buttons).expandX().fillX().colspan(2).row();
        }

        return root;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
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
