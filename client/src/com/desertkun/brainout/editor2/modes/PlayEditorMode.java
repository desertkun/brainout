package com.desertkun.brainout.editor2.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.common.msg.client.editor2.SpawnEditor2Msg;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.NonContactBD;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.editor2.Editor2Menu;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.MyPlayerSetEvent;
import com.desertkun.brainout.gs.ActionPhaseState;
import com.desertkun.brainout.menu.impl.ActionPhaseMenu;

public class PlayEditorMode extends EditorMode implements EventReceiver
{
    private static final Color OK = Color.WHITE;
    private static final Color NO = Color.RED;

    private static final int WIDTH = 2;
    private static final int HEIGHT = 3;

    private Vector2 spawnPosition;
    private EditorMode previousMode;
    private boolean spawnValid;

    public PlayEditorMode(Editor2Menu menu)
    {
        super(menu);

        spawnPosition = new Vector2();
    }

    @Override
    public boolean mouseMove(Vector2 position)
    {
        spawnPosition.set(position);

        validatePosition();

        return true;
    }

    @Override
    public void init()
    {
        super.init();

        BrainOutClient.EventMgr.subscribeAt(Event.ID.setMyPlayer, this, true);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.setMyPlayer, this);
    }

    private void validatePosition()
    {
        Map map = Map.Get(getMenu().getDimension());

        float sX = spawnPosition.x - (float)WIDTH / 2.0f,
              sY = spawnPosition.y - (float)HEIGHT / 2.0f;

        spawnValid = true;

        for (int j = 0; j < HEIGHT + 1; j++)
        {
            for (int i = 0; i < WIDTH + 1; i++)
            {
                float x = sX + (float)i,
                        y = sY + (float)j;

                BlockData blockData = map.getBlockAt(x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

                if (blockData != null && (!(blockData instanceof NonContactBD)))
                {
                    spawnValid = false;
                    break;
                }
            }

            if (!spawnValid)
                break;
        }
    }

    @Override
    public boolean mouseDown(Vector2 position, int button)
    {
        spawnPosition.set(position);

        validatePosition();

        if (!spawnValid)
            return false;

        BrainOutClient.ClientController.sendTCP(new SpawnEditor2Msg(spawnPosition.x, spawnPosition.y,
            Map.GetDimensionId(getMenu().getDimension())));

        return true;
    }

    @Override
    public void renderPanels(Table panels)
    {

    }

    @Override
    public void renderBottomPanel(Table panel)
    {
        Table sign = new Table(BrainOutClient.Skin);
        sign.setBackground("buttons-group");

        Label notice = new Label(L.get("EDITOR_SELECT_SPAWN_POSITION"), BrainOutClient.Skin, "title-yellow");
        sign.add(notice).height(32).padLeft(8).padRight(8);

        panel.add(sign).padBottom(64);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);

        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.setColor(validateSpawnSelection() ? OK : NO);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        renderPlacementLocation();
        shapeRenderer.end();

        batch.begin();
    }

    private boolean validateSpawnSelection()
    {
        return spawnValid;
    }

    private void renderPlacementLocation()
    {
        ShapeRenderer shapeRenderer = BrainOutClient.ShapeRenderer;

        float sX = spawnPosition.x - (float)WIDTH / 2.0f,
              sY = spawnPosition.y - (float)HEIGHT / 2.0f;

        shapeRenderer.rect(sX, sY, WIDTH, HEIGHT);
    }

    public void setPreviousMode(EditorMode previousMode)
    {
        this.previousMode = previousMode;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case setMyPlayer:
            {
                MyPlayerSetEvent ev = ((MyPlayerSetEvent) event);

                if (ev.playerData != null)
                {
                    spawned(ev.playerData);
                }

                break;
            }
        }

        return false;
    }

    private void spawned(PlayerData playerData)
    {
        final CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);

        if (csGame == null)
            return;

        final ActionPhaseState ap = ((ActionPhaseState) getMenu().getGameState());

        if (ap == null)
            return;

        getMenu().pop();

        final ActionPhaseMenu apm = ap.getActionPhaseMenu();

        final Label notice = new Label(L.get("EDITOR_STOP_NOTICE"),
                BrainOutClient.Skin, "title-small");

        notice.setPosition(16, 16);
        apm.addActor(notice);

        apm.overrideEscape(() ->
        {
            csGame.executeConsole("kill");

            notice.remove();
            apm.overrideEscape(null);
        });
    }

    @Override
    public ID getID()
    {
        return ID.play;
    }
}
