package com.desertkun.brainout.utils;

import com.desertkun.brainout.Constants;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;

public class Physics
{
    public static void ComputeVertices(Slot slot, BoundingBoxAttachment bb, float[] worldVertices)
    {
        float m00 = slot.getBone().getA();
        float m01 = slot.getBone().getB();
        float m10 = slot.getBone().getC();
        float m11 = slot.getBone().getD();

        float[] vertices = bb.getVertices();
        for (int i = vertices.length - 2; i >= 0; i -= 2)
        {
            float px = vertices[i];
            float py = vertices[i + 1];

            worldVertices[i] = (px * m00 + py * m01) * Constants.Physics.SCALE;
            worldVertices[i + 1] = (px * m10 + py * m11) * Constants.Physics.SCALE;
        }
    }

}
