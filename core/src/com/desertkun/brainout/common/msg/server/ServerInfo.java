package com.desertkun.brainout.common.msg.server;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.online.Preset;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.utils.Compressor;
import org.json.JSONObject;

public class ServerInfo
{
    public static class KeyValue
    {
        public String name;
        public String value;
    }

    public static class Price
    {
        public String name;
        public int value;
    }

    public PlayState.ID playState;
    public String playData;
    public byte[] userProfile;
    public int maxPlayers;

    public KeyValue[] defines;
    public KeyValue[] levels;
    public Price[] prices;
    public int id;
    public String ownerKey;
    public String preset;
    public String partyId;
    public long time;

    public ServerInfo() {}

    public ServerInfo(PlayState playState, int id,
                      ObjectMap<String, String> levels,
                      ObjectMap<String, String> defines,
                      ObjectMap<String, Integer> prices,
                      UserProfile userProfile,
                      Preset preset,
                      String ownerKey,
                      String partyId,
                      int maxPlayers,
                      long time)
    {
        Json json = BrainOut.R.JSON;

        this.prices = new Price[prices.size];

        int i = 0;
        for (ObjectMap.Entry<String, Integer> entry : prices)
        {
            Price price = new Price();
            price.name = entry.key;
            price.value = entry.value;

            this.prices[i] = price;
            i++;
        }

        this.levels = new KeyValue[levels.size];

        i = 0;
        for (ObjectMap.Entry<String, String> entry : levels)
        {
            KeyValue info = new KeyValue();
            info.name = entry.key;
            info.value = entry.value;

            this.levels[i] = info;
            i++;
        }

        this.playState = playState.getID();
        this.playData = Data.ComponentSerializer.toJson(playState, Data.ComponentWriter.TRUE, id);

        this.id = id;

        this.defines = new KeyValue[defines.size];

        i = 0;
        for (ObjectMap.Entry<String, String> define : defines)
        {
            KeyValue info = new KeyValue();
            info.name = define.key;
            info.value = define.value;

            this.defines[i] = info;
            i++;
        }

        if (preset != null)
        {
            this.preset = new Json().toJson(preset);
        }
        else
        {
            this.preset = null;
        }

        if (userProfile != null)
        {
            JSONObject dump = new JSONObject();
            userProfile.write(dump);
            this.userProfile = Compressor.Compress(dump.toString());
        }
        else
        {
            this.userProfile = null;
        }

        this.ownerKey = ownerKey;
        this.partyId = partyId;
        this.maxPlayers = maxPlayers;
        this.time = time;
    }
}
