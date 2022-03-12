package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.RoundLockSafeData;
import com.desertkun.brainout.data.components.ClientRoundLockActivatorComponentData;
import com.desertkun.brainout.data.components.ClientSafeActivatorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientRoundLockActivatorComponent")
public class ClientRoundLockActivatorComponent extends ClientActiveActivatorComponent
{
    public ClientRoundLockActivatorComponent()
    {
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ClientRoundLockActivatorComponentData((RoundLockSafeData)componentObject, this);
    }
}
