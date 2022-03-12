package com.desertkun.brainout.content.components.interfaces;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.upgrades.Upgrade;

public interface AffectedByUpgrades
{
    void upgraded(ObjectMap<String, Upgrade> upgrades);
}
