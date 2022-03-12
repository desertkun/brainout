package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.common.msg.client.ClaimOnlineEventRewardMsg;
import com.desertkun.brainout.content.components.InstrumentAnimationComponent;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientShootingRangeActivatorComponentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.events.ClaimOnlineEventResultEvent;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.online.*;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONObject;

import static com.desertkun.brainout.menu.ui.Tooltip.RegisterToolTip;

public class OnlineEventMenu extends Menu
{
    private final ClientEvent event;
    private final boolean valuables;
    private float timer;
    private Label ttl;
    private MenuMode menuMode;
    private Table eventInfoUi;
    private TextButton switchButton;
    private Table header;

    public enum MenuMode
    {
        event,
        tournament
    }

    public interface ClaimResult
    {
        void done(boolean result);
    }

    private abstract class OnlineEventClaimMenu extends WaitLoadingMenu implements EventReceiver
    {
        private final int eventId;
        private final int rewardIndex;

        public OnlineEventClaimMenu(int eventId, int rewardIndex)
        {
            super("", false);

            this.eventId = eventId;
            this.rewardIndex = rewardIndex;
        }

        @Override
        public boolean popIfFocusOut()
        {
            return true;
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
            pop();

            return true;
        }

        public abstract void done(boolean success);

        @Override
        public boolean onEvent(com.desertkun.brainout.events.Event event)
        {
            switch (event.getID())
            {
                case onlineEventClaimResult:
                {
                    ClaimOnlineEventResultEvent e = ((ClaimOnlineEventResultEvent) event);

                    if (e.eventId == eventId && e.rewardIndex == rewardIndex)
                    {
                        claimResult(e);
                    }

                    break;
                }
            }

            return false;
        }

        private void claimResult(ClaimOnlineEventResultEvent result)
        {
            pop();

            done(result.success);
        }

        @Override
        public void onInit()
        {
            super.onInit();

            BrainOutClient.EventMgr.subscribe(com.desertkun.brainout.events.Event.ID.onlineEventClaimResult, this);
        }

        @Override
        public void onRelease()
        {
            super.onRelease();

            BrainOutClient.EventMgr.unsubscribe(com.desertkun.brainout.events.Event.ID.onlineEventClaimResult, this);
        }
    }

    public OnlineEventMenu(ClientEvent event, boolean valuables)
    {
        this.valuables = valuables;
        this.event = event;
        timer = 1.0f;

        this.menuMode = event.getEvent().group ? MenuMode.tournament : MenuMode.event;
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public boolean escape()
    {
        pop();
        Menu.playSound(MenuSound.back);
        return true;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion(this.event.getEvent().group ? "bg-clan" : "bg-ingame");
    }

    private void updateEvent()
    {
        ttl.setText(getTimeToEnd());

        if (!event.getEvent().isValid())
        {
            pop();
        }
    }

    public void renderEventInfo()
    {
        eventInfoUi.clear();

        switch (menuMode)
        {
            case event:
            {
                renderEventUI(eventInfoUi);

                break;
            }
            case tournament:
            {
                renderLeaderboard(eventInfoUi);
            }
        }
    }

    private void renderLeaderboard(Table data)
    {
        LoginService loginService = LoginService.Get();

        if (loginService == null)
            return;

        LoginService.AccessToken token = loginService.getCurrentAccessToken();

        if (event.getEvent().tournamentRewards.size > 0)
        {
            {

                Label reward = new Label(L.get("MENU_EVENT_TOP_REWARD"),
                        BrainOutClient.Skin, "title-small");
                reward.setAlignment(Align.center);

                data.add(reward).expandX().fillX().padBottom(8).row();
            }

            Table tournamentRewards = new Table();

            for (Event.EventTournamentReward reward : event.getEvent().tournamentRewards)
            {
                ClientReward clientReward = ((ClientReward) reward.reward);
                ClientReward.ClientAction clientAction = ((ClientReward.ClientAction) clientReward.getAction());

                Table row = new Table();

                String loc;

                if (reward.rankFrom == reward.rankTo)
                {
                    loc = String.valueOf(reward.rankFrom) + " " + L.get("MENU_REWARD_PLACE");
                }
                else
                {
                    loc = String.valueOf(reward.rankFrom) + " - " +
                          String.valueOf(reward.rankTo) + " " + L.get("MENU_REWARD_PLACES");
                }

                {
                    Label title = new Label(loc, BrainOutClient.Skin, "title-yellow");
                    title.setAlignment(Align.center);
                    BorderActor borderActor = new BorderActor(title, "form-gray");
                    row.add(borderActor).width(192).row();
                }

                {
                    Table contents = new Table();
                    contents.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

                    clientAction.render(contents);

                    int amount = reward.reward.getAction().getAmount();

                    if (amount > 1)
                    {
                        String amountTitle = "x" + amount;
                        Label amountLabel = new Label(amountTitle, BrainOutClient.Skin, "title-small");

                        amountLabel.setBounds(4, 2, 184, 60);
                        amountLabel.setAlignment(Align.right | Align.bottom);
                        amountLabel.setTouchable(Touchable.disabled);

                        contents.addActor(amountLabel);
                    }

                    row.add(contents).width(192).minHeight(64).expandY().fillY().row();
                }

                tournamentRewards.add(row).uniformY().fillY();
            }

            data.add(tournamentRewards).expandX().fillX().padBottom(24).center().row();
        }

        if (event.getEvent().group && !event.getEvent().joined)
        {
            Image warningIcon = new Image(BrainOutClient.Skin, "icon-warning");
            data.add(warningIcon).pad(16).row();

            Label warningText = new Label(L.get("MENU_EVENT_NOT_JOINED_WARNING"), BrainOutClient.Skin,
                "title-yellow");
            warningText.setAlignment(Align.center);
            warningText.setWrap(true);

            data.add(warningText).width(450).row();

            Clan myClan = BrainOutClient.SocialController.getMyClan();

            if (myClan != null)
            {
                Clan.ClanMember me = myClan.getMembers().get(BrainOutClient.ClientController.getMyAccount());

                if (me != null && me.hasPermission(Clan.Permissions.PARTICIPATE_EVENT))
                {
                    PurchaseButton purchaseButton = new PurchaseButton(
                        L.get("MENU_PARTICIPATE"), BrainOutClient.Skin, "button-green",
                        getParticipatePrice(), Constants.Clans.CURRENCY_CLAN_PARTICIPATE
                    );

                    data.add(purchaseButton).height(64).pad(32).row();

                    if (myClan.getStats().get(Constants.User.NUCLEAR_MATERIAL, 0.0f) < getParticipatePrice())
                    {
                        purchaseButton.setDisabled(true);
                    }

                    purchaseButton.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            if (myClan.getStats().get(Constants.User.NUCLEAR_MATERIAL, 0.0f) < getParticipatePrice())
                            {
                                Menu.playSound(MenuSound.denied);
                                return;
                            }

                            Menu.playSound(MenuSound.select);

                            participateGroupEvent();
                        }
                    });
                }
                //
            }
        }
        else
        {
            String myClanId;

            if (BrainOutClient.SocialController.getMyClan() != null)
            {
                myClanId = BrainOutClient.SocialController.getMyClan().getId();
            }
            else
            {
                myClanId = null;
            }

            LeaderboardList leaderboardList = new LeaderboardList(
                event.getEvent().leaderboardName, event.getEvent().leaderboardOrder, event.getEvent().tournamentRewards.size,
                event.getEvent().group ? myClanId : BrainOutClient.ClientController.getMyAccount(), event.getEvent().group)
            {
                @Override
                public float getScore(String account, float score)
                {
                    if (event.getEvent().group)
                    {
                        if (myClanId != null && myClanId.equals(account))
                        {
                            return event.getEvent().score;
                        }
                    }
                    else
                    {
                        if (BrainOutClient.ClientController.getMyAccount().equals(account))
                        {
                            return event.getEvent().score;
                        }
                    }

                    return super.getScore(account, score);
                }

                @Override
                protected void clickedOnItem(String accountId, String credential)
                {
                    GameState gs = getGameState();

                    pop();

                    if (event.getEvent().group)
                    {
                        gs.pushMenu(new ClanMenu(accountId));
                    }
                    else
                    {
                        gs.pushMenu(new RemoteAccountMenu(accountId, credential));
                    }
                }
            };

            setScrollFocus(leaderboardList);

            data.add(leaderboardList).expandY().fillY().width(580).row();
        }
    }

    private void participateGroupEvent()
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();

        args.put("event_id", event.getEvent().id);

        BrainOutClient.SocialController.sendRequest("join_clan_event", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();

                event.getEvent().joined = true;
                renderEventInfo();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();

                pushMenu(new AlertPopup(L.get("MENU_ONLINE_ERROR", L.get(reason))));
            }
        });
    }

    private int getParticipatePrice()
    {
        return BrainOutClient.ClientController.getPrice("participateClanEvent", 40);
    }

    private void renderEventUI(Table data)
    {
        Label title = new Label(event.getTitle(), BrainOutClient.Skin, valuables ? "title-small": "title-level");
        title.setAlignment(Align.center);

        data.add(new BorderActor(title, valuables ? "form-yellow" : "form-red")).size(420, 32).expandX().fillX().row();

        Table info = new Table();
        info.align(Align.center);

        String iconId = event.getIconId();

        Weapon asWeapon = BrainOutClient.ContentMgr.get(iconId, Weapon.class);

        if (asWeapon != null)
        {
            InstrumentInfo instrumentInfo = new InstrumentInfo();

            instrumentInfo.instrument = asWeapon;
            instrumentInfo.skin = asWeapon.getDefaultSkin();

            float scale;

            InstrumentAnimationComponent iac =
                    asWeapon.getComponentFrom(InstrumentAnimationComponent.class);

            if (iac != null)
            {
                scale = iac.getIconScale();
            }
            else
            {
                scale = 1.0f;
            }

            InstrumentIcon instrumentIcon = new InstrumentIcon(instrumentInfo, scale, false);
            instrumentIcon.setTouchable(Touchable.disabled);
            instrumentIcon.setBounds(0, 0, 192, 64);
            instrumentIcon.init();

            info.add(instrumentIcon).size(192, 64).pad(16, 16, 0, 16).row();
        }
        else
        {
            TextureRegion icon = event.getIcon();
            if (icon != null)
            {
                Image image = new Image(icon);
                image.setScaling(Scaling.none);

                info.add(image).pad(16, 16, 0, 16).row();
            }
        }


        Label description = new Label(event.getDescription(), BrainOutClient.Skin, "title-small");
        description.setWrap(true);

        description.setAlignment(Align.center);

        info.add(description).pad(16).width(400).expandX().fillX().row();

        data.add(new BorderActor(info, valuables ? "form-border-yellow" : "form-border-red")).width(420).expandX().fillX().row();

        switch (event.getEvent().behaviour)
        {
            case increment:
            {
                float targetScore = event.getEvent().getTargetScore();

                LabeledProgress progress = new LabeledProgress(BrainOutClient.Skin,
                    valuables ? "progress-spawn": "progress-event", 0, (int) targetScore, (int) event.getEvent().score, (int) targetScore);

                data.add(new BorderActor(progress, 400, valuables ? "form-border-yellow" : "form-border-red")).width(420).row();

                break;
            }
            case maximum:
            {
                Table bestResult = new Table(BrainOutClient.Skin);
                bestResult.setBackground("form-gray");

                {
                    Label bestResultTitle = new Label(L.get("MENU_BEST_RESULT"), BrainOutClient.Skin, "title-yellow");
                    bestResult.add(bestResultTitle).expandX().left();
                }

                {
                    Label value = new Label(String.valueOf((int)event.getEvent().score), BrainOutClient.Skin, "title-small");
                    bestResult.add(value).row();
                }

                data.add(bestResult).width(420).row();

                break;
            }
        }

        Table rewards = new Table();

        int rewardId = 0;

        for (Event.EventReward reward : event.getEvent().rewards)
        {
            final int index = rewardId++;

            Table rewardData = new Table();

            if (reward.isComplete())
            {
                Label complete = new Label(
                        L.get("MENU_COMPLETE"), BrainOutClient.Skin, valuables ? "title-small" : "title-yellow");
                complete.setAlignment(Align.center);

                rewardData.add(new BorderActor(complete, valuables ? "form-yellow" : "form-red")).expandX().fillX().row();
            }
            else
            {
                String task = String.valueOf((int)event.getEvent().score) + " / " + (int)reward.targetScore;

                Label taskTitle = new Label(task,
                        BrainOutClient.Skin, "title-small");

                taskTitle.setAlignment(Align.center);

                rewardData.add(new BorderActor(taskTitle, "form-gray")).expandX().fillX().row();
            }

            Table rewardContent = new Table();
            rewardContent.setBackground(new TextureRegionDrawable(BrainOutClient.getRegion("reward-bg")));

            renderReward(reward, rewardContent);

            if (reward.isComplete())
            {
                rewardData.add(new BorderActor(rewardContent, valuables ? "form-border-yellow" : "form-border-red")).height(96).expandX().fill().row();

                if (!reward.isClaimed())
                {
                    TextButton claim = new TextButton(
                        L.get("MENU_CLAIM"), BrainOutClient.Skin, "button-default"
                    );

                    claim.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            claimReward(index, success ->
                            {
                                if (success)
                                {
                                    claim.remove();
                                }
                            });
                        }
                    });

                    rewardData.add(claim).expandX().expandY().fillX().padBottom(-64).row();
                }
            }
            else
            {
                rewardData.add(new BorderActor(rewardContent, "form-default")).expandX().fill().row();
            }

            rewards.add(rewardData).top().uniformY().fillY().expandX().width(220);

        }

        data.add(rewards).pad(32).expandX().fillX().row();

        if (isShootingRange(event))
        {
            TextButton go = new TextButton(L.get("MENU_VOTE"), BrainOutClient.Skin, "button-green");

            go.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event_, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    startShooting(event);
                }
            });

            data.add(go).size(192, 64).padTop(32).row();
        }
        else if (isZombieEvent(event))
        {
            TextButton go = new TextButton(L.get("MENU_PLAY_THE_EVENT"), BrainOutClient.Skin, "button-green");

            go.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event_, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    startZombieEvent(event);
                }
            });

            data.add(go).size(256, 64).padTop(32).row();
        }
    }

    private void startZombieEvent(ClientEvent event)
    {
        RoomSettings roomSettings = new RoomSettings();
        roomSettings.init(BrainOutClient.ClientController.getUserProfile(), false);
        roomSettings.setRegion(BrainOutClient.ClientController.getMyRegion());

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        Matchmaking.FindGame("zombie", roomSettings, new Matchmaking.FindGameResult()
        {
            @Override
            public void success(String roomId)
            {
                waitLoadingMenu.pop();
            }

            @Override
            public void failed(Request.Result status, Request request)
            {
                waitLoadingMenu.pop();
            }

            @Override
            public void connectionFailed()
            {
                waitLoadingMenu.pop();
            }
        }, false);
    }

    private void startShooting(ClientEvent event)
    {
        if (!event.getEvent().taskAction.equals(Constants.Other.SHOOTING_RANGE_ACTION))
            return;

        ActiveData activeData = null;

        for (Map map : Map.All())
        {
            activeData = map.getActiveForTag(Constants.ActiveTags.SHOOTING_RANGE,
                activeData1 ->
            {
                ClientShootingRangeActivatorComponentData activator =
                        activeData1.getComponent(ClientShootingRangeActivatorComponentData.class);

                if (activator == null)
                    return false;

                return activator.getShootingRange().hasWeapon(event.getEvent().taskData);
            });

            if (activeData != null)
                break;
        }

        if (activeData == null)
            return;

        ClientShootingRangeActivatorComponentData activator =
            activeData.getComponent(ClientShootingRangeActivatorComponentData.class);

        if (activator == null)
            return;

        GameState gs = getGameState();

        pop();

        if (gs.topMenu() instanceof LobbyMenu)
        {
            LobbyMenu lobbyMenu = ((LobbyMenu) gs.topMenu());
            lobbyMenu.goToBase();
        }

        activator.startShooting();
    }

    private void claimReward(int index, ClaimResult result)
    {
        if (index >= event.getEvent().getRewardsCount())
            return;

        Event.EventReward reward = event.getEvent().rewards.get(index);

        if (reward == null)
            return;

        BrainOutClient.ClientController.sendTCP(
            new ClaimOnlineEventRewardMsg(event.getEvent().id, index));

        pushMenu(new OnlineEventClaimMenu(event.getEvent().id, index)
        {
            @Override
            public void done(boolean success)
            {
                result.done(success);

                if (success)
                {
                    reward.claimed = true;

                    Menu.playSound(MenuSound.equip);
                }
            }
        });
    }

    private void renderReward(Event.EventReward reward, Table data)
    {
        Reward.Action action = reward.reward.getAction();

        if (action instanceof ClientReward.ClientAction)
        {
            ClientReward.ClientAction clientAction = ((ClientReward.ClientAction) action);

            clientAction.render(data);

            int amount = reward.reward.getAction().getAmount();

            if (amount > 1)
            {
                String amountTitle = "x" + amount;
                Label amountLabel = new Label(amountTitle, BrainOutClient.Skin, "title-small");

                amountLabel.setBounds(4, 2, 184, 60);
                amountLabel.setAlignment(Align.right | Align.bottom);
                amountLabel.setTouchable(Touchable.disabled);

                data.addActor(amountLabel);
            }

        }
    }

    @Override
    public void onInit()
    {
        super.onInit();

        Table buttons = MenuHelper.AddButtonsContainers(this);

        TextButton close = new TextButton(
                L.get("MENU_CLOSE"),
                BrainOutClient.Skin, "button-default"
        );

        close.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.back);

                pop();
            }
        });

        buttons.add(close).size(192, 64).row();

        if (event.getEvent().hasTournament && !event.getEvent().group)
        {
            switchButton = new TextButton(
                    L.get("MENU_TOP"),
                    BrainOutClient.Skin, "button-yellow"
            );

            switchButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    switchMenu();
                }
            });

            buttons.add(switchButton).size(192, 64).row();
        }
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();
        data.align(Align.center);

        header = new Table();

        renderHeader();

        data.add(header).pad(8).expandX().fillX().row();

        eventInfoUi = new Table();

        renderEventInfo();

        data.add(eventInfoUi).pad(8).expand().fill().row();

        if (event.getEvent().group)
        {
            Label notice = new Label(L.get("MENU_EVENT_WEEKLY_INFO"), BrainOutClient.Skin, "title-small");
            notice.setAlignment(Align.center);
            data.add(notice).pad(8).expand().fillX().bottom().row();
        }
        else
        {
            if (isShootingRange(event))
            {
                Table whoa = new Table();

                Label notice = new Label(L.get("MENU_TRAINING_RANGE_EVENT"), BrainOutClient.Skin, "title-small");
                whoa.add(notice).row();

                data.add(whoa).pad(8).expandY().bottom().row();
            }
            else
            {
                Label notice = new Label(L.get("MENU_EVENT_INFO"), BrainOutClient.Skin, "title-small");
                notice.setAlignment(Align.center);
                data.add(notice).pad(8).expand().fillX().bottom().row();
            }
        }


        return data;
    }

    private boolean isShootingRange(ClientEvent event)
    {
        return event.getEvent().taskAction.equals(Constants.Other.SHOOTING_RANGE_ACTION);
    }

    private boolean isZombieEvent(ClientEvent event)
    {
        return event.getEvent().category.equals("zombie");
    }

    private void renderHeader()
    {
        header.clear();

        switch(menuMode)
        {
            case event:
            {
                Image image = new Image(BrainOutClient.getRegion(
                    isShootingRange(event) ? "icon-range" : "icon-event-rating"));
                image.setScaling(Scaling.none);

                header.add(image).height(64).pad(0, 8, 8, 8).row();

                break;
            }
            case tournament:
            {
                TextureRegion icon = event.getIcon();

                if (icon != null)
                {
                    Image image = new Image(icon);
                    image.setScaling(Scaling.none);

                    header.add(image).height(64).pad(0, 8, 8, 8).row();
                }
                else
                {
                    Image image = new Image(BrainOutClient.getRegion("icon-event-rating"));
                    image.setScaling(Scaling.none);

                    header.add(image).height(64).pad(0, 8, 8, 8).row();
                }

                break;
            }
        }

        Label timeToEndTitle = new Label(
            menuMode == MenuMode.event ? L.get("MENU_EVENT_TTL") : event.getDescription(),
            BrainOutClient.Skin,
            "title-small");
        timeToEndTitle.setAlignment(Align.center);
        timeToEndTitle.setWrap(true);

        header.add(timeToEndTitle).width(400).fillX().padBottom(16).row();

        this.ttl = new Label(getTimeToEnd(), BrainOutClient.Skin, "title-yellow");
        ttl.setAlignment(Align.center);

        header.add(ttl).expandX().fillX().padBottom(8).row();
    }

    private void switchMenu()
    {
        if (menuMode == MenuMode.event)
        {
            menuMode = MenuMode.tournament;
        }
        else
        {
            menuMode = MenuMode.event;
        }

        switch (menuMode)
        {
            case event:
                switchButton.setText(L.get("MENU_TOP"));
                break;
            case tournament:
                switchButton.setText(L.get("MENU_EVENTS"));
                break;
        }

        renderHeader();
        renderEventInfo();
    }

    public String getTimeToEnd()
    {
        return event.getTimerToEnd();
    }

    @Override
    public boolean lockRender()
    {
        return true;
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        timer -= delta;

        if (timer < 0)
        {
            updateEvent();
            timer = 1.0f;
        }
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
