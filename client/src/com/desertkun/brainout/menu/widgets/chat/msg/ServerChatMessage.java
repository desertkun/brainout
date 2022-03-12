package com.desertkun.brainout.menu.widgets.chat.msg;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.common.msg.server.ChatMsg;

public class ServerChatMessage extends ChatMessage
{
    private final String key;
    private final String author;
    private final int senderID;
    private final String text;
    private final boolean terminal;
    private final int color;

    public ServerChatMessage(String key, ChatMsg message)
    {
        super(null);

        this.key = key;
        this.author = message.author;
        this.senderID = message.senderID;
        this.text = message.text;
        this.terminal = message.isTerminal();
        this.color = message.color;
    }

    @Override
    public Table render(boolean full)
    {
        Table row = new Table(BrainOutClient.Skin);

        if (senderID != -1)
        {
            String chatGroup = "";

            if (full)
            {
                switch (key)
                {
                    case "team":
                        chatGroup = L.get("MENU_CHAT_TEAM");
                        break;
                }

                if (chatGroup.length() > 0)
                {
                    chatGroup = "[" + chatGroup + "] ";
                }
            }

            Label authorText = new Label(chatGroup + BrainOut.LocalizationMgr.parse(author) + ":",
                    BrainOutClient.Skin, "chat-author");

            authorText.setAlignment(Align.left);
            authorText.setEllipsis(true);

            RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(senderID);

            if (remoteClient != null)
            {
                authorText.setColor(BrainOutClient.ClientController.getColorOf(remoteClient));
            }

            row.add(authorText).expandX().width(ClientConstants.Menu.Chat.WIDTH - 20).top().padLeft(4).row();
        }

        Label textLabel = new Label(BrainOut.LocalizationMgr.parse(text), BrainOutClient.Skin,
            terminal ? "chat-console" : "chat-text");
        textLabel.setWrap(true);
        Color.rgba8888ToColor(textLabel.getColor(), color);

        row.add(textLabel).minWidth(ClientConstants.Menu.Chat.WIDTH - 20).padLeft(4).expandX().fillX().row();

        return row;
    }
    }
