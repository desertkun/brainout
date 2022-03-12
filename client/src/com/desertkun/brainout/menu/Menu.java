package com.desertkun.brainout.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.posteffects.PostEffects;
import com.desertkun.brainout.posteffects.effects.PostEffect;

public abstract class Menu extends Stage
{
    private GameState gs;
    private Table root;
    private DragAndDrop dragAndDrop;

    private PostEffects postEffects;
    protected Image bg;

    public enum MenuAlign
    {
        center,
        top,
        fillTop,
        bottom,
        fill,
        leftBottom,
        fillRight,
        fillCenter
    }

    public enum MenuSound
    {
        select,
        back,
        denied,
        character,
        hover,
        contentOwned,
        contentOwnedEx,
        levelUp,
        techLevelUp,
        install,
        skillpointsEarned,
        newWeaponSkill,
        equip,
        repair,
        trophy,
        chipSuccess,
        chipFail,
        gameStarted,
        geigerCard,
        rankUp,
        itemSold,
        trash
    }

    public Menu()
    {
        super(new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera()),
            BrainOutClient.ClientSett.allocateNewBatch());

        gs = null;
    }

    public boolean popIfFocusOut()
    {
        return false;
    }

    public boolean stayOnTop()
    {
        return false;
    }

    public PostEffects getPostEffects()
    {
        return postEffects;
    }

    public void setGameState(GameState gs)
    {
        this.gs = gs;
    }

    public void onInit()
    {
        initTable();
    }

    public void initPostEffects()
    {
        if (postEffects == null)
        {
            postEffects = new PostEffects();
            postEffects.init();
        }
    }

    public void initTable()
    {
        root = new Table();
        root.setFillParent(true);

        TextureRegion background = getBackground();

        if (background != null)
        {
            bg = new Image(background);
            bg.setScaling(Scaling.fill);
            bg.setFillParent(true);
            addActor(bg);
        }

        addActor(root);

        Table tableData = createUI();
        if (tableData != null)
        {
            Cell cell = root.add(tableData);

            switch (getMenuAlign())
            {
                case fill:
                {
                    cell.expand().fill();
                    break;
                }
                case center:
                {
                    cell.center();
                    break;
                }
                case top:
                {
                    cell.expandY().padTop(32).top();
                    break;
                }
                case fillRight:
                {
                    cell.expand().fillY().right().top();
                    break;
                }
                case fillCenter:
                {
                    cell.expand().fillY().center().top();
                    break;
                }
                case leftBottom:
                {
                    cell.expand().left().bottom();
                    break;
                }
                case fillTop:
                {
                    cell.expand().fillX().top();
                    break;
                }
                case bottom:
                {
                    cell.expandY().padBottom(32).bottom();
                    break;
                }
            }

            cell.row();
        }
    }

    protected TextureRegion getBackground()
    {
        return null;
    }

    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.center;
    }

    public static long playSound(MenuSound soundKind)
    {
        Sound sound = (Sound)BrainOut.ContentMgr.get(soundKind.toString());
        if (sound == null)
            return -1;

        return sound.play();
    }

    public Table getRootActor()
    {
        return root;
    }

    public void pop()
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        gs.popMenu(this);
    }

    public void remove()
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        gs.removeMenu(this);
    }

    public void popMeAndPushMenu(Menu menu)
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        gs.popMenu(this);
        gs.pushMenu(menu);
    }

    public void pushMenu(Menu menu)
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        gs.pushMenu(menu);
    }

    public abstract Table createUI();

    public void onRelease()
    {
        clear();

        if (postEffects != null)
        {
            postEffects.dispose();
        }

        gs = null;
    }

    public void onFocusIn()
    {
    }

    public void onFocusOut(Menu toMenu)
    {
    }

    public boolean lockUpdate() { return false; }

    public boolean lockRender() { return false; }

    public boolean lockInput() { return false; }

    public GameState getGameState()
    {
        return gs;
    }

    public void render()
    {
        if (postEffects != null)
        {
            postEffects.begin();
            super.draw();
            postEffects.end();
        }
        else
        {
            super.draw();
        }
    }

    public void addDelayedPostEffect(PostEffect postEffect, float delay)
    {
        if (getPostEffects() == null)
            return;

        getPostEffects().setEffect(postEffect);
        addAction(Actions.sequence(Actions.delay(delay), Actions.run(() -> getPostEffects().resetEffect())));
    }

    public void reset()
    {
        ((OrthographicCamera) getCamera()).setToOrtho(false, BrainOutClient.getWidth(), BrainOutClient.getHeight());
        getCamera().update();

        getViewport().setScreenSize(BrainOutClient.getWidth(), BrainOutClient.getHeight());
        getViewport().setWorldSize(BrainOutClient.getWidth(), BrainOutClient.getHeight());

        if (postEffects != null)
        {
            postEffects.reset();
        }

        clear();

        initTable();
    }

    public boolean escape()
    {
        return false;
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        switch (keyCode)
        {
            case Input.Keys.ESCAPE:
            {
                if (escape())
                {
                    return true;
                }
            }
        }

        return super.keyDown(keyCode);
    }

    public DragAndDrop getDragAndDrop()
    {
        return dragAndDrop;
    }

    public void setDragAndDrop(DragAndDrop dragAndDrop)
    {
        this.dragAndDrop = dragAndDrop;
    }
}
