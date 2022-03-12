package com.desertkun.brainout.content.battlepass;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.battlepass.BattlePassData;
import com.desertkun.brainout.data.battlepass.BattlePassTaskData;
import com.desertkun.brainout.data.battlepass.GameModeActionData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.utils.LocalizedString;
import org.json.JSONObject;

@Reflect("content.battlepass.GameModeAction")
public class GameModeAction extends BattlePassTask
{
    private Array<GameMode.ID> gameModes;
    private String action;
    private LocalizedString title;

    public GameModeAction()
    {
        gameModes = new Array<>();
    }

    @Override
    public BattlePassTaskData getData(BattlePassData data, BattlePass.TasksDefinition tasksDefinition,
          byte[] taskHash, UserProfile userProfile, JSONObject eventProfile, String taskKey)
    {
        return new GameModeActionData(data, this, tasksDefinition, userProfile, eventProfile, taskKey);
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

        JsonValue gm = jsonData.get("game-mode");
        if (gm.isArray())
        {
            for (JsonValue value : gm)
            {
                gameModes.add(GameMode.ID.valueOf(value.asString()));
            }
        }
        else
        {
            gameModes.add(GameMode.ID.valueOf(gm.asString()));
        }
    }

    public Array<GameMode.ID> getGameModes()
    {
        return gameModes;
    }

    public String getAction()
    {
        return action;
    }
}
