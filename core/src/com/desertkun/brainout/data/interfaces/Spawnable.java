package com.desertkun.brainout.data.interfaces;

import com.desertkun.brainout.content.SpawnTarget;
import com.desertkun.brainout.content.Team;

public interface Spawnable
{
    float getSpawnX();
    float getSpawnY();
    Team getTeam();
    float getSpawnRange();
    String getDimension();

    boolean canSpawn(Team teamFor);
    SpawnTarget getTarget();
}
