package com.desertkun.brainout.content.quest;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.Tree")
public class Tree extends OwnableContent
{
    private Array<Quest> quests;

    public Tree()
    {
        quests = new Array<>();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("quests"))
        {
            JsonValue questsValue = jsonData.get("quests");

            if (questsValue.isArray())
            {
                for (JsonValue value : questsValue)
                {
                    Quest quest = BrainOut.ContentMgr.get(value.asString(), Quest.class);

                    if (quest != null)
                    {
                        this.quests.add(quest);
                    }
                }
            }
            else
            {
                Quest quest = BrainOut.ContentMgr.get(questsValue.asString(), Quest.class);

                if (quest != null)
                {
                    this.quests.add(quest);
                }
            }
        }
    }

    public int getQuestsInGroup(Quest quest)
    {
        if (!quest.hasGroup())
            return 0;

        String group = quest.getGroup();

        int index = 0;

        for (Quest q : quests)
        {
            if (q.hasGroup() && q.getGroup().equals(group))
            {
                index++;
            }
        }

        return index;
    }

    public int getQuestIndex(Quest quest)
    {
        if (!quest.hasGroup())
            return 0;

        String group = quest.getGroup();

        int index = 0;

        for (Quest q : quests)
        {
            if (q.hasGroup() && q.getGroup().equals(group))
            {
                index++;
            }

            if (q == quest)
                return index;
        }

        return 0;
    }

    public boolean isActive(UserProfile userProfile, String account)
    {
        if (userProfile == null)
            return false;

        if (isLocked(userProfile))
            return false;

        for (Quest quest : quests)
        {
            if (quest instanceof DailyQuest)
                return true;

            if (quest.hasBeenCompleted(userProfile, account))
                continue;

            return quest.isEnabled();
        }

        return false;
    }

    public Quest getCurrentQuest(UserProfile userProfile, String account)
    {
        for (Quest quest : quests)
        {
            if (quest instanceof DailyQuest)
                return quest;

            if (quest.hasBeenCompleted(userProfile, account))
                continue;

            if (!quest.isEnabled())
                return null;

            return quest;
        }

        return null;
    }

    public Array<Quest> getQuests()
    {
        return quests;
    }
}
