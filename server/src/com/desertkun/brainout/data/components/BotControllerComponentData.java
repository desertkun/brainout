package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.msg.server.InventoryItemMovedMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.ServerBotWeaponComponent;
import com.desertkun.brainout.components.WeaponSlotComponent;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.AutoConvertConsumable;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;

import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.BonePointData;
import com.desertkun.brainout.events.DamagedEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeRealization;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.DistanceUtils;

@Reflect("BotControllerComponent")
@ReflectAlias("data.components.BotControllerComponentData")
public class BotControllerComponentData extends ServerPlayerControllerComponentData
{
    private static Vector2 tmp = new Vector2();

    private boolean firing;
    private boolean followPathSearching;
    private float followTimer, blocksInWayCheck, targetDistance;
    private ActiveData followTarget;
    private Runnable targetReached, stuckCallback;
    private GotBlocksInWayCallback blocksInWayCallback;
    private Queue<Vector2> followPath;
    private boolean needPathFinding;
    private String followPathDimension;
    private ObjectSet<WayPointMap.BlockCoordinates> followBlocksInWay;
    private ObjectMap<Integer, Float> pointsOfInterest;
    private ActiveData followPortalOfInterest;
    private TaskStack stack;
    private Vector2 recentPosition;
    private float vStuckCounter;
    private float recentTimer;

    public interface GotBlocksInWayCallback
    {
        void got(Queue<WayPointMap.BlockCoordinates> blocks);
    }

    public BotControllerComponentData(PlayerData playerData)
    {
        super(playerData);

        stack = new TaskStack(this);
        recentPosition = new Vector2();
        pointsOfInterest = new ObjectMap<>();
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    public TaskStack getTasksStack()
    {
        return stack;
    }

    public Queue<Vector2> getFollowPath()
    {
        return followPath;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (stack.isEmpty())
        {
            PlayerData playerData = getPlayerData();
            if (playerData == null)
                return;

            if (playerData.getOwnerId() >= 0)
            {
                Client client = BrainOutServer.Controller.getClients().get(playerData.getOwnerId());

                if (client instanceof BotClient)
                {
                    GameMode gameMode = BrainOutServer.Controller.getGameMode();
                    if (gameMode != null)
                    {
                        ServerRealization serverRealization =
                                ((ServerRealization) gameMode.getRealization());

                        Task startingTask;

                        if (gameMode.isGameActive())
                        {
                            startingTask = serverRealization.getBotStartingTask(stack, ((BotClient) client));
                        }
                        else
                        {
                            startingTask = serverRealization.getBotWarmupTask(stack, ((BotClient) client));
                        }

                        if (startingTask != null)
                        {
                            stack.init(startingTask);
                        }
                    }
                }
            }
        }

        updateFollow(dt);
        stack.update(dt);
    }

    @Override
    public void init()
    {
        super.init();

        Map map = getMap();

        pointsOfInterest.clear();

        map.countActivesForTag(Constants.ActiveTags.POINT_OF_INTEREST, activeData ->
        {
            pointsOfInterest.put(activeData.getId(), MathUtils.random(0.1f, 1.0f));
            return true;
        });

        int a = 0;
    }

    public WeaponData getCurrentWeapon()
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return null;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return null;

        ConsumableRecord record = poc.getCurrentInstrumentRecord();
        if (record == null)
            return null;

        if (!(record.getItem() instanceof InstrumentConsumableItem))
            return null;

        InstrumentData instrumentData = ((InstrumentConsumableItem) record.getItem()).getInstrumentData();

        if (!(instrumentData instanceof WeaponData))
            return null;

        return ((WeaponData) instrumentData);
    }


    public ConsumableRecord getCurrentWeaponRecord()
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return null;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return null;

        return poc.getCurrentInstrumentRecord();
    }

    public void reloadWeapon()
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        ConsumableRecord record = poc.getCurrentInstrumentRecord();
        if (record == null)
            return;

        if (!(record.getItem() instanceof InstrumentConsumableItem))
            return;

        InstrumentData instrumentData = ((InstrumentConsumableItem) record.getItem()).getInstrumentData();

        if (!(instrumentData instanceof WeaponData))
            return;

        WeaponData weaponData = ((WeaponData) instrumentData);

        ServerBotWeaponComponent sw = instrumentData.getComponent(ServerBotWeaponComponent.class);
        if (sw == null)
        {
            sw = new ServerBotWeaponComponent(weaponData, record);
            sw.init();
            instrumentData.addComponent(sw);
        }

        WeaponSlotComponent slot = sw.getSlot(Constants.Properties.SLOT_PRIMARY);
        if (slot == null)
            return;

        slot.doReload(true);
    }

    public void fetchWeapon()
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        ConsumableRecord record = poc.getCurrentInstrumentRecord();
        if (record == null)
            return;

        if (!(record.getItem() instanceof InstrumentConsumableItem))
            return;

        InstrumentData instrumentData = ((InstrumentConsumableItem) record.getItem()).getInstrumentData();

        if (!(instrumentData instanceof WeaponData))
            return;

        WeaponData weaponData = ((WeaponData) instrumentData);

        ServerBotWeaponComponent sw = instrumentData.getComponent(ServerBotWeaponComponent.class);
        if (sw == null)
        {
            sw = new ServerBotWeaponComponent(weaponData, record);
            sw.init();
            instrumentData.addComponent(sw);
        }

        WeaponSlotComponent slot = sw.getSlot(Constants.Properties.SLOT_PRIMARY);
        if (slot == null)
            return;

        slot.doFetch();
    }

    public void openFire(boolean fire)
    {
        if (this.firing == fire)
            return;

        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return;

        PlayerOwnerComponent poc = getPlayerData().getComponent(PlayerOwnerComponent.class);
        if (poc == null)
            return;

        ConsumableRecord record = poc.getCurrentInstrumentRecord();
        if (record == null)
            return;

        if (!(record.getItem() instanceof InstrumentConsumableItem))
            return;

        InstrumentData instrumentData = ((InstrumentConsumableItem) record.getItem()).getInstrumentData();

        if (!(instrumentData instanceof WeaponData))
            return;

        WeaponData weaponData = ((WeaponData) instrumentData);

        ServerBotWeaponComponent sw = instrumentData.getComponent(ServerBotWeaponComponent.class);
        if (sw == null)
        {
            sw = new ServerBotWeaponComponent(weaponData, record);
            sw.init();
            instrumentData.addComponent(sw);
        }

        WeaponSlotComponent slot = sw.getSlot(Constants.Properties.SLOT_PRIMARY);
        if (slot == null)
            return;

        this.firing = fire;
        slot.setLaunching(fire);
    }

    public boolean isFollowing(ActiveData target)
    {
        return followTarget != null && followTarget == target;
    }

    public boolean isFollowingAnything()
    {
        return followTarget != null;
    }

    public ActiveData getFollowTarget()
    {
        return followTarget;
    }

    public void gotShotFrom(ActiveData shooter)
    {
        if (stack.getTasks().size > 0)
        {
            stack.getTasks().last().gotShotFrom(shooter);
        }
    }

    public void stopFollowing()
    {
        setRun(false);
        setMoveDirection(0, 0);
        followTimer = 0;
        targetReached = null;
        followTarget = null;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case damaged:
            {
                DamagedEvent damagedEvent = ((DamagedEvent) event);

                damaged(damagedEvent);
                break;
            }
        }

        return super.onEvent(event);
    }

    private void damaged(DamagedEvent damagedEvent)
    {
        ActiveData.LastHitInfo lastHitInfo = getPlayerData().getLastHitInfo();

        if (lastHitInfo != null)
        {
            Client client = BrainOutServer.Controller.getClients().get(lastHitInfo.hitterId);

            if (client != null && client.isAlive())
            {
                gotShotFrom(client.getPlayerData());
            }
        }
    }

    private float everyOther = 0;

    private void updateFollow(float dt)
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return;

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc != null && !poc.isEnabled())
        {
            setMoveDirection(0, 0);
            return;
        }

        if (followTarget == null)
            return;

        if (followPathSearching)
            return;

        ServerMap map = getMap(ServerMap.class);
        if (map == null || map.getWayPointMap() == null)
            return;

        if (followPathDimension != null && !followPathDimension.equals(map.getDimension()))
        {
            followPath = null;
            followPathDimension = null;
            return;
        }

        if (Vector2.dst2(playerData.getX(), playerData.getY(), followTarget.getX(), followTarget.getY()) <
                targetDistance * targetDistance)
        {
            if (targetReached != null)
            {
                targetReached.run();
                targetReached = null;
            }

            stopFollowing();
            return;
        }

        recentTimer += dt;

        if (recentTimer >= 5.0f)
        {
            recentTimer = 0;

            if (this.recentPosition.dst2(playerData.getX(), playerData.getY()) < 6.0f * 6.0f)
            {
                if (stuckCallback != null)
                {
                    stuckCallback.run();
                    return;
                }
            }
            else
            {
                recentPosition.set(playerData.getX(), playerData.getY());
            }
        }

        followTimer -= dt;

        boolean rebuild;

        if (followTimer < 0 || followPath == null)
        {
            rebuild = true;
        }
        else
        {
            if (followPath.size > 0 && map.getDimension().equals(followTarget.getDimension()) &&
                followPath.last().dst(followTarget.getX(), followTarget.getY()) > 2.0f)
            {
                rebuild = true;
            }
            else
            {
                rebuild = false;
            }
        }

        if (rebuild)
        {
            followTimer = 2.0f;

            followPathSearching = true;

            if (needPathFinding)
            {
                map.getWayPointMap().findPath(
                    playerData.getX(), playerData.getY(), map,
                    followTarget.getX(), followTarget.getY(), followTarget.getMap(), 8.0f, this::checkPointOfInterest,
                    new WayPointMap.PathSearchResult()
                    {
                        @Override
                        public void found(
                                Queue<Vector2> path, String dimension,
                                ObjectSet<WayPointMap.BlockCoordinates> blocksInWay,
                                ActiveData portalOfInterest)
                        {
                            followPath = path;
                            followPathDimension = dimension;
                            followBlocksInWay = blocksInWay;
                            followPathSearching = false;
                            followPortalOfInterest = portalOfInterest;
                        }

                        @Override
                        public void notFound()
                        {
                            followPathSearching = false;

                            if (stuckCallback != null)
                            {
                                stuckCallback.run();
                                stuckCallback = null;
                            }
                        }
                    }
                );
            }
            else
            {
                followPath = new Queue<>();
                followPath.addLast(new Vector2(followTarget.getX(), followTarget.getY()));
                followPathDimension = followTarget.getDimension();
                followBlocksInWay = null;
                followPathSearching = false;
                followPortalOfInterest = null;
            }
        }

        if (followPath != null)
        {
            blocksInWayCheck -= dt;
            if (blocksInWayCheck < 0)
            {
                blocksInWayCheck = 1.0f;

                if (blocksInWayCallback != null && followBlocksInWay != null && followBlocksInWay.size > 0)
                {
                    Queue<WayPointMap.BlockCoordinates> blocks = null;

                    for (WayPointMap.BlockCoordinates coordinates : followBlocksInWay)
                    {
                        if (Vector2.dst2(playerData.getX(), playerData.getY(),
                                coordinates.x, coordinates.y) > 16.0f * 16.0f)
                            continue;

                        if (!checkVisibility(coordinates.x + 0.5f, coordinates.y + 0.5f, this::checkIgnoredBlock))
                            continue;

                        if (blocks == null)
                        {
                            blocks = new Queue<>();
                        }

                        blocks.addLast(coordinates);
                    }

                    if (blocks != null)
                    {
                        blocksInWayCallback.got(blocks);
                    }
                }
            }

            // if we have hit the first path edge, remove the first entry

            SimplePhysicsComponentData phy = getComponentObject().
                    getComponentWithSubclass(SimplePhysicsComponentData.class);

            float accuracy;
            if (phy != null && phy.hasFixture())
            {
                accuracy = 0.25f;
            }
            else
            {
                accuracy = 1.0f;
            }

            while (followPath.size > 1 && checkEdgeHit(followPath.first(), followPath.get(1), accuracy))
            {
                followPath.removeFirst();
            }

            while (followPath.size > 0 &&
                Vector2.dst2(playerData.getX(), playerData.getY(),
                    followPath.first().x, followPath.first().y) < accuracy * accuracy)
            {
                followPath.removeFirst();
            }

            if (followPortalOfInterest != null &&
                Vector2.dst2(followPortalOfInterest.getX(), followPortalOfInterest.getY(),
                    playerData.getX(), playerData.getY()) < 2f * 2f)
            {
                ServerPortalComponentData sp = followPortalOfInterest.getComponent(ServerPortalComponentData.class);
                ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

                if (!progress.isRunning())
                {
                    sp.enter(getClient(), playerData);
                }
            }

            if (followPath.size > 0)
            {
                Vector2 goTo = followPath.first();

                tmp.set(goTo).sub(playerData.getX(), playerData.getY());
                float distance = tmp.len();
                boolean forceUp = false;

                boolean jump = false;

                if (Math.abs(tmp.x) > 3.0 && tmp.y < 5.0f && tmp.y > 0.25f)
                {
                    // if we're standing
                    if (phy != null)
                    {
                        SimplePhysicsComponentData.ContactData cnt =
                            phy.getContact(SimplePhysicsComponentData.Contact.bottom);

                        if (cnt.valid() && cnt.block.isConcrete())
                        {
                            float direction = Math.signum(tmp.x),
                                length = Math.min(Math.abs(tmp.x), 4.0f);

                            // get the block under the player
                            float startX = playerData.getX() + direction,
                                startY = playerData.getY() - phy.getHalfSize().y - 0.5f;

                            int jumpCounter = 0;

                            for (float x = startX; length > 0; x += direction, length -= 1.0f)
                            {
                                BlockData blockAt = map.getBlockAt(x, startY, Constants.Layers.BLOCK_LAYER_FOREGROUND);

                                if (blockAt == null || !blockAt.isConcrete() && !blockAt.isFixture())
                                {
                                    jumpCounter++;
                                }
                            }

                            jump = jumpCounter >= 2;
                        }
                    }
                }

                everyOther += dt;
                if (everyOther > 0.5f)
                {
                    playerData.setAngle(tmp.angleDeg());
                    everyOther = 0;
                }

                tmp.set(goTo).sub(playerData.getX(), playerData.getY() - 1);

                float diffX = goTo.x - playerData.getX(),
                      diffY = goTo.y - playerData.getY() - 1.f;

                float moveDirectionX = Math.abs(diffX) > 0.01f ? Math.signum(diffX) : 0;
                float moveDirectionY;

                if (phy != null && phy.hasFixture())
                {
                    // if we're on stairs, offet the target y a bit so bot will get upstairs right
                    diffY = goTo.y - playerData.getY();
                }

                if (moveDirectionX != 0)
                {
                    if (phy != null)
                    {
                        if (moveDirectionX > 0 && phy.hasContact(SimplePhysicsComponentData.Contact.right))
                        {
                            vStuckCounter += dt;
                        }
                        else if (moveDirectionX < 0 && phy.hasContact(SimplePhysicsComponentData.Contact.left))
                        {
                            vStuckCounter += dt;
                        }
                        else
                        {
                            vStuckCounter = 0;
                        }

                        if (vStuckCounter > 0.5f)
                        {
                            forceUp = true;
                        }
                    }
                }
                else
                {
                    vStuckCounter = 0;
                }

                float yCoef;

                if (Math.abs(diffX) > 4.0f)
                {
                    yCoef = 1.0f;
                }
                else
                {
                    yCoef = 0.25f;
                }

                if (jump)
                {
                    moveDirectionY = 1.0f;
                }
                else
                {
                    moveDirectionY = Math.abs(diffY) > yCoef ? Math.signum(diffY) : 0;
                }

                if (forceUp)
                {
                    moveDirectionY = Math.signum(followPath.last().y - playerData.getY());
                }

                boolean canRun = followTarget != null &&
                    Vector2.dst2(followTarget.getX(), followTarget.getY(),
                    playerData.getX(), playerData.getY()) > 6f * 6f;

                if (Math.abs(diffX) < 4 && diffY < -4)
                {
                    // jump with small offset
                    setState(Player.State.sit);
                }
                else
                {
                    if (canRun)
                    {
                        setState(Player.State.run);
                    }
                    else
                    {
                        setState(Player.State.normal);
                    }
                }

                setMoveDirection(moveDirectionX, moveDirectionY);
            }
            else
            {
                setRun(false);
                setMoveDirection(0, 0);

                followPath = null;
                followPathDimension = null;
            }
        }
        else
        {
            setRun(false);
            setMoveDirection(0, 0);
        }
    }

    private boolean checkEdgeHit(Vector2 a, Vector2 b, float distance)
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return false;

        return DistanceUtils.PointToLineDistance(
            playerData.getX(), playerData.getY(), a.x, a.y, b.x, b.y) < distance;
    }

    private float checkPointOfInterest(int pointOfInterest)
    {
        return pointsOfInterest.get(pointOfInterest, 1.0f);
    }

    private boolean checkIgnoredBlock(int blockX, int blockY)
    {
        for (WayPointMap.BlockCoordinates coordinates :
            new ObjectSet.ObjectSetIterator<>(followBlocksInWay))
        {
            if (blockX == coordinates.x && blockY == coordinates.y)
            {
                return false;
            }
        }

        return true;
    }

    public void followDirectly(ActiveData target, Runnable targetReached)
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return;

        this.recentPosition.set(playerData.getX(), playerData.getY());
        this.recentTimer = 0;
        this.followTarget = target;
        this.targetDistance = 2.0f;
        this.needPathFinding = false;
        this.targetReached = targetReached;
        this.stuckCallback = null;
        this.blocksInWayCallback = null;
    }

    public void follow(ActiveData target, Runnable targetReached, Runnable stuck,
        GotBlocksInWayCallback blocksInWayCallback)
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return;

        this.recentPosition.set(playerData.getX(), playerData.getY());
        this.recentTimer = 0;
        this.followTarget = target;
        this.targetDistance = 2.0f;
        this.needPathFinding = true;
        this.targetReached = targetReached;
        this.stuckCallback = stuck;
        this.blocksInWayCallback = blocksInWayCallback;
    }

    public void follow(ActiveData target, Runnable targetReached, Runnable stuck,
        GotBlocksInWayCallback blocksInWayCallback, float targetDistance)
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return;

        this.recentPosition.set(playerData.getX(), playerData.getY());
        this.recentTimer = 0;
        this.followTarget = target;
        this.targetDistance = targetDistance;
        this.needPathFinding = true;
        this.targetReached = targetReached;
        this.stuckCallback = stuck;
        this.blocksInWayCallback = blocksInWayCallback;
    }

    public static boolean GetCheckVisibilityLaunchData(Map map, PlayerData playerData, Vector2 out)
    {
        if (map == null)
            return false;

        InstrumentData currentWeapon = playerData.getCurrentInstrument();
        if (currentWeapon == null)
            return false;

        WeaponAnimationComponentData anim = currentWeapon.getComponent(WeaponAnimationComponentData.class);
        if (anim == null)
            return false;

        BonePointData launch = anim.getLaunchPointData();

        out.set(launch.getX(), launch.getY());

        return true;
    }

    public static boolean CheckVisibility(
        Map map, PlayerData playerData,
        float x, float y, Map.IgnoreCheck ignoreCheck)
    {
        if (!GetCheckVisibilityLaunchData(map, playerData, tmp))
            return false;

        tmp.sub(x, y);

        if (!map.trace(x, y, Constants.Layers.BLOCK_LAYER_UPPER, tmp.angleDeg(), tmp.len(), null) &&
            !map.trace(x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND, tmp.angleDeg(), tmp.len(), tmp, ignoreCheck))
        {
            return true;
        }

        return false;
    }

    public static boolean CheckVisibility(Map map, PlayerData playerData, float x, float y)
    {
        if (!GetCheckVisibilityLaunchData(map, playerData, tmp))
            return false;

        return !map.rayCast(tmp.x, tmp.y, x, y);
    }

    public boolean checkVisibility(float x, float y, Map.IgnoreCheck ignoreCheck)
    {
        return CheckVisibility(getMap(), getPlayerData(), x, y, ignoreCheck);
    }

    public boolean checkVisibility(float x, float y)
    {
        return CheckVisibility(getMap(), getPlayerData(), x, y);
    }

    public static class VisibilityCheckOutput
    {
        public float angle;
    }

    public boolean checkVisibility(ActiveData target, float maximumDistance, VisibilityCheckOutput out)
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return false;

        Map map = getMap();

        InstrumentData currentWeapon = playerData.getCurrentInstrument();
        if (currentWeapon == null)
            return false;

        WeaponAnimationComponentData anim = currentWeapon.getComponent(WeaponAnimationComponentData.class);
        if (anim == null)
            return false;

        SimplePhysicsComponentData phy = target.getComponentWithSubclass(SimplePhysicsComponentData.class);
        if (phy == null)
            return false;

        BonePointData launch = anim.getLaunchPointData();
        if (launch == null)
            return false;

        float halfHeight = phy.getHalfSize().y;

        // check center of the body fist

        {
            tmp.set(target.getX(), target.getY());
            tmp.sub(launch.getX(), launch.getY());

            if (tmp.len2() <= maximumDistance * maximumDistance)
            {
                if (!map.rayCast(launch.getX(), launch.getY(),
                        target.getX(), target.getY()))
                {

                    if (out != null)
                        out.angle = tmp.angleDeg();

                    return true;
                }
            }
        }

        // then look for the legs
        {
            tmp.set(target.getX(), target.getY() + halfHeight);
            tmp.sub(launch.getX(), launch.getY());

            if (tmp.len2() <= maximumDistance * maximumDistance)
            {
                if (!map.rayCast(launch.getX(), launch.getY(),
                        target.getX(), target.getY() + halfHeight))
                {

                    if (out != null)
                        out.angle = tmp.angleDeg();

                    return true;
                }
            }
        }

        // then look for the head
        {
            tmp.set(target.getX(), target.getY() + halfHeight);
            tmp.sub(launch.getX(), launch.getY());

            if (tmp.len2() <= maximumDistance * maximumDistance)
            {
                if (!map.rayCast(launch.getX(), launch.getY(),
                        target.getX(), target.getY() - halfHeight))
                {

                    if (out != null)
                        out.angle = tmp.angleDeg();

                    return true;
                }
            }
        }

        return false;
    }

    public boolean lerpAngle(float angleRequired)
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return false;

        float angleHave = playerData.getAngle();
        float result = MathUtils.lerpAngleDeg(angleHave, angleRequired, 0.6f);
        playerData.setAngle(result);

        float diff = Math.abs(angleHave - angleRequired);
        return diff < 5.0f || diff >= 355.0f;
    }

    public boolean lerpAngle(float x, float y)
    {
        PlayerData playerData = getPlayerData();
        if (playerData == null)
            return false;

        InstrumentData currentWeapon = playerData.getCurrentInstrument();
        if (currentWeapon == null)
            return false;

        WeaponAnimationComponentData anim = currentWeapon.getComponent(WeaponAnimationComponentData.class);
        if (anim == null)
            return false;

        BonePointData launch = anim.getLaunchPointData();

        tmp.set(x, y).sub(launch.getX(), launch.getY());

        float angleRequired = tmp.angleDeg();
        float angleHave = playerData.getAngle();
        float result = MathUtils.lerpAngleDeg(angleHave, angleRequired, 0.6f);
        playerData.setAngle(result);

        float diff = Math.abs(angleHave - angleRequired);
        return diff < 5.0f || diff >= 355.0f;
    }

    public ItemData dropConsumable(int recordId, float angle, int amount)
    {
        if (isWounded())
            return null;

        PlayerData playerData = getPlayerData();

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        ConsumableRecord record = poc.getConsumableContainer().get(recordId);
        if (record == null || record.getAmount() == 0)
            return null;

        {
            Map map = getMap();

            if (map == null)
                return null;

            ChunkData chunk = map.getChunkAt((int)playerData.getX(), (int)playerData.getY());

            if (chunk != null && chunk.hasFlag(ChunkData.ChunkFlag.shootingDisabled))
                return null;
        }

        if (poc != null)
        {
            if (record != null && record.getAmount() >= amount)
            {
                ConsumableItem item = record.getItem();

                GameMode gameMode = BrainOutServer.Controller.getGameMode();

                if (gameMode != null)
                {
                    GameModeRealization realization = gameMode.getRealization();

                    if (realization instanceof ServerRealization)
                    {
                        Client client = BrainOutServer.Controller.getClients().get(playerData.getOwnerId());

                        if (client != null)
                        {
                            if (!((ServerRealization) realization).canDropConsumable(client, item))
                                return null;
                        }
                    }
                }

                boolean currentInstrument = false;

                Item dropItem = null;
                if (item instanceof InstrumentConsumableItem)
                {
                    InstrumentData instrumentData = ((InstrumentConsumableItem) item).getInstrumentData();
                    currentInstrument = instrumentData == playerData.getCurrentInstrument();
                    Instrument instrument = instrumentData.getInstrument();

                    if (instrument.hasComponent(ItemComponent.class))
                    {
                        dropItem = instrument.getComponent(ItemComponent.class).getDropItem();
                    }
                }

                AutoConvertConsumable auto = item.getContent().getComponent(AutoConvertConsumable.class);

                if (auto != null)
                {
                    item = auto.getConvertTo().acquireConsumableItem();
                }

                Array<ConsumableRecord> records = new Array<>();
                records.add(new ConsumableRecord(item, amount, 0));

                ItemData itemData = ServerMap.dropItem(playerData.getDimension(), dropItem, records, playerData.getOwnerId(),
                        playerData.getX(), playerData.getY(), angle);

                poc.getConsumableContainer().decConsumable(record, amount);

                BrainOutServer.Controller.getClients().sendTCP(
                        new InventoryItemMovedMsg(playerData, item.getContent()));

                if (currentInstrument)
                {
                    selectFirstInstrument(poc);
                }

                updateAttachments();
                consumablesUpdated();

                return itemData;
            }
        }

        return null;
    }

}
