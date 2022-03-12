package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.RichLabel;

public class PrivacyPolicyMenu extends Menu
{
    private final Runnable accepted;
    private String text;

    public PrivacyPolicyMenu(Runnable accepted)
    {
        text = BrainOutClient.PackageMgr.getFile("mainmenu:other/privacy").readString();
        this.accepted = accepted;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("MENU_PRIVACY_POLICY"), BrainOutClient.Skin, "title-yellow");

            header.add(title).expandX().row();
            data.add(header).expandX().fillX().row();
        }

        Table body = new Table(BrainOutClient.Skin);
        body.setBackground("form-default");

        {
            RichLabel contents = new RichLabel(text, BrainOutClient.Skin, "title-small");

            ScrollPane pane = new ScrollPane(contents, BrainOutClient.Skin, "scroll-default");
            pane.setFadeScrollBars(false);

            body.add(pane).size(600, 500).pad(16).row();
            setScrollFocus(pane);
        }

        if (accepted == null)
        {
            Table buttons = new Table();

            {
                TextButton close = new TextButton(L.get("MENU_CLOSE"), BrainOutClient.Skin, "button-default");

                close.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pop();
                    }
                });

                buttons.add(close).size(196, 64).pad(8);
            }

            body.add(buttons).expandX().fillX().pad(8).row();
        }
        else
        {
            Table buttons = new Table();

            {
                TextButton cancel = new TextButton(L.get("MENU_EXIT"), BrainOutClient.Skin, "button-default");

                cancel.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        canceled();
                    }
                });

                buttons.add(cancel).size(196, 64).pad(8);
            }

            {
                TextButton accept = new TextButton(L.get("MENU_ACCEPT"), BrainOutClient.Skin, "button-green");

                accept.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        accepted();
                    }
                });

                buttons.add(accept).size(196, 64).pad(8);
            }

            body.add(buttons).expandX().fillX().pad(8).row();
        }

        data.add(body).expand().fill().row();

        return data;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public boolean lockRender()
    {
        return true;
    }

    private void accepted()
    {
        accepted.run();
    }

    private void canceled()
    {
        Gdx.app.exit();
    }
}
