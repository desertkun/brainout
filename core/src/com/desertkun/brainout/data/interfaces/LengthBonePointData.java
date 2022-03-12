package com.desertkun.brainout.data.interfaces;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.data.Map;
import com.esotericsoftware.spine.Bone;

public class LengthBonePointData extends BonePointData
{
    private float lengthOffset;

    public LengthBonePointData(Bone bone, LaunchData ownerData, float lengthOffset)
    {
        super(bone, ownerData);

        this.lengthOffset = lengthOffset;
    }

    @Override
    public float getX()
    {
        return super.getX() + MathUtils.cosDeg(getFlippedAngle()) * getLength();
    }

    @Override
    public float getLength()
    {
        return super.getLength() + lengthOffset;
    }

    public float getFlippedAngle() {
        return getAngle();
    }

    @Override
    public float getY()
    {
        return super.getY() + MathUtils.sinDeg(getFlippedAngle()) * getLength();
    }
}
