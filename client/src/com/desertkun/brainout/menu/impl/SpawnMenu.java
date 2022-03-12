package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.settings.KeyProperties;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.shop.*;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateGame;

import java.util.Comparator;

public class SpawnMenu extends PlayerSelectionMenu implements EventReceiver, Watcher
{
    private final Spawn spawnAction;
    private Spawnable spawnSelection;
    private SpawnState spawnState;
    private Table spawnContent;

    private ProgressBar spawnProgressBar;
    private ButtonGroup<TextButton> spawnButtons;
    private Vector2 vMove;
    private float counter;

    public enum SpawnState
    {
        notReady,
        ready,
        impossible
    }

    private Table spawnPointsContent;

    public interface Spawn
    {
        void ready(Spawnable spawnable);
        void selected(Spawnable spawnable);
        void notReady();
        void spawned();
    }

    public SpawnMenu(ShopCart shopCart, Spawn spawn, Spawnable lastSpawnPoint)
    {
        super(shopCart);

        this.spawnAction = spawn;
        this.spawnSelection = lastSpawnPoint;
        this.spawnState = SpawnState.notReady;
        this.vMove = new Vector2();
        this.counter = 0;

        if (spawnSelection == null)
        {
            PlayState ps = BrainOutClient.ClientController.getPlayState();

            if (ps instanceof PlayStateGame)
            {
                PlayStateGame playStateGame = ((PlayStateGame) ps);
                GameMode gameMode = playStateGame.getMode();

                for (Map map : Map.All())
                {
                    for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.SPAWNABLE, false))
                    {
                        if (activeData instanceof Spawnable)
                        {
                            Spawnable asSpawnable = ((Spawnable) activeData);

                            if (gameMode.canSpawn(asSpawnable, BrainOutClient.ClientController.getTeam()))
                            {
                                setSpawn(asSpawnable);
                            }
                        }
                    }
                }

            }
        }

        Map.SetWatcher(this);
    }

    private void setSpawn(Spawnable spawnable)
    {
        this.spawnSelection = spawnable;

        spawnAction.selected(spawnable);

        if (spawnSelection != null)
        {
            BrainOutClient.ClientController.setWatchingPoint(spawnSelection.getSpawnX(), spawnSelection.getSpawnY());
        }
    }

    @Override
    public Table createUI()
    {
        this.spawnContent = new Table();
        this.spawnPointsContent = new Table();

        return super.createUI();
    }

    private void updateSpawnInfo()
    {
        spawnContent.clear();

        switch (spawnState)
        {
            case notReady:
            {
                final TextButton readyButton = new TextButton(
                    L.get("MENU_READY"), BrainOutClient.Skin, "button-default");

                readyButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        playSound(MenuSound.select);

                        setReady();
                    }
                });

                spawnContent.add(readyButton).size(192, 64).expandX().fillX();

                break;
            }

            case ready:
            {
                this.spawnProgressBar = new ProgressBar(0, 1, 1, false, BrainOutClient.Skin, "progress-spawn");

                spawnContent.add(new BorderActor(spawnProgressBar)).expandX().fillX().row();

                final TextButton notReadyButton = new TextButton(L.get("MENU_PLEASE_WAIT"),
                        BrainOutClient.Skin, "button-default");

                notReadyButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        playSound(MenuSound.select);

                        setState(SpawnMenu.SpawnState.notReady);
                    }
                });

                spawnContent.add(notReadyButton).size(192, 64).expandX().fillX();

                break;
            }
        }
    }

    private void setReady()
    {
        setState(SpawnState.ready);
    }

    private void setState(SpawnState state)
    {
        switch (state)
        {
            case ready:
            {
                getUserPanel().disable(true);

                if (!ready())
                {
                    return;
                }

                break;
            }
            case notReady:
            {
                getUserPanel().disable(false);
                spawnAction.notReady();

                break;
            }
        }

        this.spawnState = state;
        updateSpawnInfo();
    }

    private void updateSpawnPointsInfo()
    {
        spawnPointsContent.clear();

        Label title = new Label(L.get("MENU_SPAWN_POINT"),
            BrainOutClient.Skin, "title-level");

        title.setAlignment(Align.center);

        spawnPointsContent.add(new BorderActor(title, "form-gray")).size(192, 64).expandX().fillX().row();
        spawnButtons = new ButtonGroup<>();

        GameMode gameMode = BrainOutClient.ClientController.getGameMode();

        if (spawnSelection != null && gameMode != null && !gameMode.canSpawn(spawnSelection,
            BrainOutClient.ClientController.getTeam()))
        {
            spawnSelection = null;
        }

        if (gameMode != null)
        {
            for (Map map : Map.All())
            {
                for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.SPAWNABLE, false))
                {
                    if (activeData instanceof Spawnable)
                    {
                        final Spawnable asSpawnable = ((Spawnable) activeData);

                        if (gameMode.canSpawn(asSpawnable, BrainOutClient.ClientController.getTeam()))
                        {
                            TextButton spawnButton = new TextButton(asSpawnable.toString(),
                                    BrainOutClient.Skin, "button-checkable");

                            spawnPointsContent.add(spawnButton).width(192).expandX().fillX().row();

                            spawnButtons.add(spawnButton);

                            if (spawnSelection == null)
                            {
                                setSpawn(asSpawnable);
                            }

                            if (asSpawnable == spawnSelection)
                            {
                                spawnButton.setChecked(true);
                            }

                            spawnButton.setUserObject(asSpawnable);

                            spawnButton.addListener(new ClickOverListener()
                            {
                                @Override
                                public void clicked(InputEvent event, float x, float y)
                                {
                                    playSound(MenuSound.select);

                                    updateSpawnSelection(asSpawnable);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    private void updateSpawnSelection(Spawnable asSpawnable)
    {
        if (spawnSelection != asSpawnable)
        {
            setSpawn(asSpawnable);

            if (spawnState == SpawnState.ready)
            {
                setState(SpawnState.notReady);
            }
        }
    }

    @Override
    protected void updateInfo()
    {
        super.updateInfo();

        updateSpawnInfo();
        updateSpawnPointsInfo();
    }

    @Override
    protected void setUpRightPanel(Table rightContent)
    {
        super.setUpRightPanel(rightContent);

        rightContent.add(spawnPointsContent).padRight(8).width(192).expandX().fillX().bottom().right().row();
        rightContent.add(spawnContent).pad(0, 8, 8, 8).expandX().fillX().bottom().row();
    }

    private boolean ready()
    {
        closeSlotInfo();

        if (canSpawn())
        {
            spawnAction.ready(spawnSelection);

            return true;
        }

        return false;
    }

    private boolean canSpawn()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case spectatorFlag:
            {
                SpectatorFlagEvent e = ((SpectatorFlagEvent) event);

                if (e.flag)
                {
                    popMeAndPushMenu(new WatchSpectatorMenu());
                }

                break;
            }
            case simple:
            {
                SimpleEvent simpleEvent = ((SimpleEvent) event);

                switch (simpleEvent.getAction())
                {
                    case invalidSpawn:
                    {
                        setState(SpawnState.notReady);
                        updateSpawnPointsInfo();

                        break;
                    }
                    case updateSpawn:
                    {
                        if (BrainOutClient.ClientController.getTeam() instanceof SpectatorTeam)
                        {
                            popMeAndPushMenu(new WatchSpectatorMenu());
                            break;
                        }

                        updateSpawnPointsInfo();

                        break;
                    }
                    case teamUpdated:
                    {
                        if (BrainOutClient.ClientController.getTeam() instanceof SpectatorTeam)
                        {
                            popMeAndPushMenu(new WatchSpectatorMenu());
                            break;
                        }

                        updateSpawnPointsInfo();

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
                    case beginLaunch:
                    {
                        setReady();

                        break;
                    }
                    case move:
                    {
                        vMove.set(gcEvent.data);

                        break;
                    }
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
            case respawnIn:
            {
                respawnIn(((RespawnTimeEvent) event).time);

                return true;
            }
        }

        return super.onEvent(event);
    }

    private void respawnIn(float time)
    {
        if (spawnProgressBar != null)
        {
            spawnProgressBar.setRange(0, 1.0f);
            spawnProgressBar.setValue(0);

            spawnProgressBar.setAnimateDuration(time);
            spawnProgressBar.setValue(1.0f);
        }
    }

    private void spawned()
    {
        spawnAction.spawned();

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
    public float getWatchX()
    {
        return spawnSelection != null ? spawnSelection.getSpawnX() : 0;
    }

    @Override
    public float getWatchY()
    {
        return spawnSelection != null ? spawnSelection.getSpawnY() : 0;
    }

    @Override
    public boolean allowZoom()
    {
        return false;
    }

    @Override
    public float getScale()
    {
        return 1.5f;
    }

    @Override
    public String getDimension()
    {
        if (spawnSelection != null)
            return spawnSelection.getDimension();

        return "default";
    }

    @Override
    protected void notReady()
    {
        setState(SpawnMenu.SpawnState.notReady);
    }

    @Override
    public boolean keyDown(int keycode)
    {
        KeyProperties.Keys action = BrainOutClient.ClientSett.getControls().getKey(keycode);

        if (action != null)
        {
            switch (action)
            {
                case up:
                {
                    selectPrevSpawn();
                    break;
                }
                case down:
                {
                    selectNextSpawn();
                    break;
                }
            }
        }

        switch (keycode)
        {
            case Input.Keys.SPACE:
            case Input.Keys.ENTER:
            {
                setReady();

                return true;
            }
        }

        return super.keyDown(keycode);
    }

    private void selectPrevSpawn()
    {
        TextButton checked = spawnButtons.getChecked();

        if (checked != null)
        {
            int index = spawnButtons.getButtons().indexOf(checked, true);

            if (index > 0)
            {
                TextButton next = spawnButtons.getButtons().get(index - 1);
                Spawnable asSpawnable = ((Spawnable) next.getUserObject());
                next.setChecked(true);
                updateSpawnSelection(asSpawnable);
            }
        }
    }

    private void selectNextSpawn()
    {
        TextButton checked = spawnButtons.getChecked();

        if (checked != null)
        {
            int index = spawnButtons.getButtons().indexOf(checked, true);

            if (index + 1 < spawnButtons.getButtons().size)
            {
                TextButton next = spawnButtons.getButtons().get(index + 1);
                Spawnable asSpawnable = ((Spawnable) next.getUserObject());
                next.setChecked(true);
                updateSpawnSelection(asSpawnable);
            }

        }
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.respawnIn, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.spectatorFlag, this);
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOut.EventMgr.subscribe(Event.ID.respawnIn, this);
        BrainOut.EventMgr.subscribe(Event.ID.spectatorFlag, this);
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        counter -= delta;

        if (counter < 0)
        {
            counter = 0.25f;

            updateVMove();
        }
    }

    private void updateVMove()
    {
        if (Math.abs(vMove.y) > 0.5f)
        {
            if (vMove.y > 0)
            {
                selectPrevSpawn();
            }
            else
            {
                selectNextSpawn();
            }
        }
    }
}
