package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.settings.ClientSettings;
import com.desertkun.brainout.client.settings.Properties;
import com.desertkun.brainout.client.settings.Property;
import com.desertkun.brainout.client.states.CSPrivacyPolicy;
import com.desertkun.brainout.client.states.CSQuickPlay;
import com.desertkun.brainout.content.Animation;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.events.SettingsUpdatedEvent;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.managers.GamePadManager;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.popups.RichConfirmationPopup;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tabs;
import com.desertkun.brainout.menu.ui.UIAnimation;
import com.desertkun.brainout.online.Matchmaking;
import com.desertkun.brainout.online.RoomSettings;
import com.desertkun.brainout.online.UserProfile;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONObject;


public class SettingsMenu extends Menu
{
    private boolean onTop;
    private String originalLanguage;
    private boolean restartRequired;

    private Label notConfigured;

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-loading");
    }

    public SettingsMenu()
    {
        onTop = true;
    }

    @Override
    public void onInit()
    {
        originalLanguage = BrainOutClient.LocalizationMgr.getCurrentLanguage();

        super.onInit();
    }

    @Override
    public boolean stayOnTop()
    {
        return onTop;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        TextButton save = new TextButton(L.get("MENU_SAVE"),
            BrainOutClient.Skin, "button-default");

        Tabs tabs = new Tabs(BrainOutClient.Skin);
        tabs.align(Align.left);

        data.add(tabs).size(800, 400).row();

        ClientSettings clientSettings = BrainOutClient.ClientSett;

        for (Property property: clientSettings.getProperties().getProperties())
        {
            Tabs.Tab tab = tabs.addTab(L.get(property.getLocalization()), property).fillButton();

            renderSettings(tab, (Properties)property);
        }

        {
            Tabs.Tab accountTab = tabs.addTab(L.get("MENU_ACCOUNT")).fillButton();
            renderAccountTab(accountTab);
        }

        {
            Tabs.Tab gamePad = tabs.addTab(L.get("MENU_GAMEPAD")).fillButton();
            renderGamePadSettings(gamePad);
        }

        Table buttons = new Table();

        TextButton help = new TextButton(
            L.get("MENU_HELP"),
            BrainOutClient.Skin, "button-default");

        help.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                GameState gs = getGameState();

                if (gs == null)
                    return;

                close();

                gs.pushMenu(new HelpMenu());
            }
        });

        buttons.add(help).size(192, 64).pad(8).padLeft(0).padRight(0);

        if (BrainOutClient.ClientController.isLobby())
        {
            UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

            if (userProfile != null)
            {
                OwnableContent editorPass = BrainOutClient.ContentMgr.get(ClientConstants.Other.MAP_EDITOR_PASS,
                        OwnableContent.class);

                if (editorPass != null && (editorPass.getLockItem() == null || editorPass.getLockItem().isUnlocked(userProfile)))
                {
                    TextButton mapEditor = new TextButton(
                            L.get("MENU_MAP_EDITOR"),
                            BrainOutClient.Skin, "button-default");

                    mapEditor.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            LoginService loginService = LoginService.Get();

                            if (loginService == null)
                                throw new RuntimeException("No login service!");

                            RoomSettings partyRoom = new RoomSettings();

                            partyRoom.init(BrainOutClient.ClientController.getUserProfile(), false);
                            partyRoom.setParty(BrainOutClient.ClientController.getMyAccount());

                            close();

                            BrainOutClient.ClientController.setState(new CSQuickPlay(
                                getEditor(), partyRoom, new Matchmaking.FindGameResult()
                            {
                                @Override
                                public void success(String roomId)
                                {
                                    BrainOutClient.Env.setCurrentRoom(roomId);
                                }

                                @Override
                                public void failed(Request.Result status, Request request)
                                {
                                    //
                                }

                                @Override
                                public void connectionFailed()
                                {

                                }
                            }, false));
                        }
                    });

                    buttons.add(mapEditor).size(192, 64).pad(8).padLeft(0);
                }
            }
        }

        buttons.add(save).right().expandX().size(192, 64).pad(8).padRight(0);

        data.add(buttons).expandX().fillX().row();

        save.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);

                save();
            }
        });

        return data;
    }

    private String getEditor()
    {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? "editor" : "editor2";
    }

    private void renderAccountTab(Tabs.Tab tab)
    {
        tab.clearChildren();
        tab.align(Align.center);

        {
            TextButton privacyPolicy = new TextButton(" " + L.get("MENU_PRIVACY_POLICY") + " ",
                BrainOutClient.Skin, "button-text-clear");

            privacyPolicy.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    onTop = false;
                    popMeAndPushMenu(new PrivacyPolicyMenu(null));
                }
            });

            tab.add(privacyPolicy).minWidth(256).height(32).pad(32).row();
        }

        {
            Label dangerZone = new Label(L.get("MENU_ACCOUNT_DANGER_ZONE"),
                BrainOutClient.Skin, "title-red-bg");
            dangerZone.setAlignment(Align.center);
            tab.add(dangerZone).expandX().fillX().pad(8, 32, 8, 32).row();
        }

        {
            Label hint = new Label(L.get("MENU_DELETE_ACCOUNT_HINT"), BrainOutClient.Skin, "title-gray");
            hint.setWrap(true);
            hint.setAlignment(Align.center);
            tab.add(hint).expandX().fillX().padLeft(32).padRight(32).row();

            TextButton deleteAccount = new TextButton(L.get("MENU_DELETE_ACCOUNT"),
                BrainOutClient.Skin, "button-danger");

            deleteAccount.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    onTop = false;
                    popMeAndPushMenu(new RichConfirmationPopup(L.get("MENU_DELETE_ACCOUNT_CONFIRM"))
                    {
                        @Override
                        public String buttonYes()
                        {
                            return L.get("MENU_DELETE_ACCOUNT");
                        }

                        @Override
                        public String buttonNo()
                        {
                            return L.get("MENU_CANCEL");
                        }

                        @Override
                        public void no()
                        {
                        }

                        @Override
                        public String getTitle()
                        {
                            return L.get("MENU_DELETE_ACCOUNT");
                        }

                        @Override
                        public String buttonStyleNo()
                        {
                            return "button-green";
                        }

                        @Override
                        public String buttonStyleYes()
                        {
                            return "button-danger";
                        }

                        @Override
                        public void yes()
                        {
                            deactivateAccount();
                        }

                        @Override
                        protected float getFade()
                        {
                            return 0.75f;
                        }
                    });
                }
            });

            tab.add(deleteAccount).size(256, 32).pad(8).padTop(16).row();
        }
    }

    private void renderGamePadSettings(Tabs.Tab tab)
    {
        tab.clearChildren();
        tab.setClip(true);

        if (!BrainOutClient.GamePadMgr.isEnabled())
        {
            Label notDetected = new Label(L.get("MENU_GAMEPAD_NOT_DETECTED"),
                BrainOutClient.Skin, "title-gray");

            notDetected.setAlignment(Align.center);

            tab.add(notDetected).expand().fill().row();

            return;

        }

        Label gamePadName = new Label(BrainOutClient.GamePadMgr.getDetectedController().getName(),
                BrainOutClient.Skin, "title-yellow");

        Animation gamePadAnimation = BrainOutClient.ContentMgr.get("anim-controller", Animation.class);

        UIAnimation animation = new UIAnimation(gamePadAnimation);

        ImageButton configure = new ImageButton(BrainOutClient.Skin, "button-upgrades");

        tab.add(configure).size(48, 48).row();

        notConfigured = null;

        if (!BrainOutClient.GamePadMgr.isConfigured())
        {
            notConfigured = new Label(L.get("MENU_NOT_CONFIGURED"), BrainOutClient.Skin, "title-red");

            tab.add(notConfigured).pad(8).row();
        }

        configure.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);

                configure.setVisible(false);

                if (notConfigured != null)
                {
                    notConfigured.setVisible(false);
                }

                BrainOutClient.GamePadMgr.configure(new GamePadManager.ConfigurationCallback()
                {
                    @Override
                    public void step(int step)
                    {
                        animation.getState().setAnimation(0, "anim-" + step, true);
                        gamePadName.setText(L.get("MENU_GAMEPAD_" + step));
                    }

                    @Override
                    public void complete()
                    {
                        configure.setVisible(true);

                        if (notConfigured != null)
                        {
                            notConfigured.remove();
                            notConfigured = null;
                        }

                        animation.getState().setAnimation(0, "anim-default", false);
                        gamePadName.setText(L.get("MENU_GAMEPAD_CONFIGURED"));
                    }
                });
            }
        });

        animation.getState().setAnimation(0, "anim-default", true);
        animation.setScale(16);
        animation.setTouchable(Touchable.disabled);
        animation.getSkeleton().setSkin(BrainOutClient.GamePadMgr.getControllerKind());

        tab.add(animation).size(550, 240).row();

        gamePadName.setAlignment(Align.center);

        tab.add(gamePadName).expandX().fillX().row();
    }

    private void deactivateAccount()
    {
        onTop = false;

        GameState gs = BrainOutClient.getInstance().topState();
        pop();

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        gs.pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();

        BrainOutClient.SocialController.sendRequest("deactivate", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();

                gs.pushMenu(new AccountDeactivatedMenu(() ->
                {
                    BrainOutClient.getInstance().topState().popAll();
                    BrainOutClient.ClientController.setState(new CSPrivacyPolicy(true));
                }));
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
            }
        });
    }

    private void renderSettings(Tabs.Tab tab, Properties properties)
    {
        tab.clearChildren();

        Table settings = new Table();

        for (Property property : properties.getProperties())
        {
            final Actor actor;

            if (property instanceof Property.CheckboxProperty)
            {
                final Property.CheckboxProperty checkboxProperty = ((Property.CheckboxProperty) property);

                final CheckBox checkbox = new CheckBox("", BrainOutClient.Skin, "checkbox-default");
                final boolean isChecked = checkboxProperty.isChecked();

                checkbox.setChecked(isChecked);
                checkbox.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        playSound(MenuSound.select);

                        if (checkboxProperty.check(checkbox.isChecked()))
                        {
                            restartRequired = true;
                        }
                    }
                });

                Table t = new Table();
                t.align(Align.left);
                t.add(checkbox).size(32).left();
                actor = t;
            }
            else if (property instanceof Property.SelectProperty)
            {
                final Property.SelectProperty selectProperty = ((Property.SelectProperty) property);
                String defaultValue = selectProperty.getSelectValue();

                final SelectBox<String> selectBox = new SelectBox<String>(BrainOutClient.Skin, "select-default");
                selectBox.setAlignment(Align.center);
                selectBox.getList().setAlignment(Align.center);

                final OrderedMap<String, String> values = new OrderedMap<String, String>();
                selectProperty.getOptions(values);

                final Array<String> v = new Array<String>();
                for (String key : values.orderedKeys())
                {
                    v.add(values.get(key));
                }

                selectBox.setItems(v);

                if (values.orderedKeys().contains(selectProperty.getSelectValue(), false))
                {
                    selectBox.setSelectedIndex(values.orderedKeys().indexOf(defaultValue, false));
                }
                selectBox.addListener(new ChangeListener()
                {
                    @Override
                    public void changed(ChangeEvent event, Actor actor)
                    {
                        if (selectProperty.selectValue(values.orderedKeys().get(selectBox.getSelectedIndex())))
                        {
                            restartRequired = true;
                        }
                    }
                });

                actor = selectBox;
            }
            else if (property instanceof Property.TrackbarProperty)
            {
                final Property.TrackbarProperty trackbarProperty = ((Property.TrackbarProperty) property);

                final Slider slider = new Slider(trackbarProperty.getMin(), trackbarProperty.getMax(),
                        1, false, BrainOutClient.Skin, "slider-default");

                slider.setValue(trackbarProperty.getValue());

                slider.addListener(new ChangeListener()
                {
                    @Override
                    public void changed(ChangeEvent event, Actor actor)
                    {
                        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT))
                        {
                            playSound(MenuSound.select);
                        }

                        trackbarProperty.setValue((int) slider.getValue());
                        trackbarProperty.update();
                    }
                });

                actor = slider;
            }
            else if (property instanceof Property.KeyProperty)
            {
                final Property.KeyProperty keyProperty = ((Property.KeyProperty) property);

                final TextButton textButton = new TextButton(Input.Keys.toString(keyProperty.getValue()),
                        BrainOutClient.Skin, "button-default");

                textButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        playSound(MenuSound.select);

                        textButton.setText("...");
                        textButton.getStage().setKeyboardFocus(textButton);
                    }

                    @Override
                    public boolean keyDown(InputEvent event, int keycode)
                    {
                        keyProperty.setKeyValue(keycode);
                        textButton.setText(Input.Keys.toString(keyProperty.getValue()));

                        return true;
                    }
                });

                actor = textButton;
            }
            else
            {
                continue;
            }

            addSetting(settings, property.getLocalization(), actor);
        }

        ScrollPane pane = new ScrollPane(settings, BrainOutClient.Skin, "scroll-default");
        pane.setCancelTouchFocus(false);
        pane.setFadeScrollBars(false);

        tab.add(pane).expand().fill().padLeft(80).padRight(80).row();
    }

    private String getMultiLang(String loc)
    {
        if (originalLanguage.equals(BrainOutClient.LocalizationMgr.getCurrentLanguage()))
        {
            return L.get(loc);
        }

        return  L.get(loc) + " (" + L.getForLanguage(loc, originalLanguage) + ")";
    }

    private void addSetting(Table settings, String l, Actor actor)
    {
        settings.add(new Label(L.get(l), BrainOutClient.Skin, "title-small")).pad(4).expandX().right();
        settings.add(actor).pad(4).minSize(32, 32).expandX().fillX().left().row();
    }

    private void save()
    {
        apply();

        if (restartRequired)
        {
            onTop = false;

            String message, ok;

            if (!originalLanguage.equals(BrainOutClient.LocalizationMgr.getCurrentLanguage()))
            {
                message = L.get("MENU_APP_RESTART_REQUIRED") + "\n\n" +
                    L.getForLanguage("MENU_APP_RESTART_REQUIRED", originalLanguage);
                ok = L.get("MENU_OK") + " | " + L.getForLanguage("MENU_OK", originalLanguage);
            }
            else
            {
                message = L.get("MENU_APP_RESTART_REQUIRED");
                ok = L.get("MENU_OK");
            }

            popMeAndPushMenu(new AlertPopup(message)
            {
                @Override
                public String getOKText()
                {
                    return ok;
                }
            });
        }
        else
        {
            close();
        }
    }

    private void close()
    {
        onTop = false;
        pop();
    }

    @Override
    public boolean escape()
    {
        close();
        return true;
    }

    private void apply()
    {
        BrainOutClient.ClientSett.save();

        BrainOutClient.EventMgr.sendDelayedEvent(SettingsUpdatedEvent.obtain());
    }

    @Override
    public boolean lockRender()
    {
        return true;
    }
}
