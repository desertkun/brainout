package com.desertkun.brainout.content.components;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

public abstract class PostOwnComponent extends ContentComponent
{
    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    public abstract void owned(PlayerClient playerClient, OwnableContent content);
}
