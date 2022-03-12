package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.client.states.CSReconnect;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.common.msg.client.CurrentlyWatchingMsg;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.gs.ActionPhaseState;
import com.desertkun.brainout.gs.EndGameState;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.gs.LoadingState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;

import java.util.Comparator;

public class WatchSpectatorMenu extends Menu implements EventReceiver
{
    private Table rightBottomPanel;
    private float counter;

    public WatchSpectatorMenu()
    {
        this.counter = 0;

        spectateNext();
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        this.rightBottomPanel = new Table();
        data.add(rightBottomPanel).pad(10).expand().right().bottom().row();

        return data;
    }

    @Override
    public boolean escape()
    {
        pushMenu(new ExitMenu(false));

        return true;
    }

    private class PlayerSpectator implements Watcher
    {
        private final PlayerData playerData;
        private final ClientPlayerComponent cpc;
        private Vector2 offset;

        public PlayerSpectator(PlayerData playerData)
        {
            this.playerData = playerData;
            this.cpc = playerData.getComponent(ClientPlayerComponent.class);
            this.offset = new Vector2();
        }

        public PlayerData getPlayerData()
        {
            return playerData;
        }

        private void updateOffset()
        {
            offset.set(cpc.getMouseOffset());

            float h = (Math.min(BrainOutClient.getWidth(),
                    BrainOutClient.getHeight()) / Constants.Graphics.RES_SIZE) * 0.4f;

            if (h > offset.len())
            {
                offset.set(0, 0);
            }
            else
            {
                offset.setLength(offset.len() - h);
            }
        }
        @Override
        public float getWatchX()
        {
            updateOffset();

            return playerData.getX() + offset.x;
        }

        @Override
        public float getWatchY()
        {
            updateOffset();

            return playerData.getY() + offset.y;
        }

        @Override
        public boolean allowZoom()
        {
            return false;
        }

        @Override
        public float getScale()
        {
            return 1;
        }

        @Override
        public String getDimension()
        {
            return playerData.getDimension();
        }
    }

    private void spectateNext()
    {
        PlayerData current;

        if (Map.GetWatcher() instanceof PlayerSpectator)
        {
            PlayerSpectator spectating = ((PlayerSpectator) Map.GetWatcher());
            current = spectating.getPlayerData();
        }
        else
        {
            current = null;
        }

        Array<PlayerData> players = new Array<>();

        for (Map map : Map.All())
        {
            map.countActivesForTag(Constants.ActiveTags.PLAYERS, activeData ->
            {
                if (!(activeData instanceof PlayerData))
                    return false;

                if (activeData.getOwnerId() >= 0 && activeData.isVisible())
                {
                    players.add(((PlayerData) activeData));
                }

                return true;
            });
        }

        if (players.size == 0)
        {
            if (current != null)
            {
                reportWatching(current.getOwnerId());
                Map.SetWatcher(new Watcher()
                {
                    @Override
                    public float getWatchX()
                    {
                        return current.getX();
                    }

                    @Override
                    public float getWatchY()
                    {
                        return current.getY();
                    }

                    @Override
                    public boolean allowZoom()
                    {
                        return false;
                    }

                    @Override
                    public float getScale()
                    {
                        return 1.0f;
                    }

                    @Override
                    public String getDimension()
                    {
                        return current.getDimension();
                    }
                });
            }

            return;
        }

        players.sort(Comparator.comparingInt(ActiveData::getOwnerId));

        PlayerData next;

        if (current == null)
        {
            next = players.first();
        }
        else
        {
            int index = players.indexOf(current, true);

            if (index >= 0)
            {
                index++;
                if (index >= players.size)
                {
                    index = 0;
                }
                next = players.get(index);
            }
            else
            {
                next = players.first();
            }
        }

        reportWatching(next.getOwnerId());
        Map.SetWatcher(new PlayerSpectator(next));
    }

    private void reportWatching(int ownerId)
    {
        BrainOutClient.ClientController.sendTCP(new CurrentlyWatchingMsg(ownerId));
    }

    private void updateSpectatorInfo()
    {
        rightBottomPanel.clear();

        Label mode = new Label(L.get("MENU_SPECTATING_MODE"), BrainOutClient.Skin, "title-yellow");
        mode.setAlignment(Align.center);
        rightBottomPanel.add(mode).expandX().fillX().row();

        if (Map.GetWatcher() instanceof PlayerSpectator)
        {
            PlayerSpectator spectating = ((PlayerSpectator) Map.GetWatcher());
            PlayerData playerData = spectating.getPlayerData();

            if (playerData.isAlive())
            {
                int ownerId = playerData.getOwnerId();
                RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(ownerId);

                if (remoteClient != null)
                {
                    Label who = new Label(remoteClient.getName(), BrainOutClient.Skin, "title-small");

                    who.setAlignment(Align.center);
                    who.setWrap(true);
                    rightBottomPanel.add(who).expandX().fillX().row();
                }
            }
        }

        final TextButton next = new TextButton(
            L.get("MENU_NEXT"), BrainOutClient.Skin, "button-default");

        next.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);

                spectateNext();
                updateSpectatorInfo();
            }
        });

        rightBottomPanel.add(next).size(192, 64).padTop(10).expandX().fillX();
    }

    private boolean isSpectatorFlag()
    {
        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
        return csGame == null || csGame.isSpectator();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                SimpleEvent e = (SimpleEvent)event;

                switch (e.getAction())
                {
                    case teamUpdated:
                    {
                        if (!(BrainOutClient.ClientController.getTeam() instanceof SpectatorTeam) && !isSpectatorFlag())
                        {
                            switchToSpawnMenu();
                            break;
                        }

                        return true;
                    }
                }

                break;
            }

            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case openPlayerList:
                    {
                        if (BrainOutClient.ClientController.canSeePlayerList())
                        {
                            pushMenu(new PlayerListMenu());
                        }
                    }
                }
                break;
            }

            case spectatorFlag:
            {
                SpectatorFlagEvent e = ((SpectatorFlagEvent) event);

                if (!e.flag && !isSpectatorFlag())
                {
                    switchToSpawnMenu();
                }

                break;
            }
            case csGame:
            {
                switch (((CSGameEvent) event).state)
                {
                    case spawned:
                    {
                        spawned();

                        return true;
                    }
                }

                break;
            }
        }

        return false;
    }

    private void switchToSpawnMenu()
    {
        if (getGameState() instanceof ActionPhaseState)
        {
            ActionPhaseState s = ((ActionPhaseState) getGameState());
            s.showSpawnMenu();
        }
    }

    private void spawned()
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        if (gs.topMenu() instanceof OnlineEventMenu)
        {
            // wtf
            pop();
        }

        pop();
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.spectatorFlag, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.csGame, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.gameController, this);
    }

    @Override
    public void onInit()
    {
        super.onInit();

        updateSpectatorInfo();

        BrainOut.EventMgr.subscribe(Event.ID.spectatorFlag, this);
        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
        BrainOut.EventMgr.subscribe(Event.ID.csGame, this);
        BrainOut.EventMgr.subscribe(Event.ID.gameController, this);
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        counter -= delta;

        if (counter < 0)
        {
            counter = 2f;

            if (Map.GetWatcher() instanceof PlayerSpectator)
            {
                PlayerSpectator playerSpectator = ((PlayerSpectator) Map.GetWatcher());

                PlayerData playerData = playerSpectator.getPlayerData();

                if (!playerData.isAlive() || !playerData.isVisible())
                {
                    spectateNext();
                    updateSpectatorInfo();
                }
            }
            else
            {
                spectateNext();
                updateSpectatorInfo();
            }
        }
    }
}
