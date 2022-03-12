package com.desertkun.brainout.client.http;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ContentClient
{
    public interface DownloadResult
    {
        void success(byte[] data, Map<String, List<String>> headers);
        void failed();
    }

    public interface UploadResult
    {
        void success();
        void failed();
    }

    public static void upload(String file, InputStream data, long size, final UploadResult result)
    {
        upload(file, data, size, result, null);
    }

    public static void upload(String file, InputStream data, long size, final UploadResult result,
                                ObjectMap<String, String> headers)
    {
        if (BrainOutClient.ClientController.getServerHttpPort() == -1)
        {
            result.failed();
            return;
        }

        String url = "http://" + BrainOutClient.ClientController.getServerLocation() +
                ":" + BrainOutClient.ClientController.getServerHttpPort() + "/" + file;

        Net.HttpRequest request = new Net.HttpRequest("PUT");
        request.setUrl(url);

        if (headers != null)
        {
            for (ObjectMap.Entry<String, String> entry : headers)
            {
                request.setHeader(entry.key, entry.value);
            }
        }

        request.setContent(data, size);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener()
        {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse)
            {
                if (httpResponse.getStatus().getStatusCode() == 200)
                {
                    result.success();
                }
                else
                {
                    result.failed();
                }
            }

            @Override
            public void failed(Throwable t)
            {
                System.err.println("Failed upload: " + url);
                t.printStackTrace();

                result.failed();
            }

            @Override
            public void cancelled()
            {
                result.failed();
            }
        });
    }

    public static void download(String file, final DownloadResult result)
    {
        download(file, result, null);
    }

    public static void download(String file, final DownloadResult result, ObjectMap<String, String> headers)
    {
        if (BrainOutClient.ClientController.getServerHttpPort() == -1)
        {
            result.failed();
            return;
        }

        String url = "http://" + BrainOutClient.ClientController.getServerLocation() +
                ":" + BrainOutClient.ClientController.getServerHttpPort() + "/" + file;

        Net.HttpRequest request = new Net.HttpRequest("GET");
        request.setUrl(url);

        if (headers != null)
        {
            for (ObjectMap.Entry<String, String> entry : headers)
            {
                request.setHeader(entry.key, entry.value);
            }
        }

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener()
        {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse)
            {
                byte[] res = httpResponse.getResult();
                Map<String, List<String>> hdr = httpResponse.getHeaders();
                Gdx.app.postRunnable(() -> result.success(res, hdr));
            }

            @Override
            public void failed(Throwable t)
            {
                System.err.println("Failed to Get: " + url);
                t.printStackTrace();
                Gdx.app.postRunnable(result::failed);
            }

            @Override
            public void cancelled()
            {
                result.failed();
            }
        });
    }
}
