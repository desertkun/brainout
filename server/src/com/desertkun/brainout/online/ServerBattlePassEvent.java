package com.desertkun.brainout.online;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.BattlePassEventRewardND;
import com.desertkun.brainout.common.enums.data.LevelND;
import com.desertkun.brainout.common.msg.server.BattlePassTaskProgressUpdateMsg;
import com.desertkun.brainout.common.msg.server.OnlineEventUpdated;
import com.desertkun.brainout.content.battlepass.BattlePass;
import com.desertkun.brainout.data.battlepass.BattlePassData;
import com.desertkun.brainout.data.battlepass.BattlePassTaskData;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.EventService;
import org.json.JSONObject;

public class ServerBattlePassEvent extends BattlePassEvent implements ServerEvent
{
    private final PlayerClient client;
    private final BattlePassData data;
    private boolean keep;
    private float scoreAdded;

    public ServerBattlePassEvent(PlayerClient client, EventService.Event event)
    {
        super(event);

        this.client = client;

        BattlePass bp = BrainOut.ContentMgr.get(battlePass, BattlePass.class);
        if (bp != null)
        {
            this.data = bp.getData(this, client.getProfile(), client.getAccount(), event.profile);
        }
        else
        {
            this.data = null;
        }
    }

    @Override
    protected Reward newReward()
    {
        return new ServerReward();
    }

    public BattlePassData getData()
    {
        return data;
    }

    public void addScore(float score)
    {
        switch (behaviour)
        {
            case increment:
            {
                this.scoreAdded += score;
                this.score += score;

                break;
            }

            case maximum:
            {
                if (score > this.score)
                {
                    this.scoreAdded += score - this.score;
                    this.score = score;
                }

                break;
            }
        }

        postScore();
    }

    private void postScore()
    {
        EventService eventService = EventService.Get();

        if (eventService == null)
            return;

        JSONObject profile = new JSONObject();

        profile.put("level", client.getProfile().getLevel(Constants.User.LEVEL));

        if (client.getAvatar() != null)
            profile.put("avatar", client.getAvatar());

        if (client.getAccessTokenCredential() != null)
            profile.put("credential", client.getAccessTokenCredential());

        eventService.addEventScore(client.getAccessToken(),
            String.valueOf(id), scoreAdded, true, new JSONObject(),
            (service, request, result, newScore) ->
        {
            if (result == Request.Result.success)
            {
                scorePosted(newScore);
            }
            else
            {
                client.log("Cannot post battle pass score: " + result.toString());
            }
        });
    }

    private void scorePosted(float newScore)
    {
        float oldScore = newScore - scoreAdded;
        int oldIndex = getCurrentStage(oldScore).completedIndex;

        scoreAdded = 0;
        score = newScore;

        int newIndex = getCurrentStage(score).completedIndex;

        client.log("Battle pass score posted, new value: " + newScore);

        if (oldIndex != newIndex)
        {
            client.log("New battle pass stage: " + newIndex);
            client.notify(NotifyAward.none, 0,
                NotifyReason.battlePassStageComplete,
                NotifyMethod.message, new LevelND(String.valueOf(newIndex + 1)));
        }

        updated();
    }

    private void updated()
    {
        client.sendTCP(new OnlineEventUpdated(id, this.score));
    }

    @Override
    public Event getEvent()
    {
        return this;
    }

    @Override
    public boolean isKeep()
    {
        return keep;
    }

    @Override
    public void setKeep(boolean keep)
    {
        this.keep = keep;
    }

    @Override
    public void store()
    {
        if (data == null)
            return;

        for (BattlePassTaskData task : data.getTasks())
        {
            if (!task.hasAnythingToCommit())
            {
                continue;
            }

            task.commit(client.getAccessToken(), status ->
            {
                if (status)
                {
                    client.log("Updated progress on battle pass event " + id + " task " +
                        task.getTaskKey() + ": " + task.getProgress());
                }
                else
                {
                    client.log("Failed to progress on battle pass event " + id + " task " + task.getTaskKey());
                }
            });
        }
    }

    @Override
    public void statAdded(String stat, float amount)
    {
        int idx = 0;
        for (BattlePassTaskData task : data.getTasks())
        {
            if (task.getTaskActionMatches(stat))
            {
                client.log("Adding battle pass task progress " + task.getTaskKey() + ": " + amount);
                addProgress(task, idx, amount);
            }

            idx++;
        }
    }

    public void addProgress(BattlePassTaskData task, int idx, float amount)
    {
        if (task.isPremium() && !client.getProfile().hasItem(data.getBattlePass(), false))
        {
            return;
        }

        boolean wasCompleted = task.isCompleted();

        task.addProgress((int)amount);

        client.sendTCP(new BattlePassTaskProgressUpdateMsg(getEvent().id, idx,
            task.getUncommittedProgress(), task.getCommittedProgress()));

        if (!wasCompleted && task.isCompleted())
        {
            client.getProfile().addBadge(task.getTasksDefinition().phase == 86400L ?
                "battle-pass-tasks-daily" : "battle-pass-tasks-weekly");

            client.notify(NotifyAward.none, 0, NotifyReason.battlePassTaskCompleted, NotifyMethod.message,
                new BattlePassEventRewardND(getEvent().id, idx));

            BrainOutServer.PostRunnable(this::store);
        }
    }

    @Override
    public void claim(int rewardIndex, ClaimResult claimResult)
    {

    }
}
