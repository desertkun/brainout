package com.desertkun.brainout.events;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public abstract class Event implements Pool.Poolable
{
    public enum ID
    {
        simple,
        damage,
        damageBlock,
        damaged,

        addImpulse,
        setSpeed,
        launchBullet,
        step,
        launchEffect,
        launchAttachedEffect,

        csGame, controller,
        packageDownload,
        error,
        chat,
        setInstrument,
        resetInstrument,
        hookInstrument,

        setMyPlayer,
        destroy,
        destroyBlock,
        onZeroHealth,

        kill,
        killedBy,
        notify,
        aim,
        earn,
        gameController,
        updated,
        playStateUpdated,
        consumable,
        ammoLoaded,
        handleBlock,
        setBlock,
        activeAction,
        activeChangeDimension,
        selectSlot,
        selectPreviousSlot,
        componentUpdated,
        activeContentReceivedNotice,

        flagTaken,
        collide,
        respawnIn,
        modeWillFinish, physicsContact,

        instrumentActivate,
        physicsUpdated,
        popup,

        newRemoteClient,
        remoteClientLeft,
        remoteClientUpdated,
        teamVisibilityUpdated,
        mapListReceived,

        settingsUpdated,
        screenSizeUpdated,
        hitConfirmed,

        detected,
        animationAction,
        instrumentAction,
        setDirty,
        customInstrumentEffect,
        onScreenMessage,

        ownerChanged,
        caseOpenResult,
        promoCodeResult,
        partyStartResult,
        newOrderResult,

        playerWon,
        teamWon,
        playerSaved,
        badgeRead,

        itemTaking,
        onlineEventsUpdated,
        onlineEventUpdated,
        onlineEventClaimResult,
        weaponStateUpdated,

        statUpdated,
        openChat,
        achievementCompleted,
        socialMessage,
        socialMessageUpdated,
        socialMessageDeleted,

        freePlaySummary,
        itemAction,
        voice,

        freePlayItemTakenOut,
        freePlayItemActivated,
        freePlayItemPainted,
        freePlayItemBurned,
        freePlayItemUsed,
        freePlayEnemyOfKindKilled,
        freePlayPartnerRevived,
        freePlayWeaponUpgraded,
        freePlayMinuteSpent,
        activeActivateData,
        blockHitConfirmation,

        spectatorFlag,
        enterPortal,
        instrumentAnimationAction,
        freePlayCards,
        freePlayRadio
    }

    public abstract ID getID();

    protected static <T extends Event> T obtain(Class<T> classOf)
    {
        Pool<T> pool = Pools.get(classOf, 65535);

        if (pool == null)
            return null;

        try
        {
            return pool.obtain();
        }
        catch (IllegalStateException ignored)
        {
            return null;
        }
    }

    protected static <T extends Event> T obtain(Class<T> classOf, int max)
    {
        return Pools.get(classOf, max).obtain();
    }

    public void free()
    {
        Pools.free(this);
    }

    public void reset()
    {

    }
}
