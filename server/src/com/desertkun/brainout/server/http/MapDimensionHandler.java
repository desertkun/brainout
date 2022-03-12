package com.desertkun.brainout.server.http;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.server.ServerController;
import com.desertkun.brainout.utils.ExceptionHandler;
import com.esotericsoftware.minlog.Log;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashSet;

public class MapDimensionHandler implements HttpHandler
{
    @Override
    public void handle(final HttpExchange httpExchange) throws IOException
    {
        String verity = httpExchange.getRequestHeaders().getFirst("X-Verify");
        boolean addSignature = verity != null && !verity.isEmpty();

        String ownerKey = httpExchange.getRequestHeaders().getFirst("X-Owner-Key");
        int owner = BrainOutServer.Controller.getOwnerForKey(ownerKey);

        final Client client = BrainOutServer.Controller.getClients().get(owner);

        if (client == null)
        {
            if (Log.ERROR) Log.error("Map: Don't know who you are");
            httpExchange.sendResponseHeaders(401, 0);
            httpExchange.close();
            return;
        }

        String mapDimensions = httpExchange.getRequestHeaders().getFirst("X-Dimensions");
        String defaultDimension = httpExchange.getRequestHeaders().getFirst("X-Default-Dimension");

        JSONArray a;
        try
        {
            a = new JSONArray(mapDimensions);
        }
        catch (JSONException e)
        {
            if (Log.ERROR) Log.error("Map: specified malformed dimensions");
            httpExchange.sendResponseHeaders(409, 0);
            httpExchange.close();
            return;
        }

        HashSet<String> h = new HashSet<>();

        for (int i = 0; i < a.length(); i++)
        {
            ServerMap serverMap = Map.Get(a.optString(i, ""), ServerMap.class);

            if (serverMap == null || !serverMap.isPersonalRequestOnly())
            {
                if (Log.ERROR) Log.error("Map: requested non-personal-request map");
                httpExchange.sendResponseHeaders(409, 0);
                httpExchange.close();
                return;
            }

            if (client instanceof PlayerClient)
            {
                if (!serverMap.suitableForPersonalRequestFor(((PlayerClient) client).getAccount()))
                {
                    if (Log.ERROR) Log.error("Map: requested unauthorized map");
                    httpExchange.sendResponseHeaders(409, 0);
                    httpExchange.close();
                    return;
                }
            }

            h.add(serverMap.getDimension());
        }

        if (!h.contains(defaultDimension))
        {
            if (Log.ERROR) Log.error("Map: incorrect default dimension");
            httpExchange.sendResponseHeaders(409, 0);
            httpExchange.close();
            return;
        }

        BrainOutServer.PostRunnable(() ->
        {
            if (Log.INFO) Log.info("New map download request...");

            if (client instanceof PlayerClient)
            {
                if (!client.isInitialized())
                {
                    ((PlayerClient) client).setMapDownloading();
                }
            }

            Map defaultMap = Map.GetDefault();

            if (defaultMap != null)
            {
                httpExchange.getResponseHeaders().add("X-Name", defaultMap.getName());

                for (ObjectMap.Entry<String, String> entry : defaultMap.getCustomItems())
                {
                    httpExchange.getResponseHeaders().add("X-Custom-" + entry.key, entry.value);
                }
            }
        });

        Array<Runnable> toPostpone = new Array<>();

        final ServerController.MapSaveResult result = BrainOutServer.Controller.saveAll(
            h, client, addSignature, owner, defaultDimension, toPostpone::add);

        if (result == null)
        {
            if (Log.ERROR) Log.error("Map: cannot serialize map");

            httpExchange.sendResponseHeaders(500, 0);
            httpExchange.close();
            return;
        }

        for (Runnable runnable : toPostpone)
        {
            BrainOutServer.PostRunnable(runnable);

            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException ignored) {}
        }

        int completed = toPostpone.size;
        toPostpone.clear();
        toPostpone = null;

        BrainOutServer.PostRunnable(() ->
        {
            if (Log.INFO) Log.info("Map: completed " + completed + " jobs");

            synchronized (result)
            {
                result.notify();
            }
        });

        synchronized (result)
        {
            try
            {
                result.wait();
            }
            catch (InterruptedException e)
            {
                httpExchange.sendResponseHeaders(500, 0);
                httpExchange.close();
                e.printStackTrace();
                return;
            }
        }

        byte[] map = result.serialize();

        if (map == null)
        {
            httpExchange.sendResponseHeaders(500, 0);
            httpExchange.close();
            if (Log.ERROR) Log.error("Map: cannot serialize");
            return;
        }

        try
        {
            httpExchange.sendResponseHeaders(200, map.length);
            httpExchange.getResponseBody().write(map);
            httpExchange.close();
        }
        catch (IOException e)
        {
            // ignore it
        }
        catch (Exception e)
        {
            ExceptionHandler.handle(e, httpExchange.getResponseBody());
        }
    }
}
