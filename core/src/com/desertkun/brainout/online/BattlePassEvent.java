package com.desertkun.brainout.online;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.EventService;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONArray;
import org.json.JSONObject;

public class BattlePassEvent extends Event
{
    public Array<Stage> stages;
    public String battlePass;

    public interface RedeemResultCallback
    {
        void result(boolean success);
    }

    public class Stage
    {
        public int target;
        public int index;
        public Array<Reward> rewards;
        public Array<Reward> premiumRewards;
        public Stage nextStage;

        public Stage()
        {
            rewards = new Array<>();
            premiumRewards = new Array<>();
        }

        public boolean isRewardRedeemed(boolean premium, int index)
        {
            if (profile == null)
            {
                return false;
            }

            String key = (premium ? "rw-pr-" : "rw-") + this.index + "-" + index;
            return profile.optBoolean(key, false);
        }

        public void redeem(LoginService.AccessToken accessToken, String eventId, boolean premium, int index, RedeemResultCallback callback)
        {
            if (isRewardRedeemed(premium, index))
            {
                callback.result(false);
                return;
            }

            EventService eventService = EventService.Get();
            if (eventService == null)
            {
                callback.result(false);
                return;
            }

            String key = (premium ? "rw-pr-" : "rw-") + this.index + "-" + index;

            JSONObject profileUpdate = new JSONObject();

            {
                JSONObject taskReward = new JSONObject();
                profileUpdate.put(key, taskReward);
                taskReward.put("@func", "!=");
                taskReward.put("@cond", true);
                taskReward.put("@then", true);
            }

            eventService.updateEventProfile(accessToken, eventId, profileUpdate,
                (service, request, result, newData) -> BrainOut.getInstance().postRunnable(() ->
            {
                if (result != Request.Result.success)
                {
                    callback.result(false);
                    return;
                }

                callback.result(true);
            }));
        }
    }

    public BattlePassEvent(EventService.Event event)
    {
        super(event);
    }

    public Array<Stage> getStages()
    {
        return stages;
    }

    public static class CurrentStage
    {
        public Stage stage;
        public int remainingScore;
        public int completedIndex;
    }
    public CurrentStage getCurrentStage(float score)
    {
        CurrentStage s = new CurrentStage();
        s.remainingScore = (int)score;
        s.completedIndex = -1;

        for (Stage stage : stages)
        {
            s.stage = stage;

            if (s.remainingScore >= stage.target)
            {
                s.remainingScore -= stage.target;
                s.completedIndex = stage.index;
            }
            else
            {
                return s;
            }
        }

        // no more stages, all completed
        s.remainingScore = s.stage.target;

        return s;
    }

    @Override
    public void parse(EventService.Event event)
    {
        super.parse(event);

        stages = new Array<>();

        Stage oldStage = null;

        battlePass = event.data.getString("battle-pass");

        JSONArray stages = event.data.optJSONArray("stages");
        if (stages != null)
        {
            for (int i = 0; i < stages.length(); i++)
            {
                JSONObject stage = stages.optJSONObject(i);
                if (stage == null)
                    continue;

                Stage newStage = new Stage();
                if (oldStage != null)
                {
                    oldStage.nextStage = newStage;
                }
                oldStage = newStage;

                newStage.target = stage.optInt("target", 0);
                newStage.index = i;

                JSONArray rewards = stage.optJSONArray("rewards");
                for (int r = 0; r < rewards.length(); r++)
                {
                    JSONObject rr = rewards.optJSONObject(r);
                    if (rr == null)
                        continue;

                    Reward rw = newReward();
                    if (!rw.read(rr, 1))
                        continue;

                    boolean premium = rr.optBoolean("premium", false);

                    if (premium)
                    {
                        newStage.premiumRewards.add(rw);
                    }
                    else
                    {
                        newStage.rewards.add(rw);
                    }
                }

                this.stages.add(newStage);
            }
        }
    }
}
