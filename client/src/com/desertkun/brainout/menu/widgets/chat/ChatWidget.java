package com.desertkun.brainout.menu.widgets.chat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.events.ChatEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.OpenChatEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tabs;
import com.desertkun.brainout.menu.widgets.chat.msg.ChatMessage;
import com.desertkun.brainout.menu.widgets.chat.msg.ServerChatMessage;

public class ChatWidget extends com.desertkun.brainout.menu.widgets.Widget
{
    private TextField sendText;
    private WidgetGroup root;
    private VerticalGroup viewData;
    private ScrollPane viewPane;
    private Table replyRoot;
    private Tabs reply;
    private Mode mode;
    private ObjectMap<String, Actor> messages;

    private enum Mode
    {
        view,
        reply
    }

    public ChatWidget(float x, float y, float w, float h)
    {
        super(x, y, w, h);

        messages = new ObjectMap<>();
    }

    public void removeTab(Object key)
    {

    }

    public ChatTab addTab(String title, Object key)
    {
        ChatTab tab = new ChatTab(key.toString());
        reply.addTab(title, key, tab);
        return tab;
    }

    public class ChatTab extends Tabs.Tab
    {
        private VerticalGroup contents;
        private ScrollPane pane;

        public ChatTab(String name)
        {
            contents = new VerticalGroup()
            {
                @Override
                public void layout()
                {
                    super.layout();

                    pane.setScrollPercentY(1);
                }
            };

            setName(name);

            contents.align(Align.top);
            contents.setTouchable(Touchable.enabled);

            pane = new ScrollPane(contents, BrainOutClient.Skin, "scroll-default");

            add(pane).expand().fill().row();
        }

        public ScrollPane getPane()
        {
            return pane;
        }

        public VerticalGroup getContents()
        {
            return contents;
        }

        @Override
        protected void selected()
        {
            super.selected();

            tabChanged(this);
        }
    }

    protected void tabChanged(ChatTab chatTab)
    {

    }

    @Override
    public void init()
    {
        mode = Mode.view;

        root = new WidgetGroup();
        root.setFillParent(true);
        root.setTouchable(Touchable.disabled);

        viewData = new VerticalGroup()
        {
            @Override
            public void layout()
            {
                super.layout();

                viewPane.setScrollPercentY(1);
            }
        };

        viewData.align(Align.top);

        viewPane = new ScrollPane(viewData, BrainOutClient.Skin, "scroll-default");
        viewPane.setFillParent(true);
        root.addActor(viewPane);

        replyRoot = new Table();
        replyRoot.setFillParent(true);
        replyRoot.setVisible(false);
        root.addActor(replyRoot);

        reply = new Tabs(BrainOutClient.Skin)
        {
            @Override
            protected String getContentBackground()
            {
                return "chat-text-shape";
            }

            @Override
            protected String getTabButtonStyle()
            {
                return "button-tab";
            }
        };

        replyRoot.add(reply).colspan(2).expand().fill().row();

        sendText = new TextField("", BrainOutClient.Skin, "edit-chat");

        sendText.setTextFieldListener((textField, c) ->
        {
            if (c == 13 || c == 10)
            {
                doSendChat();
            }
        });

        sendText.addListener(new InputListener()
        {
            @Override
            public boolean keyDown(InputEvent event, int keycode)
            {
                if (keycode == Input.Keys.ESCAPE && getStage().getKeyboardFocus() == sendText)
                {
                    doCancelSendChat();
                }

                return false;
            }
        });

        replyRoot.add(sendText).expandX().fillX();

        ImageButton sendButton = new ImageButton(BrainOutClient.Skin, "button-chat-send");

        sendButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                doSendChat();
            }
        });

        replyRoot.add(sendButton).size(32, 28).row();

        root.setColor(1, 1, 1, 0);
        root.setVisible(false);

        add(root).expand().fill().row();

        BrainOut.EventMgr.subscribeAt(Event.ID.chat, this, true);
        BrainOut.EventMgr.subscribeAt(Event.ID.openChat, this, true);
        BrainOut.EventMgr.subscribeAt(Event.ID.gameController, this, true);

        Gdx.app.postRunnable(() ->
        {
            TextButton closeButton = new TextButton("x", BrainOutClient.Skin, "button-danger");

            closeButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.back);

                    closeChatFromReply();
                }
            });

            reply.addFakeActor(closeButton).expandX().size(32).right();
        });
    }

    private void doCancelSendChat()
    {
        Gdx.app.postRunnable(() ->
        {
            sendText.setText("");
            getStage().setKeyboardFocus(null);
            closeChatFromReply();
        });
    }

    private void doSendChat()
    {
        String key = reply.getCurrentTab() != null ? reply.getCurrentTab().getKey().toString() : "server";

        Gdx.app.postRunnable(() ->
        {
            if (!sendText.getText().isEmpty())
            {
                BrainOutClient.ClientController.sendChat(sendText.getText(), key);
                sendText.setText("");
            }

            onMessageSent();
        });
    }

    protected void onMessageSent()
    {
    }

    @Override
    public void release()
    {
        BrainOut.EventMgr.unsubscribe(Event.ID.chat, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.openChat, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.gameController, this);
    }

    public void openChatForReply(String tab)
    {
        viewPane.setVisible(false);

        if (tab != null)
        {
            reply.selectTab(tab);
        }

        replyRoot.setVisible(true);

        root.clearActions();
        root.setVisible(true);
        root.setColor(1, 1, 1, 1);
        root.setTouchable(Touchable.enabled);

        getStage().setKeyboardFocus(sendText);

        mode = Mode.reply;
    }

    protected void closeChatFromReply()
    {
        getStage().setKeyboardFocus(null);

        root.setVisible(false);
        root.setColor(1, 1, 1, 0);

        viewPane.setVisible(true);
        replyRoot.setVisible(false);

        root.clearActions();
        root.setTouchable(Touchable.disabled);

        mode = Mode.view;
    }

    public void showChatForeShortView()
    {
        root.clearActions();
        root.setVisible(true);

        root.addAction(Actions.sequence(
                Actions.alpha(1, ClientConstants.Menu.Chat.APPEARING_TIME),
                Actions.delay(ClientConstants.Menu.Chat.SHOW_DELAY),
                Actions.run(this::hideChat)));
    }

    public void hideChat()
    {
        root.clearActions();
        root.addAction(Actions.sequence(
            Actions.alpha(0, ClientConstants.Menu.Chat.APPEARING_TIME),
            Actions.run(this::doHideChat))
        );
    }

    private void doHideChat()
    {
        root.setVisible(false);
        viewData.clear();
    }

    public void addChatMessage(Object key, ChatMessage message, boolean notify)
    {
        if (notify)
        {
            switch (mode)
            {
                case view:
                {
                    if (validateView(key))
                    {
                        viewData.addActor(message.renderPreview(true));
                    }

                    showChatForeShortView();

                    break;
                }
            }
        }

        ChatTab tab = ((ChatTab) reply.findTab(key));

        if (tab != null)
        {
            Actor rendered = message.render(false);
            tab.getContents().addActor(rendered);

            String uuid = message.getUUID();

            if (uuid != null)
            {
                messages.put(uuid, rendered);
            }
        }
    }

    protected boolean validateView(Object key)
    {
        return true;
    }

    public void removeMessage(String uuid)
    {
        Actor actor = messages.remove(uuid);

        if (actor != null)
        {
            actor.remove();
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case chat:
            {
                ChatEvent chatEvent = (ChatEvent) event;

                if (chatEvent.message == null)
                    return false;

                addChatMessage(chatEvent.message.key, new ServerChatMessage(chatEvent.message.key, chatEvent.message), true);

                return true;
            }
            case openChat:
            {
                OpenChatEvent chatEvent = (OpenChatEvent) event;

                openChatForReply(chatEvent.key);

                return true;
            }
            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case openChat:
                    {
                        openPrimaryChat();

                        return true;
                    }

                    case openTeamChat:
                    {
                        openSecondaryChat();

                        return true;
                    }
                }

                break;
            }
        }

        return false;
    }

    private void openSecondaryChat()
    {
        if (BrainOutClient.ClientController.isLobby())
        {
            openChatForReply("clan");
        }
        else
        {
            openChatForReply("team");
        }
    }

    private void openPrimaryChat()
    {
        openChatForReply("server");
    }

    public static void Open(String key)
    {
        BrainOutClient.EventMgr.sendDelayedEvent(OpenChatEvent.obtain(key));
    }

    public static void Open()
    {
        Open(null);
    }

    protected void deleteMessage(String messageId)
    {
        Actor msg = messages.remove(messageId);

        if (msg != null)
        {
            msg.remove();
        }
    }

    public boolean isOpened()
    {
        return mode == Mode.reply;
    }

    public String getCurrentTab()
    {
        if (reply.getCurrentTab() == null)
            return null;

        return reply.getCurrentTab().getName();
    }
}
