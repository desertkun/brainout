package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.FreeplayPlayerComponent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.ComponentUpdatedEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("freepc")
@ReflectAlias("data.components.FreeplayPlayerComponentData")
public class FreeplayPlayerComponentData
    extends Component<FreeplayPlayerComponent>
    implements Json.Serializable, ComponentUpdatedEvent.Predicate
{
    private float hunger;
    private float temperature;
    private float thirst;
    private float radio;
    private boolean bones;
    private boolean bleeding;
    private boolean swamp;

    public FreeplayPlayerComponentData(PlayerData componentObject,
                                       FreeplayPlayerComponent contentComponent)
    {
        super(componentObject, contentComponent);

        this.thirst = contentComponent.getThirstMax();
        this.temperature = contentComponent.getTemperatureMax();
        this.hunger = contentComponent.getHungerMax();
        this.bones = false;
        this.swamp = false;
        this.bleeding = false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
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
    public void write(Json json)
    {
        json.writeValue("th", (float)(int)thirst);
        json.writeValue("hg", (float)(int)hunger);
        json.writeValue("bn", bones);
        json.writeValue("sw", swamp);
        json.writeValue("bl", bleeding);
        json.writeValue("rd", (float)(int)radio);
        json.writeValue("tm", temperature);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        thirst = jsonData.getFloat("th", thirst);
        hunger = jsonData.getFloat("hg", hunger);
        bones = jsonData.getBoolean("bn", bones);
        swamp = jsonData.getBoolean("sw", swamp);
        bleeding = jsonData.getBoolean("bl", bleeding);
        radio = jsonData.getFloat("rd", radio);
        temperature = jsonData.getFloat("tm", temperature);
    }

    public float getHunger()
    {
        return hunger;
    }

    public float getThirst()
    {
        return thirst;
    }

    public boolean hasBonesBroken()
    {
        return bones;
    }

    public boolean isSwamp()
    {
        return swamp;
    }

    public boolean isBleeding()
    {
        return bleeding;
    }

    public void setBleeding(boolean bleeding)
    {
        this.bleeding = bleeding;
    }

    public float calculateSpeedCoefficient()
    {
        float coef = 1.0f;

        if (isThirsty())
        {
            coef *= 0.8f;
        }

        if (hasBonesBroken())
        {
            coef *= 0.8f;
        }

        return coef;
    }

    public void setBonesBroken(boolean bones)
    {
        this.bones = bones;
    }

    public void setSwamp(boolean swamp)
    {
        this.swamp = swamp;
    }

    public void addRadio(float radio)
    {
        this.radio = Math.min(this.radio + radio, getContentComponent().getRadioMax());
    }

    public void removeRadio(float radio)
    {
        this.radio = Math.max(this.radio - radio, 0);
    }

    public void removeTemperature(float t)
    {
        this.temperature = Math.max(this.temperature - t, 0);
    }

    public boolean hasRadioWarning()
    {
        return this.radio >= getContentComponent().getRadioMax() * 0.1f;
    }

    public boolean hasRadioDanger()
    {
        return this.radio >= getContentComponent().getRadioMax() * 0.5f;
    }

    public boolean hasRadioMax()
    {
        return this.radio >= getContentComponent().getRadioMax();
    }

    public void refillHunger(float hunger)
    {
        this.hunger = Math.min(this.hunger + hunger, getContentComponent().getHungerMax());
    }

    public void consumeHunger(float hunger)
    {
        this.hunger = Math.max(this.hunger - hunger, 0);
    }

    public void refillThirst(float thirst)
    {
        this.thirst = Math.min(this.thirst + thirst, getContentComponent().getThirstMax());
    }

    public float getTemperature()
    {
        return temperature;
    }

    public boolean setTemperature(float temperature)
    {
        float tmp = Math.max(Math.min(temperature, getContentComponent().getTemperatureMax()), 0);
        boolean diff = tmp != this.temperature;
        this.temperature = tmp;
        return diff;
    }

    public void consumeThirst(float thirst)
    {
        this.thirst = Math.max(this.thirst - thirst, 0);
    }

    public boolean isThirstMax()
    {
        return this.thirst >= getContentComponent().getThirstMax() * 0.9f;
    }

    public boolean isTemperatureMax()
    {
        return this.temperature >= getContentComponent().getTemperatureMax() * 0.9f;
    }

    public boolean isHungerMax()
    {
        return this.hunger >= getContentComponent().getHungerMax() * 0.9f;
    }

    public void sync()
    {
        updated(((PlayerData) getComponentObject()), this);
    }

    @Override
    public boolean check(int owner)
    {
        return true;
    }

    public boolean isHungry()
    {
        return hunger <= 0;
    }

    public boolean isCold()
    {
        return temperature <= getContentComponent().getTemperatureMax() * 0.8f;
    }

    public float getTemperatureCoef()
    {
        return temperature / getContentComponent().getTemperatureMax();
    }

    public boolean isThirsty()
    {
         return thirst <= 0;
    }

    public float getRadio()
    {
        return radio;
    }

    public void fullRecover() {
        setBleeding(false);
        setBonesBroken(false);
        setSwamp(false);
        setTemperature(getContentComponent().getTemperatureMax());
        refillHunger(getContentComponent().getHungerMax());
        refillThirst(getContentComponent().getThirstMax());
        removeRadio(getContentComponent().getRadioMax());

        sync();
    }
}
