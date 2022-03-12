package com.desertkun.brainout.vote;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;

public abstract class ArraySelectClientVote extends ClientVote
{
    public ObjectMap<String, String> items;
    private boolean onTop;

    public ArraySelectClientVote()
    {
        items = new ObjectMap<>();
        onTop = true;
    }

    protected void addOption(String id, String title)
    {
        items.put(id, title);
    }

    public class SelectItemMenu extends Menu
    {
        private final SelectedCallback callback;

        public SelectItemMenu(SelectedCallback callback)
        {
            this.callback = callback;
        }

        @Override
        public void render()
        {
            BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

            super.render();
        }

        @Override
        public boolean stayOnTop()
        {
            return onTop;
        }

        private void close()
        {
            onTop = false;
            pop();
        }

        @Override
        public boolean lockUpdate()
        {
            return true;
        }

        @Override
        public boolean lockInput()
        {
            return true;
        }

        @Override
        protected MenuAlign getMenuAlign()
        {
            return MenuAlign.bottom;
        }

        @Override
        public Table createUI()
        {
            Table data = new Table();

            {
                Label title = new Label(getSelectTitle(), BrainOutClient.Skin, "title-yellow");
                data.add(title).pad(4).row();
            }

            for (ObjectMap.Entry<String, String> entry : items)
            {
                String id = entry.key;
                String title = entry.value;

                TextButton button = new TextButton(title,
                    BrainOutClient.Skin, "button-default");

                button.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pop();
                        callback.selected(id);
                    }
                });

                data.add(button).size(192, 48).pad(4).row();
            }

            {

                TextButton cancel = new TextButton(L.get("MENU_CANCEL"),
                        BrainOutClient.Skin, "button-default");

                cancel.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pop();
                    }
                });

                data.add(cancel).size(192, 48).pad(4).row();
            }

            return data;
        }
    }

    @Override
    public void show(SelectedCallback callback)
    {
        BrainOutClient.getInstance().topState().pushMenu(new SelectItemMenu(callback));
    }

    protected abstract String getSelectTitle();
}
