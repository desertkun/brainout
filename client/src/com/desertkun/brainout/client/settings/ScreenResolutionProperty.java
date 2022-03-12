package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScreenResolutionProperty extends Property<ScreenResolutionProperty> implements Property.SelectProperty
{
    private int width;
    private int height;
    private int bpp;
    private int hz;

    private static Pattern RESOLUTION_PATTERN = Pattern.compile("([0-9]+)x([0-9]+)/([0-9]+)bit@([0-9]+)hz");

    public ScreenResolutionProperty(String name, String localization, Graphics.DisplayMode defaultDisplayMode)
    {
        super(name, localization);

        setWidth(defaultDisplayMode.width);
        setHeight(defaultDisplayMode.height);
        setBpp(defaultDisplayMode.bitsPerPixel);
        setHz(defaultDisplayMode.refreshRate);
    }

    public ScreenResolutionProperty(String name, String localization,
                                    Graphics.DisplayMode defaultDisplayMode, Properties properties)
    {
        this(name, localization, defaultDisplayMode);

        properties.addProperty(this);
    }

    public void setResolution(int width, int height, int bpp, int hz)
    {
        setWidth(width);
        setHeight(height);
        setBpp(bpp);
        setHz(hz);

        update();
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public void setBpp(int bpp)
    {
        this.bpp = bpp;
    }

    public void setHz(int hz)
    {
        this.hz = hz;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getBpp()
    {
        return bpp;
    }

    public int getHz()
    {
        return hz;
    }

    @Override
    public void write(Json json)
    {
        json.writeObjectStart(getName());

        json.writeValue("width", width);
        json.writeValue("height", height);
        json.writeValue("bpp", bpp);
        json.writeValue("hz", hz);

        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        JsonValue obj = jsonData.get(getName());

        if (obj != null)
        {
            setWidth(obj.getInt("width", getWidth()));
            setHeight(obj.getInt("height", getHeight()));
            setBpp(obj.getInt("bpp", getBpp()));
            setHz(obj.getInt("hz", getHz()));
        }
    }

    public Graphics.DisplayMode find()
    {
        if (width == 0 || height == 0)
            return BrainOutClient.ClientSett.getDefaultDisplayMode();

        Graphics.DisplayMode[] modes = BrainOutClient.ClientSett.getDisplayModes();

        for (Graphics.DisplayMode mode: modes)
        {
            if (mode.width == width && mode.height == height && mode.bitsPerPixel == bpp && mode.refreshRate == hz)
            {
                return mode;
            }
        }

        return BrainOutClient.ClientSett.getDefaultDisplayMode();
    }


    @Override
    public void getOptions(ObjectMap<String, String> values)
    {
        for (Graphics.DisplayMode mode: BrainOutClient.ClientSett.getDisplayModes())
        {
            if (mode.width < ClientConstants.ScreenResolution.MIN_X)
                continue;

            if (mode.height < ClientConstants.ScreenResolution.MIN_Y)
                continue;

            String resolution = String.valueOf(mode.width) + "x" + mode.height +
                "/" + mode.bitsPerPixel + "bit@" + mode.refreshRate + "hz";

            if (!values.containsKey(resolution))
            {
                values.put(resolution, resolution);
            }
        }
    }

    @Override
    public String getSelectValue()
    {
        return String.valueOf(width) + "x" + height + "/" + bpp + "bit@" + hz + "hz";
    }

    @Override
    public boolean selectValue(String value)
    {
        Matcher m = RESOLUTION_PATTERN.matcher(value);

        if (m.matches())
        {
            setResolution(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)),
                Integer.valueOf(m.group(3)), Integer.valueOf(m.group(4)));
        }

        return update();
    }

    @Override
    public boolean update()
    {
        return true;
    }
}
