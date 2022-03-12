package com.desertkun.brainout.mode;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;

public class GameModeFree<R extends GameModeRealization> extends GameMode<R>
{
    private float timeOfDay;
    private float timeOfDayUpdateSpeed;

    public GameModeFree(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeFree.class);

        timeOfDay = 0;
        timeOfDayUpdateSpeed = 0;
    }

    @Override
    public ID getID()
    {
        return ID.free;
    }

    @Override
    public boolean canInviteFriend()
    {
        return false;
    }

    @Override
    public boolean isAutoKickEnabled()
    {
        return false;
    }

    @Override
    public boolean isMagazineManagementEnabled()
    {
        return true;
    }

    @Override
    public boolean canSeePlayerList()
    {
        return false;
    }

    @Override
    public boolean canSeeExchangeMenu()
    {
        return true;
    }

    @Override
    public boolean allowTeamChange()
    {
        return false;
    }

    @Override
    public boolean isEnemies(Team a, Team b)
    {
        return false;
    }

    @Override
    public boolean hasWarmUp()
    {
        return false;
    }

    public static int GetPlayersAlive()
    {
        int amount = 0;

        for (Map map : Map.All())
        {
            amount += map.countActivesForTag(Constants.ActiveTags.PLAYERS, new Map.Predicate()
            {
                @Override
                public boolean match(ActiveData activeData)
                {
                    int ownerId = activeData.getOwnerId();

                    if (ownerId < 0)
                    {
                        return false;
                    }

                    return activeData.isAlive();
                }
            });
        }

        return amount;
    }

    public static boolean IsEnoughPlayersToLeave()
    {
        return GetPlayersAlive() <= 2;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        timeOfDay = jsonData.getFloat("tod", timeOfDay);
        timeOfDayUpdateSpeed = jsonData.getFloat("todu", timeOfDayUpdateSpeed);
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timeOfDay += timeOfDayUpdateSpeed * dt;

        if (timeOfDay > 1)
        {
            timeOfDay = 0;
        }
    }

    public float getTimeOfDay()
    {
        return timeOfDay;
    }

    public boolean isThatTimeOfDay(float time, float range)
    {
        float off = Math.abs(time - timeOfDay);
        if (off > 0.5f) off = 1.0f - off;
        return off < range;
    }

    public boolean isNight()
    {
        return isThatTimeOfDay(0.9f, 0.1f);
    }

    public void setTimeOfDay(float timeOfDay)
    {
        this.timeOfDay = timeOfDay;
    }

    public void setTimeOfDayUpdateSpeed(float timeOfDayUpdateSpeed)
    {
        this.timeOfDayUpdateSpeed = timeOfDayUpdateSpeed;
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        super.write(json, componentWriter, owner);

        json.writeValue("tod", timeOfDay);
        json.writeValue("todu", timeOfDayUpdateSpeed);
    }
}
