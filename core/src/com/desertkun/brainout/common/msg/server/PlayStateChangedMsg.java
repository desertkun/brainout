package com.desertkun.brainout.common.msg.server;

import com.badlogic.gdx.utils.Json;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.playstate.PlayState;

import java.io.StringWriter;

public class PlayStateChangedMsg
{
    public PlayState.ID id;
    public String data;

    public PlayStateChangedMsg() {}
    public PlayStateChangedMsg(PlayState playState, int ownerContext)
    {
        this.id = playState.getID();
        this.data = Data.ComponentSerializer.toJson(playState, Data.ComponentWriter.TRUE, ownerContext);
    }
}
