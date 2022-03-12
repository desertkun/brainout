package com.desertkun.brainout.editor.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.editor.EditorProperty;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.inspection.*;

import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public class InspectObjectMenu extends FormMenu
{
    private final Inspectable inspectable;
    private final Array<EditorProperty> properties;
    private final Save onSave;

    public interface Save
    {
        void save(Inspectable inspectable, Array<EditorProperty> properties);
    }

    public InspectObjectMenu(Inspectable inspectable, Array<EditorProperty> properties, Save onSave)
    {
        this.inspectable = inspectable;
        this.properties = properties;
        this.onSave = onSave;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Table createUI()
    {
        Table data = super.createUI();

        Label titleLabel = new Label(inspectable.toString(), BrainOutClient.Skin, "title-inspect");
        titleLabel.setAlignment(Align.center);

        data.add(new BorderActor(titleLabel, 530, "form-gray")).row();

        final Table propertiesData = new Table();

        ScrollPane propertiesPane = new ScrollPane(propertiesData);
        propertiesPane.setCancelTouchFocus(false);

        for (final EditorProperty property: properties)
        {
            final Label text = new Label(property.name + ":", BrainOutClient.Skin, "title-inspect");
            text.setAlignment(Align.right);

            propertiesData.add(text).expandX().fillX().padRight(4).width(200);

            Actor valueActor;

            if (property.kind == null) continue;

            switch (property.kind)
            {
                case select:
                {
                    final SelectBox<String> asSelectBox = new SelectBox<String>(BrainOutClient.Skin, "select-inspect");
                    valueActor = asSelectBox;

                    Array<String> items = new Array<String>();

                    switch (property.value)
                    {
                        case vContent:
                        {
                            Array<Content> contentArray = BrainOut.ContentMgr.queryContent(
                                BrainOut.R.forName(property.clazz)
                            );

                            items.add("");

                            for (Content content: contentArray)
                            {
                                items.add(content.getID());
                            }

                            items.sort();

                            break;
                        }
                        case vString:
                        {
                            Array<Content> contentArray = BrainOut.ContentMgr.queryContent(
                                BrainOut.R.forName(property.clazz)
                            );

                            items.add("");

                            for (Content content: contentArray)
                            {
                                items.add(content.getID());
                            }

                            items.sort();

                            break;
                        }
                        case vEnum:
                        {
                            Class<? extends Enum<?>> enumClass = ((Class<? extends Enum<?>>) BrainOut.R.forName(property.clazz));

                            for (Enum<?> en: enumClass.getEnumConstants())
                            {
                                items.add(en.toString());
                            }

                            break;
                        }
                    }
                    asSelectBox.setItems(items);

                    asSelectBox.setSelected(property.data);

                    asSelectBox.addListener(new ChangeListener()
                    {
                        @Override
                        public void changed(ChangeEvent event, Actor actor)
                        {
                            property.data = asSelectBox.getSelected();
                        }
                    });

                    break;
                }
                case textureRegion:
                {
                    TextButton asTextButton = new TextButton("[CHOOSE]", BrainOutClient.Skin, "button-inspect");

                    valueActor = asTextButton;

                    asTextButton.addListener(new ClickOverListener()
                    {
                        @Override
                        @SuppressWarnings("unchecked")
                        public void clicked(InputEvent event, float x, float y)
                        {
                            pushMenu(new SelectTextureRegionMenu(new SelectTextureRegionMenu.Select()
                            {
                                @Override
                                public void selected(String region)
                                {
                                    property.data = region;
                                }

                                @Override
                                public void cancelled()
                                {

                                }
                            }));
                        }
                    });

                    break;
                }
                case map:
                {
                    TextButton asTextButton = new TextButton("[EDIT]", BrainOutClient.Skin, "button-inspect");

                    valueActor = asTextButton;

                    asTextButton.addListener(new ClickOverListener()
                    {
                        @Override
                        @SuppressWarnings("unchecked")
                        public void clicked(InputEvent event, float x, float y)
                        {
                            final ObjectMap<String, String> items = (ObjectMap<String, String>)InstectableValue.setValue(PropertyValue.vStringMap,
                                ObjectMap.class, property.data);

                            pushMenu(new EditObjectMapMenu(items, () ->
                                property.data = InstectableValue.getValue(items, PropertyValue.vStringMap)));
                        }
                    });

                    break;
                }
                case checkbox:
                {
                    Table parent = new Table();

                    final CheckBox asCheckBox = new CheckBox("",
                            BrainOutClient.Skin, "checkbox-default");

                    Object o = InstectableValue.setValue(property.value, Boolean.class, property.data);

                    if (o instanceof Boolean)
                    {
                        asCheckBox.setChecked((Boolean)o);
                    }

                    asCheckBox.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Boolean b = asCheckBox.isChecked();
                            property.data = InstectableValue.getValue(b, property.value);
                        }
                    });

                    parent.add(asCheckBox).width(32).expandX().left();

                    valueActor = parent;

                    break;
                }
                case text:
                {
                    TextButton editText = new TextButton("[EDIT]", BrainOutClient.Skin, "button-inspect");

                    valueActor = editText;

                    editText.addListener(new ClickOverListener()
                    {
                        @Override
                        @SuppressWarnings("unchecked")
                        public void clicked(InputEvent event, float x, float y)
                        {
                            pushMenu(new EditTextMenu(property.data)
                            {
                                @Override
                                public void done(String text)
                                {
                                    property.data = text;
                                }
                            });
                        }
                    });

                    break;
                }
                default:
                case string:
                {
                    TextField asTextField = new TextField(property.data,
                            BrainOutClient.Skin, "edit-inspect");

                    valueActor = asTextField;

                    asTextField.setTextFieldListener((textField, c) -> property.data = textField.getText());

                    break;
                }
            }

            propertiesData.add(valueActor).pad(2).width(300).expandX().fillX().row();
        }

        data.add(propertiesPane).pad(20).expand().fill().row();


        Table buttons = new Table();
        data.add(buttons).expandX().row();

        TextButton ok = new TextButton(L.get("MENU_OK"), BrainOutClient.Skin, "button-default");

        ok.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                save();
                close();
            }
        });

        buttons.add(ok).width(128).pad(20).padTop(0);

        TextButton cancel = new TextButton(L.get("MENU_CANCEL"), BrainOutClient.Skin, "button-default");

        cancel.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                close();
            }
        });

        buttons.add(cancel).width(128).pad(20).padTop(0);

        return data;
    }

    protected void save()
    {
        onSave.save(inspectable, properties);
    }

    protected void close()
    {
        pop();
    }
}
