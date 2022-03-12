package com.desertkun.brainout.data.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.components.my.MyWeaponComponent;
import com.desertkun.brainout.content.components.ConditionalSlotComponent;
import com.desertkun.brainout.content.components.InstrumentActionsComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.spine.Slot;

@Reflect("ConditionalSlotComponent")
@ReflectAlias("data.components.ConditionalSlotComponentData")
public class ConditionalSlotComponentData extends Component<ConditionalSlotComponent>
{
    public ConditionalSlotComponentData(InstrumentData instrumentData, ConditionalSlotComponent contentComponent)
    {
        super(instrumentData, contentComponent);
    }

    @Override
    public void init()
    {
        super.init();

        Gdx.app.postRunnable(this::updateConditions);
    }

    private void updateConditions()
    {
        InstrumentData instrumentData = ((InstrumentData) getComponentObject());

        if (instrumentData.getOwner() == null)
            return;

        if (instrumentData.getOwner().getOwnerId() != BrainOutClient.ClientController.getMyId())
            return;

        InstrumentAnimationComponentData iac =
            getComponentObject().getComponentWithSubclass(InstrumentAnimationComponentData.class);

        if (iac == null)
            return;

        for (ObjectMap.Entry<String, String> entry : getContentComponent().getConditions())
        {
            String slotName = entry.key;
            String condition = entry.value;

            Slot slot = iac.getSkeleton().findSlot(slotName);

            if (slot != null)
            {
                if (checkCondition(condition))
                {
                    if (slot.getAttachment() instanceof
                        InstrumentActionsComponent.UpdateSlotAction.VirtualAttachment)
                    {
                        InstrumentActionsComponent.UpdateSlotAction.VirtualAttachment attachment =
                            ((InstrumentActionsComponent.UpdateSlotAction.VirtualAttachment) slot.getAttachment());

                        slot.setAttachment(attachment.getFrom());
                    }
                }
                else
                {
                    if (!(slot.getAttachment() instanceof
                        InstrumentActionsComponent.UpdateSlotAction.VirtualAttachment))
                    {
                        if (slot.getAttachment() != null)
                            slot.setAttachment(
                                new InstrumentActionsComponent.UpdateSlotAction.VirtualAttachment(
                                    slot.getAttachment()));
                    }
                }
            }
        }
    }

    private boolean checkCondition(String condition)
    {
        switch (condition)
        {
            case "loaded":
            {
                MyWeaponComponent w = getComponentObject().getComponent(MyWeaponComponent.class);

                return w != null && w.getCurrentSlot().isLoaded();
            }
            case "unloaded":
            {
                MyWeaponComponent w = getComponentObject().getComponent(MyWeaponComponent.class);

                return w != null && !w.getCurrentSlot().isLoaded();
            }
        }

        return false;
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
