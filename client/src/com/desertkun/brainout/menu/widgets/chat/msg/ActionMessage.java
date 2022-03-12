package com.desertkun.brainout.menu.widgets.chat.msg;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class ActionMessage extends ChatMessage
{
    private final String text;
    private final Array<Action> actions;

    private class Action
    {
        private String title;
        private String style;
        private ActionCallback callback;
    }

    public interface ActionCallback
    {
        void run(Button button);
    }

    public ActionMessage(String text, String uuid)
    {
        super(uuid);

        this.text = text;
        this.actions = new Array<>();
    }

    public void addIconAction(String icon, ActionCallback callback)
    {
        addAction(null, callback, icon);
    }

    public void addAction(String title, ActionCallback callback)
    {
        addAction(title, callback, "button-default");
    }

    public void addAction(String title, ActionCallback callback, String style)
    {
        Action action = new Action();

        action.title = title;
        action.style = style;
        action.callback = callback;

        this.actions.add(action);
    }

    @Override
    public Actor renderPreview(boolean full)
    {
        Label textLabel = new Label(BrainOut.LocalizationMgr.parse(text),
                BrainOutClient.Skin, "title-gray");
        textLabel.setWrap(true);

        Container<Label> labelContainer = new Container<>(textLabel);

        labelContainer.prefWidth(ClientConstants.Menu.Chat.WIDTH - 20).pad(4);

        return labelContainer;
    }

    @Override
    public Table render(boolean full)
    {
        Table row = new Table(BrainOutClient.Skin);
        row.setBackground("form-border-red");

        Label textLabel = new Label(BrainOut.LocalizationMgr.parse(text),
                BrainOutClient.Skin, "title-gray");
        textLabel.setWrap(true);

        row.add(textLabel).pad(4).expandX().fillX().row();

        Table actions = new Table();

        for (Action action : this.actions)
        {
            Button btn;

            if (action.title != null)
            {
                btn = new TextButton(action.title, BrainOutClient.Skin, action.style);
            }
            else
            {
                btn = new ImageButton(BrainOutClient.Skin, action.style);
            }

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.select);

                    action.callback.run(btn);
                }
            });

            actions.add(btn).minWidth(96).pad(4);
        }

        row.add(actions).width(ClientConstants.Menu.Chat.WIDTH - 30).fillX().row();

        return row;
    }
}
