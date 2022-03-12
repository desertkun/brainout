package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.msg.server.DuelCompletedMsg;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.RichLabel;

public class DuelCompleteMenu extends Menu
{
    private final int loser;
    private final int reward;
    private final Runnable done;
    private final int myDeaths;
    private final int enemyDeaths;
    private final int deathsRequired;
    private final boolean canTryAgain;
    private final Runnable tryAgain;

    public DuelCompleteMenu(DuelCompletedMsg msg, int myDeaths, int enemyDeaths, int deathsRequired,
                            Runnable done, Runnable tryAgain)
    {
        this.myDeaths = myDeaths;
        this.enemyDeaths = enemyDeaths;
        this.deathsRequired = deathsRequired;
        this.loser = msg.loser;
        this.reward = msg.reward;
        this.canTryAgain = msg.tryAgain;
        this.done = done;
        this.tryAgain = tryAgain;
    }

    private Table renderDeaths(int deaths, String style)
    {
        Table icons = new Table();

        int alive = deathsRequired - deaths;

        for (int i = 0; i < alive; i++)
        {
            icons.add(new BorderActor(new Image(BrainOutClient.Skin, style)));
        }
        for (int i = 0; i < deaths; i++)
        {
            icons.add(new BorderActor(new Image(BrainOutClient.Skin, "assault-icon-dead")));
        }

        return icons;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("MENU_DUEL_COMPLETED"), BrainOutClient.Skin, "title-yellow");

            header.add(title).pad(8).expandX().row();
            data.add(header).expandX().fillX().row();
        }

        Table body = new Table(BrainOutClient.Skin);
        body.setBackground("form-default");

        {
            String text;
            String style;

            if (loser == BrainOutClient.ClientController.getMyId())
            {
                text = L.get("MENU_DUEL_LOST");
                style = "title-red";
            }
            else
            {
                text = L.get("MENU_DUEL_WON");
                style = "title-green";
            }

            Label title = new Label(text, BrainOutClient.Skin, style);
            body.add(title).pad(16).padLeft(64).padRight(64).row();
        }

        {
            Table root = new Table();
            root.add(renderDeaths(myDeaths, "assault-icon-friend"));
            root.add().width(64).center();
            root.add(renderDeaths(enemyDeaths, "assault-icon-enemy"));

            body.add(root).pad(16).row();
        }

        {
            String text;
            String style;

            boolean lost = loser == BrainOutClient.ClientController.getMyId();

            if (lost)
            {
                text = "- " + reward + " RU";
                style = "title-red";
            }
            else
            {
                text = "";
                style = "title-green";
            }

            Label title = new Label(text, BrainOutClient.Skin, style);

            if (!lost)
            {
                final float[] cnt = new float[1];

                title.addAction(Actions.repeat(20, Actions.sequence(
                    Actions.run(() ->
                    {
                        cnt[0]++;
                        int value = (int)(float)((cnt[0] / 20) * reward);
                        title.setText("+ " + value + " RU");
                        Menu.playSound(MenuSound.character);
                    }),
                    Actions.delay(0.05f)
                )));
            }

            body.add(title).pad(16).padLeft(64).padRight(64).row();
        }

        Table buttons = new Table();

        if (canTryAgain)
        {
            TextButton exit = new TextButton(L.get("MENU_NEXT_DUEL"), BrainOutClient.Skin, "button-green");

            exit.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    DuelCompleteMenu.this.tryAgain();
                }
            });

            buttons.add(exit).expandX().uniformX().fillX().height(64);
        }

        {
            TextButton exit = new TextButton(L.get("MENU_EXIT"), BrainOutClient.Skin, "button-default");

            exit.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    DuelCompleteMenu.this.exit();
                }
            });

            buttons.add(exit).expandX().uniformX().fillX().height(64);
        }

        data.add(body).width(600).expand().fill().row();
        data.add(buttons).expandX().fillX().row();

        return data;
    }

    private void tryAgain()
    {
        tryAgain.run();
    }

    private void exit()
    {
        //pop();
        done.run();
    }
}
