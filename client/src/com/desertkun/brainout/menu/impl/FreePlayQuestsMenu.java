package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.states.CSFreeplaySolo;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.quest.DailyQuest;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.content.quest.Tree;
import com.desertkun.brainout.content.quest.task.ClientTask;
import com.desertkun.brainout.content.quest.task.Task;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.MenuHelper;
import com.desertkun.brainout.menu.ui.RichLabel;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.online.*;
import com.desertkun.brainout.utils.ContentImage;
import com.desertkun.brainout.utils.DurationUtils;
import com.desertkun.brainout.utils.LocalizedString;
import org.anthillplatform.runtime.services.GameService;

import java.util.Comparator;

public class FreePlayQuestsMenu extends Menu
{
    private final Array<Tree> trees;
    private final UserProfile userProfile;
    private final ClientController.RegionWrapper anyRegion;
    private ButtonGroup<Button> buttons;
    private Tree selectedTree;
    private Table selectedQuest;
    private Table selectedQuestInfo;
    private String selectedRegion;

    public FreePlayQuestsMenu()
    {
        userProfile = BrainOutClient.ClientController.getUserProfile();
        anyRegion = new ClientController.RegionWrapper(new GameService.Region(L.get("MENU_ANY_REGION"), null));

        selectedRegion = BrainOutClient.ClientController.getMyRegion();

        if (userProfile == null)
        {
            trees = new Array<>();
            return;
        }

        trees = BrainOutClient.ContentMgr.queryContent(Tree.class, tree ->
            tree.isActive(userProfile, BrainOutClient.ClientController.getMyAccount()));

        trees.sort(Comparator.comparing(Content::getID));

        buttons = new ButtonGroup<>();
        buttons.setMaxCheckCount(1);
        buttons.setMinCheckCount(1);
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        if (trees.size > 0)
        {
            renderQuests(data);
        }
        else
        {
            renderNoQuestsPanel(data);
        }

        return data;
    }

    private void renderNoQuestsPanel(Table data)
    {
        {
            Table tutorial = new Table();

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
                tutorial.add(description).pad(4).padBottom(32).expandX().fillX().row();
            }

            renderQuestActionButtons(tutorial);

            data.add(tutorial).width(600).row();
        }

    }

    private void renderQuests(Table data)
    {
        {
            Table quests = new Table(BrainOutClient.Skin);
            quests.align(Align.top);

            {
                Table header = new Table(BrainOutClient.Skin);
                header.setBackground("form-gray");

                Label title = new Label(L.get("MENU_QUESTS"), BrainOutClient.Skin, "title-yellow");
                title.setAlignment(Align.center);
                header.add(title).expandX().fillX().row();

                quests.add(header).width(196).expandX().left().row();

                renderQuestTrees(quests);
            }

            data.add(quests).top().width(264).padTop(160).padBottom(128).right().expandY().fill();
        }

        {
            Table questsRoot = new Table();
            questsRoot.align(Align.top);

            {
                selectedQuestInfo = new Table(BrainOutClient.Skin);
                selectedQuestInfo.align(Align.center | Align.right);
                questsRoot.add(selectedQuestInfo).expandX().fill().height(30).row();
            }

            {
                selectedQuest = new Table(BrainOutClient.Skin);
                selectedQuest.align(Align.top);

                if (selectedTree != null)
                {
                    renderQuestTree(selectedTree);
                }

                questsRoot.add(selectedQuest).width(528).expandY().fill().row();
            }

            data.add(questsRoot).top().width(528).padTop(160).padBottom(128).left().expandY().fillY().row();
        }
    }

    private void renderQuestTree(Tree tree)
    {
        selectedQuestInfo.clear();
        selectedQuest.clear();

        Quest currentQuest = tree.getCurrentQuest(userProfile, BrainOutClient.ClientController.getMyAccount());

        if (currentQuest == null)
        {
            return;
        }

        if (currentQuest instanceof DailyQuest)
        {
            selectedQuestInfo.add(new Label(L.get("MENU_DAILY_MISSION"), BrainOutClient.Skin, "title-small"));
            Image img = new Image(BrainOutClient.Skin, "icon-quest-daily");
            img.setScaling(Scaling.none);
            selectedQuestInfo.add(img);
        } else if (currentQuest.isPerTaskReward())
        {
            selectedQuestInfo.add(new Label(L.get("MENU_QUEST_CHALLENGE"), BrainOutClient.Skin, "title-small"));
            selectedQuestInfo.add(new Image(BrainOutClient.Skin, "icon-tag-level"));
        } else if (currentQuest.isCoop())
        {
            selectedQuestInfo.add(new Label(L.get("MENU_QUEST_COOP"), BrainOutClient.Skin, "title-small"));
            selectedQuestInfo.add(new Image(BrainOutClient.Skin, "icon-coop"));
        }

        Table contents = new Table(BrainOutClient.Skin);
        contents.setBackground("form-default");

        Table page = new Table(BrainOutClient.Skin);
        page.align(Align.top | Align.center);
        renderQuestPage(page, tree, currentQuest);

        ScrollPane pane = new ScrollPane(page, BrainOutClient.Skin, "scroll-default");
        pane.setFadeScrollBars(false);
        setScrollFocus(pane);

        contents.add(pane).expand().fill().row();

        selectedQuest.add(contents).height(getQuestsPanelHeight()).expandX().fillX().row();

        {
            Label joinKind = new Label(getJoinTitleBeforeButtons(currentQuest), BrainOutClient.Skin, "title-small");

            joinKind.setAlignment(Align.center);
            joinKind.setWrap(true);

            selectedQuest.add(joinKind).expandX().fillX().pad(16).padLeft(64).padRight(64).row();
        }

        renderQuestActionButtons(selectedQuest);
    }

    private String getJoinTitleBeforeButtons(Quest currentQuest)
    {
        if (currentQuest.isPerTargetItemReward())
        {
            return L.get("QUEST_SPECIAL_REWARD_FOR_EACH_PROGRESS");
        }

        if (currentQuest.isPerTaskReward())
        {
            return L.get("MENU_QUEST_PER_TASK_REWARD");
        }

        return L.get(
            currentQuest.isCoop() ? "MENU_QUEST_JOIN_COOP" : "MENU_QUEST_JOIN_SOLO"
        );
    }

    protected int getQuestsPanelHeight()
    {
        return 420;
    }

    protected void renderQuestActionButtons(Table renderTo)
    {
        {
            Table buttons = new Table();

            {
                Table regionSelector = new Table();
                Label title = new Label(L.get("MENU_REGION") + ": ", BrainOutClient.Skin, "title-small");
                regionSelector.add(title).padRight(8);

                SelectBox<ClientController.RegionWrapper> checkBox =
                        new SelectBox<>(BrainOutClient.Skin, "select-badged");

                ClientController.RegionWrapper select = anyRegion;

                Array<ClientController.RegionWrapper> items = new Array<>();

                items.add(anyRegion);

                for (ClientController.RegionWrapper region : BrainOutClient.ClientController.getRegions())
                {
                    items.add(region);
                }

                if (BrainOutClient.ClientController.getMyRegion() != null)
                {
                    for (ClientController.RegionWrapper region : BrainOutClient.ClientController.getRegions())
                    {
                        if (region.region.name.equals(BrainOutClient.ClientController.getMyRegion()))
                        {
                            select = region;
                            break;
                        }
                    }
                }

                checkBox.addListener(new ChangeListener()
                {
                    @Override
                    public void changed(ChangeEvent event, Actor actor)
                    {
                        Menu.playSound(MenuSound.select);

                        if (checkBox.getSelected() != anyRegion)
                        {
                            selectedRegion = checkBox.getSelected().region.name;
                        }
                        else
                        {
                            selectedRegion = null;
                        }
                    }
                });

                checkBox.setItems(items);
                checkBox.setSelected(select);

                regionSelector.add(checkBox).expandX().fillX().height(32).row();
                buttons.add(regionSelector).colspan(2).expandX().fillX().height(32).padBottom(8).padLeft(64).padRight(64).row();
            }

            {
                Button coop = new Button(BrainOutClient.Skin, "button-notext");

                coop.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        playSolo();
                    }
                });

                Label title = new Label(L.get("MENU_FREEPLAY_PLAY_SOLO"), BrainOutClient.Skin, "title-small");
                title.setWrap(true);
                title.setAlignment(Align.center);
                coop.add(title).expandX().fillX().row();

                buttons.add(coop).expandX().fill().uniformX().height(64);
            }

            {
                Button coop = new Button(BrainOutClient.Skin, "button-green");

                coop.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        playWithPartner();
                    }
                });

                Image add = new Image(BrainOutClient.Skin, "icon-add");
                coop.add(add);

                Label title = new Label(L.get("MENU_FREEPLAY_PLAY_COOP"), BrainOutClient.Skin, "title-small");
                title.setWrap(true);
                coop.add(title).expandX().fillX().row();

                buttons.add(coop).expandX().fill().uniformX().height(64).row();
            }

            renderTo.add(buttons).expandX().fillX().row();
        }
    }

    private void playWithPartner()
    {
        if (!BrainOut.OnlineEnabled())
        {
            popMeAndPushMenu(new AlertPopup(
                BrainOutClient.Env.getOfflineBuildError("Run Server Free Play.bat")
            ));
            return;
        }

        GameState gs = getGameState();

        if (gs == null)
            return;

        pop();

        FreePlayPartnerLobby lobby = new FreePlayPartnerLobby();
        if (selectedRegion != null)
        {
            lobby.setSelectedRegion(selectedRegion);
        }
        gs.pushMenu(lobby);

        lobby.invite();
    }

    private void playSolo()
    {
        if (!BrainOut.OnlineEnabled())
        {
            popMeAndPushMenu(new AlertPopup(
                BrainOutClient.Env.getOfflineBuildError("Run Server Free Play.bat")
            ));
            return;
        }

        BrainOutClient.ClientController.setState(new CSFreeplaySolo(selectedRegion));
    }

    private void renderQuestPage(Table page, Tree tree, Quest currentQuest)
    {
        {
            Table header = new Table(BrainOutClient.Skin);

            String titleString = currentQuest.getTitle().get();

            if (currentQuest.hasGroup())
            {
                titleString += " ( " + tree.getQuestIndex(currentQuest) + " / " + tree.getQuestsInGroup(currentQuest) + " )";
            }

            Label title = new Label(titleString, BrainOutClient.Skin, "title-red-bg");
            title.setAlignment(Align.center);
            header.add(title).expandX().fillX().row();

            {
                IconComponent iconComponent = currentQuest.getComponent(IconComponent.class);

                if (iconComponent != null)
                {
                    TextureAtlas.AtlasRegion bg = iconComponent.getIcon("bg", null);

                    if (bg != null)
                    {
                        Image bgImage = new Image(bg);
                        header.add(bgImage).row();
                    }
                }
            }

            page.add(header).pad(16).row();
        }

        if (currentQuest instanceof DailyQuest)
        {
            Label nextOrder = new Label(L.get("MENU_DAILY_QUEST_NEXT"), BrainOutClient.Skin, "title-small");
            page.add(nextOrder).expandX().center().pad(32).padBottom(0).row();

            final long tm = (int)((DailyQuest) currentQuest).getTimeToNextQuest(userProfile);
            Label timer = new Label(DurationUtils.GetDurationString((int)tm), BrainOutClient.Skin, "title-yellow");

            timer.addAction(Actions.forever(Actions.sequence(
                Actions.run(() ->
                {
                    long time = ((DailyQuest) currentQuest).getTimeToNextQuest(userProfile);
                    timer.setText(DurationUtils.GetDurationString((int)time));

                    if (time > tm)
                    {
                        // timer overflown
                        Gdx.app.postRunnable(FreePlayQuestsMenu.this::reset);
                    }
                }),
                Actions.delay(1)
            )));

            page.add(timer).expandX().center().pad(32).padTop(0).row();

            if (((DailyQuest) currentQuest).isQuestDoneForToday(userProfile))
            {
                return;
            }
        }

        {
            Table conditions = new Table();

            if (currentQuest.isCoop())
            {
                addCondition(conditions, L.get("MENU_QUEST_COOP_REQUIRED"));
            }

            if (currentQuest.isFindLocationFirst())
            {
                for (LocalizedString location : currentQuest.getRelatedLocations())
                {
                    addCondition(conditions, L.get("QUEST_SPECIAL_FIND_LOCATION", location.get()));
                }
            }

            for (LocalizedString note : currentQuest.getSpecialNotes())
            {
                addCondition(conditions, note.get());
            }

            if (!currentQuest.isFindLocationFirst())
            {
                for (LocalizedString location : currentQuest.getRelatedLocations())
                {
                    addCondition(conditions, L.get("QUEST_SPECIAL_FIND_LOCATION", location.get()));
                }
            }

            if (currentQuest.isShowRelatedItemsText())
            {
                for (Content relatedItem : currentQuest.getRelatedItems())
                {
                    addCondition(conditions, L.get("QUEST_SPECIAL_FIND_ITEM", relatedItem.getTitle().get()));
                }
            }

            for (ObjectMap.Entry<String, Task> entry : currentQuest.getTasks())
            {
                Task task = entry.value;

                if (task instanceof ClientTask)
                {
                    ClientTask clientTask = ((ClientTask) task);

                    if (!clientTask.hasLocalizedName())
                        continue;

                    if (currentQuest.isPerTaskReward())
                    {
                        Table row = new Table();

                        int progress = task.getProgress(userProfile, BrainOutClient.ClientController.getMyAccount());
                        int target = task.getTarget(BrainOutClient.ClientController.getMyAccount());

                        {
                            Label c = new Label("-",
                                BrainOutClient.Skin, progress >= target ? "title-gray" : "title-small");
                            row.add(c).padLeft(8).padRight(8).padBottom(2).top().left();
                        }
                        {
                            if (clientTask.hasRichLocalization())
                            {
                                RichLabel r = new RichLabel(clientTask.getLocalizedName(),
                                    BrainOutClient.Skin, progress >= target ? "title-gray" : "title-small");

                                row.add(r).expandX().left().padBottom(2).padRight(16).left();
                            }
                            else
                            {
                                Label c = new Label(clientTask.getLocalizedName(),
                                        BrainOutClient.Skin, progress >= target ? "title-gray" : "title-small");
                                c.setAlignment(Align.left);
                                c.setWrap(true);

                                row.add(c).expandX().fillX().padBottom(2).padRight(16).left();
                            }
                        }

                        {
                            Label targetLabel = new Label(progress + "/" + target,
                                BrainOutClient.Skin, progress >= target ? "title-gray" : "title-yellow");
                            row.add(targetLabel).top();
                        }

                        conditions.add(row).expandX().fillX().padBottom(2).row();
                    }
                    else
                    {
                        addCondition(conditions, clientTask.getLocalizedName());
                    }
                }
            }

            if (currentQuest.isHaveToLeave())
            {
                addCondition(conditions, L.get("MENU_QUEST_HAS_TO_LEAVE"));
            }

            page.add(conditions).expandX().pad(16).fill().row();
        }

        {
            Table conditionsProgress = new Table();
            conditionsProgress.align(Align.center);

            int i = 0;

            for (Content relatedItem : currentQuest.getRelatedItems())
            {
                Table relatedItemRoot = new Table(BrainOutClient.Skin);
                relatedItemRoot.setBackground("form-transparent");
                relatedItemRoot.align(Align.bottom);

                ContentImage.RenderImage(relatedItem, relatedItemRoot, 1);

                conditionsProgress.add(relatedItemRoot).size(192, 64).pad(2);

                i++;

                if (i % 2 == 0)
                {
                    conditionsProgress.row();
                }
            }

            for (ObjectMap.Entry<String, Task> entry : currentQuest.getTasks())
            {
                Task task = entry.value;

                if (task instanceof ClientTask)
                {
                    ClientTask clientTask = ((ClientTask) task);

                    if (clientTask.hasProgress())
                    {
                        if (clientTask.hasIcon() && !currentQuest.isPerTaskReward())
                        {
                            Table conditionProgress = new Table(BrainOutClient.Skin);
                            conditionProgress.setBackground("form-transparent");
                            conditionProgress.align(Align.bottom);

                            int progress = task.getProgress(userProfile,
                                BrainOutClient.ClientController.getMyAccount());

                            Label target = new Label(String.valueOf(progress) + "/" + task.getTarget(
                                BrainOutClient.ClientController.getMyAccount()),
                                BrainOutClient.Skin, "title-yellow");

                            conditionProgress.add(target).expand().right().bottom().pad(2).row();

                            if (progress > 0)
                            {
                                ProgressBar progressBar = new ProgressBar(
                                    0, task.getTarget(BrainOutClient.ClientController.getMyAccount()), 1, false,
                                    BrainOutClient.Skin, "progress-inventory");
                                progressBar.setValue(progress);

                                progressBar.setBounds(0, 0, 192, 2);

                                conditionProgress.addActor(progressBar);
                            }

                            clientTask.renderIcon(conditionProgress);

                            conditionsProgress.add(conditionProgress).size(192, 64).pad(2);

                            i++;

                            if (i % 2 == 0)
                            {
                                conditionsProgress.row();
                            }
                        }
                    }
                }
            }

            page.add(conditionsProgress).expandX().pad(16).fill().row();
        }

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("MENU_QUEST_REWARD"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            header.add(title).expandX().fillX().row();

            page.add(header).expandX().pad(16).padBottom(8).fillX().row();
        }

        if (currentQuest.isCoop())
        {
            Image coopIcon = new Image(BrainOutClient.Skin, "icon-coop");
            page.add(coopIcon).row();

            Label doubleReward = new Label(L.get("MENU_QUEST_COOP_REWARD"),
                    BrainOutClient.Skin, "title-small");
            doubleReward.setAlignment(Align.center);
            doubleReward.setWrap(true);

            page.add(doubleReward).expandX().fillX().padBottom(8).row();
        }

        {
            Table rewards = new Table(BrainOutClient.Skin);

            int i = 0;

            for (Reward reward : currentQuest.getRewards())
            {
                if (!(reward instanceof ClientReward))
                    continue;

                ClientReward clientReward = ((ClientReward) reward);
                ClientReward.ClientAction clientAction = ((ClientReward.ClientAction) clientReward.getAction());

                Table rewardContents = new Table(BrainOutClient.Skin);
                rewardContents.setBackground("form-default");

                clientAction.render(rewardContents);

                int amount = reward.getAction().getAmount();

                if (amount > 1)
                {
                    String amountTitle = "x" + amount;
                    Label amountLabel = new Label(amountTitle, BrainOutClient.Skin, "title-small");

                    amountLabel.setBounds(4, 2, 184, 60);
                    amountLabel.setAlignment(Align.right | Align.bottom);
                    amountLabel.setTouchable(Touchable.disabled);

                    rewardContents.addActor(amountLabel);
                }

                Tooltip.RegisterToolTip(rewardContents, clientAction.getLocalizedTitle(), this);

                rewards.add(rewardContents).size(192, 64).pad(2);

                i++;

                if (i % 2 == 0)
                {
                    rewards.row();
                }
            }

            page.add(rewards).expandX().fillX().row();
        }

        page.add().height(16).row();

    }

    private void addCondition(Table conditions, String v)
    {
        Label c = new Label(" - " + v, BrainOutClient.Skin, "title-small");
        c.setWrap(true);
        c.setAlignment(Align.left);

        conditions.add(c).expandX().fillX().row();
    }

    private void renderQuestTrees(Table root)
    {
        buttons.clear();

        selectedTree = null;

        for (Tree tree : trees)
        {
            Button btn = new Button(BrainOutClient.Skin, "button-notext-checkable");
            btn.align(Align.center | Align.left);

            Quest currentQuest = tree.getCurrentQuest(userProfile, BrainOutClient.ClientController.getMyAccount());

            if (currentQuest == null)
                continue;

            if (tree.isLocked(userProfile))
            {
                {
                    Image iconImage = new Image(BrainOutClient.Skin, "icon-lock-small");
                    btn.add(iconImage).pad(4);
                }

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.denied);
                    }
                });
            }
            else
            {
                if (currentQuest instanceof DailyQuest && ((DailyQuest) currentQuest).isQuestDoneForToday(userProfile))
                {
                    Image iconImage = new Image(BrainOutClient.Skin, "icon-daily-quest-complete");
                    btn.add(iconImage).pad(4);
                }
                else
                {
                    Image iconImage = new Image(BrainOutClient.Skin,
                        currentQuest.isCoop() ? "icon-coop-small" : "quest-icon-star");
                    btn.add(iconImage).pad(4);
                }

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        renderQuestTree(tree);
                    }
                });
            }

            if (currentQuest instanceof DailyQuest && ((DailyQuest) currentQuest).isQuestDoneForToday(userProfile))
            {
                Table status = new Table();

                status.add(new Label(currentQuest.getTitle().get(), BrainOutClient.Skin, "title-small")).expandX().left().row();
                status.add(new Label(L.get("MENU_COMPLETE"), BrainOutClient.Skin, "title-green")).expandX().left().row();

                btn.add(status);
            }
            else if (currentQuest.hasProgress(userProfile, BrainOutClient.ClientController.getMyAccount()))
            {
                Table status = new Table();

                status.add(new Label(currentQuest.getTitle().get(), BrainOutClient.Skin, "title-small")).expandX().left().row();
                status.add(new Label(L.get("MENU_QUEST_ACTIVE"), BrainOutClient.Skin, "title-yellow")).expandX().left().row();

                btn.add(status);
            }
            else
            {
                btn.add(new Label(currentQuest.getTitle().get(), BrainOutClient.Skin, "title-small"));
            }

            buttons.add(btn);

            root.add(btn).expandX().fillX().height(48).row();

            if (selectedTree == null)
            {
                selectedTree = tree;
                btn.setChecked(true);
            }
        }
    }

    @Override
    public boolean escape()
    {
        pop();
        return true;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        addRightTopButtons();
    }

    protected void addRightTopButtons()
    {
        MenuHelper.AddCloseButton(this, this::pop);
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
}
