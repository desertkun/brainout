package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.online.PlayerRights;
import org.json.JSONObject;

public class NewRemoteClientMsg
{
    public int id;
    public String name, team, avatar, clanAvatar, clanId, info;
    public PlayerRights rights;

    public NewRemoteClientMsg() {}
    public NewRemoteClientMsg(int id, String name, String avatar, String clanAvatar,
                              String clanId, Team team, PlayerRights rights,
                              JSONObject info)
    {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.clanAvatar = clanAvatar;
        this.clanId = clanId;
        this.team = team.getID();
        this.info = info.toString();
        this.rights = rights;
    }
}
