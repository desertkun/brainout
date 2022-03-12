package com.desertkun.brainout.menu.widgets.chat.msg;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;

public class StatusMessage extends ChatMessage
{
    private final String text;
    private final String style;

    public StatusMessage(String text, String uuid)
    {
        this(text, uuid, "title-gray");
    }

    public StatusMessage(String text, String uuid, String style)
    {
        super(uuid);

        this.text = text;
        this.style = style;
    }

    @Override
    public Actor render(boolean full)
    {
        Label textLabel = new Label(BrainOut.LocalizationMgr.parse(text),
                BrainOutClient.Skin, style);
        textLabel.setWrap(true);

        Container<Label> labelContainer = new Container<>(textLabel);

        labelContainer.prefWidth(ClientConstants.Menu.Chat.WIDTH - 20).pad(4);

        return labelContainer;
    }
}
