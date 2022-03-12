package com.desertkun.brainout.data.battlepass;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.battlepass.BattlePass;
import com.desertkun.brainout.content.battlepass.BattlePassTask;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.ByteArrayUtils;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.EventService;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONObject;

public abstract class BattlePassTaskData<T extends BattlePassTask>
{
    private BattlePassData data;
    private final UserProfile userProfile;
    private final T task;
    private String taskKey;
    private int uncommittedProgress;
    private int committedProgress;
    private boolean rewardRedeemed;
    private BattlePass.TasksDefinition tasksDefinition;

    private boolean activeRequest = false;

    public BattlePassTaskData(BattlePassData data, T task, BattlePass.TasksDefinition tasksDefinition,
        UserProfile userProfile, JSONObject eventProfile, String taskKey)
    {
        this.data = data;
        this.task = task;
        this.tasksDefinition = tasksDefinition;
        this.userProfile = userProfile;
        this.taskKey = taskKey;
        this.committedProgress = eventProfile != null ? eventProfile.optInt(getProgressTaskKey(), 0) : 0;
        this.rewardRedeemed = eventProfile != null && eventProfile.optBoolean(getRewardTaskKey(), false);
        this.uncommittedProgress = 0;
    }

    private String getProgressTaskKey()
    {
        return "prg_" + taskKey;
    }

    public String getTaskKey()
    {
        return taskKey;
    }

    private String getRewardTaskKey()
    {
        return "rw_" + taskKey;
    }

    protected UserProfile getUserProfile()
    {
        return userProfile;
    }

    public T getTask()
    {
        return task;
    }

    public abstract String getTaskTitle();
    public abstract boolean getTaskActionMatches(String action);

    public void addProgress(int progress)
    {
        uncommittedProgress += progress;
    }

    public int getUncommittedProgress()
    {
        return uncommittedProgress;
    }

    public boolean isRewardRedeemed()
    {
        return rewardRedeemed;
    }

    public boolean isPremium()
    {
        return tasksDefinition.premium;
    }

    public void setRewardRedeemed()
    {
        this.rewardRedeemed = true;
    }

    public boolean isCompleted()
    {
        return getProgress() >= getTask().getTarget();
    }

    public interface RewardRedeemCallback
    {
        void result(boolean status);
    }

    public void redeemReward(LoginService.AccessToken userToken, RewardRedeemCallback callback)
    {
        if (activeRequest)
        {
            callback.result(false);
            return;
        }

        if (isRewardRedeemed())
        {
            callback.result(false);
            return;
        }

        if (getProgress() < getTask().getTarget())
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

        if (uncommittedProgress != 0)
        {
            commit(userToken, status ->
            {
                if (!status)
                {
                    callback.result(false);
                }

                redeemReward(userToken, callback);
            });

            return;
        }

        activeRequest = true;

        JSONObject profileUpdate = new JSONObject();

        {
            JSONObject taskReward = new JSONObject();
            profileUpdate.put(getRewardTaskKey(), taskReward);
            taskReward.put("@func", "!=");
            taskReward.put("@cond", true);
            taskReward.put("@then", true);
        }

        {
            JSONObject taskProgress = new JSONObject();
            profileUpdate.put(getProgressTaskKey(), taskProgress);
            taskProgress.put("@func", ">=");
            taskProgress.put("@cond", getTask().getTarget());
        }

        eventService.updateEventProfile(userToken, data.getEventId(), profileUpdate,
            (service, request, result, newData) -> BrainOut.getInstance().postRunnable(() ->
        {
            activeRequest = false;

            if (result != Request.Result.success)
            {
                callback.result(false);
                return;
            }

            uncommittedProgress = 0;
            committedProgress = newData.optInt(getProgressTaskKey(), 0);
            callback.result(true);
        }));
    }

    public interface ProgressCommitCallback
    {
        void result(boolean status);
    }

    protected void onCommit(JSONObject profileUpdate)
    {
        profileUpdate.put(BattlePassTask.GetTaskIDKey(taskKey), getTask().getID());
    }

    public boolean hasAnythingToCommit()
    {
        return uncommittedProgress > 0;
    }

    public void commit(LoginService.AccessToken userToken, ProgressCommitCallback callback)
    {
        if (activeRequest)
        {
            callback.result(false);
            return;
        }

        if (uncommittedProgress <= 0)
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

        activeRequest = true;

        JSONObject profileUpdate = new JSONObject();
        onCommit(profileUpdate);

        JSONObject taskProgress = new JSONObject();
        profileUpdate.put(getProgressTaskKey(), taskProgress);

        taskProgress.put("@func", "++");
        taskProgress.put("@value", uncommittedProgress);

        eventService.updateEventProfile(userToken, data.getEventId(), profileUpdate,
            (service, request, result, newData) -> BrainOut.getInstance().postRunnable(() ->
        {
            activeRequest = false;

            if (result != Request.Result.success)
            {
                callback.result(false);
                return;
            }

            uncommittedProgress = 0;
            committedProgress = newData != null ? newData.optInt(getProgressTaskKey(), 0) : 0;
            callback.result(true);
        }));
    }

    public int getCommittedProgress()
    {
        return committedProgress;
    }

    public void setCommittedProgress(int committedProgress)
    {
        this.committedProgress = committedProgress;
    }

    public void setUncommittedProgress(int uncommittedProgress)
    {
        this.uncommittedProgress = uncommittedProgress;
    }

    public int getProgress()
    {
        return uncommittedProgress + committedProgress;
    }

    public BattlePass.TasksDefinition getTasksDefinition()
    {
        return tasksDefinition;
    }
}
