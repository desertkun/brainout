package com.desertkun.brainout.mode;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.playstate.PlayState;

public class ClientFoxHuntRealization extends ClientGameRealization<GameModeFoxHunt>
{
    private int currentFox;

    public ClientFoxHuntRealization(GameModeFoxHunt gameMode)
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
    public boolean autoOpenShopOnSpawn()
    {
        return false;
    }

    @Override
    public boolean doDisplayPlayerBadge(PlayerData playerData)
    {
        return playerData.getOwnerId() == currentFox;
    }

    @Override
    public boolean canDropConsumable(ConsumableRecord record)
    {
        return false;
    }

    @Override
    public void release()
    {
        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        currentFox = jsonData.getInt("fox", -1);
    }

    public RemoteClient getFox()
    {
        return BrainOutClient.ClientController.getRemoteClients().get(currentFox);
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
    protected Cell<Table> renderTimeLeft(Table container)
    {
        Cell<Table> cell = super.renderTimeLeft(container);

        RemoteClient fox = getFox();

        if (fox != null)
        {
            container.row();

            Label currentFox = new Label(L.get("MENU_CURRENT_HUNTER", fox.getName()),
                    BrainOutClient.Skin, "title-yellow");
            container.add(currentFox).left().padLeft(8).padTop(4);
        }

        return cell;
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
