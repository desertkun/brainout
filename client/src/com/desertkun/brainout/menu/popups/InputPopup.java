package com.desertkun.brainout.menu.popups;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Popup;


public class InputPopup extends Popup
{
    private String value;
    protected TextField valueEdit;

    public InputPopup(String text, String value)
    {
        super(text);

        init(value);
    }

    private void init(String value)
    {
        this.value = value;
    }

    @Override
    protected void initContent(Table data)
    {
        super.initContent(data);
        valueEdit = newEdit(value);

        Cell cell = data.add(valueEdit).pad(20).fillX().expandX().width(getInputWidth()).height(getInputHeight());
        initInputCell(cell);
        cell.row();

        setKeyboardFocus(valueEdit);
    }

    protected TextField newEdit(String value)
    {
        return new TextField(value, BrainOutClient.Skin, "edit-default");
    }

    protected void initInputCell(Cell cell)
    {
        //
    }

    protected float getInputWidth()
    {
        return 300;
    }

    protected float getInputHeight() { return 35; }

    public String getValue()
    {
        return valueEdit.getText().toUpperCase();
    }
}
