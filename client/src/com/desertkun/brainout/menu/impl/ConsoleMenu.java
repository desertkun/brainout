package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.events.ChatEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.menu.Menu;


public class ConsoleMenu extends Menu implements EventReceiver
{
    private TextField sendTextField;
    private Table history;
    private ScrollPane pane;
    private boolean done;

    public ConsoleMenu()
    {
        done = false;
    }

    private void done()
    {
        done = true;
    }

    @Override
    public boolean stayOnTop()
    {
        return !done;
    }

    @Override
    public boolean escape()
    {
        done();
        pop();
        return true;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        history = new Table();
        history.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("edit-console")));
        history.align(Align.top);

        pane = new ScrollPane(history, BrainOutClient.Skin, "scroll-default");
        pane.setCancelTouchFocus(false);
        pane.setOverscroll(false, false);

        data.add(pane).height(ClientConstants.Menu.Console.TERMINAL_HEIGHT).expandX().fillX().row();

        sendTextField = new TextField("", BrainOutClient.Skin, "edit-console");
        sendTextField.setAlignment(Align.left);
        data.add(sendTextField).padTop(1).height(ClientConstants.Menu.Console.SEND_HEIGHT).expandX().fillX();

        sendTextField.setTextFieldListener((textField, c) ->
        {
            if (c == 13 || c == 10)
            {
                executeConsole();
            }
        });

        return data;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        setKeyboardFocus(sendTextField);

        for (String s : BrainOutClient.ClientController.getTerminal())
        {
            addTerminalMessage(s);
        }

        scrollToBottom();

        BrainOut.EventMgr.subscribe(Event.ID.chat, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.chat, this);
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fillTop;
    }
    @Override
    public boolean keyUp(int keyCode)
    {
        return true;
    }

    private void executeConsole()
    {
        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
        if (csGame != null)
        {
            csGame.executeConsole(sendTextField.getText());
        }
        done();
        pop();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case chat:
            {
                ChatEvent chatEvent = (ChatEvent) event;
                if (chatEvent.isTerminal())
                {
                    addTerminalMessage(chatEvent.message.text);
                    scrollToBottom();

                    return true;
                }
            }
        }

        return false;
    }

    private void addTerminalMessage(String text)
    {
        TextArea msg = new TextArea(text, BrainOutClient.Skin, "edit-console-message");
        msg.setPrefRows(text.split("\n").length);
        history.add(msg).padBottom(4).expandX().fillX().row();
    }

    private void scrollToBottom()
    {
        pane.setScrollPercentY(1);
        pane.layout();
    }
}
