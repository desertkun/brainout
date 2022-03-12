package com.desertkun.brainout.data.battlepass;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.battlepass.BattlePass;
import com.desertkun.brainout.content.battlepass.BattlePassTask;
import com.desertkun.brainout.online.BattlePassEvent;
import com.desertkun.brainout.online.UserProfile;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class BattlePassData
{
    private final BattlePassEvent event;
    private final BattlePass battlePass;

    private Array<BattlePassTaskData> tasks;

    public BattlePassData(BattlePass battlePass, BattlePassEvent event, UserProfile userProfile, String account, JSONObject eventProfile)
    {
        this.event = event;
        this.battlePass = battlePass;
        this.tasks = new Array<>();

        Array<BattlePassTask> taskBlackList = new Array<>();

        int index = 0;

        for (BattlePass.TasksDefinition tasksDefinition : battlePass.getTasksDefinitions())
        {
            Array<BattlePassTask> validTasks = new Array<>();
            Array<BattlePassTask> timeValidTasks = new Array<>();

            for (BattlePassTask task : tasksDefinition.tasksPool)
            {
                if (taskBlackList.contains(task, true))
                {
                    continue;
                }

                if (task.validate(userProfile))
                {
                    validTasks.add(task);
                }
                else
                {
                    continue;
                }

                if (task.validateTime(event))
                {
                    timeValidTasks.add(task);
                }
            }

            if (validTasks.size == 0)
            {
                index++;
                continue;
            }

            long currentDay = getCurrentDay(tasksDefinition.phase);
            byte[] hashBytes = calculateHash(index, currentDay, account, userProfile);
            if (hashBytes == null)
                throw new RuntimeException("Well, something is wrong.");

            byte[] slice = Arrays.copyOfRange(hashBytes, 0, 2);
            byte[] taskInternalHash = Arrays.copyOfRange(hashBytes, 2, 6);

            String taskIdKey = BattlePassTask.GetTaskIDKey(userProfile, currentDay, index);

            BattlePassTask task = null;
            if (eventProfile != null && eventProfile.has(taskIdKey))
            {
                task = BrainOut.ContentMgr.get(eventProfile.getString(taskIdKey), BattlePassTask.class);
                if (task != null && !(validTasks.contains(task, true)))
                {
                    task = null;
                }
            }

            if (task == null)
            {
                int taskId = Math.abs(new BigInteger(slice).intValue());
                task = timeValidTasks.get(taskId % timeValidTasks.size);
            }

            taskBlackList.add(task);

            tasks.add(task.getData(this, tasksDefinition, taskInternalHash, userProfile, eventProfile,
                BattlePassTask.GetTaskKey(userProfile, currentDay, index)));
            index++;
        }

    }

    private long getCurrentTime()
    {
        return BrainOut.getInstance().getController().getCurrentTime();
    }

    private long getCurrentDay(long phase)
    {
        return getCurrentTime() / phase;
    }

    private byte[] calculateHash(int index, long currentDay, String account, UserProfile userProfile)
    {
        MessageDigest digest;

        try
        {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return null;
        }

        String payload = String.valueOf(index) + "_" + String.valueOf(currentDay) + "_" + account + "_" +
            BattlePassTask.getNonce(userProfile);

        return digest.digest(payload.getBytes());
    }

    public String getEventId()
    {
        return String.valueOf(event.id);
    }

    public void addPoints(int points)
    {

    }

    public Array<BattlePassTaskData> getTasks()
    {
        return tasks;
    }

    public BattlePass getBattlePass()
    {
        return battlePass;
    }
}
