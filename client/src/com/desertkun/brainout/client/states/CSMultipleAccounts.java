package com.desertkun.brainout.client.states;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.GameUser;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONArray;
import org.json.JSONObject;

public class CSMultipleAccounts extends ControllerState
{
    private final LoginService.AccessToken resolveToken;
    private Array<Profile> profiles;

    public void resolve(String resolveAccount)
    {
        final GameUser user = BrainOutClient.Env.getGameUser();

        Request.Fields options = new Request.Fields();

        options.put("should_have", ClientConstants.Scopes.SHOULD_HAVE);

        switchTo(new CSLoading());

        LoginService loginService = LoginService.Get();

        if (loginService == null)
            throw new RuntimeException("No login service!");

        loginService.resolve(resolveToken, "multiple_accounts_attached", resolveAccount,
            LoginService.Scopes.FromString(ClientConstants.Scopes.SCOPES), options,
            (service, request, result, accessToken, credential, account, scopes) ->
        {
            switch (result)
            {
                case success:
                {
                    switchTo(new CSPrivacyPolicy());

                    break;
                }
                default:
                {
                    switchTo(new CSError(result.toString()));
                    break;
                }
            }
        });
    }

    public class Profile
    {
        public JSONObject data;
        public String account;
    }

    public CSMultipleAccounts(JSONObject data)
    {
        this.profiles = new Array<>();

        JSONArray accounts = data.optJSONArray("accounts");

        if (accounts != null)
        {
            for (int i = 0; i < accounts.length(); i++)
            {
                JSONObject item = accounts.getJSONObject(i);

                JSONObject profile = item.optJSONObject("profile");
                String account = item.optString("account");

                if (profile != null && account != null)
                {
                    Profile p = new Profile();
                    p.data = profile;
                    p.account = account;

                    profiles.add(p);
                }
            }
        }

        this.resolveToken = LoginService.Get().newAccessToken(data.getString("resolve_token"));
    }

    @Override
    public ID getID()
    {
        return ID.multipleAccounts;
    }

    @Override
    public void init()
    {

    }

    @Override
    public void release()
    {

    }

    public Array<Profile> getProfiles()
    {
        return profiles;
    }

    public LoginService.AccessToken getResolveToken()
    {
        return resolveToken;
    }
}
