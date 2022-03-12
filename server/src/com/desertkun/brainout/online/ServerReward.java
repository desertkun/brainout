package com.desertkun.brainout.online;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import org.json.JSONObject;

public class ServerReward extends Reward
{
    public interface ServerAction
    {
        void apply(PlayerClient playerClient, boolean notify);
    }

    public class UnlockServerAction extends UnlockAction implements ServerAction
    {
        public UnlockServerAction(JSONObject data)
        {
            super(data);
        }

        public UnlockServerAction(JsonValue data)
        {
            super(data);
        }

        @Override
        public void apply(PlayerClient playerClient, boolean notify)
        {
            int add = this.amount * multiply;
            int have = playerClient.getProfile().getItems().get(id.getID(), 0);

            if (max >= 0)
            {
                if (have + add > max)
                {
                    add = Math.max(max - have, 0);
                }
            }

            playerClient.gotOwnable(id, "event", ClientProfile.OnwAction.owned, add, notify);
            playerClient.sendUserProfile();
        }
    }

    public class UnlockStatServerAction extends UnlockStatAction implements ServerAction
    {
        public UnlockStatServerAction(JSONObject data)
        {
            super(data);
        }

        public UnlockStatServerAction(JsonValue data)
        {
            super(data);
        }

        @Override
        public void apply(PlayerClient playerClient, boolean notify)
        {
            float add = this.amount * multiply;
            float have = playerClient.getProfile().getStats().get(id.getStat(), 0.0f);

            if (max >= 0)
            {
                if (have + add > max)
                {
                    add = Math.max(max - have, 0);
                }
            }

            playerClient.addStat(id.getStat(), add);
            playerClient.sendUserProfile();
        }
    }

    public class AddStatServerAction extends AddStatAction implements ServerAction
    {
        public AddStatServerAction(JSONObject data)
        {
            super(data);
        }

        public AddStatServerAction(JsonValue data)
        {
            super(data);
        }

        @Override
        public void apply(PlayerClient playerClient, boolean notify)
        {
            float add = this.amount * multiply;
            float have = playerClient.getProfile().getStats().get(id, 0.0f);

            if (max >= 0)
            {
                if (have + add > max)
                {
                    add = Math.max(max - have, 0);
                }
            }

            switch (id)
            {
                case Constants.User.NUCLEAR_MATERIAL:
                {
                    playerClient.notify(NotifyAward.nuclearMaterial, add,
                        NotifyReason.nuclearMaterialReceived, NotifyMethod.message, null);

                    break;
                }
                case Constants.User.SKILLPOINTS:
                {
                    playerClient.notify(NotifyAward.skillpoints, add,
                            NotifyReason.skillPointsEarned, NotifyMethod.message, null);

                    break;
                }
                case "ch":
                {
                    playerClient.notify(NotifyAward.ch, add,
                        NotifyReason.chEarned, NotifyMethod.message, null);

                    break;
                }
                case "ru":
                {
                    playerClient.notify(NotifyAward.ru, add,
                        NotifyReason.ruEarned, NotifyMethod.message, null);

                    break;
                }
                case "gears":
                {
                    playerClient.notify(NotifyAward.gears, add,
                        NotifyReason.gearsEarned, NotifyMethod.message, null);

                    break;
                }
            }

            playerClient.addStat(id, add);
            playerClient.sendUserProfile();
        }
    }

    public class AddBattlePointsServerAction extends AddBattlePointsAction implements ServerAction
    {
        public AddBattlePointsServerAction(JSONObject data)
        {
            super(data);
        }

        public AddBattlePointsServerAction(JsonValue data)
        {
            super(data);
        }

        @Override
        public void apply(PlayerClient playerClient, boolean notify)
        {
            float add = this.amount * multiply;

            playerClient.updateEvents(() ->
            {
                ServerBattlePassEvent ev = null;

                for (ObjectMap.Entry<Integer, ServerEvent> event : playerClient.getOnlineEvents())
                {
                    if (!(event.value instanceof ServerBattlePassEvent))
                    {
                        continue;
                    }

                    ev = ((ServerBattlePassEvent) event.value);
                    break;
                }

                if (ev == null)
                    return;

                ServerBattlePassEvent f = ev;

                BrainOutServer.PostRunnable(() -> f.addScore(add));
            });
        }
    }

    @Override
    protected Action newAction(ActionType actionType, JSONObject data)
    {
        switch (actionType)
        {
            case unlock:
            {
                return new UnlockServerAction(data);
            }
            case unlockStat:
            {
                return new UnlockStatServerAction(data);
            }
            case addstat:
            {
                return new AddStatServerAction(data);
            }
            case addbattlepoints:
            {
                return new AddBattlePointsServerAction(data);
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    protected Action newAction(ActionType actionType, JsonValue data)
    {
        switch (actionType)
        {
            case unlock:
            {
                return new UnlockServerAction(data);
            }
            case unlockStat:
            {
                return new UnlockStatServerAction(data);
            }
            case addstat:
            {
                return new AddStatServerAction(data);
            }
            case addbattlepoints:
            {
                return new AddBattlePointsServerAction(data);
            }
            default:
            {
                return null;
            }
        }
    }

    public boolean apply(PlayerClient playerClient)
    {
        return apply(playerClient, true);
    }

    public boolean apply(PlayerClient playerClient, boolean notify)
    {
        if (action != null)
        {
            BrainOutServer.PostRunnable(() -> ((ServerAction) action).apply(playerClient, notify));
            return true;
        }

        return false;
    }
}
