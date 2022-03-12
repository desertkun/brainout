package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.RecipeComponent;
import com.desertkun.brainout.content.consumable.Resource;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class RecipeCharacteristics extends ConsumableItemCharacteristics
{
    private final ObjectMap<Resource, Integer> resources;

    public RecipeCharacteristics(boolean useless, ConsumableRecord record, ObjectMap<Resource, Integer> resources)
    {
        super(useless, record);

        this.resources = resources;
    }

    @Override
    public void initChars()
    {
        super.initChars();

        RecipeComponent recipeComponent = record.getItem().getContent().getComponent(RecipeComponent.class);

        if (recipeComponent != null)
        {
            if (recipeComponent.getRequiredStat() != null)
            {
                if (BrainOutClient.ClientController.getUserProfile().getInt(recipeComponent.getRequiredStat(), 0 ) <= 0)
                {
                    Table locked = new Table();
                    locked.add(new Label(L.get("MENU_LOCKED_BLUEPRINT"), BrainOutClient.Skin, "title-small")).padTop(16).row();

                    Image image = new Image(BrainOutClient.Skin, "icon-blueprint-rsitem");
                    image.setScaling(Scaling.none);
                    locked.add(image).size(64).row();

                    addExtraHeight(112);
                    add(locked).expandX().fill().row();

                    return;
                }
            }

            RecipeView recipeView = new RecipeView(recipeComponent, resources);
            addExtraHeight(recipeView.render());
            add(recipeView).expandX().fill().row();
        }
    }

    private static class RecipeView extends Table
    {
        private final RecipeComponent recipeComponent;
        private final ObjectMap<Resource, Integer> resources;

        public RecipeView(RecipeComponent recipeComponent, ObjectMap<Resource, Integer> resources)
        {
            this.recipeComponent = recipeComponent;
            this.resources = resources;
        }

        float render()
        {
            float res = 0;

            Label requiredToAssemble = new Label(L.get("MENU_REQUIRED_TO_ASSEMBLE"), BrainOutClient.Skin, "title-gray");
            requiredToAssemble.setAlignment(Align.center);
            add(requiredToAssemble).padTop(16).expandX().fillX().row();

            Table quantities = new Table();
            add(quantities).expandX().fillX().row();

            for (ObjectMap.Entry<Resource, Integer> entry : recipeComponent.getRequiredItems())
            {
                IconComponent iconComponent = entry.key.getComponent(IconComponent.class);
                if (iconComponent == null)
                    continue;

                TextureAtlas.AtlasRegion region = iconComponent.getIcon("icon-medium");

                if (region != null)
                {
                    Image image = new Image(region);
                    image.setScaling(Scaling.none);
                    quantities.add(image).size(32).padRight(8);
                }

                Label title = new Label(entry.key.getTitle().get(), BrainOutClient.Skin, "title-small");
                quantities.add(title).expandX().fillX();

                Label amounts = new Label(String.valueOf(entry.value), BrainOutClient.Skin,
                    resources.get(entry.key, 0) >= entry.value ? "title-green" : "title-gray");

                quantities.add(amounts).padLeft(8).row();

                res += 42;
            }

            return res;
        }
    }
}
