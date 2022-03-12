package com.desertkun.brainout.menu.widgets.chat.msg;

import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class ChatMessage
{
    private final String uuid;

    public ChatMessage(String uuid)
    {
        this.uuid = uuid;
    }

    public abstract Actor render(boolean full);

    public Actor renderPreview(boolean full)
    {
        return render(full);
    }

    public String getUUID()
    {
        return uuid;
    }
}
