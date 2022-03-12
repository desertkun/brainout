package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.popups.YesNoInputPopup;
import com.desertkun.brainout.utils.RoomIDEncryption;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RoomIDMenu extends YesNoInputPopup
{
    private static Pattern ROOM_ID_PATTERN = Pattern.compile("([A-Z0-9-]+)");

    private boolean onTop;

    public RoomIDMenu()
    {
        super(L.get("MENU_ENTER_ROOM_ID"), detectPromo());

        onTop = true;
    }

    @Override
    protected TextField newEdit(String value)
    {
        return new TextField(value, BrainOutClient.Skin, "edit-focused");
    }

    @Override
    protected void initContent(Table data)
    {
        super.initContent(data);

        valueEdit.setAlignment(Align.center);
    }

    @Override
    public String getTitle()
    {
        return L.get("MENU_JOIN_BY_ROOM_ID");
    }

    @Override
    protected String getYesButtonText()
    {
        return L.get("MENU_JOIN");
    }

    @Override
    protected String getNoButtonText()
    {
        return L.get("MENU_CANCEL");
    }

    @Override
    public boolean stayOnTop()
    {
        return onTop;
    }

    abstract void join(String roomId);
    abstract void error();

    private static String detectPromo()
    {
        String clip = Gdx.app.getClipboard().getContents();

        if (clip != null)
        {
            Matcher matcher = ROOM_ID_PATTERN.matcher(clip);
            if (matcher.matches())
            {
                return matcher.group(1);
            }
        }

        return "";
    }

    private void validate(String encoded)
    {
        String roomId = RoomIDEncryption.DecryptHumanReadable(encoded);

        if (roomId == null)
        {
            error();
            return;
        }

        join(roomId);
    }

    @Override
    public void pop()
    {
        onTop = false;

        super.pop();
    }

    @Override
    public void ok()
    {
        String value = getValue();

        if (value == null || value.equals(""))
            return;

        Matcher matcher = ROOM_ID_PATTERN.matcher(value);
        if (matcher.matches())
        {
            validate(matcher.group(1));
        }
        else
        {
            error();
        }
    }

    @Override
    protected float getInputWidth()
    {
        return 240;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }
}
