package com.desertkun.brainout.gs;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSEmpty;
import com.desertkun.brainout.client.states.CSError;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.menu.impl.*;
import com.desertkun.brainout.menu.popups.AlertPopup;

public class LoadingState extends GameState implements EventReceiver
{
    private final String reason;

    public LoadingState()
    {
        this("");
    }

    public LoadingState(String reason)
    {
        this.reason = reason;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOut.EventMgr.subscribe(Event.ID.controller, this);
        BrainOut.EventMgr.subscribe(Event.ID.error, this);
        BrainOut.EventMgr.subscribe(Event.ID.simple, this);

        pushMenu(new WaitLoadingMenu(reason, true));
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.controller, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.error, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
    }

    private void pushPackageLoadingMenu()
    {
        pushMenu(BrainOutClient.Env.createPackageLoadingMenu());
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case error:
            {
                ErrorEvent ev = (ErrorEvent)event;

                popAll();
                pushMenu(new AlertPopup(ev.errorText)
                {
                    @Override
                    public void ok()
                    {
                        BrainOutClient.Env.gameCompleted();
                        BrainOutClient.getInstance().initMainMenu().loadPackages();
                    }
                });

                break;
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
                                case reconnect:
                                case leave:
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
                        popTopMenu();

                        return true;
                    }
                }

                break;
            }

            case controller:
            {
                final ClientControllerEvent clientControllerEvent = ((ClientControllerEvent) event);
                switch (clientControllerEvent.state.getID())
                {
                    case none:
                    {
                        BrainOutClient.getInstance().initMainMenu().loadPackages();

                        break;
                    }

                    case connecting:
                    {
                        popAll();
                        pushMenu(new WaitLoadingMenu(L.get("MENU_CONNECTING_TO_SERVER"), true));

                        break;
                    }

                    case error:
                    {
                        final CSError error = (CSError) clientControllerEvent.state;

                        pushMenu(new AlertPopup(error.getMessage())
                        {
                            @Override
                            public boolean bg()
                            {
                                return true;
                            }

                            @Override
                            public void ok()
                            {
                                pop();

                                BrainOutClient.ClientController.disconnect(DisconnectReason.leave, () ->
                                {
                                    BrainOutClient.getInstance().initMainMenu().loadPackages();

                                    if (error.getOk() != null)
                                    {
                                        error.getOk().run();
                                    }
                                });
                            }
                        });

                        break;
                    }

                    case maintenance:
                    {
                        popAll();
                        pushMenu(new MaintenanceMenu(L.get("MENU_MAINTENANCE")));

                        break;
                    }

                    case gameOutdated:
                    {
                        popAll();
                        pushMenu(new MaintenanceMenu(L.get("MENU_GAME_OUTDATED")));

                        break;
                    }

                    case connected:
                    {
                        popAll();
                        pushMenu(new WaitLoadingMenu(L.get("MENU_CONNECTION_ESTABLISHED"), true));

                        break;
                    }

                    case findLobby:
                    case joinLobby:
                    {
                        popAll();
                        pushMenu(new WaitLoadingMenu(L.get("MENU_LOADING"), true));

                        break;
                    }

                    case empty:
                    {
                        CSEmpty empty = (CSEmpty) clientControllerEvent.state;

                        empty.pushMenu(this);

                        break;
                    }

                    case packagesLoad:
                    {
                        popAll();
                        pushPackageLoadingMenu();

                        break;
                    }

                    case mapDownload:
                    {
                        popAll();

                        pushMenu(new WaitLoadingMenu(L.get("MENU_DOWNLOADING_MAP"), true));

                        break;
                    }

                    case mapLoad:
                    {
                        popAll();
                        pushMenu(new WaitLoadingMenu(L.get("MENU_INITIALIZING_MAP"), true));

                        break;
                    }

                    case game:
                    {
                        popAll();
                        BrainOutClient.getInstance().switchState(new ActionPhaseState());

                        break;
                    }
                }

                break;
            }
        }

        return false;
    }
}
