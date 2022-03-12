package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class SelectContentMenu extends FormMenu
{
    private final Content current;
    private final Array<ConsumableRecord> available;
    private final OnSelect onSelect;
    private Table content;
    private ButtonGroup<TextButton> items;

    @Override
    public boolean popIfFocusOut()
    {
        return true;
    }

    public interface OnSelect
    {
        void selected(ConsumableRecord content);
    }

    public SelectContentMenu(Content current, Array<ConsumableRecord> available, OnSelect onSelect)
    {
        this.current = current;
        this.available = available;
        this.onSelect = onSelect;
    }

    @Override
    public Table createUI()
    {
        Table data = super.createUI();

        this.content = new Table();
        this.items = new ButtonGroup<TextButton>();

        updateContent();

        data.add(content).pad(16).row();


        TextButton cancel = new TextButton(L.get("MENU_CANCEL"),
                BrainOutClient.Skin, "button-default");

        cancel.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                pop();
            }
        });

        data.add(cancel).size(150, 32).pad(16).row();


        return data;
    }

    private void updateContent()
    {
        content.clear();
        items.clear();

        for (final ConsumableRecord c: available)
        {
            Content content = c.getItem().getContent();

            TextButton textButton = new TextButton(content.getTitle().get(), BrainOutClient.Skin, "button-default");

            if (content == current)
            {
                textButton.setChecked(true);
            }

            textButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    onSelect.selected(c);

                    pop();
                }
            });

            Table i = new Table();

            if (content.hasComponent(IconComponent.class))
            {
                IconComponent iconComponent = content.getComponent(IconComponent.class);

                Image image = new Image(iconComponent.getIcon());

                i.add(image).padRight(4);
            }

            items.add(textButton);

            i.add(textButton).size(128, 32).pad(2).expandX().fillX().row();

            this.content.add(i).expandX().fill().row();
        }
    }
}
