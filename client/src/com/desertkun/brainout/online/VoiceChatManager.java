package com.desertkun.brainout.online;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.events.Event;

import java.util.concurrent.*;

public class VoiceChatManager implements Disposable, EventReceiver
{
    public interface VoiceChatSendCallback
    {
        void sendVoiceData(short[] data, boolean trigger);
    }

    private final ThreadPoolExecutor playback;

    private static final int SAMPLE_RATE = 16000;
    private static final int SEND_BURST = 50;

    private static final int SAMPLE_LENGTH = SAMPLE_RATE / (1000 / SEND_BURST);

    private short[] buffer;

    private AudioRecorder recorder;
    private AudioDevice player;
    private boolean streaming;
    private boolean working;
    private float volume;

    private float microphoneVolume;
    private Thread streamer;
    private VoiceChatSendCallback sendCallback;

    public void init()
    {
        if (Gdx.audio == null)
            return;

        try
        {
            this.player = Gdx.audio.newAudioDevice(SAMPLE_RATE, true);
            this.recorder = Gdx.audio.newAudioRecorder(SAMPLE_RATE, true);
        }
        catch (GdxRuntimeException | NullPointerException e)
        {
            return;
        }

        updateVolume();

        this.buffer = new short[SAMPLE_LENGTH];

        working = true;

        this.streamer = new Thread(() ->
        {
            int cnt = 0;

            while (working)
            {
                if (!streaming)
                {
                    try
                    {
                        Thread.sleep(SEND_BURST);
                        continue;
                    }
                    catch (InterruptedException e)
                    {
                        working = false;
                    }
                }

                cnt = 0;

                while (streaming)
                {
                    recorder.read(buffer, 0, SAMPLE_LENGTH);
                    if (sendCallback != null)
                        sendCallback.sendVoiceData(buffer, cnt % 4 == 0);

                    cnt++;
                }
            }
        });

        this.streamer.start();

        BrainOutClient.EventMgr.subscribe(Event.ID.settingsUpdated, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.gameController, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
    }

    public void setSendCallback(VoiceChatSendCallback sendCallback)
    {
        this.sendCallback = sendCallback;
    }

    public void resetSendCallback()
    {
        this.streaming = false;
        this.sendCallback = null;
    }

    public VoiceChatManager()
    {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        RejectedExecutionHandler rejected = (r, executor) -> {};

        playback = new ThreadPoolExecutor(0, 4, 100, TimeUnit.MILLISECONDS, queue, rejected);
    }

    public void startStreaming()
    {
        if (sendCallback == null)
            return;

        streaming = true;
    }

    public void stopStreaming()
    {
        streaming = false;
    }

    private void playAudio(float volume, short[] samples)
    {
        if (player == null)
            return;

        player.setVolume(volume * this.volume);
        try
        {
            player.writeSamples(samples, 0, samples.length);
        }
        catch (UnsatisfiedLinkError ignored) {}
    }

    public void playAudioData(float volume, short[] samples)
    {
        playback.execute(() -> playAudio(volume, samples));
    }

    @Override
    public void dispose()
    {
        working = false;

        try
        {
            if (streamer != null)
                streamer.join();
        }
        catch (InterruptedException e)
        {
            //
        }

        BrainOutClient.EventMgr.unsubscribe(Event.ID.settingsUpdated, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.gameController, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);

        playback.shutdownNow();

        if (player != null)
            player.dispose();

        if (recorder != null)
            recorder.dispose();
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
            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case voiceChatBegin:
                    {
                        startStreaming();
                        break;
                    }
                    case voiceChatEnd:
                    {
                        stopStreaming();
                        break;
                    }
                }
            }
        }

        return false;
    }

    private void updateVolume()
    {
        volume = BrainOutClient.ClientSett.getVoiceChatVolume().getFloatValue();
        microphoneVolume = BrainOutClient.ClientSett.getMicrophoneVolume().getFloatValue() * 2;
    }

    public float getMicrophoneVolume()
    {
        return microphoneVolume;
    }
}
