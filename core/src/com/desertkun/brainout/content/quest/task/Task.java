package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.LocalizedString;

import java.lang.ref.WeakReference;

public abstract class Task implements Serializable
{
    private String id;
    private int target;
    private LocalizedString title;
    private WeakReference<Quest> quest;

    public Task()
    {
        target = 1;
        title = new LocalizedString();
        quest = null;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public int getProgress(UserProfile userProfile, String account)
    {
        Quest quest = getQuest();

        if (quest != null)
        {
            return quest.getTaskProgress(userProfile, this, getTarget(account));
        }

        return MathUtils.clamp(((int)(float) userProfile.getStats().get(id, 0.0f)), 0, getTarget(account));
    }

    public void setProgress(UserProfile userProfile, int progress)
    {
        Quest quest = getQuest();

        if (quest != null)
        {
            quest.setTaskProgress(userProfile, this, progress);
        }
    }

    public boolean isComplete(UserProfile userProfile, String account)
    {
        if (BrainOut.OnlineEnabled())
        {
            if (userProfile == null || account == null || account.isEmpty())
                return false;
        }

        Quest quest = getQuest();

        if (quest != null)
        {
            return quest.isTaskComplete(userProfile, this, getTarget(account));
        }

        return userProfile.getStats().get(id, 0.0f) >= getTarget(account);
    }

    public void setQuest(Quest quest)
    {
        this.quest = new WeakReference<>(quest);
    }

    public Quest getQuest()
    {
        if (quest == null)
            return null;

        return quest.get();
    }

    public Quest.TaskTriggerResult trigger(UserProfile userProfile, int amount, String account)
    {
        Quest quest = getQuest();

        if (quest != null)
        {
            return quest.triggerTask(userProfile, this, amount, getTarget(account));
        }

        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    protected abstract void readTask(JsonValue jsonData);

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        target = jsonData.getInt("target", target);

        if (jsonData.has("title"))
        {
            title.set(jsonData.getString("title"));
        }

        readTask(jsonData);
    }

    public int getTarget(String account)
    {
        return target;
    }

    public LocalizedString getTitle()
    {
        return title;
    }
}
