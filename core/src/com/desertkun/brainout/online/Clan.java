package com.desertkun.brainout.online;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import org.anthillplatform.runtime.services.SocialService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Clan
{
    public static class Permissions
    {
        public static final String CHANGE_SUMMARY = "change_summary";
        public static final String SEND_RESOURCES = "send_resources";
        public static final String PARTICIPATE_EVENT = "participate_event";
        public static final String ENGAGE_CONFLICT = "engage_conflict";

        public static final int ROLE_DEFAULT = 0;
    }

    private final String name;
    private final String id;

    private String description;
    private String avatar;
    private String avatarKey;
    private ObjectMap<String, Float> stats;
    private ObjectMap<String, ClanMember> members;
    private ClanMember owner;
    private SocialService.Group.JoinMethod joinMethod;
    private Array<ClanHistoryRecord> clanHistoryRecords;

    private String conflict;
    private String conflictWith;

    public class ClanHistoryRecord
    {
        private long time;
        private String result;
        private String clanName;
        private String clanId;
        private String clanAvatar;

        public ClanHistoryRecord(JSONObject data)
        {
            this.time = data.optInt("time", 0);
            this.result = data.optString("result", "lost");

            this.clanId = data.optString("clan-with", "0");
            this.clanName = data.optString("clan-with-name", "???");
            this.clanAvatar = data.optString("clan-with-avatar", null);
        }

        public long getTime()
        {
            return time;
        }

        public String getResult()
        {
            return result;
        }

        public String getClanId()
        {
            return clanId;
        }

        public String getClanName()
        {
            return clanName;
        }

        public String getClanAvatar()
        {
            return clanAvatar;
        }
    }

    public class ClanMember
    {
        private String accountId;
        private String name;
        private String credential;
        private String avatar;
        private int role;
        private int rating;
        private Set<String> permissions;
        private ObjectMap<String, Float> stats;

        public ClanMember(String accountId, SocialService.Group.Participant participant)
        {
            this.accountId = accountId;
            this.role = participant.getRole();
            this.permissions = participant.getPermissions();
            this.stats = new ObjectMap<>();

            read(participant.getProfile());
        }

        private void read(JSONObject data)
        {
            if (data == null)
                return;

            this.name = data.optString("name");
            this.avatar = data.optString("avatar");
            this.credential = data.optString("credential");
            this.rating = data.optInt("rating", 0);

            JSONObject stats = data.optJSONObject("stats");
            if (stats != null)
            {
                for (String key : stats.keySet())
                {
                    this.stats.put(key, (float)stats.optDouble(key, 0.0d));
                }
            }
        }

        public String getName()
        {
            return name;
        }

        public String getAvatar()
        {
            return avatar;
        }

        public boolean hasPermission(String action)
        {
            return isOwner() || permissions.contains(action);
        }

        public boolean isLieutenant()
        {
            return isOwner() ||
                hasPermission(Permissions.ENGAGE_CONFLICT) ||
                hasPermission(Permissions.CHANGE_SUMMARY) ||
                hasPermission(Permissions.PARTICIPATE_EVENT) ||
                hasPermission(Permissions.SEND_RESOURCES);
        }

        public boolean isOwner()
        {
            return owner == this;
        }

        public Set<String> getPermissions()
        {
            return permissions;
        }

        public int getRole()
        {
            return role;
        }

        public String getAccountId()
        {
            return accountId;
        }

        public String getCredential()
        {
            return credential;
        }

        public int getRating()
        {
            return rating;
        }

        public ObjectMap<String, Float> getStats()
        {
            return stats;
        }
    }

    public Clan(SocialService.Group group)
    {
        this.stats = new ObjectMap<>();
        this.members = new ObjectMap<>();

        this.name = group.getName();
        this.id = group.getId();
        this.joinMethod = group.getJoinMethod();

        read(group.getProfile());

        for (String accountId : group.getParticipants().keySet())
        {
            this.members.put(accountId, new ClanMember(accountId,
                    group.getParticipants().get(accountId)));
        }

        this.owner = this.members.get(group.getOwner());
    }

    private void read(JSONObject data)
    {
        if (data == null)
            return;

        this.description = data.optString("description", "");
        this.avatar = data.optString("avatar");
        this.avatarKey = data.optString("avatar_key");
        this.conflict = data.optString("conflict");
        this.conflictWith = data.optString("conflict-with");

        JSONObject stats = data.optJSONObject("stats");
        if (stats != null)
        {
            for (String key : stats.keySet())
            {
                this.stats.put(key, (float)stats.optDouble(key, 0.0d));
            }
        }

        JSONArray records = data.optJSONArray("clan-war-history");

        if (records != null)
        {
            this.clanHistoryRecords = new Array<>();

            for (int i = 0, t = records.length(); i < t; i++)
            {
                JSONObject record_ = records.optJSONObject(i);

                if (record_ == null)
                    continue;

                clanHistoryRecords.add(new ClanHistoryRecord(record_));
            }

            clanHistoryRecords.sort((o1, o2) -> (int)(o2.time - o1.time));
        }
    }

    public String getAvatar()
    {
        return avatar;
    }

    public String getName()
    {
        return name;
    }

    public String getId()
    {
        return id;
    }

    public ObjectMap<String, Float> getStats()
    {
        return stats;
    }

    public ObjectMap<String, ClanMember> getMembers()
    {
        return members;
    }

    public ClanMember getOwner()
    {
        return owner;
    }

    public SocialService.Group.JoinMethod getJoinMethod()
    {
        return joinMethod;
    }

    public String getAvatarKey()
    {
        return avatarKey;
    }

    public boolean isInConflict()
    {
        return conflict != null && conflictWith != null && !conflict.isEmpty() && !conflictWith.isEmpty();
    }

    public String getConflictPartyId()
    {
        return conflict;
    }

    public boolean hasDescription()
    {
        return !description.isEmpty();
    }

    public String getDescription()
    {
        return description;
    }

    public void setConflict(String conflict)
    {
        this.conflict = conflict;
    }

    public void setConflictWith(String conflictWith)
    {
        this.conflictWith = conflictWith;
    }

    public String getConflictWith()
    {
        return conflictWith;
    }

    public void resetConflict()
    {
        this.conflict = null;
        this.conflictWith = null;
    }

    public boolean hasClanHistoryRecords()
    {
        return clanHistoryRecords != null && clanHistoryRecords.size > 0;
    }

    public Array<ClanHistoryRecord> getClanHistoryRecords()
    {
        return clanHistoryRecords;
    }
}
