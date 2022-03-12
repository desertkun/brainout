package com.desertkun.brainout.data;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.block.Concrete;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveFilterComponentData;
import com.desertkun.brainout.events.ActiveActionEvent;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.containers.*;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.*;
import com.desertkun.brainout.events.SetBlockEvent;
import com.desertkun.brainout.events.UpdatedEvent;
import com.desertkun.brainout.inspection.Inspectable;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.inspection.props.PropertiesRegistration;
import com.desertkun.brainout.managers.ContentIndex;
import com.desertkun.brainout.utils.TimeMeasure;
import com.esotericsoftware.minlog.Log;

import java.util.TimerTask;

public abstract class Map extends DataContainer<RenderUpdatable> implements Json.Serializable, RenderUpdatable,
        Disposable, ComponentWritable, Inspectable, ContactListener
{
    private static Array<ActiveData> TmpArray;
    private static ObjectMap<String, Map> Dimensions;
    protected static ObjectMap<Integer, String> DimensionIds;

    protected static Watcher Watcher;

    static
    {
        TmpArray = new Array<>();
        Dimensions = new ObjectMap<>();
        DimensionIds = new ObjectMap<>();
    }

    protected final Json json;

    protected BlockMatrixData blocks;
    protected ActiveDataMap actives;
    protected ObjectMap<String, ActiveData> activeNameIndex;
    protected CompleteDataContainer<BulletData> bullets;

    protected String dimension;
    protected int dimensionId;
    protected String name;
    protected World physicWorld;
    private float physicsAccumulator;
    protected ContentIndex contentIndex;

    private TimeMeasure physicsCalculation = new TimeMeasure();
    private TimeMeasure mapUpdate = new TimeMeasure();

    protected CameraDataContainer<RenderUpdatable> root, cameraBefore, cameraAfter;
    protected ComponentObjectContainer components;
    protected ObjectMap<String, byte[]> extensions;

    private Vector2 offset;
    protected float speed;

    private float shakeDistance;

    private static Vector2 traceAngle = new Vector2();
    private static Vector2 tracePos = new Vector2();
    private static Vector2 tmp = new Vector2();

    @InspectableProperty(name = "custom", kind = PropertyKind.map, value = PropertyValue.vStringMap)
    public ObjectMap<String, String> custom;

    public Map(String dimension)
    {
        this(dimension, 0, 0);
    }

    public Map(String dimension, int width, int height)
    {
        this(dimension, width, height, true);
    }

    private static void RegisterDimension(String dimension, Map map)
    {
        Dimensions.put(dimension, map);
    }

    public static void UnregisterDimension(String dimension)
    {
        Dimensions.remove(dimension);
    }

    public static Map Get(String dimension)
    {
        if (dimension == null)
            return null;

        return Dimensions.get(dimension);
    }

    public static Map Get(int dimension)
    {
        String dimension_ = DimensionIds.get(dimension, null);

        if (dimension_ == null)
            return null;

        return Get(dimension_);
    }

    public static Map GetFirst()
    {
        if (Dimensions.size == 0)
            return null;
        return new ObjectMap.Values<>(Map.Dimensions).iterator().next();
    }

    public static Map GetDefault()
    {
        return Get("default");
    }

    public static <T extends Map> T GetDefault(Class<T> tClass)
    {
        return Get("default", tClass);
    }

    private static Thread MainThread;

    public static void SetMainThread(Thread thread)
    {
        MainThread = thread;
    }

    public static ObjectMap.Values<Map> All()
    {
        if (MainThread != null && Thread.currentThread() != MainThread)
        {
            throw new RuntimeException("Calling unsafe All for non-main thread.");
        }

        return Dimensions.values();
    }

    public static ObjectMap.Values<Map> SafeAll()
    {
        return new ObjectMap.Values<>(Map.Dimensions);
    }

    public static <T extends Map> ObjectMap<String, T> SafeAll(Class<T> tClass)
    {
        ObjectMap<String, T> v = new ObjectMap<String, T>();
        for (ObjectMap.Entry<String, Map> entry : Map.Dimensions)
        {
            if (tClass.isInstance(entry.value))
            {
                v.put(entry.key, ((T) entry.value));
            }
        }
        return v;
    }

    public static ObjectMap<String, Map> AllEntries()
    {
        return Dimensions;
    }


    @SuppressWarnings("unchecked")
    public static <T extends Map> ObjectMap.Values<T> All(Class<T> tClass)
    {
        return (ObjectMap.Values<T>)Dimensions.values();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Map> T Get(String dimension, Class<T> tClass)
    {
        if (dimension == null)
            return null;

        return (T)Dimensions.get(dimension);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Map> T Get(int dimension, Class<T> tClass)
    {
        String dimension_ = DimensionIds.get(dimension, null);

        if (dimension_ == null)
            return null;

        return Get(dimension_, tClass);
    }

    public static void Move(String dimension, String dimensionTo)
    {
        Dimensions.put(dimensionTo, Dimensions.remove(dimension));
    }

    public static int GetDimensionId(String dimension)
    {
        Map map = Get(dimension);

        if (map == null)
            return -1;

        return map.getDimensionId();
    }

    public static String FindDimension(int dimensionId)
    {
        return DimensionIds.get(dimensionId, "default");
    }

    public static void Dispose()
    {
        Array<Map> maps = new Array<>();
        Dimensions.values().toArray(maps);

        for (Map map : maps)
        {
            map.dispose();
        }

        Dimensions.clear();
    }

    public static String GetWatcherDimension()
    {
        if (Watcher == null)
            return null;

        return Watcher.getDimension();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Map> T GetWatcherMap(Class<T> tClass)
    {
        if (Watcher == null)
            return null;

        return (T)Dimensions.get(Watcher.getDimension());
    }

    public Map(String dimension, int width, int height, boolean init)
    {
        this.dimension = dimension;

        if (dimension != null)
        {
            RegisterDimension(dimension, this);
        }

        contentIndex = new ContentIndex();
        activeNameIndex = new ObjectMap<>();
        speed = 1.0f;
        name = "";

        json = new Json();
        BrainOut.R.tag(json);

        shakeDistance = 0;

        custom = new ObjectMap<>();
        extensions = new ObjectMap<>();
        components = new ComponentObjectContainer(dimension);
        offset = new Vector2();

        root = new CameraDataContainer<RenderUpdatable>()
        {
            @Override
            public void updateCamera(RenderContext context)
            {
                if (Watcher == null) return;
                if (!Watcher.getDimension().equals(getDimension())) return;

                Vector3 pos = getCamera().position;

                pos.x = Watcher.getWatchX() + context.x + offset.x;
                pos.y = Watcher.getWatchY() + context.y + offset.y;

                pos.scl(Constants.Graphics.RES_SIZE);
                pos.set((int)pos.x, (int)pos.y, (int)pos.z);
                pos.scl(1f / Constants.Graphics.RES_SIZE);

                getCamera().rotate(context.angle);
            }
        };

        cameraBefore = new CameraDataContainer<RenderUpdatable>() {
            @Override
            public void updateCamera(RenderContext context)
            {
                if (Watcher == null) return;
                if (!Watcher.getDimension().equals(getDimension())) return;

                getCamera().position.set(
                    (int)((Watcher.getWatchX() + offset.x + context.x) * Constants.Graphics.RES_SIZE),
                    (int)((Watcher.getWatchY() + offset.y + context.y) * Constants.Graphics.RES_SIZE), 0);

                getCamera().rotate(context.angle);
            }
        };

        cameraAfter = new CameraDataContainer<RenderUpdatable>() {
            @Override
            public void updateCamera(RenderContext context)
            {
                if (Watcher == null) return;
                if (!Watcher.getDimension().equals(getDimension())) return;

                getCamera().position.set(
                        (int)((Watcher.getWatchX() + offset.x + context.x) * Constants.Graphics.RES_SIZE),
                        (int)((Watcher.getWatchY() + offset.y + context.y) * Constants.Graphics.RES_SIZE), 0);

                getCamera().rotate(context.angle);
            }
        };

        actives = newActives(Constants.Layers.ACTIVE_LAYERS_COUNT, dimension);
        bullets = new CompleteDataContainer<>();
        blocks = newBlockMatrixData(this.dimension);

        initRoot();

        blocks.setSize(this, width, height, init);

        addItem(cameraBefore, RenderFilter.any);
        addItem(root, RenderFilter.any);
        addItem(cameraAfter, RenderFilter.any);
    }

    protected ActiveDataMap newActives(int layerCount, String dimension)
    {
        return new ActiveDataMap(layerCount, dimension);
    }

    protected BlockMatrixData newBlockMatrixData(String dimension)
    {
        return new BlockMatrixData(dimension);
    }

    protected void initRoot()
    {
        root.addUpdateItem(blocks);

        root.addRenderableItem(blocks.getRenderLayer(Constants.Layers.BLOCK_LAYER_BACKGROUND), RenderFilter.pre);
        root.addRenderableItem(blocks.getRenderLayer(Constants.Layers.BLOCK_LAYER_FOREGROUND), RenderFilter.pre);
        root.addItem(bullets, RenderFilter.pre);
        root.addItem(actives, RenderFilter.pre);
        root.addItem(components, RenderFilter.pre);
    }

    public ObjectMap<String, ActiveData> getActiveNameIndex()
    {
        return activeNameIndex;
    }

    public int generateServerId()
    {
        return actives.generateServerId();
    }

    public int generateClientId()
    {
        return actives.generateClientId();
    }

    public void initActive(ActiveData active) {}
    public void releaseActive(ActiveData active) {}

    public String getDimension()
    {
        return dimension;
    }

    public int getDimensionId()
    {
        return dimensionId;
    }

    public void setDimensionId(int dimensionId)
    {
        this.dimensionId = dimensionId;
    }

    public void setDimension(String dimension)
    {
        this.dimension = dimension;
    }

    public void addActive(int id, ActiveData active, boolean init)
    {
        addActive(id, active, init, true);
    }

    public void addActive(int id, ActiveData active, boolean init, boolean notify)
    {
        addActive(id, active, init, notify, ActiveData.ComponentWriter.TRUE);
    }

    public void addActive(int id, ActiveData active, boolean init, boolean notify, boolean delayed)
    {
        addActive(id, active, init, notify, delayed, ActiveData.ComponentWriter.TRUE);
    }

    public void addActive(int id, ActiveData active, boolean init, boolean notify,
                          ActiveData.ComponentWriter componentWriter)
    {
        addActive(id, active, init, notify, true, componentWriter);
    }

    public void addActive(int id, ActiveData active, boolean init, boolean notify, boolean delayed,
                          ActiveData.ComponentWriter componentWriter)
    {
        active.setId(id);

        if (init)
        {
            active.init();
        }

        actives.addItem(id, active);
        initActive(active);

        if (notify)
        {
            if (delayed)
            {
                BrainOut.EventMgr.sendDelayedEvent(
                    ActiveActionEvent.obtain(active, ActiveActionEvent.Action.added, componentWriter));
            }
            else
            {
                BrainOut.EventMgr.sendEvent(
                    ActiveActionEvent.obtain(active, ActiveActionEvent.Action.added, componentWriter));
            }
        }
    }

    public ActiveData getActiveData(int id)
    {
        return actives.get(id);
    }

    public ActiveDataMap getActives()
    {
        return actives;
    }

    private static ObjectMap<Integer, ActiveData> empty = new ObjectMap<>();

    public ObjectMap.Values<ActiveData> getActivesForTag(int tag, boolean allocate)
    {
        if (actives == null)
            return empty.values();

        ObjectMap<Integer, ActiveData> tmp = actives.getItemsForTag(tag, allocate);

        if (tmp == null)
            return empty.values();

        return tmp.values();
    }

    public ActiveData getActiveForTag(int tag, Predicate predicate)
    {
        for (ActiveData activeData : getActivesForTag(tag, false))
        {
            if (predicate.match(activeData))
                return activeData;
        }

        return null;
    }

    public ActiveData getActiveForTag_(int tag, Predicate predicate)
    {
        for (ObjectMap.Entry<Integer, ActiveData> dataEntry :
                new ObjectMap.Entries<>(getActives().getItemsForTag(tag)))
        {
            if (predicate.match(dataEntry.value))
                return dataEntry.value;
        }

        return null;
    }

    public int countActives(Predicate predicate)
    {
        int count = 0;

        for (ActiveData activeData : getActives().values())
        {
            if (predicate.match(activeData))
                count++;
        }

        return count;
    }

    public int countActivesForTag(int tag)
    {
        return actives.getItemsForTag(tag).size;
    }

    public int countActivesForTag(int tag, Predicate predicate)
    {
        int count = 0;

        for (ActiveData activeData : getActivesForTag(tag, false))
        {
            if (predicate.match(activeData))
                count++;
        }

        return count;
    }

    public int countActivesForID(String id)
    {
        int count = 0;

        for (ActiveData activeData : getActives().values())
        {
            if (activeData.getCreator() == null)
                continue;

            if (activeData.getCreator().getID().equals(id))
                count++;
        }

        return count;
    }

    public int countActivesForIDStartsWith(String id)
    {
        int count = 0;

        for (ActiveData activeData : getActives().values())
        {
            if (activeData.getCreator() == null)
                continue;

            if (activeData.getCreator().getID().startsWith(id))
                count++;
        }

        return count;
    }

    public Array<ActiveData> getActivesForTag(int tag, Predicate predicate)
    {
        Array<ActiveData> datas = new Array<>();

        for (ActiveData activeData : getActivesForTag(tag, false))
        {
            if (predicate.match(activeData))
                datas.add(activeData);
        }

        return datas;
    }

    public void getActivesForTag(int tag, Array<ActiveData> out, Predicate predicate)
    {
        for (ActiveData activeData : getActivesForTag(tag, false))
        {
            if (predicate.match(activeData))
                out.add(activeData);
        }
    }


    public ActiveData getRandomActiveForTag(int tag)
    {
        TmpArray.clear();
        actives.getItemsForTag(tag).values().toArray(TmpArray);

        if (TmpArray.size > 0)
        {
            return TmpArray.random();
        }

        return null;
    }

    public ActiveData getByOwnerId(int ownerId)
    {
        for (ActiveData activeData: getActives().values())
        {
            if (activeData.getOwnerId() == ownerId)
            {
                return activeData;
            }
        }

        return null;
    }

    public void removeActive(final ActiveData active, final boolean notify)
    {
        removeActive(active, notify, true, true);
    }

    public void relocateActive(final ActiveData active, final Map newMap, final int newId)
    {
        if (active == null)
            return;

        if (!active.isAlive())
            return;

        actives.removeItem(active.getId());
        newMap.actives.addItem(newId, active);
        active.setId(newId);

    }

    public void removeActive(final ActiveData active, final boolean notify,
                             final boolean release, final boolean ragdoll)
    {
        if (active == null) return;

        final int activeId = active.getId();

        postRunnable(() ->
        {
            if (active.isAlive())
            {
                if (notify)
                {
                    BrainOut.EventMgr.sendEvent(ActiveActionEvent.obtain(active,
                        ActiveActionEvent.Action.removed, ragdoll));
                }

                actives.removeItem(activeId);

                if (release)
                {
                    releaseActive(active);
                    active.release();
                }
            }
        });
    }

    public void moveActive(final ActiveData active, final float x, final float y)
    {
        postRunnable(new Runnable()
        {
            @Override
            public void run()
            {
                active.setPosition(x, y);
                active.updated();
            }
        });
    }

    public Json getJson()
    {
        return json;
    }

    public ActiveData newActiveData(int id, String data, boolean init)
    {
        JsonValue value = new JsonReader().parse(data);
        ActiveData activeData = newActiveData(json, value);
        if (activeData == null)
            return null;

        addActive(id, activeData, init);

        return activeData;
    }

    public void sortActives(int layer)
    {
        getActives().sort(layer);
    }

    public void reattachActive(int id, int layer)
    {
        ActiveData item = getActiveData(id);
        getActives().reattach(id, layer);
        item.updated();
    }

    public ActiveData updateActiveData(int id, String data, boolean init)
    {
        ActiveData activeData = getActives().get(id);

        if (activeData == null)
        {
            newActiveData(id, data, init);
        }
        else
        {
            JsonValue value = new JsonReader().parse(data);

            activeData.read(json, value);

            BrainOut.EventMgr.sendEvent(activeData, UpdatedEvent.obtain());
            BrainOut.EventMgr.sendEvent(ActiveActionEvent.obtain(activeData, ActiveActionEvent.Action.updated));
        }

        return activeData;
    }

    public CompleteDataContainer<BulletData> getBullets()
    {
        return bullets;
    }

    public void removeBullet(BulletData bulletData)
    {
        bullets.removeItem(bulletData);
    }

    public BulletData addBullet(BulletData bulletData)
    {
        bullets.addItem(bulletData);
        return bulletData;
    }

    public static void GetMouseScaleWatcher(float mouseX, float mouseY, Vector2 setTo)
    {
        setTo.set(mouseX / Constants.Graphics.RES_SIZE * Watcher.getScale(),
                  mouseY / Constants.Graphics.RES_SIZE * Watcher.getScale());
        setTo.add(Watcher.getWatchX(), Watcher.getWatchY());
    }

    public void setBlockNoRefresh(int x, int y, BlockData blockData, int layer, boolean updateCache)
    {
        if (blocks == null)
            return;

        blocks.set(x, y, blockData, layer, updateCache);
    }

    public boolean setBlock(int x, int y, BlockData blockData, int layer, boolean updateCache)
    {
        return setBlock(x, y, blockData, layer, updateCache, true);
    }

    public boolean setBlock(int x, int y, BlockData blockData, int layer, boolean updateCache, boolean delayed)
    {
        if (blocks == null)
            return false;

        blocks.set(x, y, blockData, layer, updateCache);

        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                blocks.refreshBlock(x + i, y + j, layer, delayed);
            }
        }

        if (delayed)
        {
            BrainOut.EventMgr.sendDelayedEvent(SetBlockEvent.obtain(x, y, layer, getDimension(), blockData));
        }
        else
        {
            BrainOut.EventMgr.sendEvent(SetBlockEvent.obtain(x, y, layer, getDimension(), blockData));
        }

        return true;
    }

    public boolean setBlock(int x, int y, BlockData blockData, int layer, boolean updateCache, int delaySettingUp)
    {
        if (blocks == null)
            return false;

        BrainOut.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOut.getInstance().postRunnable(() ->
                {
                    blocks.set(x, y, blockData, layer, updateCache);

                    for (int i = -1; i <= 1; i++)
                    {
                        for (int j = -1; j <= 1; j++)
                        {
                            blocks.refreshBlock(x + i, y + j, layer);
                        }
                    }
                });
            }
        }, delaySettingUp);

        BrainOut.EventMgr.sendDelayedEvent(SetBlockEvent.obtain(x, y, layer, getDimension(), blockData));

        return true;
    }

    public BlockData getBlock(int x, int y, int layer)
    {
        if (blocks == null)
            return null;

        return blocks.get(x, y, layer);
    }

    public ComponentObjectContainer getComponents()
    {
        return components;
    }

    public BlockData getBlockAt(float x, float y, int layer)
    {
        return getBlock((int) x, (int) y, layer);
    }

    public int getWidth()
    {
        if (blocks == null)
            return 0;

        return blocks.getWidth();
    }

    public int getHeight()
    {
        if (blocks == null)
            return 0;

        return blocks.getHeight();
    }

    public BlockMatrixData getBlocks()
    {
        return blocks;
    }

    public int getPos(float xy)
    {
        return (int)xy;
    }

    @Override
    public void write(Json json)
    {
        contentIndex.fillUp(BrainOut.ContentMgr);

        json.writeValue("dimension", dimension);
        json.writeValue("dimensionId", dimensionId);
        json.writeValue("contentIndex", contentIndex);
        json.writeValue("speed", speed);
        json.writeValue("name", name);

        json.writeObjectStart("custom");
        writeCustom(json);
        json.writeObjectEnd();
    }

    @Override
    public void write(Json json, ActiveData.ComponentWriter componentWriter, int owner)
    {
        long writeStart = System.currentTimeMillis();

        write(json);

        json.writeObjectStart("active");

        for (ObjectMap.Entry<Integer, ActiveData> item: actives.entries())
        {
            ActiveData activeData = item.value;

            ActiveFilterComponentData f = activeData.getComponent(ActiveFilterComponentData.class);
            if (f != null)
            {
                if (!f.filters(owner))
                    continue;
            }

            if (activeData.getCreator() != null)
            {
                json.writeObjectStart(item.key.toString());
                activeData.write(json, componentWriter, owner);
                json.writeObjectEnd();
            }
        }

        json.writeObjectEnd();

        long activesEnd = System.currentTimeMillis();

        BlockData.CURRENT_DIMENSION = getDimension();

        json.writeObjectStart("blocks");
        blocks.write(json, componentWriter, owner);
        json.writeObjectEnd();

        long writeEnd = System.currentTimeMillis();

        if (writeEnd - writeStart > 10)
        {
            long activesTook = activesEnd - writeStart;
            long blocksTook = writeEnd - activesEnd;
            if (Log.INFO) Log.info("Map " + getDimension() + " save took " + activesTook + " (actives) and " + blocksTook + " (blocks)");
        }
    }

    public void clear()
    {
        blocks.clear();
    }

    public InstrumentData newInstrument(Json json, JsonValue jsonValue)
    {
        Instrument instrument = newT(jsonValue, Instrument.class);
        if (instrument == null)
        {
            return null;
        }

        InstrumentData instrumentData = instrument.getData(this.dimension);
        instrumentData.read(json, jsonValue);

        return instrumentData;
    }

    public static ConsumableItem newConsumableItem(Json json, JsonValue jsonValue)
    {
        if (jsonValue == null || !jsonValue.has("class"))
            return null;

        try
        {
            ConsumableItem obj = (ConsumableItem)BrainOut.R.newInstance(jsonValue.getString("class"));

            if (obj == null)
            {
                return null;
            }

            obj.read(json, jsonValue);

            return obj;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    protected boolean validateActive(Active active)
    {
        return true;
    }

    public <T extends Content> T newT(JsonValue jsonValue, Class<T> cc)
    {
        if (!jsonValue.has("class"))
        {
            return null;
        }

        JsonValue clazz = jsonValue.get("class");
        if (clazz.isString())
        {
            return BrainOut.ContentMgr.get(clazz.asString(), cc);
        }
        else if (clazz.isNumber())
        {
            return BrainOut.getInstance().getController().getContentFromIndex(clazz.asInt(), cc);
        }
        else
        {
            return null;
        }
    }

    public ActiveData newActiveData(Json json, JsonValue jsonValue)
    {
        Active active = newT(jsonValue, Active.class);

        if (active == null)
            return null;
        if (!validateActive(active))
            return null;

        ActiveData activeData = active.getData(getDimension());
        activeData.read(json, jsonValue);

        return activeData;
    }

    public BlockData newBlockData(Json json, JsonValue jsonValue)
    {
        JsonValue clazz = jsonValue.get("c");
        if (clazz == null) return null;

        Block block;
        if (clazz.isString())
        {
            block = (Block)BrainOut.ContentMgr.get(clazz.asString());
        }
        else
        {
            block = ((Block) contentIndex.getContent(clazz.asInt()));
        }

        if (block == null) return null;

        BlockData blockData = block.getBlock();
        blockData.read(json, jsonValue);

        return blockData;
    }

    public ChunkData getChunkAt(int x, int y)
    {
        if (blocks == null)
            return null;

        return blocks.getChunkByBlock(x, y);
    }

    public ChunkData getChunk(int x, int y)
    {
        return blocks.getChunk(x, y);
    }

    public InstrumentData newInstrumentData(Json json, JsonValue jsonValue)
    {
        Instrument instrument = newT(jsonValue, Instrument.class);

        if (instrument == null)
            return null;

        InstrumentData instrumentData = instrument.getData(this.dimension);
        instrumentData.read(json, jsonValue);
        return instrumentData;
    }

    public interface IgnoreCheck
    {
        boolean check(int blockX, int blockY);
    }

    public boolean trace(float xFrom, float yFrom, float xTo, float yTo, int layer,
        Vector2 result, IgnoreCheck ignoreCheck)
    {
        tmp.set(xTo, yTo).sub(xFrom, yFrom);
        return trace(xFrom, yFrom, layer, tmp.angleDeg(), tmp.len(), result, ignoreCheck);
    }

    public boolean trace(float x, float y, int layer, float angle, float maxDistance,
                         Vector2 result, IgnoreCheck ignoreCheck)
    {
        traceAngle.set(
            (float) Math.cos(Math.toRadians((double) angle)),
            (float) Math.sin(Math.toRadians((double) angle))
        );
        tracePos.set(x, y);

        float dist = maxDistance;

        while (dist >= 0)
        {
            int blockX = (int)tracePos.x, blockY = (int)tracePos.y;

            BlockData blockData = getBlockAt(blockX, blockY, layer);

            if (blockData != null)
            {
                if ((ignoreCheck != null && !ignoreCheck.check(blockX, blockY)) ||
                    blockData.getCreator().isCanBeSeenTrough())
                {
                    tracePos.add(traceAngle);
                    dist -= 1;
                    continue;
                }

                if (blockData.isContact(null, tracePos.x % 1, tracePos.y % 1, null, traceAngle, 0, this,
                    blockX, blockY))
                {
                    if (result != null)
                    {
                        accurateTrace(result, layer);
                    }

                    return true;
                }
            }

            tracePos.add(traceAngle);
            dist -= 1;
        }

        if (result != null)
        {
            result.set(tracePos);
        }

        return false;
    }

    public boolean trace(float x, float y, int layer, float angle, float maxDistance, Vector2 result)
    {
        return trace(x, y, layer, angle, maxDistance, result, null);
    }

    private void accurateTrace(Vector2 result, int layer)
    {
        int dist = 10;

        while (dist >= 0)
        {
            int blockX = (int)tracePos.x, blockY = (int)tracePos.y;

            tracePos.add(- traceAngle.x / 10f, - traceAngle.y / 10f);

            BlockData blockData = getBlockAt(blockX, blockY, layer);

            if (blockData == null || !blockData.isContact(null,
                tracePos.x % 1, tracePos.y % 1, null, traceAngle, 0, this,
                blockX, blockY))
            {
                result.set(tracePos);
                break;
            }

            dist -= 1;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        clear();

        dimension = jsonData.getString("dimension", "default");

        readDimension(json, jsonData);

        if (jsonData.has("contentIndex"))
        {
            contentIndex.read(json, jsonData.get("contentIndex"));
        }

        speed = jsonData.getFloat("speed", speed);
        name = jsonData.getString("name", name);

        blocks.read(this, json, jsonData.get("blocks"));

        if (jsonData.hasChild("active"))
        {
            JsonValue child = jsonData.getChild("active");

            while (child != null)
            {
                ActiveData activeData;

                try
                {
                    activeData = newActiveData(json, child);
                }
                catch (Exception e)
                {
                    activeData = null;
                }

                if (activeData != null)
                {
                    addActive(Integer.valueOf(child.name()), activeData, false);
                }

                child = child.next();
            }
        }

        if (jsonData.has("custom"))
        {
            readCustom(json, jsonData.get("custom"));
        }
    }

    protected void readDimension(Json json, JsonValue jsonData)
    {
        dimensionId = jsonData.getInt("dimensionId", dimensionId);

        if (dimensionId >= 0)
        {
            DimensionIds.put(dimensionId, dimension);
        }
    }

    public ObjectMap<String, String> getCustomItems()
    {
        return custom;
    }

    public Json.Serializable getCustom()
    {
        return new Json.Serializable()
        {
            @Override
            public void write(Json json)
            {
                writeCustom(json);
            }

            @Override
            public void read(Json json, JsonValue jsonData)
            {
                readCustom(json, jsonData);
            }
        };
    }

    public void readCustom(Json json, JsonValue jsonValue)
    {
        custom.clear();

        if (jsonValue.isObject())
        {
            for (JsonValue v: jsonValue)
            {
                custom.put(v.name(), v.asString());
            }
        }

        initCustom();
    }

    public void setCustom(String key, String value)
    {
        this.custom.put(key, value);
    }

    protected void initCustom() {}

    public String getCustom(String key)
    {
        return custom.get(key);
    }

    public void writeCustom(Json json)
    {
        for (ObjectMap.Entry<String, String> entry: custom)
        {
            json.writeValue(entry.key, entry.value);
        }
    }

    public Matrix4 getProjectionMatrix()
    {
        return root.getCamera().combined;
    }

    public CameraDataContainer getCameraBefore()
    {
        return cameraBefore;
    }

    @Override
    public void dispose()
    {
        if (actives == null)
            return;

        actives.release();
        actives.clear();

        extensions.clear();
        blocks.dispose();

        if (physicWorld != null)
        {
            physicWorld.dispose();
        }

        blocks = null;
        physicWorld = null;

        UnregisterDimension(this.dimension);

        if (dimensionId >= 0)
        {
            DimensionIds.remove(dimensionId);
            dimensionId = -1;
        }
    }

    public boolean isSafeMap()
    {
        return dimension.startsWith("intro") || dimension.startsWith("real-");
    }

    public static boolean IsSafeMap(String dimension)
    {
        return dimension.startsWith("intro") || dimension.startsWith("real-");
    }

    public void init()
    {
        physicsAccumulator = 0;

        physicWorld = new World(new Vector2(0, - Constants.Core.GRAVITY * Constants.Physics.MASS_COEF), true);
        physicWorld.setContactListener(this);

        initChilds();
    }

    protected void initChilds()
    {
        if (blocks == null)
            return;

        blocks.init();
        actives.init();
    }

    @Override
    public void update(float dt)
    {
        dt *= speed;

        mapUpdate.start();
        super.update(dt);
        mapUpdate.end();

        physicsCalculation.start();

        physicsAccumulator += dt;

        int needSteps = (int)(Math.floor(physicsAccumulator / Constants.Physics.FIXED_TIME_STEP));

        if (needSteps > 0)
        {
            physicsAccumulator -= needSteps * Constants.Physics.FIXED_TIME_STEP;
        }

        int steps = Math.min(needSteps, Constants.Physics.MAX_STEPS);

        if (physicWorld != null)
        {
            for (int i = 0; i < steps; i++)
            {
                physicWorld.step(Constants.Physics.FIXED_TIME_STEP,
                        Constants.Physics.VELOCITY_ITERATIONS,
                        Constants.Physics.POSITION_ITERATIONS);
            }
        }

        physicsCalculation.end();

        if (shakeDistance > 0.02f)
        {
            if (dt != 0)
            {
                float rate = MathUtils.clamp(60.f * dt, 1.0f, 5.0f);
                float u = ((rate + 3.0f) / 4.0f);
                shakeDistance *= 0.8f / u;
                offset.set(shakeDistance, 0);
                offset.rotate(MathUtils.random(360));
            }
        }
        else
        {
            shakeDistance = 0;
            offset.set(0, 0);
        }
    }

    public void shake(float power)
    {
        shakeDistance = power;
    }

    public ChunkData getChunkData(BlockMatrixData matrixData, int x, int y)
    {
        return new ChunkData(matrixData, x, y);
    }

    public ActiveData getActiveAt(float x, float y)
    {
        for (ObjectMap.Entry<Integer, ActiveData> active : getActives().entries())
        {
            ActiveData activeData = active.value;
            if (activeData.hover(x - activeData.getX(), y - activeData.getY()))
            {
                return activeData;
            }
        }

        return null;
    }

    @Override
    public void beginContact(Contact contact)
    {
        //
    }

    @Override
    public void endContact(Contact contact)
    {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold)
    {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse)
    {

    }

    public void updateCustom(Map from)
    {
        custom.putAll(from.custom);
    }

    public static interface Predicate
    {
        public boolean match(ActiveData activeData);
    }

    public boolean isSameDimension(ActiveData activeData)
    {
        return dimension.equals(activeData.getDimension());
    }

    public void getListActivesGap(float detectDist, float x, float y, Class<? extends ActiveData> classOf,
                                       Predicate predicate)
    {
        for (ActiveData data : getActives().values())
        {
            if (data == Watcher) continue;
            if (!classOf.isInstance(data)) continue;

            float diffX = (data.getX() - x), diffY = (data.getY() - y);
            float dist = diffX * diffX + diffY * diffY;

            if (dist < detectDist)
            {
                predicate.match(data);
            }
        }
    }

    public void getListActivesForTag(float detectDist, float x, float y,
        Class<? extends ActiveData> classOf, int tag, Predicate predicate)
    {
        for (ActiveData data : getActivesForTag(tag, false))
        {
            if (data == Watcher) continue;
            if (!classOf.isInstance(data)) continue;

            float diffX = (data.getX() - x), diffY = (data.getY() - y);
            float dist = diffX * diffX + diffY * diffY;

            if (dist < detectDist)
            {
                predicate.match(data);
            }
        }
    }

    public int countClosestActiveForTag(
            float detectDist, float x, float y,
            Class<? extends ActiveData> classOf,
            int tag, Predicate predicate)
    {
        ActiveData closestActive = null;
        detectDist *= detectDist;
        int count = 0;

        for (ActiveData data : getActivesForTag(tag, false))
        {
            if (data == Watcher) continue;
            if (!classOf.isInstance(data)) continue;

            float diffX = (data.getX() - x), diffY = (data.getY() - y);
            float dist = diffX * diffX + diffY * diffY;

            if (dist < detectDist)
            {
                if (predicate != null && !predicate.match(data)) continue;

                closestActive = data;
                count++;
            }
        }

        return count;
    }

    public ActiveData getClosestActiveForTag(
        float detectDist, float x, float y,
        Class<? extends ActiveData> classOf,
        int tag, Predicate predicate)
    {
        ActiveData closestActive = null;
        detectDist *= detectDist;

        for (ActiveData data : getActivesForTag(tag, false))
        {
            if (data == Watcher) continue;
            if (!classOf.isInstance(data)) continue;

            float diffX = (data.getX() - x), diffY = (data.getY() - y);
            float dist = diffX * diffX + diffY * diffY;

            if (dist < detectDist)
            {
                if (predicate != null && !predicate.match(data)) continue;

                closestActive = data;
                detectDist = dist;
            }
        }

        return closestActive;
    }

    public ActiveData getClosestActive(float detectDist, float x, float y, Class<? extends ActiveData> classOf,
                                       Predicate predicate)
    {
        ActiveData closestActive = null;

        for (ActiveData data : getActives().values())
        {
            if (data == Watcher) continue;
            if (!classOf.isInstance(data)) continue;

            float diffX = (data.getX() - x), diffY = (data.getY() - y);
            float dist = diffX * diffX + diffY * diffY;

            if (dist < detectDist)
            {
                if (predicate != null && !predicate.match(data)) continue;

                closestActive = data;
                detectDist = dist;
            }
        }

        return closestActive;
    }

    public abstract void postRunnable(Runnable runnable);

    public World getPhysicWorld()
    {
        return physicWorld;
    }

    @Override
    public void inspect(PropertiesRegistration registration)
    {
        //
    }

    public ContentIndex getContentIndex()
    {
        return contentIndex;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setSpeed(float speed)
    {
        this.speed = speed;
    }

    public float getSpeed()
    {
        return speed;
    }

    @Override
    public String toString()
    {
        return "[Map]";
    }

    public TimeMeasure getPhysicsCalculation()
    {
        return physicsCalculation;
    }

    public TimeMeasure getMapUpdate()
    {
        return mapUpdate;
    }

    public static void SetWatcher(Watcher watcher)
    {
        Watcher = watcher;
    }

    public static Watcher GetWatcher()
    {
        return Watcher;
    }

    public void addExtension(String key, byte[] extension)
    {
        extensions.put(key, extension);
    }

    public void updateExtensions()
    {
        //
    }

    public byte[] getExtension(String key)
    {
        return extensions.get(key, null);
    }

    public void removeExtension(String key)
    {
        extensions.remove(key);
    }

    public ObjectMap<String, byte[]> getExtensions()
    {
        return extensions;
    }

    private static boolean RayCastContact;
    private static Vector2 RayCastContactPoint = new Vector2();

    public boolean rayCast(float fromX, float fromY, float toX, float toY)
    {
        return rayCast(fromX, fromY, toX, toY, null);
    }

    public boolean rayCast(float fromX, float fromY, float toX, float toY, Vector2 contactPointOut)
    {
        if (physicWorld == null)
            return false;

        if (Vector2.dst2(fromX, fromY, toX, toY) < 0.0001f)
            return false;

        RayCastContact = false;
        physicWorld.rayCast(Map::RayCastNeighbors, fromX, fromY, toX, toY);
        if (RayCastContact && contactPointOut != null)
            contactPointOut.set(RayCastContactPoint);
        return RayCastContact;
    }

    private static float RayCastNeighbors(Fixture fixture, Vector2 point, Vector2 normal, float v)
    {
        RayCastContact = true;
        RayCastContactPoint.set(point);

        return 0;
    }
}
