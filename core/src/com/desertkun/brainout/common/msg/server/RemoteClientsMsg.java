package com.desertkun.brainout.common.msg.server;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.online.PlayerRights;
import org.json.JSONObject;

public class RemoteClientsMsg
{
    public RemotePlayer[] players;

    public static class RemotePlayer
    {
        public int id;
        public String name;
        public String avatar;
        public String clanAvatar;
        public String clanId;
        public String team;
        public String info;
        public PlayerRights rights;

        public RemotePlayer() {}
        public RemotePlayer(int id, String name, String avatar, String clanAvatar, String clanId, Team team,
                            PlayerRights rights, JSONObject info)
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

    public RemoteClientsMsg() {}
    public RemoteClientsMsg(Array<RemotePlayer> players)
    {
        this.players = players.toArray(RemotePlayer.class);
    }
}
