package com.desertkun.brainout.menu.widgets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.InviteMenu;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.online.PlayerRights;

public class PartyFriendsWidget extends Widget
{
    public PartyFriendsWidget(
        float x, float y, float w, float h)
    {
        super(x, y, w, h);
    }

    @Override
    public void init()
    {
        updateFriends();

        BrainOutClient.EventMgr.subscribe(Event.ID.newRemoteClient, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.remoteClientLeft, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.newRemoteClient, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.remoteClientLeft, this);
    }

    private void updateFriends()
    {
        Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.LEVEL);
        RemoteClient remoteClient = BrainOutClient.ClientController.getMyRemoteClient();

        clear();

        int index = 0;

        for (ObjectMap.Entry<Integer, RemoteClient> entry : BrainOutClient.ClientController.getRemoteClients())
        {
            RemoteClient client = entry.value;

            if (client.getId() == BrainOutClient.ClientController.getMyId())
                continue;

            Image avatar;

            if (client.getAvatar() != null && !client.getAvatar().isEmpty())
            {
                avatar = new Image();
                Avatars.Get(client.getAvatar(), (has, av) ->
                {
                    if (has)
                    {
                        avatar.setDrawable(new TextureRegionDrawable(new TextureRegion(av)));
                    }
                    else
                    {
                        avatar.setDrawable(new TextureRegionDrawable(BrainOutClient.getRegion("skillpoints-big")));
                    }
                });
            }
            else
            {
                avatar = new Image(BrainOutClient.getRegion("skillpoints-big"));
            }

            avatar.setBounds(2, 2, 64, 64);
            avatar.setTouchable(Touchable.disabled);

            Button button = new Button(BrainOutClient.Skin, "button-notext");

            button.addActor(avatar);

            Table stats = new Table();
            stats.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

            stats.setBounds(80, 0, 450, 68);
            stats.setVisible(false);

            {
                Levels.Level level = levels.getLevel(client.getLevel());

                TextureRegion levelImage = BrainOutClient.getRegion(level.icon);

                if (levelImage != null)
                {
                    Image image = new Image(levelImage);
                    image.setScaling(Scaling.none);
                    stats.add(image).size(96, 64).pad(4);
                }

                Table rows = new Table();
                stats.add(rows).expandX().fillX().row();

                {
                    Table row = new Table();

                    {
                        Label name = new Label(client.getName(), BrainOutClient.Skin, "title-small");
                        row.add(name).padRight(4).expandX().left().row();
                    }

                    rows.add(row).expandX().fillX().row();
                }

                {
                    Table row = new Table();

                    {
                        Label levelName = new Label(level.name.get(), BrainOutClient.Skin, "title-level");
                        levelName.setAlignment(Align.left);
                        row.add(levelName).expandX().fillX().padRight(4);
                    }
                    {
                        Label levelText = new Label(level.toString(), BrainOutClient.Skin, "title-yellow");
                        levelText.setAlignment(Align.left);
                        row.add(levelText).padRight(12);
                    }

                    rows.add(row).expandX().fillX().row();
                }

            }

            button.addActor(stats);

            Image kick;

            if (remoteClient.getRights() == PlayerRights.owner ||
                remoteClient.getRights() == PlayerRights.admin)
            {
                kick = new Image(BrainOutClient.getRegion("icon-kick"));
                kick.setScaling(Scaling.none);
                kick.setVisible(false);
                kick.setFillParent(true);
                kick.setTouchable(Touchable.disabled);

                button.addActor(kick);
            }
            else
            {
                kick = null;
            }

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.select);

                    if (remoteClient.getRights() == PlayerRights.owner ||
                        remoteClient.getRights() == PlayerRights.admin)
                    {
                        BrainOutClient.ClientController.kickPlayer(client);
                    }
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                {
                    if (!isOver())
                    {
                        if (kick != null)
                        {
                            kick.setVisible(true);
                        }
                        stats.setVisible(true);
                    }

                    super.enter(event, x, y, pointer, fromActor);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                {
                    if (isOver())
                    {
                        if (kick != null)
                        {
                            kick.setVisible(false);
                        }

                        stats.setVisible(false);
                    }

                    super.exit(event, x, y, pointer, toActor);
                }
            });

            add(button).size(68, 68).pad(4).row();

            index++;

            if (index >= 3)
            {
                break;
            }
        }

        for (; index < 3; index++)
        {
            Button button = new Button(BrainOutClient.Skin, "button-notext");

            Image addIcon = new Image(BrainOutClient.getRegion("icon-add"));
            addIcon.setFillParent(true);
            addIcon.setScaling(Scaling.none);
            addIcon.setTouchable(Touchable.disabled);

            button.addActor(addIcon);

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    BrainOutClient.getInstance().topState().pushMenu(
                        new InviteMenu(L.get("MENU_INVITE_TO_SQUAD"),
                        friend -> BrainOutClient.Env.getGameUser().inviteFriend(friend)));
                }
            });

            add(button).size(68, 68).pad(4).row();
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case newRemoteClient:
            case remoteClientLeft:
            {
                updateFriends();
            }
        }

        return super.onEvent(event);
    }
}
