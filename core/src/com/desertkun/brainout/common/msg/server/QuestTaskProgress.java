package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.content.quest.Quest;

public class QuestTaskProgress
{
    public String quest;
    public String task;
    public int progress;

    public QuestTaskProgress() {}

    public QuestTaskProgress(Quest quest, String task, int progress)
    {
        this.quest = quest.getID();
        this.task = task;
        this.progress = progress;
    }
}
