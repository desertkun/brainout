package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.ServerActiveVisibilityMsg;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.ServerTeamVisibilityComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.server.ServerController;

@Reflect("ServerTeamVisibilityComponent")
@ReflectAlias("data.components.ServerTeamVisibilityComponentData")
public class ServerTeamVisibilityComponentData extends VisibilityComponentData<ServerTeamVisibilityComponent>
{
    private Vector2 tmp, tmp2;
    private ObjectMap<Integer, Long> visibility;
    private ObjectSet<Integer> currentlyVisible, currentlyVisibleDirectly;
    private Array<Integer> toRemove;
    private float timer;

    public enum VisibilityResult
    {
        invisible,
        indirect,
        direct
    }

    public ServerTeamVisibilityComponentData(ComponentObject componentObject,
                                             ServerTeamVisibilityComponent contentComponent)
    {
        super(componentObject, contentComponent);

        this.tmp = new Vector2();
        this.tmp2 = new Vector2();
        this.visibility = new ObjectMap<>();
        this.currentlyVisible = new ObjectSet<>();
        this.currentlyVisibleDirectly = new ObjectSet<>();
        this.toRemove = new Array<>();
        this.timer = 0;
    }

    @Override
    public boolean isVisibleTo(int to)
    {
        Map map = getMap();
        ActiveData playerData = ((ActiveData) getComponentObject());

        if (map != null && playerData != null)
        {
            if (to != playerData.getOwnerId())
            {
                ChunkData chunkData = map.getChunkAt((int) playerData.getX(), (int) playerData.getY());

                if (chunkData != null && chunkData.hasFlag(ChunkData.ChunkFlag.hideOthers))
                {
                    return false;
                }
            }
            else
            {
                return true;
            }
        }

        Client client = BrainOutServer.Controller.getClients().get(to);

        if (client == null)
            return false;

        if (currentlyVisible.contains(client.getId()) || currentlyVisibleDirectly.contains(client.getId()))
        {
            return true;
        }

        long tm = visibility.get(client.getId(), 0L);

        if (tm == 0)
            return false;

        return System.currentTimeMillis() < tm;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    public boolean isVisibleTo(ActiveData activeData)
    {
        return isVisibleTo(activeData.getOwnerId());
    }

    public boolean isVisibleDirectlyRightNow(Client client)
    {
        return currentlyVisibleDirectly.contains(client.getId());
    }

    public boolean isVisibleTo(PlayerClient playerClient)
    {
        if (currentlyVisibleDirectly.contains(playerClient.getId()) ||
            currentlyVisible.contains(playerClient.getId()))
        {
            return true;
        }

        long tm = visibility.get(playerClient.getId(), 0L);

        if (tm == 0)
            return false;

        return System.currentTimeMillis() < tm;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    public VisibilityResult processVisibility(Client toClient)
    {
        ServerController CC = BrainOutServer.Controller;

        ActiveData currentPlayerData = ((ActiveData) getComponentObject());

        if (!toClient.isInitialized())
            return VisibilityResult.invisible;

        GameMode gameMode = CC.getGameMode();

        if (gameMode == null)
            return VisibilityResult.invisible;

        ServerMap map = getMap(ServerMap.class);

        if (map == null)
            return VisibilityResult.invisible;

        boolean bot = BrainOutServer.Controller.getClients().get(currentPlayerData.getOwnerId()) instanceof BotClient;

        ChunkData chunkData = map.getChunkAt((int)currentPlayerData.getX(), (int)currentPlayerData.getY());
        if (chunkData != null && chunkData.hasFlag(ChunkData.ChunkFlag.hideOthers))
        {
            return VisibilityResult.invisible;
        }

        ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());
        if (serverRealization.spectatorsCanSeeEnemies() && toClient.isSpectator())
        {
            return VisibilityResult.direct;
        }

        if (currentPlayerData.getOwnerId() >= 0)
        {
            if (!gameMode.isEnemies(currentPlayerData.getOwnerId(), toClient.getId()))
                return VisibilityResult.direct;
        }

        PlayerData toPlayerData = toClient.getPlayerData();

        if (toPlayerData != null)
        {
            if (!toPlayerData.getDimension().equals(currentPlayerData.getDimension()))
                return VisibilityResult.invisible;

            if (!gameMode.isEnemiesActive(toPlayerData, currentPlayerData))
                return VisibilityResult.direct;

            SimplePhysicsComponentData phy = toPlayerData.getComponentWithSubclass(SimplePhysicsComponentData.class);
            SimplePhysicsComponentData otherPhy = currentPlayerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            if (phy == null || otherPhy == null)
            {
                return VisibilityResult.invisible;
            }

            float myHeight = phy.getHalfSize().y, otherHeight = otherPhy.getHalfSize().y;

            tmp.set(currentPlayerData.getX(), currentPlayerData.getY() + otherHeight);
            tmp.sub(toPlayerData.getX(), toPlayerData.getY() + myHeight);
            float len = tmp.len();

            if (bot && len > 64)
            {
                return VisibilityResult.invisible;
            }

            tmp.nor();

            PlayerComponentData cpc = currentPlayerData.getComponent(PlayerComponentData.class);

            if (cpc == null)
            {
                return VisibilityResult.invisible;
            }

            tmp2.set(1.0f, 0f);
            tmp2.setAngle(toPlayerData.getAngle());

            if (tmp2.dot(tmp) < 0 && cpc.getState() == Player.State.sit)
            {
                // we are behind the player and sitting
                return VisibilityResult.invisible;
            }

            if (!map.trace(toPlayerData.getX(),
                    toPlayerData.getY() + myHeight,
                    Constants.Layers.BLOCK_LAYER_UPPER, tmp.angleDeg(), len, null) &&
                !map.trace(toPlayerData.getX(),
                    toPlayerData.getY() + myHeight,
                    Constants.Layers.BLOCK_LAYER_FOREGROUND, tmp.angleDeg(), len, null))
            {
                return VisibilityResult.direct;
            }

            tmp.set(currentPlayerData.getX(), currentPlayerData.getY() - otherHeight);
            tmp.sub(toPlayerData.getX(), toPlayerData.getY() + myHeight);

            if (!map.trace(toPlayerData.getX(),
                    toPlayerData.getY() + myHeight,
                    Constants.Layers.BLOCK_LAYER_UPPER, tmp.angleDeg(), len, null) &&
                !map.trace(toPlayerData.getX(),
                    toPlayerData.getY() + myHeight,
                    Constants.Layers.BLOCK_LAYER_FOREGROUND, tmp.angleDeg(), len, null))
            {
                return VisibilityResult.direct;
            }
        }

        if (gameMode.isTeamVisibilityEnabled())
        {
            for (ObjectMap.Entry<Integer, Client> entry : CC.getClients())
            {
                Client client = entry.value;

                if (client == toClient)
                    continue;
                if (client.getId() == currentPlayerData.getOwnerId())
                    continue;
                if (gameMode.isEnemies(client.getId(), toClient.getId()))
                    continue;
                if (client.isSpectator())
                    continue;

                // if ve're currently visible to a friend of toClient, and isTeamVisibilityEnabled is
                // enabled, then we're also visible to toClient
                if (isVisibleDirectlyRightNow(client))
                    return VisibilityResult.indirect;
            }
        }

        return VisibilityResult.invisible;
    }

    private ServerActiveVisibilityMsg generateVisibilityMessage(ActiveData activeData, boolean visible)
    {
        SimplePhysicsComponentData phy = activeData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        float speedX, speedY;

        if (phy == null)
        {
            speedX = 0;
            speedY = 0;
        }
        else
        {
            Vector2 speed = phy.getSpeed();
            speedX = speed.x;
            speedY = speed.y;
        }

        return new ServerActiveVisibilityMsg(
            activeData.getId(),
            activeData.getX(),
            activeData.getY(),
            speedX, speedY, activeData.getAngle(), activeData.getDimension(), visible
        );
    }

    @Override
    public void update(float dt)
    {
        timer -= dt;

        if (timer < 0)
        {
            timer = 0.25f;

            processVisibility(false);
        }
    }

    @Override
    public void init()
    {
        super.init();

        BrainOutServer.PostRunnable(() ->
            processVisibility(false));
    }

    private long getDetectionTime()
    {
        return (long)(getContentComponent().getDetectionTime() * 1000L);
    }

    private void processVisibility(boolean forceVisible)
    {
        ActiveData currentPlayerData = ((ActiveData) getComponentObject());

        Map map = currentPlayerData.getMap();

        if (map == null)
            return;

        ServerController CC = BrainOutServer.Controller;

        for (ObjectMap.Entry<Integer, Client> entry : new ObjectMap.Entries<>(BrainOutServer.Controller.getClients()))
        {
            Client client = entry.value;

            PlayerClient asPlayer = client instanceof PlayerClient ? ((PlayerClient) client) : null;

            if (client.getId() == currentPlayerData.getOwnerId())
                continue;

            processVisibilityClient(client, forceVisible, asPlayer);
        }

        // cleanup

        for (ObjectMap.Entry<Integer, Long> entry : visibility)
        {
            Client client = CC.getClients().get(entry.key);

            if (client == null)
            {
                toRemove.add(entry.key);
                continue;
            }

            long tm = entry.value;
            if (tm < System.currentTimeMillis())
            {
                toRemove.add(entry.key);
            }
        }

        if (toRemove.size > 0)
        {
            for (Integer id : toRemove)
            {
                visibility.remove(id);
                Client client = BrainOutServer.Controller.getClients().get(id);

                if (!(client instanceof PlayerClient))
                    continue;

                ((PlayerClient) client).sendTCP(generateVisibilityMessage(currentPlayerData, false));
            }

            toRemove.clear();
        }
    }

    public void processVisibilityClient(Client client, boolean forceVisible, PlayerClient asPlayer)
    {
        ActiveData currentPlayerData = ((ActiveData) getComponentObject());

        VisibilityResult was, now;

        if (currentlyVisibleDirectly.contains(client.getId()))
        {
            was = VisibilityResult.direct;
        }
        else if (currentlyVisible.contains(client.getId()))
        {
            was = VisibilityResult.indirect;
        }
        else
        {
            was = VisibilityResult.invisible;
        }

        if (forceVisible)
        {
            now = VisibilityResult.direct;
        }
        else
        {
            now = processVisibility(client);
        }

        if (was != now)
        {
            switch (now)
            {
                case direct:
                {
                    if (asPlayer != null)
                        asPlayer.sendTCP(generateVisibilityMessage(currentPlayerData, true));
                    currentlyVisibleDirectly.add(client.getId());
                    currentlyVisible.remove(client.getId());
                    visibility.remove(client.getId());
                    break;
                }
                case indirect:
                {
                    if (asPlayer != null)
                        asPlayer.sendTCP(generateVisibilityMessage(currentPlayerData, true));
                    currentlyVisible.add(client.getId());
                    currentlyVisibleDirectly.remove(client.getId());
                    visibility.remove(client.getId());
                    break;
                }
                case invisible:
                {
                    currentlyVisible.remove(client.getId());
                    currentlyVisibleDirectly.remove(client.getId());
                    visibility.put(client.getId(), System.currentTimeMillis() + getDetectionTime());
                    break;
                }
            }
        }
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    public void show()
    {
        processVisibility(true);
    }

    public void hide()
    {
        ActiveData currentPlayerData = ((ActiveData) getComponentObject());

        Map map = currentPlayerData.getMap();

        if (map == null)
            return;

        for (ObjectMap.Entry<Integer, Long> entry : visibility)
        {
            int id = entry.key;
            Client client = BrainOutServer.Controller.getClients().get(id);

            if (!(client instanceof PlayerClient))
                continue;

            ((PlayerClient) client).sendTCP(generateVisibilityMessage(currentPlayerData, false));
        }

        visibility.clear();
        currentlyVisibleDirectly.clear();
    }
}
