package com.desertkun.brainout.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.Music;
import com.desertkun.brainout.content.MusicList;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.SimpleEvent;


public class MusicManager implements com.badlogic.gdx.audio.Music.OnCompletionListener, EventReceiver
{
    private com.badlogic.gdx.audio.Music currentMusic = null;

    private MusicList musicList;

    public MusicManager()
    {
        musicList = null;

        BrainOutClient.EventMgr.subscribe(Event.ID.settingsUpdated, this);
    }

    public com.badlogic.gdx.audio.Music getCurrentMusic()
    {
        return currentMusic;
    }

    public void playList(MusicList musicList)
    {
        this.musicList = musicList;

        playNext();
    }

    public void init()
    {
        BrainOutClient.EventMgr.subscribe(Event.ID.settingsUpdated, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
    }

    public void playNext()
    {
        stopMusic();

        if (musicList == null) return;

        if (musicList.getMusic().size > 0)
        {
            Music music = musicList.getRandom();
            if (music != null)
            {
                try
                {
                    currentMusic = music.getMusic();
                    if (currentMusic != null)
                    {
                        currentMusic.play();

                        updateVolume();

                        currentMusic.setOnCompletionListener(this);
                    }
                }
                catch (GdxRuntimeException ignored)
                {
                    currentMusic = null;
                }
            }
        }
    }

    public com.badlogic.gdx.audio.Music playMusic(String id)
    {
        return playMusic(id, false);
    }

    public com.badlogic.gdx.audio.Music playMusic(String id, boolean repeat)
    {
        stopMusic();
        musicList = null;

        Music music = ((Music) BrainOut.ContentMgr.get(id));
        if (music != null)
        {
            currentMusic = music.getMusic();

            try
            {
                currentMusic.play();
                currentMusic.setLooping(repeat);
            }
            catch (GdxRuntimeException ignored)
            {
                //
            }

            updateVolume();

            return currentMusic;
        }

        return null;
    }

    public void stopMusic()
    {
        if (currentMusic != null)
        {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
    }

    @Override
    public void onCompletion(com.badlogic.gdx.audio.Music music)
    {
        playNext();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case settingsUpdated:
            {
                updateVolume();

                break;
            }
            case simple:
            {
                if (((SimpleEvent) event).getAction() == SimpleEvent.Action.audioUpdated)
                {
                    updateVolume();
                }

                break;
            }
        }

        return false;
    }

    private void updateVolume()
    {
        if (currentMusic != null)
        {
            currentMusic.setVolume(BrainOutClient.ClientSett.getMusicVolume().getFloatValue());
        }
    }
}
