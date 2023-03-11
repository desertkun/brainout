package com.desertkun.brainout.server;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.components.ServerWeaponComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.WayPoint;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.data.interfaces.BonePointData;
import com.desertkun.brainout.data.interfaces.Watcher;

import java.lang.StringBuilder;

public class ServerDebugWindow implements ApplicationListener, Watcher
{
    private static final float SCALE = 1f;
    private static final float MOVE_SPEED = 50;

    private final Vector2 windowSize;
    private OrthographicCamera camera;
    private Matrix4 debugRendererMatrix;
    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private ActiveData follow;
    private Vector2 mousePosition;
    private Vector2 tmp;

    private Queue<Vector2> currentPath;
    private ObjectSet<WayPointMap.BlockCoordinates> currentBlocksInWay;
    private Vector2 pathFrom;
    private BitmapFont font;

    private Vector2 visibilityCheckA, visibilityCheckB;
    private boolean visibilityCheckSuccess;

    public ServerDebugWindow()
    {
        this.windowSize = new Vector2(1600, 900);
        this.mousePosition = new Vector2();
        this.tmp = new Vector2();

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Server");
        cfg.setWindowedMode((int)windowSize.x, (int)windowSize.y);
        cfg.setForegroundFPS(60);

        new Lwjgl3Application(this, cfg);
    }

    @Override
    public void create()
    {
        camera = new OrthographicCamera(windowSize.x / (Constants.Graphics.BLOCK_SIZE / SCALE),
                windowSize.y / (Constants.Graphics.BLOCK_SIZE / SCALE));
        debugRenderer = new Box2DDebugRenderer(true, false, false, true, false, false);
        debugRendererMatrix = new Matrix4();

        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.setUseIntegerPositions(false);
        font.getData().setScale(1f / 16.0f);
        spriteBatch = new SpriteBatch();

        Map.SetWatcher(this);
    }

    @Override
    public void resize(int width, int height)
    {

    }

    private Vector2 convertMousePosition(float screenX, float screenY)
    {
        Map map = Map.Get(getDimension());

        if (map == null)
            return mousePosition;

        mousePosition.x = MathUtils.clamp(
            (screenX - (Gdx.graphics.getWidth() / 2.0f)) / Constants.Graphics.BLOCK_SIZE  + camera.position.x,
                0, map.getWidth());
        mousePosition.y = MathUtils.clamp((Gdx.graphics.getHeight() / 2.0f - screenY)
            / Constants.Graphics.BLOCK_SIZE + camera.position.y,
                0, map.getHeight());

        return mousePosition;
    }

    @Override
    public void render()
    {
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_BLEND);

        moveCamera();

        ServerMap map = Map.Get(getDimension(), ServerMap.class);

        if (map == null || map.getPhysicWorld() == null)
            return;

        camera.update();

        debugRendererMatrix.set(camera.combined);
        debugRendererMatrix.scale(Constants.Physics.SCALE_OF, Constants.Physics.SCALE_OF, Constants.Physics.SCALE_OF);
        debugRenderer.render(map.getPhysicWorld(), debugRendererMatrix);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        WayPointMap wayPointMap = map.getWayPointMap();

        if (wayPointMap != null)
        {
            wayPointMap.render(shapeRenderer);
        }

        Vector2 mousePointer = convertMousePosition(Gdx.input.getX(), Gdx.input.getY());

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
        {
            WayPoint wayPoint = map.getWayPointMap().getClosestWayPoint(mousePointer.x, mousePointer.y, 8, tmp);

            if (wayPoint != null)
            {
                shapeRenderer.setColor(Color.BLUE);
                shapeRenderer.circle(wayPoint.getX(), wayPoint.getY(), 2, 8);

                shapeRenderer.setColor(Color.RED);
                shapeRenderer.circle(tmp.x, tmp.y, 1, 3);
            }
        }

        if (Gdx.input.justTouched())
        {
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            {
                if (Gdx.input.isKeyPressed(Input.Keys.V) && follow instanceof PlayerData)
                {
                    visibilityCheckA = new Vector2();

                    if (BotControllerComponentData.GetCheckVisibilityLaunchData(follow.getMap(),
                        ((PlayerData) follow), visibilityCheckA))
                    {
                        visibilityCheckB = new Vector2(mousePointer);

                        visibilityCheckSuccess = BotControllerComponentData.CheckVisibility(
                            follow.getMap(), ((PlayerData) follow), visibilityCheckB.x, visibilityCheckB.y
                        );
                    }
                    else
                    {
                        visibilityCheckA = null;
                    }
                }
                else
                {
                    if (currentPath != null)
                    {
                        currentPath = null;
                        currentBlocksInWay = null;
                        pathFrom = new Vector2(mousePointer);
                    }
                    else if (pathFrom != null)
                    {
                        map.getWayPointMap().findPath(pathFrom.x, pathFrom.y, mousePointer.x, mousePointer.y,
                            16, null, new WayPointMap.PathSearchResult()
                        {
                            @Override
                            public void found(
                                Queue<Vector2> path, String dimension,
                                ObjectSet<WayPointMap.BlockCoordinates> blocksInWay,
                                ActiveData portalOfInterest)
                            {
                                pathFrom = null;
                                currentPath = path;
                                currentBlocksInWay = blocksInWay;
                            }

                            @Override
                            public void notFound()
                            {
                                pathFrom = null;
                                currentPath = null;
                            }
                                });
                    }
                    else
                    {
                        pathFrom = new Vector2(mousePointer);
                    }
                }
            }
            else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT))
            {
                map.setBlock((int)mousePointer.x, (int)mousePointer.y, null, Constants.Layers.BLOCK_LAYER_FOREGROUND,
                    true, true);
            }
        }

        if (currentPath != null)
        {
            shapeRenderer.setColor(Color.WHITE);

            Vector2 last = null;
            for (Vector2 entry : currentPath)
            {
                if (last != null)
                {
                    shapeRenderer.line(last.x, last.y, entry.x, entry.y);
                }

                last = entry;
            }
        }

        if (currentBlocksInWay != null)
        {
            shapeRenderer.setColor(Color.RED);

            for (WayPointMap.BlockCoordinates blockData : currentBlocksInWay)
            {
                shapeRenderer.rect(blockData.x + 0.1f, blockData.y + 0.1f, 0.8f, 0.8f);
            }
        }

        for (ObjectMap.Entry<Integer, ActiveData> entry :
            new ObjectMap.Entries<>(map.getActives().getItemsForTag(Constants.ActiveTags.PLAYERS)))
        {
            ActiveData activeData = entry.value;

            if (activeData.getTeam() != null)
            {
                shapeRenderer.setColor(activeData.getTeam().getColor());
            }
            else
            {
                shapeRenderer.setColor(Color.WHITE);
            }

            shapeRenderer.circle(activeData.getX(), activeData.getY(), 0.25f, 4);

            if (activeData instanceof PlayerData)
            {
                InstrumentData currentInstrument = ((PlayerData) activeData).getCurrentInstrument();
                if (currentInstrument != null)
                {
                    InstrumentAnimationComponentData wp = currentInstrument.getComponent(InstrumentAnimationComponentData.class);

                    if (wp != null)
                    {
                        BonePointData lp = wp.getLaunchPointData();
                        shapeRenderer.line(lp.getX(), lp.getY(),
                            lp.getX() - MathUtils.cosDeg(activeData.getAngle()) * 2.0f,
                            lp.getY() - MathUtils.sinDeg(activeData.getAngle()) * 2.0f);
                    }
                }
            }

            ColliderComponentData cl = activeData.getComponent(ColliderComponentData.class);

            if (cl != null)
            {
                for (ObjectMap.Entry<String, ColliderComponentData.Collider> entry_ :
                    new ObjectMap.Entries<>(cl.getColliders()))
                {
                    ColliderComponentData.Collider c = entry_.value;

                    float x1 = cl.getPosition().x + c.x1,
                          y1 = cl.getPosition().y + c.y1;

                    float w = c.x2 - c.x1,
                          h = c.y2 - c.y1;

                    shapeRenderer.rect(x1, y1, w, h);
                }
            }

            ServerPhysicsSyncComponentData p = activeData.getComponent(ServerPhysicsSyncComponentData.class);

            if (p != null)
            {
                shapeRenderer.setColor(Color.YELLOW);
                Vector2 tp = p.getTargetPosition();
                shapeRenderer.circle(tp.x, tp.y, 0.4f, 4);
            }

            BotControllerComponentData ctl = activeData.getComponent(BotControllerComponentData.class);

            if (ctl != null && ctl.getFollowPath() != null &&
                    ctl.getFollowPath().size > 0)
            {
                shapeRenderer.setColor(Color.RED);

                Vector2 last = null;
                for (Vector2 fp : ctl.getFollowPath())
                {
                    if (last != null)
                    {
                        shapeRenderer.line(last.x, last.y, fp.x, fp.y);
                    }

                    last = fp;
                }
            }
        }

        shapeRenderer.setColor(Color.RED);

        for (BulletData bullet : map.getBullets())
        {
            float x1 = bullet.getX(),
                y1 = bullet.getY(),
                x2 = x1 + MathUtils.cosDeg(bullet.getAngle()) * 4.0f,
                y2 = y1 + MathUtils.sinDeg(bullet.getAngle()) * 4.0f;

            shapeRenderer.line(x1, y1, x2, y2);
        }

        if (visibilityCheckA != null && follow != null)
        {
            shapeRenderer.setColor(visibilityCheckSuccess ? Color.GREEN : Color.PINK);
            shapeRenderer.line(visibilityCheckA.x, visibilityCheckA.y, visibilityCheckB.x, visibilityCheckB.y);
        }

        shapeRenderer.end();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
        {
            for (ObjectMap.Entry<Integer, ActiveData> entry : new ObjectMap.Entries<>(map.getActives().getItemsForTag(Constants.ActiveTags.PLAYERS)))
            {
                ActiveData activeData = entry.value;

                BotControllerComponentData cmp = activeData.getComponent(BotControllerComponentData.class);

                if (cmp != null)
                {
                    int id = 0;

                    for (Task task : cmp.getTasksStack().getTasks())
                    {
                        float x = activeData.getX(),
                              y = activeData.getY() + 4 + id;

                        font.draw(spriteBatch, task.getClass().getSimpleName(), x - 10.0f, y, 20, Align.center, false);

                        id++;
                    }
                }

            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1))
        {
            for (ObjectMap.Entry<Integer, ActiveData> entry : new ObjectMap.Entries<>(map.getActives().getItemsForTag(Constants.ActiveTags.PLAYERS)))
            {
                ActiveData activeData = entry.value;

                if (activeData instanceof PlayerData)
                {
                    PlayerData playerData = ((PlayerData) activeData);

                    InstrumentData currentInstrument = playerData.getCurrentInstrument();

                    StringBuilder info;

                    if (currentInstrument instanceof WeaponData)
                    {
                        WeaponData weaponData = ((WeaponData) currentInstrument);
                        ServerWeaponComponentData sw = weaponData.getComponent(ServerWeaponComponentData.class);

                        ServerWeaponComponentData.Slot primarySLot = sw.getSlot(Constants.Properties.SLOT_PRIMARY);
                        if (primarySLot != null && primarySLot.getBullet() != null)
                        {
                            info = new StringBuilder(weaponData.getWeapon().getID() + " [" + primarySLot.getBullet().getID() + "/" + primarySLot.getRounds() + "/" + primarySLot.getChambered() + "]");

                            if (primarySLot.hasMagazineManagement())
                            {
                                info.append("\n mags: ");

                                for (IntMap.Entry<ServerWeaponComponentData.Slot.Magazine> magazine : primarySLot.getMagazines())
                                {
                                    info.append(magazine.key).append(":").append(magazine.value.rounds).append(" ");
                                }
                            }
                        }
                        else
                        {
                            info = new StringBuilder(weaponData.getWeapon().getID() + " [unloaded]");
                        }
                    }
                    else
                    {
                        info = new StringBuilder("no weapon");
                    }

                    float x = activeData.getX(),
                        y = activeData.getY() + 4;

                    font.draw(spriteBatch, info.toString(), x - 10.0f, y, 20, Align.center, false);
                }
            }
        }

        spriteBatch.end();

        if (follow != null)
        {
            camera.position.set(follow.getX(), follow.getY(), 0);
        }
    }

    private void moveCamera()
    {
        float dt = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.W))
        {
            camera.position.y += dt * MOVE_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S))
        {
            camera.position.y -= dt * MOVE_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A))
        {
            camera.position.x -= dt * MOVE_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D))
        {
            camera.position.x += dt * MOVE_SPEED;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F))
        {
            if (follow != null)
            {
                follow = null;
            }
            else
            {
                Array<ActiveData> toFollow = new Array<>();
                for (Map map : Map.SafeAll())
                {
                    ActiveData follow_ = map.getRandomActiveForTag(Constants.ActiveTags.PLAYERS);

                    if (follow_ != null)
                    {
                        toFollow.add(follow_);
                    }
                }

                if (toFollow.size > 0)
                {
                    follow = toFollow.random();
                }
                else
                {
                    follow = null;
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.G))
        {
            if (follow != null)
            {
                follow = null;
            }
            else
            {
                Array<ActiveData> toFollow = new Array<>();
                for (Map map : Map.SafeAll())
                {
                    ActiveData follow_ = map.getActiveForTag(Constants.ActiveTags.PLAYERS,
                        activeData -> BrainOutServer.Controller.getClients().get(activeData.getOwnerId()) instanceof PlayerClient);

                    if (follow_ != null)
                    {
                        toFollow.add(follow_);
                    }
                }

                if (toFollow.size > 0)
                {
                    follow = toFollow.random();
                }
                else
                {
                    follow = null;
                }
            }
        }

    }

    @Override
    public void pause()
    {
        Gdx.graphics.setTitle("[INACTIVE] Server");
    }

    @Override
    public void resume()
    {
        Gdx.graphics.setTitle("Server");
    }

    @Override
    public void dispose()
    {
        font.dispose();
        spriteBatch.dispose();
        debugRenderer.dispose();
    }

    @Override
    public float getWatchX()
    {
        return 0;
    }

    @Override
    public float getWatchY()
    {
        return 0;
    }

    @Override
    public boolean allowZoom()
    {
        return false;
    }

    @Override
    public float getScale()
    {
        return 10;
    }

    @Override
    public String getDimension()
    {
        return follow != null ? follow.getDimension() : "default";
    }
}
