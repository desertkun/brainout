package com.desertkun.brainout.mode.payload;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.ShootingRangeData;

import java.util.TimerTask;

public class LobbyPayload extends ModePayload
{
    private String currentTarget;
    private String currentWeapon;
    private TimerTask shootingRangeWatchdog;
    private TimerTask shootingRangeTask;
    private int hits;

    public interface ShootingRangeCompletedCallback
    {
        void shootingRangeComplete(LobbyPayload playerClient, String currentTarget, String currentWeapon, int hits);
        boolean shootingRangeWatchdog(LobbyPayload playerClient, float initialX, float initialY);
    }

    public LobbyPayload(Client playerClient)
    {
        super(playerClient);

        currentTarget = null;
        shootingRangeTask = null;
        hits = 0;
    }

    public boolean isInTargetPracticing()
    {
        return currentTarget != null;
    }

    public void setCurrentTarget(String currentTarget)
    {
        this.currentTarget = currentTarget;
    }

    public void setCurrentWeapon(String currentWeapon)
    {
        this.currentWeapon = currentWeapon;
    }

    public String getCurrentTarget()
    {
        return currentTarget;
    }

    public String getCurrentWeapon()
    {
        return currentWeapon;
    }

    public void resetPracticing()
    {
        if (shootingRangeTask != null)
        {
            shootingRangeTask.cancel();
            shootingRangeTask = null;
        }

        if (shootingRangeWatchdog != null)
        {
            shootingRangeWatchdog.cancel();
            shootingRangeWatchdog = null;
        }

        currentTarget = null;
        currentWeapon = null;
        hits = 0;
    }

    public void startPracticing(ShootingRangeData shootingRangeData, int time, ShootingRangeCompletedCallback complete)
    {
        float x = shootingRangeData.getX(), y = shootingRangeData.getY();

        shootingRangeTask = new TimerTask()
        {
            @Override
            public void run()
            {
                shootingRangeTask = null;

                BrainOutServer.PostRunnable(() ->
                {
                    String currentTarget = LobbyPayload.this.currentTarget;
                    String currentWeapon = LobbyPayload.this.currentWeapon;
                    int hits = LobbyPayload.this.hits;

                    resetPracticing();
                    complete.shootingRangeComplete(LobbyPayload.this, currentTarget, currentWeapon, hits);
                });
            }
        };

        BrainOutServer.Timer.schedule(shootingRangeTask, time * 1000L);

        shootingRangeWatchdog = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() ->
                {
                    if (!complete.shootingRangeWatchdog(LobbyPayload.this, x, y))
                    {
                        String currentTarget = LobbyPayload.this.currentTarget;
                        String currentWeapon = LobbyPayload.this.currentWeapon;
                        int hits = LobbyPayload.this.hits;

                        resetPracticing();
                        complete.shootingRangeComplete(LobbyPayload.this, currentTarget, currentWeapon, hits);
                    }
                });
            }
        };

        BrainOutServer.Timer.schedule(shootingRangeWatchdog, 1000L, 1000L);
    }

    public int hit()
    {
        return ++this.hits;
    }

    @Override
    public void init()
    {

    }

    @Override
    public void release()
    {

    }
}
