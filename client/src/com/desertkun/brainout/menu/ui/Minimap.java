package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.settings.ClientSettings;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.ThrowableActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.instrument.ChipData;
import com.desertkun.brainout.mode.GameMode;

public class Minimap extends Actor implements Disposable
{
    private final boolean lowGraphics;
    private OrthographicCamera camera;
    private FrameBuffer fb;
    private TextureRegion textureRegion;
    private float timer;
    private float blinky;

    private final static Color BLOCK_COLOR = Color.GRAY;
    private final static Color DRAW_COLOR = new Color(1, 1, 1, 0.5f);
    private final static float SCALE = 1.0f;

    private TextureAtlas.AtlasRegion marker,
        markerPointFriend, markerPointEnemy, markerPointNone, markerChip, throwable;

    private float offsetX, offsetY;

    public Minimap()
    {
        int v = BrainOutClient.ClientSett.getGraphicsQuality().getValue();
        lowGraphics = (v == ClientSettings.GRAPHICS_LOW) || (v == ClientSettings.GRAPHICS_VERY_LOW);

        Map map = Map.GetDefault();

        if (map == null)
            return;

        try
        {
            Pixmap.Format format = lowGraphics ? Pixmap.Format.RGBA4444 : Pixmap.Format.RGBA8888;
            fb = new FrameBuffer(format, map.getWidth(), map.getHeight(), false);
        }
        catch (IllegalStateException ignored)
        {
            fb = null;
            return;
        }

        fb.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        textureRegion = new TextureRegion(fb.getColorBufferTexture());
        textureRegion.flip(false, true);

        timer = 0;
        blinky = 0;
        camera = new OrthographicCamera();
        camera.setToOrtho(true, map.getWidth(), map.getHeight());

        camera.position.set(map.getWidth() / 2.0f, map.getHeight() / 2.0f, 0.0f);

        this.marker = BrainOutClient.getRegion("minimap-white");
        this.markerPointFriend = BrainOutClient.getRegion("minimap-point-friend");
        this.markerPointEnemy = BrainOutClient.getRegion("minimap-point-enemy");
        this.markerPointNone = BrainOutClient.getRegion("minimap-point-none");
        this.markerChip = BrainOutClient.getRegion("minimap-chip");
        this.throwable = BrainOutClient.getRegion("minimap-throwable-white");
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        if (fb == null)
            return;

        Map map = Map.GetDefault();

        super.draw(batch, parentAlpha);

        ClientController CC = BrainOutClient.ClientController;
        CSGame csGame = CC.getState(CSGame.class);

        if (csGame != null && csGame.getPlayerData() != null)
        {
            float playerX = csGame.getPlayerData().getX();
            float playerY = csGame.getPlayerData().getY();

            float w = getWidth() / (2.0f * SCALE), h = getHeight() / (2.0f * SCALE);

            float pX = MathUtils.clamp(playerX, w, map.getWidth() - w),
                pY = MathUtils.clamp(playerY, h, map.getHeight() - h);

            this.offsetX = (pX - w) * SCALE;
            this.offsetY = (pY - h) * SCALE;

            textureRegion.setRegion(
                (int)( pX - w),
                fb.getHeight() - (int)(h * 2.0f + (pY - h)),
                (int)(w * 2.0f),
                (int)(h * 2.0f));

            batch.setColor(DRAW_COLOR);
            batch.draw(textureRegion, getX(), getY(), getWidth(), getHeight());
            batch.setColor(Color.WHITE);

            ActiveData me = csGame.getPlayerData();
            Team myTeam = csGame.getTeam();

            for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
            {
                RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(
                    activeData.getOwnerId());

                boolean friend = (me != null ?
                    !CC.isEnemies(CC.getRemoteClients().get(activeData.getOwnerId()), CC.getMyRemoteClient()) :
                    !CC.isEnemies(activeData.getTeam(), myTeam));

                if (activeData instanceof PlayerData)
                {
                    if (remoteClient == null) continue;
                    if (!activeData.isVisible()) continue;

                    PlayerData playerData = ((PlayerData) activeData);

                    if (BrainOut.R.instanceOf(ChipData.class, playerData.getCurrentInstrument()))
                    {
                        drawMarker(batch, activeData, markerChip, csGame.getController().getColorOf(activeData.getTeam()));
                    }
                    else
                    {
                        drawMarker(batch, activeData, marker, csGame.getController().getColorOf(remoteClient, false, false));
                    }
                }
            }

            for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.THROWABLE, false))
            {
                if (activeData instanceof ThrowableActiveData)
                {
                    if (!((ThrowableActive) activeData.getContent()).isShowOnMinimap())
                        continue;
                }

                drawMarker(batch, activeData, throwable, csGame.getController().getColorOf(activeData.getTeam()));
            }

            for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.CHIP_RECEIVER, false))
            {
                drawMarker(batch, activeData,
                    activeData.getTeam() == myTeam ? markerPointFriend : markerPointEnemy, Color.WHITE);
            }

            for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.SPAWNABLE, false))
            {
                boolean friend = me != null ?
                    !CC.isEnemies(CC.getRemoteClients().get(activeData.getOwnerId()), CC.getMyRemoteClient()) :
                    !CC.isEnemies(activeData.getTeam(), myTeam);

                if (activeData instanceof FlagData)
                {
                    TextureAtlas.AtlasRegion region;

                    FlagData flagData = ((FlagData) activeData);

                    switch (flagData.getState())
                    {
                        case taking:
                        {
                            // if team is mine, ignore if it's far away
                            if (friend)
                            {
                                if (Vector2.dst(playerX, playerY,
                                        activeData.getX(), activeData.getY()) > getWidth() / SCALE)
                                {
                                    continue;
                                }
                            }

                            TextureAtlas.AtlasRegion blink1 = activeData.getTeam() == myTeam ? markerPointFriend :
                                    (activeData.getTeam() == null ? markerPointNone : markerPointEnemy);
                            TextureAtlas.AtlasRegion blink2 = flagData.getTakingTeam() == myTeam ? markerPointFriend :
                                    (flagData.getTakingTeam() == null ? markerPointNone : markerPointEnemy);

                            region = (blinky % 1 > 0.5f) ? blink1 : blink2;

                            break;
                        }
                        case normal:
                        default:
                        {
                            // if team is mine, ignore the flag if it's far away
                            if (friend || activeData.getTeam() == null)
                            {
                                if (Vector2.dst(playerX, playerY,
                                        activeData.getX(), activeData.getY()) > getWidth() / SCALE)
                                {
                                    continue;
                                }
                            }

                            region = activeData.getTeam() == myTeam ? markerPointFriend :
                                    (activeData.getTeam() == null ? markerPointNone : markerPointEnemy);

                            break;
                        }
                    }

                    drawMarker(batch, activeData, region, Color.WHITE);
                }
            }

            batch.end();

            ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

            ObjectMap<Integer, RemoteClient> remoteClients = BrainOutClient.ClientController.getRemoteClients();
            GameMode gameMode = BrainOutClient.ClientController.getGameMode();

            if (gameMode != null)
            {
                for (BulletData bullet : map.getBullets())
                {
                    boolean enemy;

                    RemoteClient remoteClient = remoteClients.get(bullet.getOwnerId());

                    if (remoteClient != null)
                    {
                        if (bullet.getOwnerId() == BrainOutClient.ClientController.getMyId())
                        {
                            enemy = false;
                        }
                        else
                        {
                            enemy = gameMode.isEnemies(myTeam, remoteClient.getTeam());
                        }

                        if (bullet.isSilent() && enemy)
                            continue;

                        if (enemy)
                        {
                            shapeRenderer.setColor(ClientConstants.Menu.KillList.MINIMAP_ENEMY_COLOR);
                        }
                        else
                        {
                            shapeRenderer.setColor(ClientConstants.Menu.KillList.MINIMAP_MY_COLOR);
                        }

                        shapeRenderer.getColor().a = bullet.calculateDamage() / bullet.getDamage();

                        float bulletX = getX() + (bullet.getX() * SCALE - offsetX),
                                bulletY = getY() + (bullet.getY() * SCALE - offsetY);

                        if (bulletX < getX())
                            continue;

                        if (bulletX > getX() + getWidth())
                            continue;

                        if (bulletY < getY())
                            continue;

                        if (bulletY > getY() + getHeight())
                            continue;

                        float scl = SCALE / 60.0f;

                        shapeRenderer.line(bulletX, bulletY, 0,
                                bulletX + bullet.getSpeed().x * scl,
                                bulletY + bullet.getSpeed().y * scl, 0);
                    }
                }
            }

            shapeRenderer.end();

            batch.begin();
        }
    }

    private void drawMarker(Batch batch, ActiveData activeData,
                            TextureAtlas.AtlasRegion atlasRegion,
                            Color color)
    {
        float w = atlasRegion.getRegionWidth(), h = atlasRegion.getRotatedPackedHeight();
        float limit = 4;

        batch.setColor(color);

        batch.draw(atlasRegion,
            getX() + (int)(MathUtils.clamp(activeData.getX() * SCALE - offsetX,
                    limit, getWidth() - limit) - w),
            getY() + (int)(MathUtils.clamp(activeData.getY() * SCALE - offsetY,
                    limit, getHeight() - limit) - h),
               w * 2, h * 2);

        batch.setColor(Color.WHITE);
    }


    @Override
    public void act(float delta)
    {
        super.act(delta);

        timer -= delta;
        blinky += delta;

        if (timer <= 0)
        {
            timer = lowGraphics ? 120.0f : 5.0f;
            redraw();
        }
    }

    private void redraw()
    {
        if (fb == null)
            return;

        Map map = Map.GetDefault();

        if (map == null || map.getBlocks() == null)
            return;

        fb.begin();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        camera.update();

        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(BLOCK_COLOR);

        for (int j = 0, t = map.getBlocks().getBlockHeight(); j < t; j++)
        {
            for (int i = 0, k = map.getBlocks().getBlockWidth(); i < k; i++)
            {
                ChunkData chunk = map.getChunk(i, j);

                for (int j1 = 0; j1 < Constants.Core.CHUNK_SIZE; j1++)
                {
                    for (int i1 = 0; i1 < Constants.Core.CHUNK_SIZE; i1++)
                    {
                        BlockData blockData = chunk.get(i1, j1, Constants.Layers.BLOCK_LAYER_FOREGROUND);

                        if (blockData != null)
                        {
                            shapeRenderer.rect((i * Constants.Core.CHUNK_SIZE) + i1,
                                    (j * Constants.Core.CHUNK_SIZE) + j1, 2, 2);
                        }
                    }
                }
            }
        }

        shapeRenderer.end();
        fb.end();
    }

    @Override
    public void dispose()
    {
        if (fb == null)
            return;

        fb.dispose();
    }
}
