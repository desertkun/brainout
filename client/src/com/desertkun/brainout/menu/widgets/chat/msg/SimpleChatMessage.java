package com.desertkun.brainout.menu.widgets.chat.msg;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;

public class SimpleChatMessage extends ChatMessage
{
    private final String author;
    private final String text;
    private final Color authorColor;
    private Runnable authorClick;

    public SimpleChatMessage(String text, String author, Color authorColor, String uuid)
    {
        super(uuid);

        this.author = author;
        this.authorColor = authorColor;
        this.text = text;
    }

    public void setAuthorClick(Runnable authorClick)
    {
        this.authorClick = authorClick;
    }

    @Override
    public Table render(boolean full)
    {
        Table row = new Table(BrainOutClient.Skin);

        if (author != null)
        {
            Label authorText = new Label(author + ":", BrainOutClient.Skin, "chat-author");

            authorText.setAlignment(Align.left);
            authorText.setEllipsis(true);
            authorText.setColor(authorColor);

            if (authorClick != null)
            {
                authorText.addListener(new ClickListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        if (getTapCount() >= 2)
                        {
                            authorClick.run();
                        }
                    }
                });
            }

            row.add(authorText).expandX().width(ClientConstants.Menu.Chat.WIDTH - 20).top().padLeft(4).row();
        }

        Label textLabel = new Label(BrainOut.LocalizationMgr.parse(text),
                BrainOutClient.Skin, "chat-text");
        textLabel.setWrap(true);

        row.add(textLabel).minWidth(ClientConstants.Menu.Chat.WIDTH - 20).padLeft(4).expandX().fillX().row();

        return row;
    }
}
