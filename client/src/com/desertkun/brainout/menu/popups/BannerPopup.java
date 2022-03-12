package com.desertkun.brainout.menu.popups;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.ui.Avatars;

public class BannerPopup extends ConfirmationPopup
{
    public interface DownloadResult
    {
        void complete(boolean success, BannerPopup self);
    }

    private Texture texture;
    private Image image;


    public BannerPopup(String url, DownloadResult result)
    {
        super("");

        download(url, result);
    }

    private void download(String url, DownloadResult callback)
    {
        Net.HttpRequest request = new Net.HttpRequest("GET");
        request.setUrl(url);
        request.setFollowRedirects(true);

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
                        texture = new Texture(new Pixmap(result, 0, result.length), true);
                        texture.setFilter(
                                Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);

                        updateImage();

                        callback.complete(true, BannerPopup.this);
                    }
                    catch (Exception e)
                    {
                        System.err.println("Failed to Get banner: " + url);
                        e.printStackTrace();
                        callback.complete(false, BannerPopup.this);
                    }
                });
            }

            @Override
            public void failed(Throwable t)
            {
                System.err.println("Failed to Get banner: " + url);
                t.printStackTrace();
                callback.complete(false, BannerPopup.this);
            }

            @Override
            public void cancelled()
            {
                callback.complete(false, BannerPopup.this);
            }
        });
    }

    @Override
    protected String getTitleLabelStyle()
    {
        return "title-yellow";
    }

    @Override
    protected String getTitleBackgroundStyle()
    {
        return "form-red";
    }

    @Override
    protected String getContentBackgroundStyle()
    {
        return "form-border-red";
    }

    private void updateImage()
    {
        if (image != null)
        {
            image.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
        }
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        if (texture != null)
        {
            texture.dispose();

            texture = null;
        }
    }

    @Override
    protected float getContentWidth()
    {
        return 640;
    }

    @Override
    protected void initContent(Table data)
    {
        if (texture != null)
        {
            image = new Image(texture);
        }
        else
        {
            image = new Image();
        }

        image.setScaling(Scaling.fit);

        data.add(image).expand().fill().row();
    }
}
