package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.utils.ByteArrayUtils;
import com.desertkun.brainout.utils.ByteTextureData;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Avatars
{
    private static ObjectMap<String, Texture> Avatars;

    static
    {
        Avatars = new ObjectMap<>();
    }

    public interface AvatarCallback
    {
        void complete(boolean has, Texture avatar);
    }

    public interface DataRequest
    {
        ByteBuffer getData();
        int getWidth();
        int getHeight();
    }

    public static void Get(DataRequest request, String key, AvatarCallback callback)
    {
        Texture avatar = Avatars.get(key);

        if (avatar != null)
        {
            callback.complete(true, avatar);
            return;
        }

        try
        {
            ByteBuffer data = request.getData();

            if (data == null)
            {
                callback.complete(false, null);
                return;
            }

            avatar = new Texture(new ByteTextureData(request.getWidth(), request.getHeight(), data));
            avatar.setFilter(
                    Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Avatars.put(key, avatar);
            callback.complete(true, avatar);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            callback.complete(false, null);
        }
    }

    public static void GetAndCache(String url, AvatarCallback callback)
    {
        MessageDigest digest = null;
        try
        {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            callback.complete(false, null);
            return;
        }

        String cacheKey = ByteArrayUtils.toHex(digest.digest(url.getBytes(StandardCharsets.UTF_8)));

        {
            Texture avatar = Avatars.get(url);

            if (avatar != null)
            {
                callback.complete(true, avatar);
                return;
            }
        }

        FileHandle cacheDir = Gdx.files.local("cache");
        if (!cacheDir.isDirectory())
        {
            cacheDir.mkdirs();
        }

        FileHandle cachedFile = Gdx.files.local("cache/" + cacheKey);
        if (cachedFile.exists())
        {
            try
            {
                Texture avatar = new Texture(cachedFile, true);
                avatar.setFilter(
                        Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
                Avatars.put(url, avatar);
                callback.complete(true, avatar);
            }
            catch (Exception e)
            {
                try
                {
                    cachedFile.delete();
                }
                catch (Exception ignored)
                {
                    callback.complete(false, null);
                    return;
                }
            }
        }

        Net.HttpRequest request = new Net.HttpRequest("GET");
        request.setUrl(url);
        request.setFollowRedirects(true);

        try
        {
            Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener()
            {
                @Override
                public void handleHttpResponse(Net.HttpResponse httpResponse)
                {
                    byte[] result = httpResponse.getResult();

                    Gdx.app.postRunnable(() ->
                    {
                        try
                        {
                            Texture avatar = new Texture(new Pixmap(result, 0, result.length), true);
                            avatar.setFilter(
                                    Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
                            Avatars.put(url, avatar);
                            callback.complete(true, avatar);
                        }
                        catch (Exception e)
                        {
                            System.err.println("Failed to Get avatar: " + url);
                            e.printStackTrace();
                            callback.complete(false, null);
                            return;
                        }

                        cachedFile.writeBytes(result, false);
                    });
                }

                @Override
                public void failed(Throwable t)
                {
                    System.err.println("Failed to Get avatar: " + url);
                    t.printStackTrace();
                    callback.complete(false, null);
                }

                @Override
                public void cancelled()
                {
                    callback.complete(false, null);
                }
            });
        }
        catch (Exception ignored)
        {
            callback.complete(false, null);
        }
    }

    public static void Get(String url, AvatarCallback callback)
    {
        Texture avatar = Avatars.get(url);

        if (avatar != null)
        {
            callback.complete(true, avatar);
            return;
        }

        Net.HttpRequest request = new Net.HttpRequest("GET");
        request.setUrl(url);
        request.setFollowRedirects(true);

        try
        {
            Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener()
            {
                @Override
                public void handleHttpResponse(Net.HttpResponse httpResponse)
                {
                    byte[] result = httpResponse.getResult();

                    Gdx.app.postRunnable(() ->
                    {
                        try
                        {
                            Texture avatar = new Texture(new Pixmap(result, 0, result.length), true);
                            avatar.setFilter(
                                    Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
                            Avatars.put(url, avatar);
                            callback.complete(true, avatar);
                        }
                        catch (Exception e)
                        {
                            System.err.println("Failed to Get avatar: " + url);
                            e.printStackTrace();
                            callback.complete(false, null);
                        }
                    });
                }

                @Override
                public void failed(Throwable t)
                {
                    System.err.println("Failed to Get avatar: " + url);
                    t.printStackTrace();
                    callback.complete(false, null);
                }

                @Override
                public void cancelled()
                {
                    callback.complete(false, null);
                }
            });
        }
        catch (Exception ignored)
        {
            callback.complete(false, null);
        }
    }

    public static void Clear(String url)
    {
        Texture texture = Avatars.get(url);

        if (texture != null)
        {
            texture.dispose();
            Avatars.remove(url);
        }
    }

    public static void Reset()
    {
        for (ObjectMap.Entry<String, Texture> entry : Avatars)
        {
            entry.value.dispose();
        }

        Avatars.clear();
    }
}
