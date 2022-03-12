package com.desertkun.brainout.inspection.props;

import com.desertkun.brainout.inspection.Inspectable;

public abstract class PropertiesRegistration
{
    public PropertiesRegistration()
    {
    }

    public void act(Inspectable inspectable)
    {
        inspectable.inspect(this);
        doInspect(inspectable);
    }

    public void inspectChild(Inspectable inspectable)
    {
        inspectable.inspect(this);
        doInspect(inspectable);
    }

    protected abstract void doInspect(Inspectable inspectable);
}