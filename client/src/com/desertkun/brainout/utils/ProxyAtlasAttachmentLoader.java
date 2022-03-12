package com.desertkun.brainout.utils;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.desertkun.brainout.BrainOutClient;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.attachments.*;

public class ProxyAtlasAttachmentLoader implements AttachmentLoader
{
    public ProxyAtlasAttachmentLoader()
    {
    }

    @Override
    public RegionAttachment newRegionAttachment (Skin skin, String name, String path)
    {
        TextureAtlas.AtlasRegion region = BrainOutClient.getRegion(path);
        if (region == null)
            throw new RuntimeException("Region not found in atlas: " + path + " (region attachment: " + name + ")");
        RegionAttachment attachment = new RegionAttachment(name);
        attachment.setRegion(region);
        return attachment;
    }

    @Override
    public MeshAttachment newMeshAttachment (Skin skin, String name, String path)
    {
        TextureAtlas.AtlasRegion region = BrainOutClient.getRegion(path);
        if (region == null) throw new RuntimeException("Region not found in atlas: " + path + " (mesh attachment: " + name + ")");
        MeshAttachment attachment = new MeshAttachment(name);
        attachment.setRegion(region);
        return attachment;
    }

    @Override
    public ClippingAttachment newClippingAttachment(Skin skin, String name)
    {
        return new ClippingAttachment(name);
    }

    @Override
    public PathAttachment newPathAttachment(Skin skin, String name)
    {
        return new PathAttachment(name);
    }

    @Override
    public BoundingBoxAttachment newBoundingBoxAttachment (Skin skin, String name)
    {
        return new BoundingBoxAttachment(name);
    }

    @Override
    public PointAttachment newPointAttachment(Skin skin, String name)
    {
        return new PointAttachment(name);
    }
}
