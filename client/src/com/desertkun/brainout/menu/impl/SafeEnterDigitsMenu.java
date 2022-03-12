package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class SafeEnterDigitsMenu extends Menu
{
    public static final int CODE_LENGTH = 4;

    private final Array<String> digits;
    private final String emptyDigit;
    private final Entered entered;
    private String value;
    private SoundEffect beep;
    private Table digitsValue;
    private int codeLength;

    public interface Entered
    {
        void entered(String code);
    }

    public SafeEnterDigitsMenu(Array<String> digits, String emptyDigit, SoundEffect beep, Entered entered)
    {
        this(digits, emptyDigit, beep, entered, CODE_LENGTH);
    }

    public SafeEnterDigitsMenu(Array<String> digits, String emptyDigit, SoundEffect beep, Entered entered, int codeLength)
    {
        this.digits = digits;
        this.emptyDigit = emptyDigit;
        this.beep = beep;
        this.value = "";
        this.entered = entered;
        this.codeLength = codeLength;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            digitsValue = new Table();
            renderDigits();
            data.add(new BorderActor(digitsValue)).pad(32).row();
        }

        // buttons

        {
            Table buttons = new Table();

            addButtonsRow(buttons, "1", "2", "3").row();
            addButtonsRow(buttons, "4", "5", "6").row();
            addButtonsRow(buttons, "7", "8", "9").row();
            addButtonsRow(buttons, "0").colspan(3).row();

            data.add(new BorderActor(buttons)).pad(32).row();
        }

        return data;
    }

    private Cell<TextButton> addButtonsRow(Table buttons, String ... values)
    {
        Cell<TextButton> cell = null;

        for (String s : values)
        {
            final String v = s;
            TextButton btn = new TextButton(s, BrainOutClient.Skin, "button-default");
            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    addValue(v);
                }
            });

            cell = buttons.add(btn).size(96).pad(4);
        }

        return cell;
    }

    private void addValue(String v)
    {
        if (value.length() >= codeLength)
            return;

        value = value + v;

        beep();
        renderDigits();

        if (value.length() >= codeLength)
        {
            addAction(Actions.sequence(
                Actions.delay(0.5f),
                Actions.run(() -> {
                        pop();
                        entered.entered(value);
                })
            ));
        }
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    private void beep()
    {
        Watcher watcher = Map.GetWatcher();

        if (watcher == null)
            return;

        ClientMap map = Map.GetWatcherMap(ClientMap.class);

        if (map != null)
        {
            map.addEffect(beep, new PointLaunchData(watcher.getWatchX(), watcher.getWatchY(), 0, watcher.getDimension()));
        }
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    private void renderDigits()
    {
        digitsValue.clear();

        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);
            int ord = c - '0';

            if (ord < 0 || ord > 9)
                continue;

            Image img = new Image(BrainOutClient.Skin, digits.get(ord));
            img.setTouchable(Touchable.disabled);
            img.setScaling(Scaling.none);

            digitsValue.add(img);
        }

        for (int i = 0, t = codeLength - value.length(); i < t; i++)
        {
            Image img = new Image(BrainOutClient.Skin, emptyDigit);
            img.setTouchable(Touchable.disabled);
            img.setScaling(Scaling.none);

            digitsValue.add(img);
        }
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
