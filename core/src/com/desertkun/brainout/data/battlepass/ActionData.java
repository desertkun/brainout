package com.desertkun.brainout.data.battlepass;

import com.desertkun.brainout.content.battlepass.Action;
import com.desertkun.brainout.content.battlepass.BattlePass;
import com.desertkun.brainout.online.UserProfile;
import org.json.JSONObject;

public class ActionData extends BattlePassTaskData<Action>
{
    public ActionData(BattlePassData data, Action task,
                      BattlePass.TasksDefinition tasksDefinition, UserProfile userProfile, JSONObject eventProfile, String taskKey)
    {
        super(data, task, tasksDefinition, userProfile, eventProfile, taskKey);
    }

    @Override
    public String getTaskTitle()
    {
        return getTask().getTitle().get(String.valueOf(getTask().getTarget()));
    }

    @Override
    public boolean getTaskActionMatches(String action)
    {
        return action.equals(getTask().getAction());
    }
}
