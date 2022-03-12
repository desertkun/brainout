package com.desertkun.brainout.online;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.battlepass.BattlePass;
import com.desertkun.brainout.data.battlepass.BattlePassData;
import com.desertkun.brainout.managers.LocalizationManager;
import com.desertkun.brainout.utils.DurationUtils;
import org.anthillplatform.runtime.services.EventService;

public class ClientBattlePassEvent extends BattlePassEvent implements ClientEvent
{
    private String title;
    private BattlePassData data;

    public ClientBattlePassEvent(EventService.Event event)
    {
        super(event);

        String language = BrainOut.LocalizationMgr.getCurrentLanguage();

        this.title = event.title.getOrDefault(language, event.title.getOrDefault(LocalizationManager.GetDefaultLanguage(), ""));
    }

    @Override
    public void parse(EventService.Event event)
    {
        super.parse(event);

        BattlePass bp = BrainOut.ContentMgr.get(battlePass, BattlePass.class);

        data = bp.getData(this, BrainOutClient.ClientController.getUserProfile(),
            BrainOutClient.ClientController.getMyAccount(), event.profile);
    }

    public BattlePassData getData()
    {
        return data;
    }

    @Override
    public TextureRegion getIcon()
    {
        return null;
    }

    public String getTimerToEnd()
    {
        return DurationUtils.GetDurationString(getSecondsLeft());
    }

    @Override
    protected Reward newReward()
    {
        return new ClientReward();
    }

    @Override
    public String getIconId()
    {
        return "";
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public String getDescription()
    {
        return "";
    }

    @Override
    public Event getEvent()
    {
        return this;
    }
}
