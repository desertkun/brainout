package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.*;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.CampFireFuelComponent;
import com.desertkun.brainout.content.components.CampFireStarterComponent;
import com.desertkun.brainout.content.components.ServerCampfireActivatorComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.ServerCampFireComponentData;
import com.desertkun.brainout.data.components.ServerCampfireActivatorComponentData;
import com.desertkun.brainout.data.components.ServerPortalComponentData;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeFree;
import com.desertkun.brainout.utils.RandomValue;

public class TaskSeekFire extends Task
{
    private final IntSet weaponBlackList;
    private float check;

    public TaskSeekFire(TaskStack stack, IntSet weaponBlackList)
    {
        super(stack);
        this.weaponBlackList = weaponBlackList;
    }

    private boolean haveMatches()
    {
        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return false;

        ConsumableContainer cnt = poc.getConsumableContainer();
        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            if (entry.value.getItem().getContent().hasComponent(CampFireStarterComponent.class))
                return true;
        }

        return false;
    }

    private static Array<String> outside = new Array<>();
    static
    {
        outside.add("default");
        outside.add("forest");
        outside.add("swamp2");
    }

    @Override
    protected void update(float dt)
    {
        check -= dt;
        if (check > 0)
            return;

        check = 1.0f;

        // is it still night?
        GameMode gameMode = BrainOutServer.Controller.getGameMode();
        if (gameMode instanceof GameModeFree)
        {
            if (!((GameModeFree) gameMode).isNight())
            {
                // not a night anymore
                pop();
                return;
            }
        }

        // are we outside?
        if (!outside.contains(getPlayerData().getMap().getDimension(), false))
        {
            // we should be
            ActiveData portalToOutside = getPlayerData().getMap().getActiveForTag(
                Constants.ActiveTags.PORTAL, activeData ->
            {
                ServerPortalComponentData sp = activeData.getComponent(ServerPortalComponentData.class);
                if (sp.getOtherPortal() != null)
                {
                    return outside.contains(sp.getOtherPortal().getDimension(), false);
                }

                return false;
            });

            if (portalToOutside != null)
            {
                ServerPortalComponentData sp = portalToOutside.getComponent(ServerPortalComponentData.class);
                PortalData otherPortal = sp.getOtherPortal();

                if (otherPortal != null)
                {
                    ActiveData goTo = otherPortal.getMap().getClosestActiveForTag(128,
                        otherPortal.getX(), otherPortal.getY(),
                        ActiveData.class, Constants.ActiveTags.ITEM, activeData -> true);

                    if (goTo != null)
                    {
                        pushTask(new TaskFollowTarget(getStack(), goTo, null, true, 8));
                        return;
                    }
                }
            }
        }

        final ActiveData fire = getPlayerData().getMap().getClosestActiveForTag(16,
            getPlayerData().getX(), getPlayerData().getY(),
            ActiveData.class, Constants.ActiveTags.CAMP_FIRE, activeData -> true);

        if (fire == null)
        {
            ActiveData fire2 = getPlayerData().getMap().getClosestActiveForTag(128,
                getPlayerData().getX(), getPlayerData().getY(),
                ActiveData.class, Constants.ActiveTags.CAMP_FIRE, activeData -> true);

            if (fire2 != null)
            {
                // there's ongoing fire nearby
                pushTask(new ApproachActive(getStack(), fire2, (stack, enemy) -> false, 8.0f));
            }
            else
            {
                // do we have matches?
                if (haveMatches())
                {
                    // are we near sticks?
                    ActiveData sticks = nearSticks();
                    if (sticks == null)
                    {
                        // we're not
                        pushTask(new FindAndApproachItem(getStack(), new FindAndApproachItem.ItemPredicate()
                        {
                            @Override
                            public int matches(ItemData itemData, ConsumableRecord record)
                            {
                                return itemData.getComponent(ServerCampfireActivatorComponentData.class) != null ? 1 : 0;
                            }

                            @Override
                            public void notFound()
                            {
                                pushTask(new TaskHide(getStack(), null));
                            }
                        }, (stack, enemy) -> {
                            pushTask(new TaskShootTarget(getStack(), enemy,
                                new RandomValue(0.3f, 0.8f), new RandomValue(0.5f, 1.0f)));
                            return false;
                        }));
                    }
                    else
                    {
                        pushTask(new TaskActivateItem(getStack(), sticks));
                    }
                }
                else
                {
                    pushTask(new FindItem(getStack(), new FindItem.ItemPredicate()
                    {
                        @Override
                        public int matches(ItemData itemData, ConsumableRecord record)
                        {
                            if (record.getItem().getContent().hasComponent(CampFireStarterComponent.class))
                            {
                                return 1;
                            }

                            return 0;
                        }

                        @Override
                        public void notFound()
                        {
                            pushTask(new TaskHide(getStack(), null));
                        }
                    }, (stack, enemy) -> {
                        pushTask(new TaskShootTarget(getStack(), enemy,
                                new RandomValue(0.3f, 0.8f), new RandomValue(0.5f, 1.0f)));

                        return false;
                    }, (record) -> {}));
                }
            }
        }
        else
        {
            ServerCampFireComponentData cf = fire.getComponent(ServerCampFireComponentData.class);

            if (cf.getDuration() < 120)
            {
                // need some fuel
                pushTask(new FindItem(getStack(), new FindItem.ItemPredicate()
                {
                    @Override
                    public int matches(ItemData itemData, ConsumableRecord record)
                    {
                        // do not touch sticks
                        if (record.getItem().getContent().hasComponent(ServerCampfireActivatorComponent.class))
                            return 0;

                        if (record.getItem().getContent().hasComponent(CampFireFuelComponent.class))
                            return 1;

                        return 0;
                    }

                    @Override
                    public void notFound()
                    {
                        // oh well
                    }
                }, new EnemyNoticedCallback()
                {
                    @Override
                    public boolean noticed(TaskStack stack, ActiveData enemy)
                    {
                        pushTask(new TaskShootTarget(getStack(), enemy,
                            new RandomValue(0.3f, 0.8f), new RandomValue(0.5f, 1.0f)));

                        return false;
                    }
                }, new FindItem.ItemTakenCallback()
                {
                    @Override
                    public void found(ConsumableRecord record)
                    {
                        pushTask(new PutItem(getStack(), ((ItemData) fire), record, record.getAmount(),
                        (stack, enemy) -> {
                            pushTask(new TaskShootTarget(getStack(), enemy,
                                    new RandomValue(0.3f, 0.8f), new RandomValue(0.5f, 1.0f)));
                            return true;
                        }, () -> pushTask(new DelayTask(getStack(), 5.0f))));
                    }
                }));
            }
            else
            {
                pushTask(new TaskWait(getStack(), 10.0f));
            }
        }
    }

    private ActiveData nearSticks()
    {
        return getMap().getClosestActiveForTag(4, getPlayerData().getX(), getPlayerData().getY(), ItemData.class,
            Constants.ActiveTags.ITEM, activeData ->
        {
            ServerCampfireActivatorComponentData cmp = activeData.getComponent(ServerCampfireActivatorComponentData.class);
            return cmp != null;
        });
    }
}
