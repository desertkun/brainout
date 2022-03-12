package com.desertkun.brainout.playstate;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.mode.GameMode;
import com.esotericsoftware.minlog.Log;

import java.util.HashSet;

public class PlayStateEndGame extends PlayState
{
    private GameMode.ID lastGameMode;
    private GameResult gameResult;
    private float restartIn;

    public final int VOTING_RESULTS_TIME = 8;
    public final int VOTING_ROULETTE_TIME = 4;
    protected Array<VoteMap> votesMaps;
    protected HashSet<Integer> votedPlayers;

    protected int winningMapIndex;

    @Override
    public ID getID()
    {
        return ID.endgame;
    }

    public PlayStateEndGame()
    {
        if (BrainOut.getInstance().getController().getGameMode() != null)
        {
            lastGameMode = BrainOut.getInstance().getController().getGameMode().getID();
        }

        gameResult = new GameResult();
        winningMapIndex = -1;
    }

    public GameMode.ID getLastGameMode()
    {
        return lastGameMode;
    }

    public static class GameResult implements Json.Serializable
    {
        private Team teamWon = null;
        private int playerWon = -1;
        private ObjectMap<Integer, InstrumentInfo> playerInstruments = new ObjectMap<>();

        @Override
        public void write(Json json)
        {
            if (playerWon != -1)
            {
                json.writeValue("playerWon", playerWon);
            }

            if (teamWon != null)
            {
                json.writeValue("teamWon", teamWon.getID());
            }

            json.writeObjectStart("inst");
            for (ObjectMap.Entry<Integer, InstrumentInfo> entry : playerInstruments)
            {
                json.writeObjectStart(entry.key.toString());

                InstrumentInfo info = entry.value;

                json.writeValue("instrument", info.instrument.getID());
                json.writeValue("skin", info.skin.getID());
                json.writeObjectStart("upgrades");
                for (ObjectMap.Entry<String, Upgrade> upgrade : info.upgrades)
                {
                    json.writeValue(upgrade.key, upgrade.value.getID());
                }
                json.writeObjectEnd();

                json.writeObjectEnd();
            }
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            if (jsonData.has("teamWon"))
            {
                teamWon = ((Team) BrainOut.ContentMgr.get(jsonData.getString("teamWon")));
            }

            if (jsonData.has("playerWon"))
            {
                playerWon = jsonData.getInt("playerWon");
            }

            if (jsonData.has("inst"))
            {
                for (JsonValue inst : jsonData.get("inst"))
                {
                    InstrumentInfo info = new InstrumentInfo();

                    info.instrument = BrainOut.ContentMgr.get(inst.getString("instrument"), Instrument.class);
                    info.skin = BrainOut.ContentMgr.get(inst.getString("skin"), Skin.class);

                    for (JsonValue upgrade : inst.get("upgrades"))
                    {
                        info.upgrades.put(upgrade.name, BrainOut.ContentMgr.get(upgrade.asString(), Upgrade.class));
                    }

                    getPlayerInstruments().put(Integer.valueOf(inst.name()), info);
                }

            }
        }

        public Team getTeamWon()
        {
            return teamWon;
        }

        public ObjectMap<Integer, InstrumentInfo> getPlayerInstruments()
        {
            return playerInstruments;
        }

        public int getPlayerWon()
        {
            return playerWon;
        }

        public void setPlayerWon(int playerWon)
        {
            this.playerWon = playerWon;
        }

        public void setTeamWon(Team teamWon)
        {
            this.teamWon = teamWon;
        }

        public boolean hasTeamWon()
        {
            return teamWon != null;
        }

        public boolean hasPlayerWon()
        {
            return playerWon != -1;
        }
    }

    public static class VoteMap implements Json.Serializable
    {
        public String mapName;
        public GameMode.ID mapMode;
        public int votes;

        public VoteMap()
        {

        }

        public VoteMap(String mapName, GameMode.ID mapMode, int votes)
        {
            this.mapName = mapName;
            this.mapMode = mapMode;
            this.votes = votes;
        }

        @Override
        public void write(Json json)
        {
            json.writeValue("mapName", mapName);
            json.writeValue("mapMode", mapMode.name());
            json.writeValue("votes", votes);
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            if (!jsonData.has("mapName"))
            {
                Log.error("Can't find map name for voting");
                return;
            }

            mapName = jsonData.getString("mapName");
            mapMode = GameMode.ID.valueOf(jsonData.getString("mapMode", GameMode.ID.deathmatch.name()));
            votes = jsonData.getInt("votes", 0);
        }
    }

    public Array<VoteMap> getVotesMaps()
    {
        return votesMaps;
    }

    public HashSet<Integer> getVotedPlayers()
    {
        return votedPlayers;
    }

    public int getWinningMapIndex()
    {
        return winningMapIndex;
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        super.write(json, componentWriter, owner);

        json.writeValue("result", gameResult);
        json.writeValue("restartIn", restartIn);

        if (votesMaps != null)
        {
            json.writeArrayStart("votesMaps");
            for (VoteMap voteMap : votesMaps)
            {
                json.writeValue(voteMap);
            }
            json.writeArrayEnd();
        }

        if (votedPlayers != null)
        {
            json.writeArrayStart("votedPlayers");
            for (Integer votedPlayer : votedPlayers)
            {
                json.writeValue(votedPlayer);
            }
            json.writeArrayEnd();
        }

        if (winningMapIndex >= 0)
        {
            json.writeValue("winningMap", winningMapIndex);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("result"))
        {
            gameResult.read(json, jsonData.get("result"));
        }

        restartIn = jsonData.getFloat("restartIn", 0);

        if (jsonData.has("votesMaps"))
        {
            votesMaps = new Array<>();

            JsonValue voteMapJson = jsonData.get("votesMaps").child;
            while (voteMapJson != null)
            {
                VoteMap voteMap = new VoteMap();
                voteMap.read(json, voteMapJson);
                votesMaps.add(voteMap);
                voteMapJson = voteMapJson.next;
            }
        }

        if (jsonData.has("votedPlayers"))
        {
            votedPlayers = new HashSet<>();

            JsonValue votedPlayer = jsonData.get("votedPlayers").child;
            while (votedPlayer != null)
            {
                votedPlayers.add(votedPlayer.asInt());
                votedPlayer = votedPlayer.next;
            }
        }

        if (jsonData.has("winningMap"))
        {
            winningMapIndex = jsonData.getInt("winningMap");
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        restartIn -= dt;
    }

    public GameResult getGameResult()
    {
        return gameResult;
    }

    public void setGameResult(GameResult gameResult)
    {
        this.gameResult = gameResult;
    }

    public float getRestartIn()
    {
        return restartIn;
    }

    public void setRestartIn(float restartIn)
    {
        this.restartIn = restartIn;
    }
}
