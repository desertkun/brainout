package com.desertkun.brainout.desktop;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamID;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.GameUser;
import com.desertkun.brainout.client.states.ControllerState;
import com.desertkun.brainout.desktop.client.states.CSSteamInit;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.online.KryoNetworkClient;
import com.desertkun.brainout.online.NetworkClient;
import com.desertkun.brainout.online.NetworkConnectionListener;
import com.esotericsoftware.kryo.Kryo;

import java.util.Map;

public class SteamEnvironment extends DesktopEnvironment implements EventReceiver
{
    public SteamEnvironment(String[] args)
    {
        super(args, new SteamController());
    }

    @Override
    public GameUser newUser()
    {
        return new GameSteamUser();
    }

    @Override
    public String getUniqueId()
    {
        return "steam";
    }

    @Override
    public GameSteamUser getGameUser()
    {
        return (GameSteamUser)super.getGameUser();
    }

    @Override
    public boolean openURI(String uri)
    {
        getGameUser().getSteamFriends().activateGameOverlayToWebPage(uri, SteamFriends.OverlayToWebPageMode.Modal);

        return true;
    }

    @Override
    public boolean openURI(String uri, Runnable done)
    {
        getGameUser().openOverlay(uri, done);

        return true;
    }

    @Override
    public void initOnline()
    {
        BrainOutClient.ClientController.initOnline(new CSSteamInit());
    }

    @Override
    public void init()
    {
        super.init();

        BrainOutClient.EventMgr.subscribe(Event.ID.statUpdated, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.achievementCompleted, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.controller, this);
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (SteamAPI.isSteamRunning())
        {
            SteamAPI.runCallbacks();
        }
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.statUpdated, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.achievementCompleted, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.controller, this);

        SteamAPI.shutdown();
    }

    @Override
    public String getStoreName()
    {
        return "main";
    }

    @Override
    public boolean storeEnabled()
    {
        return true;
    }

    @Override
    public boolean greenlightEnabled()
    {
        return false;
    }

    @Override
    public String getStoreComponent()
    {
        return "steam";
    }

    @Override
    public void getStoreEnvironment(Map<String, String> env)
    {
        super.getStoreEnvironment(env);

        env.put("steam_id", SteamHelper.getSteamIdCredential(getGameUser().getSteamUser().getSteamID()));
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case statUpdated:
            {
                StatUpdatedEvent e = ((StatUpdatedEvent) event);

                if (getGameUser() != null && getGameUser().getSteamUserStats() != null)
                    getGameUser().getSteamUserStats().setStatI(e.statId, ((int) e.value));

                return true;
            }

            case achievementCompleted:
            {
                AchievementCompletedEvent e = ((AchievementCompletedEvent) event);

                getGameUser().getSteamUserStats().setAchievement(e.achievementId);
                getGameUser().getSteamUserStats().storeStats();

                return true;
            }

            case controller:
            {
                ClientControllerEvent e = ((ClientControllerEvent) event);

                if (e.state != null && (
                        e.state.getID() == ControllerState.ID.endGame ||
                                e.state.getID() == ControllerState.ID.onlineInit))
                {
                    getGameUser().getSteamUserStats().storeStats();
                }

                return false;
            }
        }

        return false;
    }

    @Override
    public void pause()
    {
        super.pause();
    }

    @Override
    public void gameStarted(String room)
    {
        super.gameStarted(room);

        getGameUser().getSteamFriends().setRichPresence(
            "connect", "--join-room " + room);
        getGameUser().getSteamFriends().setRichPresence(
            "status", "Ready");
    }

    @Override
    public void gameCompleted()
    {
        super.gameCompleted();

        if (getGameUser() == null || getGameUser().getSteamFriends() == null)
            return;

        getGameUser().getSteamFriends().setRichPresence(
                "connect", "");
        getGameUser().getSteamFriends().setRichPresence(
                "status", "");
    }

    @Override
    public NetworkClient newNetworkClient(Kryo kryo, NetworkConnectionListener listener)
    {
        return new KryoNetworkClient(kryo, listener);
    }
}
