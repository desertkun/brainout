package com.desertkun.brainout.desktop.client.states;

import com.codedisaster.steamworks.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSError;
import com.desertkun.brainout.client.states.CSOnlineInit;
import com.desertkun.brainout.client.states.ControllerState;
import com.desertkun.brainout.desktop.SteamConstants;
import com.desertkun.brainout.desktop.SteamEnvironment;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.utils.ByteArrayUtils;

import java.nio.ByteBuffer;

public class CSSteamUserInit extends ControllerState
{
    private SteamUser user;
    private SteamUserStats stats;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(256);

    @Override
    public ID getID()
    {
        return ID.steamUserInit;
    }

    @Override
    public void init()
    {
        SteamEnvironment env = ((SteamEnvironment) BrainOutClient.Env);

        user = new SteamUser(new SteamUserCallback()
        {
            @Override
            public void onAuthSessionTicket(SteamAuthTicket authTicket, SteamResult result)
            {

            }

            @Override
            public void onValidateAuthTicket(
                SteamID steamID,
                SteamAuth.AuthSessionResponse authSessionResponse,
                SteamID ownerSteamID)
            {
                /*
                switch(authSessionResponse)
                {
                    case OK:
                    {
                        ticketValidated();
                        break;
                    }

                    case UserNotConnectedToSteam:
                    case NoLicenseOrExpired:
                    case VACBanned:
                    case LoggedInElseWhere:
                    case VACCheckTimedOut:
                    case AuthTicketCanceled:
                    case AuthTicketInvalidAlreadyUsed:
                    case AuthTicketInvalid:
                    case PublisherIssuedBan:
                    {
                        switchTo(new CSError(L.get("MENU_FAILED_TO_AUTH_STEAM", authSessionResponse.toString())));
                    }
                }
                */
            }

            @Override
            public void onMicroTxnAuthorization(int appID, long orderID, boolean authorized)
            {
                if (authorized)
                {
                    if (appID == SteamConstants.APP_ID)
                    {
                        BrainOutClient.ClientController.updateOrder(orderID);
                    }
                }

                Menu topMenu = BrainOutClient.getInstance().topState().topMenu();

                if (topMenu != null)
                {
                    topMenu.onFocusIn();
                }
            }

            @Override
            public void onEncryptedAppTicket(SteamResult result)
            {

            }
        });

        env.getGameUser().setSteamUser(user);

        getTicket();
    }

    private void getTicket()
    {
        SteamEnvironment env = ((SteamEnvironment) BrainOutClient.Env);

        buffer.clear();

        int[] sizeRequired = new int[1];

        SteamAuthTicket ticket;

        try
        {
            ticket = user.getAuthSessionTicket(buffer, sizeRequired);
        }
        catch (SteamException e)
        {
            switchTo(new CSError(e.getMessage()));
            return;
        }

        if (ticket.isValid())
        {
            env.getGameUser().setKey(ByteArrayUtils.toHex(buffer));

            switchTo(new CSSteamStats());
        }
        else
        {
            if (sizeRequired[0] < buffer.capacity())
            {
                switchTo(new CSError("Error: failed creating auth ticket"));
            }
            else
            {
                switchTo(new CSError("Error: buffer too small for auth ticket, need " + sizeRequired[0] + " bytes"));
            }
        }

    }

    @Override
    public void release()
    {

    }
}
