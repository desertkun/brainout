package com.desertkun.brainout.mode;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.gs.ActionPhaseState;
import com.desertkun.brainout.gs.actions.MenuAction;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.ActionPhaseMenu;
import com.desertkun.brainout.menu.impl.DeathCameraMenu;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.Notify;
import com.desertkun.brainout.utils.TimeUtils;

public abstract class ClientGameRealization<G extends GameMode> extends ClientRealization<G>
{
    private Array<FlagIcon> icons;

    private boolean tookPartInWarmUp;

    public ClientGameRealization(G gameMode)
    {
        super(gameMode);

        this.icons = new Array<FlagIcon>();
        this.tookPartInWarmUp = false;
    }

    public Color getColorOf(ActiveData data)
    {
        return null;
    }

    public Color getColorOf(RemoteClient remoteClient)
    {
        return null;
    }

    public static class FlagIcon extends Table implements EventReceiver
    {
        private final FlagData flagData;
        private FlagData.State state;

        public FlagData getFlagData()
        {
            return flagData;
        }

        public FlagIcon(FlagData flagData)
        {
            this.flagData = flagData;
            this.state = null;
            updateState();

            BrainOutClient.EventMgr.subscribe(Event.ID.activeAction, this);
        }

        private void updateState()
        {
            if (this.state == flagData.getState())
            {
                return;
            }

            this.state = flagData.getState();

            clear();

            ClientController CC = BrainOutClient.ClientController;

            clearActions();

            switch (flagData.getState())
            {
                case normal:
                {
                    TextureAtlas.AtlasRegion region = BrainOutClient.getRegion(flagData.getTeam() == null ?
                            "icon-flag-none" :
                            (CC.getTeam() == flagData.getTeam() ?
                                    "icon-flag-friend" : "icon-flag-enemy"));

                    if (region != null)
                        add(new Image(region));

                    break;
                }
                case taking:
                case paused:
                {
                    final String blink1 = flagData.getTeam() == null ?
                            "icon-flag-none" :
                            (CC.getTeam() == flagData.getTeam() ?
                                    "icon-flag-friend" : "icon-flag-enemy");

                    final String blink2 =
                            (CC.getTeam() == flagData.getTakingTeam() ?
                                    "icon-flag-friend" : "icon-flag-enemy");

                    TextureAtlas.AtlasRegion blink1Region = BrainOutClient.getRegion(blink1);
                    TextureAtlas.AtlasRegion blink2Region = BrainOutClient.getRegion(blink2);

                    if (blink1Region != null && blink2Region != null)
                    {
                        final Image image = new Image(blink1Region);

                        image.addAction(Actions.repeat(RepeatAction.FOREVER,
                                Actions.sequence(
                                        Actions.run(() -> image.setDrawable(BrainOutClient.Skin, blink2)),
                                        Actions.delay(0.5f),
                                        Actions.run(() -> image.setDrawable(BrainOutClient.Skin, blink1)),
                                        Actions.delay(0.5f)
                                )));

                        add(image);
                    }

                    break;
                }
            }
        }

        public void release()
        {
            BrainOutClient.EventMgr.unsubscribe(Event.ID.activeAction, this);
        }

        @Override
        public boolean onEvent(Event event)
        {
            switch (event.getID())
            {
                case activeAction:
                {
                    ActiveActionEvent activeEvent = (ActiveActionEvent)event;

                    if (activeEvent.activeData == flagData && activeEvent.action == ActiveActionEvent.Action.updated)
                    {
                        updateState();
                    }

                    break;
                }
            }

            return false;
        }
    }

    @Override
    public void release()
    {
        super.release();

        clear();
    }

    protected void clear()
    {
        if (stats != null)
        {
            topStats.clear();
            stats.clear();
        }

        for (FlagIcon flagIcon: icons)
        {
            flagIcon.release();
        }

        icons.clear();
    }

    @Override
    public void init(ActionPhaseMenu menu)
    {
        topStats = new Table();
        topStats.align(Align.top | Align.center);
        topStats.setFillParent(true);

        this.stats = new Table();
        stats.align(Align.left | Align.bottom);
        stats.setBounds(16, 16, BrainOutClient.getWidth(), BrainOutClient.getHeight() / 2f);

        updateStats();

        menu.addActor(stats);
        menu.addActor(topStats);
    }

    protected void updateStats()
    {
        clear();

        switch (getGameMode().getPhase())
        {
            case warmUp:
            {
                tookPartInWarmUp = true;

                break;
            }
        }

        if (topStats != null)
        {
            switch (getGameMode().getPhase())
            {
                case warmUp:
                {
                    Table counter = new Table();

                    Image notice = new Image(BrainOutClient.getRegion("label-warmup"));
                    counter.add(notice).padBottom(-72).row();

                    Label title = new Label(L.get("MENU_WARMUP"),
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
            }
        }

        Table points = new Table();

        CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

        if (game != null)
        {
            for (Map map : Map.All())
            {
                for (ActiveData entry : map.getActivesForTag(Constants.ActiveTags.SPAWNABLE, false))
                {
                    if (entry instanceof FlagData)
                    {
                        FlagData flagData = ((FlagData) entry);

                        icons.add(new FlagIcon(flagData));
                    }
                }
            }
        }

        icons.sort((o1, o2) ->
                o1.getFlagData().getX() > o2.getFlagData().getX() ? 1 : -1);

        for (FlagIcon icon: icons)
        {
            points.add(new BorderActor(icon));
        }

        renderTimeLeft(points);

        if (stats != null)
        {
            stats.add(points).expandX().left().padBottom(16).row();
        }
    }

    protected Cell<Table> renderTimeLeft(Table container)
    {
        TextureRegion timeIcon;

        try
        {
            timeIcon = BrainOutClient.Skin.getRegion("icon-time");
        }
        catch (GdxRuntimeException ignored)
        {
            return null;
        }

        if (timeIcon == null)
            return null;

        Table time = new Table();

        {
            time.add(new Image(timeIcon));

            int timeLeft = getTimeLeft();

            if (timeLeft == 0)
            {
                time.setVisible(false);
            }

            Label timeLabel = new Label(
                    timeLeft > 0 ? TimeUtils.formatMinutesInterval(timeLeft * 1000L) : "",
                    BrainOutClient.Skin, "title-small");

            timeLabel.addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                    Actions.delay(1.0f),
                    Actions.run(() ->
                    {
                        int newTimeLeft = getTimeLeft();

                        if (newTimeLeft > 0)
                        {
                            timeLabel.setText(TimeUtils.formatMinutesInterval(newTimeLeft * 1000L));
                            time.setVisible(true);

                            if (newTimeLeft < 60)
                            {
                                timeLabel.setColor(newTimeLeft % 2 == 0 ? Color.RED : Color.WHITE);
                            }
                            else
                            {
                                timeLabel.setColor(Color.WHITE);
                            }
                        }
                        else
                        {
                            time.setVisible(false);
                        }
                    })
            )));

            time.add(timeLabel);
        }

        return container.add(time).padLeft(8).left();
    }

    public boolean autoOpenShopOnSpawn()
    {
        return true;
    }

    private int getTimeLeft()
    {
        long longEndTime = getGameMode().getEndTime();

        if (longEndTime == 0)
            return 0;

        long now = System.currentTimeMillis() / 1000L;

        if (now >= longEndTime)
        {
            return 0;
        }

        return (int)(longEndTime - now);
    }

    public boolean isWarmUp()
    {
        return getGameMode().getPhase() == GameMode.Phase.warmUp;
    }

    public boolean isTookPartInWarmUp()
    {
        return tookPartInWarmUp;
    }

    public void onKilledBy(ActionPhaseState ps, Map map, ActiveData killer, InstrumentInfo info)
    {
        ps.addAction(new MenuAction()
        {
            @Override
            public void run()
            {
                ps.popAllUntil(ActionPhaseMenu.class);
                ps.pushMenu(new DeathCameraMenu(map, killer, info, this::done));
            }
        });
    }
}
