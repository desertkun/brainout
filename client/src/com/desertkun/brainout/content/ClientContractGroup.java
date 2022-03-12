package com.desertkun.brainout.content;

import com.desertkun.brainout.online.ClientReward;
import com.desertkun.brainout.online.Reward;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.ContractGroup")
public class ClientContractGroup extends ContractGroup
{
    @Override
    public Reward newReward()
    {
        return new ClientReward();
    }

    public ClientReward getReward()
    {
        return ((ClientReward) reward);
    }
}
