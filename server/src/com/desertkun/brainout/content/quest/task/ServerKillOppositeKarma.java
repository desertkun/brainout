package com.desertkun.brainout.content.quest.task;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.KarmaComponentData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.KillEvent;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.KillOppositeKarma")
public class ServerKillOppositeKarma extends KillOppositeKarma implements ServerTask
{
    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case kill:
            {
                KillEvent ev = ((KillEvent) event);

                if (ev.killer instanceof PlayerClient && ev.victim != null)
                {
                    PlayerClient playerClient = ((PlayerClient) ev.killer);
                    PlayerData killerPlayer = playerClient.getPlayerData();
                    PlayerData victimPlayer = ev.victim.getPlayerData();
                    
                    if (killerPlayer != null && victimPlayer != null)
                    {
                        KarmaComponentData kcd = killerPlayer.getComponent(KarmaComponentData.class);
                        KarmaComponentData vcd = victimPlayer.getComponent(KarmaComponentData.class);

                        if (kcd != null && vcd != null)
                        {
                            if (kcd.getKarma() < -1 && vcd.getKarma() > 1)
                            {
                                ServerTask.Trigger(this, playerClient, 1);
                                playerClient.addStat("karma-kill", 1);
                            }
                            else if (kcd.getKarma() > 1 && vcd.getKarma() < -1)
                            {
                                ServerTask.Trigger(this, playerClient, 1);
                                playerClient.addStat("karma-kill", 1);
                            }
                        }
                    }

                }
            }
        }

        return false;
    }

    @Override
    public void started(ServerFreeRealization free, PlayerClient playerClient)
    {
    }
}
