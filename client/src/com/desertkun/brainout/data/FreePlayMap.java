package com.desertkun.brainout.data;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.ClientHighlightComponent;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Animation;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.mode.GameModeFree;
import com.desertkun.brainout.utils.RealEstateInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class FreePlayMap extends ClientMap
{
    private boolean dayLight;
    private RealEstateInfo realEstateInfo = null;
    private ClientHighlightComponent highlightComponent;

    public RealEstateInfo getRealEstateInfo()
    {
        return realEstateInfo;
    }

    public FreePlayMap(String dimension)
    {
        super(dimension);
        dayLight();
    }

    private void dayLight()
    {
        this.dayLight = dimension.equals("intro") || dimension.equals("default") || dimension.equals("forest") || dimension.equals("swamp2");
    }

    public FreePlayMap(String dimension, int width, int height)
    {
        super(dimension, width, height);
        dayLight();
    }

    @Override
    public void update(float dt)
    {
        if (lights != null && dayLight)
        {
            updateDayLight();
        }

        super.update(dt);
    }

    @Override
    public void init()
    {
        super.init();

        highlightComponent = new ClientHighlightComponent();
        getComponents().addComponent(highlightComponent);
    }

    public ClientHighlightComponent getHighlightComponent()
    {
        return highlightComponent;
    }

    @Override
    public ActiveData newActiveData(Json json, JsonValue jsonValue)
    {
        if (!BrainOutClient.ClientSett.hasAnimatedTrees())
        {
            return super.newActiveData(json, jsonValue);
        }

        Active active = newT(jsonValue, Active.class);

        if (active == null)
            return null;

        if (!validateActive(active))
            return null;

        if (active.getID().equals("sprite"))
        {
            String sname = jsonValue.getString("sname", null);
            if (sname != null)
            {
                String anim = "active-" + sname;
                Animation animation = BrainOut.ContentMgr.get(anim, Animation.class);
                if (animation != null)
                {
                    ActiveData activeData = animation.getData(getDimension());
                    activeData.read(json, jsonValue);

                    /*
                    //TODO: made animated object scaling possible
                    float scale = 1.0f;
                    if (jsonValue.has("sscale"))
                    {
                        scale = jsonValue.getFloat("sscale");

                    AnimationComponentData animComp = activeData.getComponentWithSubclass(AnimationComponentData.class);
                    if (animComp != null)
                    {
                        animComp.getSkeleton().setScale(scale, scale);
                        animComp.getSkeleton().set
                    }
                     */

                    TextureRegion region = BrainOutClient.getRegion(sname);

                    if (region != null)
                    {
                        activeData.setX(activeData.getX() + region.getRegionWidth() / (Constants.Graphics.BLOCK_SIZE * 2.f));
                    }

                    return activeData;
                }
            }
        }

        ActiveData activeData = active.getData(getDimension());
        activeData.read(json, jsonValue);

        return activeData;
    }

    private void updateDayLight()
    {
        GameModeFree free = ((GameModeFree) BrainOutClient.ClientController.getGameMode());
        if (free == null)
            return;

        float offset = free.getTimeOfDay() + 0.1f; if (offset > 1) offset -= 1;
        float day = (0.5f - Math.abs(offset - 0.5f)) * 3.f; if (day > 1) day = 1;
        ambientLight.a = 0.05f + Interpolation.pow2.apply(day) * 0.95f;
        this.lights.setAmbientLight(ambientLight);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("rs"))
        {
            JsonValue rs = jsonData.get("rs");
            realEstateInfo = new RealEstateInfo();
            realEstateInfo.read(json, rs);
        }
    }
}
