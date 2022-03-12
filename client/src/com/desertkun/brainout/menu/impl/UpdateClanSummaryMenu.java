package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.online.Clan;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.MenuUtils;
import org.anthillplatform.runtime.services.SocialService;
import org.json.JSONObject;

public class UpdateClanSummaryMenu extends Menu
{
    private final Clan clan;
    private final Runnable success;
    private Button saveButton;
    private TextField nameValue;
    private Label saveTitle;
    private SelectBox<UIJoinMethod> joinMethod;

    public UpdateClanSummaryMenu(Clan clan, Runnable success)
    {
        this.clan = clan;
        this.success = success;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Label title = new Label(L.get("MENU_EDIT"), BrainOutClient.Skin, "title-yellow");
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

            buttons.add(cancel).expand().fill().uniformX();
        }
        {
            this.saveButton = new Button(BrainOutClient.Skin, "button-green");

            {
                saveTitle = new Label(L.get("MENU_SAVE"), BrainOutClient.Skin, "title-gray");
                saveButton.add(saveTitle).padRight(32);
            }
            {
                saveButton.add(new Label(String.valueOf(getUpdatePrice()),
                    BrainOutClient.Skin, "title-small")).padRight(2);
                saveButton.add(new Image(BrainOutClient.Skin, MenuUtils.getStatIcon(
                    Constants.Clans.CURRENCY_UPDATE_CLAN)));
            }

            saveButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    if (saveButton.isDisabled())
                        return;

                    Menu.playSound(MenuSound.select);

                    tryUpdateClan();
                }
            });
            saveButton.setDisabled(true);

            buttons.add(saveButton).expand().fill().uniformX().row();
        }
    }

    private void tryUpdateClan()
    {
        String name = nameValue.getText();

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        if (userProfile.getStats().get(Constants.Clans.CURRENCY_UPDATE_CLAN, 0.0f) < getUpdatePrice())
        {
            pushMenu(new AlertPopup(L.get("MENU_NOT_ENOUGH_SKILLPOINTS")));
            return;
        }

        boolean dirty = false;

        JSONObject args = new JSONObject();

        if (!name.equals(clan.getName()))
        {
            args.put("name", name);
            dirty = true;
        }

        if (joinMethod.getSelected().method != clan.getJoinMethod())
        {
            args.put("join_method", joinMethod.getSelected().method.toString());
            dirty = true;
        }

        if (!dirty)
        {
            pop();
            success.run();
            return;
        }

        args.put("clan_id", clan.getId());

        WaitLoadingMenu loadingMenu = new WaitLoadingMenu("");

        pushMenu(loadingMenu);

        BrainOutClient.SocialController.sendRequest("change_clan_summary", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                loadingMenu.pop();
                pop();

                success.run();
            }

            @Override
            public void error(String reason)
            {
                loadingMenu.pop();

                pushMenu(new AlertPopup(L.get("MENU_ONLINE_ERROR", L.get(reason))));
            }
        });
    }

    private int getUpdatePrice()
    {
        return BrainOutClient.ClientController.getPrice("updateClan", 5);
    }

    private class UIJoinMethod
    {
        public final SocialService.Group.JoinMethod method;

        public UIJoinMethod(SocialService.Group.JoinMethod method)
        {
            this.method = method;
        }

        @Override
        public String toString()
        {
            switch (method)
            {
                case invite:
                    return L.get("MENU_CLAN_JOIN_METHOD_INVITE");
                case approve:
                    return L.get("MENU_CLAN_JOIN_METHOD_APPROVE");
                case free:
                default:
                    return L.get("MENU_CLAN_JOIN_METHOD_FREE");
            }
        }
    }

    private void renderBody(Table body)
    {
        {
            Label avatarTitle = new Label(L.get("NEW_CLAN_AVATAR"), BrainOutClient.Skin, "title-small");
            body.add(avatarTitle).expandX().center().padTop(32).padBottom(8).row();

            Image image;

            if (clan.getAvatar() == null || clan.getAvatar().isEmpty())
            {
                image = new Image(BrainOutClient.getRegion("default-avatar"));
            }
            else
            {
                image = new Image();

                Avatars.Get(clan.getAvatar(), (has, avatarTexture) ->
                {
                    if (!has)
                    {
                        image.setDrawable(BrainOutClient.Skin, "default-avatar");
                        return;
                    }

                    image.setDrawable(new TextureRegionDrawable(new TextureRegion(avatarTexture)));
                });
            }

            body.add(image).size(128, 128).center().row();
        }

        {
            Label nameTitle = new Label(L.get("NEW_CLAN_NAME"), BrainOutClient.Skin, "title-small");
            body.add(nameTitle).expandX().center().padTop(64).padBottom(8).row();

            this.nameValue = new TextField(clan.getName(), BrainOutClient.Skin, "edit-yellow");
            body.add(nameValue).expandX().fillX().pad(0, 64, 32, 64).row();
            nameValue.setAlignment(Align.center);
            nameValue.addListener(new ChangeListener()
            {
                @Override
                public void changed(ChangeEvent event, Actor actor)
                {
                    validate(isValid());
                }
            });

            setKeyboardFocus(nameValue);
        }

        {
            Label joinRulesTitle = new Label(L.get("MENU_CLAN_JOIN_METHOD"), BrainOutClient.Skin, "title-small");
            body.add(joinRulesTitle).expandX().center().padBottom(8).row();

            this.joinMethod = new SelectBox<>(BrainOutClient.Skin, "select-badged");

            joinMethod.setItems(
                new UIJoinMethod(SocialService.Group.JoinMethod.free),
                new UIJoinMethod(SocialService.Group.JoinMethod.approve),
                new UIJoinMethod(SocialService.Group.JoinMethod.invite)
            );

            for (UIJoinMethod method : joinMethod.getItems())
            {
                if (method.method == clan.getJoinMethod())
                {
                    joinMethod.setSelected(method);
                    break;
                }
            }

            joinMethod.addListener(new ChangeListener()
            {
                @Override
                public void changed(ChangeEvent event, Actor actor)
                {
                    validate(isValid());
                }
            });

            joinMethod.setAlignment(Align.center);

            body.add(joinMethod).expandX().fillX().pad(0, 64, 32, 64).height(32).row();
        }
    }

    private void validate(boolean valid)
    {
        saveTitle.setStyle(valid ? BrainOutClient.Skin.get("title-small", Label.LabelStyle.class) :
                                     BrainOutClient.Skin.get("title-gray", Label.LabelStyle.class));
        saveButton.setDisabled(!valid);

    }

    private boolean isValid()
    {
        if (nameValue.getText().equals(clan.getName()) &&
            joinMethod.getSelected().method == clan.getJoinMethod())
        {
            return false;
        }

        int l = nameValue.getText().length();
        return l >= 3 && l <= 32;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
