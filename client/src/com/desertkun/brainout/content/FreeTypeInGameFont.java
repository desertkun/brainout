package com.desertkun.brainout.content;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.FreeTypeInGameFont")
public class FreeTypeInGameFont extends FreeTypeFont
{
    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        // scale font data to render correctly
        BitmapFont fontData = BrainOutClient.FontMgr.get(fontId);
        fontData.setUseIntegerPositions(false);

        fontData.getData().setScale(1f / ClientConstants.Graphics.RES_SIZE);
    }
}
