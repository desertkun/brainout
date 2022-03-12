package com.desertkun.brainout.mode.payload;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.ContentND;
import com.desertkun.brainout.common.msg.server.MadeFriendsMsg;
import com.desertkun.brainout.common.msg.server.QuestTaskProgress;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.content.quest.Tree;
import com.desertkun.brainout.content.quest.task.ServerTask;
import com.desertkun.brainout.content.quest.task.Task;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.online.Reward;
import com.desertkun.brainout.online.ServerReward;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.FPUtils;
import org.json.JSONObject;

import java.util.function.Consumer;

public class FreePayload extends ModePayload
{
    private Array<Quest> activeQuests;
    private ObjectSet<Quest> justCompleteQuests;
    private ObjectSet<Task> updatedTasks;
    private ObjectSet<Task> justCompleteTasks;
    private boolean hasPartyMembers;
    private ObjectMap<String, Object> custom;
    private FriendGroup friends;
    private float checkFriends;
    private long timeSinceHadFriend;
    private boolean friendly;
    private boolean exited;
    private float teamLandingTimer;
    private int teamLandingTargetId;
    private ObjectMap<String, Map> personalMaps;

    public static class FriendGroup
    {
        private static int NEXT_ID = 0;
        private IntSet friends;
        private int id;

        public FriendGroup(int owner)
        {
            id = NEXT_ID++;
            friends = new IntSet();
            friends.add(owner);
        }

        public int getId()
        {
            return id;
        }

        public void merge(FriendGroup from)
        {
            friends.addAll(from.friends);
        }

        public boolean isFriend(int id)
        {
            return friends.contains(id);
        }

        public int count()
        {
            return friends.size;
        }

        public void addFriend(int id)
        {
            friends.add(id);
        }

        public void removeFriend(int id)
        {
            friends.remove(id);
        }

        public IntSet getFriends()
        {
            return friends;
        }
    }

    public FreePayload(Client playerClient)
    {
        super(playerClient);

        activeQuests = new Array<>();
        justCompleteQuests = new ObjectSet<>();
        justCompleteTasks = new ObjectSet<>();
        updatedTasks = new ObjectSet<>();
        custom = new ObjectMap<>();
        friendly = false;
        teamLandingTimer = 0;
    }

    public boolean isExited()
    {
        return exited;
    }

    public void setExited(boolean exited)
    {
        this.exited = exited;
    }

    public void setCustom(String key, Object value)
    {
        this.custom.put(key, value);
    }

    public void setCustomBool(String key, boolean value)
    {
        this.custom.put(key, value);
    }

    public Object getCustom(String key, Object default_)
    {
        return this.custom.get(key, default_);
    }

    public boolean getCustomBool(String key, boolean default_)
    {
        return (Boolean)this.custom.get(key, default_);
    }

    public ObjectSet<Quest> getJustCompleteQuests()
    {
        return justCompleteQuests;
    }

    public ObjectSet<Task> getUpdatedTasks()
    {
        return updatedTasks;
    }

    public Array<Quest> getActiveQuests()
    {
        return activeQuests;
    }

    public boolean hasQuestJustCompleted(Quest quest)
    {
        return justCompleteQuests.contains(quest);
    }

    public boolean hasJustCompleteTasks(Quest quest)
    {
        for (Task task : justCompleteTasks)
        {
            if (task.getQuest() == quest)
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasUpdatedTasks(Quest quest)
    {
        for (Task task : updatedTasks)
        {
            if (task.getQuest() == quest)
            {
                return true;
            }
        }

        return false;
    }

    public void getJustCompleteTasks(Consumer<Task> tasks, Quest ofQuest)
    {
        for (Task task : justCompleteTasks)
        {
            if (task.getQuest() == ofQuest)
            {
                tasks.accept(task);
            }
        }
    }

    public void getUpdatedTasks(Consumer<Task> tasks, Quest ofQuest)
    {
        for (Task task : updatedTasks)
        {
            if (task.getQuest() == ofQuest)
            {
                tasks.accept(task);
            }
        }
    }

    @Override
    public void init()
    {
        //
    }

    public boolean hasPartyMembers()
    {
        return hasPartyMembers;
    }

    public void setupQuests()
    {
        hasPartyMembers = checkIfHasPartyMembers();

        activeQuests.clear();

        Client client = getPlayerClient();
        if (!(client instanceof PlayerClient))
            return;
        PlayerClient playerClient = ((PlayerClient) client);

        UserProfile profile = playerClient.getProfile();

        for (Tree tree: BrainOutServer.ContentMgr.queryContent(Tree.class))
        {
            if (!tree.isActive(profile, playerClient.getAccount()))
                continue;

            Quest current;

            do
            {
                current = tree.getCurrentQuest(profile, playerClient.getAccount());

                if (current == null)
                    continue;

                if (current.isCoop() && !hasPartyMembers())
                {
                    getPlayerClient().log("Skipping quest " + current.getID() + " because it is coop and no party!");
                    continue;
                }

                if (current.isComplete(profile, playerClient.getAccount()) && !current.hasBeenCompleted(profile, playerClient.getAccount()))
                {
                    getPlayerClient().log("Awarding on stuck quest " + current.getID());
                    playerQuestComplete(current);
                }
                else
                {
                    break;
                }
            } while (true);

            activeQuests.add(current);
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();
        if (gameMode != null && gameMode.getRealization() instanceof ServerFreeRealization)
        {
            ServerFreeRealization free = ((ServerFreeRealization) gameMode.getRealization());

            for (Quest quest : activeQuests)
            {
                for (ObjectMap.Entry<String, Task> tasks : quest.getTasks())
                {
                    Task task = tasks.value;

                    if (!(task instanceof ServerTask))
                        continue;

                    ServerTask serverTask = ((ServerTask) task);

                    serverTask.started(free, playerClient);
                }
            }
        }
    }

    public void questEvent(com.desertkun.brainout.events.Event event, boolean free)
    {
        try
        {
            for (Quest quest : activeQuests)
            {
                if (quest.onEvent(event))
                {
                    return;
                }
            }
        }
        finally
        {
            if (free)
            {
                event.free();
            }
        }
    }

    public boolean isQuestActive(Quest quest)
    {
        return activeQuests.contains(quest, true);
    }

    public void playerQuestComplete(Quest quest)
    {
        Client client = getPlayerClient();
        if (!(client instanceof PlayerClient))
            return;
        PlayerClient playerClient = ((PlayerClient) client);

        playerClient.log("Quest completed: " + quest.getID());

        if (quest.isCoop() && quest.isAwardPartner() &&
                playerClient.getPartyId() != null && !playerClient.getPartyId().isEmpty())
        {
            GameMode gameMode = BrainOutServer.Controller.getGameMode();
            if (gameMode != null && gameMode.getRealization() instanceof ServerFreeRealization)
            {
                ServerFreeRealization free = ((ServerFreeRealization) gameMode.getRealization());

                ServerFreeRealization.Party party = free.getParty(playerClient.getPartyId());

                for (ObjectMap.Entry<String, PlayerClient> entry : party.getMembers())
                {
                    if (entry.key.equals(playerClient.getAccount()))
                        continue;

                    PlayerClient friend = entry.value;

                    if (friend.getModePayload() instanceof FreePayload)
                    {
                        ((FreePayload) friend.getModePayload()).awardCompleteQuest(quest);
                    }
                }
            }
        }

        quest.complete(playerClient.getProfile());

        awardCompleteQuest(quest);
    }

    private void awardCompleteQuest(Quest quest)
    {
        if (quest.isPerTargetItemReward())
            return;

        Client client = getPlayerClient();
        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        justCompleteQuests.add(quest);

        if (!quest.isPerTaskReward())
        {
            for (Reward reward : quest.getRewards())
            {
                if (!(reward instanceof ServerReward))
                    continue;

                ServerReward serverReward = ((ServerReward) reward);

                serverReward.apply(playerClient, false);
            }
        }

        if (playerClient.isAllowDrop())
        {
            playerClient.notify(NotifyAward.none, 0, NotifyReason.questComplete, NotifyMethod.message,
                    new ContentND(quest));
        }

        if (playerClient.getProfile() != null)
        {
            playerClient.getProfile().setDirty();
            playerClient.getProfile().doSave();
        }
    }

    private boolean checkIfHasPartyMembers()
    {
        Client client = getPlayerClient();
        if (!(client instanceof PlayerClient))
            return false;

        PlayerClient playerClient = ((PlayerClient) client);

        if (playerClient.getPartyId() != null && !playerClient.getPartyId().isEmpty())
        {
            GameMode gameMode = BrainOutServer.Controller.getGameMode();
            if (gameMode != null && gameMode.getRealization() instanceof ServerFreeRealization)
            {
                ServerFreeRealization free = ((ServerFreeRealization) gameMode.getRealization());

                ServerFreeRealization.Party party = free.getParty(playerClient.getPartyId());

                if (party != null)
                {
                    return party.getMembers().size >= 2;
                }
            }
        }

        return false;
    }

    public int triggerQuestTask(Task task, int amount)
    {
        Client client = getPlayerClient();
        if (!(client instanceof PlayerClient))
            return 0;
        PlayerClient playerClient = ((PlayerClient) client);
        UserProfile userProfile = playerClient.getProfile();

        if (userProfile == null)
            return 0;

        Quest quest = task.getQuest();

        if (quest == null)
            return 0;

        if (quest.isCoop() && !hasPartyMembers())
            return 0;

        if (!task.isComplete(userProfile, playerClient.getAccount()))
        {
            updatedTasks.add(task);

            Quest.TaskTriggerResult res = task.trigger(userProfile, amount, playerClient.getAccount());
            if (res != null)
            {
                if (res.used > 0)
                {
                    if (quest.isPerTargetItemReward())
                    {
                        for (Reward reward : quest.getRewards())
                        {
                            if (!(reward instanceof ServerReward))
                                continue;

                            ServerReward serverReward = ((ServerReward) reward);

                            for (int i = 0; i < res.used; i++)
                            {
                                serverReward.apply(playerClient, false);
                            }
                        }
                    }

                    playerClient.sendTCP(new QuestTaskProgress(quest, task.getId(),
                        task.getProgress(userProfile, playerClient.getAccount())));
                }

                if (res.completed)
                {
                    taskHaveBeenCompleted(task);
                }

                return res.used;
            }
        }

        return 0;
    }

    private void taskHaveBeenCompleted(Task task){
        Client client = getPlayerClient();
        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        UserProfile userProfile = playerClient.getProfile();
        if (userProfile == null)
            return;

        Quest quest = task.getQuest();

        if (quest == null)
            return;

        if (quest.isCoop() && !hasPartyMembers())
            return;

        justCompleteTasks.add(task);

        playerClient.log("Quest task completed: " + task.getId());

        if (quest.isComplete(userProfile, playerClient.getAccount()))
        {
            playerQuestComplete(quest);
        }

        if (quest.isPerTargetItemReward())
        {
            return;
        }

        if (quest.isPerTaskReward())
        {
            for (Reward reward : quest.getRewards())
            {
                if (!(reward instanceof ServerReward))
                    continue;

                ServerReward serverReward = ((ServerReward) reward);
                serverReward.apply(playerClient, false);
            }
        }
    }

    public void questEvent(com.desertkun.brainout.events.Event event)
    {
        questEvent(event, true);
    }

    @Override
    public void release()
    {
        activeQuests.clear();
        justCompleteQuests.clear();
        justCompleteTasks.clear();
        updatedTasks.clear();
        custom.clear();
        personalMaps.clear();
    }

    public void resetUpdatedAndCompletedTasks()
    {
        updatedTasks.clear();
        justCompleteTasks.clear();
        justCompleteQuests.clear();

        setupQuests();
    }

    public void setFriendly(boolean friendly)
    {
        this.friendly = friendly;
    }

    public boolean isFriendly()
    {
        return friendly;
    }

    public boolean isFriend(Client client)
    {
        if (friends == null)
            return false;

        return friends == ((FreePayload) client.getModePayload()).friends;
    }

    public int getFriendsCount()
    {
        if (friends == null)
            return 0;

        return friends.count();
    }

    public void addFriend(PlayerClient client)
    {
        if (friends == null)
            friends = new FriendGroup(getPlayerClient().getId());

        if (isFriend(client))
            return;

        FreePayload pp = ((FreePayload) client.getModePayload());
        if (pp.friends != null)
        {
            IntSet.IntSetIterator pit = pp.friends.friends.iterator();
            while (pit.hasNext)
            {
                int f = pit.next();

                IntSet.IntSetIterator it = friends.friends.iterator();
                while (it.hasNext)
                {
                    int f2 = it.next();
                    Client clientInGroup = BrainOutServer.Controller.getClients().get(f);
                    Client clientInGroup2 = BrainOutServer.Controller.getClients().get(f2);
                    if (clientInGroup instanceof PlayerClient && clientInGroup2 instanceof PlayerClient)
                    {
                        ((PlayerClient) clientInGroup).sendTCP(new MadeFriendsMsg(f2, true));
                        ((PlayerClient) clientInGroup2).sendTCP(new MadeFriendsMsg(f, true));
                    }
                }

            }

            merge(pp);
        }
        else
        {
            IntSet.IntSetIterator it = friends.friends.iterator();
            while (it.hasNext)
            {
                int f = it.next();
                Client clientInGroup = BrainOutServer.Controller.getClients().get(f);
                if (clientInGroup instanceof PlayerClient)
                {
                    ((PlayerClient) clientInGroup).sendTCP(new MadeFriendsMsg(client.getId(), true));
                    client.sendTCP(new MadeFriendsMsg(f, true));
                }
            }

            friends.addFriend(client.getId());
        }

        pp.friends = friends;

        BrainOutServer.Controller.sendRemotePlayers();
    }

    public FriendGroup getFriends()
    {
        return friends;
    }

    public void merge(FreePayload friendGroup)
    {
        this.friends.merge(friendGroup.getFriends());

        IntSet.IntSetIterator it = friendGroup.friends.friends.iterator();
        while (it.hasNext)
        {
            int f = it.next();
            Client client = BrainOutServer.Controller.getClients().get(f);
            if (client instanceof PlayerClient)
            {
                ((FreePayload) client.getModePayload()).friends = this.friends;
            }
        }

        friendGroup.friends = this.friends;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        checkFriends -= dt;
        if (checkFriends < 0)
        {
            checkFriends = 1.0f;

            if (friends != null)
            {
                checkFriends();
            }

            connectFriends();
        }

        if (teamLandingTimer > 0)
        {
            teamLandingTimer -= dt;
            if (teamLandingTimer < 0) teamLandingTimer = 0;
        }
    }

    public void breakUp()
    {
        if (friends == null)
            return;

        IntSet.IntSetIterator it = friends.friends.iterator();
        while (it.hasNext)
        {
            int f = it.next();
            if (f == getPlayerClient().getId())
                continue;

            Client client = BrainOutServer.Controller.getClients().get(f);
            if (client == null)
                continue;

            ((PlayerClient)client).sendTCP(new MadeFriendsMsg(getPlayerClient().getId(), false));
            ((PlayerClient) getPlayerClient()).sendTCP(new MadeFriendsMsg(f, false));
        }

        friends.removeFriend(getPlayerClient().getId());
        friends = null;

        BrainOutServer.Controller.sendRemotePlayers();
    }

    private static IntSet toRemove = new IntSet();
    private static IntSet toAdd = new IntSet();

    private void checkFriends()
    {
        Client me = getPlayerClient();
        if (!me.isAlive())
            return;
        PlayerData myPlayer = me.getPlayerData();
        if (myPlayer == null)
            return;

        toRemove.clear();
        toAdd.clear();

        {
            boolean good = false;

            IntSet.IntSetIterator it = friends.friends.iterator();
            while (it.hasNext)
            {
                int f = it.next();

                if (f == getPlayerClient().getId())
                    continue;

                Client client = BrainOutServer.Controller.getClients().get(f);
                if (client == null || !client.isAlive())
                {
                    toRemove.add(f);
                    break;
                }

                PlayerData otherPlayer = client.getPlayerData();
                if (otherPlayer == null)
                    continue;

                if (!(client.getModePayload() instanceof FreePayload))
                    return;

                if (myPlayer.getDimension().equals(otherPlayer.getDimension()) &&
                        Vector2.dst2(myPlayer.getX(), myPlayer.getY(), otherPlayer.getX(), otherPlayer.getY()) < 30 * 30)
                {
                    good = true;
                    break;
                }

                if (FPUtils.isPlayersHasWalkietalkieContact(myPlayer, otherPlayer))
                {
                    good = true;
                    break;
                }
            }

            if (good)
            {
                timeSinceHadFriend = System.currentTimeMillis() + 30000;
            }
            else
            {
                if (System.currentTimeMillis() > timeSinceHadFriend)
                {
                    breakUp();
                    return;
                }
            }
        }

        IntSet.IntSetIterator fit = toRemove.iterator();
        if (fit.hasNext)
        {
            int id = fit.next();
            friends.friends.remove(id);
        }
    }

    private void connectFriends()
    {
        if (!isFriendly())
            return;

        Client me = getPlayerClient();
        if (!(me instanceof PlayerClient))
            return;
        if (!me.isAlive())
            return;
        PlayerData myPlayer = me.getPlayerData();
        if (myPlayer == null)
            return;

        toAdd.clear();

        myPlayer.getMap().countClosestActiveForTag(30, myPlayer.getX(),
            myPlayer.getY(), PlayerData.class, Constants.ActiveTags.PLAYERS, new Map.Predicate()
        {
            @Override
            public boolean match(ActiveData activeData)
            {
                int player = activeData.getOwnerId();
                Client client = BrainOutServer.Controller.getClients().get(player);
                if (!(client instanceof PlayerClient))
                    return false;

                if (isFriend(client))
                    return false;

                FreePayload payload = ((FreePayload) client.getModePayload());

                if (!payload.friendly)
                    return false;

                if (payload.friends != null && payload.friends.friends.size >= 3)
                    return false;

                toAdd.add(player);

                return true;
            }
        });

        ServerFreeRealization free = ((ServerFreeRealization) BrainOutServer.Controller.getGameMode().getRealization());
        IntSet.IntSetIterator fit = toAdd.iterator();
        if (fit.hasNext)
        {
            int player = fit.next();
            PlayerClient playerWith = (PlayerClient)BrainOutServer.Controller.getClients().get(player);
            FreePayload otherFreePayload = ((FreePayload) playerWith.getModePayload());

            otherFreePayload.addFriend(((PlayerClient) me));

            if (playerWith.getPartyId() != null)
            {
                ServerFreeRealization.Party party = free.getParty(playerWith.getPartyId());
                if (party != null)
                {
                    for (ObjectMap.Entry<String, PlayerClient> member : party.getMembers())
                    {
                        if (member.value == playerWith)
                            continue;

                        if (member.value.getModePayload() instanceof FreePayload)
                        {
                            ((FreePayload) member.value.getModePayload()).addFriend(((PlayerClient) me));
                        }
                    }
                }
            }

            if (me.getPartyId() != null)
            {
                ServerFreeRealization.Party party = free.getParty(me.getPartyId());
                for (ObjectMap.Entry<String, PlayerClient> member : party.getMembers())
                {
                    if (member.value == me)
                        continue;

                    if (member.value.getModePayload() instanceof FreePayload)
                    {
                        ((FreePayload) member.value.getModePayload()).addFriend(playerWith);
                    }
                }
            }
        }

        toRemove.clear();
    }

    public void getInfo(JSONObject info)
    {
        if (friends != null)
        {
            info.put("friend", friends.getId());
        }

        info.put("friendly", friendly);

        Client client = getPlayerClient();
        if (client instanceof PlayerClient)
        {
            info.put("karma", ((PlayerClient) client).getProfile().getInt("karma", 0));
        }
    }

    public void nonFriendAction()
    {
        if (!friendly)
            return;

        setFriendly(false);

        BrainOutServer.PostRunnable(() -> getPlayerClient().sendRemotePlayers(false));
    }

    public void updateTeamLandingTimer(int id)
    {
        teamLandingTargetId = id;
        teamLandingTimer = Constants.Other.TEAM_LANDING_TIMER;
    }

    public boolean isTeamLandingTimer()
    {
        return teamLandingTimer > 0;
    }

    public int getTeamLandingTargetId()
    {
        return teamLandingTargetId;
    }

    public ObjectMap<String, Map> getPersonalMaps()
    {
        return personalMaps;
    }

    public void setPersonalMaps(ObjectMap<String, Map> personalMaps)
    {
        this.personalMaps = personalMaps;
    }
}
