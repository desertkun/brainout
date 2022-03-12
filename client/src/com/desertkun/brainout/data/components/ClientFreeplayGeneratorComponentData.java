package com.desertkun.brainout.data.components;

import com.desertkun.brainout.L;
import com.desertkun.brainout.content.components.ClientFreeplayGeneratorComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.FreeplayGeneratorData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientFreeplayGeneratorComponent")
@ReflectAlias("data.components.ClientFreeplayGeneratorComponentData")
public class ClientFreeplayGeneratorComponentData extends
    ClientActiveActivatorComponentData<ClientFreeplayGeneratorComponent>
{
    private boolean dirty;
    private CurrentMode mode;
    private EffectData currentEffect;
    private LaunchData launchData;

    private enum CurrentMode
    {
        none,
        startup,
        startupFail,
        loop,
        stop
    }

    public ClientFreeplayGeneratorComponentData(
        FreeplayGeneratorData componentObject,
        ClientFreeplayGeneratorComponent contentComponent)
    {
        super(componentObject, contentComponent);

        mode = CurrentMode.none;

        launchData = new LaunchData()
        {
            @Override
            public float getX()
            {
                return getGenerator().getX();
            }

            @Override
            public float getY()
            {
                return getGenerator().getY();
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public String getDimension()
            {
                return getGenerator().getDimension();
            }

            @Override
            public boolean getFlipX()
            {
                return false;
            }
        };
    }

    @Override
    public boolean dirty()
    {
        if (dirty)
        {
            dirty = false;

            return true;
        }

        return false;
    }

    @Override
    public boolean test(PlayerData playerData)
    {
        if (getGenerator().isEmpty())
        {
            if (playerData.getCurrentInstrument() != null)
            {
                return playerData.getCurrentInstrument().getInstrument() == getGenerator().getPetrol();
            }

            return false;
        }

        if (!getGenerator().isRequiredItemFulfilled())
        {
            return false;
        }

        if (!getGenerator().isWorking())
        {
            return true;
        }

        return false;
    }

    private FreeplayGeneratorData getGenerator()
    {
        return ((FreeplayGeneratorData) getComponentObject());
    }

    @Override
    public String getFailedConditionLocalizedText()
    {
        if (getGenerator().isEmpty())
        {
            return L.get("MENU_EMPTY");
        }

        if (!getGenerator().isRequiredItemFulfilled())
        {
            return L.get("MENU_BROKEN");
        }

        return "";
    }

    @Override
    public String getActivateText()
    {
        if (getGenerator().isEmpty())
        {
            return L.get("MENU_REFILL");
        }

        if (!getGenerator().isWorking())
        {
            return L.get("MENU_START_UP");
        }

        return "";
    }

    @Override
    public void update(float dt)
    {
        FreeplayGeneratorData g = getGenerator();
        ClientFreeplayGeneratorComponent c = getContentComponent();

        ClientMap map = getMap(ClientMap.class);

        if (map == null)
            return;

        switch (mode)
        {
            case none:
            {
                if (g.isWorking())
                {
                    if (g.hasFailed())
                    {
                        currentEffect = map.addEffect(c.getStartupFailSound(), launchData);
                        mode = CurrentMode.startupFail;
                    }
                    else
                    {
                        currentEffect = map.addEffect(c.getStartupSound(), launchData);
                        mode = CurrentMode.startup;
                    }
                }

                break;
            }
            case startupFail:
            {
                if (currentEffect.isDone() && !g.isWorking())
                {
                    currentEffect = null;
                    mode = CurrentMode.none;
                }

                break;
            }
            case startup:
            {
                if (currentEffect.isDone())
                {
                    currentEffect = map.addEffect(c.getIdleMusic(), launchData);
                    mode = CurrentMode.loop;
                }

                break;
            }
            case loop:
            {
                if (!g.isWorking())
                {
                    map.removeEffect(currentEffect);
                    currentEffect = map.addEffect(c.getStopSound(), launchData);
                    mode = CurrentMode.stop;
                }

                break;
            }
            case stop:
            {
                if (currentEffect.isDone())
                {
                    currentEffect = null;
                    mode = CurrentMode.none;
                }

                break;
            }
        }
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
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case updated:
            {
                dirty = true;
                break;
            }
        }

        return false;
    }
}
