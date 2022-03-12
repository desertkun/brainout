package com.desertkun.brainout.packages;

import java.io.File;

public class AndroidPackageManager extends ClientPackageManager
{
    public AndroidPackageManager(int threads)
    {
        super(threads);
    }

    @Override
    public ContentPackage createPackage(String name)
    {
        return new AndroidPackage(name);
    }
}
