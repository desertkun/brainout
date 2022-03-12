package com.desertkun.brainout.plugins;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.ChatMsg;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.KillEvent;
import com.desertkun.brainout.server.ServerConstants;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("plugins.TeamKillKickPlugin")
public class TeamKillKickPlugin extends Plugin implements EventReceiver
{
    private int killToKick;
    private boolean kill;

    public TeamKillKickPlugin()
    {

    }

    @Override
    public void init()
    {
        super.init();

        BrainOutServer.EventMgr.subscribe(Event.ID.kill, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutServer.EventMgr.unsubscribe(Event.ID.kill, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.killToKick = jsonData.getInt("killToKick", 3);
        this.kill = jsonData.getBoolean("kill", false);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case kill:
            {
                KillEvent killEvent = ((KillEvent) event);

                killed(killEvent);

                break;
            }
        }

        return false;
    }

    private boolean isKillIntended(Client killer, Client victim)
    {
        if (killer != victim &&
            !killer.isEnemy(victim) &&
            !victim.isSpectator() &&
            killer.isSpectator())
        {
            return true;
        }

        return false;
    }

    private void killed(KillEvent killEvent)
    {
        Client killer = killEvent.killer;
        Client victim = killEvent.victim;

        if (isKillIntended(killer, victim))
        {
            killer.kick("teamkill");
            return;
        }

        if (killer != victim &&
            !killer.isEnemy(victim) &&
            !victim.isSpectator())
        {
            int count = killer.incFriendlyKills();
            victim.addKilledBy(killer);

            if (count > killToKick)
            {
                killer.kick("teamkill");
            }
            else
            {
                if (kill)
                {
                    killer.kill();
                }

                if (killer instanceof PlayerClient)
                {
                    ((PlayerClient) killer).sendTCP(new ChatMsg("console", "{TEAMKILL_WARNING}", "server",
                            ServerConstants.Chat.COLOR_IMPORTANT, -1));
                }
            }
        }
    }
}
