package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.Compressor;
import org.json.JSONObject;

public class SpawnRequestMsg
{
    public byte[] data;

    public SpawnRequestMsg() {}
    public SpawnRequestMsg(UserProfile userProfile)
    {
        if (userProfile == null)
        {
            data = null;
        }
        else
        {
            JSONObject jsonObject = new JSONObject();
            userProfile.write(jsonObject);
            this.data = Compressor.Compress(jsonObject.toString());
        }
    }
}
