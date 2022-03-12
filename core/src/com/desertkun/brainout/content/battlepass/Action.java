package com.desertkun.brainout.content.battlepass;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.battlepass.ActionData;
import com.desertkun.brainout.data.battlepass.BattlePassData;
import com.desertkun.brainout.data.battlepass.BattlePassTaskData;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.utils.LocalizedString;
import org.json.JSONObject;

@Reflect("content.battlepass.Action")
public class Action extends BattlePassTask
{
    private String action;
    private LocalizedString title;

    public Action()
    {
    }

    @Override
    public BattlePassTaskData getData(BattlePassData data, BattlePass.TasksDefinition tasksDefinition,
          byte[] taskHash, UserProfile userProfile, JSONObject eventProfile, String taskKey)
    {
        return new ActionData(data, this, tasksDefinition, userProfile, eventProfile, taskKey);
    }

    @Override
    public boolean validate(UserProfile userProfile)
    {
        return true;
    }

    @Override
    public LocalizedString getTitle()
    {
        return title;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        action = jsonData.getString("action");
        title = new LocalizedString(jsonData.getString("title"));
    }

    public String getAction()
    {
        return action;
    }
}
