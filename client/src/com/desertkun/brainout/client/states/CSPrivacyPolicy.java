package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.menu.impl.PrivacyPolicyMenu;

public class CSPrivacyPolicy extends ControllerState
{
    public CSPrivacyPolicy()
    {
        this(false);
    }

    public CSPrivacyPolicy(boolean clearFlag)
    {
        if (clearFlag)
        {
            Preferences privacy = Gdx.app.getPreferences("privacy");
            privacy.remove("accepted");
            privacy.flush();
        }
    }

    @Override
    public ID getID()
    {
        return ID.privacy;
    }

    @Override
    public void init()
    {
        if (!BrainOutClient.Env.needsPrivacyPolicy())
        {
            accepted();
            return;
        }

        Preferences privacy = Gdx.app.getPreferences("privacy");

        if (privacy.contains("accepted"))
        {
            accepted();
        }
        else
        {
            BrainOutClient.getInstance().topState().pushMenu(new PrivacyPolicyMenu(new Runnable()
            {
                @Override
                public void run()
                {
                    privacy.putBoolean("accepted", true);
                    privacy.flush();

                    accepted();
                }
            }));
        }
    }

    private void accepted()
    {
        switchTo(new CSOnlineInit());
    }

    @Override
    public void release()
    {

    }
}
