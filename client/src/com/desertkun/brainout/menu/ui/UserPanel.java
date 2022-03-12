package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.data.interfaces.WithBadge;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.BattlePassMenu;
import com.desertkun.brainout.menu.impl.ContainersMenu;
import com.desertkun.brainout.mode.ClientFreeRealization;
import com.desertkun.brainout.online.*;
import org.anthillplatform.runtime.services.EnvironmentService;

public class UserPanel extends Table
{
    protected UserProfile user;
    protected boolean enableCaseButton;
    private BadgeButton caseButton;
    private Runnable openExchange;

    public UserPanel()
    {
    }

    public UserPanel(boolean caseButton)
    {
        this.user = BrainOutClient.ClientController.getUserProfile();
        this.enableCaseButton = caseButton;

        fill();
    }

    protected void fill()
    {
        Levels.Level level = getLevel();

        if (level != null)
        {
            Image levelIcon = new Image(BrainOutClient.getRegion(level.icon));
            levelIcon.setScaling(Scaling.none);
            add(new BorderActor(levelIcon)).top().size(96, 64);

            Table userContainer = new Table();
            userContainer.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

            if (!user.getAvatar().isEmpty())
            {
                Table avatarInfo = new Table();

                fetchAvatar(avatarInfo);

                userContainer.add(avatarInfo).padLeft(2);
            }

            Table userInfo = new Table();
            userContainer.add(userInfo);

            if (!user.getName().isEmpty())
            {
                Label userName = new Label(user.getName(), BrainOutClient.Skin, "title-small");
                userName.setEllipsis(true);
                userName.setWrap(false);
                userName.setAlignment(Align.left);
                userInfo.add(userName).pad(-6, 8, 0, 8).width(362).row();
            }

            Table levelInfo = new Table();

            Label levelName = new Label(level.name.get(), BrainOutClient.Skin, "title-level");
            levelName.setAlignment(Align.left);
            Label levelNumber = new Label(getLevelString(level), BrainOutClient.Skin, getLevelStyle());

            levelInfo.add(levelName).expandX().fillX();
            addLevelNumberIcon(levelInfo);
            levelInfo.add(levelNumber).row();

            userInfo.add(levelInfo).top().pad(-3, 8, -1, 8).expandX().fillX().row();

            if (level.hasNextLevel())
            {
                int currentScore = getScore();
                int prevScore = level.getPrevLevel() != null ? level.getPrevLevel().score : 0;
                int nextScore = level.score;

                Group progress = new Group();

                ProgressBar scoreBar = new ProgressBar(prevScore, nextScore,
                        1, false, BrainOutClient.Skin,
                        getProgressStyle());

                scoreBar.setBounds(
                        0,
                        -4,
                        362,
                        ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT
                );

                scoreBar.setValue(currentScore);
                progress.addActor(scoreBar);

                Label scoreValue = new Label(String.valueOf(currentScore) + " / " + nextScore,
                        BrainOutClient.Skin, "title-small");

                scoreValue.setAlignment(Align.center);
                scoreValue.setFillParent(true);
                progress.addActor(scoreValue);

                userInfo.add(progress).expandX().fillX().pad(0, 8, 0, 8).height(16).row();
            }

            Table group = new Table();
            group.add(userContainer).top().height(64).row();

            if (BrainOutClient.ClientController.isFreePlay())
            {
                RemoteClient rm = BrainOutClient.ClientController.getMyRemoteClient();
                if (rm != null)
                {
                    Table ratingParent = new Table();
                    ratingParent.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-gray")));

                    Table ratingText = new Table();

                    int reputation = rm.getInfoInt("karma", -5);

                    ratingText.add(new Label(L.get("MENU_REPUTATION"), BrainOutClient.Skin, "title-small")).padRight(16);

                    String reputationString;

                    if (reputation > 1)
                    {
                        reputationString = L.get("MENU_REPUTATION_ENFORCER");
                    }
                    else if (reputation < -1)
                    {
                        reputationString = L.get("MENU_REPUTATION_HITMAN");
                    }
                    else
                    {
                        reputationString = L.get("MENU_REPUTATION_NEUTRAL");
                    }

                    Label reputationText = new Label(reputationString, BrainOutClient.Skin, "title-small");
                    reputationText.setColor(ClientFreeRealization.GetKarmaColor(reputation));

                    ratingText.add(reputationText);

                    ratingParent.add(ratingText).expandX().fillX().row();

                    group.add(ratingParent).expandX().fillX().left().row();
                }
            }
            else if (BrainOutClient.ClientController.isRatingEnabled())
            {
                Array<ClientEvent> events = BrainOutClient.ClientController.getOnlineEvents();

                ClientBattlePassEvent activeBattlePass = null;
                for (ClientEvent event : events)
                {
                    if (event instanceof ClientBattlePassEvent)
                    {
                        activeBattlePass = ((ClientBattlePassEvent) event);
                        break;
                    }
                }

                if (activeBattlePass == null)
                {
                    Table ratingParent = new Table();
                    ratingParent.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-gray")));

                    ratingParent.add(new Image(BrainOutClient.getRegion("icon-rating"))).left();

                    Table ratingText = new Table();
                    float rating = BrainOutClient.ClientController.getUserProfile().getStats().get(
                            Constants.Stats.RATING, 0.0f);

                    ratingText.add(new Label(
                        L.get("MENU_RATING") + " [ ",
                        BrainOutClient.Skin, "title-yellow"));

                    ratingText.add(new Label(String.valueOf((int)rating),
                        BrainOutClient.Skin, "title-small"));

                    ratingText.add(new Label(" ]",
                        BrainOutClient.Skin, "title-yellow"));

                    ratingParent.add(ratingText).expandX().fillX().row();
                    group.add(ratingParent).expandX().fillX().left().row();
                }
                else
                {
                    ClientBattlePassEvent finalActiveBattlePass = activeBattlePass;

                    Button pass = new Button(BrainOutClient.Skin, "button-notext");

                    MenuBadge.apply(pass, new WithBadge()
                    {
                        @Override
                        public boolean hasBadge(UserProfile profile, Involve involve)
                        {
                            return profile.hasBadge("battle-pass-tasks-daily") || profile.hasBadge("battle-pass-tasks-weekly") || profile.hasBadge("battle-pass-rewards");
                        }

                        @Override
                        public String getBadgeId()
                        {
                            return null;
                        }
                    });

                    BattlePassEvent.CurrentStage st = activeBattlePass.getCurrentStage(activeBattlePass.score);

                    Table stageNumber = new Table();
                    Image stageBg = new Image(BrainOutClient.Skin, "icon-battle-pass-stage-complete");
                    stageBg.setScaling(Scaling.none);
                    stageBg.setOrigin(Align.center);
                    stageBg.setFillParent(true);
                    stageNumber.addActor(stageBg);
                    Label stageId = new Label(String.valueOf(st.completedIndex + 1),
                            BrainOutClient.Skin, "title-small");
                    stageId.setAlignment(Align.center);
                    stageId.setBounds(2, 3, 32, 32);
                    stageNumber.addActor(stageId);

                    pass.add(stageNumber).pad(-4).padLeft(8).padRight(8).padBottom(-8).size(32);

                    Label lll = new Label(L.get("MENU_BP_STAGE"), BrainOutClient.Skin, "title-yellow");
                    lll.setEllipsis(true);
                    pass.add(lll).expandX().padLeft(8).fillX();

                    {
                        Table stats = new Table();

                        Label title = new Label(String.valueOf(st.remainingScore) + " / " + st.stage.target,
                                BrainOutClient.Skin, "title-small");
                        stats.add(title).pad(4);

                        Image bp = new Image(BrainOutClient.Skin, "icon-battle-pass-points");
                        bp.setScaling(Scaling.none);
                        stats.add(bp).pad(4);
                        pass.add(stats);
                    }


                    pass.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(Menu.MenuSound.select);
                            BrainOutClient.getInstance().topState().topMenu().pushMenu(new BattlePassMenu(
                                finalActiveBattlePass));
                        }
                    });
                    group.add(pass).expandX().fillX().left().row();
                }
            }

            add(group).top();

            int casesAmount = user.getAmountOf(Case.class);
            renderCustomStats();

            if (enableCaseButton)
            {
                renderStats();
            }

            add().expandX();

            if (enableCaseButton)
            {
                String caseIcon = "standard-case";

                Object caseIcon_ = null;

                if (EnvironmentService.Get() != null && EnvironmentService.Get().getEnvironmentVariables() != null)
                {
                    caseIcon_ = EnvironmentService.Get().getEnvironmentVariables().getOrDefault("case-icon", null);
                }

                if (caseIcon_ != null && !caseIcon_.toString().isEmpty())
                {
                    if (BrainOutClient.getRegion(caseIcon_.toString()) != null)
                    {
                        caseIcon = caseIcon_.toString();
                    }
                }

                this.caseButton = new BadgeButton(caseIcon, casesAmount, false);

                add(this.caseButton).size(128, 96);

                this.caseButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(Menu.MenuSound.select);

                        GameState topState = BrainOutClient.getInstance().topState();
                        UserProfile profile = BrainOutClient.ClientController.getUserProfile();

                        if (profile != null)
                        {
                            topState.pushMenu(new ContainersMenu(profile));
                        }
                    }
                });

                Object caseNotice = EnvironmentService.Get().getEnvironmentVariables().getOrDefault("case-notice", null);

                if (caseNotice != null && !caseNotice.toString().isEmpty())
                {
                    Table noteTable = new Table();
                    noteTable.setTouchable(Touchable.disabled);
                    noteTable.align(Align.right | Align.bottom);
                    noteTable.setFillParent(true);

                    Label caseNote = new Label(caseNotice.toString(), BrainOutClient.Skin, "title-small");
                    BorderActor noteBorder = new BorderActor(caseNote, "form-red");
                    noteBorder.getCell().padBottom(1).padTop(-1);
                    noteTable.add(noteBorder).size(56, 20);

                    this.caseButton.addActor(noteTable);
                }
            }
        }
    }

    protected void renderStats()
    {
        int skillPoints = user.getInt(Constants.User.SKILLPOINTS, 0);
        int gears = user.getInt(Constants.User.GEARS, 0);
        int nuclearMaterial = user.getInt(Constants.User.NUCLEAR_MATERIAL, 0);
        int ru = user.getInt("ru", 0);

        addStat(String.valueOf(skillPoints), "skillpoints-big", -8, 68);
        addStat(String.valueOf(gears), "icon-gears-big", 0, 68);

        if (nuclearMaterial > 0)
        {
            addStat(String.valueOf(nuclearMaterial) + " / " + Constants.DailyReward.MAX_DAILY_CONTAINERS,
                    "icon-nuclear-material-big", -6, 80);
        }

        if (ru > 0)
        {
            String amount = String.valueOf(ru);

            addStat(amount + " RU", "icon-ru-5000", -6, 120);
        }
    }

    protected void renderCustomStats()
    {

    }

    private void fetchAvatar(Table avatarInfo)
    {
        Avatars.Get(user.getAvatar(), (has, avatar) ->
        {
            if (has)
            {
                Image avatarImage = new Image(avatar);

                avatarImage.setScaling(Scaling.fit);

                avatarInfo.add(avatarImage).size(48, 48).row();
            }
        });
    }

    protected String getLevelStyle()
    {
        return "title-level";
    }

    protected String getLevelString(Levels.Level level)
    {
        return level.toString();
    }

    protected void addLevelNumberIcon(Table levelInfo)
    {

    }

    protected String getProgressStyle()
    {
        return "progress-score";
    }

    protected Levels.Level getLevel()
    {
        Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.LEVEL);

        if (levels == null)
        {
            return null;
        }

        return levels.getLevel(user.getLevel(
            Constants.User.LEVEL, 1
        ));
    }

    protected int getScore()
    {
        return user.getInt("score", 0);
    }

    protected TextButton addStat(String value, String icon, float pad, float width)
    {
        TextButton btn = new TextButton("", BrainOutClient.Skin, "button-default");

        Table border = new Table();
        border.align(Align.bottom);
        border.setFillParent(true);
        border.setTouchable(Touchable.disabled);

        btn.addActor(border);

        Image statIcon = new Image(BrainOutClient.getRegion(icon));
        statIcon.setScaling(Scaling.none);

        Label statValue = new Label(value, BrainOutClient.Skin, "title-small");
        statValue.setAlignment(Align.center);

        border.add(statIcon).expandY().fillY().padTop(pad).row();
        border.add(statValue).expandX().padTop(2).padBottom(4).fillX().row();

        btn.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(Menu.MenuSound.select);

                if (openExchange != null)
                {
                    openExchange.run();
                }
            }
        });

        add(btn).top().size(width, 64);

        return btn;
    }

    public void setOpenExchange(Runnable openExchange)
    {
        this.openExchange = openExchange;
    }

    public void refresh()
    {
        reset();
        fill();
    }

    public BadgeButton getCaseButton()
    {
        return caseButton;
    }

    public void disable(boolean disable)
    {
        setTouchable(disable ? Touchable.disabled : Touchable.childrenOnly);
    }
}
