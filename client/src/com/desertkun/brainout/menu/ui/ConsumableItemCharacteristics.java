package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.*;
import com.desertkun.brainout.content.consumable.Armor;
import com.desertkun.brainout.content.consumable.impl.ArmorConsumableItem;
import com.desertkun.brainout.content.consumable.impl.RealEstateConsumableItem;
import com.desertkun.brainout.content.consumable.impl.RealEstateItemConsumableItem;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class ConsumableItemCharacteristics extends CharacteristicsPanel
{
    protected final boolean useless;
    protected final ConsumableRecord record;

    public ConsumableItemCharacteristics(boolean useless, ConsumableRecord record)
    {
        this.useless = useless;
        this.record = record;
    }

    public static boolean HasAnyCharacteristic(ConsumableRecord record)
    {
        if (record.getItem().getContent().hasComponent(AddWeightComponent.class))
        {
            return true;
        }

        if (record.getItem() instanceof ArmorConsumableItem)
        {
            return true;
        }

        if (record.getItem() instanceof RealEstateConsumableItem)
        {
            return true;
        }

        if (record.getItem().getContent().hasComponent(RecipeComponent.class))
        {
            return true;
        }

        BoosterActivatorComponent booster = record.getItem().getContent().getComponent(BoosterActivatorComponent.class);
        if (booster != null)
        {
            {
                float h_ = booster.getHealth(record.getQuality());
                if (h_ > 0)
                {
                    return true;
                }
            }
            {
                float h_ = booster.getHunger(record.getQuality());
                if (h_ > 0)
                {
                    return true;
                }
            }
            {
                float h_ = booster.getRadio(record.getQuality());
                if (h_ > 0)
                {
                    return true;
                }
            }
            {
                float t_ = booster.getThirst(record.getQuality());
                if (t_ > 0)
                {
                    return true;
                }
            }
            {
                float t_ = booster.getTemp(record.getQuality());
                if (t_ > 0)
                {
                    return true;
                }
            }

            return booster.getBoosters().size > 0;
        }

        return false;
    }

    public void initChars()
    {
        CampFireFuelComponent cf = record.getItem().getContent().getComponent(CampFireFuelComponent.class);
        if (cf != null)
        {
            add("item-char-burning-time", "MENU_BURNING_TIME", () -> cf.getDuration(record.getQuality()), new SimpleView("MENU_SECONDS"));
        }

        AddWeightComponent aw = record.getItem().getContent().getComponent(AddWeightComponent.class);
        if (aw != null)
        {
            add("item-char-weight", "MENU_WEIGHT_BOOST", () -> aw.getWeight(record.getQuality()), new SimpleView());
        }

        if (record.getItem() instanceof RealEstateConsumableItem)
        {
            RealEstateConsumableItem rci = ((RealEstateConsumableItem) record.getItem());

            add("item-char-location", "MENU_REAL_ESTATE_LOCATION", () -> 0, new TextView(
                L.get("REAL_ESTATE_LOCATION_" + rci.getLocation())));

            String code = rci.getId();
            if (code.startsWith("A"))
            {
                code = code.substring(1);
            }
            while (code.length() < 3)
            {
                code = "0" + code;
            }
            String fcode = code;

            add("item-char-id", "MENU_REAL_ESTATE_UNIT", () -> 0, new TextView(rci.getId()));
            add("item-char-id", "MENU_REAL_ESTATE_CODE", () -> 0, new TextView(fcode));
            add("item-char-rooms", "MENU_REAL_ESTATE_ROOMS", () -> 0, new TextView(rci.getContent().getRooms()));
            add("item-char-rooms", "MENU_REAL_ESTATE_VARIANT", () -> 0, new TextView(rci.getContent().getVariant()));
        }

        if (record.getItem() instanceof RealEstateItemConsumableItem)
        {
            RealEstateItemConsumableItem rcii = ((RealEstateItemConsumableItem) record.getItem());
            SpriteWithBlocksComponent cc = rcii.getContent().getComponent(SpriteWithBlocksComponent.class);
            if (cc != null)
            {
                add(null, null, () -> 0, new View()
                {
                    @Override
                    public void render(Data from, Table to)
                    {
                        Group g = new Group();
                        g.setSize(cc.getWidth() * Constants.Graphics.BLOCK_SIZE,
                                cc.getHeight() * Constants.Graphics.BLOCK_SIZE);

                        for (SpriteWithBlocksComponent.SpriteImage image : cc.getImages())
                        {
                            Image im = new Image(BrainOutClient.Skin, image.getImage());
                            im.setBounds(image.getX() * Constants.Graphics.BLOCK_SIZE,
                                    image.getY() * Constants.Graphics.BLOCK_SIZE,
                                    image.getW() * Constants.Graphics.BLOCK_SIZE,
                                    image.getH() * Constants.Graphics.BLOCK_SIZE);
                            im.setScaling(Scaling.fill);
                            im.setTouchable(Touchable.disabled);

                            g.addActor(im);
                        }

                        to.add(g).size(cc.getWidth() * Constants.Graphics.BLOCK_SIZE,
                            cc.getHeight() * Constants.Graphics.BLOCK_SIZE);
                    }

                    @Override
                    public boolean hasLabel()
                    {
                        return false;
                    }

                    @Override
                    public float getExpectedHeight()
                    {
                        return cc.getHeight() * Constants.Graphics.BLOCK_SIZE + 24;
                    }
                });
            }
        }

        if (record.getItem() instanceof ArmorConsumableItem)
        {
            ArmorConsumableItem armor = ((ArmorConsumableItem) record.getItem());

            float head = armor.getProtect("head", record.getQuality());

            if (head > 0)
            {
                add("item-char-protection", "MENU_ARMOR_HEAD", () -> head, new SimpleView());
            }

            float body = armor.getProtect("body", record.getQuality());

            if (body > 0)
            {
                add("item-char-protection", "MENU_ARMOR_BODY", () -> body, new SimpleView());
            }
        }

        BoosterActivatorComponent booster = record.getItem().getContent().getComponent(BoosterActivatorComponent.class);
        if (booster != null)
        {
            {
                float h_ = booster.getHealth(record.getQuality());
                if (h_ > 0)
                {
                    add("item-char-health", "MENU_HEALTH", () -> h_, new SimpleView(true));
                }
            }
            {
                float h_ = booster.getHunger(record.getQuality());
                if (h_ > 0)
                {
                    add("item-char-hunger", "MENU_FOOD", () -> h_, new SimpleView(true));
                }
            }
            {
                float h_ = booster.getRadio(record.getQuality());
                if (h_ > 0)
                {
                    add("item-char-radx", "MENU_RADX", () -> h_, new SimpleView(true));
                }
            }
            {
                float t_ = booster.getThirst(record.getQuality());
                if (t_ > 0)
                {
                    add("item-char-thirst", "MENU_HYDRATION", () -> t_, new SimpleView(true));
                }
            }
            {
                float t_ = booster.getTemp(record.getQuality());
                if (t_ > 0)
                {
                    add("item-char-temp", "MENU_TEMPERATURE", () -> t_, new SimpleView(true));
                }
            }

            for (ObjectMap.Entry<String, BoosterActivatorComponent.BoosterActivator> entry : booster.getBoosters())
            {
                if (entry.key.equals("speed"))
                {
                    float t_ = booster.getBoosterDuration(entry.value.duration, record.getQuality());
                    add("item-char-speed", "MENU_SPEED_BOOST", () -> t_, new SimpleView("MENU_SECONDS"));
                }
            }
        }
    }
}
