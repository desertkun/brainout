package com.desertkun.brainout.content;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.desertkun.brainout.ClientConstants;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.IngameFont")
public class IngameFont extends Font
{
    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        // scale font data to render correctly
        BitmapFont fontData = assetManager.get(fileName, BitmapFont.class);
        fontData.setUseIntegerPositions(false);

        fontData.getData().setScale(1f / ClientConstants.Graphics.RES_SIZE);
    }
}
