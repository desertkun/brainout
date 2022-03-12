package com.desertkun.brainout.content.quest;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.quest.task.Task;
import com.desertkun.brainout.online.UserProfile;

public class DailyQuest extends Quest
{
    private long cycle;

    public DailyQuest()
    {
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        cycle = jsonData.getLong("cycle", 86400L);
    }

    public long getCycle()
    {
        return cycle;
    }

    protected long getTime()
    {
        return System.currentTimeMillis() / 1000L;
    }

    private long getCurrentDay()
    {
        return getTime() / cycle;
    }

    public boolean isQuestDoneForToday(UserProfile profile)
    {
        long current = getCurrentDay();
        return profile.getStats().get(getID() + "-time", 0f) == current;
    }

    public long getTimeToNextQuest(UserProfile profile)
    {
        long nextDay = getCurrentDay() + 1;
        long nextTime = nextDay * cycle;
        long now = getTime();

        return nextTime - now;
    }

    @Override
    public void complete(UserProfile profile)
    {
        profile.setStat(getID() + "-time", getCurrentDay());
        profile.addStat("daily-quest-complete", 1, true);
    }

    @Override
    public boolean isComplete(UserProfile userProfile, String account)
    {
        return super.isComplete(userProfile, account);
    }

    @Override
    public boolean isTaskComplete(UserProfile profile, Task task, int target)
    {
        long current = getCurrentDay();

        if (profile.getStats().get(task.getId() + "-time", (float)current).longValue() != current)
        {
            return false;
        }

        return super.isTaskComplete(profile, task, target);
    }

    @Override
    public int getTaskProgress(UserProfile profile, Task task, int target)
    {
        long current = getCurrentDay();

        if (profile.getStats().get(task.getId() + "-time", (float)current).longValue() != current)
        {
            return 0;
        }

        return super.getTaskProgress(profile, task, target);
    }

    public void setTaskProgress(UserProfile profile, Task task, int progress)
    {
        super.setTaskProgress(profile, task, progress);

        long current = getCurrentDay();
        profile.setInt(task.getId() + "-time", (int)current);
    }

    @Override
    public boolean hasBeenCompleted(UserProfile userProfile, String account)
    {
        return isQuestDoneForToday(userProfile);
    }

    @Override
    public TaskTriggerResult triggerTask(UserProfile profile, Task task, int amount, int target)
    {
        long current = getCurrentDay();

        if (profile.getStats().get(task.getId() + "-time", 0.f).longValue() != current)
        {
            // reset
            profile.setStat(task.getId() + "-time", current);
            profile.setStat(task.getId(), 0);
        }

        return super.triggerTask(profile, task, amount, target);
    }
}
