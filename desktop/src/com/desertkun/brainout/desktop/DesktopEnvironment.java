package com.desertkun.brainout.desktop;

import com.desertkun.brainout.*;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.controllers.KeyboardController;
import com.desertkun.brainout.online.KryoNetworkClient;
import com.desertkun.brainout.online.NetworkClient;
import com.desertkun.brainout.online.NetworkConnectionListener;
import com.esotericsoftware.kryo.Kryo;

import java.io.File;
import java.util.Map;

public class DesktopEnvironment extends ClientEnvironment
{
    private KeyboardController desktopController;

    public DesktopEnvironment(String[] args)
    {
        this.desktopController = new KeyboardController();

        setArgs(args);
    }

    public DesktopEnvironment(String[] args, KeyboardController controller)
    {
        this.desktopController = controller;

        setArgs(args);
    }


    @Override
    public String getUniqueId()
    {
        String name = System.getenv("DESKTOP_PROFILE_NAME");

        if (name != null)
            return name;

        return "desktop";
    }

    @Override
    public GameUser newUser()
    {
        return new GameUser();
    }

    @Override
    public String getExternalPath(String from)
    {
        return from;
    }

    @Override
    public File getFile(String path)
    {
        return new File(path);
    }

    @Override
    public GameController getGameController()
    {
        return desktopController;
    }

    @Override
    public boolean storeEnabled()
    {
        return false;
    }

    @Override
    public String getStoreComponent()
    {
        return null;
    }

    @Override
    public void getStoreEnvironment(Map<String, String> env)
    {
        super.getStoreEnvironment(env);
    }

    @Override
    public NetworkClient newNetworkClient(Kryo kryo, NetworkConnectionListener listener)
    {
        return new KryoNetworkClient(kryo, listener);
    }
}
