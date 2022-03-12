package com.desertkun.brainout.content;

import com.desertkun.brainout.online.Reward;
import com.desertkun.brainout.online.ServerReward;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.ContractGroup")
public class ServerContractGroup extends ContractGroup
{
    @Override
    public Reward newReward()
    {
        return new ServerReward();
    }

    public ServerReward getReward()
    {
        return ((ServerReward) reward);
    }
}
