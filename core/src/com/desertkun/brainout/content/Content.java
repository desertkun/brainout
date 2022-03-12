package com.desertkun.brainout.content;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.shop.ShopCart;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.utils.LocalizedString;
import com.desertkun.brainout.packages.ContentPackage;
import com.esotericsoftware.minlog.Log;


import java.util.Observable;

public abstract class Content implements Disposable, Json.Serializable
{
	protected String id;
    protected LocalizedString name;
    protected LocalizedString description;
    private ContentPackage contentPackage;
    private String tag;

    protected Array<ContentComponent> components;
    private ObjectMap<Class<? extends ContentComponent>, ContentComponent> componentsIndex;

    public String getID() { return id; }
	
	public LocalizedString getTitle() { return name; }
	public LocalizedString getDescription() { return description; }
    public void setID(String id) { this.id = id; }

    @Override
    public String toString()
    {
        return getID();
    }

    public String getTag()
    {
        return tag;
    }

    @Override
	public void write(Json json)
    {
        if (getTitle().isValid())
        {
            json.writeValue("name", name.getID());
        }

        if (getDescription().isValid())
        {
            json.writeValue("description", description.getID());
        }
		
		json.writeValue("class", BrainOut.R.getClassName(getClass()));
	}

    public Content()
    {
        id = null;
        name = new LocalizedString();
        description = new LocalizedString();
        components = null;
        contentPackage = null;
    }

	@Override
	public void read(Json json, JsonValue jsonData)
    {
		name.set(json.readValue("name", String.class, jsonData));
		description.set(json.readValue("description", String.class, jsonData));

        if (jsonData.has("tag"))
        {
            String tag = jsonData.getString("tag");
            if (BrainOut.getInstance().hasTag(tag))
            {
                this.tag = tag;
            }
        }

        if (jsonData.has("components"))
        {
            JsonValue componentsValue = jsonData.get("components");
            if (componentsValue.isArray())
            {
                if (components == null)
                {
                    components = new Array<>();
                }

                if (componentsIndex == null)
                {
                    componentsIndex = new ObjectMap<>();
                }

                for (JsonValue componentValue: componentsValue)
                {
                    if (componentValue.has("tag"))
                    {
                        if (!BrainOut.getInstance().hasTag(componentValue.getString("tag")))
                        {
                            continue;
                        }
                    }

                    JsonValue classValue = componentValue.get("class");
                    if (classValue != null && classValue.isString())
                    {
                        String className = classValue.asString();

                        try
                        {
                            Class<? extends ContentComponent> componentClass = BrainOut.R.forName(className);

                            final ContentComponent contentComponent;

                            if (hasComponent(componentClass))
                            {
                                contentComponent = getComponent(componentClass);
                            }
                            else
                            {
                                contentComponent = ((ContentComponent) BrainOut.R.newInstance(className));
                                addComponent(contentComponent);
                            }

                            contentComponent.read(json, componentValue);
                        }
                        catch (Exception e)
                        {
                            if (Log.ERROR) Log.error("Content: " + BrainOut.R.getClassName(getContentClass()));
                            e.printStackTrace();
                        }
                    }
                }
            }

            for (ContentComponent contentComponent: components)
            {
                contentComponent.setContent(this);
            }
        }
	}

    private void addComponent(ContentComponent component)
    {
        components.add(component);
        componentsIndex.put(component.getContentClass(), component);
    }

    @SuppressWarnings("unchecked")
    public <T extends ContentComponent> T getComponent(Class<T> classOf)
    {
        return componentsIndex != null ? (T)componentsIndex.get(classOf) : null;
    }

    @SuppressWarnings("unchecked")
    public <T extends ContentComponent> T getComponentFrom(Class<T> classOf)
    {
        if (components == null)
            return null;

        for (ContentComponent component : components)
        {
            if (BrainOut.R.instanceOf(classOf, component))
            {
                return (T)component;
            }
        }

        return null;
    }

    public boolean hasComponent(Class<? extends ContentComponent> clazz)
    {
        return componentsIndex != null && componentsIndex.get(clazz) != null;

    }

    public void initComponentObject(ComponentObject componentObject)
    {
        if (components == null) return;

        for (ContentComponent contentComponent: components)
        {
            Component component = contentComponent.getComponent(componentObject);
            if (component != null)
            {
                componentObject.addComponent(component);
            }
        }
    }

    public void loadContent(AssetManager assetManager)
    {
        if (components == null) return;

        for (ContentComponent contentComponent: components)
        {
            contentComponent.loadContent(assetManager);
        }
    }

    public void completeLoad(AssetManager assetManager)
    {
        if (components == null) return;

        for (ContentComponent contentComponent: components)
        {
            contentComponent.completeLoad(assetManager);
        }

        if (needLocalizationCheck())
        {
            if (getTitle().isVanilla() && !getTitle().isValid())
            {
                System.err.println("Error: No localization for content " +
                        getID() + " title: " + getTitle().getID());
            }

            if (getDescription().isVanilla() && !getDescription().isValid())
            {
                System.err.println("Error: No localization for content " +
                        getID() + " description: " + getTitle().getID());
            }
        }
    }

    protected boolean needLocalizationCheck()
    {
        return !BrainOut.getInstance().getController().isServer();
    }

    public ContentPackage getPackage()
    {
        return contentPackage;
    }

    public void setPackage(ContentPackage contentPackage)
    {
        this.contentPackage = contentPackage;
    }

    @Override
    public void dispose() {}

    @SuppressWarnings("unchecked")
    public Class<? extends Content> getContentClass()
    {
        return (Class<? extends Content>)((Object)this).getClass();
    }

    public boolean isEditorSelectable()
    {
        return false;
    }

    public Array<ContentComponent> getComponents()
    {
        return components;
    }
}
