package com.desertkun.brainout.utils;

import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.attachments.*;

public class NullRenderAttachmentLoader implements AttachmentLoader
{
    @Override
    public RegionAttachment newRegionAttachment(Skin skin, String name, String path)
    {
        return null;
    }

    @Override
    public MeshAttachment newMeshAttachment(Skin skin, String name, String path)
    {
        return null;
    }

    @Override
    public ClippingAttachment newClippingAttachment(Skin skin, String name)
    {
        return null;
    }

    @Override
    public PathAttachment newPathAttachment(Skin skin, String name)
    {
        return null;
    }

    @Override
    public BoundingBoxAttachment newBoundingBoxAttachment(Skin skin, String name)
    {
        return new BoundingBoxAttachment(name);
    }

    @Override
    public PointAttachment newPointAttachment(Skin skin, String name)
    {
        return new PointAttachment(name);
    }
}
