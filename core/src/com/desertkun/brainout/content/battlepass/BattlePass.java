package com.desertkun.brainout.content.battlepass;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.data.battlepass.BattlePassData;
import com.desertkun.brainout.online.BattlePassEvent;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import org.json.JSONObject;

@Reflect("content.battlepass.BattlePass")
public class BattlePass extends OwnableContent
{
    private Array<TasksDefinition> tasksDefinitions;

    public class TasksDefinition
    {
        public TasksDefinition()
        {
            tasksPool = new Array<>();
            tasksPoolStrings = new Array<>();
        }

        public Array<BattlePassTask> tasksPool;
        public Array<String> tasksPoolStrings;
        public int phase;
        public boolean premium;

        public void read(Json json, JsonValue task)
        {
            phase = task.getInt("phase");
            premium = task.getBoolean("premium", false);

            JsonValue v = task.get("pool");
            if (v.isArray())
            {
                for (JsonValue tt : v)
                {
                    tasksPoolStrings.add(tt.asString());
                }
            }
            else
            {
                tasksPoolStrings.add(v.asString());
            }
        }

        public void completeLoad(AssetManager assetManager)
        {
            for (String id : tasksPoolStrings)
            {
                tasksPool.add(BrainOut.ContentMgr.get(id, BattlePassTask.class));
            }
        }
    }

    public BattlePass()
    {
        tasksDefinitions = new Array<>();
    }

    public BattlePassData getData(BattlePassEvent event, UserProfile userProfile, String account, JSONObject eventProfile)
    {
        return new BattlePassData(this, event, userProfile, account, eventProfile);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        for (JsonValue task : jsonData.get("tasks"))
        {
            TasksDefinition newDefinition = new TasksDefinition();
            newDefinition.read(json, task);
            tasksDefinitions.add(newDefinition);
        }
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        for (TasksDefinition tasksDefinition : tasksDefinitions)
        {
            tasksDefinition.completeLoad(assetManager);
        }
    }

    public Array<TasksDefinition> getTasksDefinitions()
    {
        return tasksDefinitions;
    }
}
