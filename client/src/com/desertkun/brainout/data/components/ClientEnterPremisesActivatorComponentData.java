package com.desertkun.brainout.data.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.http.ContentClient;
import com.desertkun.brainout.common.msg.client.ActivateActiveMsg;
import com.desertkun.brainout.content.Effect;
import com.desertkun.brainout.content.components.ClientActiveActivatorComponent;
import com.desertkun.brainout.content.components.ClientEnterPremisesActivatorComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.EnterPremisesDoorData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.menu.impl.SafeEnterDigitsMenu;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.playstate.PlayStateGame;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.List;

@Reflect("ClientEnterPremisesActivatorComponentData")
@ReflectAlias("data.components.ClientEnterPremisesActivatorComponentData")
public class ClientEnterPremisesActivatorComponentData extends ClientActiveActivatorComponentData<ClientEnterPremisesActivatorComponent>
{
    private final EnterPremisesDoorData d;
    private final ClientEnterPremisesActivatorComponent a;

    public ClientEnterPremisesActivatorComponentData(EnterPremisesDoorData activeData,
        ClientEnterPremisesActivatorComponent activatorComponent)
    {
        super(activeData, activatorComponent);

        d = activeData;
        a = activatorComponent;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean activate(PlayerData playerData)
    {
        if (!test(playerData))
            return false;


        BrainOutClient.getInstance().topState().pushMenu(new SafeEnterDigitsMenu(
            a.getDigits(), a.getEmptyDigit(), a.getBeep(), code ->
        {
            while (code.startsWith("0"))
            {
                code = code.substring(1);
            }

            String id_ = "A" + code;
            SocialController socialController = BrainOutClient.SocialController;

            JSONObject args = new JSONObject();
            args.put("location", d.location);
            args.put("id", id_);
            args.put("dim", d.getDimension());
            args.put("dim-id", d.getId());

            socialController.sendRequest("generate_premises", args, new SocialController.RequestCallback()
            {
                @Override
                public void success(JSONObject response)
                {
                    final Effect effect = ((Effect) BrainOutClient.ContentMgr.get(a.getActivateEffect()));

                    String map = response.getString("map");

                    JSONArray dimensions = response.optJSONArray("dimensions");
                    boolean dontHaveAll = false;

                    for (int i = 0; i < dimensions.length(); i++)
                    {
                        String d = dimensions.getString(i);
                        if (Map.Get(d) == null)
                        {
                            dontHaveAll = true;
                            break;
                        }
                    }

                    if (dontHaveAll)
                    {
                        downloadMaps(dimensions, map);
                    }
                    else
                    {
                        move(map);
                    }

                    if (effect != null)
                    {
                        effect.getSet().launchEffects(new PointLaunchData(playerData.getX(), playerData.getX(), 0,
                            playerData.getDimension()));
                    }
                }

                @Override
                public void error(String reason)
                {
                    final Effect effect = ((Effect) BrainOutClient.ContentMgr.get(a.getDeniedEffect()));

                    if (effect != null)
                    {
                        effect.getSet().launchEffects(new PointLaunchData(playerData.getX(), playerData.getX(), 0,
                            playerData.getDimension()));
                    }
                }
            });
        }, 3));

        return true;
    }

    private void move(String map)
    {
        BrainOutClient.ClientController.sendTCP(
            new ActivateActiveMsg(getActiveData().getId(), map));
    }

    private void downloadMaps(JSONArray dimensions, String map)
    {
        ObjectMap<String, String> headers = new ObjectMap<>();

        headers.put("X-Owner-Key", BrainOutClient.ClientController.getOwnerKey());
        headers.put("X-Dimensions", dimensions.toString());
        headers.put("X-Default-Dimension", map);

        ContentClient.download("map-dimensions", new ContentClient.DownloadResult()
        {
            @Override
            public void success(byte[] data, java.util.Map<String, List<String>> headers)
            {
                try
                {
                    ByteArrayInputStream bt = new ByteArrayInputStream(data);

                    PlayStateGame game = ((PlayStateGame) BrainOutClient.ClientController.getPlayState());

                    ClientRealization clientRealization = ((ClientRealization) game.getMode().getRealization());
                    Class<ClientMap> mapClass = clientRealization.getMapClass();

                    final Array<ClientMap> maps = BrainOut.loadMapsFromStreamDimension(bt, mapClass, map);

                    if (Log.INFO) Log.info("Downloaded dimensions:");

                    for (ClientMap map : maps)
                    {
                        if (Log.INFO) Log.info(map.getDimension());
                        map.init();
                    }

                    move(map);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed()
            {
                if (Log.ERROR) Log.error("Failed to download dimensions");
            }

        }, headers);

        return;
    }

    @Override
    public boolean test(PlayerData playerData)
    {
        return true;
    }
}
