package com.desertkun.brainout.content.quest;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.quest.task.Task;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.online.Reward;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.LocalizedString;
import org.json.JSONException;
import org.json.JSONObject;

@Reflect("content.quest.Quest")
public class Quest extends OwnableContent implements EventReceiver
{
    private Array<Task> iter = new Array<>();

    private ObjectMap<String, Task> tasks;
    private Array<Reward> rewards;
    private Array<LocalizedString> relatedLocations;
    private Array<Content> relatedItems;
    private Array<LocalizedString> specialNotes;
    private String group;

    private boolean enabled;
    private boolean coop;
    private boolean awardPartner;
    private boolean hasToLeave;
    private boolean findLocationFirst;
    private boolean showRelatedItemsText;
    private boolean perTaskReward;
    private boolean perTargetItemReward;

    public Quest()
    {
        tasks = new ObjectMap<>();
        rewards = new Array<>();
        relatedLocations = new Array<>();
        relatedItems = new Array<>();
        specialNotes = new Array<>();
        coop = false;
        enabled = true;
        awardPartner = true;
        findLocationFirst = false;
        hasToLeave = false;
        perTaskReward = false;
        perTargetItemReward = false;
    }

    public boolean hasProgress(UserProfile userProfile, String account)
    {
        if (!enabled)
            return false;

        for (ObjectMap.Entry<String, Task> entry : tasks)
        {
            Task task = entry.value;

            if (task.getProgress(userProfile, account) > 0)
                return true;
        }

        return false;
    }

    public boolean isComplete(UserProfile userProfile, String account)
    {
        for (ObjectMap.Entry<String, Task> entry : tasks)
        {
            Task task = entry.value;

            if (!task.isComplete(userProfile, account))
                return false;
        }

        return true;
    }

    public Array<Content> getRelatedItems()
    {
        return relatedItems;
    }

    public Array<LocalizedString> getSpecialNotes()
    {
        return specialNotes;
    }

    public Array<LocalizedString> getRelatedLocations()
    {
        return relatedLocations;
    }

    public boolean hasGroup()
    {
        return group != null;
    }

    public String getGroup()
    {
        return group;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("group"))
        {
            group = jsonData.getString("group");
        }

        enabled = jsonData.getBoolean("enabled", enabled);

        if (jsonData.has("related-items"))
        {
            JsonValue relatedItems = jsonData.get("related-items");

            for (JsonValue item : relatedItems)
            {
                Content relatedItem = BrainOut.ContentMgr.get(item.asString(), Content.class);

                if (relatedItem == null)
                    continue;

                this.relatedItems.add(relatedItem);
            }
        }

        this.showRelatedItemsText = jsonData.getBoolean("show-related-items-text", true);
        this.findLocationFirst = jsonData.getBoolean("find-location-first", false);
        this.perTaskReward = jsonData.getBoolean("perTaskReward", false);
        this.perTargetItemReward = jsonData.getBoolean("perTargetItemReward", false);

        if (jsonData.has("related-locations"))
        {
            JsonValue relatedLocations = jsonData.get("related-locations");
            for (JsonValue relatedLocation : relatedLocations)
            {
                this.relatedLocations.add(new LocalizedString(relatedLocation.asString()));
            }
        }

        if (jsonData.has("special-notes"))
        {
            JsonValue specialNotes = jsonData.get("special-notes");
            for (JsonValue specialNote : specialNotes)
            {
                this.specialNotes.add(new LocalizedString(specialNote.asString()));
            }
        }

        this.coop = jsonData.getBoolean("coop", coop);
        this.awardPartner = jsonData.getBoolean("awardPartner", awardPartner);
        this.hasToLeave = jsonData.getBoolean("hasToLeave", hasToLeave);

        {
            tasks.clear();

            JsonValue tasks = jsonData.get("tasks");
            if (tasks != null && tasks.isObject())
            {
                for (JsonValue taskValue : tasks)
                {
                    String clazz = taskValue.getString("class");
                    if (clazz == null)
                        throw new RuntimeException("No class defined for task");

                    Object newTask = BrainOut.R.newInstance(clazz);
                    if (!(newTask instanceof Task))
                        throw new RuntimeException("Class " + clazz + " is not a task");

                    String taskId = getID() + "-" + taskValue.name();

                    Task task = ((Task) newTask);
                    task.setId(taskId);
                    task.setQuest(this);
                    task.read(json, taskValue);

                    this.tasks.put(taskId, task);
                }
            }
        }

        {
            rewards.clear();

            JsonValue rewards = jsonData.get("rewards");
            if (rewards != null && rewards.isArray())
            {
                for (JsonValue rewardValue : rewards)
                {
                    String text = rewardValue.toJson(JsonWriter.OutputType.json);

                    JSONObject well;

                    try
                    {
                        well = new JSONObject(text);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to parse reward");
                    }

                    Reward reward = BrainOut.getInstance().newReward();
                    reward.read(well, 1);

                    this.rewards.add(reward);
                }
            }
        }
    }

    public ObjectMap<String, Task> getTasks()
    {
        return tasks;
    }

    public Array<Reward> getRewards()
    {
        return rewards;
    }

    public boolean isCoop()
    {
        return coop;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public boolean isAwardPartner()
    {
        return awardPartner;
    }

    public boolean isHaveToLeave()
    {
        return hasToLeave;
    }

    public boolean isPerTaskReward()
    {
        return perTaskReward;
    }

    public boolean isPerTargetItemReward()
    {
        return perTargetItemReward;
    }

    public boolean isShowRelatedItemsText()
    {
        return showRelatedItemsText;
    }

    public boolean isFindLocationFirst()
    {
        return findLocationFirst;
    }

    @Override
    public boolean onEvent(Event event)
    {
        iter.clear();
        tasks.values().toArray(iter);

        for (Task task : iter)
        {
            if (task instanceof EventReceiver)
            {
                EventReceiver ev = ((EventReceiver) task);
                if (ev.onEvent(event))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public void complete(UserProfile profile)
    {
        profile.addItem(this, 1);
    }

    public boolean isTaskComplete(UserProfile profile, Task task, int target)
    {
        return profile.getStats().get(task.getId(), 0.0f) >= target;
    }

    public int getTaskProgress(UserProfile profile, Task task, int target)
    {
        return MathUtils.clamp(((int)(float) profile.getStats().get(task.getId(), 0.0f)), 0, target);
    }

    public void setTaskProgress(UserProfile profile, Task task, int progress)
    {
        profile.setInt(task.getId(), progress);
    }

    public static class TaskTriggerResult
    {
        public boolean completed;
        public int used;
    }

    public TaskTriggerResult triggerTask(UserProfile userProfile, Task task, int amount, int target)
    {
        float oldValue = userProfile.getStats().get(task.getId(), 0f);
        float newValue = userProfile.addStat(task.getId(), amount, true);
        TaskTriggerResult res = new TaskTriggerResult();
        res.used = (int)(Math.min(newValue, target) - oldValue);
        res.completed = newValue >= target;
        return res;
    }

    public boolean hasBeenCompleted(UserProfile userProfile, String account)
    {
        return hasItem(userProfile, false);
    }
}
