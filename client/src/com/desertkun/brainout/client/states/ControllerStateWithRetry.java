package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOutClient;

import java.util.TimerTask;

public abstract class ControllerStateWithRetry extends ControllerState
{
    private int retryCounter;
    private int retryTimer = 500;

    public ControllerStateWithRetry(int maxRetries)
    {
        this.retryCounter = maxRetries;
    }

    protected abstract void retryFailed();

    protected void retry()
    {
        if (retryCounter > 0)
        {
            retryCounter--;

            doRetry();
            return;
        }

        retryFailed();
    }

    private void doRetry()
    {
        BrainOutClient.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Gdx.app.postRunnable(() ->
                    BrainOutClient.ClientController.setState(ControllerStateWithRetry.this));
            }
        }, retryTimer);

        retryTimer *= 2;

        if (retryTimer > 8000)
        {
            retryTimer = 8000;
        }
    }
}
