package com.desertkun.brainout.playstate;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.common.msg.server.ModeWillFinishInMsg;
import com.desertkun.brainout.common.msg.server.UpdateGlobalContentIndex;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.MapDimensionsGraph;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.packages.ContentPackage;
import com.desertkun.brainout.playstate.special.CandiesGame;
import com.desertkun.brainout.playstate.special.PumpkinsGame;
import com.desertkun.brainout.playstate.special.SpecialGame;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.server.ServerController;
import com.desertkun.brainout.server.ServerSettings;
import com.desertkun.brainout.server.mapsource.MapSource;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.EventService;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONArray;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ServerPSGame extends PlayStateGame
{
    private float checkTime;
    private Array<ServerMap> maps;
    private float finishedTimer;
    private Array<Team> teams;
    private ServerPSEndGame.GameResult gameResult;

    private ServerSettings.GameModeConditions currentMode;
    private ServerSettings.MapConditions currentMap;
    private Array<SpecialGame> specialGames;

    public ServerPSGame()
    {
        this.checkTime = 0;
        this.finishedTimer = 0;
        this.teams = new Array<>();
        this.gameResult = new ServerPSEndGame.ServerGameResult();
        this.specialGames = new Array<>();
    }

    @Override
    public void init(final InitCallback done)
    {
        if (BrainOutServer.Controller.getMapSource() == null)
        {
            throw new RuntimeException("Bad map source");
        }

        BrainOutServer.PackageMgr.loadPackages(() -> packagesLoaded(done));
    }

    private void checkEvents()
    {
        LoginService loginService = LoginService.Get();
        EventService eventService = EventService.Get();

        if (eventService == null || loginService == null)
            return;

        eventService.getEvents(loginService.getCurrentAccessToken(), 0,
            (service, request, result, events) ->
        {
            if (result == Request.Result.success)
            {
                processEvents(events);
            }
        });
    }

    private void processEvents(EventService.EventList events)
    {
        ObjectSet<String> special = new ObjectSet<>();

        for (EventService.Event event : events)
        {
            if (event.data.has("special"))
            {
                JSONArray special_ = event.data.getJSONArray("special");

                for (int i = 0, t = special_.length(); i < t; i++)
                {
                    special.add(special_.getString(i));
                }
            }
        }

        if (!BrainOutServer.Controller.isFreePlay())
        {
            if (special.contains("pumpkins"))
            {
                addSpecialGame(new PumpkinsGame());
            }

            if (special.contains("candies"))
            {
                addSpecialGame(new CandiesGame());
            }
        }
    }

    private void addSpecialGame(SpecialGame game)
    {
        game.init();

        specialGames.add(game);
    }

    public Array<SpecialGame> getSpecialGames()
    {
        return specialGames;
    }

    private void packagesLoaded(final InitCallback done)
    {
        if (Log.INFO) Log.info("Context-related packages loaded.");

        ServerController C = BrainOutServer.Controller;

        C.updateContentIndex();
        C.getClients().sendTCP(new UpdateGlobalContentIndex(C.getContentIndex()));

        MapSource.Settings settings = C.getCurrentSettings();

        currentMode = settings.acquireMode();
        currentMap = settings.acquireMap();

        if (currentMode == null)
        {
            throw new RuntimeException("Bad mode!");
        }

        updateTeams(currentMode);

        if (Log.INFO) Log.info("Server mode: [" + currentMode.mode + "]");

        GameMode gameMode = setMode(currentMode.mode);

        C.getMapSource().loadMode(gameMode, currentMode.getSettings());

        maps = C.loadMaps(currentMap, false);

        if (maps == null)
        {
            if (Log.ERROR) Log.error("Failed to load maps!");

            done.done(false);
            return;
        }

        for (ServerMap map : maps)
        {
            map.init();
        }

        initMode(done);
        inited();

        MapDimensionsGraph.Build();
    }

    public ServerSettings.GameModeConditions getCurrentMode()
    {
        return currentMode;
    }

    public ServerSettings.MapConditions getCurrentMap()
    {
        return currentMap;
    }

    private void inited()
    {
        ServerController C = BrainOutServer.Controller;

        C.getClients().clearHistory();
        C.getClients().updateBalance(true);

        for (ObjectMap.Entry<Integer, Client> entry : C.getClients())
        {
            Client client = entry.value;

            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);
                // Wait 1 second to let player's state to change
                BrainOut.Timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!playerClient.isInitialized()) return;

                        playerClient.setModePayload(
                            ((ServerRealization) getMode().getRealization()).newPlayerPayload(playerClient));
                    }
                }, TimeUnit.SECONDS.toMillis(1));
            }
        }

        if (BrainOut.OnlineEnabled())
        {
            checkEvents();
        }
    }

    private void updateTeams(ServerSettings.GameModeConditions modeConditions)
    {
        teams.clear();
        Array<String> teamsNames;

        if (modeConditions.teams == null)
        {
            teamsNames = BrainOutServer.Settings.getTeamNames();
        }
        else
        {
            teamsNames = modeConditions.teams;
        }

        for (String name : teamsNames)
        {
            Team team = ((Team) BrainOutServer.ContentMgr.get(name));
            teams.add(team);
        }

        ServerController C = BrainOutServer.Controller;
        C.reset();
    }

    @Override
    public void write(Json json, Data.ComponentWriter componentWriter, int owner)
    {
        super.write(json, componentWriter, owner);

        json.writeArrayStart("teams");
        for (Team team : teams)
        {
            if (team == null)
                continue;
            
            json.writeValue(team.getID());
        }
        json.writeArrayEnd();

        json.writeObjectStart("defines");
        for (ObjectMap.Entry<String, String> entry : BrainOutServer.PackageMgr.getDefines())
        {
            json.writeValue(entry.key, entry.value);
        }
        json.writeObjectEnd();

        json.writeArrayStart("packages");
        for (ObjectMap.Entry<String, ContentPackage> entry: BrainOut.PackageMgr.getPackages())
        {
            json.writeObjectStart();

            json.writeValue("name", entry.key);
            json.writeValue("version", entry.value.getVersion());
            long crc32 = entry.value.getCRC32();

            json.writeValue("crc32", crc32, Long.class);

            json.writeObjectEnd();
        }
        json.writeArrayEnd();
    }

    @Override
    public void release()
    {
        super.release();

        for (ObjectMap.Entry<Integer, Client> clientEntry : BrainOutServer.Controller.getClients())
        {
            Client client = clientEntry.value;
            client.store();

            client.kill();
        }

        for (SpecialGame game : specialGames)
        {
            game.release();
        }
        specialGames.clear();

        if (maps != null)
        {
            for (ServerMap map : maps)
            {
                map.dispose();
            }

            maps = null;
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (maps != null)
        {
            for (ServerMap map : maps)
            {
                map.update(dt);
            }
        }

        if (isGameFinished())
        {
            if (finishedTimer > 0)
            {
                finishedTimer -= dt;

                if (finishedTimer <= 0)
                {
                    endGame();
                }
            }
        }
        else
        {
            checkTime -= dt;
            if (checkTime < 0)
            {
                checkTime = ServerConstants.GameMode.CHECK_TIME;

                check();
            }
        }
    }

    public void endGame()
    {
        ServerPSEndGame endGame = new ServerPSEndGame();
        ServerPSEndGame.ServerGameResult serverGameResult = (ServerPSEndGame.ServerGameResult)gameResult;
        serverGameResult.setGameMode(getMode().getID());
        serverGameResult.generateInstrumentInfo();
        endGame.setGameResult(gameResult);
        endGame.generateVotesMaps(3);
        endGame.setRestartIn(BrainOutServer.Settings.getRestartIn());

        BrainOutServer.Controller.updatePlayState(endGame);
    }

    private void check()
    {
        if (getMode() == null)
            return;

        final ServerRealization serverRealization = ((ServerRealization) getMode().getRealization());
        serverRealization.check();

        if (getMode().getEndTime() != 0)
        {
            long now = System.currentTimeMillis() / 1000L;

            if (now > getMode().getEndTime())
            {
                serverRealization.timedOut(gameResult);

                getMode().setPhase(GameMode.Phase.finished);

                finishedTimer = BrainOutServer.Settings.getModeDelay();

                BrainOutServer.PostRunnable(() ->
                {
                    if (serverRealization.hasFinishedTimer())
                    {
                        BrainOutServer.Controller.getClients().sendTCP(new ModeWillFinishInMsg(finishedTimer));
                    }

                    serverRealization.finished();
                });

                return;
            }
        }

        // if some team won then swith to endgame
        if (serverRealization.isComplete(gameResult))
        {
            getMode().setPhase(GameMode.Phase.finished);

            finishedTimer = BrainOutServer.Settings.getModeDelay();

            BrainOutServer.PostRunnable(() ->
            {
                if (serverRealization.hasFinishedTimer())
                {
                    BrainOutServer.Controller.getClients().sendTCP(new ModeWillFinishInMsg(finishedTimer));
                }

                serverRealization.finished();
            });
        }
    }

    public Array<ServerMap> getMaps()
    {
        return maps;
    }

    public void doCheck()
    {
        if (!isGameFinished())
        {
            check();
        }
    }

    public boolean received(Object from, ModeMessage o)
    {
        ServerRealization realization = (ServerRealization) getMode().getRealization();

        return realization.received(from, o) || super.received(from, o);
    }

    public PlayStateEndGame.GameResult getGameResult()
    {
        return gameResult;
    }

    public Array<Team> getTeams()
    {
        return teams;
    }
}
