package com.desertkun.brainout.common.msg.server;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.online.PlayerRights;

public class ClientsInfo implements UdpMessage
{
    public static class PingInfo
    {
        public int id;
        public long ping;
        public float score;
        public int deaths;
        public int kills;
        public String team;
        public int level;
        public PlayerRights rights;

        public PingInfo() {}
        public PingInfo(int id, long ping, float score, int kills, int deaths,
                        String team, int level, PlayerRights rights)
        {
            this.id = id;
            this.ping = ping;
            this.score = score;
            this.deaths = deaths;
            this.team = team;
            this.kills = kills;
            this.level = level;
            this.rights = rights;
        }
    }

    public PingInfo[] info;

    public ClientsInfo() {}
    public ClientsInfo(Array<PingInfo> pingInfo)
    {
        this.info = pingInfo.toArray(PingInfo.class);
    }
}
