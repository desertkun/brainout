package com.desertkun.brainout.client.settings;

import com.desertkun.brainout.L;

public class IntegerRangeProperty extends IntegerProperty implements Property.TrackbarProperty
{
    private int min;
    private int max;

    public IntegerRangeProperty(String name, String localization, Integer def, Integer min, Integer max)
    {
        super(name, localization, def);

        setMax(max);
        setMin(min);
    }

    public IntegerRangeProperty(String name, String localization, Integer def, Integer min, Integer max, Properties properties)
    {
        super(name, localization, def, properties);

        setMax(max);
        setMin(min);
    }

    public float getFloatValue()
    {
        int diff = getMax() - getMin();

        return (float)(getValue() - getMin()) / (float)diff;
    }

    public void setMax(int max)
    {
        this.max = max;
    }

    public void setMin(int min)
    {
        this.min = min;
    }

    public int getMin()
    {
        return min;
    }

    public int getMax()
    {
        return max;
    }

    @Override
    public void update() {}
}
