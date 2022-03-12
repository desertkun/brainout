package com.desertkun.brainout.menu.popups;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.enums.DisconnectReason;

public class ExitPopup extends ConfirmationPopup
{
    public ExitPopup()
    {
        super(L.get("MENU_EXIT_CONFIRM"));
    }

    @Override
    public void yes()
    {
        pop();

        exit();
    }

    protected void exit()
    {
        BrainOutClient.ClientController.disconnect(DisconnectReason.leave, new Runnable()
        {
            @Override
            public void run()
            {
                BrainOutClient.exit();
            }
        });
    }
}
