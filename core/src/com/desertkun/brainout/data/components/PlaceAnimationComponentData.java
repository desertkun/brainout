package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.content.components.PlaceAnimationComponent;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.PlaceBlockData;
import com.desertkun.brainout.data.interfaces.BonePointData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("PlaceAnimationComponent")
@ReflectAlias("data.components.PlaceAnimationComponentData")
public class PlaceAnimationComponentData extends InstrumentAnimationComponentData<PlaceAnimationComponent>
{
    private final InstrumentData instrumentData;
    private final PlaceAnimationComponent instrumentComponent;
    private BonePointData launchPointData;
    private PlayerAnimationComponentData playerAnimation;

    public PlaceAnimationComponentData(PlaceBlockData instrumentData, PlaceAnimationComponent instrumentComponent)
    {
        super(instrumentData, instrumentComponent);

        this.instrumentData = instrumentData;
        this.instrumentComponent = instrumentComponent;
    }

    @Override
    public void init()
    {
        super.init();

        if (instrumentData == null || instrumentData.getOwner() == null)
            return;

        this.playerAnimation = instrumentData.getOwner().getComponent(PlayerAnimationComponentData.class);

        launchPointData = new BonePointData(getSkeleton().findBone("fire-bone"), instrumentLaunch);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case setInstrument:
            {
                activateInstrument();

                break;
            }
        }

        return false;
    }

    public void activateInstrument()
    {
        if (playerAnimation == null)
        {
            return;
        }
        
        if (instrumentData != null && instrumentData.getOwner() != null)
        {
            PlayerComponentData poc = instrumentData.getOwner().getComponentWithSubclass(PlayerComponentData.class);

            if (poc != null)
            {
                poc.setInstrumentState(instrumentComponent.getStates());
            }
        }

        attachTo(getAttachSlot(), playerAnimation);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);
    }

    public BonePointData getLaunchPointData()
    {
        return launchPointData;
    }
}
