package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.ModeMessage;
import org.json.JSONObject;

public class PartyStartedMsg implements ModeMessage
{
    public String roomId;
    public String key;
    public String host;
    public int[] ports;
    public String settings;

    public PartyStartedMsg() {}
    public PartyStartedMsg(String roomId, String key, String host, int[] ports, JSONObject settings)
    {

        this.roomId = roomId;
        this.key = key;
        this.host = host;
        this.ports = ports;
        this.settings = settings.toString();
    }
}
