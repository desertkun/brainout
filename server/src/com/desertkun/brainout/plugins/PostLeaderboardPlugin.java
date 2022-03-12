package com.desertkun.brainout.plugins;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.PlayerSavedEvent;
import com.desertkun.brainout.utils.LongName;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LeaderboardService;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import org.json.JSONObject;

@Reflect("plugins.PostLeaderboardPlugin")
public class PostLeaderboardPlugin extends Plugin implements EventReceiver
{
    private String leaderboardName;
    private String statName;
    private String order;
    private String credential;
    private int expireIn;
    private float min;

    @Override
    public void init()
    {
        super.init();

        BrainOutServer.EventMgr.subscribe(Event.ID.playerSaved, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutServer.EventMgr.unsubscribe(Event.ID.playerSaved, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.leaderboardName = jsonData.getString("leaderboard");
        this.credential = jsonData.getString("credential", null);
        this.statName = jsonData.getString("stat");
        this.order = jsonData.getString("leaderboard-order", "desc");
        this.expireIn = jsonData.getInt("expire-in", 3600);
        this.min = jsonData.getFloat("min", 0.0f);
    }

    @Override
    public boolean onEvent(Event event)
    {
        if (event.getID() == Event.ID.playerSaved)
        {
            playerSaved(((PlayerSavedEvent) event).client);
        }

        return false;
    }

    private void playerSaved(Client client)
    {
        if (!BrainOut.OnlineEnabled())
            return;

        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        LeaderboardService leaderboardService = LeaderboardService.Get();

        if (leaderboardService == null)
        {
            if (Log.INFO) Log.info("No leaderboard service!");
            return;
        }

        if (credential != null && playerClient.getAccessTokenCredential() != null &&
                !playerClient.getAccessTokenCredential().startsWith(credential))
        {
            if (Log.INFO) Log.info("Skipping posting to " + leaderboardName +
                " because credential " + playerClient.getAccessTokenCredential() + " does not starts with " +
                credential);
            return;
        }

        if (playerClient.getProfile() == null)
            return;

        float stat = playerClient.getProfile().getStats().get(statName, 0.0f);

        if (stat >= min)
        {
            JSONObject profile = new JSONObject();

            if (playerClient.getAvatar() != null)
                profile.put("avatar", playerClient.getAvatar());

            if (playerClient.getAccessTokenCredential() != null)
                profile.put("credential", playerClient.getAccessTokenCredential());

            leaderboardService.postLeaderboard(
                    playerClient.getAccessToken(),
                    leaderboardName, order,
                    stat, LongName.limit(client.getName(), 45), expireIn, profile, null,
                    (service, request, result) ->
                    {
                        if (result == Request.Result.success)
                        {
                            if (Log.INFO) Log.info("Successfully posted user score: " +
                                    leaderboardName + " " + order + " @" + statName);
                        }
                        else
                        {
                            if (Log.INFO) Log.info("Failed to post user score" +
                                    leaderboardName + " " + order + " @" + statName + ": " +
                                    result.toString());
                        }
                    });
        }
    }
}
