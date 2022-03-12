package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.PurchaseButton;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.StringFunctions;
import org.anthillplatform.runtime.services.SocialService;
import org.json.JSONObject;

public class CreateClanMenu extends Menu
{
    private final String avatarKey;
    private Button createButton;
    private TextField nameValue;
    private String avatar;
    private SelectBox<UIJoinMethod> joinMethod;

    public CreateClanMenu()
    {
        this.avatarKey = StringFunctions.generate(32);
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Label title = new Label(L.get("MENU_CREATE_CLAN"), BrainOutClient.Skin, "title-yellow");
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
            this.createButton = new PurchaseButton(L.get("MENU_CREATE"),
                BrainOutClient.Skin, "button-green", getCreatePrice(),
                Constants.Clans.CURRENCY_CREATE_CLAN);

            createButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    if (createButton.isDisabled())
                        return;

                    Menu.playSound(MenuSound.select);

                    tryCreateClan(nameValue.getText(), avatar);
                }
            });
            createButton.setDisabled(true);

            buttons.add(createButton).expand().fill().uniformX().row();
        }
    }

    private void tryCreateClan(String name, String avatar)
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        if (userProfile.getStats().get(Constants.User.SKILLPOINTS, 0.0f) < getCreatePrice())
        {
            pushMenu(new AlertPopup(L.get("MENU_NOT_ENOUGH_SKILLPOINTS")));
            return;
        }

        JSONObject args = new JSONObject();
        args.put("name", name);
        args.put("avatar_key", avatarKey);
        args.put("join_method", joinMethod.getSelected().method.toString());

        if (avatar != null && !avatar.isEmpty())
        {
            args.put("avatar", avatar);
        }

        WaitLoadingMenu loadingMenu = new WaitLoadingMenu("");

        pushMenu(loadingMenu);

        BrainOutClient.SocialController.sendRequest("create_clan", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                GameState gs = getGameState();

                if (gs == null)
                    return;

                loadingMenu.pop();
                pop();

                String clanId = response.optString("clan_id");

                if (clanId != null)
                {
                    gs.pushMenu(new ClanMenu(clanId));
                }
            }

            @Override
            public void error(String reason)
            {
                loadingMenu.pop();

                pushMenu(new AlertPopup(L.get("MENU_CLAN_CREATE_FAILED", L.get(reason))));
            }
        });
    }

    private int getCreatePrice()
    {
        return BrainOutClient.ClientController.getPrice("createClan", 10);
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

            Button avatar = new Button(BrainOutClient.Skin, "button-notext");
            body.add(avatar).size(128, 128).center().row();

            Image image = new Image(BrainOutClient.getRegion("default-avatar"));
            image.setBounds(4, 4, 120, 120);
            image.setTouchable(Touchable.disabled);
            avatar.addActor(image);

            Label change = new Label(L.get("MENU_CHANGE"), BrainOutClient.Skin, "title-yellow");
            change.setVisible(false);
            change.setFillParent(true);
            change.setTouchable(Touchable.disabled);
            change.setAlignment(Align.center, Align.center);
            avatar.addActor(change);

            avatar.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    pushMenu(new ChangeAvatarMenu(avatarKey + ".png", url -> Avatars.Get(url, (has, avatar1) ->
                    {
                        if (has)
                        {
                            CreateClanMenu.this.avatar = url;
                            image.setDrawable(new TextureRegionDrawable(new TextureRegion(
                                    avatar1
                            )));
                        }
                        else
                        {
                            CreateClanMenu.this.avatar = null;
                            image.setDrawable(BrainOutClient.Skin, "default-avatar");
                        }
                    })));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                {
                    change.setVisible(false);
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                {
                    change.setVisible(true);
                }
            });
        }

        {
            Label nameTitle = new Label(L.get("NEW_CLAN_NAME"), BrainOutClient.Skin, "title-small");
            body.add(nameTitle).expandX().center().padTop(64).padBottom(8).row();

            this.nameValue = new TextField("", BrainOutClient.Skin, "edit-yellow");
            nameValue.setAlignment(Align.center);
            body.add(nameValue).expandX().fillX().pad(0, 64, 32, 64).row();
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

            joinMethod.setSelectedIndex(0);
            joinMethod.setAlignment(Align.center);

            body.add(joinMethod).expandX().fillX().pad(0, 64, 32, 64).height(32).row();
        }
    }

    private void validate(boolean valid)
    {
        createButton.setDisabled(!valid);

    }

    private boolean isValid()
    {
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
