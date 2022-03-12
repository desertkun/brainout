package com.desertkun.brainout.menu.impl;

import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.popups.AlertPopup;

class NewOrderResultPopup extends AlertPopup
{
    public NewOrderResultPopup(String text)
    {
        super(text);
    }

    @Override
    public boolean popIfFocusOut()
    {
        return true;
    }

    @Override
    public String getTitle()
    {
        return L.get("MENU_STORE_TITLE");
    }
}
