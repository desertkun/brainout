package com.desertkun.brainout.menu.widgets.chat;

import com.desertkun.brainout.L;

public class InGameChatWidget extends ChatWidget
{
    public InGameChatWidget(float x, float y, float w, float h)
    {
        super(x, y, w, h);
    }

    @Override
    public void init()
    {
        super.init();

        addTab(L.get("MENU_CHAT_SERVER"), "server").defSize();
        addTab(L.get("MENU_CHAT_TEAM"), "team").size(160, 32);
    }

    @Override
    protected void onMessageSent()
    {
        closeChatFromReply();
    }
}
