package com.desertkun.brainout.online;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.shop.Shop;
import com.desertkun.brainout.events.PlayerSavedEvent;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.utils.JSONDiff;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.ProfileService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class ClientProfile extends UserProfile
{
    protected ObjectMap<String, FreePlayContainer> containers;

    private WeakReference<PlayerClient> client;
    private JSONObject profile;
    private boolean dirty, sendProfile;
    private boolean locked;
    private final OwnableOwned onOwn;
    private float uploadTimer;

    public enum OnwAction
    {
        owned,
        unlocked
    }

    public interface OwnableOwned
    {
        void owned(OwnableContent content, String reason, OnwAction onwAction, int amount);
    }

    public ClientProfile(PlayerClient client, JSONObject profile, OwnableOwned onOwn)
    {
        this.client = new WeakReference<>(client);
        this.containers = new ObjectMap<>();
        this.profile = profile;

        this.dirty = false;

        this.locked = false;
        this.sendProfile = false;
        this.onOwn = onOwn;
        this.uploadTimer = 0;

        parse();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void parse()
    {
        clear();

        if (profile != null)
        {
            read(profile);
        }
    }

    public ObjectMap<String, FreePlayContainer> getContainers()
    {
        return containers;
    }

    @Override
    public void readExt(JSONObject ext)
    {
        super.readExt(ext);

        if (ext.has("cont"))
        {
            this.containers.clear();

            JSONObject cnt = ext.getJSONObject("cont");
            for (String key : cnt.keySet())
            {
                JSONArray o = cnt.optJSONArray(key);
                if (o == null)
                    continue;
                FreePlayContainer ccc = new FreePlayContainer();
                ccc.read(o);
                this.containers.put(key, ccc);
            }

            // so diff would work
            ext.remove("cont");
        }
    }

    private PlayerClient getClient()
    {
        return client.get();
    }

    public void init()
    {
        PlayerClient client = getClient();
        if (client == null)
            return;

        client.profileInited(this);
    }

    public void setDirty()
    {
        setDirty(false);
    }

    public void setDirty(boolean sendProfile)
    {
        this.dirty = true;
        this.sendProfile = sendProfile;
    }

    public void doSave() throws org.json.JSONException
    {
        if (!dirty)
            return;

        PlayerClient client = getClient();
        if (client == null)
            return;

        client.log("Flushing user profile");

        dirty = false;
        locked = true;

        JSONObject ext = new JSONObject();

        JSONObject items = new JSONObject();
        for (ObjectMap.Entry<String, Integer> entry: getItems())
        {
            items.put(entry.key, entry.value);
        }

        final JSONObject stats = new JSONObject();
        for (ObjectMap.Entry<String, Float> entry: getStats())
        {
            stats.put(entry.key, entry.value);
        }

        final JSONObject slots = new JSONObject();
        for (ObjectMap.Entry<String, String> entry : selectedSlots)
        {
            slots.put(entry.key, entry.value);
        }

        final JSONObject cnt = new JSONObject();
        for (ObjectMap.Entry<String, FreePlayContainer> entry : containers)
        {
            JSONArray out = new JSONArray();

            if (!entry.value.isSaveEmpty())
            {
                entry.value.write(out);
            }

            cnt.put(entry.key, out);
        }

        JSONArray badges = new JSONArray();
        for (String badge : this.badges)
        {
            badges.put(badge);
        }

        JSONArray favorites = new JSONArray();
        for (String favorite : this.favorites)
        {
            favorites.put(favorite);
        }

        JSONObject trophies = new JSONObject();

        for (int i = 0; i < MAX_TROPHIES; i++)
        {
            Trophy trophy = this.trophies.get(i);

            if (trophy != null)
            {
                try
                {
                    if (trophy.isValid())
                    {
                        trophies.put(String.valueOf(i), trophy.write());
                    }
                }
                catch (Exception ignored)
                {
                    trophies.put(String.valueOf(i), "");
                }
            }
            else
            {
                trophies.put(String.valueOf(i), "");
            }
        }

        ext.put("items", items);
        ext.put("stats", stats);
        ext.put("slots", slots);
        ext.put("cont", cnt);
        ext.put("badges", badges);
        ext.put("favorites", favorites);
        ext.put("trophies", trophies);
        ext.put("name", getName());

        ext.put("clan-avatar", getClanAvatar());
        ext.put("clan-avatar", getClanAvatar());
        ext.put("clan-id", getClanId());
        ext.put("layout", layout);

        ext.put("level", getStats().get("level", 1.0f));

        ext.put("last-daily-claim", getLastDailyClaim());
        ext.put("deactivated", getDeactivated());

        JSONObject limits = new JSONObject();
        ext.put("limits", limits);

        for (ObjectMap.Entry<String, Limits> entry : this.limits)
        {
            long now = System.currentTimeMillis() / 1000L;

            if (now > entry.value.time)
                continue;

            JSONObject limit = new JSONObject();

            limit.put("time", entry.value.time);
            limit.put("amount", entry.value.amount);

            limits.put(entry.key, limit);
        }

        dumpProfile(ext);

        BrainOutServer.EventMgr.sendDelayedEvent(PlayerSavedEvent.obtain(client));
    }

    private void dumpProfile(JSONObject ext)
    {
        PlayerClient client = getClient();
        if (client == null)
            return;

        if (!BrainOut.OnlineEnabled())
        {
            locked = false;
            Gdx.files.local("default-profile.json").writeString(ext.toString(4), false, "UTF-8");
            return;
        }

        JSONObject diff = JSONDiff.Diff(profile, ext);

        if (diff == null)
            return;

        ProfileService.Get().updateMyProfile(client.getAccessToken(), diff, null, true,
            (service, request, result, profile1) ->
        {
            locked = false;

            if (result == Request.Result.success)
            {
                profile = profile1;

                if (Log.INFO)
                    Log.info("Profile " + client.getAccessTokenAccount() + " was saved.");
            }
            else
            {
                if (Log.INFO) Log.info("Failed to save profile for " +
                        client.getAccessTokenAccount() + ": " + result.toString());
            }
        });
    }

    public void reset()
    {
        super.clear();
    }

    public void clear()
    {
        super.clear();

        name = "";
    }

    public void itemUnlocked(OwnableContent content)
    {
        if (Shop.getInstance().isFree(content))
        {
            content.addItem(this, 1);
            onOwn.owned(content, "lock-tree", OnwAction.owned, 1);
        }
        else
        {
            onOwn.owned(content, "lock-tree", OnwAction.unlocked, 1);
        }

        setDirty();
    }

    public void update(float dt)
    {
        uploadTimer += dt;

        if (uploadTimer > ServerConstants.Online.PROFILE_UPLOAD_PERIOD)
        {
            if (!locked)
            {
                uploadTimer = 0;

                try
                {
                    doSave();
                }
                catch (JSONException ignored)
                {
                    //
                }
            }
        }

        if (sendProfile)
        {
            PlayerClient client = getClient();
            if (client != null)
            {
                client.sendUserProfile();
            }

            sendProfile = false;
        }
    }

    public void flush()
    {
        if (!locked)
        {
            try
            {
                doSave();
            }
            catch (JSONException ignored)
            {

            }
        }
    }
    @Override
    public void statUpdated(String stat, float newValue, float added)
    {
        PlayerClient client = getClient();
        if (client != null)
        {
            client.statUpdated(stat, newValue, added);
        }
    }
}
