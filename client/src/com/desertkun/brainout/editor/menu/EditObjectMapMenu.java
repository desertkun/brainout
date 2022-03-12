package com.desertkun.brainout.editor.menu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.popups.OKInputPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class EditObjectMapMenu extends FormMenu
{
    private final ObjectMap<String, String> items;
    private final Runnable onSaved;
    private Table propertiesData;

    public EditObjectMapMenu(ObjectMap<String, String> items, Runnable onSaved)
    {
        this.items = items;
        this.onSaved = onSaved;
    }

    private void addEntry(final String name, String value)
    {
        final Label text = new Label(name + ":", BrainOutClient.Skin, "title-small");
        text.setAlignment(Align.right);

        propertiesData.add(text).padRight(4).padBottom(4).width(200);

        TextField asTextField = new TextField(value,
                BrainOutClient.Skin, "edit-default");

        asTextField.setTextFieldListener((textField, c) -> items.put(name, textField.getText()));

        propertiesData.add(asTextField).padBottom(4).width(300);

        ImageButton img = new ImageButton(BrainOutClient.Skin, "button-editor-remove");

        img.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                items.remove(name);

                resetEntries();
            }
        });

        propertiesData.add(img).padBottom(4).row();
    }

    @Override
    public boolean lockRender()
    {
        return true;
    }

    @Override
    public Table createUI()
    {
        Table data = super.createUI();

        this.propertiesData = new Table();

        ScrollPane propertiesPane = new ScrollPane(propertiesData);

        resetEntries();

        data.add(propertiesPane).pad(20).expand().fill().row();

        Table buttons = new Table();
        data.add(buttons).expandX().row();


        TextButton add = new TextButton("[NEW]", BrainOutClient.Skin, "button-default");

        add.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                pushMenu(new OKInputPopup("Name", "")
                {
                    @Override
                    public void ok()
                    {
                        if (!getValue().isEmpty())
                        {
                            items.put(getValue(), "");

                            resetEntries();
                        }
                    }
                });
            }
        });


        buttons.add(add).width(128).pad(0, 20, 20, 0);


        TextButton ok = new TextButton(L.get("MENU_OK"), BrainOutClient.Skin, "button-default");

        ok.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                save();
                close();
            }
        });

        buttons.add(ok).width(128).pad(0, 10, 20, 0);

        TextButton cancel = new TextButton(L.get("MENU_CANCEL"), BrainOutClient.Skin, "button-default");

        cancel.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                close();
            }
        });

        buttons.add(cancel).width(128).pad(0, 10, 20, 20);

        return data;
    }

    private void resetEntries()
    {
        propertiesData.clear();

        for (ObjectMap.Entry<String, String> entry: items)
        {
            addEntry(entry.key, entry.value);
        }
    }

    private void close()
    {
        pop();
    }

    private void save()
    {
        onSaved.run();
    }
}
