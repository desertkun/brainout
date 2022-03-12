package com.desertkun.brainout.client.states.packages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.client.states.CSClientInit;
import com.desertkun.brainout.client.states.ControllerState;
import com.desertkun.brainout.client.states.map.CSMapDownload;
import com.desertkun.brainout.packages.ContentPackage;
import com.desertkun.brainout.packages.PackageManager;

public class CSPackagesLoad extends ControllerState implements Runnable
{
    private final Array<String> packagesToLoad;
    private final Runnable loaded;

    public CSPackagesLoad(Array<String> packagesToLoad)
    {
        this(packagesToLoad, null);
    }

    public CSPackagesLoad(Array<String> packagesToLoad, Runnable loaded)
    {
        this.packagesToLoad = packagesToLoad;
        this.loaded = loaded;
    }

    @Override
    public ID getID()
    {
        return ID.packagesLoad;
    }

    @Override
    public void init()
    {
        // unload all packages except mainmenu
        BrainOut.PackageMgr.unloadPackages(false,
            contentPackage -> ClientConstants.Client.MAINMENU_PACKAGE.equals(contentPackage.getName()));

        // load it anyway in case it wasn't loaded yet
        BrainOut.PackageMgr.registerPackage(ClientConstants.Client.MAINMENU_PACKAGE);

        for (String pack: packagesToLoad)
        {
            BrainOut.PackageMgr.registerPackage(pack);
        }

        BrainOut.PackageMgr.checkValidation();
        BrainOut.PackageMgr.loadPackages(this);
    }

    private void packagesLoaded()
    {
        if (loaded != null)
        {
            loaded.run();
            return;
        }

        if (BrainOutClient.ClientController.getServerHttpPort() == -1)
        {
            switchTo(new CSClientInit(null));
        }
        else
        {
            // download the map then
            switchTo(new CSMapDownload());
        }
    }

    @Override
    public void release()
    {

    }

    @Override
    public void run()
    {
        Gdx.app.postRunnable(this::packagesLoaded);
    }
}
