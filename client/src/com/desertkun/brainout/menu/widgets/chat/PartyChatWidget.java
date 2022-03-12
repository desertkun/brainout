package com.desertkun.brainout.menu.widgets.chat;

import com.desertkun.brainout.L;

public class PartyChatWidget extends ChatWidget
{
    public PartyChatWidget(float x, float y, float w, float h)
    {
        super(x, y, w, h);
    }

    @Override
    public void init()
    {
        super.init();

        addTab(L.get("MENU_CHAT_PARTY"), "server").defSize();
    }
}
