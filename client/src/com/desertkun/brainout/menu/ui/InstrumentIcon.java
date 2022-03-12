package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.components.InstrumentAnimationComponent;
import com.desertkun.brainout.content.components.ReplaceSlotComponent;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.components.InstrumentAnimationComponentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;

public class InstrumentIcon extends WidgetGroup implements Disposable
{
    private final InstrumentInfo info;
    private final InstrumentAnimationComponentData data;
    private final Vector2 location;
    private final Bone centerBone;
    private final boolean dynamic;

    private FrameBuffer cached;
    private TextureRegion cachedTexture;
    private boolean dirty;
    private OrthographicCamera camera;
    private Batch cacheBatch;

    private ObjectSet<String> disabledAttachments;
    private OrderedMap<Slot, Attachment> originalAttachments;

    public InstrumentIcon(InstrumentInfo info, float instrumentScale, boolean dynamic)
    {
        this.info = info;
        this.dynamic = dynamic;
        this.dirty = false;

        InstrumentAnimationComponent animation = info.instrument.getComponentFrom(InstrumentAnimationComponent.class);

        if (animation == null)
        {
            throw new RuntimeException("Instrument " + info.instrument.getID() + " should have some animation.");
        }

        location = new Vector2();

        data = animation.getComponent(null);

        float scale = ClientConstants.Graphics.RES_SIZE * instrumentScale * (1f / 0.4f);

        data.setSkin(info.skin);
        data.getSkeleton().getRootBone().setScale(data.getSkeleton().getRootBone().getScaleX() * scale);

        data.init();

        centerBone = data.getSkeleton().findBone("center-bone");

        if (dynamic)
        {
            originalAttachments = new OrderedMap<>();
            updateOriginalAttachments();
        }
        else
        {
            updateLocation();
        }
    }

    public void animateTo(String animation, int frame, int frames)
    {
        AnimationState.TrackEntry track = data.getState().setAnimation(0, animation, false);
        track.setTimeScale(0);
        track.setAnimationEnd(((float)frame / (float)frames) * track.getAnimationEnd());

        data.getState().apply(data.getSkeleton());
        data.getSkeleton().updateWorldTransform();
    }

    private void updateOriginalAttachments()
    {
        if (dynamic)
        {
            originalAttachments.clear();

            for (Slot slot : data.getSkeleton().getSlots())
            {
                originalAttachments.put(slot, slot.getAttachment());
            }
        }
    }

    public void resetAnimation()
    {
        clearToOriginalArrachments();

        for (String key : info.upgrades.orderedKeys())
        {
            Upgrade upgrade = info.upgrades.get(key);
            ReplaceSlotComponent replaceSlot = upgrade.getComponent(ReplaceSlotComponent.class);

            if (replaceSlot != null)
            {
                replaceSlot.upgradeSkeleton(data.getSkeleton());
            }
        }

        if (disabledAttachments != null)
        {
            for (String name : disabledAttachments)
            {
                Skeleton skeleton = data.getSkeleton();
                if (skeleton.findSlot(name) != null)
                    skeleton.setAttachment(name, null);
            }
        }

        if (cached != null)
        {
            dirty = true;
        }
    }

    private void clearToOriginalArrachments()
    {
        if (dynamic)
        {
            for (ObjectMap.Entry<Slot, Attachment> entry : originalAttachments)
            {
                entry.key.setAttachment(entry.value);
            }
        }
    }

    protected float getCenterX()
    {
        return 0.5f;
    }

    protected float getCenterY()
    {
        return 0.5f;
    }

    private void updateLocation()
    {
        location.set(getWidth() * getCenterX(), getHeight() * getCenterY());

        if (centerBone != null)
        {
            location.add(data.getSkeleton().getX(), data.getSkeleton().getY());
            location.sub(centerBone.getWorldX(), centerBone.getWorldY());
        }
    }

    public void setSkin(Skin skin)
    {
        info.skin = skin;

        resetSkin();
    }

    public void resetSkin()
    {
        clearToOriginalArrachments();

        data.setSkin(info.skin);
        data.updateSkin();

        updateOriginalAttachments();

        resetAnimation();
    }

    public void disableAttachment(String attachment)
    {
        if (disabledAttachments == null)
        {
            disabledAttachments = new ObjectSet<>();
        }

        disabledAttachments.add(attachment);

        resetAnimation();
    }

    public void enableUpgrade(String key, Upgrade upgrade)
    {
        info.upgrades.put(key, upgrade);

        resetAnimation();
    }

    public void disableUpgrade(String key)
    {
        info.upgrades.remove(key);

        resetAnimation();
    }

    @Override
    protected void drawChildren(Batch batch, float parentAlpha)
    {
        super.drawChildren(batch, parentAlpha);

        if (cachedTexture != null)
        {
            if (dirty)
            {
                dirty = false;

                batch.end();

                cached.begin();

                Gdx.gl.glClearColor(0, 0, 0, 0);
                Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

                cacheBatch.begin();
                cacheBatch.enableBlending();

                data.getSkeleton().setPosition(location.x, location.y);
                data.getSkeleton().updateWorldTransform();
                data.render(cacheBatch, null);

                cacheBatch.end();

                cached.end();

                batch.begin();
            }

            batch.draw(cachedTexture, 0, 0, getWidth(), getHeight());
        }
        else
        {
            data.getSkeleton().setPosition(location.x, location.y);
            data.getSkeleton().updateWorldTransform();
            data.render(batch, null);
        }
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        data.update(delta);
    }

    @Override
    public void validate()
    {
        super.validate();

        updateLocation();
    }

    public void init()
    {
        data.getSkeleton().updateWorldTransform();

        resetAnimation();
    }

    @Override
    public void dispose()
    {
        if (cached != null)
        {
            cached.dispose();
            cacheBatch.dispose();
        }
    }

    public void setCached(int width, int height)
    {
        try
        {
            cached = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        }
        catch (IllegalStateException ignored)
        {
            cached = null;
            return;
        }

        cachedTexture = new TextureRegion(cached.getColorBufferTexture());
        cachedTexture.flip(false, true);

        camera = new OrthographicCamera(width, height);

        camera.position.x = width / 2;
        camera.position.y = height / 2;

        cacheBatch = BrainOutClient.ClientSett.allocateNewBatch();

        camera.update();
        cacheBatch.setProjectionMatrix(camera.combined);
    }

    @Override
    public float getPrefWidth()
    {
        return getWidth();
    }

    @Override
    public float getMinHeight()
    {
        return getHeight();
    }

    public TextureRegion getCachedTexture()
    {
        return cachedTexture;
    }
}
