package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.vote.ClientVote;
import com.desertkun.brainout.vote.ClientVotes;
import com.desertkun.brainout.vote.Votes;

public class NewVoteMenu extends Menu
{
    private boolean onTop;

    public NewVoteMenu()
    {
        onTop = true;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Label title = new Label(L.get("MENU_VOTE"), BrainOutClient.Skin, "title-yellow");
            data.add(title).pad(4).row();
        }

        for (ObjectMap.Entry<Votes.ID, ClientVote> entry : ClientVotes.GetVotes())
        {
            ClientVote vote = entry.value;

            TextButton button = new TextButton(vote.getTitle(),
                    BrainOutClient.Skin, "button-default");

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    close();
                }
            });

            data.add(button).size(192, 48).pad(4).row();
        }

        {

            TextButton cancel = new TextButton(L.get("MENU_CANCEL"),
                    BrainOutClient.Skin, "button-default");

            cancel.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    close();
                }
            });

            data.add(cancel).size(192, 48).pad(4).row();
        }


        return data;
    }

    @Override
    public boolean stayOnTop()
    {
        return onTop;
    }

    @Override
    public boolean escape()
    {
        close();
        return true;
    }

    private void close()
    {
        onTop = false;
        pop();
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.bottom;
    }

    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    @Override
    public boolean lockUpdate()
    {
        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
