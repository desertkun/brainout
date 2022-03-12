package com.desertkun.brainout.mode;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.playstate.PlayState;

public class ClientGunGameRealization extends ClientGameRealization<GameModeGunGame<ClientGunGameRealization>>
{
    private ObjectMap<String, Integer> ranks;
    private ObjectMap<Integer, Integer> users;

    public ClientGunGameRealization(GameModeGunGame<ClientGunGameRealization> gameMode)
    {
        super(gameMode);

        ranks = new ObjectMap<>();
        users = new ObjectMap<>();
    }

    @Override
    public boolean autoOpenShopOnSpawn()
    {
        return false;
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

    public int getPlayersOfRank(int rank)
    {
        String weapon = getGameMode().getGunGameWeapons().get(rank);

        if (weapon == null)
            return 0;

        return ranks.get(weapon, 0);
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

        ranks.clear();
        users.clear();

        for (JsonValue value : jsonData.get("ranks"))
        {
            int rank = value.asInt();

            String weapon = getGameMode().getGunGameWeapons().get(rank);

            if (weapon != null)
            {
                ranks.put(weapon, ranks.get(weapon, 0) + 1);
            }

            try
            {
                users.put(Integer.valueOf(value.name()), value.asInt());
            }
            catch (NumberFormatException ignored)
            {
                //
            }
        }
    }

    public int getMyRank()
    {
        return users.get(BrainOutClient.ClientController.getMyId(), 0);
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

        if (!getGameMode().isGameActive())
            return;

        if (stats != null)
        {
            CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

            if (game != null)
            {
                Table item = new Table();
                int myRank = getMyRank();

                for (int rank = 0, t = getGameMode().getGunGameWeapons().size; rank < t; rank++)
                {
                    Table entry = new Table(BrainOutClient.Skin);
                    entry.setBackground(myRank == rank ? "button-green-normal" : "form-default");

                    if (getPlayersOfRank(rank) > 0)
                    {
                        Image img = new Image(BrainOutClient.Skin, "rank-item");
                        img.setScaling(Scaling.none);
                        entry.add(img);
                    }

                    item.add(entry).size(32, 16).left();
                }

                stats.add(item).expandX().center().padBottom(64).padTop(-64).row();
            }
        }
    }

    @Override
    protected Cell<Table> renderTimeLeft(Table container)
    {
        Cell<Table> cell = super.renderTimeLeft(container);

        if (cell != null)
        {
            cell.padBottom(-16);
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
