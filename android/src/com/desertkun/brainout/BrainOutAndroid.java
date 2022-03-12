package com.desertkun.brainout;

import com.desertkun.brainout.client.settings.ClientSettings;
import com.desertkun.brainout.packages.AndroidPackageManager;
import com.desertkun.brainout.packages.PackageManager;

public class BrainOutAndroid extends BrainOutClient
{
    public BrainOutAndroid(AndroidEnvironment env, ClientSettings clientSettings)
    {
        super(env, clientSettings);
    }

    public static BrainOutAndroid initAndroidInstance(AndroidEnvironment env,
          ClientSettings clientSettings)
    {
        instance = new BrainOutAndroid(env, clientSettings);
        return (BrainOutAndroid)instance;
    }

    @Override
    public PackageManager getPackageManager(int threads)
    {
        return new AndroidPackageManager(threads);
    }
}
