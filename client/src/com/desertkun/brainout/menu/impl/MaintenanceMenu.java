package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class MaintenanceMenu extends Menu
{
    private final String message;

    public MaintenanceMenu(String message)
    {
        this.message = message;
    }

    @Override
    public Table createUI()
    {
        try
        {
            Image bg = new Image(BrainOutClient.getRegion("bg-ingame"));
            bg.setScaling(Scaling.fit);
            bg.setFillParent(true);

            getRoot().addActorAt(0, bg);
        }
        catch (RuntimeException e)
        {
            // ignore
        }

        Table root = new Table();

        {
            Label label = new Label(
                    L.get("MENU_ATTENTION"),
                    BrainOutClient.Skin, "title-yellow"
            );

            label.setAlignment(Align.center);

            root.add(new BorderActor(label, "form-red")).padBottom(0).expandX().fillX().row();
        }

        Table data = new Table();
        data.setSkin(BrainOutClient.Skin);
        data.setBackground("form-border-red");
        data.align(Align.center);

        root.add(data);

        Image maintenance = new Image(BrainOutClient.getRegion("maintenance"));

        data.add(maintenance).expandX().pad(8).row();

        {
            Label label = new Label(
                message,
                BrainOutClient.Skin, "title-yellow"
            );

            label.setAlignment(Align.center);

            data.add(label).pad(16).expandX().fillX().row();
        }

        TextButton close = new TextButton(L.get("MENU_EXIT_GAME"),
            BrainOutClient.Skin, "button-default");

        close.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                BrainOutClient.exit();
            }
        });

        data.add(close).size(192, 64).pad(16).expandX().row();

        return root;
    }
}
