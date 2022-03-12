package com.desertkun.brainout.playstate.special;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.mode.GameMode;

import java.util.TimerTask;

public class PumpkinsGame extends SpecialGame
{
    private TimerTask timerTask;

    @Override
    public void init()
    {
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(PumpkinsGame.this::process);

            }
        };

        BrainOutServer.Timer.schedule(timerTask, 2000, 2000);
    }

    private void process()
    {
        GameMode mode = BrainOutServer.Controller.getGameMode();

        if (mode == null)
            return;

        switch (mode.getID())
        {
            case editor:
            case editor2:
            case free:
            case lobby:
            {
                return;
            }
        }

        if (BrainOutServer.Controller.getClients().size <= 1)
            return;

        Map map = Map.GetDefault();

        if (map == null)
            return;

        ActiveData at = map.getRandomActiveForTag(Constants.ActiveTags.SPAWNABLE);
        if (at == null)
            return;

        int amount = map.countActivesForID("active-pumpkin");

        if (amount >= 3)
            return;

        Active pumpkin = BrainOutServer.ContentMgr.get("active-pumpkin", Active.class);
        ActiveData data = pumpkin.getData(map.getDimension());

        data.setPosition(at.getX(), at.getY());

        map.addActive(map.generateServerId(), data, true);
    }

    @Override
    public void release()
    {
        timerTask.cancel();
    }
}
