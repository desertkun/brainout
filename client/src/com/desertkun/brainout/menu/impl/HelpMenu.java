package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.RichLabel;
import com.desertkun.brainout.utils.Pair;

public class HelpMenu extends Menu
{
    public Array<Pair<String, String>> items = new Array<>();
    private int currentItem = 0;
    private Table content;

    public HelpMenu()
    {
        addItem("MENU_TUTORIAL_1_TITLE", "MENU_TUTORIAL_1_DESC");
        addItem("MENU_TUTORIAL_2_TITLE", "MENU_TUTORIAL_2_DESC");
        addItem("MENU_TUTORIAL_3_TITLE", "MENU_TUTORIAL_3_DESC");
        addItem("MENU_TUTORIAL_4_TITLE", "MENU_TUTORIAL_4_DESC");
    }

    private void addItem(String title, String description)
    {
        items.add(new Pair<>(title, description));
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public boolean lockRender()
    {
        return true;
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

    @Override
    public Table createUI()
    {
        Table data = new Table();

        content = new Table();

        data.add(content).expandX().fillX().row();

        renderItem();

        TextButton next = new TextButton(L.get("MENU_NEXT") + " 1 / " + items.size,
            BrainOutClient.Skin, "button-default");

        next.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                currentItem++;

                if (currentItem >= items.size)
                {
                    pop();
                }
                else
                {
                    if (currentItem == items.size - 1)
                    {
                        next.setText(L.get("MENU_FINISH"));
                    }
                    else
                    {
                        next.setText(L.get("MENU_NEXT") + " " +
                            (currentItem + 1) + " / " + items.size);
                    }

                    renderItem();
                }
            }
        });

        data.add(next).size(192, 64).pad(16).row();

        return data;
    }

    private void renderItem()
    {
        content.clear();

        Pair<String, String> item = items.get(currentItem);

        {
            final Label text = new Label(L.get(item.first),
                    BrainOutClient.Skin, "title-yellow");
            text.setAlignment(Align.center);
            content.add(text).pad(16).center().expandX().fill().row();
        }

        {
            final RichLabel text = new RichLabel(L.get(item.second),
                    BrainOutClient.Skin, "title-medium");
            content.add(text).pad(16).center().expand().fill().row();
        }
    }
}
