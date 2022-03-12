package com.desertkun.brainout.menu.impl;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.L;

public class PackagedLoadingMenu extends ProgressiveLoadingMenu
{
    public PackagedLoadingMenu()
    {
        super(() -> BrainOut.PackageMgr.getLoadProgress(), L.get("MENU_LOADING_PACKAGES"));
    }
}
