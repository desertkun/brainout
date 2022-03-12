package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.utils.Compressor;
import com.esotericsoftware.minlog.Log;

public class NewActiveDataMsg
{
    public int id;
    public int d;
    public byte[] data;
    public boolean compressed;

    public NewActiveDataMsg() {}
    public NewActiveDataMsg(ActiveData activeData, ActiveData.ComponentWriter componentWriter, int owner)
    {
        this.id = activeData.getId();
        String data = Data.ComponentSerializer.toJson(activeData, componentWriter, owner);

        if (Log.TRACE) Log.trace("NewActiveDataMsg: " + data);

        compressed = data.length() > 128;

        if (compressed)
        {
            this.data = Compressor.Compress(data);
        }
        else
        {
            this.data = data.getBytes();
        }

        this.d = activeData.getDimensionId();
    }
}
