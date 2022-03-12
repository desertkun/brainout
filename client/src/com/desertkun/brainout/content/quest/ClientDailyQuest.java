package com.desertkun.brainout.content.quest;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.DailyQuest")
public class ClientDailyQuest extends DailyQuest
{
    public ClientDailyQuest()
    {
    }

    @Override
    protected long getTime()
    {
        return BrainOutClient.ClientController.getServerTime();
    }
}
