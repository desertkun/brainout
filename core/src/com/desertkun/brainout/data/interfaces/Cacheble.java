package com.desertkun.brainout.data.interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.desertkun.brainout.data.Map;

public interface Cacheble
{
    void cache(Map map, SpriteCache cache);
    boolean hasCache();
}
