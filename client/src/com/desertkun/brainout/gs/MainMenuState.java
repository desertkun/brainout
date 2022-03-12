package com.desertkun.brainout.gs;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSError;
import com.desertkun.brainout.client.states.CSMultipleAccounts;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.managers.ClientLocalizationManager;
import com.desertkun.brainout.menu.impl.IntroMenu;
import com.desertkun.brainout.menu.impl.MultipleAccountsMenu;
import com.desertkun.brainout.menu.impl.ProgressiveLoadingMenu;
import com.desertkun.brainout.menu.impl.WaitLoadingMenu;
import com.desertkun.brainout.menu.popups.AlertPopup;

public class MainMenuState extends GameState implements EventReceiver
{
    @Override
    public void onInit()
    {
        super.onInit();

        BrainOut.EventMgr.subscribe(Event.ID.controller, this);
        BrainOut.EventMgr.subscribe(Event.ID.gameController, this);
        BrainOut.EventMgr.subscribe(Event.ID.error, this);
        BrainOut.EventMgr.subscribe(Event.ID.simple, this);

        pushMenu(new WaitLoadingMenu("", true));
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.controller, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.gameController, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.error, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
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
                        showFirstMenu();
                    }
                });

                break;
            }

            case gameController:
            {
                GameControllerEvent gce = ((GameControllerEvent) event);

                switch (gce.action)
                {
                    case back:
                    {
                        goBack();

                        break;
                    }
                }

                break;
            }

            case settingsUpdated:
            {
                reset();

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
                                case leave:
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

                if (clientControllerEvent.state == null)
                    return false;

                switch (clientControllerEvent.state.getID())
                {
                    case none:
                    {
                        showFirstMenu();

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
                                    showFirstMenu();

                                    if (error.getOk() != null)
                                    {
                                        error.getOk().run();
                                    }
                                });
                            }
                        });

                        break;
                    }

                    case validate:
                    {

                        popAll();
                        pushMenu(new WaitLoadingMenu(L.get("MENU_LOADING"), true));

                        break;
                    }

                    case loading:
                    {

                        popAll();
                        pushMenu(new WaitLoadingMenu(L.get("MENU_LOADING"), true));

                        break;
                    }

                    case multipleAccounts:
                    {

                        popAll();
                        pushMenu(new MultipleAccountsMenu(clientControllerEvent.get(CSMultipleAccounts.class)));

                        break;
                    }

                    case onlineInit:
                    {
                        popAll();
                        showIntroMenus();

                        break;
                    }

                    case clientInit:
                    {
                        popAll();
                        pushMenu(new WaitLoadingMenu(L.get("MENU_INITIALIZING_MAP"), true));

                        break;
                    }
                }

                break;
            }
        }

        return false;
    }

    private void goBack()
    {
        popTopMenu();
    }

    private void showFirstMenu()
    {
        BrainOutClient.Env.initOnline();
    }

    private void showIntroMenus()
    {
        pushMenu(BrainOutClient.Env.createIntroMenu());
    }

    public void loadPackages()
    {
        loadPackages(this::showFirstMenu);
    }

    public void loadPackages(Runnable runnable)
    {
        Gdx.app.postRunnable(() -> BrainOut.PackageMgr.loadPackages(runnable));
    }
}
