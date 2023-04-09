package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.utils.ByteArrayUtils;
import org.anthillplatform.runtime.services.GameService;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Reflect("content.GlobalConflict")
public class GlobalConflict extends Content
{
    private int width = 7;
    private int height = 5;
    private Array<Zone> zones;
    private ObjectMap<String, Zone> zonesMap;

    public enum Owner
    {
        neutral,
        a,
        b
    }

    public GlobalConflict()
    {
        zones = new Array<>();
        zonesMap = new ObjectMap<>();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static Owner GetAccountOwner(String accountId, String groupId, long conflictStart)
    {
        String hashValue;

        if (groupId != null)
        {
            hashValue = groupId + "-" + conflictStart;
        }
        else
        {
            hashValue = accountId + "-" + conflictStart;
        }

        MessageDigest digest;

        try
        {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e)
        {
            return Owner.a;
        }

        byte[] b = digest.digest(hashValue.getBytes(StandardCharsets.UTF_8));

        return ((b[0] & 1) == 0) ? Owner.a : Owner.b;
    }

    public class Zone
    {
        private String key;
        private int x;
        private int y;
        private Array<Zone> neighbors;

        public Zone(String key, int x, int y)
        {
            this.key = key;
            this.x = x;
            this.y = y;
            this.neighbors = new Array<>();
        }

        public String getKey() {
            return key;
        }

        public Array<Zone> getNeighbors()
        {
            return neighbors;
        }

        private void addNeighbor(int x, int y)
        {
            if (x < 0)
                return;
            if (y < 0)
                return;
            if (x >= width)
                return;
            if (y >= height)
                return;
            Zone neighborZone = getZoneAt(x, y);
            this.neighbors.add(neighborZone);
        }

        public float getPhysicalX()
        {
            return x;
        }

        public float getPhysicalY()
        {
            return y + ((x % 2 == 0) ? 0.5f : 0);
        }

        private void calculateNeighbors()
        {
            addNeighbor(x, y - 1);
            addNeighbor(x, y + 1);
            addNeighbor(x - 1, y);
            addNeighbor(x + 1, y);

            if (x % 2 == 0)
            {
                addNeighbor(x - 1, y + 1);
                addNeighbor(x + 1, y + 1);
            }
            else
            {
                addNeighbor(x - 1, y - 1);
                addNeighbor(x + 1, y - 1);
            }
        }
    }

    public enum ZoneStatus
    {
        inactive,
        ongoing,
        full
    }

    public class ConflictData
    {
        private final long conflictStart;
        private Array<ZoneData> zoneDataInstances;
        private ObjectMap<String, ZoneData> zonesMap;

        public class ZoneData
        {
            private Zone zone;
            private long lastA;
            private long lastB;
            private Owner defaultOwner;
            private ZoneStatus status;
            private GameService.Room room;
            private int myPlayers;
            private int maxPlayers;
            private Array<ZoneData> neighbors;

            public ZoneData(Zone zone, Owner defaultOwner, long lastA, long lastB)
            {
                this.zone = zone;
                this.neighbors = new Array<>();
                this.defaultOwner = defaultOwner;
                this.lastA = lastA;
                this.lastB = lastB;
                this.status = ZoneStatus.inactive;
                this.myPlayers = 0;
            }

            public Array<ZoneData> getNeighbors() {
                return neighbors;
            }

            public int getMyPlayers() {
                return myPlayers;
            }

            public void setMyPlayers(int myPlayers) {
                this.myPlayers = myPlayers;
            }

            public void setMaxPlayers(int maxPlayers) {
                this.maxPlayers = maxPlayers;
            }

            public int getMaxPlayers() {
                return maxPlayers;
            }

            public void addNeighbor(ZoneData zoneData)
            {
                this.neighbors.add(zoneData);
            }

            public long lastAction()
            {
                long a = lastA >= conflictStart ? lastA : 0;
                long b = lastB >= conflictStart ? lastB : 0;

                return Math.max(a, b);
            }

            public ZoneStatus getStatus()
            {
                return status;
            }

            public void setStatus(ZoneStatus status) {
                this.status = status;
            }

            public void setRoomId(GameService.Room room) {
                this.room = room;
            }

            public GameService.Room getRoom() {
                return room;
            }

            public String getKey()
            {
                return zone.getKey();
            }

            public Zone getZone()
            {
                return zone;
            }

            private Owner getRealOwner()
            {
                long a = lastA >= conflictStart ? lastA : 0;
                long b = lastB >= conflictStart ? lastB : 0;

                if (a == 0 && b == 0)
                {
                    return defaultOwner;
                }

                if (a > b)
                {
                    return Owner.a;
                }
                else if (a < b)
                {
                    return Owner.b;
                }
                else
                {
                    return Owner.neutral;
                }
            }

            public Owner postProcess()
            {
                if (getOwner() == Owner.neutral)
                {
                    int neighborsA = 0;
                    int neighborsB = 0;

                    for (ZoneData neighbor : neighbors)
                    {
                        if (neighbor.getOwner() == Owner.a)
                        {
                            neighborsA++;
                        }

                        if (neighbor.getOwner() == Owner.b)
                        {
                            neighborsB++;
                        }
                    }

                    if (neighborsA == 0 && neighborsB > 0)
                    {
                        return Owner.a;
                    }

                    if (neighborsB == 0 && neighborsA > 0)
                    {
                        return Owner.b;
                    }
                }

                return Owner.neutral;
            }

            public Owner getOwner()
            {
                Owner realOwner = getRealOwner();

                long lastOurAction = lastAction();

                for (ZoneData neighbor : neighbors)
                {
                    if (neighbor.lastAction() < lastOurAction)
                        continue;

                    if (neighbor.getRealOwner() == Owner.neutral)
                        continue;

                    // a neighbor was captured (and owner changed)
                    if (realOwner != neighbor.getRealOwner())
                        return Owner.neutral;
                }

                return realOwner;
            }
        }

        public Array<ZoneData> getZones()
        {
            return zoneDataInstances;
        }

        public ZoneData getByKey(String key)
        {
            return zonesMap.get(key, null);
        }

        public Owner hasSomeoneWon()
        {
            int countA = 0;
            int countB = 0;

            for (ZoneData zone : getZones())
            {
                Owner ow = zone.getOwner();

                if (ow == Owner.a)
                {
                    countA++;
                }

                if (ow == Owner.b)
                {
                    countA++;
                }
            }

            if (countA == getZones().size)
            {
                return Owner.a;
            }

            if (countB == getZones().size)
            {
                return Owner.b;
            }

            return Owner.neutral;
        }

        public ConflictData(JSONObject data, long conflictStart)
        {
            this.conflictStart = conflictStart;

            zoneDataInstances = new Array<>();
            zonesMap = new ObjectMap<>();

            for (Zone zone : zones)
            {
                JSONObject zoneData = data.optJSONObject(zone.getKey());

                long a = 0;
                long b = 0;

                if (zoneData != null)
                {
                    a = zoneData.optLong("a");
                    b = zoneData.optLong("b");
                }

                Owner owner;

                if (zone.x == (width / 2))
                {
                    owner = Owner.neutral;
                }
                else
                {
                    owner = zone.x > (width / 2) ? Owner.b : Owner.a;
                }

                ZoneData z = new ZoneData(zone, owner, a, b);
                zoneDataInstances.add(z);
                zonesMap.put(zone.getKey(), z);
            }

            for (ZoneData zoneDataInstance : zoneDataInstances)
            {
                Zone zone = zoneDataInstance.getZone();

                for (Zone neighbor : zone.getNeighbors())
                {
                    zoneDataInstance.addNeighbor(zonesMap.get(neighbor.getKey()));
                }
            }
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        width = jsonData.getInt("width", width);
        height = jsonData.getInt("height", height);

        generate();
    }

    public ConflictData getData(JSONObject status, long conflictStart)
    {
        return new ConflictData(status, conflictStart);
    }

    private Zone getZoneAt(int x, int y)
    {
        return zones.get(x + y * width);
    }

    public Zone getZoneByKey(String key)
    {
        return zonesMap.get(key, null);
    }

    private void generate()
    {
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                String zoneId = getID() + "-" + y + "x" + x;
                Zone zone = new Zone(zoneId, x, y);
                zones.add(zone);
                zonesMap.put(zoneId, zone);
            }
        }

        for (Zone zone : zones)
        {
            zone.calculateNeighbors();
        }
    }

    public Array<Zone> getZones() {
        return zones;
    }
}
