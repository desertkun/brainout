package com.desertkun.brainout.data.components;

import com.desertkun.brainout.components.WeaponDurabilityComponent;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("WeaponDurabilityComponent")
@ReflectAlias("data.components.WeaponDurabilityComponentData")
public class WeaponDurabilityComponentData extends DurabilityComponentData<WeaponDurabilityComponent>
{
    private final float wear;

    public WeaponDurabilityComponentData(ComponentObject componentObject,
                                         WeaponDurabilityComponent contentComponent)
    {
        super(componentObject, contentComponent);

        this.wear = contentComponent.getWear();
    }

    public float getWear()
    {
        return wear;
    }
}
