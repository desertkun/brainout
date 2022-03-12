package com.desertkun.brainout.online;

import org.anthillplatform.runtime.services.EventService;

public interface ServerEvent
{
    Event getEvent();

    boolean isKeep();
    void setKeep(boolean keep);
    void parse(EventService.Event event);
    void store();
    void statAdded(String stat, float amount);

    interface ClaimResult
    {
        void done(boolean success);
    }

    void claim(int rewardIndex, ClaimResult claimResult);
}
