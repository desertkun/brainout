package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.components.ClientFreeplayCricketComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientFreeplayCricketComponent")
@ReflectAlias("data.components.ClientFreeplayCricketComponentData")
public class ClientFreeplayCricketComponentData extends Component<ClientFreeplayCricketComponent>
{
    private final LaunchData launchData;
    private float check;
    private float interval;
    private boolean enabled;
    private int burst;
    private boolean playing;

    public ClientFreeplayCricketComponentData(ComponentObject componentObject,
                                              ClientFreeplayCricketComponent contentComponent)
    {
        super(componentObject, contentComponent);

        this.launchData = new LaunchData()
        {
            @Override
            public float getX()
            {
                return ((ActiveData) getComponentObject()).getX();
            }

            @Override
            public float getY()
            {
                return ((ActiveData) getComponentObject()).getY();
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }

            @Override
            public String getDimension()
            {
                return getComponentObject().getDimension();
            }
        };

        if (contentComponent.isPeriodic())
        {
            burst = contentComponent.getCycles();
        }
    }

    @Override
    public void init()
    {
        super.init();
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public void update(float dt)
    {
        check -= dt;
        if (check < 0)
        {
            check();
            check = 5.0f;
        }

        if (enabled)
        {
            interval -= dt;
            if (interval < 0)
            {
                if (playing)
                {
                    playOnce();

                    burst--;

                    if (burst <= 0)
                    {
                        playing = false;

                        if (getContentComponent().isPeriodic())
                        {
                            burst = getContentComponent().getCycles() + MathUtils.random(4);
                            interval = getContentComponent().getDelayAfterPeriod();
                        }
                        else
                        {
                            burst = MathUtils.random(2, 10);
                            interval = MathUtils.random(5.f, 10.f);
                        }
                    }
                    else
                    {
                        interval = getContentComponent().getInterval();
                    }
                }
                else
                {
                    playing = true;
                }
            }
        }
    }

    private void playOnce()
    {
        Map.Get(getComponentObject().getDimension(), ClientMap.class).addEffect(
            getContentComponent().getEffects().random(), launchData
        );
    }

    private void check()
    {
        PlayerData playerData = BrainOutClient.ClientController.getState(CSGame.class).getPlayerData();
        enabled = playerData != null && Math.abs(launchData.getX() - playerData.getX()) > 32;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
