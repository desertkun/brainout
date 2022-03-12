package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.PlayerBoostersComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.TimerTask;

@Reflect("plboostc")
@ReflectAlias("data.components.PlayerBoostersComponentData")
public class PlayerBoostersComponentData extends Component<PlayerBoostersComponent> implements Json.Serializable
{
    public class Booster implements Json.Serializable
    {
        public float value;
        public long endTime;
        public TimerTask task;

        public boolean valid()
        {
            return System.currentTimeMillis() <= endTime;
        }

        public long getSecondsLeft()
        {
            return (Math.max(endTime - System.currentTimeMillis(), 0) / 1000L);
        }

        @Override
        public void write(Json json)
        {
            long timeLeft = endTime - System.currentTimeMillis();

            json.writeValue("v", value);
            json.writeValue("t", timeLeft);
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            endTime = System.currentTimeMillis() + jsonData.getLong("t");
            value = jsonData.getFloat("v");
        }
    }

    private ObjectMap<String, Booster> boosters;

    public PlayerBoostersComponentData(ActiveData activeData,
                                       PlayerBoostersComponent contentComponent)
    {
        super(activeData, contentComponent);

        boosters = new ObjectMap<>();
    }

    public boolean hasBooster(String key)
    {
        return this.boosters.containsKey(key);
    }

    public boolean hasAnyBooster()
    {
        for (ObjectMap.Entry<String, Booster> entry : this.boosters)
        {
            if (entry.value.valid())
                return true;
        }

        return false;
    }

    public void removeBooster(String key)
    {
        Booster bst = this.boosters.remove(key);

        if (bst != null && bst.task != null)
        {
            bst.task.cancel();
            updated(((ActiveData) getComponentObject()));
        }
    }

    public void setBooster(String key, float value, float time)
    {
        long time_ = (long)(time * 1000L);

        Booster bst = new Booster();

        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                bst.task = null;
                BrainOut.getInstance().postRunnable(() -> removeBooster(key));
            }
        };

        bst.value = value;
        bst.endTime = System.currentTimeMillis() + time_;
        bst.task = task;

        this.boosters.put(key, bst);
        updated(((ActiveData) getComponentObject()));

        BrainOut.Timer.schedule(task, time_);
    }

    public Booster getBooster(String key)
    {
        Booster bst = boosters.get(key);

        if (bst != null && !bst.valid())
            return null;

        return bst;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void write(Json json)
    {
        json.writeObjectStart("bst");
        for (ObjectMap.Entry<String, Booster> entry : boosters)
        {
            json.writeValue(entry.key, entry.value);
        }
        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        boosters.clear();

        if (jsonData.has("bst"))
        {
            for (JsonValue value : jsonData.get("bst"))
            {
                Booster bst = new Booster();
                bst.read(json, value);
                boosters.put(value.name(), bst);
            }
        }
    }
}
