package com.desertkun.brainout.data.components;

import box2dLight.ConeLight;
import com.badlogic.gdx.physics.box2d.Filter;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.InstrumentLightComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.BonePointData;
import com.desertkun.brainout.data.interfaces.FlippedAngle;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.OwnerChangedEvent;
import com.desertkun.brainout.events.SetInstrumentEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.spine.Skeleton;

@Reflect("InstrumentLightComponent")
@ReflectAlias("data.components.InstrumentLightComponentData")
public class InstrumentLightComponentData extends Component<InstrumentLightComponent>
{
    private final InstrumentData instrumentData;
    private BonePointData laserPoint;
    private ConeLight light;
    private boolean active;
    private String generatedDimension = "";

    private static Filter SHADOW_FILTER = new Filter();

    static
    {
        SHADOW_FILTER.categoryBits = 1 << Constants.Physics.CATEGORY_LIGHT;
        SHADOW_FILTER.maskBits = ~(1 << Constants.Physics.CATEGORY_BELT | 1 << Constants.Physics.CATEGORY_RAGDOLL);
    }

    public InstrumentLightComponentData(InstrumentData instrumentData, InstrumentLightComponent lightComponent)
    {
        super(instrumentData, lightComponent);

        this.instrumentData = instrumentData;
        this.active = false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case setInstrument:
            {
                SetInstrumentEvent e = ((SetInstrumentEvent) event);

                if (e.playerData == instrumentData.getOwner())
                {
                    setActive(instrumentData == e.selected);
                }
                break;
            }
            case simple:
            {
                SimpleEvent e = ((SimpleEvent) event);

                if (e.getAction() == SimpleEvent.Action.deselected)
                {
                    setActive(false);
                    break;
                }

                break;
            }
            case ownerChanged:
            {
                OwnerChangedEvent e = ((OwnerChangedEvent) event);

                if (e.newOwner == null)
                {
                    setActive(false);
                }
            }
        }

        return false;
    }

    @Override
    public void init()
    {
        super.init();

        InstrumentAnimationComponentData cwp = getComponentObject().
            getComponentWithSubclass(InstrumentAnimationComponentData.class);

        if (cwp == null)
            return;

        laserPoint = new BonePointData(
            cwp.getSkeleton().findBone("laser-bone"),
            cwp.getInstrumentLaunch());
    }

    private void setActive(boolean active)
    {
        this.active = active;

        update(0);
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (!canRenderLights())
        {
            return;
        }

        if (Map.GetWatcher() == null)
        {
            return;
        }

        if (laserPoint == null)
            return;

        boolean generated = light != null;

        if (generated)
        {
            if (!generatedDimension.equals(Map.GetWatcher().getDimension()))
            {
                free();
                generate();
            }
            updateTransform();
        }

        boolean isVisible = instrumentData.getOwner() != null && instrumentData.getOwner().isVisible();
        boolean should = active && isVisible;

        if (should != generated)
        {
            if (should)
            {
                generate();
            }
            else
            {
                free();
            }
        }
    }

    private boolean canRenderLights()
    {
        return BrainOutClient.ClientSett.isLightsEnabled();
    }

    private void free()
    {
        if (light != null)
        {
            try
            {
                light.remove(true);
            }
            catch (IllegalArgumentException ignored) {}

            light = null;
        }
    }

    private void generate()
    {
        if (light != null)
            return;

        ActiveData playerData = instrumentData.getOwner();

        ClientMap clientMap = ((ClientMap) getMap());

        if (clientMap == null)
        {
            return;
        }

        if (clientMap.getLights() == null)
        {
            return;
        }

        float angle = laserPoint.getAngle();

        InstrumentLightComponent lightComponent = getContentComponent();

        light = new ConeLight(clientMap.getLights(), lightComponent.getRays(),
                lightComponent.getColor(), lightComponent.getDistance(),
                laserPoint.getX(), laserPoint.getY(), angle, lightComponent.getCone());

        light.setSoft(lightComponent.getSoft() > 0);
        light.setSoftnessLength(lightComponent.getSoft());
        light.setContactFilter(SHADOW_FILTER);

        if (Map.GetWatcher() != null)
        {
            generatedDimension = Map.GetWatcher().getDimension();
        }
    }

    private void updateTransform()
    {
        if (light != null)
        {
            light.setPosition(laserPoint.getX(), laserPoint.getY());
            float angle = laserPoint.getAngle();
            light.setDirection(angle);
        }
    }

    @Override
    public void release()
    {
        super.release();

        generatedDimension = "";

        free();
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return BrainOutClient.ClientSett.isLightsEnabled();
    }
}
