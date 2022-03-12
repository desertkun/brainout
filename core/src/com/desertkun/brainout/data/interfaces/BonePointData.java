package com.desertkun.brainout.data.interfaces;

import com.desertkun.brainout.data.Map;
import com.esotericsoftware.spine.Bone;

public class BonePointData extends LaunchData
{
    private final Bone bone;
    private final LaunchData ownerData;

    public BonePointData(Bone bone, LaunchData ownerData)
    {
        super();

        if (bone == null) throw new RuntimeException("Bone cannot be null.");

        this.bone = bone;
        this.ownerData = ownerData;
    }

    public float getLength()
    {
        return bone.getData().getLength();
    }

    @Override
    public float getX() {
        return bone.getWorldX();
    }

    @Override
    public float getY() {
        return bone.getWorldY();
    }

    @Override
    public float getAngle()
    {
        return bone != null ? bone.getWorldRotation() : 0;
    }

    @Override
    public String getDimension()
    {
        return ownerData != null ? ownerData.getDimension() : null;
    }

    @Override
    public boolean getFlipX()
    {
        return bone.getSkeleton().getFlipX();
    }
}
