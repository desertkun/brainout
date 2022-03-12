package com.desertkun.brainout.mode;

import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.data.interfaces.Spawnable;

public class GameModeDuel extends GameMode
{
    public enum DuelState
    {
        waiting,
        spawn,
        steady,
        active,
        await,
        end
    }

    public GameModeDuel(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeDuel.class);
    }

    @Override
    public ID getID()
    {
        return ID.duel;
    }

    @Override
    public boolean canSeePlayerList()
    {
        return false;
    }

    @Override
    public boolean isAutoKickEnabled()
    {
        return false;
    }

    @Override
    public boolean allowTeamChange()
    {
        return false;
    }

    @Override
    public boolean canSpawn(Spawnable spawnable, Team team)
    {
        return super.canSpawn(spawnable, team);
    }

    @Override
    public boolean validateActive(Active active)
    {
        return super.validateActive(active);
    }

    @Override
    public boolean hasWarmUp()
    {
        return false;
    }
}
