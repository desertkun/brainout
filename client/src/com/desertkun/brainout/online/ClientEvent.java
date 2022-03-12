package com.desertkun.brainout.online;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface ClientEvent
{
    TextureRegion getIcon();
    String getTimerToEnd();
    String getIconId();
    String getTitle();
    String getDescription();
    Event getEvent();
}
