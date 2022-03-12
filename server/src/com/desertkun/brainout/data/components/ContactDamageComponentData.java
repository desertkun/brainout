package com.desertkun.brainout.data.components;

import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.ContactDamageComponent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.DamageEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.PhysicsContactEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ContactDamageComponent")
@ReflectAlias("data.components.ContactDamageComponentData")
public class ContactDamageComponentData extends Component<ContactDamageComponent>
{
    private HealthComponentData health;

    public ContactDamageComponentData(ComponentObject componentObject,
                                      ContactDamageComponent content)
    {
        super(componentObject, content);
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public void init()
    {
        super.init();

        health = getComponentObject().getComponent(HealthComponentData.class);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case physicsContact:
            {
                PhysicsContactEvent e = ((PhysicsContactEvent) event);

                contact(e);

                break;
            }
        }

        return false;
    }

    private void contact(PhysicsContactEvent e)
    {
        if (health == null)
            return;

        float speed = e.speed.len();
        float coef = getContentComponent().getSpeed().getRangeCoef(speed);

        if (coef > 0)
        {
            float damage = getContentComponent().getDamage().getValue(coef);
            ActiveData activeData = e.activeData;

            health.damage((DamageEvent)DamageEvent.obtain(damage,
                activeData.getOwnerId(), null, null,
                activeData.getX(), activeData.getY(), e.speed.angleDeg(),
                Constants.Damage.DAMAGE_FRACTURE));
        }
    }
}
