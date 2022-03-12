package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandleStream;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.StreamUtils;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Popup;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.StaticService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ChangeAvatarMenu extends Popup
{
    private final AvatarChangedCallback callback;
    private final String key;
    private TextField url;
    private Label title;

    public interface AvatarChangedCallback
    {
        void changed(String url);
    }

    public ChangeAvatarMenu(String key, AvatarChangedCallback callback)
    {
        super(L.get("MENU_CLAN_AVATAR_CHANGE_DESC"));

        this.callback = callback;
        this.key = key;

        ArrayMap<String, PopupButtonStyle> buttons = new ArrayMap<>();

        buttons.put(L.get("MENU_CANCEL"), new PopupButtonStyle(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.back);

                pop();
            }
        }));

        buttons.put(L.get("MENU_CHANGE"), new PopupButtonStyle(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);
                check();
            }
        }));

        setButtons(buttons);
    }

    private void check()
    {
        String url = this.url.getText();

        if (url.isEmpty())
            return;

        if (!url.endsWith(".png") && !url.endsWith(".jpg") && !url.endsWith(".jpeg"))
        {
            failed();
            return;
        }

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        Avatars.Clear(url);

        Avatars.Get(url,
            (has, avatar) ->
        {
            waitLoadingMenu.pop();

            if (has)
            {
                if (!renderAvatar(url, avatar))
                {
                    failed();
                }
            }
            else
            {
                failed();
            }
        });
    }

    private boolean renderAvatar(String url, Texture avatar)
    {
        if (avatar.getWidth() < 16 || avatar.getHeight() < 16)
            return false;

        Pixmap original = avatar.getTextureData().consumePixmap();
        original.setFilter(Pixmap.Filter.BiLinear);

        StreamUtils.OptimizedByteArrayOutputStream outputStream =
                new StreamUtils.OptimizedByteArrayOutputStream(4096);

        if (original.getWidth() > 128 || original.getHeight() > 128)
        {
            Pixmap avatarPixmap = new Pixmap(128, 128, original.getFormat());

            avatarPixmap.drawPixmap(original,
                    0, 0, original.getWidth(), original.getHeight(),
                    0, 0, avatarPixmap.getWidth(), avatarPixmap.getHeight()
            );

            PixmapIO.writePNG(new FileHandleStream("")
            {
                @Override
                public OutputStream write(boolean overwrite)
                {
                    return outputStream;
                }
            }, avatarPixmap);

            avatarPixmap.dispose();
        }
        else
        {
            PixmapIO.writePNG(new FileHandleStream("")
            {
                @Override
                public OutputStream write(boolean overwrite)
                {
                    return outputStream;
                }
            }, original);
        }

        upload(new ByteArrayInputStream(outputStream.getBuffer()));

        return true;
    }

    private void upload(InputStream body)
    {
        StaticService staticService = StaticService.Get();
        LoginService loginService = LoginService.Get();

        if (staticService != null && loginService != null)
        {
            WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
            pushMenu(waitLoadingMenu);

            staticService.upload(loginService.getCurrentAccessToken(), body, key,
                (service, request, result, url) -> Gdx.app.postRunnable(() ->
            {
                Gdx.app.postRunnable(() ->
                {
                    waitLoadingMenu.pop();

                    if (result == Request.Result.success)
                    {
                        pop();

                        Avatars.Clear(url);
                        callback.changed(url);
                    }
                    else
                    {
                        failed();
                    }
                });
            }));
        }
    }

    private void failed()
    {
        title.setText(L.get("MENU_AVATAR_FAILED"));
        title.setStyle(BrainOutClient.Skin.get("title-red", Label.LabelStyle.class));
    }

    @Override
    protected String getTitleLabelStyle()
    {
        return "title-yellow";
    }

    @Override
    protected void initContent(Table data)
    {
        title = new Label(this.text, BrainOutClient.Skin, "title-medium");
        title.setWrap(true);
        title.setAlignment(Align.center);

        data.add(title).pad(16).center().expand().fill().row();

        url = new TextField("", BrainOutClient.Skin, "edit-focused");

        data.add(url).pad(20).fillX().expandX().padLeft(64).padRight(64).height(35).row();
        setKeyboardFocus(url);
    }
}
