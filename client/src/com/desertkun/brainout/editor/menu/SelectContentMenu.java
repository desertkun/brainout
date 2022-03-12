package com.desertkun.brainout.editor.menu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.managers.ContentManager;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.ui.ClickOverListener;

import java.util.function.Consumer;

public class SelectContentMenu extends FormMenu implements ContentManager.ContentQueryPredicate
{
    private final ContentSelected delegate;
    private final Class<? extends Content> classOf;
    private Content selectedContent;

    private Table content;
    private ButtonGroup<Button> items;
    private String filter;
    private int rowCounter;

    @Override
    public boolean isMatch(Content check)
    {
        if (!check.isEditorSelectable()) return false;

        if (filter != null)
        {
            if (check.getTitle().get() != null && check.getTitle().get().toLowerCase().contains(filter))
                return true;

            if (check.getID().toLowerCase().contains(filter))
                return true;

            return false;
        }

        return true;
    }

    @Override
    protected String formBorderStyle()
    {
        return "form-border-red";
    }

    public interface ContentSelected
    {
        void selected(Content content);
        void canceled();
        boolean filter(Content content);
    }

    public SelectContentMenu(Content defaultContent,
         Class<? extends Content> classOf,
         ContentSelected delegate)
    {
        this.delegate = delegate;
        this.selectedContent = defaultContent;
        this.classOf = classOf;
    }

    @Override
    public Table createUI()
    {
        Table data = super.createUI();

        this.content = new Table();

        Table buttons = new Table();

        this.items = new ButtonGroup<>();

        ScrollPane scrollPane = new ScrollPane(content, BrainOutClient.Skin, "scroll-default");
        scrollPane.setFadeScrollBars(false);

        getRootActor().getStage().setScrollFocus(scrollPane);

        updateContent();

        TextButton cancel = new TextButton(L.get("MENU_CANCEL"),
            BrainOutClient.Skin, "button-default");

        cancel.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                SelectContentMenu.this.cancel();
            }
        });

        buttons.add(cancel).size(196, 64);

        Table filterTable = new Table();

        final TextField filerField = new TextField("", BrainOutClient.Skin, "edit-default");

        filerField.setTextFieldListener((textField, c) ->
        {
            filter = filerField.getText().toLowerCase();

            updateContent();
        });

        data.add(filerField).pad(8).expandX().fillX().row();
        data.add(scrollPane).height(500).width(820).pad(8).padTop(0).expand().fill().row();
        data.add(buttons).padBottom(8).expandX().row();

        setKeyboardFocus(filerField);
        setScrollFocus(scrollPane);

        return data;
    }

    private void cancel()
    {
        delegate.canceled();
        pop();
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public boolean escape()
    {
        cancel();

        return true;
    }

    private void updateContent()
    {
        content.clear();
        items.clear();

        rowCounter = 0;

        BrainOutClient.ContentMgr.queryContentGen(classOf, c ->
        {
            if (!isMatch(c))
                return;

            if (!delegate.filter(c))
            {
                return;
            }

            Button textButton = new Button(BrainOutClient.Skin, "button-hoverable-clear");

            if (c == selectedContent)
            {
                textButton.setChecked(true);
            }

            textButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    delegate.selected(c);

                    pop();
                }
            });

            Table i = new Table();

            if (c.hasComponent(IconComponent.class))
            {
                IconComponent iconComponent = c.getComponent(IconComponent.class);

                Image image = new Image(iconComponent.getIcon());
                image.setTouchable(Touchable.disabled);
                image.setScaling(Scaling.none);

                textButton.add(image).size(32);
            }
            else
            {
                textButton.add().size(32);
            }

            {
                Label l = new Label(c.getTitle().get(), BrainOutClient.Skin, "title-small");
                l.setTouchable(Touchable.disabled);
                textButton.add(l).padLeft(8).expand().fill().row();
            }

            items.add(textButton);

            i.add(textButton).height(32).pad(2).expandX().fillX().row();

            Cell<Table> cell = content.add(i).width(400).fill();

            rowCounter++;

            if (rowCounter % 2 == 0)
            {
                cell.row();
            }
        });
    }
}
