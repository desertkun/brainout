package com.desertkun.brainout.online;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.utils.ContentImage;
import org.json.JSONObject;

public class ClientReward extends Reward
{
    private ClientAction clientAction;

    public interface ClientAction
    {
        Actor render(Table data);
        String getLocalizedTitle();
    }

    public class UnlockClientAction extends UnlockAction implements ClientAction
    {
        public UnlockClientAction(JSONObject data)
        {
            super(data);
        }

        public UnlockClientAction(JsonValue data)
        {
            super(data);
        }

        @Override
        public Actor render(Table data)
        {
            return ContentImage.RenderImage(id, data, getAmount());
        }

        @Override
        public String getLocalizedTitle()
        {
            return getContent().getTitle().get();
        }
    }

    public class UnlockStatClientAction extends UnlockStatAction implements ClientAction
    {
        public UnlockStatClientAction(JSONObject data)
        {
            super(data);
        }

        public UnlockStatClientAction(JsonValue data)
        {
            super(data);
        }

        @Override
        public Actor render(Table data)
        {
            return ContentImage.RenderImage(id, data, getAmount());
        }

        @Override
        public String getLocalizedTitle()
        {
            return getId().getTitle().get();
        }
    }

    public class AddStatClientAction extends AddStatAction implements ClientAction
    {
        public AddStatClientAction(JSONObject data)
        {
            super(data);
        }

        public AddStatClientAction(JsonValue data)
        {
            super(data);
        }

        @Override
        public Actor render(Table data)
        {
            return ContentImage.RenderStatImage(id, amount, data);
        }

        @Override
        public String getLocalizedTitle()
        {
            switch (getStat())
            {
                case Constants.User.GEARS:
                {
                    return L.get("CARD_GEARS");
                }
                case Constants.User.NUCLEAR_MATERIAL:
                {
                    return L.get("CARD_NUCLEAR_MATERIAL");
                }
                case Constants.User.SCORE:
                {
                    return L.get("CARD_SCORE");
                }
                case Constants.User.SKILLPOINTS:
                {
                    return L.get("CARD_SKILLPOINTS");
                }
                case Constants.User.TECH_SCORE:
                {
                    return L.get("CARD_TECH_SCORE");
                }
                default:
                {
                    return getStat();
                }
            }
        }
    }

    public class AddBattlePointsClientAction extends AddBattlePointsAction implements ClientAction
    {
        public AddBattlePointsClientAction(JSONObject data)
        {
            super(data);
        }

        public AddBattlePointsClientAction(JsonValue data)
        {
            super(data);
        }

        @Override
        public Actor render(Table data)
        {
            return ContentImage.RenderStatImage(id, amount, data);
        }

        @Override
        public String getLocalizedTitle()
        {
            return L.get("MENU_BATTLEPOINTS");
        }
    }

    private void setClientAction(ClientAction clientAction)
    {
        this.clientAction = clientAction;
    }

    @Override
    protected Action newAction(ActionType actionType, JSONObject data)
    {
        switch (actionType)
        {
            case unlock:
            {
                UnlockClientAction uca = new UnlockClientAction(data);
                setClientAction(uca);
                return uca;
            }
            case unlockStat:
            {
                UnlockStatClientAction uca = new UnlockStatClientAction(data);
                setClientAction(uca);
                return uca;
            }
            case addstat:
            {
                AddStatClientAction uca = new AddStatClientAction(data);
                setClientAction(uca);
                return uca;
            }
            case addbattlepoints:
            {
                AddBattlePointsClientAction uca = new AddBattlePointsClientAction(data);
                setClientAction(uca);
                return uca;
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
                UnlockClientAction uca = new UnlockClientAction(data);
                setClientAction(uca);
                return uca;
            }
            case unlockStat:
            {
                UnlockStatClientAction uca = new UnlockStatClientAction(data);
                setClientAction(uca);
                return uca;
            }
            case addstat:
            {
                AddStatClientAction uca = new AddStatClientAction(data);
                setClientAction(uca);
                return uca;
            }
            default:
            {
                return null;
            }
        }
    }

    public ClientAction getClientAction()
    {
        return clientAction;
    }
}
