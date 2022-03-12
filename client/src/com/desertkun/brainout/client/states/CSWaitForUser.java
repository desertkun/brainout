package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.InputPopup;
import com.desertkun.brainout.menu.popups.OKInputPopup;

public class CSWaitForUser extends ControllerState
{
    @Override
    public ID getID()
    {
        return ID.waitForUser;
    }

    @Override
    public void init()
    {

    }

    @Override
    public void release()
    {

    }

    public void proceed()
    {
        if (BrainOut.OnlineEnabled())
        {
            switchTo(new CSGetRegions());
        }
        else
        {
            BrainOutClient.getInstance().topState().pushMenu(new OKInputPopup("Enter Server IP", "127.0.0.1")
            {
                @Override
                public void ok()
                {
                    String address = getValue();
                    Gdx.app.postRunnable(() ->
                    {
                        BrainOutClient.ClientController.connect(
                            address,
                            36555, 36556, 36557,
                            null,
                            false,
                            -1, () -> pushMenu(new AlertPopup("MENU_CONNECTION_ERROR")));
                    });
                }
            });
        }
    }
}
