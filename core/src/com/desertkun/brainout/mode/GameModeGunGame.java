package com.desertkun.brainout.mode;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Flag;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.interfaces.Spawnable;

public class GameModeGunGame<R extends GameModeRealization> extends GameMode<R>
{
    private Array<String> weapons;

    public GameModeGunGame(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeGunGame.class);

        this.weapons = new Array<>();
    }

    @Override
    public float getGameProgress()
    {
        return 0;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        weapons.clear();

        JsonValue weaps = jsonData.get("weapons");

        if (weaps == null)
            return;

        for (JsonValue weaponValue : weaps)
        {
            String weaponId;

            if (weaponValue.isArray())
            {
                String[] options = weaponValue.asStringArray();
                weaponId = options[MathUtils.random(options.length - 1)];
            }
            else
            {
                weaponId = weaponValue.asString();
            }

            this.weapons.add(weaponId);
        }

        super.read(json, jsonData);
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        json.writeArrayStart("weapons");
        for (String weapon : weapons)
        {
            json.writeValue(weapon);
        }
        json.writeArrayEnd();

        super.write(json, componentWriter, owner);
    }

    @Override
    public ID getID()
    {
        return ID.gungame;
    }

    public Array<String> getGunGameWeapons()
    {
        return weapons;
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
