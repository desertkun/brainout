package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.states.CSFreeplaySolo;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.common.msg.client.FreePlayPlayAgain;
import com.desertkun.brainout.common.msg.client.SummaryReadyMsg;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.InstrumentPartialIconComponent;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.content.quest.task.ClientTask;
import com.desertkun.brainout.content.quest.task.Task;
import com.desertkun.brainout.content.shop.SlotItem;
import com.desertkun.brainout.gs.actions.MenuAction;
import com.desertkun.brainout.gs.actions.WaitAction;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.ActionList;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.LoadingBlock;
import com.desertkun.brainout.online.ClientReward;
import com.desertkun.brainout.online.Reward;
import com.desertkun.brainout.utils.ContentImage;
import org.json.JSONObject;

import java.util.TimerTask;

public class FreePlaySummaryMenu extends Menu
{
    private final JSONObject summary;
    private Table statsContents;
    boolean even;
    private ActionList actions;
    private Table slide;
    private Table buttons;
    private boolean alive;

    public FreePlaySummaryMenu(JSONObject summary, boolean alive)
    {
        Gdx.app.postRunnable(() -> BrainOutClient.MusicMng.playMusic("music-endgame"));

        this.summary = summary;
        this.alive = alive;
        this.actions = new ActionList();
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            data.add(new Image(BrainOutClient.Skin, "logo-small")).padTop(64).expandY().row();
        }

        actions.addAction(new WaitAction(0.5f));

        {
            WidgetGroup holder = new WidgetGroup();
            slide = new Table();

            if (this.summary.has("stats"))
            {
                JSONObject stats = this.summary.optJSONObject("stats");

                if (stats != null)
                {
                    {
                        int valuables = stats.optInt("valuables", 0);
                        if (valuables > 0)
                        {
                            showPage((page) ->
                            {
                                Menu.playSound(MenuSound.rankUp);
                                renderTakenStat(page, "valuables", L.get("MENU_VALUABLES"), valuables);
                            });
                        }
                    }
                    {
                        int ru = stats.optInt("ru", 0);

                        if (ru > 0)
                        {
                            int takenRu = stats.optInt("freeplay-taken-ru", 0);

                            if (takenRu > 0)
                            {
                                showPage((page) ->
                                {
                                    Menu.playSound(MenuSound.contentOwned);
                                    renderTakenStat(page, "ru", "RU", takenRu);
                                });
                            }
                        }
                    }

                    for (String key : stats.keySet())
                    {
                        if (!key.startsWith("blueprint-"))
                            continue;

                        String freeplayItem = "freeplay-" + key;
                        Content content = BrainOut.ContentMgr.get(freeplayItem);

                        if (content == null)
                            continue;

                        int amount = stats.optInt(key, 1);
                        showPage((page) -> renderTakenContent(page, content, amount));
                    }

                    for (String key : stats.keySet())
                    {
                        if (!key.startsWith("parts-of-"))
                            continue;

                        String freeplayItem = "freeplay-" + key;
                        Content content = BrainOut.ContentMgr.get(freeplayItem);

                        if (content == null)
                            continue;

                        int amount = stats.optInt(key, 1);
                        showPage((page) -> renderTakenContent(page, content, amount));

                        InstrumentPartialIconComponent pic = content.getComponent(InstrumentPartialIconComponent.class);

                        if (pic != null)
                        {
                            Content c = pic.getContent();
                            if (c instanceof SlotItem)
                            {
                                ContentLockTree.LockItem lockItem = ((SlotItem) c).getLockItem();
                                int have = (int)(float)BrainOutClient.ClientController.getUserProfile().getStats().get(
                                    key, 0.0f) + amount;

                                if (have >= lockItem.getParam())
                                {
                                    showPage((page) -> renderTakenContent(page, pic.getContent(), amount));
                                }
                            }
                        }
                    }
                }
            }

            if (this.summary.has("quests"))
            {
                JSONObject quests = this.summary.optJSONObject("quests");

                for (String questId: quests.keySet())
                {
                    Quest quest = BrainOut.ContentMgr.get(questId, Quest.class);

                    if (quest == null)
                        continue;

                    JSONObject questObject = quests.getJSONObject(questId);

                    if (questObject.has("tasks-progress"))
                    {
                        JSONObject tasksProgress = questObject.optJSONObject("tasks-progress");

                        for (String taskId : tasksProgress.keySet())
                        {
                            int progress = tasksProgress.getInt(taskId);

                            Task task = quest.getTasks().get(taskId);

                            if (!(task instanceof ClientTask))
                                continue;

                            if (!((ClientTask) task).hasProgress())
                                continue;

                            if (!((ClientTask) task).showInSummaryScreen() && !task.isComplete(
                                BrainOutClient.ClientController.getUserProfile(),
                                BrainOutClient.ClientController.getMyAccount()))
                            {
                                continue;
                            }

                            showPage((page) ->
                            {
                                BrainOutClient.Timer.schedule(new TimerTask()
                                {
                                    private int counter = 10;
                                    @Override
                                    public void run()
                                    {
                                        if (counter-- < 0)
                                        {
                                            cancel();
                                            return;
                                        }

                                        Menu.playSound(MenuSound.character);
                                    }
                                }, 100, 25);

                                renderProgressTask(page, quest, task, progress);
                            });

                            if (quest.isPerTaskReward() && progress >=
                                task.getTarget(BrainOutClient.ClientController.getMyAccount()))
                            {
                                for (Reward reward : quest.getRewards())
                                {
                                    showPage((page) ->
                                    {
                                        renderQuestReward(page, reward);
                                    });
                                }
                            }
                        }
                    }

                    boolean completed = questObject.optBoolean("completed", false);

                    if (completed)
                    {
                        showPage((page) ->
                        {
                            Sound snd = BrainOut.ContentMgr.get("achievement-unlocked-snd", Sound.class);

                            if (snd != null)
                            {
                                snd.play();
                            }

                            renderCompletedQuest(page, quest);
                        });

                        if (!quest.isPerTaskReward())
                        {
                            for (Reward reward : quest.getRewards())
                            {
                                showPage((page) ->
                                {
                                    renderQuestReward(page, reward);
                                });
                            }
                        }

                    }
                }
            }

            slide.setSize(416, 300);
            holder.addActor(slide);

            data.add(holder).expand().size(416, 300).padBottom(64).row();
        }

        this.renderTaken();

        {
            buttons = new Table(BrainOutClient.Skin);
            buttons.align(Align.top);

            {
                TextButton next = new TextButton(L.get("MENU_NEXT"), BrainOutClient.Skin, "button-default");
                next.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        nextPage();
                    }
                });

                buttons.add(next).size(192, 64).pad(8);
            }

            buttons.setVisible(false);

            data.add(buttons).height(160).top().row();
        }

        {
            data.add().expandY().row();
        }

        showPage((page) ->
        {
            {
                Label title = new Label(L.get("MENU_GAME_SUMMARY"), BrainOutClient.Skin, "title-small");
                page.add(title).pad(16).center().row();

            }

            {
                renderStats(page);
            }
        });

        return data;
    }

    private enum PartnerStatus
    {
        solo,
        left,
        waiting,
        ready
    }

    private void renderExitReward(Table page, String stat, String currencyTitle, int amount)
    {
        {
            Label title = new Label(L.get("MENU_SURVIVOR_REWARD"), BrainOutClient.Skin, "title-small");

            page.add(title).pad(16).row();
        }

        Menu.playSound(MenuSound.levelUp);

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(currencyTitle, BrainOutClient.Skin, "title-yellow");
            header.add(title).center().row();

            page.add(header).expandX().fillX().row();
        }

        {
            Table contents = new Table(BrainOutClient.Skin);
            contents.setBackground("form-border-red");

            Table unlockedData = new Table(BrainOutClient.Skin);
            ScrollPane unlockedPane = new ScrollPane(unlockedData, BrainOutClient.Skin, "scroll-default");

            contents.add(unlockedPane).expandX().fillX().height(96).padBottom(8).row();

            Table btn = new Table(BrainOutClient.Skin);

            ContentImage.RenderStatImage(stat, amount, btn);

            if (amount > 1)
            {
                String amountTitle = "x" + amount;
                Label amountLabel = new Label(amountTitle, BrainOutClient.Skin, "title-small");

                amountLabel.setBounds(4, 2, 154, 76);
                amountLabel.setAlignment(Align.right | Align.bottom);
                amountLabel.setTouchable(Touchable.disabled);

                btn.addActor(amountLabel);
            }

            unlockedData.add(btn).size(160, 80).pad(8);
            page.add(contents).expandX().fillX().row();
        }
    }

    private void renderQuestReward(Table page, Reward reward)
    {
        if (!(reward instanceof ClientReward))
            return;

        ClientReward clientReward = ((ClientReward) reward);

        Reward.Action action = clientReward.getAction();

        if (!(action instanceof ClientReward.ClientAction))
            return;

        ClientReward.ClientAction clientAction = ((ClientReward.ClientAction) action);

        {
            Label title = new Label(L.get("MENU_QUEST_REWARD"), BrainOutClient.Skin, "title-small");

            page.add(title).pad(16).row();
        }

        Menu.playSound(MenuSound.contentOwnedEx);

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(clientAction.getLocalizedTitle(), BrainOutClient.Skin, "title-yellow");
            header.add(title).center().row();

            page.add(header).expandX().fillX().row();
        }

        {
            Table contents = new Table(BrainOutClient.Skin);
            contents.setBackground("form-border-red");

            Table unlockedData = new Table(BrainOutClient.Skin);
            ScrollPane unlockedPane = new ScrollPane(unlockedData, BrainOutClient.Skin, "scroll-default");

            contents.add(unlockedPane).expandX().fillX().height(96).padBottom(8).row();

            Table btn = new Table(BrainOutClient.Skin);

            clientAction.render(btn);

            int amount = reward.getAction().getAmount();

            if (amount > 1)
            {
                String amountTitle = "x" + amount;
                Label amountLabel = new Label(amountTitle, BrainOutClient.Skin, "title-small");

                amountLabel.setBounds(4, 2, 154, 76);
                amountLabel.setAlignment(Align.right | Align.bottom);
                amountLabel.setTouchable(Touchable.disabled);

                btn.addActor(amountLabel);
            }

            unlockedData.add(btn).size(160, 80).pad(8);
            page.add(contents).expandX().fillX().row();
        }
    }

    private void renderProgressTask(Table data, Quest quest, Task task, int progress)
    {
        {
            Label complete = new Label(L.get("MENU_QUEST_PROGRESS"), BrainOutClient.Skin, "title-small");

            data.add(complete).pad(16).row();
        }

        ClientTask clientTask = ((ClientTask) task);

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(clientTask.getShortLocalizedName(), BrainOutClient.Skin, "title-yellow");
            header.add(title).center().row();

            data.add(header).width(416).row();
        }


        {
            Table contents = new Table(BrainOutClient.Skin);
            contents.setBackground("form-border-red");

            Table unlockedData = new Table(BrainOutClient.Skin);

            contents.add(unlockedData).expandX().fillX().height(96).pad(8).row();

            Table btn = new Table(BrainOutClient.Skin);

            clientTask.renderIcon(btn);

            if (progress > 0)
            {
                String amountTitle = String.valueOf(progress) + " / " + task.getTarget(
                    BrainOutClient.ClientController.getMyAccount());
                Label amountLabel = new Label(amountTitle, BrainOutClient.Skin, "title-yellow");

                amountLabel.setFillParent(true);
                amountLabel.setAlignment(Align.right | Align.bottom);
                amountLabel.setTouchable(Touchable.disabled);

                unlockedData.addActor(amountLabel);
            }

            unlockedData.add(btn).size(160, 80).pad(8);
            data.add(contents).expandX().fillX().row();
        }
    }

    private void renderCompletedQuest(Table data, Quest quest)
    {
        {
            Label complete = new Label(L.get("MENU_QUEST_COMPLETE"), BrainOutClient.Skin, "title-small");

            data.add(complete).pad(16).row();
        }

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(quest.getTitle().get(), BrainOutClient.Skin, "title-yellow");
            header.add(title).center().row();

            data.add(header).width(416).row();
        }

        IconComponent iconComponent = quest.getComponent(IconComponent.class);

        if (iconComponent != null)
        {
            TextureRegion bg = iconComponent.getIcon("bg", null);

            if (bg != null)
            {
                Image image = new Image(bg);
                data.add(image).pad(-2).row();
            }
        }
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        actions.processActions(delta);
    }

    private interface Page
    {
        void render(Table data);
    }

    private void showPage(Page page)
    {
        actions.addAction(new MenuAction()
        {
            @Override
            public void run()
            {
                slide.clear();
                slide.getColor().a = 0;
                slide.setPosition(128, 0);
                page.render(slide);

                buttons.setVisible(true);

                slide.addAction(Actions.sequence(
                    Actions.parallel(
                        Actions.moveTo(0, 0, 0.25f, Interpolation.circleOut),
                        Actions.alpha(1, 0.25f)
                    )
                ));
            }
        });
    }

    private void nextPage()
    {
        MenuAction current = actions.getCurrentAction();

        if (current != null)
        {
            slide.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.moveTo(-128, 0, 0.25f, Interpolation.circleIn),
                    Actions.alpha(0, 0.25f)
                ),
                Actions.run(current::done)
            ));
        }
    }

    private void renderTaken()
    {
        JSONObject unlocked = this.summary.optJSONObject("unlocked");

        if (unlocked == null)
            return;

        for (String id : unlocked.keySet())
        {
            int amount = unlocked.optInt(id, 0);

            OwnableContent content = BrainOutClient.ContentMgr.get(id, OwnableContent.class);

            if (content == null)
                continue;

            showPage(data -> renderTaken(data, content, amount));
        }
    }

    private void renderTakenContent(Table page, Content content, int amount)
    {
        Menu.playSound(MenuSound.contentOwned);

        {
            Label complete = new Label(L.get("MENU_FREEPLAY_TAKEN"), BrainOutClient.Skin, "title-small");

            page.add(complete).pad(16).row();
        }

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(content.getTitle().get(), BrainOutClient.Skin, "title-yellow");
            header.add(title).center().row();

            page.add(header).expandX().fillX().row();
        }

        {
            Table contents = new Table(BrainOutClient.Skin);
            contents.setBackground("form-border-red");

            Table unlockedData = new Table(BrainOutClient.Skin);
            ScrollPane unlockedPane = new ScrollPane(unlockedData, BrainOutClient.Skin, "scroll-default");

            contents.add(unlockedPane).expandX().fillX().height(96).padBottom(8).row();

            Table btn = new Table(BrainOutClient.Skin);

            InstrumentPartialIconComponent pic = content.getComponent(InstrumentPartialIconComponent.class);

            if (pic != null)
            {
                pic.renderImage(btn);
            }
            else
            {
                ContentImage.RenderImage(content, btn, amount);
            }

            if (amount > 1)
            {
                String amountTitle = "x" + amount;
                Label amountLabel = new Label(amountTitle, BrainOutClient.Skin, "title-small");

                amountLabel.setBounds(4, 2, 154, 76);
                amountLabel.setAlignment(Align.right | Align.bottom);
                amountLabel.setTouchable(Touchable.disabled);

                btn.addActor(amountLabel);
            }

            unlockedData.add(btn).size(160, 80).pad(8);
            page.add(contents).expandX().fillX().row();
        }
    }

    private void renderTakenStat(Table page, String stat, String statTitle, int amount)
    {

        {
            Label complete = new Label(L.get("MENU_FREEPLAY_TAKEN"), BrainOutClient.Skin, "title-small");

            page.add(complete).pad(16).row();
        }

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(statTitle, BrainOutClient.Skin, "title-yellow");
            header.add(title).center().row();

            page.add(header).expandX().fillX().row();
        }

        {
            Table contents = new Table(BrainOutClient.Skin);
            contents.setBackground("form-border-red");

            Table unlockedData = new Table(BrainOutClient.Skin);
            ScrollPane unlockedPane = new ScrollPane(unlockedData, BrainOutClient.Skin, "scroll-default");

            contents.add(unlockedPane).expandX().fillX().height(96).padBottom(8).row();

            Table btn = new Table(BrainOutClient.Skin);

            ContentImage.RenderStatImage(stat, amount, btn);

            if (amount > 1)
            {
                String amountTitle = "x" + amount;
                Label amountLabel = new Label(amountTitle, BrainOutClient.Skin, "title-small");

                amountLabel.setBounds(4, 2, 154, 76);
                amountLabel.setAlignment(Align.right | Align.bottom);
                amountLabel.setTouchable(Touchable.disabled);

                btn.addActor(amountLabel);
            }

            unlockedData.add(btn).size(160, 80).pad(8);
            page.add(contents).expandX().fillX().row();
        }
    }

    private void renderTaken(Table page, OwnableContent content, int amount)
    {
        Menu.playSound(MenuSound.contentOwned);

        {
            Label complete = new Label(L.get("MENU_FREEPLAY_TAKEN"), BrainOutClient.Skin, "title-small");

            page.add(complete).pad(16).row();
        }

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(content.getTitle().get(), BrainOutClient.Skin, "title-yellow");
            header.add(title).center().row();

            page.add(header).expandX().fillX().row();
        }

        {
            Table contents = new Table(BrainOutClient.Skin);
            contents.setBackground("form-border-red");

            Table unlockedData = new Table(BrainOutClient.Skin);
            ScrollPane unlockedPane = new ScrollPane(unlockedData, BrainOutClient.Skin, "scroll-default");

            contents.add(unlockedPane).expandX().fillX().height(96).padBottom(8).row();

            Table btn = new Table(BrainOutClient.Skin);

            ContentImage.RenderImage(content, btn, amount);

            if (amount > 1)
            {
                String amountTitle = "x" + amount;
                Label amountLabel = new Label(amountTitle, BrainOutClient.Skin, "title-small");

                amountLabel.setBounds(4, 2, 154, 76);
                amountLabel.setAlignment(Align.right | Align.bottom);
                amountLabel.setTouchable(Touchable.disabled);

                btn.addActor(amountLabel);
            }

            unlockedData.add(btn).size(160, 80).pad(8);
            page.add(contents).expandX().fillX().row();
        }
    }

    private void renderStats(Table contents)
    {
        JSONObject stats = this.summary.optJSONObject("stats");

        if (stats != null)
        {
            this.statsContents = new Table();

            SequenceAction s = Actions.sequence();

            {
                int timeSpent = (int)stats.optDouble(Constants.Stats.TIME_SPENT, 0.0d);
                addStat(L.get("MENU_STATS_TIME_SPENT"), "stats-time-spent", String.valueOf(timeSpent) + " min", s);
            }
            {
                int shots = (int)stats.optDouble("shots", 0.0d);
                addStat(L.get("MENU_STATS_TOTAL_ROUNDS_WASTED"), "stats-icon-shoots", String.valueOf(shots), s);
            }
            {
                int kills = (int)stats.optDouble(Constants.Stats.KILLS, 0.0d);
                addStat(L.get("MENU_STATS_KILLS"), "stats-kills", String.valueOf(kills), s);
            }
            {
                int headshots = (int)stats.optDouble("headshots", 0.0d);
                addStat(L.get("MENU_STATS_HEADSHOTS"), "stats-headshots", String.valueOf(headshots), s);
            }
            {
                int knife = (int)stats.optDouble("kills-from-weapon-knife", 0.0d);
                addStat(L.get("MENU_STATS_KNIFE_KILLS"), "stats-kills-from-weapon-knife", String.valueOf(knife), s);
            }

            statsContents.addAction(s);

            contents.add(statsContents).expandX().fillX().row();
        }

        buttons.clear();

        if (alive)
        {
            {
                TextButton exit = new TextButton(L.get("MENU_CLOSE"), BrainOutClient.Skin, "button-green");
                exit.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        pop();
                    }
                });

                buttons.add(exit).size(256, 64).pad(8);
            }
        }
        else
        {
            {
                TextButton playMore = new TextButton(L.get("MENU_PLAY_MORE"), BrainOutClient.Skin, "button-green");
                playMore.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        playAgain();
                    }
                });
                buttons.add(playMore).size(256, 64).pad(8);
            }

            {
                TextButton exit = new TextButton(L.get("MENU_EXIT"), BrainOutClient.Skin, "button-default");
                exit.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        close();
                    }
                });

                buttons.add(exit).size(256, 64).pad(8);
            }
        }
    }

    private void playAgain()
    {
        pop();
        BrainOutClient.ClientController.sendTCP(new FreePlayPlayAgain());
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-freeplay");
    }

    @Override
    public boolean lockRender()
    {
        return super.lockRender();
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        Gdx.app.postRunnable(() -> BrainOutClient.MusicMng.stopMusic());
    }

    private void addStat(String title, String icon, String value, SequenceAction s)
    {
        even = !even;

        Table row = new Table(BrainOutClient.Skin);
        row.setBackground(even ? "form-dark-blue" : "border-dark-blue");

        TextureRegion iconRegion = BrainOutClient.getRegion(icon);
        if (iconRegion != null)
        {
            Image iconImage = new Image(iconRegion);
            iconImage.setScaling(Scaling.none);
            row.add(iconImage).height(28).padRight(10).padTop(-2).padBottom(-2);
        }
        else
        {
            row.add().height(28).padRight(10).padTop(-2).padBottom(-2);
        }

        Label titleLabel = new Label(title, BrainOutClient.Skin, "title-yellow");
        row.add(titleLabel).expandX().padTop(-2).padBottom(-2).left();

        Label valueLabel = new Label(value, BrainOutClient.Skin, "title-small");
        row.add(valueLabel).expandX().right().padRight(10).padTop(-2).padBottom(-2).row();

        row.setVisible(false);

        s.addAction(Actions.sequence(
            Actions.delay(0.05f),
            Actions.run(() ->
            {
                Menu.playSound(MenuSound.character);
                row.setVisible(true);
            })
        ));

        statsContents.add(row).expandX().fillX().row();
    }

    @Override
    public boolean escape()
    {
        close();

        return true;
    }

    private void close()
    {
        if (!BrainOut.OnlineEnabled())
        {
            Gdx.app.exit();
            return;
        }

        WaitLoadingMenu loadingMenu = new WaitLoadingMenu("");
        BrainOutClient.getInstance().topState().pushMenu(loadingMenu);

        BrainOutClient.ClientController.disconnect(DisconnectReason.leave, () ->
        {
            loadingMenu.pop();

            BrainOutClient.Env.gameCompleted();

            BrainOutClient.getInstance().popState();
            BrainOutClient.getInstance().initMainMenu().loadPackages();
        });
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
