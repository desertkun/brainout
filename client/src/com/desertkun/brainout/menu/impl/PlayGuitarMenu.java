package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class PlayGuitarMenu extends Menu
{
    private static IntMap<String> keys = new IntMap<>();
    private static IntMap<String> shiftKeys = new IntMap<>();
    private boolean playing;

    static
    {
        keys.put(Input.Keys.NUM_1, "c2");
        shiftKeys.put(Input.Keys.NUM_1, "db2");
        keys.put(Input.Keys.NUM_2, "d2");
        shiftKeys.put(Input.Keys.NUM_2, "eb2");
        shiftKeys.put(Input.Keys.AT, "eb2");
        keys.put(Input.Keys.NUM_3, "e2");
        keys.put(Input.Keys.NUM_4, "f2");
        shiftKeys.put(Input.Keys.NUM_4, "gb2");
        keys.put(Input.Keys.NUM_5, "g2");
        shiftKeys.put(Input.Keys.NUM_5, "ab2");
        keys.put(Input.Keys.NUM_6, "a2");
        shiftKeys.put(Input.Keys.NUM_6, "bb2");
        keys.put(Input.Keys.NUM_7, "b2");
        keys.put(Input.Keys.NUM_8, "c3");
        shiftKeys.put(Input.Keys.NUM_8, "db3");
        keys.put(Input.Keys.NUM_9, "d3");
        shiftKeys.put(Input.Keys.NUM_9, "eb3");
        keys.put(Input.Keys.NUM_0, "e3");

        keys.put(Input.Keys.Q, "f3");
        shiftKeys.put(Input.Keys.Q, "gb3");
        keys.put(Input.Keys.W, "g3");
        shiftKeys.put(Input.Keys.W, "ab3");
        keys.put(Input.Keys.E, "a3");
        shiftKeys.put(Input.Keys.E, "bb3");
        keys.put(Input.Keys.R, "b3");
        keys.put(Input.Keys.T, "c4");
        shiftKeys.put(Input.Keys.T, "db4");
        keys.put(Input.Keys.Y, "d4");
        shiftKeys.put(Input.Keys.Y, "eb4");
        keys.put(Input.Keys.U, "e4");
        keys.put(Input.Keys.I, "f4");
        shiftKeys.put(Input.Keys.I, "gb4");
        keys.put(Input.Keys.O, "g4");
        shiftKeys.put(Input.Keys.O, "ab4");
        keys.put(Input.Keys.P, "a4");
        shiftKeys.put(Input.Keys.P, "bb4");

        keys.put(Input.Keys.A, "b4");
        keys.put(Input.Keys.S, "c5");
        shiftKeys.put(Input.Keys.S, "db5");
        keys.put(Input.Keys.D, "d5");
        shiftKeys.put(Input.Keys.D, "eb5");
        keys.put(Input.Keys.F, "e5");
        keys.put(Input.Keys.G, "f5");
        shiftKeys.put(Input.Keys.G, "gb5");
        keys.put(Input.Keys.H, "g5");
        shiftKeys.put(Input.Keys.H, "ab5");
        keys.put(Input.Keys.J, "a5");
        shiftKeys.put(Input.Keys.J, "bb5");
        keys.put(Input.Keys.K, "b5");
        keys.put(Input.Keys.L, "c6");
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        Image piano = new Image(BrainOutClient.Skin, "guitar-piano");
        data.add(piano).padBottom(32).row();

        TextButton paste = new TextButton(L.get("MENU_PASTE"), BrainOutClient.Skin, "button-text-clear");
        paste.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                super.clicked(event, x, y);

                if (playing)
                {
                    playing = false;
                    paste.setText(L.get("MENU_PASTE"));
                    paste.clearActions();
                }
                else
                {
                    playing = true;
                    paste.setText(L.get("MENU_CANCEL"));
                    SequenceAction seq = Actions.sequence();
                    playFromClipboard(seq);
                    seq.addAction(Actions.run(() ->
                    {
                        paste.setText(L.get("MENU_PASTE"));
                        playing = false;
                    }));
                    paste.addAction(seq);
                }
            }
        });

        data.add(paste).size(192, 64).padBottom(32).row();

        return data;
    }

    private Action playCharacter(char c)
    {
        int key;
        boolean shift;

        if (c <= '9' && c >= '0')
        {
            shift = false;
            key = Input.Keys.NUM_0 + (Character.getNumericValue(c) - Character.getNumericValue('0'));
        }
        else if (c <= 'z' && c >= 'a')
        {
            shift = false;
            key = Input.Keys.A + (Character.getNumericValue(c) - Character.getNumericValue('a'));
        }
        else if (c <= 'Z' && c >= 'A')
        {
            shift = true;
            key = Input.Keys.A + (Character.getNumericValue(c) - Character.getNumericValue('A'));
        }
        else
        {
            switch (c)
            {
                case '!':
                {
                    shift = true;
                    key = Input.Keys.NUM_1;
                    break;
                }
                case '@':
                {
                    shift = true;
                    key = Input.Keys.NUM_2;
                    break;
                }
                case '$':
                {
                    shift = true;
                    key = Input.Keys.NUM_4;
                    break;
                }
                case '%':
                {
                    shift = true;
                    key = Input.Keys.NUM_5;
                    break;
                }
                case '^':
                {
                    shift = true;
                    key = Input.Keys.NUM_6;
                    break;
                }
                case '*':
                {
                    shift = true;
                    key = Input.Keys.NUM_8;
                    break;
                }
                case '(':
                {
                    shift = true;
                    key = Input.Keys.NUM_9;
                    break;
                }
                default:
                {
                    return null;
                }
            }
        }

        if (shift)
        {
            String v = shiftKeys.get(key);
            if (v != null)
            {
                return Actions.run(() ->
                    BrainOutClient.EventMgr.sendDelayedEvent(
                        GameControllerEvent.obtain(GameControllerEvent.Action.custom, v)));
            }
        }
        else
        {
            String v = keys.get(key);
            if (v != null)
            {
                return Actions.run(() ->
                    BrainOutClient.EventMgr.sendDelayedEvent(
                        GameControllerEvent.obtain(GameControllerEvent.Action.custom, v)));
            }
        }

        return null;
    }

    private void playFromClipboard(SequenceAction seq)
    {
        String buffer = Gdx.app.getClipboard().getContents();

        boolean group = false;
        boolean fast = false;
        CharArray groupArray = new CharArray();

        float delay = 0.16f;
        float d = 0;
        float da = 0;
        char prev = 0;
        char c = 0;

        int i = 0;
        while (i < buffer.length())
        {
            prev = c;
            c = buffer.charAt(i++);

            switch (c)
            {
                case '\n':
                {
                    if (prev == ' ')
                    {
                        continue;
                    }
                }
                case ' ':
                {
                    if (group)
                    {
                        fast = true;
                    }
                    else
                    {
                        da += delay;
                        d += da;
                    }
                    continue;
                }
                case '[':
                case '{':
                {
                    if (group)
                    {
                        if (d > 0)
                        {
                            seq.addAction(Actions.delay(d));
                            d = 0;
                            da = 0;
                        }
                        playSequence(seq, groupArray, fast);
                        seq.addAction(Actions.delay(delay));
                    }
                    group = true;
                    fast = false;
                    groupArray.clear();
                    continue;
                }
                case ']':
                case '}':
                {
                    if (group)
                    {
                        if (d > 0)
                        {
                            seq.addAction(Actions.delay(d));
                            d = 0;
                            da = 0;
                        }
                        playSequence(seq, groupArray, fast);
                        seq.addAction(Actions.delay(delay));
                    }

                    group = false;
                    continue;
                }
                case '|':
                {
                    seq.addAction(Actions.delay(0.4f));
                    continue;
                }
            }


            if (group)
            {
                groupArray.add(c);
            }
            else
            {
                Action a = playCharacter(c);
                if (a != null)
                {
                    if (d > 0)
                    {
                        seq.addAction(Actions.delay(d));
                        d = 0;
                        da = 0;
                    }
                    seq.addAction(a);
                    seq.addAction(Actions.delay(delay));
                }
            }
        }
    }

    private void playSequence(SequenceAction seq, CharArray array, boolean fast)
    {
        if (fast)
        {

            for (int i = 0; i < array.size; i++)
            {
                char c = array.get(i);
                Action a = playCharacter(c);
                if (a != null)
                {
                    seq.addAction(a);
                    seq.addAction(Actions.delay(0.05f));
                }
            }
        }
        else
        {
            ParallelAction p = Actions.parallel();
            for (int i = 0; i < array.size; i++)
            {
                char c = array.get(i);
                Action a = playCharacter(c);
                if (a != null)
                {
                    p.addAction(a);
                }
            }
            seq.addAction(p);
        }
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.bottom;
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
        {
            String v = shiftKeys.get(keyCode);
            if (v != null)
            {
                BrainOutClient.EventMgr.sendDelayedEvent(GameControllerEvent.obtain(GameControllerEvent.Action.custom, v));
                return true;
            }
        }
        else
        {
            String v = keys.get(keyCode);
            if (v != null)
            {
                BrainOutClient.EventMgr.sendDelayedEvent(GameControllerEvent.obtain(GameControllerEvent.Action.custom, v));
                return true;
            }
        }

        return super.keyDown(keyCode);
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE, getBatch());

        super.render();
    }

    @Override
    public void onFocusIn()
    {
        BrainOutClient.Env.getGameController().setControllerMode(GameController.ControllerMode.disabledKeys);

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }
}
