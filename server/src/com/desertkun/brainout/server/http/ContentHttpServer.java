package com.desertkun.brainout.server.http;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ContentHttpServer
{
    private HttpServer server;

    public ContentHttpServer(int port) throws IOException
    {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

    }

    public void start()
    {
        server.createContext("/map", new MapHandler());
        server.createContext("/map-dimensions", new MapDimensionHandler());
        server.createContext("/add-image", new AddImageExtensionHandler());
        server.createContext("/upload-map", new UploadMapHandler());

        server.setExecutor(Executors.newFixedThreadPool(4));

        server.start();
    }

    public void stop()
    {
        server.stop(0);
    }
}
