package com.desertkun.brainout.mode;

import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.data.interfaces.Spawnable;

public class GameModeEditor<R extends GameModeRealization> extends GameMode<R>
{
    public GameModeEditor(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeEditor.class);
    }

    @Override
    public ID getID()
    {
        return ID.editor;
    }

    @Override
    public boolean canSpawn(Spawnable spawnable, Team team)
    {
        return true;
    }

    @Override
    public boolean validateActive(Active active)
    {
        return !(active instanceof Player);
    }

    @Override
    public boolean isAutoKickEnabled()
    {
        return false;
    }

    @Override
    public boolean hasWarmUp()
    {
        return false;
    }
}
