package com.desertkun.brainout.mode;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.playstate.PlayState;

public class ClientDeathmatchRealization extends ClientGameRealization<GameModeDeathmatch>
{
    public ClientDeathmatchRealization(GameModeDeathmatch gameMode)
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

        if (stats != null)
        {
            CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

            if (game != null)
            {
                Table item = new Table();

                int points = getGameMode().getTickets();
                String value = String.valueOf(points);

                Table statContainer = new Table();

                TextureAtlas.AtlasRegion tickets = BrainOutClient.getRegion("icon-tickets");

                if (tickets != null)
                    statContainer.add(new Image(tickets)).padRight(8);

                Label statValue = new Label(value, BrainOutClient.Skin, "title-friend");
                statValue.setAlignment(Align.center);

                statContainer.add(statValue);

                item.add(new BorderActor(statContainer, 64)).expandY().fillY();

                ProgressBar progress = new ProgressBar(0, getGameMode().getInitialTickets(), 1, false,
                        BrainOutClient.Skin, "progress-friend");
                progress.setValue(points);

                BorderActor progressBorder = new BorderActor(progress, 96);
                progressBorder.getCell().padLeft(2).padRight(2);
                item.add(progressBorder).expandY().fillY();

                stats.add(item).expandX().left().row();
            }
        }
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
