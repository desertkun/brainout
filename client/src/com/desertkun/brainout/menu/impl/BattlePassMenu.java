package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.content.ProfileBadge;
import com.desertkun.brainout.content.battlepass.BattlePassTask;
import com.desertkun.brainout.content.components.AnimatedIconComponent;
import com.desertkun.brainout.content.components.AnimationComponent;
import com.desertkun.brainout.data.battlepass.BattlePassTaskData;
import com.desertkun.brainout.data.interfaces.WithBadge;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.OnlineEventUpdatedEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.online.*;
import com.desertkun.brainout.utils.DurationUtils;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.services.StoreService;
import org.json.JSONObject;

public class BattlePassMenu extends Menu implements EventReceiver
{
    public static long DAILY_PHASE = 86400L;
    public static long WEEKLY_PHASE = DAILY_PHASE * 7;

    private ClientBattlePassEvent event;
    private BattlePassEvent.CurrentStage st;
    private Table contents;
    private Tab currentTab = Tab.dailyGoals;
    private Table progressPane;
    private int oldScore = -1;
    private int oldIndex = -1;
    private int counter = 0;
    private float rewardsScroll = 0;
    private Button rewardsButton;
    private Button dailyGoalsButton;
    private Button weeklyGoalsButton;
    private ScrollPane rewardsPane;
    private long phase = DAILY_PHASE;
    private Table leftButtons;

    public enum Tab
    {
        rewards,
        dailyGoals,
        weeklyGoals
    }

    public BattlePassMenu(ClientBattlePassEvent event)
    {
        this.event = event;
        this.st = event.getCurrentStage(event.score);
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion(isPassPurchased() ? "bg-battlepass" : "bg-clan");
    }

    @Override
    public void onInit()
    {
        super.onInit();
        MenuHelper.AddCloseButton(this, this::pop);

        leftButtons = MenuHelper.AddLeftButtonsContainers(this);

        BrainOut.EventMgr.subscribe(Event.ID.onlineEventsUpdated, this);
        BrainOut.EventMgr.subscribe(Event.ID.onlineEventUpdated, this);
        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
    }

    private void renderLeftButtons()
    {
        leftButtons.clearChildren();

        Button purchaseBattlePass = new Button(BrainOutClient.Skin, "button-notext");

        Label brain = new Label("BRAIN", BrainOutClient.Skin, "title-yellow");
        purchaseBattlePass.add(brain).pad(8);

        Image bp = new Image(BrainOutClient.Skin, "icon-bp-premium");
        bp.setScaling(Scaling.none);
        bp.setScale(2);
        bp.setOrigin(9, 9);
        purchaseBattlePass.add(bp).size(18).pad(4);

        Label pass = new Label("PASS", BrainOutClient.Skin, "title-small");
        purchaseBattlePass.add(pass).pad(8);

        purchaseBattlePass.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                pushMenu(new PurchaseBattlePassMenu(BattlePassMenu.this.event.getData().getBattlePass()));
            }
        });

        leftButtons.add(purchaseBattlePass).size(192, 64).row();

        if (!(!event.getData().getBattlePass().hasItem(BrainOutClient.ClientController.getUserProfile(), false) &&
                BrainOutClient.Env.storeEnabled()))
        {
            Table activated = new Table(BrainOutClient.Skin);
            activated.setBackground("form-gray");
            Label title = new Label(L.get("MENU_BP_ACTIVATED"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            activated.add(title).expand().fill();
            leftButtons.add(activated).size(192, 32).row();
        }
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        renderLeftButtons();
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.onlineEventsUpdated, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.onlineEventUpdated, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
    }

    @Override
    public boolean escape()
    {
        pop();
        return true;
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    private void renderBattlePassProgressPane(Table pane, int score)
    {
        this.st = event.getCurrentStage(score);

        pane.clearChildren();

        pane.add().expandX().uniformX();

        Table root = new Table();
        pane.add(root);

        {
            Table stats = new Table();

            Label title = new Label(String.valueOf(st.remainingScore) + " / " + st.stage.target,
                BrainOutClient.Skin, "title-small");
            stats.add(title).pad(4);

            Image bp = new Image(BrainOutClient.Skin, "icon-battle-pass-points");
            bp.setScaling(Scaling.none);
            stats.add(bp).pad(4);

            root.add(stats).row();
        }

        {
            Group progress = new Group();

            ProgressBar bar = new ProgressBar(0, st.stage.target, 1, false, BrainOutClient.Skin, "progress-battle-points");

            if (oldScore != -1 && oldScore != st.remainingScore)
            {
                if (oldIndex == st.completedIndex || st.completedIndex == event.getStages().peek().index)
                {
                    final int newScore = st.remainingScore;
                    counter = 0;

                    bar.setValue(oldScore);
                    bar.setAnimateDuration(1.0f);
                    bar.setAnimateInterpolation(Interpolation.exp5);
                    bar.setValue(newScore);
                }
                else
                {
                    final int newScore = st.remainingScore;
                    counter = 0;

                    bar.setValue(oldScore);
                    bar.setAnimateDuration(0.5f);
                    bar.setAnimateInterpolation(Interpolation.circleIn);
                    bar.setValue(bar.getMaxValue());

                    bar.addAction(Actions.sequence(
                        Actions.delay(0.55f),
                        Actions.run(() ->
                        {
                            bar.setAnimateDuration(0);
                            bar.setValue(0);
                            Gdx.app.postRunnable(() ->
                            {
                                bar.setAnimateDuration(0.5f);
                                bar.setAnimateInterpolation(Interpolation.circleOut);
                                bar.setValue(newScore);
                            });
                        })
                    ));
                }
            }
            else
            {
                bar.setValue(st.remainingScore);
            }

            this.oldScore = st.remainingScore;
            this.oldIndex = st.completedIndex;
            bar.setBounds(16, 8, 400, 16);
            progress.addActor(bar);

            for (int i = 0; i < 8; i++)
            {
                Image spline = new Image(BrainOutClient.Skin, "progress-background-spline");
                spline.setBounds(16 + 50 * i, 8, 4, 16);
                spline.setScaling(Scaling.none);
                spline.setTouchable(Touchable.disabled);
                progress.addActor(spline);
            }

            {
                Image a = new Image(BrainOutClient.Skin, "icon-battle-pass-stage");
                a.setScaling(Scaling.none);
                a.setFillParent(true);

                Label ai = new Label(String.valueOf(st.stage.index), BrainOutClient.Skin, "title-yellow");
                ai.setAlignment(Align.center);
                ai.setFillParent(true);

                Group g = new Group();
                g.setSize(32, 32);
                g.addActor(a);
                g.addActor(ai);
                g.setZIndex(10);
                g.setBounds(0, 0, 32, 32);

                progress.addActor(g);
            }

            {
                Image a = new Image(BrainOutClient.Skin, "icon-battle-pass-stage");
                a.setScaling(Scaling.none);
                a.setFillParent(true);

                Label ai = new Label(String.valueOf(st.stage.index + 1), BrainOutClient.Skin, "title-yellow");
                ai.setAlignment(Align.center);
                ai.setFillParent(true);

                Group g = new Group();
                g.setSize(32, 32);
                g.addActor(a);
                g.addActor(ai);
                g.setZIndex(10);
                g.setBounds(400, 0, 32, 32);

                progress.addActor(g);
            }

            root.add(progress).size(432, 32).padTop(-8).row();
        }

        Table skip = new Table();

        if (st.completedIndex < BattlePassMenu.this.event.stages.size - 1)
        {
            TextButton skipBtn = new TextButton(L.get("MENU_SKIP_STAGES"), BrainOutClient.Skin, "button-yellow-label");

            skipBtn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    pushMenu(new PurchaseBattlePointsMenu(BattlePassMenu.this.event.id,
                        BattlePassMenu.this.event.stages.size - st.completedIndex - 1));
                }
            });

            skip.add(skipBtn).size(216, 32).expand().right().bottom();
        }

        pane.add(skip).expand().uniformX().fill();
    }

    private String getTimeToEndOfProgress(long phase)
    {
        return DurationUtils.GetDurationString((int)(phase - BrainOutClient.ClientController.getServerTime() % phase));
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        contents = new Table();
        contents.align(Align.top);

        {
            Table panelSelector = new Table();

            ButtonGroup<Button> selectors = new ButtonGroup<>();
            selectors.setMaxCheckCount(1);
            selectors.setMinCheckCount(1);

            {
                rewardsButton = new Button(BrainOutClient.Skin, "button-notext-checkable");

                Label im = new Label(L.get("MENU_BP_REWARDS"), BrainOutClient.Skin, "title-small");
                im.setAlignment(Align.center);
                im.setWrap(true);
                im.setFillParent(true);
                rewardsButton.addActor(im);

                selectors.add(rewardsButton);
                panelSelector.add(rewardsButton).size(192, 64);
                rewardsButton.setChecked(true);

                rewardsButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        switchToRewards();
                    }
                });
            }

            {
                dailyGoalsButton = new Button(BrainOutClient.Skin, "button-notext-checkable");

                Label im = new Label(L.get("MENU_BP_DAILY_TASKS"), BrainOutClient.Skin, "title-small");
                im.setAlignment(Align.center);
                im.setWrap(true);
                im.setFillParent(true);
                dailyGoalsButton.addActor(im);

                selectors.add(dailyGoalsButton);
                panelSelector.add(dailyGoalsButton).size(192, 64);

                dailyGoalsButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        switchToGoals(DAILY_PHASE);
                    }
                });
            }

            {
                weeklyGoalsButton = new Button(BrainOutClient.Skin, "button-notext-checkable");

                Label im = new Label(L.get("MENU_BP_WEEKLY_TASKS"), BrainOutClient.Skin, "title-small");
                im.setAlignment(Align.center);
                im.setWrap(true);
                im.setFillParent(true);
                weeklyGoalsButton.addActor(im);

                selectors.add(weeklyGoalsButton);
                panelSelector.add(weeklyGoalsButton).size(192, 64);

                weeklyGoalsButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        switchToGoals(WEEKLY_PHASE);
                    }
                });
            }

            data.add(panelSelector).pad(20).padBottom(10).expandX().fillX().row();
        }

        generateButtonBadges();

        progressPane = new Table();
        renderBattlePassProgressPane(progressPane, (int)event.score);

        data.add(progressPane).expandX().fillX().pad(20).padTop(0).row();
        data.add(contents).expand().fill().row();

        BattlePassTaskData unclaimed = hasUnclaimedGoals();
        if (unclaimed != null)
        {
            if (unclaimed.getTasksDefinition().phase == DAILY_PHASE)
            {
                dailyGoalsButton.setChecked(true);
            }
            else
            {
                weeklyGoalsButton.setChecked(true);
            }

            switchToGoals(unclaimed.getTasksDefinition().phase);
        }
        else
        {
            switchToRewards();
        }

        return data;
    }

    private void generateRewardsBadge()
    {
        MenuBadge.apply(rewardsButton, new WithBadge()
        {
            @Override
            public boolean hasBadge(UserProfile profile, Involve involve)
            {
                return profile.hasBadge("battle-pass-rewards");
            }

            @Override
            public String getBadgeId()
            {
                return "battle-pass-rewards";
            }
        });
    }

    private void generateGoalsBadge()
    {
        MenuBadge.apply(dailyGoalsButton, new WithBadge()
        {
            @Override
            public boolean hasBadge(UserProfile profile, Involve involve)
            {
                return profile.hasBadge("battle-pass-tasks-daily");
            }

            @Override
            public String getBadgeId()
            {
                return "battle-pass-tasks-daily";
            }
        });

        MenuBadge.apply(weeklyGoalsButton, new WithBadge()
        {
            @Override
            public boolean hasBadge(UserProfile profile, Involve involve)
            {
                return profile.hasBadge("battle-pass-tasks-weekly");
            }

            @Override
            public String getBadgeId()
            {
                return "battle-pass-tasks-weekly";
            }
        });
    }

    private void generateButtonBadges()
    {
        generateRewardsBadge();
        generateGoalsBadge();
    }

    private BattlePassTaskData hasUnclaimedGoals()
    {
        for (BattlePassTaskData task : event.getData().getTasks())
        {
            if (task.isCompleted() && !task.isRewardRedeemed())
            {
                return task;
            }
        }

        return null;
    }

    private void switchToRewards()
    {
        MenuBadge.MarkBadge("battle-pass-rewards", BrainOutClient.ClientController.getUserProfile());

        currentTab = Tab.rewards;
        contents.clearChildren();

        {
            Label newMissions = new Label(L.get("MENU_BP_SEASON_ENDS_IN"),
                BrainOutClient.Skin, "title-yellow");
            newMissions.setAlignment(Align.center);

            contents.add(newMissions).pad(4).padTop(-8).expandX().fillX().center().row();
            Label time = new Label(event.getTimerToEnd(),
                BrainOutClient.Skin, "title-small");

            time.addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                Actions.delay(1),
                Actions.run(() -> time.setText(event.getTimerToEnd()))
            )));

            contents.add(time).pad(4).padBottom(16).expandX().center().row();
        }

        {
            Table header = new Table();

            {
                Table free = new Table(BrainOutClient.Skin);
                free.setBackground("form-gray");

                Label f = new Label(L.get("MENU_FREE"), BrainOutClient.Skin, "title-small");
                free.add(f);

                header.add(free).expand().uniformX().fill();
            }

            {
                Table brainPass = new Table(BrainOutClient.Skin);
                brainPass.setBackground("form-red");

                Label brain = new Label("BRAIN", BrainOutClient.Skin, "title-yellow");
                brainPass.add(brain).pad(8);

                Image bp = new Image(BrainOutClient.Skin, "icon-bp-premium");
                bp.setScaling(Scaling.none);
                bp.setScale(2);
                bp.setOrigin(9, 9);
                brainPass.add(bp).size(18).pad(4);

                Label pass = new Label("PASS", BrainOutClient.Skin, "title-small");
                brainPass.add(pass).pad(8);

                header.add(brainPass).expand().uniformX().fill();
            }

            contents.add(header).height(48).expandX().fillX().row();
        }

        Table rewards = new Table();
        rewards.align(Align.top);
        rewardsPane = new ScrollPane(rewards, BrainOutClient.Skin, "scroll-default")
        {
            @Override
            public void setScrollY(float pixels)
            {
                super.scrollY(pixels);
            }
        };
        rewardsPane.setScrollbarsOnTop(true);

        {
            Table entry = new Table(BrainOutClient.Skin);
            entry.setBackground("form-transparent-bottom-line");
            rewards.add(entry).height(16).fill().row();
        }

        for (BattlePassEvent.Stage stage : event.getStages())
        {
            Table entry = new Table(BrainOutClient.Skin);
            entry.setBackground("form-transparent-bottom-line");

            Table premiumRewards = new Table();
            Table freeRewards = new Table();

            Table stageNumber = new Table(BrainOutClient.Skin);
            stageNumber.setBackground(st.completedIndex >= stage.index ? "form-red" : "form-default");


            {
                if (st.completedIndex >= stage.index)
                {
                    Image a = new Image(BrainOutClient.Skin, "icon-battle-pass-stage-complete");
                    a.setScaling(Scaling.none);
                    a.setFillParent(true);
                    stageNumber.addActor(a);
                }

                Label ai = new Label(String.valueOf(stage.index + 1), BrainOutClient.Skin, "title-small");
                ai.setAlignment(Align.center);
                ai.setBounds(1, 42, 30, 16);

                stageNumber.addActor(ai);
            }

            renderRewards(stage, stage.premiumRewards, premiumRewards, true);
            renderRewards(stage, stage.rewards, freeRewards, false);

            entry.add(freeRewards).uniformX().expandX().center();
            entry.add(stageNumber).width(32).expandY().padTop(-6).padBottom(-6).fill();
            entry.add(premiumRewards).uniformX().expandX().center();

            rewards.add(entry).expandX().height(96).fill().row();
        }

        contents.add(rewardsPane).expand().fill();

        Gdx.app.postRunnable(() ->
        {
            rewardsPane.setScrollY(rewardsScroll);
            rewardsPane.updateVisualScroll();
        });

        setScrollFocus(rewardsPane);
    }

    private void renderRewards(BattlePassEvent.Stage stage, Array<Reward> rewards, Table table, boolean premium)
    {
        int idx = 0;

        for (Reward reward : rewards)
        {
            String style;
            String status;

            String tooltipTitle = ((ClientReward) reward).getClientAction().getLocalizedTitle();

            if (stage.isRewardRedeemed(premium, idx))
            {
                tooltipTitle += " (" + L.get("MENU_COMPLETE") + ")";
                style = "button-disabled";
                status = "content-owned";
            }
            else if (premium)
            {
                if (isPassPurchased())
                {
                    if (st.completedIndex >= stage.index)
                    {
                        style = "button-green-border";
                        status = null;
                    }
                    else
                    {
                        tooltipTitle += " (" + L.get("MENU_LOCKED") + ")";
                        style = "button-disabled";
                        status = null;
                    }
                }
                else
                {
                    tooltipTitle += " (" + L.get("MENU_BRAIN_PASS_REQUIRED") + ")";
                    style = "button-disabled";
                    status = "content-locked";
                }
            }
            else
            {
                if (st.completedIndex >= stage.index)
                {
                    style = "button-green-border";
                    status = null;
                }
                else
                {
                    tooltipTitle += " (" + L.get("MENU_LOCKED") + ")";
                    style = "button-disabled";
                    status = null;
                }
            }

            Button item = new Button(BrainOutClient.Skin, style);

            boolean rnd = true;

            if (((ClientReward) reward).getClientAction() instanceof Reward.UnlockAction)
            {
                Reward.UnlockAction aa = ((Reward.UnlockAction) ((ClientReward) reward).getClientAction());

                if (aa.id instanceof ProfileBadge)
                {
                    AnimationComponent ai = aa.id.getComponent(AnimationComponent.class);
                    if (ai != null)
                    {
                        item.setClip(true);
                        rnd = false;
                        ProfileBadgeAnimation aaa = new ProfileBadgeAnimation(ai.getAnimation());
                        aaa.setPosition(-120, -20);
                        item.addActor(aaa);
                    }
                }
            }

            if (rnd)
            {
                ((ClientReward) reward).getClientAction().render(item);
            }

            Tooltip.RegisterToolTip(item, tooltipTitle, BattlePassMenu.this);

            if (reward.getAction().getAmount() != 1)
            {
                Label amount = new Label("x" + reward.getAction().getAmount(),
                    BrainOutClient.Skin, "title-small");
                amount.setTouchable(Touchable.disabled);
                amount.setFillParent(true);
                amount.setAlignment(Align.right | Align.bottom);
                item.addActor(amount);
            }

            if (status != null)
            {
                Image img = new Image(BrainOutClient.Skin, status);
                img.setScaling(Scaling.none);
                img.setBounds(192 - 24, 64 - 24, 24, 24);
                img.setTouchable(Touchable.disabled);
                item.addActor(img);
            }

            final int iii = idx;

            item.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    if (premium && !isPassPurchased())
                    {
                        // TODO: Open buy battle pass menu
                        Menu.playSound(MenuSound.denied);
                        return;
                    }

                    if (stage.isRewardRedeemed(premium, iii))
                    {
                        return;
                    }

                    if (st.completedIndex >= stage.index)
                    {
                        Menu.playSound(MenuSound.select);

                        JSONObject args = new JSONObject();

                        args.put("event", BattlePassMenu.this.event.getData().getEventId());
                        args.put("stage", stage.index);
                        args.put("idx", iii);
                        args.put("premium", premium);

                        BrainOutClient.SocialController.sendRequest("redeem_bp_reward", args,
                            new SocialController.RequestCallback()
                        {
                            @Override
                            public void success(JSONObject response)
                            {
                                //
                            }

                            @Override
                            public void error(String reason)
                            {
                                if (Log.ERROR) Log.error(reason);
                                Menu.playSound(MenuSound.denied);
                            }
                        });
                    }
                    else
                    {
                        Menu.playSound(MenuSound.denied);
                    }
                }
            });

            table.add(item).size(192, 64).padRight(8).padLeft(8);

            idx++;
        }

    }

    private boolean isPassPurchased()
    {
        return BrainOutClient.ClientController.getUserProfile().hasItem(event.getData().getBattlePass(), false);
    }

    private void switchToGoals(long phase)
    {
        MenuBadge.MarkBadge(phase == DAILY_PHASE ? "battle-pass-tasks-daily" : "battle-pass-tasks-weekly",
            BrainOutClient.ClientController.getUserProfile());

        currentTab = phase == DAILY_PHASE ? Tab.dailyGoals : Tab.weeklyGoals;
        contents.clearChildren();

        {
            Label newMissions = new Label(L.get("MENU_NEW_MISSIONS_WILL_BE_DELIVERED_IN"),
                    BrainOutClient.Skin, "title-yellow");
            newMissions.setAlignment(Align.center);

            contents.add(newMissions).pad(4).padTop(-8).expandX().fillX().center().row();
            Label time = new Label(getTimeToEndOfProgress(phase),
                    BrainOutClient.Skin, "title-small");

            time.addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                    Actions.delay(1),
                    Actions.run(() -> time.setText(getTimeToEndOfProgress(phase)))
            )));

            contents.add(time).pad(4).padBottom(8).expandX().center().row();
        }

        for (BattlePassTaskData task : event.getData().getTasks())
        {
            if (task.getTasksDefinition().phase != phase)
            {
                continue;
            }

            Table taskRoot = new Table();

            Table leftSide = new Table();

            Table titleRoot = new Table(BrainOutClient.Skin);
            titleRoot.setBackground("form-default");

            String taskTitle;
            String titleStyle;

            if (task.isPremium() && !isPassPurchased())
            {
                taskTitle = L.get("MENU_BRAIN_PASS_REQUIRED");
                titleStyle = "title-yellow";
            }
            else
            {
                taskTitle = task.getTaskTitle();
                titleStyle = "title-small";
            }

            Label title = new Label(taskTitle, BrainOutClient.Skin, titleStyle);
            title.setWrap(true);
            title.setAlignment(Align.center);
            titleRoot.add(title).expand().fill();
            leftSide.add(titleRoot).height(64).expand().fill().row();

            Table progressRoot = new Table(BrainOutClient.Skin);
            progressRoot.setBackground("form-default");

            int progress = task.getProgress();
            ProgressBar progressBar = new ProgressBar(0, task.getTask().getTarget(), 1, false,
                BrainOutClient.Skin, "progress-health");
            progressBar.setValue(progress);

            progressRoot.add(progressBar).expand().padBottom(2).fill().row();

            Label progressTitle = new Label(String.valueOf(progress) + " / " + task.getTask().getTarget(),
                BrainOutClient.Skin, "title-small");
            progressTitle.setAlignment(Align.center | Align.top);
            progressTitle.setBounds(0, 6, 608, 24);
            progressRoot.addActor(progressTitle);
            leftSide.add(progressRoot).height(32).expandX().fillX().row();

            taskRoot.add(leftSide).width(608).pad(8).padRight(16).padLeft(16);

            Table reward = new Table();

            if (task.isRewardRedeemed())
            {
                Image rewardIcon = new Image(BrainOutClient.Skin, "icon-ready-big");
                rewardIcon.setScaling(Scaling.none);
                reward.add(rewardIcon).size(64).expandX().center().row();
            }
            else
            {
                Table rewardPanel = new Table();
                rewardPanel.align(Align.center);

                Image rewardIcon = new Image(BrainOutClient.Skin, "icon-battle-pass-points-big");
                String rewardTitle = String.valueOf(task.getTask().getReward());

                rewardIcon.setScaling(Scaling.none);
                rewardPanel.add(rewardIcon).expandX().center().row();

                Label rewardAmount = new Label(rewardTitle, BrainOutClient.Skin, "title-small");
                rewardPanel.add(rewardAmount).expandX().center().row();

                reward.add(rewardPanel).height(64).expandX().fill().row();
            }

            if (task.isPremium() && !isPassPurchased())
            {
                TextButton locked = new TextButton(L.get("MENU_LOCKED"), BrainOutClient.Skin, "button-gray-bg");
                locked.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        pushMenu(new PurchaseBattlePassMenu(BattlePassMenu.this.event.getData().getBattlePass()));
                    }
                });
                reward.add(locked).expandX().fillX().center().row();
            }
            else if (progress >= task.getTask().getTarget())
            {
                if (task.isRewardRedeemed())
                {
                    Label rewardOngoing = new Label(L.get("MENU_REDEEMED"), BrainOutClient.Skin, "title-green");
                    reward.add(rewardOngoing).expandX().center().row();
                }
                else
                {
                    TextButton redeem = new TextButton(L.get("MENU_CLAIM"), BrainOutClient.Skin, "button-green");
                    redeem.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            if (redeem.isDisabled())
                            {
                                return;
                            }

                            Menu.playSound(MenuSound.select);

                            redeem.setText("...");
                            redeem.setDisabled(true);

                            JSONObject args = new JSONObject();
                            args.put("event", BattlePassMenu.this.event.getData().getEventId());
                            args.put("task", task.getTaskKey());

                            BrainOutClient.SocialController.sendRequest("claim_battle_task_reward", args,
                                new SocialController.RequestCallback()
                            {
                                @Override
                                public void success(JSONObject response)
                                {
                                    Menu.playSound(MenuSound.contentOwnedEx);

                                    BattlePassMenu.this.addAction(Actions.repeat(20, Actions.sequence(
                                        Actions.run(() -> Menu.playSound(MenuSound.character)),
                                        Actions.delay(0.05f)
                                    )));

                                    task.setRewardRedeemed();
                                    refresh(response.optInt("newScore", 0));
                                    redeem.setDisabled(false);
                                }

                                @Override
                                public void error(String reason)
                                {
                                    if (Log.ERROR) Log.error(reason);
                                    Menu.playSound(MenuSound.denied);
                                    redeem.setDisabled(false);
                                }
                            });
                        }
                    });

                    reward.add(redeem).expandX().fillX().center().row();
                }
            }
            else
            {
                Label rewardOngoing = new Label(L.get("MENU_IN_PROGRESS"), BrainOutClient.Skin, "title-small");
                reward.add(rewardOngoing).expandX().center().row();
            }

            taskRoot.add(reward).width(192).pad(8).padLeft(16).padRight(16).row();

            contents.add(taskRoot).row();
        }

        Label desc = new Label(L.get("MENU_BATTLEPASS_TASKS_INTRO"), BrainOutClient.Skin, "title-small");
        desc.setAlignment(Align.center);
        desc.setWrap(true);
        contents.add(desc).width(600).expandY().bottom().pad(16).row();
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event e)
    {
        switch (e.getID())
        {
            case onlineEventsUpdated:
            {
                Array<ClientEvent> events = BrainOutClient.ClientController.getOnlineEvents();

                for (ClientEvent event : events)
                {
                    if (event.getEvent().id != this.event.id)
                    {
                        continue;
                    }

                    if (event instanceof ClientBattlePassEvent)
                    {
                        this.event = ((ClientBattlePassEvent) event);
                        refresh((int)this.event.score);
                        break;
                    }
                }

                break;
            }
            case onlineEventUpdated:
            {
                OnlineEventUpdatedEvent ev = ((OnlineEventUpdatedEvent) e);

                if (ev.event.getEvent().id == this.event.id)
                {
                    if (ev.event instanceof ClientBattlePassEvent)
                    {
                        this.event = (ClientBattlePassEvent)ev.event;
                        refresh((int)this.event.score);
                        break;
                    }
                }

                break;
            }
            case simple:
            {
                SimpleEvent se = ((SimpleEvent) e);

                if (se.getAction() == SimpleEvent.Action.userProfileUpdated)
                {
                    profileUpdated();
                }

                break;
            }
        }

        return false;
    }

    private void profileUpdated()
    {
        if (Log.INFO) Log.info("Profile updated!");
        refresh(-1);

        if (isPassPurchased())
        {
            bg.setDrawable(BrainOutClient.Skin, "bg-battlepass");
        }
    }

    private void refresh(int newScore)
    {
        if (Log.INFO) Log.info("Refreshing to score: " + newScore);
        rewardsScroll = rewardsPane != null ? rewardsPane.getScrollY() : 0;

        if (newScore >= 0)
        {
            renderBattlePassProgressPane(progressPane, newScore);
        }

        switch (currentTab)
        {
            case rewards:
            {
                switchToRewards();
                generateGoalsBadge();
                break;
            }
            case dailyGoals:
            {
                switchToGoals(DAILY_PHASE);
                generateRewardsBadge();
                break;
            }
            case weeklyGoals:
            {
                switchToGoals(WEEKLY_PHASE);
                generateRewardsBadge();
                break;
            }
        }
    }
}
