package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.online.RoomSettings;
import org.anthillplatform.runtime.services.GameService;

public class StartPartyMsg implements ModeMessage
{
    public String roomSettings;

    public StartPartyMsg() {}
    public StartPartyMsg(RoomSettings roomSettings)
    {
        GameService.RoomSettings stt = new GameService.RoomSettings();
        roomSettings.write(stt);
        this.roomSettings = stt.getSettings().toString();
    }
}
