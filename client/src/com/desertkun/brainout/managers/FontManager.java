package com.desertkun.brainout.managers;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ArrayMap;
import com.desertkun.brainout.BrainOutClient;

public class FontManager 
{
    private ArrayMap<String, BitmapFont> fonts;

	public void registerFont(String fontId, BitmapFont bitmapFont)
	{
		fonts.put(fontId, bitmapFont);

		BrainOutClient.Skin.add(fontId, bitmapFont);
	}

	public void unregisterFont(String fontId)
	{
		fonts.removeKey(fontId);
	}
	
	public FontManager()
	{
		fonts = new ArrayMap<String, BitmapFont>();
	}

    public BitmapFont get(String s)
    {
        return fonts.get(s);
    }
}
