package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.GameUser;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class InviteMenu extends Menu
{
    private final String header;
    private final FriendCallback callback;
    private boolean onTop;
    private Array<GameUser.Friend> friends;
    private Table friendsData;
    private String filter = "";

    public interface FriendCallback
    {
        void selected(GameUser.Friend friend);
    }

    public InviteMenu(String header, FriendCallback callback)
    {
        this.header = header;
        this.friends = new Array<>();
        this.onTop = true;
        this.callback = callback;
    }

    @Override
    public boolean stayOnTop()
    {
        return onTop;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Label header = new Label(this.header, BrainOutClient.Skin, "title-small");
            header.setAlignment(Align.center);
            data.add(header).expandX().fillX().pad(32).row();
        }

        {
            Label title = new Label(L.get("MENU_FRIEND_LIST"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);

            BorderActor borderActor = new BorderActor(title, "form-gray");
            borderActor.getCell().expandX().fillX();
            data.add(borderActor).expandX().fillX().row();
        }

        {
            Table contents = new Table();
            contents.setSkin(BrainOutClient.Skin);
            contents.setBackground("form-default");
            contents.align(Align.center);

            renderFriends(contents);

            data.add(contents).row();
        }

        {
            TextButton close = new TextButton(L.get("MENU_CLOSE"), BrainOutClient.Skin, "button-default");
            close.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    close();
                }
            });

            data.add(close).center().size(192, 64).pad(32).row();
        }

        return data;
    }

    private void close()
    {
        onTop = false;
        pop();
    }

    @Override
    public boolean lockUpdate()
    {
        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public boolean escape()
    {
        onTop = false;
        pop();
        return true;
    }

    private void renderFriends(Table contents)
    {
        Table filter = new Table();

        TextField filterText = new TextField("", BrainOutClient.Skin, "edit-default");
        filterText.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                InviteMenu.this.filter = filterText.getText().toLowerCase();
                updateFriends();
            }
        });
        setKeyboardFocus(filterText);

        Image searchIcon = new Image(BrainOutClient.getRegion("icon-search"));

        filter.add(filterText).expandX().fillX();
        filter.add(searchIcon).padLeft(-36);

        contents.add(filter).expandX().fillX().pad(8).row();

        friendsData = new Table();

        ScrollPane pane = new ScrollPane(friendsData, BrainOutClient.Skin, "scroll-no-background");
        setScrollFocus(pane);
        contents.add(pane).width(400).minHeight(400).row();

        GameUser gameUser = BrainOutClient.Env.getGameUser();

        if (gameUser.getFriends(friends))
        {
            updateFriends();
        }
        else
        {
            Label noFriends = new Label(L.get("MENU_NO_FRIENDS"), BrainOutClient.Skin, "title-gray");
            friendsData.add(noFriends).expandX().fillX().pad(32);
        }
    }

    private void updateFriends()
    {
        friendsData.clear();

        for (final GameUser.Friend friend : friends)
        {
            if (!filter.isEmpty())
            {
                if (!friend.getName().toLowerCase().contains(filter))
                    continue;
            }

            Button btn = new Button(BrainOutClient.Skin, "button-hoverable");

            Texture avatar = friend.getAvatar();

            if (avatar != null)
            {
                Image image = new Image(avatar);
                btn.add(image).size(48, 48).pad(2).padRight(6);
            }

            Table info = new Table();

            Label name = new Label(friend.getName(), BrainOutClient.Skin, "title-small");
            name.setEllipsis(true);
            name.setWrap(false);
            info.add(name).width(274).row();

            String status;
            String style;

            if (friend.isOnline())
            {
                if (friend.getRoom() != null)
                {
                    status = L.get("MENU_FRIEND_IN_GAME");
                    style = "title-green";
                }
                else
                {
                    status = L.get("MENU_FRIEND_ONLINE");
                    style = "title-light-blue";
                }
            }
            else
            {
                status = L.get("MENU_FRIEND_OFFLINE");
                style = "title-gray";
            }

            Label statusLabel = new Label(status, BrainOutClient.Skin, style);
            info.add(statusLabel).expandX().fillX().row();

            btn.add(info).height(48).expandX().fillX();

            Image iconAdd = new Image(BrainOutClient.getRegion("icon-add"));
            iconAdd.setVisible(false);

            btn.add(iconAdd).pad(4);

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    callback.selected(friend);
                    close();
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                {
                    if (!isOver())
                    {
                        iconAdd.setVisible(true);
                    }

                    super.enter(event, x, y, pointer, fromActor);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                {
                    if (isOver())
                    {
                        iconAdd.setVisible(false);
                    }

                    super.exit(event, x, y, pointer, toActor);
                }
            });

            friendsData.add(btn).width(396).expandX().fillX().row();
        }
    }
}
