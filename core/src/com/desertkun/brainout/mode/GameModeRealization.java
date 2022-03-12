package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.playstate.PlayState;

public abstract class GameModeRealization<G extends GameMode> implements EventReceiver
{
    private G gameMode;

    public GameModeRealization(G gameMode)
    {
        this.gameMode = gameMode;
    }

    public void init(PlayState.InitCallback callback)
    {
        if (callback != null)
        {
            callback.done(true);
        }
    }

    public void release() {}
    public abstract void update(float dt);

    public void write(Json json, int owner) {}
    public void read(Json json, JsonValue jsonData) {}

    public G getGameMode()
    {
        return gameMode;
    }

    public abstract boolean isEnemies(int ownerA, int ownerB);

    public boolean isEnemiesActive(ActiveData a, ActiveData b)
    {
        if (a == null || b == null)
        {
            return true;
        }

        return isEnemies(a.getOwnerId(), b.getOwnerId());
    }

    public boolean canSpawn(Spawnable spawnable, Team team)
    {
        return spawnable.canSpawn(team);
    }

    public void openExchangeMenu(PlayerData myPlayerData) {}

    public void currentPlayerDimensionChanged(ActiveData activeData)
    {

    }
}
