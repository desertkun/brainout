package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.settings.KeyProperties;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.gs.ActionPhaseState;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.playstate.ClientPSGame;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateGame;

public class PlayerListMenu extends Menu implements EventReceiver
{
    protected final ClickOverListener clicked;
    private ObjectMap<Team, Array<RemoteClient>> clients;
    private Array<RemoteClient> spectators;
    private boolean done;

    public PlayerListMenu()
    {
        clients = new ObjectMap<>();
        spectators = new Array<>();
        done = false;

        this.clicked = new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                Object player = event.getTarget().getUserObject();

                if (player instanceof RemoteClient)
                {
                    playerClicked(((RemoteClient) player));
                }
            }
        };
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
        BrainOut.EventMgr.subscribe(Event.ID.gameController, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.gameController, this);

        subRelease();
    }

    protected void subRelease()
    {
        ActionPhaseState ps = ((ActionPhaseState) getGameState());

        if (ps == null)
            return;

        ps.getPostEffects().resetEffect();
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
                    case pingUpdated:
                    {
                        reset();

                        break;
                    }
                }

                break;
            }

            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case closePlayerList:
                    {
                        escape();
                        break;
                    }
                    case back:
                    {
                        escape();
                        break;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean escape()
    {
        if (done)
            return true;

        done();
        pop();

        return true;
    }

    @Override
    public boolean stayOnTop()
    {
        return !done;
    }

    @Override
    public Table createUI()
    {
        Table list = new Table();
        list.align(Align.center);

        Table content = new Table();

        initContent(content);

        ScrollPane items = new ScrollPane(content);

        list.add(items).pad(4).expand().fillX().maxHeight(BrainOutClient.getHeight() - 20f);

        return list;
    }

    protected boolean showPing()
    {
        return true;
    }

    protected CSGame getGame()
    {
        return BrainOutClient.ClientController.getState(CSGame.class);
    }

    protected Table generateTeam(Team team, Array<RemoteClient> clients)
    {
        final int columntSize = 80;
        final int padleft = 8;
        float levelScale = 0.25f;

        Table data = new Table();
        data.align(Align.top);

        Table title = new Table();

        Label TitleLabel = new Label(team.getTitle().get(), BrainOutClient.Skin, "title-small");
        title.add(TitleLabel).padLeft(padleft).expandX().fillX();

        Label KDTitle = new Label(L.get("MENU_PLAYER_KD"),
                BrainOutClient.Skin, "player-list");
        KDTitle.setAlignment(Align.center);
        title.add(KDTitle).width(columntSize);

        Label ScoreTitle = new Label(L.get("MENU_PLAYER_SCORE"),
                BrainOutClient.Skin, "player-list");
        title.add(ScoreTitle).width(columntSize);
        ScoreTitle.setAlignment(Align.center);

        if (showPing())
        {
            Label ping = new Label(L.get("MENU_PLAYER_PING"),
                    BrainOutClient.Skin, "player-list");
            ping.setAlignment(Align.center);
            title.add(ping).width(48);
        }

        ClientController CC = BrainOutClient.ClientController;

        BorderActor titleBorder = new BorderActor(title, 0, "form-white", CC.getColorOf(team));
        titleBorder.getCell().expandX().fillX();
        data.add(titleBorder).expandX().fillX().row();

        Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.LEVEL);

        for (RemoteClient remoteClient: clients)
        {
            Color color = CC.getColorOf(remoteClient);

            Table item = new Table();
            item.align(Align.left | Align.center);
            item.setTouchable(Touchable.disabled);

            if (!remoteClient.getClanAvatar().isEmpty())
            {
                Table avatarInfo = new Table();
                fetchAvatar(remoteClient.getClanAvatar(), avatarInfo);
                item.add(avatarInfo).padLeft(-2);
            }

            if (!remoteClient.getAvatar().isEmpty())
            {
                Table avatarInfo = new Table();

                fetchAvatar(remoteClient.getAvatar(), avatarInfo);

                item.add(avatarInfo).padLeft(-2);
            }
            else
            {
                Image def = new Image(BrainOutClient.Skin, "default-avatar");
                def.setScaling(Scaling.fit);
                item.add(def).size(40, 40).padLeft(-2);
            }

            Levels.Level level = levels.getLevel(remoteClient.getLevel());

            Label levelText = new Label(level.toString(), BrainOutClient.Skin, "player-list");
            levelText.setColor(color);
            levelText.setAlignment(Align.left);

            item.add(levelText).padLeft(4);

            TextureRegion levelImage = BrainOutClient.getRegion(level.icon);

            if (levelImage != null)
            {
                Image image = new Image(levelImage);
                item.add(image).size(levelImage.getRegionWidth() * levelScale,
                        levelImage.getRegionHeight() * levelScale).padLeft(4);
            }

            String showIds = Gdx.input.isButtonPressed(Input.Buttons.RIGHT) ?
                    ("[" + remoteClient.getId() + "] ") : "";

            Label clientName = new Label(showIds + remoteClient.getName(), BrainOutClient.Skin, "player-list");
            clientName.setColor(color);
            clientName.setAlignment(Align.left);
            clientName.setEllipsis(true);

            Tooltip.RegisterToolTip(clientName, "ID: " + remoteClient.getId(), this);

            item.add(clientName).padLeft(4).width(160).expandX().left();

            String KD = String.valueOf(remoteClient.getKills()) + " / " + String.valueOf(remoteClient.getDeaths());

            Label KDLabel = new Label(KD, BrainOutClient.Skin, "player-list");
            KDLabel.setAlignment(Align.center);

            item.add(KDLabel).width(columntSize);

            Label ScoreLabel = new Label(String.valueOf((int) remoteClient.getScore()),
                    BrainOutClient.Skin, "player-list");
            ScoreLabel.setAlignment(Align.center);

            item.add(ScoreLabel).width(columntSize);

            if (showPing())
            {
                Table latency = new Table();

                long ping = remoteClient.getPing();
                String pingImage = ping <= Constants.Core.PING_GOOD ? "ping-good" :
                        ping <= Constants.Core.PING_NORMAL ? "ping-normal" : "ping-bad";

                latency.add(new Image(BrainOutClient.Skin, pingImage)).pad(4, 0, 4, 0);

                Label clientPing = new Label(String.valueOf(ping), BrainOutClient.Skin, "player-list-ping");

                latency.add(clientPing).fillX().padLeft(2).row();

                item.add(latency).right().width(48).row();
            }

            Button itemBorder = new Button(BrainOutClient.Skin, "button-notext");
            itemBorder.setUserObject(remoteClient);

            itemBorder.add(item).expand().fill();

            itemBorder.addListener(clicked);

            data.add(itemBorder).height(48).expandX().fillX().row();
        }

        return data;
    }

    private void playerClicked(RemoteClient player)
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        if (hasTab())
        {
            close();
        }

        if (!BrainOut.OnlineEnabled())
            return;

        gs.pushMenu(new RemoteAccountMenu(player.getAccountId(), player.getCredential()));
    }

    @Override
    public boolean keyUp(int keyCode)
    {
        if (!hasTab())
            return false;

        KeyProperties.Keys keys = BrainOutClient.ClientSett.getControls().getKey(keyCode);

        if (keys == KeyProperties.Keys.playerList)
        {
            escape();
        }

        return true;
    }

    protected boolean hasTab()
    {
        return true;
    }

    protected void close()
    {
        done();
        pop();
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

    protected int getTeamWidth()
    {
        return 500;
    }

    private void initContent(Table content)
    {
        refreshTeams();

        for (ObjectMap.Entry<Team, Array<RemoteClient>> teamEntry: clients)
        {
            Table data = generateTeam(teamEntry.key, teamEntry.value);
                content.add(data).width(getTeamWidth()).expandY().fill();
        }

        content.row();

        if (spectators.size > 0)
        {
            String spectatingPlayers = spectators.toString(", ");

            Label spectating = new Label(L.get("MENU_SPECTATING_LIST", spectatingPlayers),
                    BrainOutClient.Skin, "player-list");
            spectating.setWrap(true);
            spectating.setAlignment(Align.left);

            content.add(spectating).colspan(clients.size).padTop(32).padLeft(8).expandX().fillX().row();
        }
    }

    private void refreshTeams()
    {
        CSGame game = getGame();

        if (game == null) return;

        ObjectMap<Integer, RemoteClient> remoteClients = BrainOutClient.ClientController.getRemoteClients();

        clients.clear();
        spectators.clear();

        for (Team team: game.getTeams())
        {
            if (team == null) continue;
            if (team instanceof SpectatorTeam) continue;

            clients.put(team, new Array<>());
        }

        for (ObjectMap.Entry<Integer, RemoteClient> entry: remoteClients)
        {
            RemoteClient remoteClient = entry.value;

            if (remoteClient.getTeam() != null)
            {
                if (remoteClient.getTeam() instanceof SpectatorTeam)
                {
                    spectators.add(remoteClient);
                }
                else
                {
                    Array<RemoteClient> cs = clients.get(remoteClient.getTeam());

                    if (cs != null)
                    {
                        cs.add(remoteClient);
                    }
                }
            }
        }

        for (Team team: game.getTeams())
        {
            if (team == null) continue;
            if (team instanceof SpectatorTeam) continue;

            Array<RemoteClient> r = clients.get(team);
            r.sort((o1, o2) ->
            {
                if (o1.getScore() > o2.getScore())
                {
                    return -1;
                }
                if (o1.getScore() < o2.getScore())
                {
                    return 1;
                }
                return 0;
            });
        }
    }

    public void done()
    {
        this.done = true;
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }
    }
}
