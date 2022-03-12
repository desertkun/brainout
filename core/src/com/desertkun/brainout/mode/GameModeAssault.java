package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Flag;
import com.desertkun.brainout.data.Data;

public class GameModeAssault<R extends GameModeRealization> extends GameMode<R>
{
    private ObjectMap<Team, Integer> score;
    private ObjectMap<Team, TeamState> alive;
    private int targetScore;
    private State state;
    private int round;

    public static class TeamState implements Json.Serializable
    {
        public int alive;
        public int dead;

        public TeamState() {}

        public TeamState(int alive, int dead)
        {
            this.alive = alive;
            this.dead = dead;
        }

        @Override
        public void write(Json json)
        {
            json.writeValue("a", alive);
            json.writeValue("d", dead);
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            alive = jsonData.getInt("a");
            dead = jsonData.getInt("d");
        }
    }

    public enum State
    {
        none,
        waiting,
        active,
        end
    }

    public GameModeAssault(Class<? extends GameModeRealization> realization)
    {
        super(realization, GameModeAssault.class);

        this.score = new ObjectMap<>();
        this.alive = new ObjectMap<>();
        this.state = State.none;
        this.round = -1;
    }

    public int getPoints(Team team)
    {
        Integer points = this.score.get(team);

        if (points != null)
        {
            return points;
        }

        return 0;
    }

    public TeamState getPlayersStats(Team team)
    {
        if (team == null)
            return null;

        TeamState stats = this.alive.get(team);

        if (stats != null)
        {
            return stats;
        }

        return null;
    }


    public void setPlayersStats(Team team, int alive, int dead)
    {
        this.alive.put(team, new TeamState(alive, dead));
    }

    public Team getBestTeam()
    {
        int max = -1;
        Team best = null;

        for (ObjectMap.Entry<Team, Integer> point : score)
        {
            if (point.value > max)
            {
                max = point.value;
                best = point.key;
            }
        }

        return best;
    }

    public int getScoreSum()
    {
        int sum = 0;

        for (ObjectMap.Entry<Team, Integer> point : score)
        {
            if (point.key instanceof SpectatorTeam)
                continue;

            sum += point.value;
        }

        return sum;
    }


    @Override
    public float getGameProgress()
    {
        int sum = 0;

        for (ObjectMap.Entry<Team, Integer> point : score)
        {
            if (point.key instanceof SpectatorTeam)
                continue;

            sum += point.value;
        }

        return (float)sum / (float) targetScore;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        score.clear();

        if (jsonData.has("score"))
        {
            JsonValue pointsValue = jsonData.get("score");

            if (pointsValue.isObject())
            {
                for (JsonValue point: pointsValue)
                {
                    Team team = ((Team) BrainOut.ContentMgr.get(point.name()));
                    int p = point.asInt();

                    if (team != null)
                    {
                        score.put(team, p);
                    }
                }
            }
        }

        if (jsonData.has("alive"))
        {
            JsonValue aliveValue = jsonData.get("alive");

            if (aliveValue.isObject())
            {
                for (JsonValue alive: aliveValue)
                {
                    Team team = ((Team) BrainOut.ContentMgr.get(alive.name()));

                    if (team != null)
                    {
                        TeamState state = new TeamState();
                        state.read(json, alive);
                        this.alive.put(team, state);
                    }
                }
            }
        }

        targetScore = jsonData.getInt("target", 0);
        round = jsonData.getInt("round", 0);
        state = State.valueOf(jsonData.getString("roundState", State.none.toString()));

        super.read(json, jsonData);
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        json.writeObjectStart("score");

        for (ObjectMap.Entry<Team, Integer> point : score)
        {
            json.writeValue(point.key.getID(), point.value);
        }

        json.writeObjectEnd();
        json.writeObjectStart("alive");

        for (ObjectMap.Entry<Team, TeamState> alive : this.alive)
        {
            json.writeValue(alive.key.getID(), alive.value);
        }
        json.writeObjectEnd();

        json.writeValue("target", targetScore);
        json.writeValue("round", round);
        json.writeValue("roundState", state.toString());

        super.write(json, componentWriter, owner);
    }

    @Override
    public ID getID()
    {
        return ID.assault;
    }

    public ObjectMap<Team, Integer> getScores()
    {
        return score;
    }

    public int getTargetScore()
    {
        return targetScore;
    }

    public void setTargetScore(int targetScore)
    {
        this.targetScore = targetScore;
    }

    public State getState()
    {
        return state;
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
    public boolean enableKillTracking()
    {
        return false;
    }

    @Override
    public boolean countDeaths()
    {
        if (!super.countDeaths())
            return false;

        switch (state)
        {
            case none:
            case waiting:
            case end:
            {
                return false;
            }
            case active:
            default:
            {
                return true;
            }
        }
    }

    public boolean canSpawn()
    {
        switch (state)
        {
            case none:
            case waiting:
            {
                return true;
            }
            case end:
            case active:
            default:
            {
                return false;
            }
        }
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public int getRound()
    {
        return round;
    }

    public void setRound(int round)
    {
        this.round = round;
    }
}
