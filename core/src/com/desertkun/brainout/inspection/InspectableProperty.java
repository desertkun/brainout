package com.desertkun.brainout.inspection;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface InspectableProperty
{
    public String name();
    public PropertyKind kind();
    public PropertyValue value();
    public String className() default "";
}
