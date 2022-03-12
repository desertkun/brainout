package com.desertkun.brainout.common.msg.server;

public class AchievementCompletedMsg
{
    public String achievementId;

    public AchievementCompletedMsg() {}
    public AchievementCompletedMsg(String achievementId)
    {
        this.achievementId = achievementId;
    }
}
