package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.content.components.WeaponAnimationComponent;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.*;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("WeaponAnimationComponent")
@ReflectAlias("data.components.WeaponAnimationComponentData")
public class WeaponAnimationComponentData extends InstrumentAnimationComponentData<WeaponAnimationComponent>
    implements Animable
{
    protected final WeaponData weaponData;
    protected Array<String> suppressedAttachments;
    public WeaponAnimationComponentData(WeaponData weaponData, WeaponAnimationComponent contentComponent)
    {
        super(weaponData, contentComponent);

        this.weaponData = weaponData;
    }
    @Override
    public void update(float dt)
    {
        super.update(dt);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);
    }

    @Override
    public float getX()
    {
        return launchPointData.getX();
    }

    @Override
    public float getY() {
        return launchPointData.getY();
    }

    @Override
    public float getAngle()
    {
        return launchPointData.getAngle();
    }

    @Override
    public boolean getFlipX()
    {
        return launchPointData.getFlipX();
    }

    public Array<String> getSuppressedAttachments()
    {
        return suppressedAttachments;
    }

    public void suppressAttachments(Array<String> suppress)
    {
        this.suppressedAttachments = suppress;
    }
}
