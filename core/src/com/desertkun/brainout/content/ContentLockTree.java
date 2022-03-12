package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.LocalizedString;
import com.desertkun.brainout.utils.UnlockSubscription;

import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.ContentLockTree")
public class ContentLockTree extends Content
{
    private ObjectMap<String, LockItem> items;

    public enum Visibility
    {
        visible,
        hidden,
        visibleOnUpdate,
        diff
    }

    public interface ItemUnlocked
    {
        void unlocked(OwnableContent content);
    }

    public static class Subscription extends UnlockSubscription
    {
        public Subscription()
        {
        }

        public void addContentSubscriber(OwnableContent ownableContent, ItemUnlocked onUnlocked)
        {
            LockItem lockItem = ownableContent.getLockItem();

            if (lockItem == null) return;

            lockItem.subscribe(this, ownableContent, onUnlocked);
        }

        public void update(String item)
        {
            updatePublisher(item);
        }

        public void init()
        {
            updatePublishers();
        }
    }

    public static class LockItem implements Json.Serializable
    {
        private String unlockFor;
        private String icon;
        private String name;
        private int param;
        private int displayOffset;
        private LocalizedString unlockTitle;
        private LocalizedString unlockObject;
        private Visibility visibility;
        private boolean notify;

        public LockItem(String name)
        {
            unlockTitle = new LocalizedString();
            unlockObject = null;
            visibility = Visibility.visible;
            icon = null;
            this.name = name;
        }

        @Override
        public void write(Json json)
        {

        }

        private String parseUnlockFor(JsonValue f)
        {
            if (f != null)
            {
                if (f.isString())
                {
                    return f.asString();
                }
                else if (f.isArray())
                {
                    Array<String> asArray = new Array<>(f.asStringArray());
                    return asArray.toString("-");
                }
                else
                {
                    return "";
                }
            }
            else
            {
                return "";
            }
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            if (jsonData.has("hide"))
            {
                this.visibility = jsonData.getBoolean("hide", false) ? Visibility.hidden : Visibility.visible;
            }
            else
            {
                if (jsonData.has("visibility"))
                {
                    this.visibility = Visibility.valueOf(jsonData.getString("visibility"));
                }
            }

            if (jsonData.has("icon"))
            {
                this.icon = jsonData.getString("icon");
            }

            this.unlockFor = parseUnlockFor(jsonData.get("for"));
            this.param = jsonData.getInt("param", 0);
            this.displayOffset = jsonData.getInt("displayOffset", 0);
            this.notify = jsonData.getBoolean("notify", true);

            if (jsonData.has("unlockTitle"))
            {
                this.unlockTitle.set(jsonData.getString("unlockTitle"));
            }

            if (jsonData.has("unlockObject"))
            {
                this.unlockObject = new LocalizedString();
                this.unlockObject.set(jsonData.getString("unlockObject"));
            }
        }

        public boolean hasDiff()
        {
            return visibility == Visibility.diff;
        }

        public int getDisplayOffset()
        {
            return displayOffset;
        }

        public String getLockIcon()
        {
            return icon;
        }

        public int getParam()
        {
            return param;
        }

        public String getUnlockFor()
        {
            return unlockFor;
        }

        public String getDiffUnlockFor()
        {
            return unlockFor + "-d-" + name;
        }

        public boolean hasDiffStarted(UserProfile profile)
        {
            return profile.hasStat(getDiffUnlockFor());
        }

        public int getUnlockDiffValue(UserProfile profile, int def)
        {
            return ((int)(float) profile.getStats().get(getDiffUnlockFor(), (float) def));
        }

        public int getUnlockValue(UserProfile profile, int def)
        {
            int have = profile.getInt(getUnlockFor(), def);

            if (hasDiff())
            {
                if (!hasDiffStarted(profile))
                {
                    // if it's a diff value, do nto start counting until the diff value has been marked
                    return 0;
                }

                have -= profile.getInt(getDiffUnlockFor(), have);
            }

            return have;
        }

        public String getUnlockTitle(int param)
        {
            if (unlockObject != null)
            {
                return unlockTitle.get(unlockObject.get());
            }

            return unlockTitle.get(String.valueOf(param));
        }

        public String getUnlockTitle()
        {
            if (unlockObject != null)
            {
                return unlockTitle.get(unlockObject.get());
            }

            return unlockTitle.get();
        }

        public boolean isUnlockTitleValid()
        {
            return unlockTitle.isValid();
        }

        public Visibility getVisibility()
        {
            return visibility;
        }

        public boolean isNotify()
        {
            return notify;
        }

        public void subscribe(Subscription subscription, OwnableContent ownableContent, ItemUnlocked onUnlocked)
        {
            final String contentId = ownableContent.getID();

            subscription.addSubscriber(getUnlockFor(),
                new UnlockSubscription.Subscriber(getParam())
            {
                @Override
                public void complete()
                {
                    OwnableContent oc = BrainOut.ContentMgr.get(contentId, OwnableContent.class);
                    if (oc == null)
                        return;

                    onUnlocked.unlocked(oc);
                }
            });
        }

        public boolean isDiffValid(UserProfile userProfile)
        {
            if (hasDiff())
            {
                return hasDiffStarted(userProfile);
            }

            return true;
        }

        public boolean canSubscribe(UserProfile userProfile)
        {
            return true;
        }

        public boolean startDiff(UserProfile userProfile)
        {
            if (!hasDiff())
                return false;

            if (hasDiffStarted(userProfile))
                return false;

            if (isUnlocked(userProfile))
                return false;

            userProfile.setStat(getDiffUnlockFor(), userProfile.getStats().get(getUnlockFor(), 0.0f));
            return true;
        }

        public boolean isUnlocked(UserProfile userProfile)
        {
            if (hasDiff())
            {
                return isValid() && isDiffValid(userProfile) &&
                    userProfile.getStats().get(getUnlockFor(), 0.0f) -
                    userProfile.getStats().get(getDiffUnlockFor(), userProfile.getStats().get(getUnlockFor(), 0.0f)) >= getParam();
            }

            return isValid() && userProfile.getStats().get(getUnlockFor(), 0.0f) >= getParam();
        }

        public boolean isUnlockStarted(UserProfile userProfile)
        {
            return isValid() && userProfile.getStats().get(getUnlockFor(), 0.0f) > 0;
        }

        public boolean isDiffUnlockStarted(UserProfile userProfile)
        {
            return isValid() && isDiffValid(userProfile) && userProfile.getStats().get(getUnlockFor(), 0.0f) > 0;
        }

        public boolean isValid()
        {
            return !getUnlockFor().equals("");
        }

        public boolean isVisible(UserProfile userProfile)
        {
            switch (visibility)
            {
                case visible:
                {
                    return true;
                }
                case hidden:
                {
                    return isUnlocked(userProfile);
                }
                case visibleOnUpdate:
                {
                    return isUnlockStarted(userProfile);
                }
                case diff:
                {
                    return isDiffUnlockStarted(userProfile);
                }
            }

            return true;
        }

        public void completeDiff(UserProfile profile)
        {
            if (!hasDiff())
            {
                return;
            }

            float oldValue = profile.getStats().get(getUnlockFor(), 0.0f);
            float left = getParam() - (oldValue -
                profile.getStats().get(getDiffUnlockFor(), profile.getStats().get(getUnlockFor(), 0.0f)));

            profile.getStats().put(getDiffUnlockFor(), profile.getStats().get(getDiffUnlockFor()) - left);
            profile.statUpdated(getUnlockFor(), left, left - oldValue);
        }
    }

    public ContentLockTree()
    {
        this.items = new ObjectMap<>();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("items"))
        {
            for (JsonValue value : jsonData.get("items"))
            {
                LockItem item = new LockItem(value.name());
                item.read(json, value);

                this.items.put(value.name(), item);
            }
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();

        this.items.clear();
    }

    public boolean isItemUnlocked(OwnableContent content, UserProfile userProfile)
    {
        LockItem item = items.get(content.getID());

        return item == null || item.isUnlocked(userProfile);
    }

    public boolean hasLock(OwnableContent content)
    {
        return items.get(content.getID()) != null;
    }

    public LockItem getItem(OwnableContent content)
    {
        return items.get(content.getID());
    }

    public static ContentLockTree getInstance()
    {
        return (ContentLockTree)BrainOut.ContentMgr.get(Constants.Core.UNLOCK_TREE);
    }

    public static String GetComplexValue(String category, String item)
    {
        return category + "-" + item;
    }

    public Subscription subscribe()
    {
        return new Subscription();
    }
}
