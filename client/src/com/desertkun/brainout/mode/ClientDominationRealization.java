package com.desertkun.brainout.mode;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.playstate.PlayState;

public class ClientDominationRealization extends ClientGameRealization<GameModeDomination>
{
    public ClientDominationRealization(GameModeDomination gameMode)
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

    protected void updateStats()
    {
        super.updateStats();

        if (stats != null)
        {
            CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

            if (game != null)
            {
                for (Team team : game.getTeams())
                {
                    if (team == null) continue;
                    if (team instanceof SpectatorTeam) continue;

                    Table item = new Table();

                    IconComponent iconComponent = team.getComponent(IconComponent.class);

                    item.add(new BorderActor(new Image(iconComponent.getIcon()))).expandY().fillY();

                    int points = getGameMode().getPoints(team);
                    String value = String.valueOf(points);

                    Table statContainer = new Table();

                    statContainer.add(new Image(BrainOutClient.getRegion("icon-points"))).padRight(8);

                    Label statValue = new Label(value, BrainOutClient.Skin, "title-small");
                    statValue.setColor(game.getController().getColorOf(team));
                    statValue.setAlignment(Align.center);

                    statContainer.add(statValue);

                    item.add(new BorderActor(statContainer, 64)).expandY().fillY();

                    ProgressBar progress = new ProgressBar(0, getGameMode().getWinPoints(), 1, false,
                            BrainOutClient.Skin, "progress-upgrades");
                    progress.setColor(game.getController().getColorOf(team));
                    progress.setValue(points);

                    BorderActor progressBorder = new BorderActor(progress, 96);
                    progressBorder.getCell().padLeft(2).padRight(2);
                    item.add(progressBorder).expandY().fillY();

                    stats.add(item).expandX().left().row();
                }
            }
        }
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

                if (simpleEvent.getAction() != null)
                {
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
        }

        return false;
    }
}
