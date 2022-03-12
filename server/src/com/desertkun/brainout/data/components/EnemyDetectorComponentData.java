package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.EnemyDetectorComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.mode.GameMode;

import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("EnemyDetectorComponent")
@ReflectAlias("data.components.EnemyDetectorComponentData")
public class EnemyDetectorComponentData extends DetectorComponentData<EnemyDetectorComponent>
{

    public EnemyDetectorComponentData(ActiveData activeData,
                                      EnemyDetectorComponent detectorComponent)
    {
        super(activeData, detectorComponent);
    }

    @Override
    protected boolean validate(ActiveData activeData)
    {
        if (!(activeData instanceof PlayerData))
            return false;

        ActiveData me = getMe();
        Client owner = BrainOutServer.Controller.getClients().get(me.getOwnerId());

        Team team = owner != null ? owner.getTeam() : null;
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (activeData.getOwnerId() == me.getOwnerId()) return false;

        Client activeOwner = BrainOutServer.Controller.getClients().get(activeData.getOwnerId());

        return !(gameMode == null || team == null ||
               !(gameMode.isEnemies(team, activeData.getTeam()) ||
                 ((ServerRealization) gameMode.getRealization()).isEnemies(activeOwner, owner)));
    }
}
