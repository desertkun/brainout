package com.desertkun.brainout.mode;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.Notify;
import com.desertkun.brainout.playstate.PlayState;

import java.util.Comparator;

public class ClientZombieRealization extends ClientGameRealization<GameModeZombie<ClientZombieRealization>>
{
    private int currentWave, waveCount, zombiesAmount, zombiesSpawned, zombiesAlive;
    private ObjectSet<Integer> playersAlive;
    private boolean waveActive;
    private Team zombieTeam;

    public ClientZombieRealization(GameModeZombie<ClientZombieRealization> gameMode)
    {
        super(gameMode);

        playersAlive = new ObjectSet<>();
    }

    @Override
    public boolean autoOpenShopOnSpawn()
    {
        return true;
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

        zombieTeam = BrainOutClient.ContentMgr.get(jsonData.getString("zombieTeam"), Team.class);
        currentWave = jsonData.getInt("currentWave");
        waveCount = jsonData.getInt("waveCount");
        waveActive = jsonData.getBoolean("waveActive");
        zombiesAmount = jsonData.getInt("zombiesAmount", 0);
        zombiesSpawned = jsonData.getInt("zombiesSpawned", 0);
        zombiesAlive = jsonData.getInt("zombiesAlive", 0);

        playersAlive.clear();

        for (JsonValue alive : jsonData.get("playersAlive"))
        {
            playersAlive.add(alive.asInt());
        }
    }

    @Override
    protected void updated()
    {
        super.updated();

        updateStats();
    }

    private int countPlayersAlive()
    {
        return playersAlive.size;
    }

    @Override
    protected void updateStats()
    {
        super.updateStats();

        if (!getGameMode().isGameActive(false, false))
            return;

        updateCurrentState();

        if (stats != null)
        {
            CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

            if (game != null)
            {
                Table item = new Table();

                if (waveActive)
                {
                    Table row = new Table();

                    {
                        String v = L.get("MENU_WAVE") + ": " + (currentWave + 1) + " / " + (waveCount);
                        Label l = new Label(v, BrainOutClient.Skin, "title-yellow");
                        row.add(l).padRight(8);
                    }
                    {
                        String v = L.get("MENU_FREEPLAY_ALIVE", String.valueOf(countPlayersAlive()));
                        Label l = new Label(v, BrainOutClient.Skin, "title-small");
                        row.add(l);
                    }

                    item.add(row).row();
                }

                stats.add(item).expandX().center().padBottom(64).padTop(-64).row();
            }
        }
    }

    protected void fetchAvatar(String avatar, Table avatarInfo)
    {
        Avatars.Get(avatar, (has, avatarTexture) ->
        {
            if (has)
            {
                Image avatarImage = new Image(avatarTexture);
                avatarImage.setScaling(Scaling.fit);
                avatarInfo.add(avatarImage).size(40, 40).row();
            }
        });
    }

    private void updateCurrentState()
    {
        if (topStats != null)
        {
            if (waveActive)
            {
                Table players = new Table();

                Array<RemoteClient> clients = new Array<>();
                for (ObjectMap.Entry<Integer, RemoteClient> entry : BrainOutClient.ClientController.getRemoteClients())
                {
                    clients.add(entry.value);
                }

                clients.sort((o1, o2) ->
                {
                    int a1 = playersAlive.contains(o1.getId()) ? 1 : 0;
                    int a2 = playersAlive.contains(o2.getId()) ? 1 : 0;

                    return a1 - a2;
                });

                for (RemoteClient remoteClient : clients)
                {
                    boolean alive = playersAlive.contains(remoteClient.getId());

                    Table player = new Table(BrainOutClient.Skin);
                    player.setBackground(alive ? "form-default" : "form-red");

                    if (!remoteClient.getAvatar().isEmpty())
                    {
                        Table avatarInfo = new Table();
                        fetchAvatar(remoteClient.getAvatar(), avatarInfo);
                        player.add(avatarInfo);
                    }
                    else
                    {
                        Image def = new Image(BrainOutClient.Skin, "default-avatar");
                        def.setScaling(Scaling.fit);
                        player.add(def).size(40, 40);
                    }

                    players.add(player).size(44).pad(2);
                }

                topStats.add(players).padTop(10);
            }
            else
            {
                Table counter = new Table();

                Image notice = new Image(BrainOutClient.getRegion("label-warmup"));
                counter.add(notice).padBottom(-72).row();

                Label title = new Label(L.get("MENU_NEXT_WAVE_TIMER"),
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
