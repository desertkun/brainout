package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.states.CSEndGame;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.common.msg.client.MapVotedMsg;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.events.NotifyEvent;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Notifications;
import com.desertkun.brainout.menu.ui.ProfileBadgeWidget;
import com.desertkun.brainout.menu.widgets.chat.InGameChatWidget;
import com.desertkun.brainout.playstate.ClientPSEndGame;
import com.desertkun.brainout.playstate.PlayStateEndGame;

import java.util.HashSet;

public class EndGameMenu extends PlayerListMenu
{
    private final ClientPSEndGame endGame;
    private float updateCounter;
    private Label nextRoundIn;
    private InGameChatWidget chat;

    private Array<Button> voteMapsButtons;
    private Label votesCounter;
    private float voteMapsRouletteCounter;
    private int currentRouletteMap;
    private Array<Integer> indexesOfMostVotedMaps;
    private boolean isWinningMapSelected;

    public EndGameMenu(ClientPSEndGame endGame)
    {
        this.endGame = endGame;
        this.updateCounter = 0;

        this.voteMapsButtons = new Array<>();
        this.voteMapsRouletteCounter = 0;
        this.currentRouletteMap = 0;
        this.isWinningMapSelected = false;

        chat = new InGameChatWidget(10, 10, ClientConstants.Menu.Chat.WIDTH, ClientConstants.Menu.Chat.HEIGHT);
    }

    @Override
    public boolean stayOnTop()
    {
        return false;
    }

    @Override
    protected CSGame getGame()
    {
        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);

        if (csGame != null)
        {
            return csGame;
        }

        CSEndGame csEndGame = BrainOutClient.ClientController.getState(CSEndGame.class);
        return csEndGame.getCsGame();
    }

    @Override
    protected boolean showPing()
    {
        return false;
    }

    @Override
    protected void subRelease()
    {
        //
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-loading");
    }

    @Override
    public boolean escape()
    {
        pushMenu(new ExitMenu());
        return true;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        CSGame csGame = getGame();

        if (csGame != null)
        {
            PlayStateEndGame.GameResult gameResult = endGame.getGameResult();

            Array<PlayStateEndGame.VoteMap> votesMaps = endGame.getVotesMaps();

            if (votesMaps != null)
            {
                final int columntSize = 100;
                final int padleft = 8;

                final int mapLogoSize = 160;

                Table votesMapTable = new Table();
                votesMapTable.align(Align.top);

                Table title = new Table();

                Label TitleLabel = new Label(L.get("VOTING_SELECT_MAP"), BrainOutClient.Skin, "title-small");
                title.add(TitleLabel).padLeft(padleft).expandX().fillX();


                votesCounter = new Label("0/0", BrainOutClient.Skin, "player-list");
                votesCounter.setAlignment(Align.center);
                title.add(votesCounter).width(columntSize);

                BorderActor titleBorder = new BorderActor(title, 0, "form-white",
                        ClientConstants.Menu.KillList.FRIEND_COLOR);
                titleBorder.getCell().expandX().fillX();
                votesMapTable.add(titleBorder).expandX().fillX().row();

                Table votesMapsList = new Table();

                for (int i = 0; i < votesMaps.size; i++)
                {
                    PlayStateEndGame.VoteMap voteMap = votesMaps.get(i);

                    Button btn = new Button(BrainOutClient.Skin, "button-notext");

                    voteMapsButtons.add(btn);

                    {
                        final int logoPadding = 4;

                        Image image = new Image(BrainOutClient.Skin, "map-" + voteMap.mapName);
                        image.setSize(mapLogoSize - logoPadding * 2, mapLogoSize - logoPadding * 2);
                        image.setPosition(logoPadding, logoPadding);
                        image.setTouchable(Touchable.disabled);
                        btn.addActor(image);
                    }

                    {
                        Label votesCount = new Label(Integer.toString(voteMap.votes),
                                BrainOutClient.Skin, "title-medium");
                        votesCount.setName("votesCount");
                        votesCount.setAlignment(Align.top | Align.center);
                        votesCount.setFillParent(true);
                        votesCount.setTouchable(Touchable.disabled);
                        votesCount.setY(-7);
                        btn.addActor(votesCount);
                    }

                    {
                        Label mapName = new Label(L.get("MAP_" + voteMap.mapName.toUpperCase()),
                                BrainOutClient.Skin, "title-yellow");
                        mapName.setAlignment(Align.bottom | Align.center);
                        mapName.setFillParent(true);
                        mapName.setTouchable(Touchable.disabled);
                        mapName.setY(25);
                        btn.addActor(mapName);
                    }

                    {
                        Label mapMode = new Label(L.get("MODE_" + voteMap.mapMode.name().toUpperCase()),
                                BrainOutClient.Skin, "title-medium");
                        mapMode.setAlignment(Align.bottom | Align.center);
                        mapMode.setFillParent(true);
                        mapMode.setTouchable(Touchable.disabled);
                        mapMode.setY(7);
                        btn.addActor(mapMode);
                    }

                    btn.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            HashSet<Integer> votedPlayers = endGame.getVotedPlayers();

                            boolean isVotingTime = endGame.getRestartIn() > endGame.VOTING_RESULTS_TIME;
                            boolean isVotingPossible = votedPlayers == null ||
                                    !votedPlayers.contains(BrainOutClient.ClientController.getMyId());

                            if (isVotingTime && isVotingPossible)
                            {
                                Menu.playSound(MenuSound.select);
                                BrainOutClient.ClientController
                                        .sendTCP(new MapVotedMsg(votesMaps.indexOf(voteMap, true)));
                            }
                            else
                            {
                                Menu.playSound(MenuSound.denied);
                            }
                        }
                    });

                    votesMapsList.add(btn).size(mapLogoSize, mapLogoSize).pad(4);
                }

                votesMapTable.add(votesMapsList).expandX().fillX().row();
                data.add(votesMapTable).row();

                updateVotes();
            }

            if (gameResult.hasTeamWon())
            {
                {
                    RemoteClient bestClient = null;

                    for (ObjectMap.Entry<Integer, RemoteClient> entry :
                        BrainOutClient.ClientController.getRemoteClients())
                    {
                        RemoteClient remoteClient = entry.value;

                        if (remoteClient.getTeam() != gameResult.getTeamWon())
                            continue;

                        if (bestClient == null || remoteClient.getScore() > bestClient.getScore())
                        {
                            bestClient = remoteClient;
                        }
                    }

                    if (bestClient != null)
                    {
                        Table header = new Table();
                        Label title = new Label(L.get("MENU_BEST_OF_THE_BEST"), BrainOutClient.Skin, "title-small");
                        header.add(title).pad(8).row();

                        if (renderPlayerBadge(header, bestClient))
                        {
                            data.add(header).expandX().center().pad(8).row();
                        }
                    }
                }

                Label wonLost = new Label(L.get(
                        csGame.getTeam() == gameResult.getTeamWon() ? "MENU_YOU_WON" : "MENU_YOU_LOST"
                ), BrainOutClient.Skin, "title-messages-white");
                wonLost.setAlignment(Align.center);
                data.add(wonLost).expandX().fillX().pad(8).row();
            }
            else
            if (gameResult.hasPlayerWon())
            {
                RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(gameResult.getPlayerWon());

                if (remoteClient != null)
                {
                    String name = remoteClient.getName();
                    if (name.length() > 20)
                    {
                        name = name.substring(0, 20) + "...";
                    }

                    Label wonLost = new Label(L.get(
                        "MENU_PLAYER_WON",  name
                    ), BrainOutClient.Skin, "title-messages-white");
                    wonLost.setAlignment(Align.center);
                    data.add(wonLost).expandX().fillX().pad(8).row();

                    Table wonBadge = new Table();
                    if (renderPlayerBadge(wonBadge, remoteClient))
                    {
                        data.add(wonBadge).expandX().center().pad(8).row();
                    }
                }
            }

        }

        Table stats = new Table();

        Label endOfTheRound = new Label(L.get("MENU_END_ROUNT"),
            BrainOutClient.Skin, "title-messages-white");
        endOfTheRound.setAlignment(Align.left);
        stats.add(endOfTheRound).expandX().fill().pad(16);

        this.nextRoundIn = new Label("",
                BrainOutClient.Skin, "title-messages-white");
        nextRoundIn.setAlignment(Align.right);
        stats.add(nextRoundIn).expandX().fill().pad(16).row();

        data.add(stats).expand().fill().row();

        Table players = super.createUI();
        data.add(players).expand().fill();

        return data;
    }

    private boolean renderPlayerBadge(Table table, RemoteClient remoteClient)
    {
        InstrumentInfo instrumentInfo = endGame.getGameResult().getPlayerInstruments().get(remoteClient.getId());

        ProfileBadgeWidget widget = new ProfileBadgeWidget(
            remoteClient.getName(), BrainOutClient.ClientController.getColorOf(remoteClient),
            remoteClient.getInfoString(Constants.User.PROFILE_BADGE, Constants.User.PROFILE_BADGE_DEFAULT),
            remoteClient.getAvatar(), remoteClient.getLevel(), -1, instrumentInfo);

        table.add(widget).size(384, 112).row();

        return true;
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        updateCounter -= delta;

        if (updateCounter < 0)
        {
            updateCounter = 1f;

            updateRestart();
        }

        if (endGame.getVotesMaps() != null && endGame.getRestartIn() < endGame.VOTING_RESULTS_TIME
                && indexesOfMostVotedMaps != null && !isWinningMapSelected)
        {
            if (indexesOfMostVotedMaps.size > 1)
            {
                voteMapsRouletteCounter -= delta;
                if (voteMapsRouletteCounter < 0)
                {
                    for (int i = 0; i < indexesOfMostVotedMaps.size; i++)
                    {
                        int mapIndex = indexesOfMostVotedMaps.get(i);

                        if (i == currentRouletteMap)
                            voteMapsButtons.get(mapIndex).setStyle(BrainOutClient.Skin.get("button-green",
                                    Button.ButtonStyle.class));
                        else
                            voteMapsButtons.get(mapIndex).setStyle(BrainOutClient.Skin.get("button-notext",
                                    Button.ButtonStyle.class));
                    }

                    if (endGame.getRestartIn() > endGame.VOTING_RESULTS_TIME - endGame.VOTING_ROULETTE_TIME
                            || currentRouletteMap != endGame.getWinningMapIndex())
                    {
                        currentRouletteMap++;
                        if (currentRouletteMap >= indexesOfMostVotedMaps.size) currentRouletteMap = 0;

                        Menu.playSound(MenuSound.select);

                        voteMapsRouletteCounter = 0.1f;
                    }
                    else
                    {
                        isWinningMapSelected = true;
                        Menu.playSound(MenuSound.character);
                    }
                }
            }
            else
            {
                int index = endGame.getWinningMapIndex();

                if (index < voteMapsButtons.size)
                {
                    voteMapsButtons.get(index).setStyle(BrainOutClient.Skin.get("button-green",
                            Button.ButtonStyle.class));
                }

                isWinningMapSelected = true;
                Menu.playSound(MenuSound.character);

            }
        }
    }

    private void updateVotes()
    {
        int votedPlayersCount = 0;
        int clientsCount = BrainOutClient.ClientController.getRemoteClients().size;

        if (endGame.getVotedPlayers() != null)
            votedPlayersCount = endGame.getVotedPlayers().size();

        if (votesCounter != null)
            votesCounter.setText(votedPlayersCount + "/" + clientsCount);

        if (voteMapsButtons.size == 0)
        {
            return;
        }

        for (int i = 0; i < endGame.getVotesMaps().size; i++)
        {
            Button g = voteMapsButtons.get(i);
            if (g == null)
                continue;
            Label votesCount = g.findActor("votesCount");
            if (votesCount == null)
                continue;
            PlayStateEndGame.VoteMap gg = endGame.getVotesMaps().get(i);
            if (gg == null)
                continue;
            votesCount.setText(gg.votes);
        }
    }

    private void updateRestart()
    {
        if (endGame.getRestartIn() > 0)
        {
            if (endGame.getVotesMaps() != null && endGame.getRestartIn() > endGame.VOTING_RESULTS_TIME)
                nextRoundIn.setText(L.get("VOTING_END_IN",
                        String.valueOf((int) endGame.getRestartIn() - endGame.VOTING_RESULTS_TIME)));
            else
                nextRoundIn.setText(L.get("MENU_NEXT_ROUND_IN",
                    String.valueOf((int) endGame.getRestartIn())));
        }
        else
        {
            nextRoundIn.setVisible(false);
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case back:
                    {
                        pushMenu(new ExitMenu());

                        return true;
                    }
                }

                break;
            }
            case notify:
            {
                Notifications.AddNotification((NotifyEvent) event);

                break;
            }
            case playStateUpdated:
            {
                if (endGame.getVotesMaps() != null && endGame.getRestartIn() < endGame.VOTING_RESULTS_TIME)
                {
                    int bestScore = 0;

                    for (int i = 0; i < endGame.getVotesMaps().size; i++)
                    {
                        if (endGame.getVotesMaps().get(i).votes > bestScore)
                            bestScore = endGame.getVotesMaps().get(i).votes;
                    }

                    indexesOfMostVotedMaps = new Array<>();

                    for (int i = 0; i < endGame.getVotesMaps().size; i++)
                    {
                        if (endGame.getVotesMaps().get(i).votes == bestScore)
                            indexesOfMostVotedMaps.add(i);
                    }
                }

                updateVotes();
                break;
            }
        }
        return false;
    }

    @Override
    protected boolean hasTab()
    {
        return false;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        GameState gs = getGameState();

        if (gs != null)
        {
            gs.getWidgets().removeAll();
            gs.getWidgets().addWidget(chat);
        }

        BrainOut.EventMgr.subscribe(Event.ID.gameController, this);
        BrainOut.EventMgr.subscribe(Event.ID.notify, this);
        BrainOut.EventMgr.subscribe(Event.ID.playStateUpdated, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.gameController, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.notify, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.playStateUpdated, this);
    }
}
