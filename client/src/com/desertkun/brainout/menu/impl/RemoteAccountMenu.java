package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.online.UserProfile;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.ProfileService;

public class RemoteAccountMenu extends PlayerProfileMenu
{
    private final String accountId;
    private final String credential;

    public RemoteAccountMenu(String accountId, String credential)
    {
        this(accountId, credential, false);
    }

    public RemoteAccountMenu(String accountId, String credential, boolean limited)
    {
        super(limited);

        this.accountId = accountId;
        this.credential = credential;
    }

    @Override
    protected void receive(ProfileCallback callback)
    {
        LoginService loginService = LoginService.Get();
        ProfileService profileService = ProfileService.Get();

        if (profileService == null || loginService == null)
        {
            callback.received(false, null);
            return;
        }

        profileService.getAccountProfile(loginService.getCurrentAccessToken(), accountId,
            (service, request, result, profile) ->
        {
            Gdx.app.postRunnable(() ->
            {
                if (result == Request.Result.success)
                {
                    UserProfile userProfile = new UserProfile();
                    userProfile.read(profile);

                    callback.received(true, userProfile);
                }
                else
                {
                    callback.received(false, null);
                }
            });
        });
    }

    @Override
    protected String getSteamID()
    {
        if (credential == null)
            return null;

        if (!credential.startsWith("steam"))
            return null;

        return credential.substring(6);
    }

    @Override
    protected String getAccountID()
    {
        return accountId;
    }

    @Override
    protected boolean hasComplaintsButton()
    {
        return !accountId.equals(BrainOutClient.ClientController.getMyAccount());
    }
}
