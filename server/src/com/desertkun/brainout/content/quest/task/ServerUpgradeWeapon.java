package com.desertkun.brainout.content.quest.task;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayWeaponUpgradedEvent;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.UpgradeWeapon")
public class ServerUpgradeWeapon extends UpgradeWeapon implements ServerTask
{
    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayWeaponUpgraded:
            {
                FreePlayWeaponUpgradedEvent ev = ((FreePlayWeaponUpgradedEvent) event);

                ServerTask.Trigger(this, ev.player, 1);
            }
        }

        return false;
    }

    @Override
    public void started(ServerFreeRealization free, PlayerClient playerClient)
    {
        //
    }
}
