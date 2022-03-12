package com.desertkun.brainout.events;

public class AchievementCompletedEvent extends Event
{
    public String achievementId;

    @Override
    public ID getID()
    {
        return ID.achievementCompleted;
    }

    private Event init(String achievementId)
    {
        this.achievementId = achievementId;

        return this;
    }

    public static Event obtain(String achievementId)
    {
        AchievementCompletedEvent e = obtain(AchievementCompletedEvent.class);
        if (e == null) return null;
        return e.init(achievementId);
    }

    @Override
    public void reset()
    {
        achievementId = null;
    }
}
