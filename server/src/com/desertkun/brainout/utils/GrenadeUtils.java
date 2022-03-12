package com.desertkun.brainout.utils;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;

public class GrenadeUtils
{
    public static void getGrenadeOutOfWall (Map map, float throwX, float throwY, ActiveData ownerData, ActiveData grenadeData)
    {
        BlockData blockAt = map.getBlockAt(throwX, throwY, Constants.Layers.BLOCK_LAYER_FOREGROUND);
        if (blockAt != null && blockAt.isConcrete())
        {
            // move step-by-step by "stepSize" along the line
            // from throw position to player position and search free block

            boolean throwAnyway = false;
            float stepSize = 0.5f;
            Vector2 fromBodyToItemStep = new Vector2(throwX - ownerData.getX(), throwY - ownerData.getY());
            fromBodyToItemStep = fromBodyToItemStep.nor().scl(stepSize);

            do
            {
                float dst = Vector2.dst(ownerData.getX(), ownerData.getY(), throwX, throwY);

                if (dst <= stepSize)
                {
                    throwX = ownerData.getX();
                    throwY = ownerData.getY();
                    throwAnyway = true;
                }
                else
                {
                    throwX -= fromBodyToItemStep.x;
                    throwY -= fromBodyToItemStep.y;
                }

                blockAt = map.getBlockAt(throwX, throwY, Constants.Layers.BLOCK_LAYER_FOREGROUND);

            } while (blockAt != null && blockAt.isConcrete() && !throwAnyway);
        }

        grenadeData.setPosition(throwX, throwY);
    }
}
