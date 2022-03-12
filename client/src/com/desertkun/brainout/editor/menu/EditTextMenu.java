package com.desertkun.brainout.editor.menu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.Menu;

public abstract class EditTextMenu extends FormMenu
{
    private final String text;
    private TextArea valueEdit;

    public EditTextMenu(String text)
    {
        this.text = text;
    }

    public abstract void done(String text);

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.bottom;
    }

    @Override
    public Table createUI()
    {
        Table data = super.createUI();

        valueEdit = new TextArea(text, BrainOutClient.Skin, "edit-inspect-no-borders");

        ScrollPane pane = new ScrollPane(valueEdit, BrainOutClient.Skin, "scroll-borders");
        pane.setForceScroll(false, true);
        pane.setFlickScroll(false);
        pane.setOverscroll(false, true);

        setScrollFocus(pane);

        valueEdit.setTextFieldListener((textField, c) ->
        {
            valueEdit.setPrefRows(textField.getText().split("\n").length);
            pane.layout();
        });

        setKeyboardFocus(valueEdit);

        data.add(pane).minSize(800, 512).pad(10).expand().fill().row();

        Table buttons = new Table();

        TextButton save = new TextButton(L.get("MENU_OK"),
                BrainOutClient.Skin, "button-default");

        TextButton cancel = new TextButton(L.get("MENU_CANCEL"),
                BrainOutClient.Skin, "button-default");

        save.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                String text = valueEdit.getText();

                getRoot().getStage().setKeyboardFocus(null);

                done(text);

                pop();
            }
        });

        cancel.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                pop();
            }
        });

        buttons.add(save).pad(2).size(128, 40);
        buttons.add(cancel).pad(2).size(128, 40);

        data.add(buttons).pad(10).row();

        return data;
    }
}
