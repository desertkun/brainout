package com.desertkun.brainout.server.http;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.components.UserSpriteWithBlocksComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerEditor2Realization;
import com.desertkun.brainout.utils.ExceptionHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class AddImageExtensionHandler implements HttpHandler
{
    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        try
        {
            String ownerKey = httpExchange.getRequestHeaders().getFirst("X-Owner-Key");
            int owner = BrainOutServer.Controller.getOwnerForKey(ownerKey);

            String activeName = httpExchange.getRequestHeaders().getFirst("X-Active");

            if (activeName == null)
            {
                httpExchange.sendResponseHeaders(400, 0);
                return;
            }

            String update = httpExchange.getRequestHeaders().getFirst("X-Update");

            BrainOutServer.PostRunnable(() ->
            {
                Client client = BrainOutServer.Controller.getClients().get(owner);

                Map defaultMap = Map.GetDefault();

                try
                {
                    Active active = BrainOutServer.ContentMgr.get(activeName, Active.class);

                    if (active == null || !active.hasComponent(UserSpriteWithBlocksComponent.class))
                    {
                        httpExchange.sendResponseHeaders(400, 0);
                        return;
                    }

                    if (client == null || defaultMap == null)
                    {
                        httpExchange.sendResponseHeaders(403, 0);
                        return;
                    }

                    GameMode gameMode = BrainOutServer.Controller.getGameMode();

                    if (gameMode == null || !(gameMode.getRealization() instanceof ServerEditor2Realization))
                    {
                        httpExchange.sendResponseHeaders(409, 0);
                        return;
                    }

                    ServerEditor2Realization realization = ((ServerEditor2Realization) gameMode.getRealization());

                    if (realization.addImageExtension(active, update, httpExchange.getRequestBody()))
                    {
                        httpExchange.sendResponseHeaders(200, 0);
                    }
                    else
                    {
                        httpExchange.sendResponseHeaders(400, 0);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }
            });
        }
        catch (Exception e)
        {
            httpExchange.sendResponseHeaders(500, 0);
            ExceptionHandler.handle(e, httpExchange.getResponseBody());
        }
    }
}
