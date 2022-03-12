package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.ComponentWritable;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.playstate.PlayState;
import com.esotericsoftware.minlog.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class GameMode <R extends GameModeRealization> implements EventReceiver, ComponentWritable
{
    private R realization;
    private Phase phase;

    private float timer;
    private Runnable timerComplete;
    private long endTime;

    public enum Phase
    {
        none,
        warmUp,
        game,
        aboutToEnd,
        finished
    }

    public enum ID
    {
        normal,
        editor,
        domination,
        deathmatch,
        ctf,
        assault,
        lobby,
        free,
        editor2,
        foxhunt,
        gungame,
        zombie,
        duel
    }

    @SuppressWarnings({"unchecked", "reduant"})
    public GameMode(Class<? extends GameModeRealization> realization, Class<? extends GameMode> gameModeClass)
    {
        try
        {
            Constructor<? extends GameModeRealization> constructor = realization.getConstructor(gameModeClass);

            this.realization = (R) constructor.newInstance(this);
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        this.phase = Phase.none;
        this.timer = 0;
        this.timerComplete = null;
        this.endTime = 0;
    }

    public R getRealization()
    {
        return realization;
    }

    public void init(PlayState.InitCallback callback)
    {
        realization.init(callback);

        BrainOut.ScheduleGC();
    }

    public void release()
    {
        realization.release();
    }

    public void update(float dt)
    {
        realization.update(dt);

        if (timer > 0)
        {
            timer -= dt;
            if (timer <= 0)
            {
                timer = 0;
                if (timerComplete != null)
                {
                    Runnable temp = timerComplete;
                    timerComplete = null;
                    BrainOut.getInstance().postRunnable(temp);
                }
            }
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        return realization.onEvent(event);
    }

    public abstract ID getID();

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        realization.write(json, owner);

        json.writeValue("phase", phase);
        json.writeValue("timer", timer);

        json.writeValue("et", endTime);
    }

    public void read(Json json, JsonValue jsonData)
    {
        realization.read(json, jsonData);

        phase = Phase.valueOf(jsonData.getString("phase", phase.toString()));
        timer = jsonData.getFloat("timer", 0);

        endTime = jsonData.getLong("et", 0);
    }

    public Phase getPhase()
    {
        return phase;
    }

    public void setPhase(Phase phase)
    {
        if (Log.INFO) Log.info("Updathing phase to: " + (phase != null ? phase.toString() : "null"));
        this.phase = phase;
    }

    public float getTimer()
    {
        return timer;
    }

    public void setTimer(float timer, Runnable onComplete)
    {
        this.timer = timer;
        this.timerComplete = onComplete;
    }

    public void forceTimer()
    {
        this.timer = 0.01f;
    }

    public void cancelTimer()
    {
        this.timerComplete = null;
        this.timer = 0;
    }

    public boolean isEnemies(int ownerA, int ownerB)
    {
        return getRealization().isEnemies(ownerA, ownerB);
    }

    public boolean isEnemies(Team a, Team b)
    {
        return a != b;
    }

    public boolean isEnemiesActive(ActiveData a, ActiveData b)
    {
        return getRealization().isEnemiesActive(a, b);
    }

    public boolean hasWarmUp()
    {
        return true;
    }

    public float getGameProgress()
    {
        return 0;
    }

    public boolean isAboutToEnd()
    {
        return getGameProgress() > Constants.Core.GAME_END_THRESHOLD;
    }

    public boolean isTeamVisibilityEnabled()
    {
        return true;
    }

    public boolean validateActive(Active active)
    {
        return true;
    }

    public boolean isGameActive()
    {
        return isGameActive(false, false);
    }

    public boolean countDeaths()
    {
        return isGameActive();
    }

    public boolean enableKillTracking()
    {
        return true;
    }

    public boolean isGameActive(boolean countFinished, boolean countWarmUp)
    {
        switch (getPhase())
        {
            case finished:
            {
                return countFinished;
            }
            case warmUp:
            {
                return countWarmUp;
            }
            case game:
            case aboutToEnd:
                return true;

            default:
                return false;
        }
    }

    public boolean isGameFinished()
    {
        return getPhase() == GameMode.Phase.finished;
    }

    public boolean isMagazineManagementEnabled()
    {
        return false;
    }

    public boolean isAutoKickEnabled() { return true; }

    public boolean canSpawn(Spawnable spawnable, Team team)
    {
        if (team instanceof SpectatorTeam)
        {
            return true;
        }

        return realization.canSpawn(spawnable, team);
    }

    public boolean canInviteFriend()
    {
        return true;
    }

    public boolean canSeePlayerList()
    {
        return true;
    }

    public boolean canSeeExchangeMenu()
    {
        return false;
    }

    public void openExchangeMenu(PlayerData myPlayerData)
    {
        getRealization().openExchangeMenu(myPlayerData);
    }

    public boolean allowTeamChange()
    {
        return true;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void resetEndTime()
    {
        this.endTime = 0;
    }

    public void setEndTime(long endTime)
    {
        long now = System.currentTimeMillis() / 1000L;

        this.endTime = now + endTime;
    }
}
