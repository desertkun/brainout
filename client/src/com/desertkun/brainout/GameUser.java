package com.desertkun.brainout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.utils.HashUtils;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.util.ApplicationInfo;

import java.io.File;
import java.lang.StringBuilder;
import java.util.Date;
import java.util.Random;

public class GameUser
{
    public static final int PROFILE_VERSION_2 = 1;
    public static final int CURRENT_PROFILE_VERSION = PROFILE_VERSION_2;

    private abstract class UserLoader
    {
        public abstract void read(Preferences preferences);
    }

    public abstract static class Account implements Json.Serializable
    {
        private String accessToken;

        public Account(String accessToken)
        {
            this.accessToken = accessToken;
        }

        public Account()
        {
            this("");
        }

        @Override
        public void write(Json json)
        {
            if (!accessToken.isEmpty())
            {
                json.writeValue("token", accessToken);

                String sign = HashUtils.Verify(getCredential(), accessToken);
                json.writeValue("__S", sign);
            }
        }

        public abstract String getId();
        public abstract String getCredential();

        public abstract void auth(
            LoginService loginService,
            LoginService.Scopes scopes,
            Request.Fields options,
            LoginService.AuthenticationCallback callback);

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            accessToken = jsonData.getString("token", "");

            if (accessToken == null)
            {
                accessToken = "";
            }

            String hash = jsonData.getString("__S", "");

            if (hash.isEmpty())
            {
                accessToken = "";
            }
            else
            {
                String sign = HashUtils.Verify(getCredential(), accessToken);

                if (!hash.equals(sign))
                {
                    accessToken = "";
                }
            }
        }

        public String getAccessToken()
        {
            return accessToken;
        }

        public void resetAccessToken()
        {
            this.accessToken = "";
        }

        public boolean hasToken()
        {
            return !accessToken.isEmpty();
        }

        public void setAccessToken(String accessToken)
        {
            this.accessToken = accessToken;
        }
    }

    public static class AnonymousAccount extends Account
    {
        private String id;
        private String password;

        public AnonymousAccount(String id, String password, String accessToken)
        {
            super(accessToken);

            this.id = id;
            this.password = password;
        }

        public AnonymousAccount()
        {
            super();

            this.id = null;
            this.password = null;
        }

        @Override
        public void auth(
            LoginService loginService,
            LoginService.Scopes scopes,
            Request.Fields options,
            LoginService.AuthenticationCallback callback)
        {
            ApplicationInfo applicationInfo = BrainOutClient.Online.getApplicationInfo();

            loginService.authAnonymous(getId(), getPassword(), applicationInfo.gamespace,
                scopes, options, callback, null, applicationInfo.shouldHaveScopes);
        }

        @Override
        public String getId()
        {
            return id;
        }

        @Override
        public String getCredential()
        {
            return "anonymous:" + getId();
        }

        public String getPassword()
        {
            return password;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        @Override
        public void write(Json json)
        {
            json.writeValue("id", id);
            json.writeValue("password", password);

            super.write(json);
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            id = jsonData.getString("id");
            password = jsonData.getString("password");

            super.read(json, jsonData);
        }
    }

    public class AnonymousAccounts extends Accounts
    {
    }

    public class Accounts implements Json.Serializable
    {
        protected ObjectMap<String, Account> accounts;
        protected String currentEnvironment;

        public Accounts()
        {
            accounts = new ObjectMap<>();
            currentEnvironment = null;
        }

        @Override
        public void write(Json json)
        {
            json.writeObjectStart("accounts");

            for (ObjectMap.Entry<String, Account> account : accounts)
            {
                json.writeValue(account.key, account.value);
            }

            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            this.accounts.clear();

            JsonValue accounts = jsonData.get("accounts");
            if (accounts != null && accounts.isObject())
            {
                for (JsonValue acc : accounts)
                {
                    Account account = newAccount();
                    account.read(json, acc);
                    this.accounts.put(acc.name(), account);
                }
            }
        }

        public Account getAccount(String environment)
        {
            if (accounts == null || environment == null)
            {
                return null;
            }

            return accounts.get(environment);
        }

        public Account getAccount()
        {
            return getAccount(currentEnvironment);
        }

        public void clear()
        {
            currentEnvironment = null;
            accounts.clear();
        }

        public void setAccount(String environment, Account account)
        {
            accounts.put(environment, account);
        }

        public void setNewAccountForEnvironment()
        {
            removeAccount();
            setAccount(getCurrentEnvironment(), newAccount());
        }

        public void setCurrentEnvironment(String currentEnvironment)
        {
            this.currentEnvironment = currentEnvironment;
        }

        public String getCurrentEnvironment()
        {
            return currentEnvironment;
        }

        public void removeAccount(String environment)
        {
            accounts.remove(environment);
        }

        public void removeAccount()
        {
            removeAccount(currentEnvironment);
        }
    }

    public class Friend
    {
        private String credential;
        private Texture avatar;
        private String name;
        private String room;
        private boolean online;

        public Texture getAvatar()
        {
            return avatar;
        }

        public String getCredential()
        {
            return credential;
        }

        public String getName()
        {
            return name;
        }

        public void setAvatar(Texture avatar)
        {
            this.avatar = avatar;
        }

        public void setCredential(String credential)
        {
            this.credential = credential;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getRoom()
        {
            return room;
        }

        public void setRoom(String room)
        {
            this.room = room;
        }

        public boolean isOnline()
        {
            return online;
        }

        public void setOnline(boolean online)
        {
            this.online = online;
        }
    }

    public interface WorkshopItemDownloadedFileGetResultCallback
    {
        void success(byte[] data, int length);
        void failed(String reason);
    }

    public interface WorkshopItemFiles
    {
        void request(String name, WorkshopItemDownloadedFileGetResultCallback callback);
    }

    public interface WorkshopItemFileRequestCallback
    {
        void success(WorkshopItemFiles files);
        void failed(String reason);
    }

    public abstract class WorkshopItem
    {
        public abstract String getID();
        public abstract String getTitle();
        public abstract String[] getTags();
        public abstract String getDescription();
        public abstract Date getTimeCreated();
        public abstract Date getTimeUpdated();
        public abstract String getPreviewURL();
        public abstract String getWebURL();
        public abstract void download(WorkshopItemFileRequestCallback callback);
    }

    private ArrayMap<Integer, UserLoader> processors;
    private Preferences preferences;

    private Accounts accounts;

    public GameUser()
    {
        processors = new ArrayMap<>();
        accounts = createAccounts();
        preferences = null;

        initProcessors();
    }

    public Accounts createAccounts()
    {
        return new AnonymousAccounts();
    }

    public void init()
    {
        clear();
    }

    public void clear()
    {
        accounts.clear();
    }

    private void initProcessors()
    {
        /*----------------------- PROFILE_VERSION_2 -----------------------*/

        processors.put(PROFILE_VERSION_2, new UserLoader()
        {
            @Override
            public void read(Preferences preferences)
            {
                String accountsData = preferences.getString("accounts", "{}");

                JsonReader jsonReader = new JsonReader();
                JsonValue value = jsonReader.parse(accountsData);

                accounts.read(new Json(), value);
            }
        });

        /*-----------------------------------------------------------------*/
    }

    public void write()
    {
        preferences.putInteger("version", CURRENT_PROFILE_VERSION);

        /*-----------------------  PROFILE DATA  --------------------------*/

        preferences.putString("accounts", new Json().toJson(this.accounts));

        /*-----------------------------------------------------------------*/

        preferences.flush();
    }

    public void read()
    {
        preferences = Gdx.app.getPreferences(Constants.Files.USER_PROFILE_NAME + '-' + BrainOut.Env.getUniqueId());

        clear();

        final int profileVersion = preferences.getInteger("version", -1);

        UserLoader loader = processors.get(profileVersion);

        if (loader != null)
        {
            loader.read(preferences);
        }
    }

    public Accounts getAccounts()
    {
        return accounts;
    }

    private String generate(int length)
    {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }

        return sb.toString();
    }

    public Account newAccount()
    {
        return new AnonymousAccount(generate(24), generate(32), "");
    }

    public boolean validateName(String value)
    {
        return !value.isEmpty();
    }

    public void release()
    {
        //
    }

    public boolean getFriends(Array<Friend> friends)
    {
        return true;
    }

    public boolean inviteFriend(Friend friend)
    {
        return false;
    }

    public boolean inviteFriendCustom(Friend friend, String context)
    {
        return false;
    }

    public interface WorkshopItemsQueryCallback
    {
        void success(Queue<WorkshopItem> items, int results, int totalResults);
        void failed(String reason);
    }

    public interface WorkshopItemQueryCallback
    {
        void success(WorkshopItem item);
        void failed(String reason);
    }

    public abstract class WorkshopItemsQuery
    {
        public abstract void addRequiredTag(String tag);
        public abstract void addRequiredKeyValueTag(String key, String value);
        public abstract void sendQuery(WorkshopItemsQueryCallback callback);
    }

    public boolean hasWorkshop()
    {
        return false;
    }

    public WorkshopItemsQuery queryMyPublishedWorkshopItems()
    {
        return queryMyPublishedWorkshopItems(1);
    }

    public WorkshopItemsQuery queryMySubscribedWorkshopItems()
    {
        return queryMySubscribedWorkshopItems(1);
    }

    public WorkshopItemsQuery queryMyPublishedWorkshopItems(int page)
    {
        return null;
    }

    public WorkshopItemsQuery queryMySubscribedWorkshopItems(int page)
    {
        return null;
    }

    public void queryWorkshopItem(String id, WorkshopItemQueryCallback callback)
    {}

    public void queryWorkshopItems(Queue<String> ids, WorkshopItemsQueryCallback callback)
    {}

    public interface WorkshopUploadCallback
    {
        void complete(String itemID, String url);
        void needToAcceptWLA(Runnable done);
        void failed(String reason);
    }

    public boolean hasWorkshopLegalTerms()
    {
        return false;
    }

    public String getWorkshopLegalTermsLink()
    {
        return null;
    }

    public void publishWorkshopItem(String name, String description, File preview, ObjectMap<String, File> files,
                                    String[] tags, String comment, WorkshopUploadCallback callback)
    {
    }

    public void updateWorkshopItem(String fileId, File preview, ObjectMap<String, File> files,
                                   String[] tags, String comment, WorkshopUploadCallback callback)
    {
    }
}
