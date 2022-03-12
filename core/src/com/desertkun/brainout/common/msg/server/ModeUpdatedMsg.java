package com.desertkun.brainout.common.msg.server;

import com.badlogic.gdx.utils.Json;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.mode.GameMode;

import java.io.StringWriter;

public class ModeUpdatedMsg implements ModeMessage
{
    public String data;

    public ModeUpdatedMsg() {}
    public ModeUpdatedMsg(GameMode gameMode, int ownerContext)
    {
        this.data = Data.ComponentSerializer.toJson(gameMode, Data.ComponentWriter.TRUE, ownerContext);
    }
}
