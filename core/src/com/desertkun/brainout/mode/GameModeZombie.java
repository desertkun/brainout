package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Flag;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.interfaces.Spawnable;

public class GameModeZombie<R extends GameModeRealization> extends GameMode<R>
{

    public GameModeZombie(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeZombie.class);
    }

    @Override
    public float getGameProgress()
    {
        return 0;
    }

    @Override
    public ID getID()
    {
        return ID.zombie;
    }

    @Override
    public boolean isEnemies(Team a, Team b)
    {
        return a != b;
    }

    @Override
    public boolean isEnemiesActive(ActiveData a, ActiveData b)
    {
        return a.getTeam() != b.getTeam();
    }

    @Override
    public boolean isTeamVisibilityEnabled()
    {
        return true;
    }

    @Override
    public boolean countDeaths()
    {
        return false;
    }

    @Override
    public boolean validateActive(Active active)
    {
        if (active instanceof Flag)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canSpawn(Spawnable spawnable, Team team)
    {
        if (spawnable instanceof FlagData)
        {
            return false;
        }

        return super.canSpawn(spawnable, team);
    }
}
