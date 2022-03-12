package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.utils.MenuUtils;

public class JoinClanMenu extends Menu
{
    private final String clanName;
    private final String avatar;
    private final Runnable callback;

    public JoinClanMenu(String clanName, String avatar, Runnable callback)
    {
        this.clanName = clanName;
        this.avatar = avatar;
        this.callback = callback;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Label title = new Label(L.get("MENU_CLAN_JOIN"), BrainOutClient.Skin, "title-yellow");
            BorderActor borderActor = new BorderActor(title, "form-red");
            data.add(borderActor).expandX().fillX().row();
        }

        {
            Table body = new Table(BrainOutClient.Skin);
            body.setBackground("form-border-red");
            renderBody(body);
            data.add(body).width(530).expand().fill().row();
        }

        {
            Table buttons = new Table();
            renderButtons(buttons);
            data.add(buttons).height(64).expandX().fillX().row();
        }

        return data;
    }

    private void renderButtons(Table buttons)
    {
        {
            TextButton cancel = new TextButton(
                L.get("MENU_CANCEL"), BrainOutClient.Skin, "button-default");

            cancel.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.back);

                    pop();
                }
            });

            buttons.add(cancel).expand().fill().uniformX();
        }
        {
            Button createButton = new Button(BrainOutClient.Skin, "button-green");

            {
                Label createTitle = new Label(L.get("MENU_JOIN"), BrainOutClient.Skin, "title-small");
                createButton.add(createTitle).padRight(32);
            }
            {
                createButton.add(new Label(String.valueOf(getJoinPrice()),
                    BrainOutClient.Skin, "title-small")).padRight(2);
                createButton.add(new Image(BrainOutClient.Skin, MenuUtils.getStatIcon(
                    Constants.Clans.CURRENCY_JOIN_CLAN)));
            }

            createButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    pop();

                    callback.run();
                }
            });

            buttons.add(createButton).expand().fill().uniformX().row();
        }
    }

    private int getJoinPrice()
    {
        return BrainOutClient.ClientController.getPrice("joinClan", 5);
    }

    private void renderBody(Table body)
    {
        {
            Image image;

            if (avatar == null)
            {
                image = new Image(BrainOutClient.getRegion("default-avatar"));
            }
            else
            {
                image = new Image();
                Avatars.Get(avatar, (has, avatar) ->
                {
                    if (!has)
                    {
                        image.setDrawable(BrainOutClient.Skin, "default-avatar");
                        return;
                    }

                    image.setDrawable(new TextureRegionDrawable(new TextureRegion(avatar)));
                });
            }

            image.setBounds(4, 4, 120, 120);
            image.setTouchable(Touchable.disabled);
            body.add(image).pad(32).row();
        }

        {
            Label nameTitle = new Label(clanName, BrainOutClient.Skin, "title-small");
            body.add(nameTitle).expandX().center().pad(32).padTop(0).row();
        }
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }
}
