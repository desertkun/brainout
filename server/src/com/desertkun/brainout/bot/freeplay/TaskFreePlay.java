package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntSet;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskFollowAndShootTarget;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.utils.RandomValue;

public class TaskFreePlay extends Task
{
    private final BotClient client;
    private float timer = 0;
    private IntSet weaponBlackList;
    private boolean firstTime;

    public TaskFreePlay(TaskStack stack, BotClient client)
    {
        super(stack);

        this.client = client;
        this.weaponBlackList = new IntSet();
        this.firstTime = true;
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.1f;

        PlayerData playerData = client.getPlayerData();
        if (playerData == null)
            return;

        Map map = playerData.getMap();
        if (map == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        if (FindEssentials.EnsureEssentials(this, weaponBlackList))
            return;

        if (firstTime)
        {
            firstTime = false;
            pushTask(new TaskHide(getStack(), weaponBlackList));
            return;
        }

        switch (MathUtils.random(2))
        {
            case 0:
            {
                pushTask(new TaskHide(getStack(), weaponBlackList));
                break;
            }
            default:
            {
                pushTask(new HuntAround(getStack(), weaponBlackList));
            }
        }
    }

    @Override
    public void gotShotFrom(ActiveData enemy)
    {
        pushTask(new TaskFollowAndShootTarget(getStack(), enemy,
            new RandomValue(0.1f, 2.0f), new RandomValue(0.4f, 0.8f)));
    }

}
