package com.desertkun.brainout.data.battlepass;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.battlepass.BattlePass;
import com.desertkun.brainout.content.battlepass.GameModeAction;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateEndGame;
import org.json.JSONObject;

public class GameModeActionData extends BattlePassTaskData<GameModeAction>
{
    public GameModeActionData(BattlePassData data, GameModeAction task,
        BattlePass.TasksDefinition tasksDefinition, UserProfile userProfile, JSONObject eventProfile, String taskKey)
    {
        super(data, task, tasksDefinition, userProfile, eventProfile, taskKey);
    }

    @Override
    public String getTaskTitle()
    {
        StringBuilder modes = new StringBuilder();

        boolean first = true;
        for (GameMode.ID gameMode : getTask().getGameModes())
        {
            if (!first)
            {
                modes.append(", ");
            }

            first = false;

            modes.append(L.get("MODE_" + gameMode.toString().toUpperCase()));
        }

        return L.get("MENU_DO_X_IN_GAME_MODE_Y", getTask().getTitle().get(String.valueOf(getTask().getTarget())),
            modes.toString());
    }

    @Override
    public boolean getTaskActionMatches(String action)
    {
        if (!(action.equals(getTask().getAction())))
        {
            return false;
        }

        if (BrainOut.getInstance().getController().getPlayState().getID() == PlayState.ID.endgame)
        {
            GameMode.ID ll = ((PlayStateEndGame) BrainOut.getInstance().getController().getPlayState()).getLastGameMode();
            if (ll != null)
            {
                return getTask().getGameModes().contains(ll, true);
            }
        }

        return getTask().getGameModes().contains(BrainOut.getInstance().getController().getGameMode().getID(), true);
    }
}
