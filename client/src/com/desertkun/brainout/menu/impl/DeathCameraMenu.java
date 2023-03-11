package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.common.msg.client.CurrentlyWatchingMsg;
import com.desertkun.brainout.common.msg.client.ForgiveKillMsg;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.HealthComponentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.ProfileBadgeWidget;

public class DeathCameraMenu extends Menu implements Watcher, EventReceiver
{
    private final ActiveData killer;
    private final Runnable complete;
    private final InstrumentInfo instrumentInfo;
    private final boolean showId;
    private float x, y;
    private String dimension;
    private boolean lerp;

    public DeathCameraMenu(Map map,
                           ActiveData killer,
                           InstrumentInfo instrumentInfo,
                           Runnable complete)
    {
        this(map, killer, instrumentInfo, complete, false);
    }

    public DeathCameraMenu(Map map,
                           ActiveData killer,
                           InstrumentInfo instrumentInfo,
                           Runnable complete,
                           boolean showId)
    {
        this.killer = killer;
        this.dimension = killer.getDimension();

        this.instrumentInfo = instrumentInfo;
        this.showId = showId;

        if (Map.GetWatcher() != null)
        {
            this.x = Map.GetWatcher().getWatchX();
            this.y = Map.GetWatcher().getWatchY();
        }

        this.lerp = false;
        this.complete = complete;

        BrainOutClient.ClientController.sendTCP(new CurrentlyWatchingMsg(killer.getOwnerId()));
        Map.SetWatcher(this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.gameController, this);
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOutClient.EventMgr.subscribe(Event.ID.gameController, this);

        addAction(Actions.sequence(
                Actions.delay(1.0f),
                Actions.run(() -> lerp = true),
                Actions.delay(4.0f),
                Actions.run(this::complete)
        ));
    }

    private void complete()
    {
        if (lerp)
        {
            pop();
            complete.run();

            lerp = false;
        }
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if (killer != null)
        {
            if (lerp && BrainOutClient.PackageMgr.getDefine("killcam", "enabled").equals("enabled"))
            {
                x = MathUtils.lerp(x, killer.getX(), delta * 2.0f);
                y = MathUtils.lerp(y, killer.getY(), delta * 2.0f);
            }
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
        if (killer == null)
        {
            Table data = new Table();

            Label youKilled = new Label(L.get("MENU_KILLED"), BrainOutClient.Skin, "title-small");
            youKilled.setAlignment(Align.center);

            return data;
        }

        final RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(killer.getOwnerId());

        ClientController CC = BrainOutClient.ClientController;

        if (remoteClient == null) return null;

        Table data = new Table();

        Label youKilled = new Label(L.get("MENU_KILLED"), BrainOutClient.Skin, "title-small");
        youKilled.setAlignment(Align.center);

        int id = remoteClient.getId();
        if (!CC.isEnemies(remoteClient, CC.getMyRemoteClient()) && id != CC.getMyId())
        {
            TextButton forgive = new TextButton(L.get("MENU_FORGIVE"),
                    BrainOutClient.Skin, "button-default");

            forgive.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    if (lerp)
                    {
                        playSound(MenuSound.select);
                        BrainOutClient.ClientController.sendTCP(new ForgiveKillMsg(id));

                        complete();
                    }
                }
            });

            data.add(forgive).colspan(2).pad(10).size(192, 40).row();

        }

        data.add(youKilled).colspan(2).pad(8).fillX().row();

        HealthComponentData health = killer.getComponent(HealthComponentData.class);

        if (health != null)
        {
            ProfileBadgeWidget badgeWidget = new ProfileBadgeWidget(
                    remoteClient.getName(), CC.getColorOf(remoteClient),
                    remoteClient.getInfoString(Constants.User.PROFILE_BADGE, Constants.User.PROFILE_BADGE_DEFAULT),
                    remoteClient.getAvatar(), remoteClient.getLevel(), (int) health.getHealth(), instrumentInfo);

            data.add(badgeWidget).size(384, 112).row();
        }

        boolean isBot = remoteClient.getInfoBoolean("bot", false);
        if (showId && !isBot)
        {
            String labelId = "id " + remoteClient.getAccountId();
            // Based on the account id label in PlayerProfileMenu
            Label label = new Label(labelId, BrainOutClient.Skin, "title-gray");
            data.add(label).pad(10).padTop(0).row();
        }

        return data;
    }

    @Override
    public float getWatchX()
    {
        return x;
    }

    @Override
    public float getWatchY()
    {
        return y;
    }

    @Override
    public boolean allowZoom()
    {
        return false;
    }

    @Override
    public float getScale()
    {
        return 0.75f;
    }

    @Override
    public String getDimension()
    {
        return dimension;
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        switch (keyCode)
        {
            case Input.Keys.SPACE:
            {
                if (lerp)
                {
                    complete();
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case beginLaunch:
                    {
                        complete();

                        return true;
                    }
                }

                break;
            }
        }

        return false;
    }
}
