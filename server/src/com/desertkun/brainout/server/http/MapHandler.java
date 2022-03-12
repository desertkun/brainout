package com.desertkun.brainout.server.http;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.server.ServerController;
import com.desertkun.brainout.utils.ExceptionHandler;
import com.esotericsoftware.minlog.Log;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.HttpException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MapHandler implements HttpHandler
{
    @Override
    public void handle(final HttpExchange httpExchange) throws IOException
    {
        String verity = httpExchange.getRequestHeaders().getFirst("X-Verify");
        boolean addSignature = verity != null && !verity.isEmpty();

        String ownerKey = httpExchange.getRequestHeaders().getFirst("X-Owner-Key");
        int owner = BrainOutServer.Controller.getOwnerForKey(ownerKey);

        final Client client = BrainOutServer.Controller.getClients().get(owner);

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

        Data.ComponentWriter componentWriter = client != null ? client : Data.ComponentWriter.TRUE;

        Array<Runnable> toPostpone = new Array<>();

        final ServerController.MapSaveResult result = BrainOutServer.Controller.saveAll(
                BrainOutServer.Controller.getSuitableDimensions(client),
                componentWriter, addSignature, owner, toPostpone::add);

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
