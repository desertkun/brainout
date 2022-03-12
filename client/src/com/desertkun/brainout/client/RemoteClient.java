package com.desertkun.brainout.client;

import com.badlogic.gdx.utils.IntArray;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.online.PlayerRights;
import org.json.JSONObject;

public class RemoteClient
{
    private int id;
    private String name;
    private String avatar;
    private String clanAvatar;
    private String clanId;
    private Team team;
    private long ping;
    private float score;
    private int deaths;
    private int kills;
    private int level;
    private PlayerRights rights;
    private IntArray friends;

    private JSONObject info;

    public RemoteClient(int id, String name, String avatar, String clanAvatar, String clanId,
                        Team team, PlayerRights rights, JSONObject info)
    {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.clanAvatar = clanAvatar;
        this.clanId = clanId;
        this.team = team;
        this.ping = 0;
        this.score = 0;
        this.deaths = 0;
        this.kills = 0;
        this.level = 1;
        this.rights = rights;
        this.info = info;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Team getTeam()
    {
        return team;
    }

    public long getPing()
    {
        return ping;
    }

    public void setPing(long ping)
    {
        this.ping = ping;
    }

    public float getScore()
    {
        return score;
    }

    public void setScore(float score)
    {
        this.score = score;
    }

    public int getDeaths()
    {
        return deaths;
    }

    public void setDeaths(int deaths)
    {
        this.deaths = deaths;
    }

    public void setTeam(Team team)
    {
        this.team = team;
    }

    public int getKills()
    {
        return kills;
    }

    public void setKills(int kills)
    {
        this.kills = kills;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public int getLevel()
    {
        return level;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getClanAvatar()
    {
        return clanAvatar;
    }

    public void setClanAvatar(String clanAvatar)
    {
        this.clanAvatar = clanAvatar;
    }

    public String getClanId()
    {
        return clanId;
    }

    public void setClanId(String clanId)
    {
        this.clanId = clanId;
    }

    public PlayerRights getRights()
    {
        return rights;
    }

    public void setRights(PlayerRights rights)
    {
        this.rights = rights;
    }

    public String getAccountId()
    {
        return getInfoString("account", "0");
    }

    public String getCredential()
    {
        return getInfoString("credential", null);
    }

    public String getPartyId()
    {
        return getInfoString("party", null);
    }

    public boolean isReady()
    {
        return getInfoBoolean("ready", false);
    }

    public float getInfoFloat(String key, float def)
    {
        if (info == null)
            return def;

        return info.optFloat(key, def);
    }

    public int getInfoInt(String key, int def)
    {
        if (info == null)
            return def;

        return info.optInt(key, def);
    }

    public String getInfoString(String key, String def)
    {
        if (info == null)
            return def;

        return info.optString(key, def);
    }

    public boolean getInfoBoolean(String key, boolean def)
    {
        if (info == null)
            return def;

        return info.optBoolean(key, def);
    }

    public void setInfoFloat(String key, float v)
    {
        if (this.info == null)
            return;

        this.info.put(key, v);
    }

    public void setInfoInt(String key, int v)
    {
        if (this.info == null)
            return;

        this.info.put(key, v);
    }

    public void setInfoBoolean(String key, boolean v)
    {
        if (this.info == null)
            return;

        this.info.put(key, v);
    }

    public void setInfo(JSONObject info)
    {
        this.info = info;
    }

    public boolean isSpecial()
    {
        return getInfoBoolean("special", false);
    }

    public boolean isBrainPass()
    {
        return getInfoBoolean("bp", false);
    }

    public boolean isFriend(RemoteClient other)
    {
        int g = getInfoInt("friend", -1);
        if (g == -1)
            return false;

        return g == other.getInfoInt("friend", -1);
    }
}
