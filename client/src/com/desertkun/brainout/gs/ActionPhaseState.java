package com.desertkun.brainout.gs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSError;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.client.states.CSReconnect;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.content.Music;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.gs.actions.MenuAction;
import com.desertkun.brainout.gs.actions.WaitAction;
import com.desertkun.brainout.menu.impl.*;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.RichAlertPopup;
import com.desertkun.brainout.mode.ClientGameRealization;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.posteffects.PostEffects;


public class ActionPhaseState extends GameState implements EventReceiver
{
    private final Batch batch;
    private CSGame gameController;
    private RenderContext renderContext;
    private PostEffects postEffects;
    private ActionPhaseMenu actionPhaseMenu;
    private float flashTimer;
    private float counter;

    public ActionPhaseState()
    {
        flashTimer = 0;
        counter = 0;
        batch = BrainOutClient.ClientSett.allocateNewBatch();
        postEffects = new PostEffects();
        gameController = BrainOutClient.ClientController.getState(CSGame.class);
        renderContext = new RenderContext(BrainOutClient.getWidth(), BrainOutClient.getHeight());
    }

    public ActionPhaseMenu getActionPhaseMenu()
    {
        return actionPhaseMenu;
    }

    @Override
    public void onInit()
    {
        this.actionPhaseMenu = new ActionPhaseMenu();
        pushMenu(actionPhaseMenu);

        postEffects.init();

        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
        BrainOut.EventMgr.subscribe(Event.ID.csGame, this);
        BrainOut.EventMgr.subscribe(Event.ID.killedBy, this);
        BrainOut.EventMgr.subscribe(Event.ID.modeWillFinish, this);
        BrainOut.EventMgr.subscribe(Event.ID.popup, this);
        BrainOut.EventMgr.subscribe(Event.ID.screenSizeUpdated, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        postEffects.dispose();

        BrainOutClient.MusicMng.stopMusic();

        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.csGame, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.killedBy, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.modeWillFinish, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.popup, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.screenSizeUpdated, this);

        this.actionPhaseMenu = null;
    }

    @Override
    public void render()
    {
        BrainOutClient.ClientController.preRender();

        postEffects.begin();

        try
        {
            batch.begin();
        }
        catch (IllegalStateException ignored)
        {
            return;
        }

        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        renderContext.post = false;
        BrainOutClient.ClientController.render(batch, renderContext);

        batch.end();

        postEffects.end();

        BrainOutClient.ClientController.postRender();

        try
        {
            batch.begin();
        }
        catch (IllegalStateException ignored)
        {
            return;
        }

        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        renderContext.post = true;
        BrainOutClient.ClientController.render(batch, renderContext);

        batch.end();

        super.render();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case screenSizeUpdated:
            {
                screenSizeUpdated();

                break;
            }

            case modeWillFinish:
            {
                modeWillFinish(((ModeWillFinishInEvent) event).time);

                return true;
            }

            case simple:
            {
                SimpleEvent e = (SimpleEvent)event;

                switch (e.getAction())
                {
                    case disconnect:
                    {
                        Gdx.app.postRunnable(() ->
                        {
                            DisconnectReason reason = BrainOutClient.ClientController.getDisconnectReason();

                            switch (reason)
                            {
                                case leave:
                                case disconnect:
                                case reconnect:
                                {
                                    break;
                                }
                                case shutdown:
                                {
                                    BrainOutClient.getInstance().popState();
                                    BrainOutClient.getInstance().initMainMenu().loadPackages();
                                    break;
                                }
                                case connectionError:
                                {
                                    Gdx.app.postRunnable(() ->
                                    {
                                        BrainOutClient.getInstance().switchState(new LoadingState(
                                            L.get("MENU_RECONNECTING")
                                        ));
                                        BrainOutClient.ClientController.setState(new CSReconnect());
                                    });

                                    break;
                                }
                                default:
                                {
                                    pushMenu(new AlertPopup(L.get("MENU_DISCONNECTED",
                                            reason.toString()))
                                    {
                                        @Override
                                        public void ok()
                                        {
                                            BrainOutClient.getInstance().popState();
                                            BrainOutClient.getInstance().initMainMenu().loadPackages();
                                        }
                                    });
                                    break;
                                }
                            }
                        });

                        return true;
                    }

                    case teamSelected:
                    {
                        if (topMenu() instanceof SelectTeamMenu)
                        {
                            popTopMenu();
                        }

                        return true;
                    }
                }

                break;
            }

            case popup:
            {
                final PopupEvent popupEvent = ((PopupEvent) event);

                final String title = popupEvent.title;
                final String text = popupEvent.data;

                addAction(new MenuAction()
                {
                    @Override
                    public void run()
                    {
                        pushMenu(new FadeInMenu(0.25f, () ->
                            pushMenu(new RichAlertPopup(title, text)
                        {
                            @Override
                            public void ok()
                            {
                                done();
                            }
                        })));
                    }
                });

                break;
            }

            case controller:
            {
                ClientControllerEvent clientControllerEvent = ((ClientControllerEvent) event);

                switch (clientControllerEvent.state.getID())
                {
                    case error:
                    {
                        final CSError error = (CSError) clientControllerEvent.state;

                        pushMenu(new AlertPopup(error.getMessage())
                        {
                            @Override
                            public void ok()
                            {
                                if (error.getOk() != null)
                                {
                                    error.getOk().run();
                                }
                            }
                        });

                        break;
                    }
                }

                break;
            }

            case csGame:
            {
                switch (((CSGameEvent) event).state)
                {
                    case spawningMenu:
                    {
                        addAction(new MenuAction()
                        {
                            @Override
                            public void run()
                            {
                                pushMenu(new FadeInMenu(0.25f, () ->
                                {
                                    showSpawnMenu();
                                    done();
                                }));
                            }
                        });

                        return true;
                    }
                }

                break;
            }

            case killedBy:
            {
                final KilledByEvent killEvent = ((KilledByEvent) event);

                final ActiveData killer = killEvent.killer;
                final InstrumentInfo info = killEvent.info;

                if (killer != null)
                {

                    GameMode gameMode = BrainOutClient.ClientController.getGameMode();

                    if (gameMode != null && gameMode.getRealization() instanceof ClientGameRealization)
                    {
                        ClientGameRealization realization = ((ClientGameRealization) gameMode.getRealization());
                        realization.onKilledBy(this, killer.getMap(), killer, info);
                    }
                }

                break;
            }
        }

        return false;
    }

    private void screenSizeUpdated()
    {
        renderContext.resize(BrainOutClient.getWidth(), BrainOutClient.getHeight());
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (flashTimer > 0)
        {
            flashTimer -= dt;
            if (flashTimer <= 0)
            {
                Gdx.app.postRunnable(() ->
                {
                    BrainOutClient.MusicMng.playMusic("after-flash");
                    pushMenu(new EndGameFlashMenu(ClientConstants.Menu.Flash.FLASH));
                });
            }
        }

        counter -= dt;
        if (counter < 0)
        {
            counter = 0.25f;

            if (!hasTopMenu())
            {
                this.actionPhaseMenu = new ActionPhaseMenu();
                pushMenu(this.actionPhaseMenu);
            }
        }
    }

    private void modeWillFinish(float time)
    {
        Music music = ((Music) BrainOutClient.ContentMgr.get("music-flash"));

        this.flashTimer = time - ClientConstants.Menu.Flash.FLASH;

        float sheduleIn = time - music.getLength();

        if (sheduleIn > 0)
        {
            addAction(new WaitAction(sheduleIn)
            {
                @Override
                public void run()
                {
                    playFinish();
                }
            });
        }
        else
        {
            playFinish();
        }
    }

    private void playFinish()
    {
        Gdx.app.postRunnable(() -> BrainOutClient.MusicMng.playMusic("music-flash"));
    }

    public void showSpawnMenu()
    {
        popAllUntil(ActionPhaseMenu.class);

        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
        if (csGame == null || csGame.isSpectator())
        {
            pushMenu(new WatchSpectatorMenu());
            return;
        }

        GameMode gameMode = BrainOutClient.ClientController.getGameMode();

        if (gameMode != null)
        {
            ClientRealization realization = ((ClientRealization) gameMode.getRealization());

            if (realization != null)
            {
                if (gameController == null)
                    return;
                
                realization.showSpawnMenu(this, gameController.getShopCart(), new SpawnMenu.Spawn()
                {
                    @Override
                    public void ready(Spawnable spawnAt)
                    {
                        gameController.spawnAt(spawnAt);
                    }

                    @Override
                    public void selected(Spawnable spawnable)
                    {
                        gameController.setLastSpawnPoint(spawnable);
                        gameController.saveSelection(gameController.getShopCart());
                    }

                    @Override
                    public void notReady()
                    {
                        gameController.cancelSpawn();
                    }

                    @Override
                    public void spawned()
                    {

                    }
                }, gameController.getLastSpawnPoint());
            }
        }
    }

    public PostEffects getPostEffects()
    {
        return postEffects;
    }
}
