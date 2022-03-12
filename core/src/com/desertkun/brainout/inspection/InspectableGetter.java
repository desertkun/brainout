package com.desertkun.brainout.inspection;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited

public @interface InspectableGetter
{
    public String name();
    public PropertyKind kind();
    public PropertyValue value();
}
