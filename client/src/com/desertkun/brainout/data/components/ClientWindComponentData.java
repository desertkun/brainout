package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.ClientWindComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.Event;

public class ClientWindComponentData extends Component<ClientWindComponent>
{
    private WindComponentData wind;
    private Array<EffectData> res;
    private LaunchData launchData;

    public ClientWindComponentData(ComponentObject componentObject,
                                   ClientWindComponent contentComponent)
    {
        super(componentObject, contentComponent);
        res = new Array<>();
    }

    @Override
    public void init()
    {
        wind = getComponentObject().getComponent(WindComponentData.class);
        ActiveData activeData = ((ActiveData) getComponentObject());

        launchData = new LaunchData()
        {

            @Override
            public float getX()
            {
                float plus = Math.signum(wind.getMovement()) * 32.0f;

                return activeData.getX() - plus;
            }

            @Override
            public float getY()
            {
                return Constants.Core.CHUNK_SIZE;
            }

            @Override
            public float getAngle()
            {
                return activeData.getAngle();
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }

            @Override
            public String getDimension()
            {
                return activeData.getDimension();
            }
        };

        activate();
    }

    @Override
    public void release()
    {
        deactivate();

        wind = null;
        launchData = null;
    }

    @Override
    public void update(float dt)
    {
        Watcher watcher = Map.GetWatcher();

        if (watcher == null)
        {
            deactivate();
            return;
        }

        ActiveData activeData = ((ActiveData) getComponentObject());

        float distance = Math.abs(watcher.getWatchX() - activeData.getX());
        float requiredDistance = wind.getDistance() * getContentComponent().getExtraDistance();

        if (distance > requiredDistance)
        {
            deactivate();
            return;
        }

        ClientMap map = activeData.getMap(ClientMap.class);

        if (activeData != map.getClosestActiveForTag(
            requiredDistance, watcher.getWatchX(), activeData.getY(), ActiveData.class,
            Constants.ActiveTags.WIND, activeData1 ->
                activeData1.getComponent(WindComponentData.class) != null))
        {
            deactivate();
            return;
        }

        activate();
    }

    private void activate()
    {
        if (res.size > 0)
            return;

        getContentComponent().getEffects().launchEffects(launchData, res);
    }

    private void deactivate()
    {
        if (res.size == 0)
            return;

        ActiveData activeData = ((ActiveData) getComponentObject());
        ClientMap map = activeData.getMap(ClientMap.class);

        map.removeEffect(res);
        res.clear();
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
}
