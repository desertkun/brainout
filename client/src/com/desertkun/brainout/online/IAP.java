package com.desertkun.brainout.online;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.Environment;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.StoreService;

public class IAP
{
    public interface StoreCallback
    {
        void succeed(StoreService.Store store);
        void failed();
    }

    public static void GetStore(String storeName, StoreCallback callback)
    {
        StoreService store = StoreService.Get();
        LoginService login = LoginService.Get();

        if (store == null || login == null)
        {
            callback.failed();
            return;
        }

        store.getStore(login.getCurrentAccessToken(), storeName,
            (service, request, result, store1) ->
        {
            if (result == Request.Result.success)
            {
                callback.succeed(store1);
            }
            else
            {
                callback.failed();
            }
        });
    }

}
