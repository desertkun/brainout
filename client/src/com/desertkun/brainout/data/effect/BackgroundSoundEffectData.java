package com.desertkun.brainout.data.effect;


import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.effect.BackgroundSoundEffect;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.BackgroundSoundEffectData")
public class BackgroundSoundEffectData extends EffectData
{
    private final Sound sound;

    public BackgroundSoundEffectData(BackgroundSoundEffect effect, LaunchData launchData)
    {
        super(effect, launchData);
        this.sound = effect.getSound();
    }

    @Override
    public boolean done()
    {
        return true;
    }

    @Override
    public void init()
    {
        if (Map.GetWatcherMap(Map.class) != getMap())
            return;

        sound.play(BrainOutClient.ClientSett.getSoundVolume().getFloatValue());
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {

    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public void update(float dt)
    {

    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }
}
