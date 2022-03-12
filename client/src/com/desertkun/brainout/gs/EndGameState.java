package com.desertkun.brainout.gs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSError;
import com.desertkun.brainout.client.states.CSReconnect;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.gs.actions.MenuAction;
import com.desertkun.brainout.menu.impl.*;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.RichAlertPopup;
import com.desertkun.brainout.playstate.ClientPSEndGame;

public class EndGameState extends GameState implements EventReceiver
{
    private final ClientPSEndGame endGame;

    public EndGameState(ClientPSEndGame endGame)
    {
        this.endGame = endGame;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        pushMenu(new EndGameMenu(endGame));
        pushMenu(new FadeOutMenu(ClientConstants.Menu.Flash.FLASH, Color.WHITE));

        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
        BrainOut.EventMgr.subscribe(Event.ID.popup, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.popup, this);
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
                                    BrainOutClient.Env.gameCompleted();
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
                                            BrainOutClient.Env.gameCompleted();
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
        }

        return false;
    }
}
