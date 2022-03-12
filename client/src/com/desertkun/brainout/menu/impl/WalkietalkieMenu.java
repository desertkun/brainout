package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.content.consumable.Walkietalkie;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class WalkietalkieMenu extends Menu
{
    public static final int CODE_LENGTH = 6;

    private final Array<String> digits;
    private final String emptyDigit;
    private final Changed changed;
    private final SoundEffect touchdown;
    private String value;
    private final SoundEffect changeFrequency;
    private Table digitsValue;

    public interface Changed
    {
        void changed(int frequency);
    }

    public WalkietalkieMenu(int frequency, Array<String> digits, String emptyDigit, SoundEffect touchdown,
                            SoundEffect changeFrequency, Changed changed)
    {
        this.digits = new Array<String>(new String[]{
                "broufeng_font_0",
                "broufeng_font_1",
                "broufeng_font_2",
                "broufeng_font_3",
                "broufeng_font_4",
                "broufeng_font_5",
                "broufeng_font_6",
                "broufeng_font_7",
                "broufeng_font_8",
                "broufeng_font_9"
        });
        this.emptyDigit = emptyDigit;
        this.touchdown = touchdown;
        this.changeFrequency = changeFrequency;
        this.value = String.valueOf(frequency);
        this.changed = changed;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();
        data.setSkin(BrainOutClient.Skin);
        data.setBackground("broufeng_main2");
        data.bottom();
        data.left();
        data.setTransform(true);
        data.setLayoutEnabled(true);
        data.setSize(266, 736);
        data.setDebug(false);
        data.padRight(30).padTop(360);

        //digits

        {
            digitsValue = new Table();
            renderDigits();
            data.add(digitsValue).padBottom(59).padLeft(88).align(Align.left).row();
        }

        //exit button
        Drawable btnDrawable = new Image(BrainOutClient.Skin, "broufeng_exit").getDrawable();
        ImageButton btn = new ImageButton(null, btnDrawable);
        btn.addListener(new ClickOverListener()
        {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                playSound(touchdown);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
            {
                pop();
            }
        });

        data.add(btn).size(40, 28).padBottom(108).padLeft(52).align(Align.left).row();

        // buttons

        {
            Table buttons = new Table();

            addButtonsRow(buttons, "1", "2", "3").row();
            addButtonsRow(buttons, "4", "5", "6").row();
            addButtonsRow(buttons, "7", "8", "9").row();
            addButtonsRow(buttons, "0").colspan(3).row();

            data.add(buttons).padLeft(50).padBottom(36).row();
        }

        return data;
    }

    private Cell<ImageButton> addButtonsRow(Table buttons, String ... values)
    {
        Cell<ImageButton> cell = null;

        for (String s : values)
        {
            final String v = s;
            Drawable btnDrawable = new Image(BrainOutClient.Skin, "broufeng_" + s).getDrawable();
            ImageButton btn = new ImageButton(null, btnDrawable);
            btn.addListener(new ClickOverListener()
            {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
                {
                    addValue(v);
                    playSound(touchdown);
                    return true;
                }
            });

            cell = buttons.add(btn).size(46, 26).padRight(16).padTop(6);
        }

        return cell;
    }

    private void addValue(String v)
    {
        if (value.length() >= CODE_LENGTH)
            value = "";

        value = value + v;

        if (value.length() >= CODE_LENGTH)
        {
            try {
                int intValue = Integer.valueOf(value);
                intValue = Walkietalkie.validateFrequency(intValue);
                changed.changed(intValue);
                value = String.valueOf(intValue);
                playSound(changeFrequency);
            }
            catch(NumberFormatException ignored)
            {}
        }

        renderDigits();
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    private void playSound(SoundEffect sound)
    {
        Watcher watcher = Map.GetWatcher();

        if (watcher == null)
            return;

        ClientMap map = Map.GetWatcherMap(ClientMap.class);

        if (map != null)
        {
            map.addEffect(sound, new PointLaunchData(watcher.getWatchX(), watcher.getWatchY(), 0, watcher.getDimension()));
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

            int pad = 1;

            if (i == 2) pad += 9;
            else if (i == value.length() - 1) pad = 0;

            digitsValue.add(img).padRight(pad);
        }
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
