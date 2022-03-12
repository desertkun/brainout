package com.desertkun.brainout.online;

import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.consumable.ConsumableToStatContent;
import com.esotericsoftware.minlog.Log;
import org.json.JSONObject;

public class Reward
{
    protected Action action;

    public enum ActionType
    {
        unlock,
        unlockStat,
        addstat,
        addbattlepoints
    }

    public abstract class Action
    {
        protected int multiply;
        public Action(JSONObject data) {}
        public Action(JsonValue data) {}

        public abstract int getAmount();

        public void setMultiply(int multiply)
        {
            this.multiply = multiply;
        }
    }

    public class UnlockAction extends Action
    {
        public final OwnableContent id;
        public final int amount;
        public final int max;

        public UnlockAction(JSONObject data)
        {
            super(data);

            Content content = BrainOut.ContentMgr.get(data.optString("id"));

            if (!(content instanceof OwnableContent))
            {
                throw new RuntimeException("Not an ownable");
            }

            this.id = ((OwnableContent) content);
            this.amount = data.optInt("amount", 1);
            this.max = data.optInt("max", -1);
        }

        public UnlockAction(JsonValue data)
        {
            super(data);

            Content content = BrainOut.ContentMgr.get(data.getString("id", null));

            if (!(content instanceof OwnableContent))
            {
                throw new RuntimeException("Not an ownable");
            }

            this.id = ((OwnableContent) content);
            this.amount = data.getInt("amount", 1);
            this.max = data.getInt("max", -1);
        }

        @Override
        public int getAmount()
        {
            return amount;
        }

        public OwnableContent getContent()
        {
            return id;
        }

        @Override
        public String toString()
        {
            return "Unlock {" + id.getID() + "} count " + amount + " max " + max;
        }
    }

    public class UnlockStatAction extends Action
    {
        public final ConsumableToStatContent id;
        public final int amount;
        public final int max;

        public UnlockStatAction(JSONObject data)
        {
            super(data);

            Content content = BrainOut.ContentMgr.get(data.optString("id"));

            if (!(content instanceof ConsumableToStatContent))
            {
                throw new RuntimeException("Not an ConsumableToStatContent");
            }

            this.id = ((ConsumableToStatContent) content);
            this.amount = data.optInt("amount", 1);
            this.max = data.optInt("max", -1);
        }

        public UnlockStatAction(JsonValue data)
        {
            super(data);

            Content content = BrainOut.ContentMgr.get(data.getString("id", null));

            if (!(content instanceof ConsumableToStatContent))
            {
                throw new RuntimeException("Not an ConsumableToStatContent");
            }

            this.id = ((ConsumableToStatContent) content);
            this.amount = data.getInt("amount", 1);
            this.max = data.getInt("max", -1);
        }

        public ConsumableToStatContent getId()
        {
            return id;
        }

        @Override
        public int getAmount()
        {
            return amount;
        }

        @Override
        public String toString()
        {
            return "Unlock {" + id.getID() + "} count " + amount + " max " + max;
        }
    }

    public class AddStatAction extends Action
    {
        public final String id;
        public final int amount;
        public final int max;

        public AddStatAction(JSONObject data)
        {
            super(data);

            this.id = data.optString("id");
            this.amount = data.optInt("amount", 1);
            this.max = data.optInt("max", -1);
        }

        public AddStatAction(JsonValue data)
        {
            super(data);

            this.id = data.getString("id");
            this.amount = data.getInt("amount", 1);
            this.max = data.getInt("max", -1);
        }

        public String getStat()
        {
            return id;
        }

        @Override
        public int getAmount()
        {
            return amount;
        }

        @Override
        public String toString()
        {
            return "AddStat {" + id + "} count " + amount + " max " + max;
        }
    }

    public class AddBattlePointsAction extends Action
    {
        public final String id;
        public final int amount;

        public AddBattlePointsAction(JSONObject data)
        {
            super(data);

            this.id = data.optString("id");
            this.amount = data.optInt("amount", 1);
        }

        public AddBattlePointsAction(JsonValue data)
        {
            super(data);

            this.id = data.getString("id");
            this.amount = data.getInt("amount", 1);
        }

        public String getStat()
        {
            return id;
        }

        @Override
        public int getAmount()
        {
            return amount;
        }

        @Override
        public String toString()
        {
            return "AddBattlePointsAction {" + id + "} count " + amount;
        }
    }

    public Reward()
    {
        this.action = null;
    }

    public boolean read(JSONObject data, int multiplyAmount)
    {
        String actionType = data.optString("action");

        if (actionType == null)
            return false;

        Action promoAction;

        try
        {
            promoAction = newAction(actionType, data);
        }
        catch (RuntimeException ignored)
        {
            if (Log.ERROR) Log.error(ignored.getMessage());
            return false;
        }

        if (promoAction != null)
        {
            promoAction.setMultiply(multiplyAmount);
            this.action = promoAction;
        }

        return true;
    }

    public boolean read(JsonValue data, int multiplyAmount)
    {
        String actionType = data.getString("action");

        if (actionType == null)
            return false;

        Action promoAction;

        try
        {
            promoAction = newAction(actionType, data);
        }
        catch (RuntimeException ignored)
        {
            if (Log.ERROR) Log.error(ignored.getMessage());
            return false;
        }

        if (promoAction != null)
        {
            promoAction.setMultiply(multiplyAmount);
            this.action = promoAction;
        }

        return true;
    }


    private Action newAction(String actionType, JSONObject data)
    {
        ActionType action;

        try
        {
            action = ActionType.valueOf(actionType);
        }
        catch (IllegalArgumentException ignored)
        {
            return null;
        }

        return newAction(action, data);
    }

    private Action newAction(String actionType, JsonValue data)
    {
        ActionType action;

        try
        {
            action = ActionType.valueOf(actionType);
        }
        catch (IllegalArgumentException ignored)
        {
            return null;
        }

        return newAction(action, data);
    }

    protected Action newAction(ActionType actionType, JSONObject data)
    {
        switch (actionType)
        {
            case unlock:
            {
                return new UnlockAction(data);
            }
            case addstat:
            {
                return new AddStatAction(data);
            }
            case addbattlepoints:
            {
                return new AddBattlePointsAction(data);
            }
            default:
            {
                return null;
            }
        }
    }

    protected Action newAction(ActionType actionType, JsonValue data)
    {
        switch (actionType)
        {
            case unlock:
            {
                return new UnlockAction(data);
            }
            case addstat:
            {
                return new AddStatAction(data);
            }
            case addbattlepoints:
            {
                return new AddBattlePointsAction(data);
            }
            default:
            {
                return null;
            }
        }
    }

    public Action getAction()
    {
        return action;
    }

    @Override
    public String toString()
    {
        return "Reward: " + (action != null ? action.toString() : "<no action>");
    }
}
