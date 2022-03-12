package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Flag;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.interfaces.Spawnable;

public class GameModeDeathmatch<R extends GameModeRealization> extends GameMode<R>
{
    private int tickets;
    private int initialTickets;

    public GameModeDeathmatch(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeDeathmatch.class);

        this.tickets = 0;
        this.initialTickets = 0;
    }

    @Override
    public float getGameProgress()
    {
        return 1.0f - (float)tickets / (float)initialTickets;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        tickets = jsonData.getInt("tickets", 0);
        initialTickets = jsonData.getInt("intickets", tickets);

        super.read(json, jsonData);
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        json.writeValue("tickets", tickets);
        json.writeValue("intickets", initialTickets);

        super.write(json, componentWriter, owner);
    }

    @Override
    public ID getID()
    {
        return ID.deathmatch;
    }

    public int getTickets()
    {
        return tickets;
    }

    public int getInitialTickets()
    {
        return initialTickets;
    }

    public void setTickets(int tickets)
    {
        this.tickets = tickets;
    }

    public void setInitialTickets(int initialTickets)
    {
        this.initialTickets = initialTickets;
    }

    @Override
    public boolean isEnemies(Team a, Team b)
    {
        return true;
    }

    @Override
    public boolean isEnemiesActive(ActiveData a, ActiveData b)
    {
        return true;
    }

    @Override
    public boolean isTeamVisibilityEnabled()
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
