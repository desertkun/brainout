package com.desertkun.brainout.mode;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.Notify;
import com.desertkun.brainout.playstate.PlayState;

public class ClientAssaultRealization extends ClientGameRealization<GameModeAssault>
{
    public ClientAssaultRealization(GameModeAssault gameMode)
    {
        super(gameMode);
    }

    @Override
    public void init(PlayState.InitCallback callback)
    {
        BrainOut.EventMgr.subscribe(Event.ID.simple, this);

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
    protected void updateStats()
    {
        super.updateStats();

        if (getGameMode().isGameActive(false, false))
        {
            updateCurrentState();
        }

        if (stats != null)
        {
            CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

            if (game != null)
            {
                boolean w = false;

                int targetScore = getGameMode().getTargetScore();

                int teams = 0;

                Team teamA = null, teamB = null;
                int scoreA = 0, scoreB = 0;

                for (Team team : game.getTeams())
                {
                    if (team == null)
                        continue;

                    if (team instanceof SpectatorTeam)
                        continue;

                    teams++;

                    if (teamA == null)
                    {
                        teamA = team;
                        scoreA = getGameMode().getPoints(team);
                        continue;
                    }

                    teamB = team;
                    scoreB = getGameMode().getPoints(team);
                }

                if (teams != 2)
                    return;

                int max = targetScore / 2 + 1;

                renderTeamStats(teamA, scoreA, max);
                renderTeamStats(teamB, scoreB, max);
            }
        }
    }

    private void renderTeamStats(Team team, int score, int progressMax)
    {
        CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

        if (game == null)
            return;

        Table item = new Table();

        IconComponent iconComponent = team.getComponent(IconComponent.class);

        item.add(new BorderActor(new Image(iconComponent.getIcon()))).expandY().fillY();

        String value = String.valueOf(score);

        Table statContainer = new Table();

        statContainer.add(new Image(BrainOutClient.getRegion("icon-points"))).padRight(8);

        Label statValue = new Label(value, BrainOutClient.Skin, "title-small");
        statValue.setColor(game.getController().getColorOf(team));
        statValue.setAlignment(Align.center);

        statContainer.add(statValue);

        item.add(new BorderActor(statContainer, 64)).expandY().fillY();

        ProgressBar progress = new ProgressBar(0, progressMax, 1, false,
                BrainOutClient.Skin, "progress-upgrades");
        progress.setColor(game.getController().getColorOf(team));
        progress.setValue(score);

        BorderActor progressBorder = new BorderActor(progress, 96);
        progressBorder.getCell().padLeft(2).padRight(2);
        item.add(progressBorder).expandY().fillY();

        stats.add(item).expandX().left().row();
    }

    private void updateCurrentState()
    {
        if (topStats != null)
        {
            switch (getGameMode().getState())
            {
                case active:
                {
                    Table counter = new Table();

                    Notify time = new Notify(String.valueOf((int)getGameMode().getTimer()), true, 0);

                    time.addAction(Actions.repeat(RepeatAction.FOREVER,
                        Actions.sequence(
                            Actions.delay(1.0f),
                            Actions.run(() ->
                            {
                                float timer = getGameMode().getTimer();

                                if (timer > 0)
                                {
                                    time.getTitle().setText(
                                            String.valueOf((int) timer)
                                    );

                                    if (timer <= 10)
                                    {
                                        Menu.playSound(Menu.MenuSound.character);
                                    }
                                }
                                else
                                {
                                    time.getTitle().setText("...");
                                }
                            })
                        )));

                    counter.add(time).padBottom(-32).row();

                    Label roundTitle = new Label(L.get("MENU_ROUND",
                        String.valueOf(getGameMode().getRound())), BrainOutClient.Skin, "title-small");

                    counter.add(roundTitle).row();

                    Table root = new Table();

                    Team teamA = null;
                    Team teamB = null;

                    CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

                    if (game != null)
                    {
                        for (Team team : game.getTeams())
                        {
                            if (team == null) continue;
                            if (team instanceof SpectatorTeam) continue;

                            if (teamA == null)
                            {
                                teamA = team;
                            }
                            else
                            {
                                teamB = team;
                            }
                        }
                    }

                    if (teamA != null)
                    {
                        Table icons = renderTeamAmount(teamA);
                        if (icons != null)
                        {
                            root.add(icons).padTop(-32).right();
                        }
                    }

                    root.add(counter).pad(-32, -96, 0, -96).center();

                    if (teamB != null)
                    {
                        Table icons = renderTeamAmount(teamB);
                        if (icons != null)
                        {
                            root.add(icons).padTop(-32).left();
                        }
                    }

                    topStats.add(root).padTop(16).center().row();

                    break;
                }
                case waiting:
                {
                    Table counter = new Table();

                    Image notice = new Image(BrainOutClient.getRegion("label-warmup"));
                    counter.add(notice).padBottom(-72).row();

                    Label title = new Label(L.get("MENU_EQUIP"),
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

                                if (timer > 0)
                                {
                                    time.getTitle().setText(
                                            String.valueOf((int) timer)
                                    );

                                    if (timer <= 10)
                                    {
                                        Menu.playSound(Menu.MenuSound.character);
                                    }
                                }
                                else
                                {
                                    time.getTitle().setText("...");
                                }
                            })
                        )));

                    counter.add(time).row();

                    topStats.add(counter).row();

                    break;
                }
            }
        }
    }

    private Table renderTeamAmount(Team team)
    {
        CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

        if (game != null)
        {
            GameModeAssault.TeamState teamStats = getGameMode().getPlayersStats(team);
            if (teamStats != null)
            {
                Table icons = new Table();

                for (int i = 0; i < teamStats.alive; i++)
                {
                    String icon = team == game.getTeam() ? "assault-icon-friend" : "assault-icon-enemy";
                    icons.add(new BorderActor(new Image(BrainOutClient.Skin, icon)));
                }
                for (int i = 0; i < teamStats.dead; i++)
                {
                    icons.add(new BorderActor(new Image(BrainOutClient.Skin, "assault-icon-dead")));
                }

                return icons;
            }
        }

        return null;
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
}
