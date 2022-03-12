package com.desertkun.brainout.online;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.data.interfaces.WithBadge;
import org.anthillplatform.runtime.services.EventService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

public class Event implements WithBadge
{
    public int id;
    public float score;
    public Date timeStart;
    public Date timeEnd;

    public String taskAction;
    public String taskData;

    public int timeLeft;
    public Date timeLeftNow;
    public String icon;
    public String category;
    public Behaviour behaviour;

    public boolean hasTournament;
    public boolean group;
    public boolean joined;

    public String leaderboardName;
    public String leaderboardOrder;

    public JSONObject profile;
    public Array<EventReward> rewards;
    public Array<EventTournamentReward> tournamentRewards;

    public enum Behaviour
    {
        increment,
        maximum
    }

    public class EventReward
    {
        public Reward reward;
        public boolean claimed;
        public float targetScore;

        public EventReward(JSONObject data)
        {
            reward = newReward();
            targetScore = (float)data.optDouble("target", 0);
            reward.read(data, 1);

            if (profile != null && profile.has("claim_" + (int)targetScore))
            {
                claimed = true;
            }
            else
            {
                claimed = false;
            }
        }

        public boolean isJustComplete(float scoreAdded)
        {
            return score - scoreAdded < targetScore && isComplete();
        }

        public boolean isComplete()
        {
            return score >= targetScore;
        }

        public boolean isClaimed()
        {
            return claimed;
        }
    }

    public class EventTournamentReward
    {
        public Reward reward;
        public int rankFrom;
        public int rankTo;

        public EventTournamentReward(JSONObject data)
        {
            reward = newReward();
            reward.read(data, 1);

            rankFrom = data.optInt("rank_from", 0);
            rankTo = data.optInt("rank_to", 0);
        }

        public boolean isMatches(int rank)
        {
            return rank >= rankFrom && rank <= rankTo;
        }
    }

    public float getTargetScore()
    {
        float target = 0;

        for (EventReward reward : rewards)
        {
            if (reward.targetScore > target)
            {
                target = reward.targetScore;
            }
        }

        return target;
    }

    public int getSecondsLeft()
    {
        long passed = (System.currentTimeMillis() - timeLeftNow.getTime()) / 1000;

        return Math.max((int)(timeLeft - passed), 0);
    }

    public boolean isValid()
    {
        return getSecondsLeft() > 0;
    }

    public Event(EventService.Event event)
    {
        rewards = new Array<>();
        tournamentRewards = new Array<>();

        parse(event);
    }

    public void parse(EventService.Event event)
    {
        this.id = event.id;
        this.score = event.score;
        this.timeStart = event.timeStart;
        this.timeEnd = event.timeEnd;
        this.group = event.kind == EventService.EventKind.group;
        this.joined = event.joined;

        this.timeLeft = event.timeLeft;
        this.timeLeftNow = new Date();
        this.profile = event.profile;
        this.category = event.category;

        this.hasTournament = event.tournament;
        this.leaderboardName = event.leaderboardName;
        this.leaderboardOrder = event.leaderboardOrder;

        rewards.clear();
        tournamentRewards.clear();

        JSONObject data = event.data;

        String behaviour = data.optString("behaviour", Behaviour.increment.name());

        try
        {
            this.behaviour = Behaviour.valueOf(behaviour);
        }
        catch (Exception ignored)
        {
            this.behaviour = Behaviour.increment;
        }

        this.icon = data.optString("icon");

        if (!group)
        {
            JSONArray rewardsValue = data.optJSONArray("rewards");

            if (rewardsValue != null)
            {
                for (int i = 0, t = rewardsValue.length(); i < t; i++)
                {
                    EventReward eventReward = new EventReward(rewardsValue.getJSONObject(i));
                    rewards.add(eventReward);
                }
            }
        }

        JSONArray tournamentRewardsValue = data.optJSONArray("tournament_rewards");

        if (tournamentRewardsValue != null)
        {
            for (int i = 0, t = tournamentRewardsValue.length(); i < t; i++)
            {
                EventTournamentReward eventReward = new EventTournamentReward(tournamentRewardsValue.getJSONObject(i));
                tournamentRewards.add(eventReward);
            }
        }

        if (event.data.has("task"))
        {
            JSONObject task = event.data.getJSONObject("task");

            this.taskAction = task.optString("action");
            this.taskData = task.optString("data");
        }
        else
        {
            this.taskAction = "";
            this.taskData = "";
        }
    }

    public String getTargetStat()
    {
        if (taskData != null && !taskData.equals(""))
        {
            return taskAction + "-" + taskData;
        }

        return taskAction;
    }

    public int getRewardsUnlocked()
    {
        int unlocked = 0;

        for (EventReward reward : rewards)
        {
            if (reward.isComplete())
            {
                unlocked++;
            }
        }

        return unlocked;
    }

    public boolean isGroup()
    {
        return group;
    }

    public boolean isJoined()
    {
        return joined;
    }

    public int getRewardsCount()
    {
        return rewards.size;
    }

    protected Reward newReward()
    {
        return new Reward();
    }

    @Override
    public boolean hasBadge(UserProfile profile, Involve involve)
    {
        return profile.hasBadge(getBadgeId());
    }

    @Override
    public String getBadgeId()
    {
        return "event-" + id;
    }
}
