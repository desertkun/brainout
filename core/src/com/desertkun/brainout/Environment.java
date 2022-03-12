package com.desertkun.brainout;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.online.NetworkClient;
import com.desertkun.brainout.online.NetworkConnectionListener;
import com.esotericsoftware.kryo.Kryo;

import java.io.File;

public abstract class Environment
{
    private String[] args = {};

    public abstract String getUniqueId();
    public abstract String getExternalPath(String from);
    public abstract File getFile(String path);

    public void init()
    {
        //
    }

    public void release()
    {

    }

    public String getDefaultLanguage()
    {
        return "EN";
    }

    public File getCacheDir()
    {
        return null;
    }

    public ObjectMap<String, String> getEnvironmentValues()
    {
        ObjectMap<String, String> data = new ObjectMap<String, String>();

        initEnvironmentValues(data);

        return data;
    }

    protected void initEnvironmentValues(ObjectMap<String, String> data)
    {
        data.put("id", getUniqueId());

        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        data.put("heap-size", String.valueOf(heapSize));
        data.put("heap-max", String.valueOf(heapMaxSize));
        data.put("heap-free", String.valueOf(heapFreeSize));

        data.put("game-version", Version.VERSION);
        data.put("game-build", String.valueOf(Constants.Version.BUILD));
    }

    public abstract NetworkClient newNetworkClient(Kryo kryo, NetworkConnectionListener listener);

    public String[] getArgs()
    {
        return args;
    }

    public void setArgs(String[] args)
    {
        this.args = args;
    }

    public boolean needsPrivacyPolicy()
    {
        return true;
    }
}
