package com.desertkun.brainout.data;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.ServerPortalComponentData;

public class MapDimensionsGraph
{
    private static ObjectMap<String, MapDimensionGraphPoint> WayPoints;

    static
    {
        WayPoints = new ObjectMap<>();
    }

    private static class MapDimensionGraphPoint
    {
        private Map map;
        private ObjectMap<MapDimensionGraphPoint, Float> neighbors;

        public MapDimensionGraphPoint(Map map)
        {
            this.map = map;
            this.neighbors = new ObjectMap<>();
        }

        public Map getMap()
        {
            return map;
        }

        public ObjectMap<MapDimensionGraphPoint, Float> getNeighbors()
        {
            return neighbors;
        }

        public void addNeighbor(MapDimensionGraphPoint neighbor, float value)
        {
            if (neighbors.containsKey(neighbor))
                return;

            neighbors.put(neighbor, value);
        }
    }

    private static class MapDistanceEntry
    {
        float distance = Float.POSITIVE_INFINITY;
        Queue<MapDimensionGraphPoint> shortestPath = new Queue<>();
    }

    private static class MapDistanceMap extends ObjectMap<MapDimensionGraphPoint, MapDistanceEntry>
    {
        float getDistance(MapDimensionGraphPoint map)
        {
            MapDistanceEntry entry = get(map, null);

            if (entry == null)
                return Float.POSITIVE_INFINITY;

            return entry.distance;
        }

        Queue<MapDimensionGraphPoint> getShortestPath(MapDimensionGraphPoint map)
        {
            MapDistanceEntry entry = get(map, null);

            if (entry == null)
            {
                return null;
            }

            return entry.shortestPath;
        }

        Queue<MapDimensionGraphPoint> acquireShortestPath(MapDimensionGraphPoint map)
        {
            MapDistanceEntry entry = get(map, null);

            if (entry == null)
            {
                entry = new MapDistanceEntry();
                put(map, entry);
            }

            return entry.shortestPath;
        }

        void setDistance(MapDimensionGraphPoint map, float distance)
        {
            MapDistanceEntry entry = get(map, null);

            if (entry == null)
            {
                entry = new MapDistanceEntry();
                put(map, entry);
            }

            entry.distance = distance;
        }
    }

    private static MapDimensionGraphPoint GetLowestDistanceNode(
        MapDistanceMap distanceMap, 
        ObjectSet<MapDimensionGraphPoint> unsettledNodes)
    {
        MapDimensionGraphPoint lowestDistanceNode = null;
        float lowestDistance = Float.POSITIVE_INFINITY;

        for (MapDimensionGraphPoint node: unsettledNodes)
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
        MapDistanceMap distanceMap,
        MapDimensionGraphPoint evaluationNode,
        float edgeWeigh, MapDimensionGraphPoint sourceNode)
    {
        float sourceDistance = distanceMap.getDistance(sourceNode);
        if (sourceDistance + edgeWeigh < distanceMap.getDistance(evaluationNode))
        {
            distanceMap.setDistance(evaluationNode, sourceDistance + edgeWeigh);
            Queue<MapDimensionGraphPoint> evShortestPath = distanceMap.acquireShortestPath(evaluationNode);
            Queue<MapDimensionGraphPoint> srcShortestPath = distanceMap.acquireShortestPath(sourceNode);

            evShortestPath.clear();
            for (MapDimensionGraphPoint MapDimensionGraphPoint : srcShortestPath)
            {
                evShortestPath.addLast(MapDimensionGraphPoint);
            }
            evShortestPath.addLast(sourceNode);
        }
    }

    public static boolean IsNeighbor(Map map, Map to)
    {
        MapDimensionGraphPoint point = WayPoints.get(map.getDimension());
        MapDimensionGraphPoint pointTo = WayPoints.get(to.getDimension());

        if (point == null || pointTo == null)
            return false;

        return point.getNeighbors().containsKey(pointTo);
    }

    private static MapDimensionGraphPoint EnsureMap(Map map)
    {
        MapDimensionGraphPoint existing = WayPoints.get(map.getDimension());

        if (existing != null)
            return existing;

        MapDimensionGraphPoint newMapMap = new MapDimensionGraphPoint(map);
        WayPoints.put(map.getDimension(), newMapMap);

        return newMapMap;
    }

    public static Queue<Map> FindPath(Map from, Map to)
    {
        MapDimensionGraphPoint source = WayPoints.get(from.getDimension());
        MapDimensionGraphPoint target = WayPoints.get(to.getDimension());
        
        if (source == null || target == null)
            return null;
        
        MapDistanceMap distanceMap = new MapDistanceMap();
        distanceMap.setDistance(source, 0);

        ObjectSet<MapDimensionGraphPoint> settledNodes = new ObjectSet<>();
        ObjectSet<MapDimensionGraphPoint> unsettledNodes = new ObjectSet<>();

        unsettledNodes.add(source);

        while (unsettledNodes.size != 0)
        {
            MapDimensionGraphPoint currentNode = GetLowestDistanceNode(distanceMap, unsettledNodes);
            unsettledNodes.remove(currentNode);

            for (ObjectMap.Entry<MapDimensionGraphPoint, Float> entry : currentNode.getNeighbors())
            {
                MapDimensionGraphPoint adjacentNode = entry.key;
                if (adjacentNode == null)
                    continue;
                float edgeWeight = entry.value;

                if (!settledNodes.contains(adjacentNode))
                {
                    CalculateMinimumDistance(distanceMap, adjacentNode, edgeWeight, currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }

            settledNodes.add(currentNode);
        }

        Queue<MapDimensionGraphPoint> shortestPath = distanceMap.getShortestPath(target);

        if (shortestPath == null)
        {
            return null;
        }

        Queue<Map> result = new Queue<>();

        for (MapDimensionGraphPoint entry : shortestPath)
        {
            result.addLast(entry.getMap());
        }

        result.addLast(target.getMap());

        return result;
    }

    public static void Build()
    {
        Clear();

        for (Map map: Map.All())
        {
            MapDimensionGraphPoint me = EnsureMap(map);

            ObjectMap.Values<ActiveData> portals =
                map.getActivesForTag(Constants.ActiveTags.PORTAL, true);

            for (ActiveData activeData : portals)
            {
                ServerPortalComponentData cmp = activeData.getComponent(ServerPortalComponentData.class);
                if (cmp == null)
                    continue;

                PortalData other = cmp.findOtherPortal();
                if (other == null)
                    continue;

                Map otherMap = other.getMap(Map.class);
                if (otherMap == null)
                    continue;

                MapDimensionGraphPoint otherGraphPoint = EnsureMap(otherMap);

                float distance = 1;

                me.addNeighbor(otherGraphPoint, distance);
                otherGraphPoint.addNeighbor(me, distance);
            }
        }
    }

    public static void Clear()
    {
        WayPoints.clear();
    }
}
