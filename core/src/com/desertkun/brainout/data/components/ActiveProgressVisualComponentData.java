package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.ActiveProgressComponent;
import com.desertkun.brainout.content.components.ActiveProgressVisualComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.ComponentUpdatedEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("activprogviscom")
@ReflectAlias("data.components.ActiveProgressVisualComponentData")
public class ActiveProgressVisualComponentData extends Component<ActiveProgressVisualComponent>
    implements Json.Serializable, ComponentUpdatedEvent.Predicate
{
    private long startTime;
    private long endTime;
    private boolean cancellable;
    private boolean active;

    public ActiveProgressVisualComponentData(ActiveData activeData,
                                             ActiveProgressVisualComponent contentComponent)
    {
        super(activeData, contentComponent);

        cleanUp();
    }

    public long getStartTime()
    {
        return startTime;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void start(long startTime, long endTime, boolean cancellable)
    {
        this.active = true;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cancellable = cancellable;

        updated(((ActiveData) getComponentObject()), this);
    }

    public void stop()
    {
        this.active = false;

        updated(((ActiveData) getComponentObject()), this);
    }

    public boolean isActive()
    {
        return active;
    }

    public boolean isCancellable()
    {
        return cancellable;
    }

    private void cleanUp()
    {
        startTime = 0;
        endTime = 0;
        cancellable = false;
        active = false;
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
        long totalTime = endTime - startTime;
        long passed = System.currentTimeMillis() - startTime;

        if (passed < totalTime && active)
        {
            json.writeValue("total", totalTime);
            json.writeValue("passed", passed);
            json.writeValue("c", cancellable);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("total"))
        {
            this.active = true;

            long now = System.currentTimeMillis();
            long total = jsonData.getLong("total");
            long passed = jsonData.getLong("passed", 0);

            this.cancellable = jsonData.getBoolean("c", true);
            this.endTime = now + total;
            this.startTime = now - passed;
        }
        else
        {
            cleanUp();
        }
    }

    @Override
    public boolean check(int owner)
    {
        int me = ((ActiveData) getComponentObject()).getOwnerId();

        return me == owner || !BrainOut.getInstance().getController().isEnemies(me, owner);
    }
}
