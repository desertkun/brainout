package com.desertkun.brainout.mode;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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

public class ClientCTFRealization extends ClientGameRealization<GameModeCTF>
{
    public ClientCTFRealization(GameModeCTF gameMode)
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
                    if (team instanceof SpectatorTeam) continue;

                    Table item = new Table();

                    IconComponent iconComponent = team.getComponent(IconComponent.class);
                    item.add(new BorderActor(new Image(iconComponent.getIcon()))).expandY().fillY();

                    int points = getGameMode().getPoints(team);

                    Table statContainer = new Table();

                    for (int i = 0; i < getGameMode().getWinPoints(); i++)
                    {
                        Image image;

                        if (points > i)
                        {
                            image = new Image(BrainOutClient.getRegion(
                                team == game.getTeam() ? "icon-chip-my-taken" : "icon-chip-enemy-taken"));
                        }
                        else
                        {
                            if (i < points + getGameMode().getTakingPoints(team))
                            {
                                TextureRegion blink1 = BrainOutClient.getRegion(
                                        team == game.getTeam() ? "icon-chip-my-taking" : "icon-chip-enemy-taking");
                                TextureRegion blink2 = BrainOutClient.getRegion("icon-chip-none");

                                image = new Image(blink1);

                                image.addAction(Actions.repeat(RepeatAction.FOREVER,
                                    Actions.sequence(
                                        Actions.run(() -> image.setDrawable(new TextureRegionDrawable(blink2))),
                                        Actions.delay(0.5f),
                                        Actions.run(() -> image.setDrawable(new TextureRegionDrawable(blink1))),
                                        Actions.delay(0.5f)
                                    )));
                            }
                            else
                            {
                                image = new Image(BrainOutClient.getRegion("icon-chip-none"));
                            }
                        }

                        statContainer.add(image).pad(4);
                    }

                    item.add(new BorderActor(statContainer)).expandY().fillY();

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
