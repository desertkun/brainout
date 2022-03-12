package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.common.msg.client.ForgiveKillMsg;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.InstrumentAnimationComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.HealthComponentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.KillEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.InstrumentIcon;

public class FreePlayFollowFriendMenu extends Menu implements Watcher, EventReceiver
{
    private final ActiveData follow;
    private final Runnable complete;
    private float x, y;

    public FreePlayFollowFriendMenu(Map map, ActiveData follow,
                                    Runnable complete)
    {
        this.follow = follow;

        this.x = follow.getX();
        this.y = follow.getY();

        this.complete = complete;

        Map.SetWatcher(this);
    }

    private void complete()
    {
        pop();
        complete.run();
    }

    private float getFollowX()
    {
        ClientPlayerComponent cpc = follow.getComponent(ClientPlayerComponent.class);

        if (cpc != null)
        {
            return x + cpc.getMouseOffsetX();
        }

        return x;
    }

    private float getFollowY()
    {
        ClientPlayerComponent cpc = follow.getComponent(ClientPlayerComponent.class);

        if (cpc != null)
        {
            return y + cpc.getMouseOffsetY();
        }

        return y;
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if (Vector2.dst2(x, y, follow.getX(), follow.getY()) > 100)
        {
            x = follow.getX();
            y = follow.getY();
        }
        else
        {
            x = MathUtils.lerp(x, follow.getX(), delta * 2.0f);
            y = MathUtils.lerp(y, follow.getY(), delta * 2.0f);
        }

        if (!follow.isAlive())
        {
            complete();
        }
    }

    @Override
    public boolean popIfFocusOut()
    {
        return true;
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        BrainOutClient.Env.getGameController().setControllerMode(GameController.ControllerMode.disabled);
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.bottom;
    }

    @Override
    public Table createUI()
    {
        /*
        Table data = new Table();

        {
            TextButton completeButton = new TextButton(L.get("MENU_GAME_SUMMARY"),
                    BrainOutClient.Skin, "button-default");

            completeButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    playSound(MenuSound.select);
                    complete();
                }
            });

            data.add(completeButton).colspan(2).pad(10).size(300, 64).row();
        }

        return data;
        */

        return null;
    }

    @Override
    public float getWatchX()
    {
        return getFollowX();
    }

    @Override
    public float getWatchY()
    {
        return getFollowY();
    }

    @Override
    public boolean allowZoom()
    {
        return false;
    }

    @Override
    public float getScale()
    {
        return 1.0f;
    }

    @Override
    public String getDimension()
    {
        return follow.getDimension();
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
