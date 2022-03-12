package com.desertkun.brainout.data;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.active.ActiveData;

import java.util.Iterator;

public class WayPoint
{
    public class Neighbor
    {
        private int wayPointId;
        private float distance;
        private boolean active;

        Neighbor(int wayPointId, float distance, boolean active)
        {
            this.wayPointId = wayPointId;
            this.distance = distance;
            this.active = active;
        }

        boolean isActive()
        {
            return active;
        }

        float getWeight()
        {
            return distance;
        }

        public int getWayPointId()
        {
            return wayPointId;
        }

        WayPoint getWayPoint(WayPointMap map)
        {
            return map.getWayPointById(wayPointId);
        }

        boolean isValid(WayPointMap map)
        {
            return getWayPoint(map) != null;
        }

        // used to avoid double code execution, only one of the two neighbors will return true
        boolean isPrimary(WayPointMap map)
        {
            WayPoint wayPoint = map.getWayPointById(wayPointId);

            if (wayPoint == null)
                return false;

            if (wayPoint.y > y)
                return false;
            else if (wayPoint.y < y)
                return true;
            else if (wayPoint.x > x)
                return false;
            else if (wayPoint.x < x)
                return true;
            else
                return wayPoint.hashCode() < WayPoint.this.hashCode();
        }

        boolean isExternal(WayPointMap map)
        {
            WayPoint wayPoint = map.getWayPointById(wayPointId);

            if (wayPoint == null)
                return true;

            int myChunkX = (int)x / Constants.Core.CHUNK_SIZE,
                myChunkY = (int)y / Constants.Core.CHUNK_SIZE;

            int neighborChunkX = (int)wayPoint.x / Constants.Core.CHUNK_SIZE,
                neighborChunkY = (int)wayPoint.y / Constants.Core.CHUNK_SIZE;

            return myChunkX != neighborChunkX || myChunkY != neighborChunkY;
        }
    }

    private int pointOfInterest;
    private ServerMap map;
    private float x;
    private final int id;
    private float y;
    private ObjectMap<Integer, Neighbor> neighbors;
    private float distance;

    WayPoint(ServerMap map, float x, float y, int id)
    {
        this.map = map;
        this.id = id;
        this.x = x;
        this.y = y;
        this.neighbors = new ObjectMap<>();
    }

    public int getId()
    {
        return id;
    }

    public boolean isNeighbor(WayPoint wayPoint)
    {
        return neighbors.containsKey(wayPoint.getId());
    }

    ObjectMap<Integer, Neighbor> getNeighbors()
    {
        return neighbors;
    }

    public ServerMap getMap()
    {
        return map;
    }

    int getPointOfInterest()
    {
        return pointOfInterest;
    }

    void setPointOfInterest(int pointOfInterest)
    {
        this.pointOfInterest = pointOfInterest;
    }

    void removeNeighbor(WayPoint wayPoint)
    {
        neighbors.remove(wayPoint.getId());
    }

    private static final Queue<Integer> toRemove = new Queue<>();

    public void cleanUp(WayPointMap map)
    {
        toRemove.clear();

        for (ObjectMap.Entry<Integer, Neighbor> entry : neighbors)
        {
            if (!entry.value.isValid(map))
            {
                toRemove.addLast(entry.key);
            }
        }

        for (Integer key : toRemove)
        {
            neighbors.remove(key);
        }

        toRemove.clear();
    }

    void addNeighbor(WayPoint wayPoint, boolean active)
    {
        Neighbor n = new Neighbor(wayPoint.getId(), Vector2.dst(x, y, wayPoint.x, wayPoint.y), active);
        this.neighbors.put(wayPoint.getId(), n);
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    public void disconnectExternal(WayPointMap map)
    {
        toRemove.clear();

        for (ObjectMap.Entry<Integer, Neighbor> entry : neighbors)
        {
            Neighbor neighbor = entry.value;
            if (!neighbor.isExternal(map))
                continue;
            WayPoint wp = neighbor.getWayPoint(map);
            if (wp == null)
                continue;
            wp.removeNeighbor(this);
            toRemove.addLast(entry.key);
        }

        for (Integer key : toRemove)
        {
            neighbors.remove(key);
        }

        toRemove.clear();
    }

    public void disconnect(WayPointMap map)
    {
        for (ObjectMap.Entry<Integer, Neighbor> entry : neighbors)
        {
            Neighbor neighbor = entry.value;
            WayPoint wp = neighbor.getWayPoint(map);
            if (wp == null)
                continue;
            wp.removeNeighbor(this);
        }

        neighbors.clear();
    }

    public void release()
    {
        this.neighbors.clear();
    }
}
