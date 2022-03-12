package com.desertkun.brainout.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.Launcher;
import com.desertkun.brainout.ui.LoadingBlock;
import org.anthillplatform.runtime.OnlineLib;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.DLCService;
import org.anthillplatform.runtime.services.DiscoveryService;

public class ConnectingMenu extends Menu
{
    public ConnectingMenu()
    {
    }

    @Override
    public void initUI(Table content)
    {
        content.align(Align.right | Align.bottom);

        LoadingBlock loadingBlock = new LoadingBlock();

        content.add(loadingBlock).size(16).pad(16);
    }

    @Override
    public void init()
    {
        attachBackground();

        super.init();

        Launcher.Online.init((lib, status) ->
            Gdx.app.postRunnable(() -> connected(lib, status)));
    }

    private void connected(OnlineLib lib, Status status)
    {
        DiscoveryService.Get().discoverServices(
            new String[]
        {
            DLCService.ID
        }
        , this::discoverResult);


    }

    private void discoverResult(Status status)
    {
        switch (status)
        {
            case notFound:
            {
                Launcher.APP.setMenu(new AlertMenu("UPDATE SERVICE NOT FOUND", "Try again", new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Launcher.APP.setMenu(new ConnectingMenu());
                    }
                }));

                break;
            }

            case success:
            {
                success();
                break;
            }

            case failed:
            default:
            {
                Launcher.APP.setMenu(new AlertMenu("FAILED TO REQUEST UPDATES", "Try again", new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Launcher.APP.setMenu(new ConnectingMenu());
                    }
                }));

                break;
            }
        }
    }

    private void success()
    {
        DLCService dlcService = DLCService.Get();

        dlcService.download(Launcher.GAME_ID, Launcher.GAME_VERSION,
            (service, status) -> Gdx.app.postRunnable(() -> dlcComplete(service, status)));
    }

    private void dlcComplete(DLCService service, Status status)
    {
        switch (status)
        {
            case notFound:
            {
                Launcher.APP.setMenu(new AlertMenu("CONNECTED, BUT UPDATES NOT FOUND", "Try again", new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Launcher.APP.setMenu(new ConnectingMenu());
                    }
                }));

                break;
            }

            case success:
            {
                dlcSuccess(service);
                break;
            }

            case failed:
            default:
            {
                Launcher.APP.setMenu(new AlertMenu("FAILED TO REQUEST UPDATES", "Try again", new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Launcher.APP.setMenu(new ConnectingMenu());
                    }
                }));

                break;
            }
        }
    }

    private void dlcSuccess(DLCService service)
    {
        Launcher.APP.download(service);
    }
}
