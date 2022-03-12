package com.desertkun.brainout.content.battlepass;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.data.battlepass.BattlePassData;
import com.desertkun.brainout.data.battlepass.BattlePassTaskData;
import com.desertkun.brainout.online.BattlePassEvent;
import com.desertkun.brainout.online.UserProfile;
import org.json.JSONObject;

public abstract class BattlePassTask extends Content
{
    private int target;
    private int reward;
    private int validFrom;
    private int validTo;
    private int validMultiplier;

    public abstract BattlePassTaskData getData(BattlePassData data, BattlePass.TasksDefinition tasksDefinition,
        byte[] taskHash, UserProfile userProfile, JSONObject eventProfile, String taskKey);

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        target = jsonData.getInt("target");
        reward = jsonData.getInt("reward");
        validMultiplier = jsonData.getInt("validMultiplier", 1);
        validFrom = jsonData.getInt("validFrom", 0) * validMultiplier;
        validTo = jsonData.getInt("validTo", 0) * validMultiplier;
    }

    public abstract boolean validate(UserProfile userProfile);

    public boolean validateTime(BattlePassEvent event)
    {
        long startTime = event.timeStart.getTime() / 1000L;
        long currentTime = BrainOut.getInstance().getController().getCurrentTime();
        long timePassed = currentTime - startTime;

        if (validFrom != 0)
        {
            if (timePassed < validFrom)
            {
                return false;
            }
        }

        if (validTo != 0)
        {
            if (timePassed > validTo)
            {
                return false;
            }
        }

        return true;
    }

    public static String getNonce(UserProfile userProfile)
    {
        return String.valueOf(userProfile.getInt("nonce", 0));
    }

    public static String GetTaskKey(UserProfile userProfile, long day, int index)
    {

        return "t" + index + "_" + day + "_" + getNonce(userProfile);
    }

    public static String GetTaskIDKey(UserProfile userProfile, long day, int index)
    {
        return GetTaskIDKey(GetTaskKey(userProfile, day, index));
    }

    public static String GetTaskIDKey(String taskKey)
    {
        return "k" + taskKey;
    }

    public int getTarget()
    {
        return target;
    }

    public int getReward()
    {
        return reward;
    }
}
