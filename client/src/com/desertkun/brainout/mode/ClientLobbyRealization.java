package com.desertkun.brainout.mode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.common.msg.server.ShootingRangeCompletedMsg;
import com.desertkun.brainout.common.msg.server.ShootingRangeHitMsg;
import com.desertkun.brainout.common.msg.server.ShootingRangeStartedMsg;
import com.desertkun.brainout.common.msg.server.ShootingRangeWarmupMsg;
import com.desertkun.brainout.content.Effect;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.*;
import com.desertkun.brainout.menu.ui.Notify;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.utils.TimeUtils;

public class ClientLobbyRealization extends ClientGameRealization<GameModeLobby>
{
    public enum ShootingRangeMode
    {
        none,
        warmup,
        shooting
    }

    private ShootingRangeMode shootingRangeMode;
    private int shootingRangeHits;

    public ClientLobbyRealization(GameModeLobby gameMode)
    {
        super(gameMode);

        shootingRangeMode = ShootingRangeMode.none;
        shootingRangeHits = 0;
    }

    @Override
    public void init(PlayState.InitCallback callback)
    {
        BrainOut.EventMgr.subscribe(Event.ID.simple, this);

        GameState topState = BrainOutClient.getInstance().topState();

        final CSGame gameController = BrainOutClient.ClientController.getState(CSGame.class);

        topState.pushMenu(new WaitProfileMenu(() ->
        {
            UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

            if (userProfile.isDeactivated())
            {
                topState.pushMenu(new AccountDeactivatedMenu(() ->
                    topState.pushMenu(new LobbyMenu(gameController.getShopCart()))));
            }
            else
            {
                topState.pushMenu(new LobbyMenu(gameController.getShopCart()));
            }

            topState.pushMenu(new FadeOutMenu(1.0f, Color.BLACK));
        }));

        if (callback != null)
        {
            callback.done(true);
        }
    }

    @Override
    public void release()
    {
        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
    }

    @Override
    protected void updated()
    {
        super.updated();

        updateStats();
    }

    @Override
    public void update(float dt)
    {

    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                SimpleEvent simpleEvent = ((SimpleEvent) event);

                if (simpleEvent.getAction() == null)
                    return false;

                switch (simpleEvent.getAction())
                {
                    case teamUpdated:
                    {
                        updateStats();
                        break;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean canDropConsumable(ConsumableRecord record)
    {
        return false;
    }

    public void setShootingRangeMode(ShootingRangeMode shootingRangeMode)
    {
        this.shootingRangeMode = shootingRangeMode;

        updateStats();
    }

    @Override
    protected void updateStats()
    {
        clear();

        switch (shootingRangeMode)
        {
            case warmup:
            {
                Table counter = new Table();

                Image notice = new Image(BrainOutClient.getRegion("label-shooting-range"));
                counter.add(notice).padBottom(-72).row();

                Label title = new Label(L.get("MENU_GET_READY"),
                        BrainOutClient.Skin, "title-small");
                title.setAlignment(Align.left);
                counter.add(title).padBottom(16).row();

                Notify time = new Notify(String.valueOf((int)getGameMode().getTimer()), true, 0);

                time.addAction(Actions.repeat(RepeatAction.FOREVER,
                        Actions.sequence(
                            Actions.delay(1.0f),
                            Actions.run(() ->
                            {
                                float timer = getGameMode().getTimer();

                                time.getTitle().setText(
                                        String.valueOf((int)timer)
                                );

                                if (timer <= 10)
                                {
                                    Menu.playSound(Menu.MenuSound.character);
                                }
                            })
                        )));

                counter.add(time).row();

                topStats.add(counter).row();

                break;
            }
            case shooting:
            {
                Table counter = new Table();

                Image notice = new Image(BrainOutClient.getRegion("label-shooting-range"));
                counter.add(notice).padBottom(-72).row();

                Label hitsCounter = new Label(String.valueOf(shootingRangeHits), BrainOutClient.Skin, "title-small");

                hitsCounter.setAlignment(Align.left);
                counter.add(hitsCounter).padBottom(16).row();

                Notify time = new Notify(
                    TimeUtils.formatMinutesInterval((int)getGameMode().getTimer()* 1000L), true, 0);

                time.addAction(Actions.repeat(RepeatAction.FOREVER,
                    Actions.sequence(
                        Actions.delay(1.0f),
                        Actions.run(() ->
                        {
                            float timer = getGameMode().getTimer();

                            time.getTitle().setText(
                                TimeUtils.formatMinutesInterval((int)timer * 1000L)
                            );

                            if (timer <= 30)
                            {
                                Menu.playSound(Menu.MenuSound.character);
                            }
                        })
                )));

                counter.add(time).row();

                topStats.add(counter).row();

                break;
            }
        }

    }

    public boolean received(ShootingRangeCompletedMsg msg)
    {
        Gdx.app.postRunnable(() -> {

            SoundEffect effect = BrainOutClient.ContentMgr.get("buzzer-sound", SoundEffect.class);

            if (effect != null)
            {
                ClientMap clientMap = Map.GetWatcherMap(ClientMap.class);

                if (clientMap != null && Map.GetWatcher() != null)
                {
                    clientMap.addEffect(effect, new PointLaunchData(
                        Map.GetWatcher().getWatchX(),
                        Map.GetWatcher().getWatchY(), 0, Map.GetWatcher().getDimension()
                    ));
                }
            }

            setShootingRangeMode(ShootingRangeMode.none);
        });

        return true;
    }

    public boolean received(ShootingRangeHitMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            shootingRangeHits = msg.hits;
            updateStats();
        });

        return true;
    }

    public boolean received(ShootingRangeStartedMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            SoundEffect effect = BrainOutClient.ContentMgr.get("buzzer-sound", SoundEffect.class);

            if (effect != null)
            {
                ClientMap clientMap = Map.GetWatcherMap(ClientMap.class);

                if (clientMap != null && Map.GetWatcher() != null)
                {
                    clientMap.addEffect(effect, new PointLaunchData(
                            Map.GetWatcher().getWatchX(),
                            Map.GetWatcher().getWatchY(), 0, Map.GetWatcher().getDimension()
                    ));
                }
            }

            getGameMode().setTimer(msg.time, null);
            shootingRangeHits = 0;
            setShootingRangeMode(ShootingRangeMode.shooting);
        });

        return true;
    }

    public boolean received(ShootingRangeWarmupMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            getGameMode().setTimer(msg.time, null);
            setShootingRangeMode(ShootingRangeMode.warmup);
        });

        return true;
    }

    public boolean isInShootingRangeMode()
    {
        return shootingRangeMode != ShootingRangeMode.none;
    }
}
