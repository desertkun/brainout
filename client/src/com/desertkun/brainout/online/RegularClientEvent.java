package com.desertkun.brainout.online;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.managers.LocalizationManager;
import com.desertkun.brainout.utils.DurationUtils;
import org.anthillplatform.runtime.services.EventService;

public class RegularClientEvent extends Event implements ClientEvent
{
    private String title;
    private String description;

    public RegularClientEvent(EventService.Event event)
    {
        super(event);

        String language = BrainOut.LocalizationMgr.getCurrentLanguage();

        this.title = event.title.getOrDefault(language, event.title.getOrDefault(LocalizationManager.GetDefaultLanguage(), ""));
        this.description = event.description.getOrDefault(language, event.title.getOrDefault(LocalizationManager.GetDefaultLanguage(), ""));

    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public String getTimerToEnd()
    {
        return DurationUtils.GetDurationString(getSecondsLeft());
    }

    public String getIconId()
    {
        return icon;
    }

    public TextureRegion getIcon()
    {
        if (icon != null && !icon.equals(""))
        {
            return BrainOutClient.getRegion(icon);
        }
        else
        {
            return BrainOutClient.getRegion("icon-gears-big");
        }
    }

    @Override
    public Event getEvent()
    {
        return this;
    }

    @Override
    protected Reward newReward()
    {
        return new ClientReward();
    }
}
