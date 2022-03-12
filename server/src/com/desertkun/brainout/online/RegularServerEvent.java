package com.desertkun.brainout.online;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.EventRewardND;
import com.desertkun.brainout.common.msg.server.OnlineEventUpdated;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.EventService;
import org.json.JSONObject;

import java.util.TimerTask;

public class RegularServerEvent extends Event implements ServerEvent
{
    private final PlayerClient client;
    private float scoreAdded;
    private boolean keep;
    private TimerTask postEventScore;

    @Override
    public Event getEvent()
    {
        return this;
    }

    public RegularServerEvent(PlayerClient client, EventService.Event event)
    {
        super(event);

        this.client = client;
    }

    @Override
    public void parse(EventService.Event event)
    {
        super.parse(event);

        switch (behaviour)
        {
            case increment:
            {
                this.score += scoreAdded;

                break;
            }
        }
    }

    public void setKeep(boolean keep)
    {
        this.keep = keep;
    }

    public boolean isKeep()
    {
        return keep;
    }

    @Override
    protected Reward newReward()
    {
        return new ServerReward();
    }

    private void addScore(float score)
    {
        if (!isValid())
            return;

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

        int index = 0;

        for (EventReward reward : rewards)
        {
            final int rewardIndex = index++;

            if (reward.isJustComplete(score))
            {
                BrainOutServer.PostRunnable(() ->
                {
                    rewardUnlocked(rewardIndex, reward);
                });
            }
        }

        updated();
    }

    public void addScoreMaximum(float score)
    {
        if (behaviour != Behaviour.maximum)
            return;

        addScore(score);
    }

    private void rewardUnlocked(int rewardIndex, EventReward reward)
    {
        client.designEvent(1, "gameplay", "event-reward-unlocked", "reward-" + (rewardIndex + 1));

        client.getProfile().addBadge(getBadgeId());

        client.notify(NotifyAward.none, 0, NotifyReason.eventRewardUnlocked, NotifyMethod.message,
                new EventRewardND(id, getRewardsUnlocked(), getRewardsCount()));

        postScoreScheduled();
    }

    private void updated()
    {
        client.sendTCP(new OnlineEventUpdated(id, this.score));
    }

    @Override
    public void statAdded(String stat, float amount)
    {
        if (stat != null && stat.equals(getTargetStat()))
        {
            client.designEvent(amount, "gameplay", "event-progress");
            client.designEvent(amount, "gameplay", "event-progress-task", getTargetStat());

            addScore(amount);
        }
    }

    private void postScoreScheduled()
    {
        if (postEventScore != null) postEventScore.cancel();

        postEventScore = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() ->
                        postScore());
            }
        };

        BrainOutServer.Timer.schedule(postEventScore, 500);
    }

    private void postScore()
    {
        EventService eventService = EventService.Get();

        if (eventService == null)
            return;

        JSONObject leaderboard_info = new JSONObject();
        JSONObject profile = new JSONObject();

        if (group)
        {
            leaderboard_info.put("display_name", client.getClanName());

            if (!client.getClanAvatar().isEmpty())
            {
                profile.put("avatar", client.getClanAvatar());
            }
        }
        else
        {
            leaderboard_info.put("display_name", client.getName());

            profile.put("level", client.getProfile().getLevel(Constants.User.LEVEL));

            if (client.getAvatar() != null)
                profile.put("avatar", client.getAvatar());

            if (client.getAccessTokenCredential() != null)
                profile.put("credential", client.getAccessTokenCredential());
        }

        leaderboard_info.put("expire_in", "241920");
        leaderboard_info.put("profile", profile);

        if (isGroup())
        {
            if (isJoined())
            {
                eventService.addGroupEventScore(client.getAccessToken(),
                    String.valueOf(id),
                    client.getClanId(),
                    scoreAdded, false, leaderboard_info,
                    (service, request, result, newScore) ->
                {
                    if (result == Request.Result.success)
                    {
                        scorePosted(newScore);
                    }
                });
            }
        }
        else
        {
            eventService.addEventScore(client.getAccessToken(),
                String.valueOf(id), scoreAdded, true, leaderboard_info,
                (service, request, result, newScore) ->
            {
                if (result == Request.Result.success)
                {
                    scorePosted(newScore);
                }
            });
        }
    }

    private void scorePosted(float newScore)
    {
        scoreAdded = 0;
        score = newScore;

        updated();
    }

    @Override
    public void store()
    {
        if (scoreAdded != 0)
        {
            postScoreScheduled();
        }
    }

    public void claim(int rewardIndex, ClaimResult claimResult)
    {
        if (rewardIndex >= rewards.size)
        {
            claimResult.done(false);
            return;
        }

        EventReward reward = rewards.get(rewardIndex);

        if (reward.isComplete() && !reward.isClaimed())
        {
            EventService eventService = EventService.Get();

            if (eventService == null)
            {
                claimResult.done(false);
                return;
            }

            JSONObject condition = new JSONObject();

            condition.put("@func", "!=");
            condition.put("@cond", true);
            condition.put("@then", true);

            JSONObject profile = new JSONObject();
            profile.put("claim_" + (int)reward.targetScore, condition);

            eventService.updateEventProfile(client.getAccessToken(),
                String.valueOf(id), profile,
                (service, request, result, newData) ->
            {
                if (result == Request.Result.success)
                {
                    BrainOutServer.PostRunnable(() -> rewardClaimed(rewardIndex, reward));

                    claimResult.done(true);
                }
                else
                {
                    claimResult.done(false);
                }

            });
        }
        else
        {
            claimResult.done(false);
        }
    }

    private void rewardClaimed(int rewardIndex, EventReward reward)
    {
        client.designEvent(1, "gameplay", "event-reward-claimed", "reward-" + (rewardIndex + 1));

        if (reward.reward.getAction() instanceof ServerReward.ServerAction)
        {
            ServerReward.ServerAction action = ((ServerReward.ServerAction) reward.reward.getAction());

            action.apply(client, true);
        }

        reward.claimed = true;
    }
}
