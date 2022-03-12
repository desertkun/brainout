package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.settings.KeyProperties;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.esotericsoftware.minlog.Log;

public class RoundLockCodeMenu extends Menu
{
    public static final float ACCELERATION = 10;
    public static final float MAX_SPEED = 50;

    private final Array<Integer> code;
    private final Runnable opened;
    private Array<Image> tumblerLocks;
    private Array<Label> tumblerLabels;
    private Image pad;

    private Sound lockerClick;
    private Sound lockerClickHit;
    private Sound lockerClickFail;

    private int progress;
    private int value;
    private int prevValue;
    private int moving;
    private int currentDirection;
    private float speed;
    private float angle;
    private TextButton open;

    public RoundLockCodeMenu(String code, Runnable opened)
    {
        tumblerLocks = new Array<>();
        tumblerLabels = new Array<>();
        moving = 0;
        value = 0;
        prevValue = 0;
        speed = 0;
        angle = 1000;
        progress = 0;
        currentDirection = 0;
        this.opened = opened;
        this.code = new Array<>();

        for (String s : code.split(","))
        {
            try
            {
                int v = Integer.parseInt(s);
                this.code.add(v);
            }
            catch (NumberFormatException ignored)
            {
                //
            }
        }

        lockerClick = BrainOutClient.ContentMgr.get("fp-lock-click-snd", Sound.class);
        lockerClickHit = BrainOutClient.ContentMgr.get("fp-lock-click-hit-snd", Sound.class);
        lockerClickFail = BrainOutClient.ContentMgr.get("fp-lock-click-fail-snd", Sound.class);
    }

    public int getTumblersCount()
    {
        return code.size;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Table tumblers = new Table();
            tumblers.align(Align.top);

            for (int i = 0; i < getTumblersCount(); i++)
            {
                Table tumbler = new Table(BrainOutClient.Skin);
                tumbler.setBackground("form-default");

                Image image = new Image(BrainOutClient.Skin, "fp-tumbler-locked");
                tumbler.add(image).pad(8);
                tumblerLocks.add(image);

                Label label = new Label(L.get("MENU_TUMBLER", String.valueOf(i + 1)),
                    BrainOutClient.Skin, "title-gray");
                tumbler.add(label).expandX().left().pad(8).row();
                tumblerLabels.add(label);

                tumblers.add(tumbler).expandX().fill().height(64).row();
            }

            open = new TextButton(L.get("MENU_OPEN"), BrainOutClient.Skin, "button-green");
            open.setVisible(false);

            open.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    open();
                }
            });

            tumblers.add(open).expandX().fill().height(64).padTop(8);

            data.add(tumblers).expandY().top().width(192);
        }

        {
            Table root = new Table(BrainOutClient.Skin);
            root.setBackground("form-default");

            {
                Image bg = new Image(BrainOutClient.Skin, "fp-locker-bg");
                bg.setTouchable(Touchable.disabled);
                bg.setScaling(Scaling.none);
                bg.setFillParent(true);

                root.addActor(bg);
            }

            {
                pad = new Image(BrainOutClient.Skin, "fp-locker-fg");
                pad.setTouchable(Touchable.disabled);
                pad.setScaling(Scaling.none);
                pad.setOrigin(354 / 2, 354 / 2);
                pad.setBounds(38, 38, 354, 354);

                root.addActor(pad);
            }

            {
                Image shadow = new Image(BrainOutClient.Skin, "fp-locker-shadow");
                shadow.setTouchable(Touchable.disabled);
                shadow.setScaling(Scaling.none);
                shadow.setFillParent(true);

                root.addActor(shadow);
            }

            data.add(root).size(430).row();
        }

        return data;
    }

    private void open()
    {
        if (progress == getTumblersCount())
        {
            opened.run();
            pop();
        }
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        KeyProperties.Keys action = BrainOutClient.ClientSett.getControls().getKey(keyCode);

        if (action != null)
        {
            switch (action)
            {
                case right:
                {
                    moving = 1;
                    return true;
                }
                case left:
                {
                    moving = -1;
                    return true;
                }
            }
        }

        return super.keyDown(keyCode);
    }

    @Override
    public boolean keyUp(int keyCode)
    {
        KeyProperties.Keys action = BrainOutClient.ClientSett.getControls().getKey(keyCode);

        if (action != null)
        {
            switch (action)
            {
                case right:
                case left:
                {
                    moving = 0;
                    return true;
                }
            }
        }
        return super.keyUp(keyCode);
    }

    @Override
    public void act(float dt)
    {
        super.act(dt);

        angle += speed * dt;

        if (moving > 0)
        {
            speed = Math.min(MAX_SPEED, speed + ACCELERATION * dt);
        }
        else if (moving < 0)
        {
            speed = Math.max(-MAX_SPEED, speed - ACCELERATION * dt);
        }
        else
        {
            speed = MathUtils.lerp(speed, 0, dt * 5);
        }

        if (pad != null)
        {
            pad.setRotation((angle + 15) * 3.6f);
        }

        if (Math.abs(angle - (float)prevValue) > 1)
        {
            prevValue = Math.round(angle);
            click((int)Math.signum(speed));
        }
    }

    private void click(int direction)
    {
        if (currentDirection == 0)
        {
            currentDirection = direction;
        }

        value = (Math.round(pad.getRotation() / 3.6f) - 15) % 100;

        if (Log.INFO) Log.info("Picking: " + value);

        if (currentDirection != direction)
        {
            fail();
            return;
        }

        if (progress < getTumblersCount())
        {
            int requiredValue = code.get(progress);

            if (value == requiredValue)
            {
                lockerClickHit.play();
                progress++;
                currentDirection = -currentDirection;

                updateProgress();
                return;
            }
        }

        lockerClick.play();

    }

    private void fail()
    {
        lockerClickFail.play();
        progress = 0;
        currentDirection = 0;
        value = 0;

        updateProgress();
    }

    private void updateProgress()
    {
        for (int i = 0; i < progress; i++)
        {
            tumblerLocks.get(i).setDrawable(BrainOutClient.Skin, "fp-tumbler-unlocked");
            tumblerLabels.get(i).setStyle(BrainOutClient.Skin.get("title-small", Label.LabelStyle.class));
        }

        for (int i = progress; i < getTumblersCount(); i++)
        {
            tumblerLocks.get(i).setDrawable(BrainOutClient.Skin, "fp-tumbler-locked");
            tumblerLabels.get(i).setStyle(BrainOutClient.Skin.get("title-gray", Label.LabelStyle.class));
        }

        open.setVisible(progress == getTumblersCount());
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }
}
