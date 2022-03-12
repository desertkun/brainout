package com.desertkun.brainout.bot;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.freeplay.FindAmmo;
import com.desertkun.brainout.bot.freeplay.LoadMagazine;
import com.desertkun.brainout.bot.freeplay.TaskHide;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.bullet.LimitedBullet;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.BotControllerComponentData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.data.components.ServerTeamVisibilityComponentData;
import com.desertkun.brainout.data.components.ServerWeaponComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.mode.GameMode;

public abstract class Task
{
    private final TaskStack stack;

    public Task(TaskStack stack)
    {
        this.stack = stack;
    }

    protected abstract void update(float dt);

    protected void popMeAndPushTask(Task task)
    {
        stack.popTask(this);
        stack.pushTask(task);
    }

    public void pushTask(Task task)
    {
        stack.pushTask(task);
    }

    protected void pop()
    {
        stack.popTask(this);
    }

    public TaskStack getStack()
    {
        return stack;
    }

    public BotControllerComponentData getController()
    {
        return stack.getController();
    }

    protected void setAim(boolean aim)
    {
        getController().setAim(aim);
    }

    protected void setState(Player.State state)
    {
        getController().setState(state);
    }

    protected void openFire(boolean fire)
    {
        getController().openFire(fire);
    }

    protected void reloadWeapon()
    {
        getController().reloadWeapon();
    }

    protected WeaponData getCurrentWeapon()
    {
        return getController().getCurrentWeapon();
    }

    protected boolean haveKnife()
    {
        WeaponData currentWeapon = getCurrentWeapon();

        if (currentWeapon != null)
        {
            ServerWeaponComponentData sw = currentWeapon.getComponent(ServerWeaponComponentData.class);

            if (sw != null)
            {
                ServerWeaponComponentData.Slot slot = sw.getSlot(Constants.Properties.SLOT_PRIMARY);

                if (slot != null && slot.getBullet() != null)
                {
                    Bullet bullet = slot.getBullet();

                    if (bullet instanceof LimitedBullet)
                    {
                        return ((LimitedBullet) bullet).getMaxDistance() <= 5;
                    }
                }
            }
        }

        return false;
    }

    public Map getMap()
    {
        return stack.getController().getMap();
    }

    public PlayerData getPlayerData()
    {
        return stack.getController().getPlayerData();
    }

    protected ActiveData checkForEnemies()
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        Array<ActiveData> a = getMap().getActivesForTag(
            Constants.ActiveTags.PLAYERS, target ->
        {
            if (target == getPlayerData())
                return false;

            if (target.getOwnerId() < 0 || getPlayerData().getOwnerId() < 0)
            {
                if (!gameMode.isEnemiesActive(target, getPlayerData()))
                    return false;
            }
            else
            {
                if (!BrainOutServer.Controller.isEnemies(
                    target.getOwnerId(), getPlayerData().getOwnerId()))
                    return false;
            }

            return getController().checkVisibility(target, 48, null);
        });

        if (a.size > 0)
        {
            return a.random();
        }

        return null;
    }

    protected boolean selectKnife()
    {
        PlayerData playerData = getPlayerData();
        PlayerOwnerComponent ownerComponent = playerData.getComponent(PlayerOwnerComponent.class);

        InstrumentData found = null;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : ownerComponent.getConsumableContainer().getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();

            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
                Slot slot = ici.getInstrumentData().getInstrument().getSlot();

                if (slot != null && slot.getID().equals("slot-melee"))
                {
                    found = ici.getInstrumentData();
                    break;
                }
            }
        }

        if (found != null)
        {
            return selectInstrument(found);
        }

        return false;
    }

    public boolean selectInstrument(InstrumentData instrumentData)
    {
        PlayerData playerData = getPlayerData();

        PlayerOwnerComponent ownerComponent = playerData.getComponent(PlayerOwnerComponent.class);
        PlayerRemoteComponent remoteComponent = playerData.getComponent(PlayerRemoteComponent.class);

        ServerPlayerControllerComponentData ctl =
                playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);
        if (ctl != null)
        {
            ConsumableRecord r = ownerComponent.findRecord(instrumentData);

            if (r != null)
            {
                ctl.instrumentSelected(r);
                ctl.changeInstrument(r.getId());

                remoteComponent.setCurrentInstrument(instrumentData);
                playerData.setCurrentInstrument(instrumentData);

                return true;
            }
        }

        return false;
    }

    public boolean selectInstrument(ConsumableRecord r)
    {
        if (r == null)
            return false;

        if (!(r.getItem() instanceof InstrumentConsumableItem))
            return false;

        InstrumentConsumableItem ici = ((InstrumentConsumableItem) r.getItem());
        InstrumentData instrumentData = ici.getInstrumentData();

        PlayerData playerData = getPlayerData();

        PlayerRemoteComponent remoteComponent = playerData.getComponent(PlayerRemoteComponent.class);

        ServerPlayerControllerComponentData ctl =
                playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

        ctl.instrumentSelected(r);
        ctl.changeInstrument(r.getId());

        remoteComponent.setCurrentInstrument(instrumentData);
        playerData.setCurrentInstrument(instrumentData);

        return true;

    }

    public void gotShotFrom(ActiveData shooter)
    {
    }

    protected boolean checkWeapons(boolean enemyInSight)
    {
        WeaponData currentWeapon = getCurrentWeapon();

        if (currentWeapon == null)
            return false;

        {
            ServerWeaponComponentData sw = currentWeapon.getComponent(ServerWeaponComponentData.class);

            if (sw == null)
                return false;

            ServerWeaponComponentData.Slot slot = sw.getSlot(Constants.Properties.SLOT_PRIMARY);

            if (slot != null)
            {
                if (slot.hasMagazineManagement())
                {
                    if (slot.isDetached())
                    {
                        runAway(slot, enemyInSight);
                        return true;
                    }
                }

                if (slot.getBullet() == null || slot.getRounds() == 0)
                {
                    PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);

                    if (poc == null)
                        return true;

                    Bullet appropriateBullet = slot.getBullet();

                    if (slot.hasMagazineManagement())
                    {
                        int other = slot.getBestLoadedMagazine(-1);
                        if (other >= 0)
                        {
                            ServerWeaponComponentData.Slot.Magazine mag = slot.getMagazines().get(other);
                            if (mag != null && mag.rounds > 0)
                            {
                                getController().reloadWeapon();
                                return true;
                            }
                            else
                            {
                                return hideAndLoadMagazine(enemyInSight, slot);
                            }
                        }

                        return true;
                    }
                    else
                    {
                        if (poc.getConsumableContainer().getAmount(appropriateBullet) == 0)
                        {
                            // we're empty

                            if (currentWeapon.getWeapon().getSlot() == null)
                                return false;

                            openFire(false);
                            pop();

                            pushTask(new FindAmmo(getStack(), null, slot.getBullet(), getController().getCurrentWeaponRecord()));

                            if (enemyInSight)
                            {
                                pushTask(new TaskHide(getStack(), null, false));
                            }

                            return true;
                        }
                        else
                        {
                            getController().reloadWeapon();
                        }
                    }

                }
                else
                {
                    if (slot.getWeaponProperties().hasChambering() && slot.getChambered() == 0)
                    {
                        getController().fetchWeapon();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void runAway(ServerWeaponComponentData.Slot slot, boolean enemyInSight)
    {
        openFire(false);
        pop();

        pushTask(new LoadMagazine(getStack(), null, slot.getBullet(), getController().getCurrentWeaponRecord()));

        if (enemyInSight)
        {
            pushTask(new TaskHide(getStack(), null, false));
        }
    }

    private boolean hideAndLoadMagazine(boolean enemyInSight, ServerWeaponComponentData.Slot slot)
    {
        openFire(false);
        pop();

        pushTask(new LoadMagazine(getStack(), null, slot.getBullet(), getController().getCurrentWeaponRecord()));

        if (enemyInSight)
        {
            pushTask(new TaskHide(getStack(), null, false));
        }

        return true;
    }

    public boolean hasTask(Class<? extends Task> classOf)
    {
        for (Task task : stack.getTasks())
        {
            if (task.getClass() == classOf)
                return true;
        }

        return false;
    }
}
