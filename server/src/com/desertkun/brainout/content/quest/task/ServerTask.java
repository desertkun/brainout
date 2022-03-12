package com.desertkun.brainout.content.quest.task;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;

public interface ServerTask extends EventReceiver
{
    void started(ServerFreeRealization free, PlayerClient playerClient);

    static int Trigger(Task task, PlayerClient client, int amount)
    {
        if (amount == 0) return 0;

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return 0;

        if (!(gameMode.getRealization() instanceof ServerFreeRealization))
            return 0;

        ModePayload modePayload = client.getModePayload();

        if (!(modePayload instanceof FreePayload))
            return 0;

        FreePayload freePayload = ((FreePayload) modePayload);

        return freePayload.triggerQuestTask(task, amount);
    }
}
