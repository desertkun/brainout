package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Levels;

public class TechUserPanel extends UserPanel
{
    public TechUserPanel()
    {
        super(true);
    }

    @Override
    protected int getScore()
    {
        return user.getInt("tech-score", 0);
    }

    @Override
    protected String getProgressStyle()
    {
        return "progress-tech-score";
    }

    @Override
    protected void addLevelNumberIcon(Table levelInfo)
    {
        levelInfo.add(new Image(BrainOutClient.getRegion("instrument-upgrades"))).padRight(4);
    }

    @Override
    protected String getLevelString(Levels.Level level)
    {
        return level.toShortString();
    }

    @Override
    protected String getLevelStyle()
    {
        return "title-small";
    }

    @Override
    protected Levels.Level getLevel()
    {
        Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.TECH_LEVEL);

        if (levels == null)
        {
            return null;
        }

        return levels.getLevel(user.getLevel(
            Constants.User.TECH_LEVEL
        ));
    }
}
