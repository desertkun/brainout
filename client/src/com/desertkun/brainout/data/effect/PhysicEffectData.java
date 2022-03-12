package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.effect.PhysicEffect;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.interfaces.FlippedAngle;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.PhysicEffectData")
public class PhysicEffectData extends EffectData implements BlockData.ContactPayload
{
    private final PointLaunchData position;
    private final Array<EffectData> child;
    private final PhysicEffect effect;
    private final float mass;
    private final Vector2 speed;
    private final boolean removeChild;

    private Vector2 TEMP_VECT = new Vector2();

    public PhysicEffectData(PhysicEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        this.removeChild = effect.isRemoveChild();
        this.child = new Array<EffectData>();

        float angleTo = launchData.getAngle();

        float offsetX = effect.getOffsetX() != null ? effect.getOffsetX().getValue() : 0;
        float offsetY = effect.getOffsetY() != null ? effect.getOffsetY().getValue() : 0;

        float x = launchData.getX() + offsetX;
        float y = launchData.getY() + offsetY;

        if (effect.getDistanceOffset() > 0)
        {
            x += MathUtils.cosDeg(launchData.getAngle()) * effect.getDistanceOffset();
            y += MathUtils.sinDeg(launchData.getAngle()) * effect.getDistanceOffset();
        }

        this.position = new PointLaunchData(
            x, y, angleTo, launchData.getDimension());

        this.effect = effect;

        this.mass = effect.getMass();
        float speedValue = effect.getSpeed();

        this.speed = new Vector2
        (
            (float)Math.cos(Math.toRadians(position.getAngle())) * speedValue,
            (float)Math.sin(Math.toRadians(position.getAngle())) * speedValue
        );
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        for (EffectData effectData: child)
        {
            effectData.render(batch, context);
        }
    }

    @Override
    public void update(float dt)
    {
        speed.y -= mass * Constants.Core.GRAVITY * dt;

        addPosition(speed.x * dt, speed.y * dt);
    }

    @Override
    public boolean done()
    {
        boolean done = true;

        for (EffectData effectData: child)
        {
            if (!effectData.isDone())
            {
                done = false;
            }
        }

        return done;
    }

    private void addPosition(float addX, float addY)
    {
        ClientMap map = ((ClientMap) getMap());

        if (map == null)
            return;

        float newPosX = position.getX() + addX,
              newPosY = position.getY() + addY;

        boolean hasContact = false;

        TEMP_VECT.set(speed.x * (1 - addX), speed.y * (1 - addY));

        BlockData blockData = map.getBlockAt(newPosX, newPosY, Constants.Layers.BLOCK_LAYER_FOREGROUND);
        if (blockData != null && blockData.isContact(this, newPosX % 1,
            newPosY % 1, speed, speed, TEMP_VECT, effect.getReduce(), map,
            (int)newPosX, (int)newPosY))
        {
            hasContact = true;

            speed.add(TEMP_VECT);

            //addX += TEMP_VECT.x;
            addY += TEMP_VECT.y;
        }

        position.getPosition().add(addX, addY);

        if (hasContact)
        {
            contact(position);
        }
    }

    protected void contact(LaunchData launchData)
    {

    }

    @Override
    public void init()
    {
        this.effect.getAttached().launchEffects(position, child);
    }

    @Override
    public void release()
    {
        super.release();

        if (removeChild)
        {
            ClientMap clientMap = Map.Get(position.getDimension(), ClientMap.class);
            clientMap.removeEffect(child);
        }
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }
}
