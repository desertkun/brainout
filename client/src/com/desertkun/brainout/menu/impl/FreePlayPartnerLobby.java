package com.desertkun.brainout.menu.impl;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.RichAlertPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.LoadingBlock;
import com.desertkun.brainout.menu.ui.MenuHelper;
import com.desertkun.brainout.online.Matchmaking;
import org.anthillplatform.runtime.services.GameService;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FreePlayPartnerLobby extends Menu
{
    private String partyId;
    private Label statusLabel;
    private Image statusImage;
    private boolean started;
    private boolean suppressErrors;
    private String selectedRegion;

    private GameService.PartySession.Listener partyListener;
    private GameService.PartySession party;
    private TextButton invite;

    public FreePlayPartnerLobby()
    {
        this(null);
    }

    public FreePlayPartnerLobby(String partyId)
    {
        this.partyId = partyId;

        initListener();
    }

    public void setSelectedRegion(String selectedRegion)
    {
        this.selectedRegion = selectedRegion;
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        suppressErrors = true;

        if (party != null)
        {
            party.leave(null);
        }
    }

    @Override
    public void onInit()
    {
        super.onInit();

        MenuHelper.AddCloseButton(this, this::pop);

        {

            Table waiting = new Table();

            if (BrainOutClient.Skin.has("title-small", Label.LabelStyle.class))
            {
                statusLabel = new Label(L.get("MENU_LOADING"),
                        BrainOutClient.Skin, "title-messages-white");
                waiting.add(statusLabel);
            }

            waiting.add(new LoadingBlock()).padLeft(16);

            waiting.setBounds(getWidth() - 288, 32, 224, 16);
            addActor(waiting);
        }

        GameService gameService = GameService.Get();
        LoginService loginService = LoginService.Get();

        if (gameService != null && loginService != null)
        {
            if (partyId != null)
            {
                joinParty(partyId, gameService, loginService);
            }
            else
            {
                createParty(gameService, loginService);
            }
        }

    }

    private void initListener()
    {
        this.partyListener = new GameService.PartySession.Listener()
        {
            @Override
            public void onError(int code, String message, String data)
            {
                if (suppressErrors)
                    return;

                Gdx.app.postRunnable(() ->
                    pushMenu(new RichAlertPopup(L.get("MENU_FREE_PLAY"), L.get("MENU_PROMO_ERROR"))));
            }

            @Override
            public void onError(Exception e)
            {

            }

            @Override
            public void onOpen()
            {

            }

            @Override
            public void onClose(int code, String message, boolean remote)
            {
                Gdx.app.postRunnable(() -> {
                    if (code > 2000 && code != 3411)
                    {
                        if (!suppressErrors)
                        {
                            pushMenu(new RichAlertPopup(L.get("MENU_FREE_PLAY"), L.get("MENU_PROMO_ERROR"))
                            {
                                @Override
                                public void ok()
                                {
                                    FreePlayPartnerLobby.this.pop();
                                }
                            });
                        }
                    }
                    else
                    {
                        pop();
                    }
                });
            }

            @Override
            public void onPartyInfoReceived(GameService.Party party, List<GameService.PartyMember> members)
            {
                partyId = party.getId();

                Gdx.app.postRunnable(() -> partyStarted());
            }

            @Override
            public void onPlayerJoined(GameService.PartyMember member)
            {

            }

            @Override
            public void onPlayerLeft(GameService.PartyMember member)
            {

            }

            @Override
            public void onGameStarting(JSONObject payload)
            {
                Gdx.app.postRunnable(() -> updateStatus(L.get("MENU_GAME_STARTING")));
            }

            @Override
            public void onGameStartFailed(int code, String message)
            {
                Gdx.app.postRunnable(() -> pushMenu(new RichAlertPopup(L.get("MENU_FREE_PLAY"), message)));
            }

            @Override
            public void onGameStarted(String roomId, String slot, String key, String host,
                                      ArrayList<Integer> ports, JSONObject roomSettings)
            {
                started = true;

                Gdx.app.postRunnable(() ->
                {
                    updateStatus(L.get("MENU_CONNECTING"));

                    int[] ports_ = new int[ports.size()];

                    for (int i = 0, t = ports.size(); i < t; i++)
                    {
                        ports_[i] = ports.get(i);
                    }

                    Matchmaking.Connect(key, host, ports_, roomSettings, partyId, () -> {
                        pushMenu(new AlertPopup("MENU_CONNECTION_ERROR"));
                    });
                });
            }

            @Override
            public void onPartyClosed(JSONObject payload)
            {
                if (started)
                    return;

                Gdx.app.postRunnable(() -> pop());
            }

            @Override
            public void onCustomMessage(String messageType, JSONObject payload)
            {

            }
        };
    }

    private void partyStarted()
    {
        updateStatus(L.get("MENU_WAITING_FOR_PARTNER"));
        invite.setDisabled(false);
    }

    private void joinParty(String partyId, GameService gameService, LoginService loginService)
    {
        JSONObject memberProfile = new JSONObject();

        party = gameService.openExistingPartySession(
            partyId,
            memberProfile,
            null,
            true,
            loginService.getCurrentAccessToken(),
            partyListener);
    }

    private void createParty(GameService gameService, LoginService loginService)
    {
        JSONObject partySettings = new JSONObject();
        JSONObject roomSettings = new JSONObject();
        JSONObject roomFilters = new JSONObject();

        JSONObject memberProfile = new JSONObject();

        party = gameService.openNewPartySession(
            "freeplay",
            partySettings,
            roomSettings,
            roomFilters,
            memberProfile,
            2,
            selectedRegion != null ? selectedRegion : BrainOutClient.ClientController.getMyRegion(),
            null,
            true,
            true,
            true,
            loginService.getCurrentAccessToken(),
            partyListener);
    }

    private void updateStatus(String status)
    {
        statusLabel.setText(status);
    }

    private void updateImageStatus()
    {
        statusImage.setDrawable(BrainOutClient.Skin,
                BrainOutClient.LocalizationMgr.getCurrentLanguage().equals("RU") ?
                        "label-partner-wait-ru" : "label-partner-wait-en");
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-freeplay");
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public boolean lockRender()
    {
        return true;
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

        {
            Table tutorial = new Table();

            {
                Table waiting = new Table();

                if (BrainOutClient.Skin.has("title-small", Label.LabelStyle.class))
                {
                    statusImage = new Image();
                    waiting.add(statusImage);
                }

                tutorial.add(waiting).pad(8).row();
            }

            // 1
            {
                Image icon = new Image(BrainOutClient.Skin, "container-pile");
                tutorial.add(icon).pad(8).padBottom(0).row();

                Label title = new Label(L.get("MENU_FP_TUTORIAL_1_TITLE"), BrainOutClient.Skin, "title-yellow");
                title.setAlignment(Align.center);
                tutorial.add(title).center().pad(4).row();

                Label description = new Label(L.get("MENU_FP_TUTORIAL_1_DESC"), BrainOutClient.Skin, "title-small");
                description.setWrap(true);
                description.setAlignment(Align.center);
                tutorial.add(description).pad(4).padBottom(8).expandX().fillX().row();
            }

            // 2
            {
                Image icon = new Image(BrainOutClient.Skin, "freeplay-weapon");
                tutorial.add(icon).pad(8).padBottom(0).row();

                Label title = new Label(L.get("MENU_FP_TUTORIAL_2_TITLE"), BrainOutClient.Skin, "title-yellow");
                title.setAlignment(Align.center);
                tutorial.add(title).center().pad(4).row();

                Label description = new Label(L.get("MENU_FP_TUTORIAL_2_DESC"), BrainOutClient.Skin, "title-small");
                description.setWrap(true);
                description.setAlignment(Align.center);
                tutorial.add(description).pad(4).padBottom(8).expandX().fillX().row();
            }

            /*
            // 3
            {
                Image icon = new Image(BrainOutClient.Skin, "freeplay-knife");
                tutorial.add(icon).pad(8).padBottom(0).row();

                Label title = new Label(L.get("MENU_FP_TUTORIAL_3_TITLE"), BrainOutClient.Skin, "title-yellow");
                title.setAlignment(Align.center);
                tutorial.add(title).center().pad(4).row();

                Label description = new Label(L.get("MENU_FP_TUTORIAL_3_DESC"), BrainOutClient.Skin, "title-small");
                description.setWrap(true);
                description.setAlignment(Align.center);
                tutorial.add(description).pad(4).padBottom(8).expandX().fillX().row();
            }
            */


            data.add(tutorial).width(600).row();
        }

        {
            invite = new TextButton(L.get("MENU_INVITE"), BrainOutClient.Skin, "button-green");

            invite.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    invite();
                }
            });

            data.add(invite).size(256, 64).pad(32).row();
        }

        return data;
    }

    public void invite()
    {
        pushMenu(new InviteMenu(L.get("MENU_INVITE"), friend ->
        {
            String context = "--free-play-join " + partyId;
            BrainOutClient.Env.getGameUser().inviteFriendCustom(friend, context);

            invite.setDisabled(true);
            invite.addAction(Actions.sequence(
                Actions.delay(2.0f),
                Actions.run(() ->
                {
                    invite.setDisabled(false);
                    invite.setText(L.get("MENU_INVITE_ONCE_AGAIN"));
                })
            ));

            updateImageStatus();
        }));
    }
}
