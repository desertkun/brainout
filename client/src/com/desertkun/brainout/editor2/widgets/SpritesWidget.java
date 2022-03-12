package com.desertkun.brainout.editor2.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.components.CategoryComponent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.content.components.UserSpriteWithBlocksComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.editor2.Editor2Menu;
import com.desertkun.brainout.editor2.UploadCustomImageMenu;
import com.desertkun.brainout.managers.ContentManager;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.Tooltip;

import java.util.Comparator;

public class SpritesWidget extends Table
{
    private final DragAndDrop dropInto;
    private final Editor2Menu menu;
    private Queue<DragAndDrop.Source> sources;
    private Table panel;
    private ObjectMap<String, String> tags;
    private static String currentTag = "CUSTOM_WALL";
    private Table objectsTable;

    public static final float TOTAL_WIDTH = 212;
    private ScrollPane pane;

    public SpritesWidget(DragAndDrop dropInto, Editor2Menu menu)
    {
        this.dropInto = dropInto;
        this.sources = new Queue<>();
        this.tags = new ObjectMap<>();
        this.menu = menu;

        fillTags();

        Group panelWidget = new Group();

        panel = new Table(BrainOutClient.Skin);
        renderPanel(panel, menu);
        panel.setFillParent(true);
        panelWidget.addActor(panel);

        add(panelWidget).width(TOTAL_WIDTH).expandY().fillY();

        panel.setX(TOTAL_WIDTH);
        panel.addAction(Actions.moveTo(0, 0, 0.25f, Interpolation.pow2Out));

        setTouchable(Touchable.childrenOnly);
    }

    public interface UpdateOffsetCallback
    {
        void update(float x, float y);
    }

    public class SpritePayload extends DragAndDrop.Payload
    {
        private final float offsetX;
        private final float offsetY;
        private final Queue<UpdateOffsetCallback> offsetCallbacks;

        public SpritePayload(float offsetX, float offsetY)
        {
            this.offsetX = offsetX;
            this.offsetY = offsetY;

            offsetCallbacks = new Queue<>();
        }

        public float getOffsetY()
        {
            return offsetY;
        }

        public float getOffsetX()
        {
            return offsetX;
        }

        public void updateOffset(float x, float y)
        {
            for (UpdateOffsetCallback callback : offsetCallbacks)
            {
                callback.update(x, y);
            }
        }

        public void addOffsetCallback(UpdateOffsetCallback offsetCallback)
        {
            this.offsetCallbacks.addLast(offsetCallback);
        }
    }

    public class UserSpritePayload extends DragAndDrop.Payload
    {
        private final float offsetX;
        private final float offsetY;
        private final Queue<UpdateOffsetCallback> offsetCallbacks;

        private final int width;
        private final int height;
        private String sprite;

        public UserSpritePayload(float offsetX, float offsetY, int width, int height, String sprite)
        {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.width = width;
            this.height = height;
            this.sprite = sprite;

            offsetCallbacks = new Queue<>();
        }

        public float getOffsetY()
        {
            return offsetY;
        }

        public float getOffsetX()
        {
            return offsetX;
        }

        public void updateOffset(float x, float y)
        {
            for (UpdateOffsetCallback callback : offsetCallbacks)
            {
                callback.update(x, y);
            }
        }

        public void addOffsetCallback(UpdateOffsetCallback offsetCallback)
        {
            this.offsetCallbacks.addLast(offsetCallback);
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }

        public String getSprite()
        {
            return sprite;
        }
    }

    private void clearSources()
    {
        for (DragAndDrop.Source source : sources)
        {
            dropInto.removeSource(source);
        }

        sources.clear();
    }

    private void fillTags()
    {
        BrainOutClient.ContentMgr.queryContentGen(Active.class,
            (sprite) ->
        {
            if (!sprite.hasComponent(SpriteWithBlocksComponent.class))
                return;

            CategoryComponent c = sprite.getComponent(CategoryComponent.class);

            if (c != null)
            {
                tags.put(c.getCategoryName(), c.getCategoryIcon());
            }
        });
    }

    private void renderPanel(Table panel, Menu menu)
    {
        {
            Table tags = new Table(BrainOutClient.Skin);
            tags.setBackground("buttons-group");
            tags.align(Align.top | Align.center);
            tags.setTouchable(Touchable.childrenOnly);

            renderTags(tags, menu);

            panel.add(tags).expandY().top().pad(4);
        }
        {
            Table holder = new Table(BrainOutClient.Skin);
            holder.setBackground("form-black");

            objectsTable = new Table(BrainOutClient.Skin);
            objectsTable.align(Align.top | Align.left);

            renderObjects();

            pane = new ScrollPane(objectsTable, BrainOutClient.Skin, "scroll-default");
            pane.setFadeScrollBars(false);
            pane.setScrollingDisabled(true, false);

            holder.add(pane).expand().fill().pad(2);
            panel.add(holder).expand().fill().row();
        }
    }

    private void renderTags(Table tags, Menu menu)
    {
        tags.clearChildren();

        ButtonGroup<Button> buttons = new ButtonGroup<>();
        buttons.setMaxCheckCount(1);
        buttons.setMinCheckCount(1);

        for (ObjectMap.Entry<String, String> entry : this.tags)
        {
            String tagName = entry.key;
            String tagIcon = entry.value;

            Button button = new Button(BrainOutClient.Skin, "button-checkable");

            Image img = new Image(BrainOutClient.Skin, tagIcon);
            img.setTouchable(Touchable.disabled);
            img.setScaling(Scaling.none);
            button.add(img);

            if (tagName.equals(currentTag))
                button.setChecked(true);

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.select);
                    selectTag(tagName);
                }
            });

            Tooltip.RegisterToolTip(button, L.get(tagName), menu);

            tags.add(button).size(32, 32).padBottom(2).row();
            buttons.add(button);
        }
    }

    private void selectTag(String tag)
    {
        currentTag = tag;
        renderObjects();
    }

    private void fail()
    {
        Menu.playSound(Menu.MenuSound.denied);
    }

    public void refresh()
    {
        renderObjects();
    }

    private void renderObjects()
    {
        clearSources();
        objectsTable.clearChildren();

        if (currentTag != null)
        {
            Active ext = BrainOutClient.ContentMgr.queryOneContentTpl(Active.class, check ->
            {
                if (!check.hasComponent(UserSpriteWithBlocksComponent.class))
                    return false;

                CategoryComponent categoryComponent = check.getComponent(CategoryComponent.class);

                if (categoryComponent == null)
                    return false;

                return currentTag.equals(categoryComponent.getCategoryName());
            });

            if (ext != null)
            {
                renderExtImages(ext);
            }
        }

        Map map = Map.Get(menu.getDimension());

        Array<Content> items = BrainOutClient.ContentMgr.queryContent(Active.class, sprite ->
        {
            SpriteWithBlocksComponent b = sprite.getComponent(SpriteWithBlocksComponent.class);

            if (b == null)
                return false;

            if (b.getLimitAmount() != 0)
            {
                if (map.countActives(activeData -> activeData.getCreator() != null && activeData.getCreator().getID().equals(sprite.getID()))
                    >= b.getLimitAmount())
                {
                    return false;
                }
            }

            return currentTag == null
                || currentTag.equals(sprite.getComponent(CategoryComponent.class).getCategoryName());
        });

        items.sort(Comparator.comparing(Content::getID));

        for (Content spriteContent : items)
        {
            SpriteWithBlocksComponent sprite = spriteContent.getComponent(SpriteWithBlocksComponent.class);

            if (sprite == null)
                continue;

            Button button = new Button(BrainOutClient.Skin, "button-hoverable-clear");

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.select);

                    /*
                    SpritesWidget.this.exit(() ->
                        callback.selected(sprite));
                    */
                }
            });

            IconComponent iconComponent = spriteContent.getComponent(IconComponent.class);

            if (iconComponent != null)
            {
                Image icon = new Image(BrainOutClient.Skin, iconComponent.getIconName("icon", null));
                button.add(icon);
            }
            else
            {
                Group entry = renderSprite(sprite);

                button.add(entry).size(sprite.getWidth() * Constants.Graphics.BLOCK_SIZE,
                        sprite.getHeight() * Constants.Graphics.BLOCK_SIZE);
            }

            DragAndDrop.Source source = new DragAndDrop.Source(button)
            {
                @Override
                public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target)
                {
                    if (target == null)
                    {
                        fail();
                    }
                }

                @Override
                public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer)
                {
                    if (menu.getScale() != 1.0f)
                    {
                        return null;
                    }

                    Group object = new Group();
                    object.setTouchable(Touchable.disabled);
                    Group invalid = new Group();
                    invalid.setTouchable(Touchable.disabled);
                    Group valid = new Group();
                    valid.setTouchable(Touchable.disabled);


                    float offsetX = - sprite.getWidth() * Constants.Graphics.BLOCK_SIZE * 0.5f + Constants.Graphics.BLOCK_SIZE * 0.5f,
                          offsetY = - sprite.getHeight() * Constants.Graphics.BLOCK_SIZE * 0.5f + Constants.Graphics.BLOCK_SIZE * 0.5f;

                    SpritePayload payload = new SpritePayload(offsetX, offsetY);

                    {
                        Group entry = renderSprite(sprite);
                        entry.setTouchable(Touchable.disabled);
                        entry.setPosition(offsetX, offsetY);
                        object.addActor(entry);
                    }
                    {
                        Image border = new Image(BrainOutClient.Skin, "form-drag-good");

                        border.setBounds(
                            offsetX - 2,
                            offsetY - 2,
                            sprite.getWidth() * Constants.Graphics.BLOCK_SIZE + 4,
                            sprite.getHeight() * Constants.Graphics.BLOCK_SIZE + 4
                        );

                        valid.addActor(border);

                        Group entry = renderSprite(sprite);
                        entry.setTouchable(Touchable.disabled);
                        entry.setPosition(offsetX, offsetY);
                        valid.addActor(entry);

                        payload.addOffsetCallback((x1, y1) ->
                        {
                            border.setPosition(offsetX - 2 + x1, offsetY - 2 + y1);
                            entry.setPosition(offsetX + x1, offsetY + y1);
                        });
                    }
                    {
                        Image border = new Image(BrainOutClient.Skin, "form-border-red");

                        border.setBounds(
                            offsetX - 2,
                            offsetY - 2,
                            sprite.getWidth() * Constants.Graphics.BLOCK_SIZE + 4,
                            sprite.getHeight() * Constants.Graphics.BLOCK_SIZE + 4
                        );

                        invalid.addActor(border);

                        Group entry = renderSprite(sprite);
                        entry.setTouchable(Touchable.disabled);
                        entry.setPosition(offsetX, offsetY);
                        invalid.addActor(entry);

                        payload.addOffsetCallback((x1, y1) ->
                        {
                            border.setPosition(offsetX - 2 + x1, offsetY - 2 + y1);
                            entry.setPosition(offsetX + x1, offsetY + y1);
                        });
                    }

                    payload.setDragActor(object);
                    payload.setObject(sprite);
                    payload.setValidDragActor(valid);
                    payload.setInvalidDragActor(invalid);

                    return payload;
                }
            };

            CategoryComponent cc = spriteContent.getComponent(CategoryComponent.class);

            boolean tooltip;

            if (cc != null)
            {
                tooltip = !spriteContent.getTitle().getID().equals(cc.getCategoryName());
            }
            else
            {
                tooltip = false;
            }

            if (tooltip)
            {
                Tooltip.RegisterToolTip(button, spriteContent.getTitle().get(), menu);
            }

            dropInto.addSource(source);

            objectsTable.add(button).expandX().fillX().pad(2).uniformY().fillY().row();
        }
    }

    private void renderExtImages(Active ext)
    {
        UserSpriteWithBlocksComponent us = ext.getComponent(UserSpriteWithBlocksComponent.class);

        if (us == null)
            return;

        {
            Button addNewImage = new Button(BrainOutClient.Skin, "button-notext");

            Image add = new Image(BrainOutClient.Skin, "icon-add");
            add.setTouchable(Touchable.disabled);
            addNewImage.add(add);

            addNewImage.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.select);
                    menu.pushMenu(new UploadCustomImageMenu(ext));
                }
            });

            objectsTable.add(addNewImage).expandX().center().size(128, 128).row();
        }

        for (ObjectMap.Entry<String, TextureRegion> entry_ : BrainOutClient.Skin.getAll(TextureRegion.class))
        {
            final String regionName = entry_.key;

            if (!regionName.startsWith(us.getExtName()))
                continue;

            final TextureRegion region = entry_.value;

            Button button = new Button(BrainOutClient.Skin, "button-hoverable-clear");

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(Menu.MenuSound.select);

                    menu.pushMenu(new UploadCustomImageMenu(ext, regionName));
                }
            });

            Group entry = renderExt(region);
            button.add(entry).size(region.getRegionWidth(), region.getRegionHeight());

            DragAndDrop.Source source = new DragAndDrop.Source(button)
            {
                @Override
                public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target)
                {
                    if (target == null)
                    {
                        fail();
                    }
                }

                @Override
                public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer)
                {
                    if (menu.getScale() != 1.0f)
                    {
                        return null;
                    }

                    int widthPixels = region.getRegionWidth(),
                        heightPixels = region.getRegionHeight();

                    int width = MathUtils.ceil((float)widthPixels / (float)Constants.Graphics.BLOCK_SIZE),
                        height = MathUtils.ceil((float)heightPixels / (float)Constants.Graphics.BLOCK_SIZE);

                    int fixedWidthPixels = width * Constants.Graphics.BLOCK_SIZE,
                        fixedHeightPixels = height * Constants.Graphics.BLOCK_SIZE;

                    Group object = new Group();
                    object.setTouchable(Touchable.disabled);
                    Group invalid = new Group();
                    invalid.setTouchable(Touchable.disabled);
                    Group valid = new Group();
                    valid.setTouchable(Touchable.disabled);

                    float offsetX = - fixedWidthPixels * 0.5f + Constants.Graphics.BLOCK_SIZE * 0.5f,
                        offsetY = - fixedHeightPixels * 0.5f + Constants.Graphics.BLOCK_SIZE * 0.5f;

                    UserSpritePayload payload = new UserSpritePayload(offsetX, offsetY, width, height, regionName);

                    {
                        Group entry = renderExt(region);
                        entry.setTouchable(Touchable.disabled);
                        entry.setPosition(offsetX, offsetY);
                        object.addActor(entry);
                    }
                    {
                        Image border = new Image(BrainOutClient.Skin, "form-drag-good");

                        border.setBounds(
                            offsetX - 2,
                            offsetY - 2,
                            fixedWidthPixels + 4,
                            fixedHeightPixels + 4
                        );

                        valid.addActor(border);

                        Group entry = renderExt(region);
                        entry.setTouchable(Touchable.disabled);
                        entry.setPosition(offsetX, offsetY);
                        valid.addActor(entry);

                        payload.addOffsetCallback((x1, y1) ->
                        {
                            border.setPosition(offsetX - 2 + x1, offsetY - 2 + y1);
                            entry.setPosition(offsetX + x1, offsetY + y1);
                        });
                    }
                    {
                        Image border = new Image(BrainOutClient.Skin, "form-border-red");

                        border.setBounds(
                            offsetX - 2,
                            offsetY - 2,
                            fixedWidthPixels + 4,
                            fixedHeightPixels + 4
                        );

                        invalid.addActor(border);

                        Group entry = renderExt(region);
                        entry.setTouchable(Touchable.disabled);
                        entry.setPosition(offsetX, offsetY);
                        invalid.addActor(entry);

                        payload.addOffsetCallback((x1, y1) ->
                        {
                            border.setPosition(offsetX - 2 + x1, offsetY - 2 + y1);
                            entry.setPosition(offsetX + x1, offsetY + y1);
                        });
                    }

                    payload.setDragActor(object);
                    payload.setObject(us);
                    payload.setValidDragActor(valid);
                    payload.setInvalidDragActor(invalid);

                    return payload;
                }
            };

            dropInto.addSource(source);

            objectsTable.add(button).expandX().fillX().pad(2).uniformY().fillY().row();
        }
    }

    private Group renderExt(TextureRegion region)
    {
        Group entry = new Group();

        Image image = new Image(new TextureRegionDrawable(region));

        image.setTouchable(Touchable.disabled);

        float x = (region.getRegionWidth() % Constants.Graphics.BLOCK_SIZE) / 2f,
            y = (region.getRegionHeight() % Constants.Graphics.BLOCK_SIZE) / 2f;

        image.setBounds(x, y, region.getRegionWidth(), region.getRegionHeight());
        entry.addActor(image);

        return entry;
    }

    private Group renderSprite(SpriteWithBlocksComponent sprite)
    {
        Group entry = new Group();

        for (SpriteWithBlocksComponent.SpriteImage spriteImage : sprite.getImages())
        {
            Image image = new Image(BrainOutClient.Skin, spriteImage.getImage());
            image.setTouchable(Touchable.disabled);
            image.setBounds(
                    spriteImage.getX() * Constants.Graphics.BLOCK_SIZE,
                    spriteImage.getY() * Constants.Graphics.BLOCK_SIZE,
                    spriteImage.getW() * Constants.Graphics.BLOCK_SIZE,
                    spriteImage.getH() * Constants.Graphics.BLOCK_SIZE);
            entry.addActor(image);
        }

        return entry;
    }

    @Override
    public boolean remove()
    {
        clearSources();

        return super.remove();
    }

    public void exit(Runnable done)
    {
        panel.clearActions();
        panel.addAction(Actions.sequence(
            Actions.moveTo(TOTAL_WIDTH, 0, 0.25f, Interpolation.pow2In),
            Actions.run(done)
        ));
    }

    public ScrollPane getPane()
    {
        return pane;
    }
}
