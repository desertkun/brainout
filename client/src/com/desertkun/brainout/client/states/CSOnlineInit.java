package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.*;
import org.anthillplatform.runtime.requests.JsonRequest;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.requests.StringRequest;
import org.anthillplatform.runtime.services.DiscoveryService;
import org.anthillplatform.runtime.services.EnvironmentService;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CSOnlineInit extends ControllerState
{
    private final boolean resetToken;

    public CSOnlineInit(boolean resetToken)
    {
        this.resetToken = resetToken;
    }

    public CSOnlineInit()
    {
        this(false);
    }

    @Override
    public ID getID()
    {
        return ID.onlineInit;
    }

    @Override
    public void init()
    {
        if (!BrainOut.OnlineEnabled())
        {
            Gdx.app.postRunnable(() -> complete(Request.Result.success, "OK"));
            return;
        }

        final GameUser user = BrainOutClient.Env.getGameUser();

        user.read();

        EnvironmentService environmentService = EnvironmentService.Get();

        if (environmentService == null)
        {
            CSOnlineInit.this.complete(Request.Result.dataCorrupted, "No environment service");
            return;
        }

        environmentService.getEnvironmentInfo((service, request, result, discoveryLocation, environmentInformation) ->
        {
            if (result == Request.Result.success)
            {
                onlineInited("");
            }
            else
            {
                CSOnlineInit.this.complete(result, "lib " + result.toString());
            }
        });
    }

    private void onlineInited(String environmentName)
    {
        EnvironmentService environmentService = EnvironmentService.Get();

        boolean maintenance = environmentService.variable("maintenance", false, Boolean.class);

        if (maintenance)
        {
            switchTo(new CSMaintenance());
            return;
        }

        final GameUser user = BrainOutClient.Env.getGameUser();

        user.getAccounts().setCurrentEnvironment(environmentName);

        /*
        if (BrainOutClient.Analytics == null)
        {
            String location = BrainOutClient.Online.variable("analytics", String.class);

            if (location != null && !location.isEmpty())
            {
                BrainOutClient.Analytics = new Analytics(location);
            }
        }
        */

        DiscoveryService discoveryService = DiscoveryService.Get();

        if (discoveryService == null)
            throw new RuntimeException("No discovery service!");

        discoveryService.discoverServices(Constants.Connection.DISCOVER,
            (service, request, result, discoveredServices) ->
        {
            switch (result)
            {
                case success:
                {
                    proceed();

                    break;
                }
                default:
                {
                    switchTo(new CSError("Failed to discover services: " + result.toString()));

                    break;
                }
            }
        });
    }

    private void proceed()
    {
        LoginService loginService = LoginService.Get();

        if (loginService == null)
            throw new RuntimeException("No login service!");

        final GameUser user = BrainOutClient.Env.getGameUser();
        GameUser.Account account = user.getAccounts().getAccount();

        if (account == null)
        {
            user.getAccounts().setNewAccountForEnvironment();
        }

        doAuth(loginService);
    }

    private void doAuth(LoginService loginService)
    {
        Request.Fields options = new Request.Fields();

        options.put("should_have", ClientConstants.Scopes.SHOULD_HAVE);
        options.put("as", "brainout");

        GameUser.Account account = BrainOutClient.Env.getCurrentAccount();

        if (account.hasToken() && StoreAccessToken())
        {
            if (resetToken)
            {
                account.resetAccessToken();
            }
            else
            {
                LoginService.AccessToken accessToken = loginService.newAccessToken(account.getAccessToken());

                loginService.validateAccessToken(accessToken,
                    (service, request, result, validatedAccount, credential, scopes) ->
                {
                    switch (result)
                    {
                        case success:
                        {
                            for (String scope : ClientConstants.Scopes.SHOULD_HAVE.split(","))
                            {
                                if (!scopes.contains(scope))
                                {
                                    // the validated token does not have required scope
                                    account.resetAccessToken();
                                    doAuth(loginService);
                                    return;
                                }
                            }

                            authorized(loginService, accessToken, validatedAccount, credential, scopes);

                            break;
                        }
                        default:
                        {
                            account.resetAccessToken();
                            doAuth(loginService);
                            break;
                        }
                    }
                });

                return;
            }
        }

        account.auth(loginService, LoginService.Scopes.FromString(ClientConstants.Scopes.SCOPES), options,
            (service, request, result, accessToken, account1, credential, scopes) ->
        {
            switch (result)
            {
                case success:
                {
                    authorized(loginService, accessToken, account1, credential, scopes);
                    break;
                }

                case multipleChoices:
                {
                    switchTo(new CSMultipleAccounts(((JsonRequest) request).getObject()));

                    break;
                }

                // we can't authorize, so create new anonymous account
                case forbidden:
                {
                    Gdx.app.postRunnable(() ->
                    {
                        final GameUser user = BrainOutClient.Env.getGameUser();

                        user.getAccounts().setNewAccountForEnvironment();
                        user.write();

                        doAuth(loginService);
                    });

                    break;
                }
                default:
                {
                    CSOnlineInit.this.complete(result, "login " + result.toString());
                }
            }
        });
    }

    private void authorized(
        LoginService loginService,
        LoginService.AccessToken token,
        String account, String credential, LoginService.Scopes scopes)
    {
        loginService.setCurrentAccessToken(token);

        final GameUser user = BrainOutClient.Env.getGameUser();

        GameUser.Account account_ = user.getAccounts().getAccount();

        if (account_ == null)
        {
            complete(Request.Result.failed, "no account");
            return;
        }

        /*
        if (BrainOutClient.Analytics != null)
        {
            BrainOutClient.Analytics.setUserId(token.getAccount());
        }
        */

        if (StoreAccessToken())
        {
            account_.setAccessToken(token.get());
            user.write();
        }

        BrainOutClient.ClientController.setMyAccount(account);
        complete(Request.Result.success, "");
    }

    private static boolean SkipOnce = false;

    private static boolean StoreAccessToken()
    {
        if (SkipOnce)
        {
            return true;
        }

        boolean result = BrainOutClient.Env.storeAccessToken();
        SkipOnce = true;
        return result;
    }

    private void complete(Request.Result result, String reason)
    {
        if (result == Request.Result.success)
        {
            if (BrainOutClient.ConnectFreePlayPartyId != null)
            {
                switchTo(new CSGetRegions());
            }
            else if (BrainOutClient.ConnectRoomId != null)
            {
                switchTo(new CSJoinRoom(BrainOutClient.ConnectRoomId));

                BrainOutClient.ConnectRoomId = null;
            }
            else
            {
                switchTo(new CSWaitForUser());
            }
        }
        else
        {
            switchTo(new CSError(L.get("MENU_FAILED_TO_INIT_ONLINE", reason),
                () -> switchTo(new CSOnlineInit())));
        }
    }

    @Override
    public void release()
    {

    }
}
