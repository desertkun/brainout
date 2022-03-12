package com.desertkun.brainout.server.http;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSEmpty;
import com.desertkun.brainout.server.mapsource.EmptyMapSource;
import com.desertkun.brainout.server.mapsource.StreamMapSource;
import com.desertkun.brainout.utils.ByteArrayUtils;
import com.desertkun.brainout.utils.ExceptionHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class UploadMapHandler implements HttpHandler
{
    @Override
    public void handle(final HttpExchange httpExchange) throws IOException
    {
        BrainOutServer.PostRunnable(() ->
        {
            try
            {
                PlayState playState = BrainOutServer.Controller.getPlayState();

                if (!(playState instanceof ServerPSEmpty))
                {
                    httpExchange.sendResponseHeaders(400, 0);
                    httpExchange.close();
                    return;
                }

                ServerPSEmpty empty = ((ServerPSEmpty) playState);

                ByteArrayInputStream input = new ByteArrayInputStream(
                        ByteArrayUtils.toByteArray(httpExchange.getRequestBody()));

                String mapName = httpExchange.getRequestHeaders().getFirst("X-Map-Name");

                ObjectMap<String, String> custom = new ObjectMap<>();

                for (String key : httpExchange.getRequestHeaders().keySet())
                {
                    if (!key.startsWith("X-custom-"))
                        continue;

                    String value = httpExchange.getRequestHeaders().getFirst(key);
                    key = key.substring(9);

                    custom.put(key, value);
                }

                BrainOutServer.PostRunnable(() ->
                {
                    empty.uploadMap(input, success ->
                    {
                        try
                        {
                            httpExchange.sendResponseHeaders(success ? 200 : 400, 0);
                            httpExchange.close();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }, mapName, custom);
                });
            }
            catch (Exception e)
            {
                try
                {
                    httpExchange.sendResponseHeaders(500, 0);
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
                ExceptionHandler.handle(e, httpExchange.getResponseBody());
            }
        });
    }
}
