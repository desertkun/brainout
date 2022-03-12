package com.desertkun.brainout.editor2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.http.ContentClient;
import com.desertkun.brainout.common.msg.client.editor2.RemoveUserImageMsg;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.LoadingBlock;

import java.util.List;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class UploadCustomImageMenu extends Menu
{
    private final String updateRegion;
    private final Active active;
    private boolean lock;
    private Table progress;


    public UploadCustomImageMenu(Active active, String updateRegion)
    {
        this.active = active;
        this.updateRegion = updateRegion;
    }

    public UploadCustomImageMenu(Active active)
    {
        this.active = active;
        this.updateRegion = null;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Table images = new Table();

            Image a = new Image(BrainOutClient.Skin, "user-image-sizes");
            images.add(a).padRight(8);

            Image b = new Image(BrainOutClient.Skin, "user-image-sizes-ui");
            images.add(b).padLeft(8);

            data.add(images).pad(16).row();
        }

        {
            Label description = new Label(L.get("EDITOR2_UPLOAD_USER_IMAGE_DESC"), BrainOutClient.Skin, "title-small");
            description.setWrap(true);
            description.setAlignment(Align.center);
            data.add(description).width(600).pad(32).row();
        }

        {
            progress = new Table();
            data.add(progress).pad(32).row();
        }

        if (updateRegion == null)
        {
            Table buttons = new Table();

            {
                TextButton check = new TextButton(
                    L.get("EDITOR2_UPLOAD_USER_IMAGE"), BrainOutClient.Skin, "button-green");

                check.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        check();
                    }
                });

                buttons.add(check).size(256, 64).pad(8);
            }

            {
                TextButton cancel = new TextButton(L.get("MENU_CANCEL"), BrainOutClient.Skin, "button-default");

                cancel.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.back);

                        pop();
                    }
                });

                buttons.add(cancel).size(192, 64).pad(8);
            }

            data.add(buttons).row();
        }
        else
        {
            Table buttons = new Table();

            {
                TextButton check = new TextButton(
                    L.get("EDITOR2_UPDATE_USER_IMAGE"), BrainOutClient.Skin, "button-green");

                check.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        check();
                    }
                });

                buttons.add(check).size(192, 64).pad(8);
            }

            {
                TextButton delete = new TextButton(L.get("MENU_DELETE"), BrainOutClient.Skin, "button-danger");

                delete.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pushMenu(new ConfirmationPopup(L.get("EDITOR2_DELETE_USER_IMAGE_WARNING"))
                        {
                            @Override
                            public void yes()
                            {
                                removeImage();
                            }
                        });
                    }
                });

                buttons.add(delete).size(192, 64).pad(8);
            }


            {
                TextButton cancel = new TextButton(L.get("MENU_CANCEL"), BrainOutClient.Skin, "button-default");

                cancel.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.back);

                        pop();
                    }
                });

                buttons.add(cancel).size(192, 64).pad(8);
            }

            data.add(buttons).row();
        }

        return data;
    }

    private void removeImage()
    {
        BrainOutClient.ClientController.sendTCP(new RemoveUserImageMsg(updateRegion));

        Gdx.app.postRunnable(this::pop);
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    public static BufferedImage toBufferedImage(java.awt.Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    private void check()
    {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            if (contents != null)
            {
                if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                {
                    List files = (List)contents.getTransferData(DataFlavor.javaFileListFlavor);

                    if (files.isEmpty())
                        return;

                    String file = files.get(0).toString();

                    BufferedImage bufferedImage = ImageIO.read(new File(file));

                    if (updateRegion != null)
                    {
                        TextureRegion region = BrainOutClient.Skin.getRegion(updateRegion);

                        if (region.getRegionWidth() != bufferedImage.getWidth() ||
                            region.getRegionHeight() != bufferedImage.getHeight())
                        {
                            String sizes = "" + region.getRegionWidth() + "x" + region.getRegionHeight();

                            renderError(L.get("EDITOR2_IMAGE_UPDATE_SIZE", sizes));
                            return;
                        }
                    }

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "PNG", bos);
                    upload(bos.toByteArray());
                    return;

                }
            }
        } catch (Exception ignored)
        {
            // Ignore JDK crashes sorting data flavors.
        }

        renderError(L.get("EDITOR2_UPLOAD_NONIMAGE"));
    }

    private void renderProgress()
    {
        progress.clear();

        progress.add(new LoadingBlock()).padRight(8);
        progress.add(new Label(L.get("MENU_PLEASE_WAIT"), BrainOutClient.Skin, "title-yellow"));
    }

    private void renderError(String text)
    {
        progress.clear();

        progress.add(new Label(text, BrainOutClient.Skin, "title-red"));
    }

    private void upload(byte[] image)
    {
        if (lock)
            return;

        lock = true;

        renderProgress();

        ObjectMap<String, String> headers = new ObjectMap<>();

        headers.put("X-Owner-Key", BrainOutClient.ClientController.getOwnerKey());
        headers.put("X-Active", active.getID());

        if (updateRegion != null)
        {
            headers.put("X-Update", updateRegion);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(image);

        ContentClient.upload("add-image", bis, image.length, new ContentClient.UploadResult()
        {
            @Override
            public void success()
            {
                Gdx.app.postRunnable(() ->
                {
                    lock = false;
                    pop();
                });
            }

            @Override
            public void failed()
            {
                Gdx.app.postRunnable(() ->
                {
                    lock = false;
                    renderError(L.get("MENU_AVATAR_FAILED"));
                });
            }
        }, headers);
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }
}
