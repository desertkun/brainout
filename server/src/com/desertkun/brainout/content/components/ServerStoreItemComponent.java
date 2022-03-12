package com.desertkun.brainout.content.components;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.components.base.ContentComponent;

public abstract class ServerStoreItemComponent extends ContentComponent
{
    public abstract boolean purchased(PlayerClient playerClient);
}
