package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.effect.MusicEffect;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.active.FreeplayGeneratorData;
import com.desertkun.brainout.data.components.ClientFreeplayGeneratorComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientFreeplayGeneratorComponent")
public class ClientFreeplayGeneratorComponent extends ClientActiveActivatorComponent
{
    private SoundEffect startupSound;
    private SoundEffect startupFailSound;
    private SoundEffect stopSound;
    private MusicEffect idleMusic;

    @Override
    public ClientFreeplayGeneratorComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientFreeplayGeneratorComponentData((FreeplayGeneratorData)componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        startupSound = BrainOutClient.ContentMgr.get(jsonData.getString("startup"), SoundEffect.class);
        startupFailSound = BrainOutClient.ContentMgr.get(jsonData.getString("fail"), SoundEffect.class);
        stopSound = BrainOutClient.ContentMgr.get(jsonData.getString("stop"), SoundEffect.class);
        idleMusic = BrainOutClient.ContentMgr.get(jsonData.getString("idle"), MusicEffect.class);
    }

    public SoundEffect getStartupSound()
    {
        return startupSound;
    }

    public SoundEffect getStartupFailSound()
    {
        return startupFailSound;
    }

    public SoundEffect getStopSound()
    {
        return stopSound;
    }

    public MusicEffect getIdleMusic()
    {
        return idleMusic;
    }
}
