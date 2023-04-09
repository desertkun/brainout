package com.desertkun.brainout.plugins;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.GlobalConflict;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.TeamWonEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.ProfileService;
import org.json.JSONObject;

@Reflect("plugins.PortGlobalConflictPlugin")
public class PortGlobalConflictPlugin extends Plugin implements EventReceiver
{
    private String conflictName;

    @Override
    public void init()
    {
        super.init();

        BrainOutServer.EventMgr.subscribe(Event.ID.teamWon, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutServer.EventMgr.unsubscribe(Event.ID.teamWon, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.conflictName = jsonData.getString("conflict");
    }

    @Override
    public boolean onEvent(Event event)
    {
        if (event.getID() == Event.ID.teamWon)
        {
            teamWon(((TeamWonEvent) event).team);
        }

        return false;
    }

    private void teamWon(Team team)
    {
        if (!BrainOut.OnlineEnabled())
            return;

        if (BrainOutServer.Settings.getZone() == null)
            return;

        String zoneName = BrainOutServer.Settings.getZone();
        if (zoneName == null)
            return;

        GlobalConflict conflict = BrainOutServer.ContentMgr.get(conflictName, GlobalConflict.class);
        if (conflict == null)
            return;

        GlobalConflict.Zone zone = conflict.getZoneByKey(zoneName);
        if (zone == null)
            return;

        int countA = 0;
        int countB = 0;

        for (Client client : BrainOutServer.Controller.getClients().values())
        {
            if (!(client instanceof PlayerClient))
                continue;

            if (client.getTeam() != team)
                continue;

            PlayerClient playerClient = ((PlayerClient) client);

            GlobalConflict.Owner owner =
                GlobalConflict.GetAccountOwner(playerClient.getAccount(), playerClient.getClanId(),
                    BrainOutServer.Settings.getLastConflict());

            if (owner == GlobalConflict.Owner.a)
            {
                countA++;
            }

            if (owner == GlobalConflict.Owner.b)
            {
                countB++;
            }
        }

        if (countA == countB)
            return;

        GlobalConflict.Owner won = countA > countB ? GlobalConflict.Owner.a : GlobalConflict.Owner.b;

        LoginService loginService = LoginService.Get();
        ProfileService profileService = ProfileService.Get();

        if (loginService == null || profileService == null)
            return;

        JSONObject cc = new JSONObject();

        JSONObject update = new JSONObject();
        long now = System.currentTimeMillis();
        update.put("conflict", cc);

        JSONObject zoneObject = new JSONObject();
        cc.put(zone.getKey(), zoneObject);

        {
            JSONObject u = new JSONObject();
            zoneObject.put(won.toString(), u);
            u.put("@func", "<");
            u.put("@cond", now);
            u.put("@then", now);
        }

        if (BrainOutServer.Settings.getLastConflict() != 0)
        {
            JSONObject c = new JSONObject();
            cc.put("last", c);
            c.put("@func", "<=");
            c.put("@cond", BrainOutServer.Settings.getLastConflict());
            c.put("@then", BrainOutServer.Settings.getLastConflict());
        }

        profileService.updateMyProfile(loginService.getCurrentAccessToken(), update, null, true,
            (profileService1, request, result, profile) ->
        {
            JSONObject c = profile.optJSONObject("conflict");
            if (c != null)
            {
                long conflictStart = profile.optLong("last");

                GlobalConflict.ConflictData d = conflict.getData(c, conflictStart);

                ObjectMap<String, GlobalConflict.Owner> changedOwners = new ObjectMap<>();

                for (GlobalConflict.ConflictData.ZoneData zoneDataInstance : d.getZones())
                {
                    GlobalConflict.Owner changedOwner = zoneDataInstance.postProcess();
                    if (changedOwner != GlobalConflict.Owner.neutral)
                    {
                        changedOwners.put(zoneDataInstance.getKey(), changedOwner);
                    }
                }

                GlobalConflict.Owner w = d.hasSomeoneWon();
                if (w != GlobalConflict.Owner.neutral)
                {
                    globalWon(w, d, conflict, profileService, loginService, conflictStart);
                }
            }

            if (Log.INFO) Log.info("Zone progress " + zoneName + ": " + result.toString());
        });
    }

    private void globalWon(GlobalConflict.Owner owner, GlobalConflict.ConflictData data, GlobalConflict conflict,
        ProfileService profileService, LoginService loginService, long lastConflict)
    {
        JSONObject cc = new JSONObject();

        for (GlobalConflict.Zone zone : conflict.getZones())
        {
            cc.put(zone.getKey(), JSONObject.NULL);
        }

        {
            long now = System.currentTimeMillis();
            JSONObject newLast = new JSONObject();
            newLast.put("@func", "<");
            newLast.put("@cond", now);
            newLast.put("@then", now);

            cc.put("last", newLast);
        }

        cc.put("prev", lastConflict);
        cc.put("winner", owner.toString());

        JSONObject update = new JSONObject();
        update.put("conflict", cc);

        profileService.updateMyProfile(loginService.getCurrentAccessToken(), update, null, true,
            (profileService1, request, result, profile) ->
        {
            if (Log.INFO) Log.info("Zone concluded team " + owner.toString() + " won: " + result.toString());
        });
    }
}
