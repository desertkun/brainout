package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.online.Reward;
import com.desertkun.brainout.online.UserProfile;

public abstract class ContractGroup extends OwnableContent
{
    private Queue<Contract> tasks;
    protected Reward reward;

    public ContractGroup()
    {
        tasks = new Queue<>();
        reward = newReward();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        reward.read(jsonData.get("reward"), 1);
    }

    protected abstract Reward newReward();

    public Queue<Contract> getTasks()
    {
        return tasks;
    }

    public void register(Contract contract)
    {
        tasks.addLast(contract);
    }

    public boolean isComplete(UserProfile profile)
    {
        for (Contract task : tasks)
        {
            if (!task.getLockItem().isUnlocked(profile))
            {
                return false;
            }
        }

        return true;
    }

    public void startFirstGroup(UserProfile profile)
    {
        getTasks().first().getLockItem().startDiff(profile);
    }
}
