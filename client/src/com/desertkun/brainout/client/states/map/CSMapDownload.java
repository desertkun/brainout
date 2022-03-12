package com.desertkun.brainout.client.states.map;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.http.ContentClient;
import com.desertkun.brainout.client.states.CSError;
import com.desertkun.brainout.client.states.ControllerState;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

public class CSMapDownload extends ControllerState
{
    @Override
    public ID getID()
    {
        return ID.mapDownload;
    }

    public CSMapDownload()
    {
    }

    @Override
    public void init()
    {
        ObjectMap<String, String> headers = new ObjectMap<>();

        headers.put("X-Owner-Key", BrainOutClient.ClientController.getOwnerKey());

        ContentClient.download("map", new ContentClient.DownloadResult()
        {
            @Override
            public void success(byte[] data, Map<String, List<String>> headers)
            {
                try
                {
                    switchTo(new CSMapLoad(new ByteArrayInputStream(data)));
                }
                catch (Exception e)
                {
                    e.printStackTrace();

                    switchTo(new CSError(L.get("ERROR_MAP_LOAD")));
                }
            }

            @Override
            public void failed()
            {
                switchTo(new CSError(L.get("ERROR_MAP_LOAD")));
            }

        }, headers);
    }

    @Override
    public void release()
    {

    }
}
