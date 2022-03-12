package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.online.Clan;

import java.util.HashSet;
import java.util.Set;

public class ClanPermissionsMenu extends Menu
{
    private final Approve approve;
    private final Set<String> permissions;

    public interface Approve
    {
        void approve(Set<String> permissions);
    }

    public ClanPermissionsMenu(Set<String> permissions, Approve approve)
    {
        this.permissions = new HashSet<>(permissions);
        this.approve = approve;
    }

    @Override
    public Table createUI()
    {
        Table root = new Table(BrainOutClient.Skin);

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("MENU_PERMISSIONS"), BrainOutClient.Skin, "title-yellow");

            header.add(title).expandX().center();

            root.add(header).height(32).expandX().fillX().row();
        }

        Table data = new Table(BrainOutClient.Skin);
        data.setBackground("form-border-red");

        data.add().pad(16).row();

        addPermission(data, Clan.Permissions.CHANGE_SUMMARY, "MENU_ROLE_CHANGE_SUMMARY");
        addPermission(data, Clan.Permissions.SEND_RESOURCES, "MENU_ROLE_SEND_RESOURCES");
        addPermission(data, Clan.Permissions.PARTICIPATE_EVENT, "MENU_ROLE_PARTICIPATE_EVENT");
        addPermission(data, Clan.Permissions.ENGAGE_CONFLICT, "MENU_ROLE_ENGAGE_CONFLICT");

        data.add().pad(16).row();

        root.add(data).expandX().fillX().row();

        {
            Table buttons = new Table();

            {
                TextButton close = new TextButton(L.get("MENU_CLOSE"), BrainOutClient.Skin, "button-default");

                close.addListener(new ClickListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.back);

                        pop();
                    }
                });

                buttons.add(close).uniformX().expandX().fillX().height(64);
            }

            TextButton connect = new TextButton(L.get("MENU_SAVE"),
                BrainOutClient.Skin, "button-green");

            connect.addListener(new ClickListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    pop();

                    approve.approve(calculatePermissions());
                }
            });

            buttons.add(connect).uniformX().expandX().fillX().height(64);
            root.add(buttons).minWidth(384).expandX().fillX().colspan(2).row();
        }

        return root;
    }

    private void addPermission(Table data, String id, String title)
    {
        CheckBox option = new CheckBox(L.get(title), BrainOutClient.Skin, "checkbox-default");
        data.add(option).expandX().left().pad(4, 16, 4, 16).row();

        if (this.permissions.contains(id))
        {
            option.setChecked(true);
        }

        option.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (option.isChecked())
                {
                    permissions.add(id);
                }
                else
                {
                    permissions.remove(id);
                }
            }
        });
    }

    private Set<String> calculatePermissions()
    {
        return permissions;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
