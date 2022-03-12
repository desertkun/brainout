package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.content.ClientContractGroup;
import com.desertkun.brainout.content.Contract;
import com.desertkun.brainout.content.ContractGroup;
import com.desertkun.brainout.content.ContractGroupQueue;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.MenuHelper;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.ContentImage;
import org.json.JSONObject;

public class ContractsMenu extends Menu
{
    @Override
    public void onInit()
    {
        super.onInit();

        MenuHelper.AddCloseButton(this, this::pop);
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        Table entries = new Table();
        renderEntries(entries);

        ScrollPane pane = new ScrollPane(entries, BrainOutClient.Skin, "scroll-default");
        data.add(pane).expand().fill().row();

        return data;
    }

    public enum TaskState
    {
        completed,
        inProgress,
        yetToComplete
    }

    private TaskState getTaskState(Contract task)
    {
        UserProfile profile = BrainOutClient.ClientController.getUserProfile();

        if (task.getLockItem().isUnlocked(profile))
        {
            return TaskState.completed;
        }

        if (task.getLockItem().hasDiffStarted(profile))
        {
            return TaskState.inProgress;
        }

        return TaskState.yetToComplete;
    }

    private String getHeaderStyle(TaskState state)
    {
        switch (state)
        {
            case completed:
            {
                return "form-green";
            }
            case inProgress:
            {
                return "form-red";
            }
            case yetToComplete:
            default:
            {
                return "form-gray";
            }
        }
    }

    private String getBodyStyle(TaskState state)
    {
        switch (state)
        {
            case completed:
            {
                return "form-drag-good";
            }
            case inProgress:
            {
                return "form-border-red";
            }
            case yetToComplete:
            default:
            {
                return "form-default";
            }
        }
    }

    private void renderEntries(Table entries)
    {
        entries.clear();

        UserProfile profile = BrainOutClient.ClientController.getUserProfile();
        ContractGroupQueue queue = BrainOut.ContentMgr.get("contracts", ContractGroupQueue.class);

        for (ContractGroup group : queue.getQueue())
        {
            Table line = new Table();

            boolean allCompleted = true;

            for (Contract task : group.getTasks())
            {
                Table t = new Table();
                t.align(Align.top);

                TaskState state = getTaskState(task);

                {
                    Table header = new Table(BrainOutClient.Skin);
                    header.setBackground(getHeaderStyle(state));

                    int progress;
                    int outOf = task.getLockItem().getParam();

                    switch (state)
                    {
                        case yetToComplete:
                        {
                            progress = 0;
                            break;
                        }
                        case completed:
                        {
                            progress = outOf;
                            break;
                        }
                        case inProgress:
                        default:
                        {
                            progress = task.getLockItem().getUnlockValue(profile, 0);
                            break;
                        }
                    }

                    Label title = new Label(progress + " / " + outOf, BrainOutClient.Skin, "title-small");
                    header.add(title).row();

                    t.add(header).width(192).row();
                }

                {
                    Table contents = new Table(BrainOutClient.Skin);
                    contents.setBackground(getBodyStyle(state));
                    contents.align(Align.center);

                    ContentImage.RenderImage(task, contents, 1);

                    t.add(contents).size(192, 64).row();

                    Tooltip.RegisterStandardToolTip(contents, task.getTitle().get(), task.getDescription().get(), this);
                }


                switch (state)
                {
                    case inProgress:
                    {
                        TextButton unlockButton = new TextButton(
                            L.get("MENU_SKIP") + "\n" + task.getSkipPrice() + " RU",
                            BrainOutClient.Skin, "button-default");

                        unlockButton.addListener(new ClickOverListener()
                        {
                            @Override
                            public void clicked(InputEvent event, float x, float y)
                            {
                                Menu.playSound(MenuSound.select);

                                JSONObject args = new JSONObject();
                                args.put("id", task.getID());

                                WaitLoadingMenu loadingMenu = new WaitLoadingMenu("");
                                BrainOutClient.getInstance().topState().pushMenu(loadingMenu);

                                BrainOutClient.SocialController.sendRequest("skip_contract", args,
                                    new SocialController.RequestCallback()
                                {
                                    @Override
                                    public void success(JSONObject response)
                                    {
                                        GameState gs = getGameState();

                                        if (gs == null)
                                            return;

                                        loadingMenu.pop();

                                        Gdx.app.postRunnable(() ->
                                            renderEntries(entries));
                                    }

                                    @Override
                                    public void error(String reason)
                                    {
                                        loadingMenu.pop();

                                        pushMenu(new AlertPopup(L.get(reason)));
                                    }
                                });
                            }
                        });

                        t.add(unlockButton).expandX().fillX().height(64).row();
                        allCompleted = false;
                        break;
                    }
                    case yetToComplete:
                    {
                        allCompleted = false;
                        break;
                    }
                }

                line.add(t).expandY().top().uniformY().pad(8);
            }

            // reward
            {

                Table t = new Table();

                {
                    Table header = new Table(BrainOutClient.Skin);
                    header.setBackground("form-gray");

                    Label title = new Label(L.get("MENU_QUEST_REWARD"), BrainOutClient.Skin, "title-yellow");
                    header.add(title).row();

                    t.add(header).width(192).row();
                }

                {
                    Table contents = new Table(BrainOutClient.Skin);
                    contents.setBackground("form-default");
                    contents.align(Align.center);

                    ClientContractGroup ccg = ((ClientContractGroup) group);

                    if (group.hasItem(profile, false))
                    {
                        Label redeemed = new Label(L.get("MENU_REDEEMED"), BrainOutClient.Skin, "title-small");
                        contents.add(redeemed).row();
                    }
                    else
                    {
                        ccg.getReward().getClientAction().render(contents);
                    }

                    t.add(contents).size(192, 64).row();
                }

                if (allCompleted)
                {
                    if (group.hasItem(profile, false))
                    {
                        t.add().height(64).row();
                    }
                    else
                    {
                        TextButton collect = new TextButton(L.get("MENU_COLLECT"),
                            BrainOutClient.Skin, "button-green");
                        collect.addListener(new ClickOverListener()
                        {
                            @Override
                            public void clicked(InputEvent event, float x, float y)
                            {
                                Menu.playSound(MenuSound.select);

                                JSONObject args = new JSONObject();
                                args.put("id", group.getID());

                                WaitLoadingMenu loadingMenu = new WaitLoadingMenu("");
                                BrainOutClient.getInstance().topState().pushMenu(loadingMenu);

                                BrainOutClient.SocialController.sendRequest("redeem_contract_reward", args,
                                    new SocialController.RequestCallback()
                                {
                                    @Override
                                    public void success(JSONObject response)
                                    {
                                        GameState gs = getGameState();

                                        if (gs == null)
                                            return;

                                        loadingMenu.pop();

                                        Gdx.app.postRunnable(() ->
                                                renderEntries(entries));
                                    }

                                    @Override
                                    public void error(String reason)
                                    {
                                        loadingMenu.pop();

                                        pushMenu(new AlertPopup(L.get(reason)));
                                    }
                                });
                            }
                        });

                        t.add(collect).expandX().fillX().height(64).row();
                    }

                }

                line.add(t).pad(8).expandY().top().uniformY();
            }

            entries.add(line).expandX().fillX().row();
        }
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-clan");
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }
}
