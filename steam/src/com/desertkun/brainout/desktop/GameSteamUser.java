package com.desertkun.brainout.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.codedisaster.steamworks.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.GameUser;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.gs.LoadingState;
import com.desertkun.brainout.menu.impl.IntroMenu;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.online.Matchmaking;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.util.ApplicationInfo;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GameSteamUser extends GameUser implements SteamUserStatsCallback, SteamFriendsCallback, SteamUtilsCallback, SteamUGCCallback, SteamRemoteStorageCallback
{
    private SteamUser steamUser;
    private String key;
    private SteamUserStats steamUserStats;
    private SteamFriends steamFriends;
    private SteamUtils steamUtils;
    private SteamUGC steamUGC;
    private SteamRemoteStorage steamRemoteStorage;

    private ObjectMap<SteamUGCQuery, WorkshopItemsQueryCallback> callbackResponseHandlers;
    private ObjectMap<Long, DownloadWorkshopItemCallback> callbackDownload;
    private Queue<CreateWorkshopItemCallback> createItemCallbacks;
    private Queue<UpdateWorkshopItemCallback> updateItemCallbacks;
    private Queue<Runnable> overlayClodedCallbacks;

    private interface CreateWorkshopItemCallback
    {
        void done(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result);
    }

    private interface UpdateWorkshopItemCallback
    {
        void done(boolean needsToAcceptWLA, SteamResult result);
    }

    private interface DownloadWorkshopItemCallback
    {
        void done(int appID, SteamPublishedFileID publishedFileID, SteamResult result);
    }

    public GameSteamUser()
    {
        callbackResponseHandlers = new ObjectMap<>();
        createItemCallbacks = new Queue<>();
        updateItemCallbacks = new Queue<>();
        overlayClodedCallbacks = new Queue<>();
        callbackDownload = new ObjectMap<>();
    }

    public class SteamAccounts extends Accounts
    {

    }

    public class SteamAccount extends Account
    {
        public SteamAccount()
        {

        }

        @Override
        public String getId()
        {
            return steamUser.getSteamID().toString();
        }

        @Override
        public String getCredential()
        {
            return "steam:" + getId();
        }

        @Override
        public void auth(
                LoginService loginService,
                LoginService.Scopes scopes,
                Request.Fields options,
                LoginService.AuthenticationCallback callback)
        {
            options.put("ticket", key);
            options.put("app_id", String.valueOf(SteamConstants.APP_ID));

            ApplicationInfo applicationInfo = BrainOutClient.Online.getApplicationInfo();

            loginService.authenticate("steam", applicationInfo.gamespace,
                    scopes, options, callback, null, applicationInfo.shouldHaveScopes);
        }
    }

    @Override
    public Accounts createAccounts()
    {
        return new SteamAccounts();
    }

    @Override
    public Account newAccount()
    {
        return new SteamAccount();
    }

    @Override
    public void release()
    {
        super.release();

        if (steamUser != null)
        {
            steamUser.dispose();
        }
    }

    public void initSocial()
    {
        steamUserStats = new SteamUserStats(this);
        steamFriends = new SteamFriends(this);
        steamUtils = new SteamUtils(this);
        steamUGC = new SteamUGC(this);
        steamRemoteStorage = new SteamRemoteStorage(this);
    }

    public void setSteamUser(SteamUser steamUser)
    {
        this.steamUser = steamUser;
    }

    public SteamUser getSteamUser()
    {
        return steamUser;
    }

    public SteamUserStats getSteamUserStats()
    {
        return steamUserStats;
    }

    public SteamUtils getSteamUtils()
    {
        return steamUtils;
    }

    public SteamFriends getSteamFriends()
    {
        return steamFriends;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    // ================================= User Stats =================================

    @Override
    public void onUserStatsReceived(long gameId, SteamID steamIDUser, SteamResult result)
    {

    }

    @Override
    public void onUserStatsStored(long gameId, SteamResult result)
    {

    }

    @Override
    public void onUserStatsUnloaded(SteamID steamIDUser)
    {

    }

    @Override
    public void onUserAchievementStored(
            long gameId, boolean isGroupAchievement, String achievementName, int curProgress, int maxProgress)
    {

    }

    @Override
    public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard, boolean found)
    {

    }

    @Override
    public void onLeaderboardScoresDownloaded(
            SteamLeaderboardHandle leaderboard, SteamLeaderboardEntriesHandle entries, int numEntries)
    {

    }

    @Override
    public void onLeaderboardScoreUploaded(
            boolean success, SteamLeaderboardHandle leaderboard, int score, boolean scoreChanged,
            int globalRankNew, int globalRankPrevious)
    {

    }

    @Override
    public void onGlobalStatsReceived(long gameId, SteamResult result)
    {

    }

    // ================================= Friends =================================

    @Override
    public void onPersonaStateChange(SteamID steamID, SteamFriends.PersonaChange change)
    {

    }

    @Override
    public void onGameOverlayActivated(boolean active)
    {
        if (!active)
        {
            for (Runnable runnable : overlayClodedCallbacks)
            {
                runnable.run();
            }

            overlayClodedCallbacks.clear();
        }
    }

    @Override
    public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend)
    {

    }

    // ================================= Utils =================================

    @Override
    public void onSteamShutdown()
    {

    }

    @Override
    public void onSetPersonaNameResponse(boolean success, boolean localSuccess, SteamResult result)
    {

    }

    @Override
    public void onAvatarImageLoaded(SteamID steamID, int image, int width, int height)
    {

    }

    @Override
    public void onFriendRichPresenceUpdate(SteamID steamIDFriend, int appID)
    {

    }

    public static class ConnectObject
    {
        @Parameter(names = {"--join-room"}, description = "ID of the room to connect to")
        public String ConnectRoomId = null;

        @Parameter(names = {"--free-play-join"}, description = "ID of the freeplay party to connect to")
        public String FreePlayJoinPartyId = null;
    }

    @Override
    public void onGameRichPresenceJoinRequested(SteamID steamIDFriend, String connect)
    {
        ConnectObject connectObject = new ConnectObject();
        new JCommander(connectObject, connect.split(" "));

        GameState topState = BrainOutClient.getInstance().topState();

        if (topState != null && topState.topMenu() instanceof IntroMenu)
        {
            IntroMenu introMenu = ((IntroMenu) topState.topMenu());

            if (introMenu.isCanSwitch())
            {
                introMenu.pop();

                if (!(BrainOutClient.getInstance().topState() instanceof LoadingState))
                {
                    BrainOutClient.getInstance().switchState(new LoadingState());
                }

                DoConnectObject(connectObject);
            }
            else
            {
                introMenu.setAutoContinue(() ->
                {
                    if (!(BrainOutClient.getInstance().topState() instanceof LoadingState))
                    {
                        BrainOutClient.getInstance().switchState(new LoadingState());
                    }

                    DoConnectObject(connectObject);
                });
            }

            return;
        }

        DoConnectObject(connectObject);
    }

    @Override
    public void onGameServerChangeRequested(String server, String password)
    {

    }

    public static void DoConnectObject(ConnectObject connectObject)
    {
        if (connectObject.FreePlayJoinPartyId != null)
        {
            Matchmaking.JoinFreePlay(connectObject.FreePlayJoinPartyId);
            return;
        }

        if (connectObject.ConnectRoomId != null)
        {
            Matchmaking.JoinGame(connectObject.ConnectRoomId, new Matchmaking.JoinGameResult()
            {
                @Override
                public void complete(String roomId)
                {
                    //
                }

                @Override
                public void failed(Request.Result status, Request request)
                {
                    //
                }

                @Override
                public void connectionFailed()
                {

                }
            });

            return;
        }

        BrainOutClient.ClientController.initOnline();
    }

    private class SteamFriend extends Friend
    {
        private SteamID steamID;

        public SteamFriend(SteamID steamID)
        {
            this.steamID = steamID;
        }

        public void setSteamID(SteamID steamID)
        {
            this.steamID = steamID;
        }

        public SteamID getSteamID()
        {
            return steamID;
        }
    }

    @Override
    public boolean getFriends(Array<Friend> friends)
    {
        if (steamFriends == null)
            return false;

        int friendsCount = steamFriends.getFriendCount(SteamFriends.FriendFlags.All);

        for (int i = 0; i < friendsCount; i++)
        {
            SteamID friend = steamFriends.getFriendByIndex(i, SteamFriends.FriendFlags.All);
            String steamCredential = "steam:" + SteamHelper.getSteamIdCredential(friend);

            int avatarIndex = steamFriends.getMediumFriendAvatar(friend);
            int w = steamUtils.getImageWidth(avatarIndex);
            int h = steamUtils.getImageHeight(avatarIndex);

            SteamFriend newFriend = new SteamFriend(friend);

            newFriend.setName(steamFriends.getFriendPersonaName(friend));
            newFriend.setCredential(steamCredential);

            switch (steamFriends.getFriendPersonaState(friend))
            {
                case Online:
                case Away:
                {
                    newFriend.setOnline(true);
                    break;
                }
            }

            String connectTo = steamFriends.getFriendRichPresence(friend, "connect");

            if (connectTo != null && !connectTo.isEmpty() )
            {
                ConnectObject connectObject = new ConnectObject();
                new JCommander(connectObject, connectTo.split(" "));

                newFriend.setRoom(connectObject.ConnectRoomId);
            }

            int size = w * h * 4;

            Avatars.Get(
                new Avatars.DataRequest()
                {
                    @Override
                    public ByteBuffer getData()
                    {
                        ByteBuffer data = ByteBuffer.allocateDirect(size);

                        try
                        {
                            if (!steamUtils.getImageRGBA(avatarIndex, data))
                            {
                                return null;
                            }
                        }
                        catch (SteamException e)
                        {
                            e.printStackTrace();

                            return null;
                        }

                        data.rewind();

                        return data;
                    }

                    @Override
                    public int getWidth()
                    {
                        return w;
                    }

                    @Override
                    public int getHeight()
                    {
                        return h;
                    }
                },
                steamCredential,
                (has, avatar) ->
                {
                    if (has)
                    {
                        newFriend.setAvatar(avatar);
                    }
                });

            friends.add(newFriend);
        }

        friends.sort(new Comparator<Friend>()
        {
            @Override
            public int compare(Friend o1, Friend o2)
            {
                int o1c = (o1.isOnline() ? 1 : 0) + (o1.getRoom() != null ? 1 : 0);
                int o2c = (o2.isOnline() ? 1 : 0) + (o2.getRoom() != null ? 1 : 0);

                return o2c - o1c;
            }
        });

        return true;
    }

    @Override
    public boolean inviteFriend(Friend friend)
    {
        if (!(friend instanceof SteamFriend))
            return false;

        SteamFriend steamFriend = ((SteamFriend) friend);
        SteamID steamID = steamFriend.getSteamID();

        String currentRoom = BrainOutClient.Env.getCurrentRoom();

        if (currentRoom == null)
            return false;

        if (steamFriends == null)
            return false;

        steamFriends.inviteUserToGame(steamID, "--join-room " + currentRoom);

        return true;
    }

    @Override
    public boolean inviteFriendCustom(Friend friend, String context)
    {
        if (!(friend instanceof SteamFriend))
            return false;

        SteamFriend steamFriend = ((SteamFriend) friend);
        SteamID steamID = steamFriend.getSteamID();

        if (steamFriends == null)
            return false;

        steamFriends.inviteUserToGame(steamID, context);

        return true;
    }

    public class SteamWorkshopItem extends WorkshopItem
    {
        private SteamUGCDetails details;
        private String previewUrl;
        private String webUrl;

        public SteamWorkshopItem(SteamUGCDetails details, String previewUrl)
        {
            this.details = details;
            this.previewUrl = previewUrl;
            this.webUrl = "https://steamcommunity.com/sharedfiles/filedetails/?id=" +
                    SteamNativeHandle.getNativeHandle(details.getPublishedFileID());
        }

        @Override
        public String getID()
        {
            return String.valueOf(SteamNativeHandle.getNativeHandle(details.getPublishedFileID()));
        }
        @Override
        public String[] getTags()
        {
            return details.getTags().split(",");
        }

        @Override
        public String getTitle()
        {
            return details.getTitle();
        }

        @Override
        public String getDescription()
        {
            return details.getDescription();
        }

        @Override
        public Date getTimeCreated()
        {
            return new Date(details.getTimeCreated() * 1000L);
        }

        @Override
        public Date getTimeUpdated()
        {
            return new Date(details.getTimeUpdated() * 1000L);
        }

        @Override
        public String getPreviewURL()
        {
            return previewUrl;
        }

        @Override
        public String getWebURL()
        {
            return webUrl;
        }

        @Override
        public void download(WorkshopItemFileRequestCallback callback)
        {
            long fileId = SteamNativeHandle.getNativeHandle(details.getPublishedFileID());

            callbackDownload.put(fileId, (appID, publishedFileID, result) ->
            {
                if (result != SteamResult.OK)
                {
                    callback.failed(result.toString());
                    return;
                }

                SteamUGC.ItemInstallInfo itemInstallInfo = new SteamUGC.ItemInstallInfo();
                if (!steamUGC.getItemInstallInfo(publishedFileID, itemInstallInfo))
                {
                    callback.failed("No install info");
                    return;
                }

                callback.success((name, callback1) ->
                {
                    try
                    {
                        Path path = Paths.get(itemInstallInfo.getFolder()).resolve(name);
                        byte[] data = Files.readAllBytes(path);
                        callback1.success(data, data.length);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        callback1.failed(e.getMessage());
                    }
                });
            });

            steamUGC.downloadItem(details.getPublishedFileID(), true);
        }
    }

    @Override
    public void onUGCQueryCompleted(
        SteamUGCQuery query,
        int numResultsReturned,
        int totalMatchingResults,
        boolean isCachedData,
        SteamResult result)
    {
        WorkshopItemsQueryCallback callback = callbackResponseHandlers.remove(query);

        if (callback == null)
            return;

        if (result != SteamResult.OK)
        {
            steamUGC.releaseQueryUserUGCRequest(query);
            callback.failed(result.toString());
            return;
        }

        Queue<WorkshopItem> items = new Queue<>();

        for (int i = 0; i < numResultsReturned; i++)
        {
            SteamUGCDetails details = new SteamUGCDetails();

            if (!steamUGC.getQueryUGCResult(query, i, details))
                continue;

            String previewUrl = steamUGC.getQueryUGCPreviewURL(query, i);

            WorkshopItem workshopItem = new SteamWorkshopItem(details, previewUrl);
            items.addLast(workshopItem);
        }

        callback.success(items, numResultsReturned, totalMatchingResults);
        steamUGC.releaseQueryUserUGCRequest(query);
    }

    @Override
    public void onSubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result)
    {

    }

    @Override
    public void onUnsubscribeItem(SteamPublishedFileID publishedFileID, SteamResult result)
    {

    }

    @Override
    public void onRequestUGCDetails(SteamUGCDetails details, SteamResult result)
    {

    }

    @Override
    public void onDownloadItemResult(int appID, SteamPublishedFileID publishedFileID, SteamResult result)
    {
        DownloadWorkshopItemCallback handler = callbackDownload.remove(SteamNativeHandle.getNativeHandle(publishedFileID));

        if (handler != null)
        {
            handler.done(appID, publishedFileID, result);
        }
    }

    @Override
    public void onUserFavoriteItemsListChanged(SteamPublishedFileID publishedFileID, boolean wasAddRequest, SteamResult result)
    {

    }

    @Override
    public void onSetUserItemVote(SteamPublishedFileID publishedFileID, boolean voteUp, SteamResult result)
    {

    }

    @Override
    public void onGetUserItemVote(SteamPublishedFileID publishedFileID, boolean votedUp, boolean votedDown, boolean voteSkipped, SteamResult result)
    {

    }

    @Override
    public void onStartPlaytimeTracking(SteamResult result)
    {

    }

    @Override
    public void onStopPlaytimeTracking(SteamResult result)
    {

    }

    @Override
    public void onStopPlaytimeTrackingForAllItems(SteamResult result)
    {

    }

    @Override
    public boolean hasWorkshop()
    {
        return true;
    }

    private class SteamMyWorkshopItemsQuery extends WorkshopItemsQuery
    {
        private final SteamUGCQuery query;

        public SteamMyWorkshopItemsQuery(int page, SteamUGC.UserUGCList kind)
        {
            query = steamUGC.createQueryUserUGCRequest(steamUser.getSteamID().getAccountID(),
                kind,
                SteamUGC.MatchingUGCType.Items,
                SteamUGC.UserUGCListSortOrder.LastUpdatedDesc,
                SteamConstants.APP_ID, SteamConstants.APP_ID,
                page);
        }

        @Override
        public void addRequiredTag(String tag)
        {
            steamUGC.addRequiredTag(query, tag);
        }

        @Override
        public void addRequiredKeyValueTag(String key, String value)
        {
            steamUGC.addRequiredKeyValueTag(query, key, value);
        }

        @Override
        public void sendQuery(WorkshopItemsQueryCallback callback)
        {
            callbackResponseHandlers.put(query, callback);
            steamUGC.sendQueryUGCRequest(query);
        }
    }

    @Override
    public void queryWorkshopItem(String id, WorkshopItemQueryCallback callback)
    {
        long id_;

        try
        {
            id_ = Long.valueOf(id);
        }
        catch (NumberFormatException e)
        {
            callback.failed(e.toString());
            return;
        }

        SteamPublishedFileID publishedFileID = new SteamPublishedFileID(id_);
        SteamUGCQuery query = steamUGC.createQueryUGCDetailsRequest(publishedFileID);
        callbackResponseHandlers.put(query, new WorkshopItemsQueryCallback()
        {
            @Override
            public void success(Queue<WorkshopItem> items, int results, int totalResults)
            {
                if (items.size < 1)
                {
                    callback.failed("Not enough items.");
                    return;
                }

                callback.success(items.get(0));
            }

            @Override
            public void failed(String reason)
            {
                callback.failed(reason);
            }
        });
        steamUGC.sendQueryUGCRequest(query);
    }

    @Override
    public void queryWorkshopItems(Queue<String> ids, WorkshopItemsQueryCallback callback)
    {
        LinkedList<SteamPublishedFileID> publishedFileIDs = new LinkedList<>();

        for (String id : ids)
        {
            long id_;

            try
            {
                id_ = Long.valueOf(id);
            }
            catch (NumberFormatException e)
            {
                continue;
            }

            SteamPublishedFileID publishedFileID = new SteamPublishedFileID(id_);
            publishedFileIDs.addLast(publishedFileID);
        }

        SteamUGCQuery query = steamUGC.createQueryUGCDetailsRequest(publishedFileIDs);
        callbackResponseHandlers.put(query, callback);
        steamUGC.sendQueryUGCRequest(query);
    }

    @Override
    public WorkshopItemsQuery queryMyPublishedWorkshopItems(int page)
    {
        return new SteamMyWorkshopItemsQuery(page, SteamUGC.UserUGCList.Published);
    }

    @Override
    public WorkshopItemsQuery queryMySubscribedWorkshopItems(int page)
    {
        return new SteamMyWorkshopItemsQuery(page, SteamUGC.UserUGCList.Subscribed);
    }

    @Override
    public void onFileShareResult(SteamUGCHandle fileHandle, String fileName, SteamResult result)
    {

    }

    @Override
    public void onDownloadUGCResult(SteamUGCHandle fileHandle, SteamResult result)
    {

    }

    @Override
    public void onPublishFileResult(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result)
    {

    }

    @Override
    public void onUpdatePublishedFileResult(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result)
    {

    }

    @Override
    public void onPublishedFileSubscribed(SteamPublishedFileID publishedFileID, int appID)
    {

    }

    @Override
    public void onPublishedFileUnsubscribed(SteamPublishedFileID publishedFileID, int appID)
    {

    }

    @Override
    public void onPublishedFileDeleted(SteamPublishedFileID publishedFileID, int appID)
    {

    }

    @Override
    public void onFileWriteAsyncComplete(SteamResult result)
    {

    }

    @Override
    public void onFileReadAsyncComplete(SteamAPICall fileReadAsync, SteamResult result, int offset, int read)
    {

    }

    @Override
    public boolean hasWorkshopLegalTerms()
    {
        return true;
    }

    @Override
    public String getWorkshopLegalTermsLink()
    {
        return "https://steamcommunity.com/sharedfiles/workshoplegalagreement";
    }

    @Override
    public void onCreateItem(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result)
    {
        if (createItemCallbacks.size == 0)
            return;

        createItemCallbacks.removeFirst().done(
            publishedFileID, needsToAcceptWLA, result
        );
    }

    @Override
    public void onSubmitItemUpdate(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result)
    {
        if (updateItemCallbacks.size == 0)
            return;

        updateItemCallbacks.removeFirst().done(
                needsToAcceptWLA, result
        );
    }

    @Override
    public void publishWorkshopItem(String name, String description, File preview, ObjectMap<String, File> files,
                                    String[] tags, String comment, WorkshopUploadCallback callback)
    {
        createItemCallbacks.addLast((publishedFileID, needsToAcceptWLA, result) ->
        {
            if (result != SteamResult.OK)
            {
                callback.failed(result.toString());
                return;
            }

            Path workshopDir;

            try
            {
                workshopDir = Files.createTempDirectory("workshop-publish");

                for (ObjectMap.Entry<String, File> entry : files)
                {
                    Files.copy(entry.value.toPath(), workshopDir.resolve(entry.key));
                }
            }
            catch (IOException e)
            {
                callback.failed(e.getMessage());
                return;
            }

            Runnable proceed = () ->
            {
                SteamUGCUpdateHandle handle = steamUGC.startItemUpdate(SteamConstants.APP_ID, publishedFileID);

                steamUGC.setItemTitle(handle, name);

                if (!description.isEmpty())
                    steamUGC.setItemDescription(handle, description);

                steamUGC.setItemVisibility(handle, SteamRemoteStorage.PublishedFileVisibility.Public);
                steamUGC.setItemPreview(handle, preview.getAbsolutePath());
                steamUGC.setItemTags(handle, tags);
                steamUGC.setItemContent(handle, workshopDir.toString());

                updateItemCallbacks.addLast((needsToAcceptWLA1, result1) ->
                {
                    Runnable proceed2 = () ->
                    {
                        if (result1 == SteamResult.OK)
                        {
                            String fileId = String.valueOf(SteamNativeHandle.getNativeHandle(publishedFileID));
                            callback.complete(fileId,
                                "https://steamcommunity.com/sharedfiles/filedetails/?id=" + fileId);
                        }
                        else
                        {
                            callback.failed(result1.toString());
                        }
                    };

                    if (needsToAcceptWLA1)
                    {
                        callback.needToAcceptWLA(proceed2);
                    }
                    else
                    {
                        proceed2.run();
                    }
                });

                steamUGC.submitItemUpdate(handle, comment);
            };

            if (needsToAcceptWLA)
            {
                callback.needToAcceptWLA(proceed);
            }
            else
            {
                proceed.run();
            }
        });

        steamUGC.createItem(SteamConstants.APP_ID, SteamRemoteStorage.WorkshopFileType.Community);
    }

    @Override
    public void updateWorkshopItem(String fileId, File preview, ObjectMap<String, File> files, String[] tags,
                                   String comment, WorkshopUploadCallback callback)
    {

        Path workshopDir;

        try
        {
            workshopDir = Files.createTempDirectory("workshop-publish");

            for (ObjectMap.Entry<String, File> entry : files)
            {
                Files.copy(entry.value.toPath(), workshopDir.resolve(entry.key));
            }
        }
        catch (IOException e)
        {
            callback.failed(e.getMessage());
            return;
        }

        SteamPublishedFileID steamFileId = new SteamPublishedFileID(Long.valueOf(fileId));

        SteamUGCUpdateHandle handle = steamUGC.startItemUpdate(SteamConstants.APP_ID, steamFileId);

        if (!handle.isValid())
        {
            callback.failed("Handle is not valid");
            return;
        }

        steamUGC.setItemPreview(handle, preview.getAbsolutePath());
        steamUGC.setItemTags(handle, tags);
        steamUGC.setItemContent(handle, workshopDir.toString());

        updateItemCallbacks.addLast((needsToAcceptWLA1, result1) ->
        {
            Runnable proceed2 = () ->
            {
                if (result1 == SteamResult.OK)
                {
                    callback.complete(fileId,
                        "https://steamcommunity.com/sharedfiles/filedetails/?id=" + fileId);
                }
                else
                {
                    callback.failed(result1.toString());
                }
            };

            if (needsToAcceptWLA1)
            {
                callback.needToAcceptWLA(proceed2);
            }
            else
            {
                proceed2.run();
            }
        });

        steamUGC.submitItemUpdate(handle, comment);
    }

    public void openOverlay(String uri, Runnable done)
    {
        getSteamFriends().activateGameOverlayToWebPage(uri);
        overlayClodedCallbacks.addLast(done);
    }
}
