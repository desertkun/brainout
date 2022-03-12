package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.GeigerComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("GeigerComponent")
@ReflectAlias("data.components.GeigerComponentData")
public class GeigerComponentData extends Component<GeigerComponent>
{
    private static PointLaunchData fakeData = new PointLaunchData(0, 0, 0, null);
    private final ActiveData activeData;
    private float counter = 0;

    public GeigerComponentData(ActiveData activeData,
                               GeigerComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;
        this.counter = 0;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        counter -= dt;

        if (counter < 0)
        {
            ClientMap map = ((ClientMap) getMap());

            if (map != null)
            {
                Watcher watcher = Map.GetWatcher();

                if (watcher != null && watcher.getDimension().equals(getComponentObject().getDimension()))
                {
                    float myX = activeData.getX(), myY = activeData.getY();
                    float targetX = watcher.getWatchX(), targetY = watcher.getWatchY();

                    fakeData.setPosition(targetX, targetY);

                    float distance = Vector2.dst(myX, myY, targetX, targetY);

                    float frequency = distance > 0 ?
                            getContentComponent().getHighestFrequency()
                                / (1.0f + distance / (getContentComponent().getDistanceDivider())) :
                            getContentComponent().getHighestFrequency();

                    float fq = frequency > 0 ? 60 / (frequency * MathUtils.random(0.5f, 1.5f)) : 0.1f;

                    while (counter < 0)
                    {
                        counter =+ fq;
                        trigger();
                    }
                }
            }
        }


    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    private void trigger()
    {
        getContentComponent().getSounds().launchEffects(fakeData);
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
