package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.SecondaryWeaponSlotComponent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("SecondaryWeaponSlotComponent")
@ReflectAlias("data.components.SecondaryWeaponSlotComponentData")
public class SecondaryWeaponSlotComponentData extends Component<SecondaryWeaponSlotComponent>
{
    public SecondaryWeaponSlotComponentData(ComponentObject componentObject,
                                            SecondaryWeaponSlotComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    public Weapon.WeaponProperties getWeaponProperties()
    {
        return getContentComponent().getWeaponProperties();
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
