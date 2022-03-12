package com.desertkun.brainout.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.CanShootThroughComponent;
import com.desertkun.brainout.content.components.IgnoreWayPointsComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.ServerPortalComponentData;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.utils.DistanceUtils;
import com.desertkun.brainout.utils.Pair;
import com.desertkun.brainout.utils.PausableThreadPoolExecutor;
import com.desertkun.brainout.utils.SupercoverUtils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class WayPointMap
{
    private static final int PLAYER_W = 2, PLAYER_H = 3;
    private static final int CACHE_GRID_SIZE = 8;
    private static PausableThreadPoolExecutor ThreadPoolExecutor = new PausableThreadPoolExecutor(1);
    private static Vector2 tmp = new Vector2();

    private int nextWayPointId;
    private ServerMap map;
    private ObjectMap<Integer, WayPoint> wayPoints;
    private ObjectMap<Integer, Queue<WayPoint>> chunkWayPoints;
    private Random random;
    private Queue<Runnable> locked;
    private ObjectSet<Integer> dirty;
    private float dirtyTimer;
    private AtomicInteger findPathLock;

    public class WayPointCacheEdge
    {
        public WayPoint a;
        public WayPoint b;
    }

    public class BlockCoordinates
    {
        public int x, y;

        BlockCoordinates(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode()
        {
            return x ^ y;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof BlockCoordinates)
            {
                return ((BlockCoordinates) obj).x == x && ((BlockCoordinates) obj).y == y;
            }

            return false;
        }
    }

    private Array<Queue<WayPointCacheEdge>> cacheGrid;
    private int cacheGridWidth, cacheGridHeight;

    public WayPointMap(ServerMap map)
    {
        this.wayPoints = new ObjectMap<>();
        this.chunkWayPoints = new ObjectMap<>();
        this.map = map;

        this.findPathLock = new AtomicInteger(0);
    }

    public ServerMap getMap()
    {
        return map;
    }

    public Queue<WayPointCacheEdge> getCacheAt(float x_, float y_)
    {
        int x = (int)x_ / CACHE_GRID_SIZE, y = (int)y_ / CACHE_GRID_SIZE;

        if (x < 0 || y < 0 || x >= cacheGridWidth || y >= cacheGridHeight)
            return null;

        int i = x + y * cacheGridWidth;

        Queue<WayPointCacheEdge> q = cacheGrid.get(i);

        if (q == null)
        {
            q = new Queue<>();
            cacheGrid.set(i, q);
        }

        return q;
    }

    public interface WayPointReceiver
    {
        void got(WayPoint wayPoint, float distance);
    }

    private boolean getClosestPointOnSegment(
            float sx1, float sy1,
            float sx2, float sy2,
            float px, float py,
            Vector2 closestPoint)
    {
        double xDelta = sx2 - sx1;
        double yDelta = sy2 - sy1;

        if ((xDelta == 0) && (yDelta == 0))
        {
            return false;
        }

        double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

        if (u < 0)
        {
            closestPoint.set(sx1, sy1);
        }
        else if (u > 1)
        {
            closestPoint.set(sx2, sy2);
        }
        else
        {
            closestPoint.set((float)(sx1 + u * xDelta), (float)(sy1 + u * yDelta));
        }

        return true;
    }

    private void queryWayPointsAt(float x_, float y_, float distance, WayPointReceiver receiver)
    {
        int x = (int)x_ / CACHE_GRID_SIZE, y = (int)y_ / CACHE_GRID_SIZE;
        Vector2 tmp = new Vector2();

        long t = System.currentTimeMillis();

        for (int j = y - 1; j <= y + 1; j++)
        {
            for (int i = x - 1; i <= x + 1; i++)
            {
                if (i < 0 || j < 0 || i >= cacheGridWidth || j >= cacheGridHeight)
                    continue;

                int index = i + j * cacheGridWidth;
                Queue<WayPointCacheEdge> q = cacheGrid.get(index);

                if (q != null)
                {
                    for (WayPointCacheEdge edge : q)
                    {
                        if (!getClosestPointOnSegment(
                                edge.a.getX(), edge.a.getY(),
                                edge.b.getX(), edge.b.getY(),
                                x_, y_, tmp))
                            continue;

                        float pointX = tmp.x, pointY = tmp.y;
                        float wd2 = Vector2.dst(pointX, pointY, x_, y_);

                        if (wd2 > distance)
                            continue;

                        float da = Vector2.dst(edge.a.getX(), edge.a.getY(), x_, y_);
                        if (da < distance)
                            receiver.got(edge.a, da);

                        float db = Vector2.dst(edge.b.getX(), edge.b.getY(), x_, y_);
                        if (db < distance)
                            receiver.got(edge.b, db);
                    }
                }
            }
        }

        long now = System.currentTimeMillis();

        if (now - t > 10)
        {
            System.out.println("queryWayPointsAt took " + (now - t));
        }
    }

    public WayPoint getClosestWayPoint(float x_, float y_, float distance, Vector2 intermediateOut)
    {
        int x = (int)x_ / CACHE_GRID_SIZE, y = (int)y_ / CACHE_GRID_SIZE;
        WayPoint closestOne = null;
        Vector2 tmp = new Vector2();

        for (int j = y - 1; j <= y + 1; j++)
        {
            for (int i = x - 1; i <= x + 1; i++)
            {
                if (i < 0 || j < 0 || i >= cacheGridWidth || j >= cacheGridHeight)
                    continue;

                int index = i + j * cacheGridWidth;
                Queue<WayPointCacheEdge> q = cacheGrid.get(index);

                if (q != null)
                {
                    for (WayPointCacheEdge edge : new Queue.QueueIterator<>(q))
                    {
                        if (!getClosestPointOnSegment(
                                edge.a.getX(), edge.a.getY(),
                                edge.b.getX(), edge.b.getY(),
                                x_, y_, tmp))
                            continue;

                        float pointX = tmp.x, pointY = tmp.y;
                        float wd2 = Vector2.dst(pointX, pointY, x_, y_);

                        if (wd2 > distance)
                            continue;

                        WayPoint wayPoint = Vector2.dst2(x_, y_, edge.a.getX(), edge.a.getY()) <
                                Vector2.dst2(x_, y_, edge.b.getX(), edge.b.getY()) ? edge.a : edge.b;

                        tmp.set(x_ - pointX, y_ - pointY);

                        if (map.trace(pointX, pointY,
                                Constants.Layers.BLOCK_LAYER_FOREGROUND,
                                tmp.angleDeg(), tmp.len(), null))
                            continue;

                        distance = wd2;
                        closestOne = wayPoint;
                        intermediateOut.set(pointX, pointY);
                    }
                }
            }
        }

        return closestOne;
    }

    private static class WayPointMatrix extends Array<WayPoint>
    {
        public final int width;
        public final int height;

        WayPointMatrix(int width, int height)
        {
            super(width * height);
            setSize(width * height);

            this.width = width;
            this.height = height;
        }

        public WayPoint get(int x, int y)
        {
            return this.get(x + y * this.width);
        }

        public void set(int x, int y, WayPoint value)
        {
            this.set(x + y * this.width, value);
        }
    }

    private static class BooleanMatrix extends BooleanArray
    {
        public final int width;
        public final int height;

        BooleanMatrix(int width, int height)
        {
            super(width * height);
            setSize(width * height);

            this.width = width;
            this.height = height;
        }

        public boolean get(int x, int y)
        {
            if (x < 0 || y < 0 || x >= this.width || y >= this.height)
                return false;

            return this.get(x + y * this.width);
        }

        public void set(int x, int y, boolean value)
        {
            if (x < 0 || y < 0 || x >= this.width || y >= this.height)
                return;

            this.set(x + y * this.width, value);
        }
    }

    private class BreakEntry
    {
        private WayPoint breaker;
        private WayPoint a;
        private WayPoint b;
    }

    public void init()
    {
        dirty = new ObjectSet<>();
        random = new Random();

        final int mapWidth = map.getBlocks().getWidth(), mapHeight = map.getBlocks().getHeight();

        if (cacheGrid != null)
        {
            cacheGrid.clear();
        }

        cacheGrid = new Array<>();
        cacheGridWidth = mapWidth / CACHE_GRID_SIZE;
        cacheGridHeight = mapHeight / CACHE_GRID_SIZE;
        cacheGrid.setSize(cacheGridWidth * cacheGridHeight);
    }

    public void generate()
    {
        final int mapChunkWidth = map.getBlocks().getBlockWidth(),
                mapChunkHeight = map.getBlocks().getBlockHeight();

        for (int j = 0; j < mapChunkHeight; j++)
        {
            for (int i = 0; i < mapChunkWidth; i++)
            {
                generate(i, j);
            }
        }
    }

    public void generate(int chunkX, int chunkY)
    {
        ChunkData chunk = map.getChunk(chunkX, chunkY);

        if (chunk == null)
            return;

        int chunkBlocksX = chunkX * Constants.Core.CHUNK_SIZE,
            chunkBlocksY = chunkY * Constants.Core.CHUNK_SIZE;

        final int validBlocksWidth = Constants.Core.CHUNK_SIZE,
                validBlocksHeight = Constants.Core.CHUNK_SIZE;

        BooleanMatrix validBlocks = new BooleanMatrix(validBlocksWidth, validBlocksHeight);
        WayPointMatrix existingWayPoints = new WayPointMatrix(validBlocksWidth, validBlocksHeight);

        GameMode gameMode = BrainOutServer.Controller.getGameMode();
        boolean canShootThrough = gameMode.isGameActive();

        for (int j = 0; j < validBlocksHeight; j++)
        {
            for (int i = 0; i < validBlocksWidth; i++)
            {
                boolean valid = true;
                boolean hadFixture = false;

                // check valid spots

                for (int j_ = 0; j_ < PLAYER_H; j_++)
                {
                    for (int i_ = 0; i_ < PLAYER_W; i_++)
                    {
                        int x = i + i_, y = j + j_;

                        BlockData blockData = map.getBlock(
                            chunkBlocksX + x, chunkBlocksY + y,
                            Constants.Layers.BLOCK_LAYER_FOREGROUND);

                        if (blockData != null)
                        {
                            Block creator = blockData.getCreator();
                            if (blockData.isConcrete() &&
                                    !creator.hasComponent(IgnoreWayPointsComponent.class) &&
                                    (!canShootThrough || !creator.hasComponent(CanShootThroughComponent.class)))
                            {
                                valid = false;
                                break;
                            }
                            else if (blockData.isFixture())
                            {
                                hadFixture = true;
                            }
                        }
                    }

                    if (!valid)
                        break;
                }

                if (!valid)
                    continue;

                if (!hadFixture)
                {
                    valid = false;

                    // check the blocks under

                    for (int i_ = 0; i_ < PLAYER_W; i_++)
                    {
                        int x = i + i_, y = j - 1;

                        BlockData blockData = map.getBlock(
                            chunkBlocksX + x, chunkBlocksY + y,
                            Constants.Layers.BLOCK_LAYER_FOREGROUND);

                        if (blockData != null && blockData.isConcrete()
                                && !blockData.getCreator().hasComponent(IgnoreWayPointsComponent.class))
                        {
                            valid = true;
                            break;
                        }
                    }

                    if (!valid)
                        continue;
                }

                validBlocks.set(i, j, true);
            }
        }

        for (int j = 0; j < validBlocksHeight; j++)
        {
            for (int i = 0; i < validBlocksWidth; i++)
            {
                if (!validBlocks.get(i, j))
                    continue;

                // vertical lines

                if (!validBlocks.get(i, j - 1))
                {
                    int h = j;

                    while (h < validBlocksHeight - 1 && validBlocks.get(i, h + 1))
                    {
                        h++;
                    }

                    if (h > j)
                    {
                        for (int jj = j + 1; jj <= h - 1; jj++)
                        {
                            // clear
                            validBlocks.set(i, jj, false);
                        }

                        WayPoint a = existingWayPoints.get(i, j);
                        if (a == null)
                        {
                            a = addWayPoint(chunkX, chunkY,
                                chunkBlocksX + i + (float) PLAYER_W / 2.0f,
                                chunkBlocksY + j + (float) PLAYER_H / 2.0f);
                            existingWayPoints.set(i, j, a);
                        }

                        WayPoint b = existingWayPoints.get(i, h);
                        if (b == null)
                        {
                            b = addWayPoint(chunkX, chunkY,
                                chunkBlocksX + i + (float) PLAYER_W / 2.0f,
                                chunkBlocksY + h + (float) PLAYER_H / 2.0f);
                            existingWayPoints.set(i, h, b);
                        }

                        a.addNeighbor(b, true);
                        b.addNeighbor(a, true);
                    }
                }

            }
        }

        for (int j = 0; j < validBlocksHeight; j++)
        {
            for (int i = 0; i < validBlocksWidth; i++)
            {
                if (!validBlocks.get(i, j))
                    continue;


                // horizontal lines

                if (!validBlocks.get(i - 1, j))
                {
                    int w = i;

                    while (w < validBlocksWidth - 1 && validBlocks.get(w + 1, j))
                    {
                        w++;
                    }

                    if (w > i)
                    {
                        WayPoint a = existingWayPoints.get(i, j);
                        if (a == null)
                        {
                            a = addWayPoint(chunkX, chunkY,
                                chunkBlocksX + i + (float) PLAYER_W / 2.0f,
                                chunkBlocksY + j + (float) PLAYER_H / 2.0f);
                            existingWayPoints.set(i, j, a);
                        }

                        WayPoint b = existingWayPoints.get(w, j);
                        if (b == null)
                        {
                            b = addWayPoint(chunkX, chunkY,
                                chunkBlocksX + w + (float)PLAYER_W / 2.0f,
                                chunkBlocksY + j + (float)PLAYER_H / 2.0f);
                            existingWayPoints.set(w, j, b);
                        }

                        a.addNeighbor(b, true);
                        b.addNeighbor(a, true);
                        continue;
                    }
                }


                // diagonal lines (right-top)

                if (!validBlocks.get(i - 1, j - 1))
                {
                    int w = i;
                    int h = j;

                    while (w < validBlocksWidth - 1 && h < validBlocksHeight - 1 &&
                            validBlocks.get(w + 1, h + 1) && !validBlocks.get(w + 1, h))
                    {
                        w++; h++;
                    }

                    if (w > i)
                    {
                        WayPoint a = existingWayPoints.get(i, j);
                        if (a == null)
                        {
                            a = addWayPoint(chunkX, chunkY,
                                chunkBlocksX + i + (float) PLAYER_W / 2.0f,
                                chunkBlocksY + j + (float) PLAYER_H / 2.0f);
                            existingWayPoints.set(i, j, a);
                        }

                        WayPoint b = existingWayPoints.get(w, h);
                        if (b == null)
                        {
                            b = addWayPoint(chunkX, chunkY,
                                chunkBlocksX + w + (float)PLAYER_W / 2.0f,
                                chunkBlocksY + h + (float)PLAYER_H / 2.0f);
                            existingWayPoints.set(w, h, b);
                        }

                        a.addNeighbor(b, true);
                        b.addNeighbor(a, true);
                        continue;
                    }
                }

                // diagonal lines (left-top)

                if (!validBlocks.get(i - 1, j + 1))
                {
                    int w = i;
                    int h = j;

                    while (w < validBlocksWidth - 1 && h > 0 &&
                            validBlocks.get(w + 1, h - 1) && !validBlocks.get(w + 1, h))
                    {
                        w++; h--;
                    }

                    if (w > i)
                    {
                        WayPoint a = existingWayPoints.get(i, j);
                        if (a == null)
                        {
                            a = addWayPoint(chunkX, chunkY,
                                chunkBlocksX + i + (float) PLAYER_W / 2.0f,
                                chunkBlocksY + j + (float) PLAYER_H / 2.0f);
                            existingWayPoints.set(i, j, a);
                        }

                        WayPoint b = existingWayPoints.get(w, h);
                        if (b == null)
                        {
                            b = addWayPoint(chunkX, chunkY,
                                chunkBlocksX + w + (float)PLAYER_W / 2.0f,
                                chunkBlocksY + h + (float)PLAYER_H / 2.0f);
                            existingWayPoints.set(w, h, b);
                        }

                        a.addNeighbor(b, true);
                        b.addNeighbor(a, true);
                    }
                }
            }
        }

        Queue<WayPoint> bottoms_ = new Queue<>();
        Queue<WayPoint> lefts_ = new Queue<>();
        Queue<WayPoint> rights_ = new Queue<>();

        Queue<WayPoint> wayPointsAtChunk = getChunkWayPointsAt(chunkX, chunkY);

        if (wayPointsAtChunk == null)
            return;

        // extend vertical/horizontal lines
        for (WayPoint wayPoint : wayPointsAtChunk)
        {
            float x1 = wayPoint.getX(),
                  y1 = wayPoint.getY();

            boolean needCleanUp = false;

            for (ObjectMap.Entry<Integer, WayPoint.Neighbor> entry : wayPoint.getNeighbors())
            {
                WayPoint.Neighbor neighbor = entry.value;

                if (!neighbor.isPrimary(this))
                    continue;

                WayPoint wp = neighbor.getWayPoint(this);
                if (wp == null)
                {
                    needCleanUp = true;
                    continue;
                }

                float x2 = wp.getX(), y2 = wp.getY();

                // verticals
                if (Math.abs(x1 - x2) < 0.1f)
                {
                    WayPoint bottom = y2 > y1 ? wayPoint : wp;

                    bottoms_.addLast(bottom);
                }

                // horizontals
                if (Math.abs(y1 - y2) < 0.1f)
                {
                    if (Math.abs(x1 - x2) <= 2.0f)
                        continue;

                    WayPoint right = x1 > x2 ? wayPoint : wp;
                    WayPoint left = x2 > x1 ? wayPoint : wp;

                    lefts_.addLast(left);
                    rights_.addLast(right);
                }
            }

            if (needCleanUp)
                wayPoint.cleanUp(this);
        }

        for (WayPoint wayPoint : wayPointsAtChunk)
        {
            float x1 = wayPoint.getX(),
                  y1 = wayPoint.getY();

            boolean needCleanUp = false;

            for (ObjectMap.Entry<Integer, WayPoint.Neighbor> entry : wayPoint.getNeighbors())
            {
                WayPoint.Neighbor neighbor = entry.value;

                if (!neighbor.isPrimary(this))
                    continue;

                WayPoint wp = neighbor.getWayPoint(this);
                if (wp == null)
                {
                    needCleanUp = true;
                    continue;
                }

                if (!neighbor.isPrimary(this))
                    continue;

                float x2 = wp.getX(), y2 = wp.getY();

                // horizontals
                if (Math.abs(y1 - y2) < 0.1f)
                {
                    float xLeft = Math.min(x1, x2);
                    float xRight = Math.max(x1, x2);

                    if (xRight - xLeft <= 2.0f)
                        continue;

                    float tolerance = 6f;

                    for (WayPoint bottom : bottoms_)
                    {
                        float x = bottom.getX(), y = bottom.getY();

                        if (x < xLeft || x > xRight)
                            continue;

                        if (y <= y1 || y > y1 + tolerance)
                            continue;

                        boolean bad = false;

                        for (float j = y; j >= y1; j--)
                        {
                            BlockData blockData = map.getBlockAt(x, j, Constants.Layers.BLOCK_LAYER_FOREGROUND);
                            if (blockData != null && !blockData.getCreator().hasComponent(IgnoreWayPointsComponent.class))
                            {
                                bad = true;
                                break;
                            }
                        }

                        if (bad)
                            continue;

                        bottom.setY(y1);
                    }
                }

                // verticals
                if (Math.abs(x1 - x2) < 0.1f)
                {
                    float xBottom = Math.min(y1, y2);
                    float xTop = Math.max(y1, y2);

                    float tolerance = 2f;

                    for (WayPoint left : lefts_)
                    {
                        float x = left.getX(), y = left.getY();

                        if (y < xBottom || y > xTop)
                            continue;

                        if (x <= x1 || x > x1 + tolerance)
                            continue;

                        left.setX(x1);
                    }

                    for (WayPoint right : rights_)
                    {
                        float x = right.getX(), y = right.getY();

                        if (y < xBottom || y > xTop)
                            continue;

                        if (x < x1 - tolerance || x >= x1)
                            continue;

                        right.setX(x1);
                    }
                }
            }

            if (needCleanUp)
                wayPoint.cleanUp(this);
        }

        Queue<BreakEntry> cutEdges = new Queue<>();

        // cut apparent edges
        for (WayPoint wayPoint : wayPointsAtChunk)
        {
            float x1 = wayPoint.getX(), y1 = wayPoint.getY();

            boolean needCleanUp = false;

            for (ObjectMap.Entry<Integer, WayPoint.Neighbor> entry : wayPoint.getNeighbors())
            {
                WayPoint.Neighbor neighbor = entry.value;

                if (!neighbor.isPrimary(this))
                    continue;

                WayPoint wp = neighbor.getWayPoint(this);
                if (wp == null)
                {
                    needCleanUp = true;
                    continue;
                }

                if (!neighbor.isPrimary(this))
                    continue;

                float x2 = wp.getX(), y2 = wp.getY();

                float dst = Vector2.dst(x1, y1, x2, y2);

                for (WayPoint point : new Queue.QueueIterator<>(wayPointsAtChunk))
                {
                    if (wayPoint == point)
                        continue;

                    if (wp == point)
                        continue;

                    float x = point.getX(), y = point.getY();

                    float tolerance = 0.5f;

                    if (Vector2.dst(x, y, x1, y1) + Vector2.dst(x, y, x2, y2) > dst + tolerance * 2)
                        continue;

                    if (DistanceUtils.PointToLineDistance(x, y, x1, y1, x2, y2) > tolerance)
                        continue;

                    // point breaks wayPoint and neighbor.getWayPoint()
                    boolean exists = false;

                    // check existing entry
                    for (BreakEntry cutEdge : cutEdges)
                    {
                        if (cutEdge.breaker == point && (
                            (cutEdge.a == wayPoint && cutEdge.b == wp) ||
                            (cutEdge.b == wayPoint && cutEdge.a == wp)
                        )) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists)
                    {
                        BreakEntry newEntry = new BreakEntry();
                        newEntry.breaker = point;
                        newEntry.a = wayPoint;
                        newEntry.b = wp;

                        cutEdges.addLast(newEntry);
                    }
                }
            }

            if (needCleanUp)
                wayPoint.cleanUp(this);
        }

        for (BreakEntry cutEdge : cutEdges)
        {
            cutEdge.a.removeNeighbor(cutEdge.b);
            cutEdge.b.removeNeighbor(cutEdge.a);

            cutEdge.a.addNeighbor(cutEdge.breaker, true);
            cutEdge.b.addNeighbor(cutEdge.breaker, true);

            cutEdge.breaker.addNeighbor(cutEdge.a, true);
            cutEdge.breaker.addNeighbor(cutEdge.b, true);
        }

        generateCache(chunkX, chunkY);

        Queue<Pair<WayPoint, WayPoint>> futureEdges = new Queue<>();

        for (WayPoint wayPoint : wayPointsAtChunk)
        {
            queryWayPointsAt(wayPoint.getX(), wayPoint.getY(), 14.0f, (w, dst) ->
            {
                if (w == wayPoint || (w.getX() == wayPoint.getX() && w.getY() == wayPoint.getY()))
                    return;

                if (Math.abs(w.getX() - wayPoint.getX()) > 8)
                    return;

                boolean oneWay = false;
                boolean wayW = false;

                // too high
                if (dst > 6 && Math.abs(w.getY() - wayPoint.getY()) > 5)
                {
                    if (w.getY() > wayPoint.getY())
                    {
                        oneWay = true;
                        wayW = true;
                    }
                    else
                    {
                        oneWay = true;
                        wayW = false;
                    }
                }

                for (ObjectMap.Entry<Integer, WayPoint.Neighbor> entry :
                        new ObjectMap.Entries<>(wayPoint.getNeighbors()))
                {
                    if (entry.key == w.getId())
                        return;
                }

                for (ObjectMap.Entry<Integer, WayPoint.Neighbor> entry :
                        new ObjectMap.Entries<>(w.getNeighbors()))
                {
                    if (entry.key == wayPoint.getId())
                        return;
                }

                if (map.rayCast(w.getX(), w.getY(), wayPoint.getX(), wayPoint.getY()))
                    return;

                if (map.trace(w.getX(), w.getY(), wayPoint.getX(), wayPoint.getY(),
                    Constants.Layers.BLOCK_LAYER_FOREGROUND, null, null))
                    return;

                float distance = Vector2.dst(w.getX(), w.getY(), wayPoint.getX(), wayPoint.getY());

                if (distance > 4)
                {
                    float blockW = 0.5f, blockH = 0.5f;

                    if (map.rayCast(w.getX() + blockW, w.getY() + blockH,
                            wayPoint.getX() + blockW, wayPoint.getY() + blockH))
                        return;

                    if (map.rayCast(w.getX() - blockW, w.getY() + blockH,
                            wayPoint.getX() - blockW, wayPoint.getY() + blockH))
                        return;

                    // we we're going only one way (jumping), no need to check for extra corners
                    if (!oneWay)
                    {
                        if (map.rayCast(w.getX() + blockW, w.getY() - blockH,
                                wayPoint.getX() + blockW, wayPoint.getY() - blockH))
                            return;

                        if (map.rayCast(w.getX() - blockW, w.getY() - blockH,
                                wayPoint.getX() - blockW, wayPoint.getY() - blockH))
                            return;
                    }
                }

                if (oneWay)
                {
                    w.addNeighbor(wayPoint, wayW);
                    wayPoint.addNeighbor(w, !wayW);
                }
                else
                {
                    w.addNeighbor(wayPoint, true);
                    wayPoint.addNeighbor(w, true);
                }

                futureEdges.addLast(new Pair<>(w, wayPoint));
            });
        }

        for (Pair<WayPoint, WayPoint> pair : futureEdges)
        {
            addCacheEdge(pair.first, pair.second);
        }

        // update points of interest

        Queue<WayPoint> q = getChunkWayPointsAt(chunkX, chunkY);
        if (q != null)
        {
            for (WayPoint wayPoint : q)
            {
                ActiveData poi = map.getClosestActiveForTag(8.0f, wayPoint.getX(), wayPoint.getY(),
                    ActiveData.class, Constants.ActiveTags.POINT_OF_INTEREST, activeData -> true);

                wayPoint.setPointOfInterest(poi != null ? poi.getId() : -1);
            }
        }
    }


    private void addCacheEdge(WayPoint a, WayPoint b)
    {
        WayPointCacheEdge newEdge = new WayPointCacheEdge();
        newEdge.a = a;
        newEdge.b = b;

        SupercoverUtils.SupercoverLine(
            a.getX(), a.getY(),
            b.getX(), b.getY(),
            (float)CACHE_GRID_SIZE,  (float)CACHE_GRID_SIZE,
            (x, y) ->
        {
            Queue<WayPointCacheEdge> q = getCacheAt(x * (float)CACHE_GRID_SIZE, y * (float)CACHE_GRID_SIZE);

            if (q == null)
                return;

            q.addLast(newEdge);
        });
    }

    private void generateCache(int chunkX, int chunkY)
    {
        Queue<WayPoint> chunkWayPoints = getChunkWayPointsAt(chunkX, chunkY);

        if (chunkWayPoints == null)
            return;

        for (WayPoint wayPoint : chunkWayPoints)
        {
            boolean needsCleanUp = false;

            for (ObjectMap.Entry<Integer, WayPoint.Neighbor> entry : wayPoint.getNeighbors())
            {
                WayPoint wp = entry.value.getWayPoint(this);

                if (wp == null)
                {
                    needsCleanUp = true;
                    continue;
                }

                addCacheEdge(wayPoint, wp);
            }

            if (needsCleanUp)
                wayPoint.cleanUp(this);
        }
    }

    public interface PathSearchResult
    {
        void found(Queue<Vector2> path, String dimension,
               ObjectSet<BlockCoordinates> blocksInWay, ActiveData portalOfInterest);
        void notFound();
    }

    private static class WayPointDistanceEntry implements Pool.Poolable
    {
        float distance = Float.POSITIVE_INFINITY;
        Queue<WayPoint> shortestPath = new Queue<>();

        @Override
        public void reset()
        {
            distance = Float.POSITIVE_INFINITY;
            shortestPath.clear();
        }
    }

    private final static Pool<WayPointDistanceEntry> distanceEntryPool = new Pool<WayPointDistanceEntry>(64)
    {
        @Override
        protected WayPointDistanceEntry newObject()
        {
            return new WayPointDistanceEntry();
        }
    };

    private class WayPointDistanceMap extends ObjectMap<WayPoint, WayPointDistanceEntry>
    {
        float getDistance(WayPoint wayPoint)
        {
            WayPointDistanceEntry entry = get(wayPoint, null);

            if (entry == null)
                return Float.POSITIVE_INFINITY;

            return entry.distance;
        }

        Queue<WayPoint> getShortestPath(WayPoint wayPoint)
        {
            WayPointDistanceEntry entry = get(wayPoint, null);

            if (entry == null)
            {
                return null;
            }

            return entry.shortestPath;
        }

        Queue<WayPoint> acquireShortestPath(WayPoint wayPoint)
        {
            WayPointDistanceEntry entry = get(wayPoint, null);

            if (entry == null)
            {
                synchronized (distanceEntryPool)
                {
                    entry = distanceEntryPool.obtain();
                }
                put(wayPoint, entry);
            }

            return entry.shortestPath;
        }

        void setDistance(WayPoint wayPoint, float distance)
        {
            WayPointDistanceEntry entry = get(wayPoint, null);

            if (entry == null)
            {
                synchronized (distanceEntryPool)
                {
                    entry = distanceEntryPool.obtain();
                }
                put(wayPoint, entry);
            }

            entry.distance = distance;
        }

        public void free()
        {
            synchronized (distanceEntryPool)
            {
                for (WayPointDistanceEntry value : values())
                {
                    distanceEntryPool.free(value);
                }
            }

            clear();
        }
    }

    private static WayPoint GetLowestDistanceNode(WayPointDistanceMap distanceMap,
                                                  ObjectSet<WayPoint> unsettledNodes)
    {
        WayPoint lowestDistanceNode = null;
        float lowestDistance = Float.POSITIVE_INFINITY;

        for (WayPoint node: unsettledNodes)
        {
            float nodeDistance = distanceMap.getDistance(node);
            if (nodeDistance < lowestDistance)
            {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }

        return lowestDistanceNode;
    }

    private static void CalculateMinimumDistance(
        WayPointDistanceMap distanceMap,
        WayPoint evaluationNode,
        float edgeWeigh, WayPoint sourceNode)
    {
        float sourceDistance = distanceMap.getDistance(sourceNode);
        if (sourceDistance + edgeWeigh < distanceMap.getDistance(evaluationNode))
        {
            distanceMap.setDistance(evaluationNode, sourceDistance + edgeWeigh);
            Queue<WayPoint> evShortestPath = distanceMap.acquireShortestPath(evaluationNode);
            Queue<WayPoint> srcShortestPath = distanceMap.acquireShortestPath(sourceNode);

            evShortestPath.clear();
            for (WayPoint wayPoint : srcShortestPath)
            {
                evShortestPath.addLast(wayPoint);
            }
            evShortestPath.addLast(sourceNode);
        }
    }

    public interface PointOfInterestQuery
    {
        float interest(int pointOfInterest);
    }

    public static boolean FindDoors(float fromX, float fromY, Map from, Map to, Vector2 in, Vector2 out)
    {
        Array<ActiveData> actives = from.getActivesForTag(Constants.ActiveTags.PORTAL, activeData ->
        {
            ServerPortalComponentData sp = activeData.getComponent(ServerPortalComponentData.class);
            if (sp == null)
                return false;

            PortalData otherPortal = sp.getOtherPortal();
            if (otherPortal == null)
                return false;

            return otherPortal.getMap() == to;
        });

        if (actives.size <= 0)
        {
            return false;
        }

        actives.sort((o1, o2) ->
        {
            float d1 = Vector2.dst2(fromX, fromY, o1.getX(), o1.getY());
            float d2 = Vector2.dst2(fromX, fromY, o2.getX(), o2.getY());
            return (int)(d1 - d2);
        });

        ActiveData door = actives.first();

        ServerPortalComponentData sp = door.getComponent(ServerPortalComponentData.class);
        if (sp == null)
            return false;

        PortalData otherPortal = sp.getOtherPortal();
        if (otherPortal == null)
            return false;

        in.set(door.getX(), door.getY());
        out.set(otherPortal.getX(), otherPortal.getY());

        return true;
    }

    public void findPath(float fromX, float fromY, Map from,
         float toX, float toY, Map to,
         float closestWayPointDistance,
         PointOfInterestQuery pointOfInterestQuery, PathSearchResult result)
    {
        if (from == to)
        {
            findPath(fromX, fromY, toX, toY, closestWayPointDistance, pointOfInterestQuery, result);
            return;
        }

        Queue<Map> path = MapDimensionsGraph.FindPath(from, to);
        if (path == null || path.size < 2)
        {
            result.notFound();
            return;
        }

        Map nextMap = path.get(1);

        Array<ActiveData> actives = from.getActivesForTag(Constants.ActiveTags.PORTAL, activeData ->
        {
            ServerPortalComponentData sp = activeData.getComponent(ServerPortalComponentData.class);
            if (sp == null)
                return false;

            PortalData otherPortal = sp.getOtherPortal();
            if (otherPortal == null)
                return false;

            return otherPortal.getMap() == nextMap;
        });

        if (actives.size <= 0)
        {
            result.notFound();
            return;
        }

        actives.sort((o1, o2) ->
        {
            float d1 = Vector2.dst2(fromX, fromY, o1.getX(), o1.getY());
            float d2 = Vector2.dst2(fromX, fromY, o2.getX(), o2.getY());
            return (int)(d1 - d2);
        });

        ActiveData door = actives.first();

        findPath(fromX, fromY, door.getX(), door.getY(), closestWayPointDistance, pointOfInterestQuery,
            new PathSearchResult()
        {
            @Override
            public void found(Queue<Vector2> path, String dimension,
                ObjectSet<BlockCoordinates> blocksInWay, ActiveData portalOfInterest)
            {
                result.found(path, dimension, blocksInWay, door);
            }

            @Override
            public void notFound()
            {
                result.notFound();
            }
        });
    }

    public void findPath(float fromX, float fromY, float toX, float toY, float closestWayPointDistance,
                         PointOfInterestQuery pointOfInterestQuery, PathSearchResult result)
    {
        if (ThreadPoolExecutor.getQueue().size() > 16)
        {
            // the queue is too big at the moment
            result.notFound();
            return;
        }

        if (locked != null)
        {
            locked.addLast(() ->
                findPath(fromX, fromY, toX, toY, closestWayPointDistance, pointOfInterestQuery, result));
            return;
        }

        Vector2 sourceOut = new Vector2();
        Vector2 targetOut = new Vector2();

        Vector2 tmp1 = new Vector2();

        WayPoint source = getClosestWayPoint(fromX, fromY, closestWayPointDistance, sourceOut);
        WayPoint target = getClosestWayPoint(toX, toY, closestWayPointDistance, targetOut);

        final ServerMap map = this.map;

        if (source == null || target == null || map == null)
        {
            result.notFound();
            return;
        }

        final String dimension = this.map.getDimension();

        ThreadPoolExecutor.execute(() ->
        {
            findPathLock.incrementAndGet();

            try
            {
                WayPointDistanceMap distanceMap = new WayPointDistanceMap();
                distanceMap.setDistance(source, 0);

                ObjectSet<WayPoint> settledNodes = new ObjectSet<>();
                ObjectSet<WayPoint> unsettledNodes = new ObjectSet<>();

                unsettledNodes.add(source);

                while (unsettledNodes.size != 0) {
                    WayPoint currentNode = GetLowestDistanceNode(distanceMap, unsettledNodes);
                    unsettledNodes.remove(currentNode);

                    for (ObjectMap.Entry<Integer, WayPoint.Neighbor> entry :
                        new ObjectMap.Entries<>(currentNode.getNeighbors()))
                    {
                        WayPoint.Neighbor neighbor = entry.value;
                        if (neighbor == null)
                            continue;
                        if (!neighbor.isActive())
                            continue;
                        WayPoint adjacentNode = neighbor.getWayPoint(this);
                        if (adjacentNode == null)
                            continue;

                        int poi = adjacentNode.getPointOfInterest();
                        float edgeWeight = neighbor.getWeight();
                        if (poi > 0 && pointOfInterestQuery != null)
                        {
                            edgeWeight *= pointOfInterestQuery.interest(poi);
                        }
                        if (!settledNodes.contains(adjacentNode))
                        {
                            CalculateMinimumDistance(distanceMap, adjacentNode, edgeWeight, currentNode);
                            unsettledNodes.add(adjacentNode);
                        }
                    }

                    settledNodes.add(currentNode);
                }

                BrainOutServer.PostRunnable(() ->
                {
                    Queue<WayPoint> shortestPath = distanceMap.getShortestPath(target);

                    if (shortestPath == null)
                    {
                        result.notFound();
                    }
                    else
                    {
                        Queue<Vector2> vectors = new Queue<>();

                        Vector2 sourceV = new Vector2(source.getX(), source.getY());

                        for (WayPoint wayPoint : shortestPath)
                        {
                            vectors.addLast(new Vector2(wayPoint.getX(), wayPoint.getY()));
                        }

                        distanceMap.free();

                        Vector2 targetV = new Vector2(target.getX(), target.getY());

                        Vector2 prev = vectors.size > 0 ? vectors.last() : null;

                        if (vectors.size > 1)
                        {
                            float a1 = tmp1.set(sourceV).sub(sourceOut).nor().angleDeg();
                            float a2 = tmp1.set(sourceV).sub(vectors.get(1)).nor().angleDeg();

                            if (Math.abs(a1 - a2) < 0.1f)
                            {
                                vectors.removeIndex(0);
                            }
                        } else if (vectors.size == 1)
                        {
                            float a1 = tmp1.set(sourceV).sub(sourceOut).nor().angleDeg();
                            float a2 = tmp1.set(sourceV).sub(targetV).nor().angleDeg();

                            if (Math.abs(a1 - a2) < 0.1f)
                            {
                                vectors.removeIndex(0);
                            }
                        }

                        if (prev != null)
                        {
                            float a1 = tmp1.set(targetV).sub(prev).nor().angleDeg();
                            float a2 = tmp1.set(targetV).sub(targetOut).nor().angleDeg();

                            if (Math.abs(a1 - a2) > 0.1f)
                            {
                                vectors.addLast(targetV);
                            }
                        }
                        else
                        {
                            if (vectors.size == 0)
                            {
                                float a1 = tmp1.set(targetV).sub(sourceOut).nor().angleDeg();
                                float a2 = tmp1.set(targetV).sub(targetOut).nor().angleDeg();

                                if (Math.abs(a1 - a2) > 0.1f)
                                {
                                    vectors.addLast(targetV);
                                }
                            }
                            else
                            {
                                vectors.addLast(targetV);
                            }
                        }

                        vectors.addFirst(sourceOut);

                        vectors.addLast(targetOut);
                        vectors.addLast(new Vector2(toX, toY));

                        ObjectSet<BlockCoordinates> blocksInWaySet = new ObjectSet<>();

                        for (float offsetX = -0.5f; offsetX < 1f; offsetX += 1.0f)
                        {
                            for (int offsetY = -1; offsetY <= 1; offsetY ++)
                            {
                                Vector2 last = null;
                                for (Vector2 entry : vectors)
                                {
                                    if (last != null)
                                    {
                                        SupercoverUtils.SupercoverLine(last.x + offsetX, last.y + offsetY,
                                            entry.x + offsetX, entry.y + offsetY,
                                            1.0f, 1.0f, (x, y) ->
                                        {
                                            int blockX = (int)(x + 0.5f), blockY = (int)(y + 0.5f);
                                            BlockData blockData = map.getBlock(
                                                blockX, blockY,
                                                Constants.Layers.BLOCK_LAYER_FOREGROUND);

                                            if (blockData == null)
                                                return;

                                            if (!blockData.getCreator().hasComponent(CanShootThroughComponent.class))
                                                return;

                                            blocksInWaySet.add(new BlockCoordinates(blockX, blockY));
                                        });
                                    }

                                    last = entry;
                                }
                            }
                        }

                        result.found(vectors, dimension, blocksInWaySet, null);
                    }
                });
            }
            finally
            {
                findPathLock.decrementAndGet();
            }
        });
    }

    public ObjectMap<Integer, WayPoint> getWayPoints()
    {
        return wayPoints;
    }

    WayPoint getWayPointById(int id)
    {
        return wayPoints.get(id);
    }

    private WayPoint addWayPoint(int chunkX, int chunkY, float x, float y)
    {
        WayPoint wayPoint = new WayPoint(map, x, y, nextWayPointId++);
        this.wayPoints.put(wayPoint.getId(), wayPoint);

        int chunkIndex = chunkX + chunkY * map.getBlocks().getBlockWidth();

        Queue<WayPoint> wp = this.chunkWayPoints.get(chunkIndex);
        if (wp == null)
        {
            wp = new Queue<>();
            this.chunkWayPoints.put(chunkIndex, wp);
        }
        wp.addLast(wayPoint);

        return wayPoint;
    }

    public void release()
    {
        clear();

        this.map = null;
        this.wayPoints = null;
        this.cacheGrid = null;
    }

    private Queue<WayPoint> getChunkWayPointsAt(int chunkX, int chunkY)
    {
        int chunkIndex = chunkX + chunkY * map.getBlocks().getBlockWidth();
        return this.chunkWayPoints.get(chunkIndex);
    }

    private void clear(int chunkX, int chunkY)
    {
        Queue<WayPoint> chunkWayPoints = getChunkWayPointsAt(chunkX, chunkY);

        if (chunkWayPoints == null)
            return;

        for (WayPoint wayPoint : chunkWayPoints)
        {
            this.wayPoints.remove(wayPoint.getId());
            wayPoint.disconnectExternal(this);
            wayPoint.release();
        }

        chunkWayPoints.clear();

        if (cacheGrid != null)
        {
            int cacheGridsPerChunk = Constants.Core.CHUNK_SIZE / CACHE_GRID_SIZE;

            // clear cache grid for that chunk
            for (int j = 0; j < cacheGridsPerChunk; j++)
            {
                for (int i = 0; i < cacheGridsPerChunk; i++)
                {
                    int _x = chunkX * cacheGridsPerChunk + i,
                        _y = chunkY * cacheGridsPerChunk + j;

                    int cacheIndex = _x + _y * cacheGridWidth;

                    Queue<WayPointCacheEdge> queue = cacheGrid.get(cacheIndex);

                    if (queue != null)
                        queue.clear();
                }
            }
        }
    }

    private void clear()
    {
        for (WayPoint wayPoint : wayPoints.values())
        {
            wayPoint.release();
        }

        chunkWayPoints.clear();
        wayPoints.clear();

        if (cacheGrid != null)
        {
            for (Queue<WayPointCacheEdge> queue : cacheGrid)
            {
                if (queue != null)
                    queue.clear();
            }

            cacheGrid.clear();
        }
    }

    public void render(ShapeRenderer shapeRenderer)
    {
        for (ObjectMap.Entry<Integer, Queue<WayPoint>> entry : new ObjectMap.Entries<>(chunkWayPoints))
        {
            // something is very wrong
            if (entry == null)
                return;

            int chunkIndex = entry.key;
            random.setSeed(chunkIndex);

            shapeRenderer.setColor(
                    random.nextFloat() * 0.4f,
                    random.nextFloat() * 0.4f,
                    random.nextFloat() * 0.4f, 1.0f);

            for (WayPoint wayPoint : new Queue.QueueIterator<>(entry.value))
            {
                if (wayPoint == null)
                    return;

                shapeRenderer.circle(wayPoint.getX(), wayPoint.getY(), 0.5f, 16);

                for (WayPoint.Neighbor neighbor : new ObjectMap.Values<>(wayPoint.getNeighbors()))
                {
                    if (neighbor == null)
                        continue;

                    WayPoint wp = neighbor.getWayPoint(this);

                    if (wp == null)
                        continue;

                    tmp.set(wayPoint.getX(), wayPoint.getY()).sub(wp.getX(), wp.getY()).nor();
                    float angle = tmp.angleDeg();

                    shapeRenderer.line(wayPoint.getX(), wayPoint.getY(),
                        (wayPoint.getX() + wp.getX()) / 2.0f,
                        (wayPoint.getY() + wp.getY()) / 2.0f);

                    if (!neighbor.isActive())
                        continue;

                    if (Gdx.input.isKeyPressed(Input.Keys.V))
                    {
                        // arrows

                        shapeRenderer.line(
                            wp.getX(), wp.getY(),
                            wp.getX() + MathUtils.cosDeg(angle + 20) * 0.75f,
                            wp.getY() + MathUtils.sinDeg(angle + 20) * 0.75f);

                        shapeRenderer.line(
                            wp.getX(), wp.getY(),
                            wp.getX() + MathUtils.cosDeg(angle - 20) * 0.75f,
                            wp.getY() + MathUtils.sinDeg(angle - 20) * 0.75f);
                    }
                }
            }
        }
    }

    private void setChunkDirtyInternal(int chunkX, int chunkY)
    {
        if (chunkX < 0 || chunkY < 0 ||
            chunkX >= map.getBlocks().getBlockWidth() || chunkY >= map.getBlocks().getBlockHeight())
            return;

        int chunkW = map.getBlocks().getBlockWidth();
        int index = chunkX + chunkY * chunkW;
        dirty.add(index);
    }

    public void setChunkDirty(int chunkX, int chunkY, int x, int y)
    {
        if (dirty == null)
            return;

        setChunkDirtyInternal(chunkX, chunkY);
    }

    public void update(float dt)
    {
        if (dirty == null || cacheGrid == null || cacheGrid.size == 0)
            return;

        dirtyTimer -= dt;
        if (dirtyTimer < 0)
        {
            dirtyTimer = 5.0f;
            updateDirty();
        }
    }

    public void regenerate()
    {
        if (!lockPathFinding())
            return;

        try
        {
            final int mapChunkWidth = map.getBlocks().getBlockWidth(),
                mapChunkHeight = map.getBlocks().getBlockHeight();

            for (int j = 0; j < mapChunkHeight; j++)
            {
                for (int i = 0; i < mapChunkWidth; i++)
                {
                    clear(i, j);
                }
            }

            for (int j = 0; j < mapChunkHeight; j++)
            {
                for (int i = 0; i < mapChunkWidth; i++)
                {
                    generate(i, j);
                }
            }

            dirty.clear();
        }
        finally
        {
            unlockPathFinding();
        }
    }

    private void updateDirty()
    {
        int chunkW = map.getBlocks().getBlockWidth();

        if (dirty.size == 0)
            return;

        if (!lockPathFinding())
            return;

        try
        {
            for (Integer index : dirty)
            {
                int chunkX = index % chunkW,
                        chunkY = index / chunkW;

                clear(chunkX, chunkY);
            }

            for (Integer index : dirty)
            {
                int chunkX = index % chunkW,
                        chunkY = index / chunkW;

                generate(chunkX, chunkY);
            }

            dirty.clear();
        }
        finally
        {
            unlockPathFinding();
        }
    }

    private void unlockPathFinding()
    {
        // enable the findPath back
        ThreadPoolExecutor.resume();

        for (Runnable runnable : locked)
        {
            runnable.run();
        }

        locked.clear();
        locked = null;
    }

    private boolean lockPathFinding()
    {
        // lock the findPath
        locked = new Queue<>();

        ThreadPoolExecutor.pause();

        int lockCounter = 0;

        // wait for currently working threads to complete
        while (findPathLock.get() > 0)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                unlockPathFinding();
                return false;
            }

            if (lockCounter++ > 100)
            {
                // time out
                unlockPathFinding();
                return false;
            }
        }

        return true;
    }
}
