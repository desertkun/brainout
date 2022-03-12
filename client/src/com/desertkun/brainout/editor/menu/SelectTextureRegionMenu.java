package com.desertkun.brainout.editor.menu;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class SelectTextureRegionMenu extends FormMenu
{
    private final Select onSelect;
    private Table content;
    private String filter;

    public interface Select
    {
        void selected(String region);
        void cancelled();
    }

    public SelectTextureRegionMenu(Select onSelect)
    {
        this.onSelect = onSelect;
    }

    @Override
    public Table createUI()
    {
        Table data = super.createUI();

        this.content = new Table();

        final TextField filerField = new TextField("", BrainOutClient.Skin, "edit-default");

        filerField.setTextFieldListener(new TextField.TextFieldListener()
        {
            @Override
            public void keyTyped(TextField textField, char c)
            {
                filter = filerField.getText().toLowerCase();

                updateContent();
            }
        });

        setKeyboardFocus(filerField);

        data.add(filerField).pad(20).expandX().fillX().row();

        ScrollPane contentPane = new ScrollPane(content);
        contentPane.setFadeScrollBars(false);

        updateContent();
        setScrollFocus(contentPane);

        data.add(contentPane).pad(20).minWidth(320).minHeight(320).row();


        Table buttons = new Table();
        data.add(buttons).expandX().row();

        TextButton cancel = new TextButton(L.get("MENU_CANCEL"), BrainOutClient.Skin, "button-default");

        cancel.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                onSelect.cancelled();

                close();
            }
        });

        buttons.add(cancel).width(128).pad(20).padTop(0);

        return data;
    }

    private void updateContent()
    {
        content.clear();

        ObjectMap<String, TextureRegion> regions = BrainOutClient.Skin.getAll(TextureRegion.class);

        int i = 0;
        for (final ObjectMap.Entry<String, TextureRegion> region: regions)
        {
            final String id = region.key;

            if (filter != null && !region.key.contains(filter))
            {
                continue;
            }

            TextButton btn = new TextButton("", BrainOutClient.Skin, "button-default");

            Image image = new Image(region.value);
            image.setScaling(Scaling.fit);
            image.setBounds(4, 4, 64, 64);

            btn.addActor(image);

            content.add(btn).pad(4).size(72, 72);

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    onSelect.selected(id);
                    close();

                }
            });

            i++;
            if (i >= 6)
            {
                i = 0;
                content.row();
            }
        }
    }

    private void close()
    {
        pop();
    }
}
