package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.client.editor.EditorActionMsg;
import com.desertkun.brainout.common.msg.client.editor2.*;
import com.desertkun.brainout.content.Layout;
import com.desertkun.brainout.content.SpawnTarget;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.Editor2EnabledComponent;
import com.desertkun.brainout.content.components.UserSpriteWithBlocksComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.shop.ShopCart;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.SpriteWithBlocksComponentData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.NonContactBD;
import com.desertkun.brainout.data.components.SpriteBlockComponentData;
import com.desertkun.brainout.data.components.UserSpriteWithBlocksComponentData;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.playstate.PlayStateEndGame;
import com.desertkun.brainout.server.mapsource.EmptyMapSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimerTask;
import java.util.UUID;

public class ServerEditor2Realization extends ServerRealization<GameModeEditor2>
{
    public ServerEditor2Realization(GameModeEditor2 gameMode)
    {
        super(gameMode);
    }

    @Override
    public boolean isComplete(PlayStateEndGame.GameResult gameResult)
    {
        return false;
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        return false;
    }

    public void redeliverMap()
    {
        BrainOutServer.Controller.playStateChanged();
    }

    @SuppressWarnings("unused")
    public boolean received(final SingleBlockMsg msg)
    {
        Map map = Map.Get(msg.d);

        if (map == null)
            return true;

        int x = msg.x, y = msg.y;
        String block_ = msg.block;

        BrainOutServer.PostRunnable(() ->
        {
            Block block = block_ == null ? null : BrainOutServer.ContentMgr.get(block_, Block.class);

            setBlock(map, x, y, block);
        });

        return true;
    }

    @Override
    public boolean awardScores()
    {
        return false;
    }

    private static final int BATCH_SIZE = 64;
    private static final int BATCH_PERIOD = 100;

    @SuppressWarnings("unused")
    public boolean received(final BlockRectMsg msg)
    {
        Map map = Map.Get(msg.d);

        if (map == null)
            return true;

        int x = msg.x, y = msg.y, w = msg.w, h = msg.h;
        String block_ = msg.block;

        BrainOutServer.PostRunnable(() ->
        {

            Block block = block_ == null ? null : BrainOutServer.ContentMgr.get(block_, Block.class);

            int length = w * h;

            for (int j = 0; j < h; j++)
            {
                for (int i = 0; i < w; i++)
                {
                    setBlock(map, x + i, y + j, block);
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final EditorActionMsg msg)
    {
        BrainOutServer.PostRunnable(() -> {
            switch (msg.id)
            {
                case unload:
                {
                    unload();

                    break;
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final Editor2ActiveRemoveMsg msg)
    {
        int id = msg.id;
        Map map = Map.Get(msg.d);

        if (map == null)
            return true;

        BrainOutServer.PostRunnable(() ->
        {
            ActiveData activeData = map.getActiveData(id);

            if (activeData != null)
            {
                map.removeActive(activeData, true);
            }
        });

        return true;
    }


    @SuppressWarnings("unused")
    public boolean received(final Editor2ActiveAddMsg msg)
    {
        String id = msg.id;
        float x = msg.x, y = msg.y;

        Map map = Map.Get(msg.d);

        if (map == null)
            return true;

        BrainOutServer.PostRunnable(() ->
        {
            Active active = ((Active) BrainOut.ContentMgr.get(id));

            if (active != null)
            {
                Editor2EnabledComponent cmp = active.getComponent(Editor2EnabledComponent.class);

                if (cmp == null)
                    return;

                ActiveData activeData = active.getData(map.getDimension());

                activeData.setLayer(cmp.getLayer());
                activeData.setPosition(x, y);

                map.addActive(map.generateServerId(), activeData, true);
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final CreateObjectMsg msg)
    {
        String d = msg.d;

        Map map = Map.Get(d);

        if (map == null)
            return true;

        String o = msg.o;
        int x = msg.x, y = msg.y;

        BrainOutServer.PostRunnable(() ->
        {
            Active content = BrainOutServer.ContentMgr.get(o, Active.class);

            if (content.hasComponent(SpriteWithBlocksComponent.class))
            {
                SpriteWithBlocksComponent sp = content.getComponent(SpriteWithBlocksComponent.class);

                if (sp.validateBlocksForAdding(map, x, y))
                {
                    ActiveData activeData = content.getData(d);

                    activeData.setLayer(sp.getBlocksLayer());
                    activeData.setzIndex(sp.getzIndex());
                    activeData.setPosition(x, y);

                    map.addActive(map.generateServerId(), activeData, true, true, false);

                    for (int j = 0; j < sp.getHeight(); j++)
                    {
                        for (int i = 0; i < sp.getWidth(); i++)
                        {
                            int x_ = x + i, y_ = y + j;

                            BlockData b;

                            if (sp.hasOnlyOneUnderlyingBlock())
                            {
                                Block underlyingBlock = sp.getUnderlyingBlock();
                                b = underlyingBlock.getBlock();
                            }
                            else
                            {
                                Block underlyingBlock = sp.getUnderlyingBlockAt(i, j);
                                b = underlyingBlock.getBlock();
                            }

                            SpriteBlockComponentData sbc = b.getComponent(SpriteBlockComponentData.class);

                            if (sbc != null)
                            {
                                sbc.setSprite(activeData);
                            }

                            map.setBlock(x_, y_, b, sp.getBlocksLayer(), false, false);
                        }
                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final CreateUserImageMsg msg)
    {
        int x = msg.x, y = msg.y, w = msg.w, h = msg.h;
        String sprite = msg.s;
        String contentName = msg.c;

        if (w <= 0 || h <= 0 || w > 8 || h > 8)
            return true;

        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            Active content = BrainOutServer.ContentMgr.get(contentName, Active.class);

            if (content.hasComponent(UserSpriteWithBlocksComponent.class))
            {
                UserSpriteWithBlocksComponent sp = content.getComponent(UserSpriteWithBlocksComponent.class);

                if (sp.validateBlocksForAdding(map, x, y, w, h))
                {
                    ActiveData activeData = content.getData(msg.d);

                    UserSpriteWithBlocksComponentData us =
                        activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                    us.init(sprite, w, h);

                    activeData.setLayer(sp.getBlocksLayer());
                    activeData.setzIndex(sp.getzIndex());
                    activeData.setPosition(x, y);

                    map.addActive(map.generateServerId(), activeData, true, true, false);

                    for (int j = 0; j < h; j++)
                    {
                        for (int i = 0; i < w; i++)
                        {
                            int x_ = x + i, y_ = y + j;

                            BlockData b;

                            Block underlyingBlock = sp.getUnderlyingBlock();
                            b = underlyingBlock.getBlock();

                            SpriteBlockComponentData sbc = b.getComponent(SpriteBlockComponentData.class);

                            if (sbc != null)
                            {
                                sbc.setSprite(activeData);
                            }

                            map.setBlock(x_, y_, b, sp.getBlocksLayer(), false, false);
                        }
                    }
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final MoveObjectsMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ObjectSet<ActiveData> items = new ObjectSet<>();

            for (int objectId : msg.o)
            {
                ActiveData activeData = map.getActives().get(objectId);

                if (activeData == null)
                    return;

                if (activeData.getComponent(SpriteWithBlocksComponentData.class) == null &&
                    activeData.getComponent(UserSpriteWithBlocksComponentData.class) == null)
                    return;

                items.add(activeData);
            }

            for (ActiveData activeData : items)
            {
                SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

                if (spi != null)
                {
                    SpriteWithBlocksComponent sp = spi.getContentComponent();

                    int x = (int)(activeData.getX() + msg.x), y = (int)(activeData.getY() + msg.y);

                    if (!sp.validateBlocksForAdding(map, x, y, items))
                        return;
                }
                else
                {
                    UserSpriteWithBlocksComponentData us =
                        activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                    if (us != null)
                    {
                        int x = (int)(activeData.getX() + msg.x), y = (int)(activeData.getY() + msg.y);

                        if (!us.validateBlocksForAdding(map, x, y, items))
                            return;
                    }
                    else
                    {
                        return;
                    }
                }
            }

            // clear up the blocks

            for (ActiveData activeData : items)
            {
                SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

                if (spi != null)
                {
                    SpriteWithBlocksComponent sp = spi.getContentComponent();

                    int layer = sp.getBlocksLayer();

                    int x_ = (int) activeData.getX(), y_ = (int) activeData.getY();

                    for (int j = 0; j < sp.getHeight(); j++)
                    {
                        for (int i = 0; i < sp.getWidth(); i++)
                        {
                            int x = x_ + i, y = y_ + j;

                            map.setBlock(x, y, null, layer, false, false);
                        }
                    }
                }
                else
                {
                    UserSpriteWithBlocksComponentData us =
                            activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                    if (us != null)
                    {
                        UserSpriteWithBlocksComponent sp = us.getContentComponent();

                        int layer = sp.getBlocksLayer();

                        int x_ = (int) activeData.getX(), y_ = (int) activeData.getY();

                        for (int j = 0; j < us.getHeight(); j++)
                        {
                            for (int i = 0; i < us.getWidth(); i++)
                            {
                                int x = x_ + i, y = y_ + j;

                                map.setBlock(x, y, null, layer, false, false);
                            }
                        }
                    }
                }
            }

            // place up the new ones

            for (ActiveData activeData : items)
            {
                SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

                if (spi != null)
                {
                    SpriteWithBlocksComponent sp = spi.getContentComponent();

                    int layer = sp.getBlocksLayer();

                    int x_ = (int) (activeData.getX() + msg.x), y_ = (int) (activeData.getY() + msg.y);

                    activeData.setLayer(sp.getBlocksLayer());
                    activeData.setzIndex(sp.getzIndex());
                    activeData.setPosition(x_, y_);
                    activeData.updated();

                    for (int j = 0; j < sp.getHeight(); j++)
                    {
                        for (int i = 0; i < sp.getWidth(); i++)
                        {
                            int x = x_ + i, y = y_ + j;

                            BlockData b;

                            if (sp.hasOnlyOneUnderlyingBlock())
                            {
                                Block underlyingBlock = sp.getUnderlyingBlock();
                                b = underlyingBlock.getBlock();
                            } else
                            {
                                Block underlyingBlock = sp.getUnderlyingBlockAt(i, j);
                                b = underlyingBlock.getBlock();
                            }

                            SpriteBlockComponentData sbc = b.getComponent(SpriteBlockComponentData.class);

                            if (sbc != null)
                            {
                                sbc.setSprite(activeData);
                            }

                            map.setBlock(x, y, b, sp.getBlocksLayer(), false, false);
                        }
                    }
                }
                else
                {
                    UserSpriteWithBlocksComponentData us =
                        activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                    UserSpriteWithBlocksComponent sp = us.getContentComponent();

                    int layer = sp.getBlocksLayer();
                    Block underlyingBlock = sp.getUnderlyingBlock();

                    int x_ = (int) (activeData.getX() + msg.x), y_ = (int) (activeData.getY() + msg.y);

                    activeData.setLayer(sp.getBlocksLayer());
                    activeData.setzIndex(sp.getzIndex());
                    activeData.setPosition(x_, y_);
                    activeData.updated();

                    for (int j = 0; j < us.getHeight(); j++)
                    {
                        for (int i = 0; i < us.getWidth(); i++)
                        {
                            int x = x_ + i, y = y_ + j;

                            BlockData b = underlyingBlock.getBlock();

                            SpriteBlockComponentData sbc = b.getComponent(SpriteBlockComponentData.class);

                            if (sbc != null)
                            {
                                sbc.setSprite(activeData);
                            }

                            map.setBlock(x, y, b, sp.getBlocksLayer(), false, false);
                        }
                    }
                }
            }

        });

        return true;
    }

    private boolean validateSpawnPosition(float x, float y, String dimension)
    {
        final int WIDTH = 2, HEIGHT = 3;

        Map map = Map.Get(dimension);

        if (map == null)
            return false;

        float sX = x - (float)WIDTH / 2.0f,
              sY = y - (float)HEIGHT / 2.0f;

        for (int j = 0; j < HEIGHT + 1; j++)
        {
            for (int i = 0; i < WIDTH + 1; i++)
            {
                float x_ = sX + (float)i,
                      y_ = sY + (float)j;

                BlockData blockData = map.getBlockAt(x_, y_, Constants.Layers.BLOCK_LAYER_FOREGROUND);

                if (blockData != null && (!(blockData instanceof NonContactBD)))
                {
                    return false;
                }
            }
        }

        return true;
    }

    private void unload()
    {
        BrainOutServer.Controller.setMapSource(new EmptyMapSource(GameMode.ID.editor2));
        BrainOutServer.PackageMgr.unloadPackages(true);
        BrainOutServer.Controller.next(null);
    }

    @SuppressWarnings("unused")
    public boolean received(final SpawnEditor2Msg msg)
    {
        final PlayerClient client = getMessageClient();
        final String dimension = Map.FindDimension(msg.d);
        final float x = msg.x, y = msg.y;

        BrainOutServer.PostRunnable(() ->
        {
            if (client == null)
                return;

            if (client.isAlive())
                return;

            if (!validateSpawnPosition(x, y, dimension))
                return;

            Spawnable spawnAt = new Spawnable()
            {
                @Override
                public float getSpawnX()
                {
                    return x;
                }

                @Override
                public float getSpawnY()
                {
                    return y;
                }

                @Override
                public Team getTeam()
                {
                    return BrainOutServer.ContentMgr.get("team-blue", Team.class);
                }

                @Override
                public float getSpawnRange()
                {
                    return 0;
                }

                @Override
                public String getDimension()
                {
                    return dimension;
                }

                @Override
                public boolean canSpawn(Team teamFor)
                {
                    return true;
                }

                @Override
                public SpawnTarget getTarget()
                {
                    return SpawnTarget.normal;
                }
            };

            ShopCart shopCart = client.getShopCart();
            shopCart.clear();

            ClientProfile profile = client.getProfile();
            if (profile == null)
                return;

            Layout selectedLayout = profile.getLayout();

            if (selectedLayout == null)
            {
                selectedLayout = BrainOut.ContentMgr.get("layout-1", Layout.class);
            }

            shopCart.initSelection(profile, selectedLayout, false);

            client.spawn(spawnAt);
        });

        return true;
    }

    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public boolean canDropConsumable(Client playerClient, ConsumableItem item)
    {
        return false;
    }

    @Override
    public float getSpawnDelay()
    {
        return 0;
    }

    @SuppressWarnings("unused")
    public boolean received(final CopyObjectsMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ObjectSet<ActiveData> items = new ObjectSet<>();

            for (int objectId : msg.o)
            {
                ActiveData activeData = map.getActives().get(objectId);

                if (activeData == null)
                    return;

                if (activeData.getComponent(SpriteWithBlocksComponentData.class) == null &&
                    activeData.getComponent(UserSpriteWithBlocksComponentData.class) == null)
                    return;

                items.add(activeData);
            }

            for (ActiveData activeData : items)
            {
                SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

                if (spi != null)
                {
                    SpriteWithBlocksComponent sp = spi.getContentComponent();

                    int x = (int)(activeData.getX() + msg.x), y = (int)(activeData.getY() + msg.y);

                    if (!sp.validateBlocksForAdding(map, x, y, null))
                        return;
                }
                else
                {
                    UserSpriteWithBlocksComponentData us = activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                    if (us != null)
                    {
                        UserSpriteWithBlocksComponent sp = us.getContentComponent();

                        int x = (int)(activeData.getX() + msg.x), y = (int)(activeData.getY() + msg.y);

                        if (!us.validateBlocksForAdding(map, x, y, null))
                            return;
                    }
                    else
                    {
                        return;
                    }
                }
            }

            JsonReader reader = new JsonReader();

            // place up the new ones

            for (ActiveData original : items)
            {
                String d = Data.ComponentSerializer.toJson(original, Data.ComponentWriter.TRUE, -1);
                ActiveData activeData = map.newActiveData(map.getJson(), reader.parse(d));

                int x_ = (int)(original.getX() + msg.x), y_ = (int)(original.getY() + msg.y);
                activeData.setPosition(x_, y_);
                map.addActive(map.generateServerId(), activeData, true, true, false);

                SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

                if (spi != null)
                {
                    SpriteWithBlocksComponent sp = spi.getContentComponent();

                    int layer = sp.getBlocksLayer();

                    for (int j = 0; j < sp.getHeight(); j++)
                    {
                        for (int i = 0; i < sp.getWidth(); i++)
                        {
                            int x = x_ + i, y = y_ + j;

                            BlockData b;

                            if (sp.hasOnlyOneUnderlyingBlock())
                            {
                                Block underlyingBlock = sp.getUnderlyingBlock();
                                b = underlyingBlock.getBlock();
                            }
                            else
                            {
                                Block underlyingBlock = sp.getUnderlyingBlockAt(i, j);
                                b = underlyingBlock.getBlock();
                            }

                            SpriteBlockComponentData sbc = b.getComponent(SpriteBlockComponentData.class);

                            if (sbc != null)
                            {
                                sbc.setSprite(activeData);
                            }

                            map.setBlock(x, y, b, sp.getBlocksLayer(), false, false);
                        }
                    }
                }
                else
                {
                    UserSpriteWithBlocksComponentData us =
                        activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                    if (us != null)
                    {
                        UserSpriteWithBlocksComponent sp = us.getContentComponent();
                        Block underlyingBlock = sp.getUnderlyingBlock();

                        int layer = sp.getBlocksLayer();

                        for (int j = 0; j < us.getHeight(); j++)
                        {
                            for (int i = 0; i < us.getWidth(); i++)
                            {
                                int x = x_ + i, y = y_ + j;

                                BlockData b = underlyingBlock.getBlock();

                                SpriteBlockComponentData sbc = b.getComponent(SpriteBlockComponentData.class);

                                if (sbc != null)
                                {
                                    sbc.setSprite(activeData);
                                }

                                map.setBlock(x, y, b, sp.getBlocksLayer(), false, false);
                            }
                        }
                    }
                    else
                    {
                        return;
                    }
                }
            }

        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final RemoveUserImageMsg msg)
    {
        String name = msg.name;

        BrainOutServer.PostRunnable(() ->
        {
            removeImageExtension(name);
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final RemoveObjectMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            ActiveData activeData = map.getActives().get(msg.o);

            if (activeData == null)
                return;

            SpriteWithBlocksComponentData spi = activeData.getComponent(SpriteWithBlocksComponentData.class);

            if (spi != null)
            {
                SpriteWithBlocksComponent sp = spi.getContentComponent();

                int x_ = (int)activeData.getX(), y_ = (int)activeData.getY();

                for (int j = 0; j < sp.getHeight(); j++)
                {
                    for (int i = 0; i < sp.getWidth(); i++)
                    {
                        int x = x_ + i, y = y_ + j;

                        map.setBlock(x, y, null, sp.getBlocksLayer(), false, false);
                    }
                }

                map.removeActive(activeData, true);
            }
            else
            {
                UserSpriteWithBlocksComponentData us = activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                if (us != null)
                {
                    UserSpriteWithBlocksComponent sp = us.getContentComponent();

                    int x_ = (int)activeData.getX(), y_ = (int)activeData.getY();

                    for (int j = 0; j < us.getHeight(); j++)
                    {
                        for (int i = 0; i < us.getWidth(); i++)
                        {
                            int x = x_ + i, y = y_ + j;

                            map.setBlock(x, y, null, sp.getBlocksLayer(), false, false);
                        }
                    }

                    map.removeActive(activeData, true);
                }
            }
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final MultipleBlocksMsg msg)
    {
        BrainOutServer.PostRunnable(() ->
        {
            Map map = Map.Get(msg.d);

            if (map == null)
                return;

            Block block = msg.block == null ? null : BrainOutServer.ContentMgr.get(msg.block, Block.class);

            int length = msg.x.length;

            if (msg.y.length != length)
                return;

            if (false)
            {
                Queue<MultipleBlocksMsg.Point> points = new Queue<>();

                for (int i = 0; i < length; i++)
                {
                    int x = msg.x[i];
                    int y = msg.y[i];

                    points.addLast(new MultipleBlocksMsg.Point(x, y));
                }

                BrainOutServer.Timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        BrainOutServer.PostRunnable(() ->
                        {
                            int size = Math.min(BATCH_SIZE, points.size);

                            for (int i = 0; i < size; i++)
                            {
                                MultipleBlocksMsg.Point point = points.removeFirst();
                                setBlock(map, point.getX(), point.getY(), block);
                            }

                            if (points.size == 0)
                                cancel();
                        });
                    }
                }, 0, BATCH_PERIOD);
            }
            else
            {
                for (int i = 0; i < length; i++)
                {
                    int x = msg.x[i];
                    int y = msg.y[i];

                    setBlock(map, x, y, block);
                }
            }
        });

        return true;
    }

    private boolean validateBlock(BlockData b)
    {
        if (b != null)
        {
            SpriteBlockComponentData sbc = b.getComponent(SpriteBlockComponentData.class);

            return sbc == null;
        }

        return true;
    }

    private void setBlock(Map map, int x, int y, Block block)
    {
        if (block == null)
        {
            {
                BlockData b = map.getBlock(x, y, Constants.Layers.BLOCK_LAYER_BACKGROUND);

                if (b != null && validateBlock(b))
                    map.setBlock(x, y, null, Constants.Layers.BLOCK_LAYER_BACKGROUND, false, false);
            }

            {
                BlockData b = map.getBlock(x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

                if (b != null && validateBlock(b))
                    map.setBlock(x, y, null, Constants.Layers.BLOCK_LAYER_FOREGROUND, false, false);
            }
        }
        else
        {

            Editor2EnabledComponent e2 = block.getComponent(Editor2EnabledComponent.class);

            if (e2 == null)
                return;

            int layer = e2.getLayer();

            if (!validateBlock(map.getBlock(x, y, layer)))
                return;

            BlockData blockData = block.getBlock();
            map.setBlock(x, y, blockData, layer, false, false);
        }
    }

    @Override
    public boolean canTakeFlags()
    {
        return false;
    }

    public boolean addImageExtension(Active active, String update, InputStream image)
    {
        if (active == null)
            return false;

        UserSpriteWithBlocksComponent us = active.getComponent(UserSpriteWithBlocksComponent.class);

        if (us == null)
            return false;

        Map defaultMap = Map.GetDefault();

        if (defaultMap == null)
            return false;

        BufferedImage bufferedImage;

        try
        {
            bufferedImage = ImageIO.read(image);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        if (bufferedImage.getWidth() > 128 || bufferedImage.getHeight() > 128)
            return false;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try
        {
            ImageIO.write(bufferedImage, "PNG", outputStream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        if (update != null)
        {
            byte[] ext = defaultMap.getExtension(update);

            if (ext != null)
            {
                defaultMap.addExtension(update, outputStream.toByteArray());
                BrainOutServer.Controller.playStateChanged();
            }
        }
        else
        {
            defaultMap.addExtension(
                us.getExtName() + "-" + UUID.randomUUID().toString() + ".png", outputStream.toByteArray());

            BrainOutServer.Controller.playStateChanged();
        }

        return true;
    }

    public void removeImageExtension(String name)
    {
        Map defaultMap = Map.GetDefault();

        if (defaultMap == null)
            return;

        byte[] ext = defaultMap.getExtension(name);

        if (ext == null)
            return;

        Queue<ActiveData> removeList = new Queue<>();

        for (Map map : Map.All())
        {
            for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.USER_IMAGE, false))
            {
                UserSpriteWithBlocksComponentData us =
                    activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                if (us == null)
                    continue;

                if (!us.getSprite().equals(name))
                    continue;

                removeList.addLast(activeData);
            }

            for (ActiveData activeData : removeList)
            {
                UserSpriteWithBlocksComponentData us =
                        activeData.getComponent(UserSpriteWithBlocksComponentData.class);

                if (us != null)
                {
                    int w = us.getWidth(), h = us.getHeight(),
                        x = (int)activeData.getX(), y = (int)activeData.getY();

                    for (int j = 0; j < h; j++)
                    {
                        for (int i = 0; i < w; i++)
                        {
                            int x_ = x + i,
                                y_ = y + j;

                            BlockData blockData = map.getBlock(x_, y_, us.getContentComponent().getBlocksLayer());

                            if (blockData != null)
                            {
                                SpriteBlockComponentData b = blockData.getComponent(SpriteBlockComponentData.class);
                                if (b != null && b.getSprite(map) == activeData)
                                {
                                    map.getBlocks().set(x_, y_, null,
                                        us.getContentComponent().getBlocksLayer(), false);
                                }
                            }
                        }
                    }
                }

                map.removeActive(activeData, false, true, false);
            }

            removeList.clear();
        }

        defaultMap.removeExtension(name);

        BrainOutServer.Controller.playStateChanged();
    }

    @Override
    public boolean isDeathDropEnabled(PlayerData playerData)
    {
        return false;
    }
}
