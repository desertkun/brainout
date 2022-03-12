package com.desertkun.brainout.data.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.containers.ClientChunkData;
import com.desertkun.brainout.content.components.ClientBackgroundEffectComponent;
import com.desertkun.brainout.content.components.ClientBackgroundMusicComponent;
import com.desertkun.brainout.content.effect.ParticleEffect;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.Objects;

@Reflect("ClientBackgroundMusicComponent")
@ReflectAlias("data.components.ClientBackgroundMusicComponentData")
public class ClientBackgroundMusicComponentData extends Component<ClientBackgroundMusicComponent> implements WithTag
{
    private Music music;
    private float check;

    public ClientBackgroundMusicComponentData(ComponentObject componentObject,
                                              ClientBackgroundMusicComponent contentComponent)
    {
        super(componentObject, contentComponent);

        music = contentComponent.getSound();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        check -= dt;
        if (check > 0)
            return;
        check = 0.05f;

        Map watcher = Map.GetWatcherMap(Map.class);
        if (watcher == null)
            return;

        if (getMap() == watcher)
        {
            play();
        }
        else
        {
            checkIfShouldStop(watcher);
        }
    }

    @Override
    public void init()
    {
        super.init();
        BrainOutClient.EventMgr.subscribe(Event.ID.settingsUpdated, this);
    }

    private void checkIfShouldStop(Map watcher)
    {
        ActiveData curr = watcher.getActiveForTag(Constants.ActiveTags.BACKGROUND_MUSIC, activeData ->
        {
            ClientBackgroundMusicComponentData bg =
                    activeData.getComponent(ClientBackgroundMusicComponentData.class);

            if (bg == null)
                return false;

            return bg.music == ClientBackgroundMusicComponentData.this.music;
        });

        if (curr == null)
        {
            stop();
        }
    }

    public boolean isPlaying()
    {
        return music.isPlaying();
    }

    protected void play()
    {
        if (music.isPlaying())
            return;

        music.setLooping(true);
        music.setVolume(BrainOutClient.ClientSett.getMusicVolume().getFloatValue());
        music.play();
    }

    protected void stop()
    {
        if (music.isPlaying())
        {
            music.stop();
        }
    }

    @Override
    public void release()
    {
        super.release();
        BrainOutClient.EventMgr.unsubscribe(Event.ID.settingsUpdated, this);

        Map wm = Map.GetWatcherMap(Map.class);
        if (wm == null)
        {
            stop();
            return;
        }

        checkIfShouldStop(wm);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case settingsUpdated:
            {
                if (music.isPlaying())
                {
                    music.setVolume(BrainOutClient.ClientSett.getMusicVolume().getFloatValue());
                }

                break;
            }
        }
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.BACKGROUND_MUSIC);
    }
}
