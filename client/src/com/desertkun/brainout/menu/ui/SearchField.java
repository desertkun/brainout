package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.AutoConvertConsumable;
import com.desertkun.brainout.content.components.HideFromMarketComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.ConsumableToOwnableContent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.utils.MarketUtils;
import org.anthillplatform.runtime.services.MarketService;

public class SearchField extends Table
{
    SearchFieldStyle style;
    private SearchFieldResultList searchFieldResultList;
    private Array<ResultObject> itemsList;
    private Predicate<MarketService.MarketItemEntry> callback;

    public SearchField(String style, Predicate<MarketService.MarketItemEntry> callback)
    {
        super();

        this.callback = callback;

        setSkin(BrainOutClient.Skin);

        searchFieldResultList = new SearchFieldResultList(this);

        setStyle(BrainOutClient.Skin.get(style, SearchFieldStyle.class));

        TextField searchBox = new TextField("", BrainOutClient.Skin, "edit-empty");
        searchBox.setName("searchBox");
        searchBox.setAlignment(Align.center);
        searchBox.setMessageText(L.get("MENU_SEARCH"));

        searchBox.addListener(new ClickListener()
        {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                switch (button)
                {
                    case Input.Buttons.LEFT:
                        if (searchBox.getText().length() >= 2)
                        {
                            if (searchFieldResultList.isHidden)
                                showResult();
                        }
                        break;

                    case Input.Buttons.RIGHT:
                        if (!searchFieldResultList.isHidden)
                            searchFieldResultList.hide();
                        break;
                }

                return true;
            }

        });

        searchBox.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                if (searchBox.getText().length() >= 2)
                {
                    itemsList = searchResults(searchBox.getText());

                    if (itemsList.size > 0)
                    {
                        itemsList.sort((a, b) ->
                        {
                            if (a.priority > b.priority) return 1;
                            else if (a.priority == b.priority) return 0;
                            return -1;
                        });

                        searchFieldResultList.setItems(itemsList.toArray(ResultObject.class));
                        showResult();
                    }
                    else if (!searchFieldResultList.isHidden)
                    {
                        searchFieldResultList.showNotFound();
                        showResult();
                    }

                }
                else if (!searchFieldResultList.isHidden) searchFieldResultList.hide();
            }
        });

        Image searchIcon = new Image(BrainOutClient.Skin, "icon-search");
        searchIcon.setName("searchIcon");
        searchIcon.setX(searchBox.getWidth() + this.style.background.getLeftWidth() + this.style.background.getRightWidth() - searchIcon.getWidth());
        searchIcon.setScaling(Scaling.none);

        add().width(searchIcon.getWidth());
        add(searchBox).expand().fill();
        add(searchIcon);

        setSearchInactive();
    }

    private Array<ResultObject> searchResults(String text)
    {
        Array<ConsumableContent> items = BrainOutClient.ContentMgr.queryContent(ConsumableContent.class);

        Array<ResultObject> results = new Array<>();

        text = text.toLowerCase().replaceAll(" ", "").replaceAll("-", "");

        for (ConsumableContent item : items)
        {
            if (item instanceof Block || item instanceof ConsumableToOwnableContent) continue;

            if (item.getComponent(HideFromMarketComponent.class) != null) continue;

            if (item.getComponent(AutoConvertConsumable.class) != null) continue;

            int index = -1;
            String title = "Noname";

            if (item.getID() != null)
            {
                title = item.getID();
                index = item.getID().toLowerCase().indexOf(text);
            }

            if (item.getTitle() != null && item.getTitle().get() != null)
            {
                title = item.getTitle().get();

                int newIndex = title.toLowerCase().replaceAll(" ", "").replaceAll("-", "").indexOf(text);
                if (index < 0 || (newIndex >= 0 && newIndex < index)) index = newIndex;
            }

            if (index < 0 && item.getDescription() != null && item.getDescription().get() != null)
            {
                index = item.getDescription().get().toLowerCase().indexOf(text);

                //add 1000 to lower the priority of matching the description over the name
                if (index >= 0) index += 1000;
            }

            if (index >= 0)
            {
                ConsumableItem consumbleItem;

                if (item instanceof Instrument)
                {
                    Instrument instrument = ((Instrument) item);
                    InstrumentData instrumentData = instrument.getData("default");
                    instrumentData.setSkin(instrument.getDefaultSkin());
                    consumbleItem = new InstrumentConsumableItem(instrumentData, "default");
                }
                else consumbleItem = item.acquireConsumableItem();

                ConsumableRecord record = new ConsumableRecord(consumbleItem, 1,0);

                MarketService.MarketItemEntry entry = MarketUtils.ConsumableRecordToMarketEntry(record);

                if (entry != null)
                    results.add(new ResultObject(index, entry, item.getID(), title));
            }
        }

        return results;
    }

    private void showResult()
    {
        searchFieldResultList.show();
    }

    private void setSearchInactive()
    {
        ((TextField)findActor("searchBox")).setText("");
    }

    class SearchFieldResultList extends ScrollPane
    {
        private final SearchField searchField;
        private List<ResultObject> list;
        private InputListener hideListener;
        private Actor oldFocus;
        private boolean isHidden;
        private Container notFoundLabel;
        private InputListener searchKeyboardControl;

        public SearchFieldResultList(SearchField searchField)
        {
            super(null);

            this.searchField = searchField;
            this.isHidden = true;

            setOverscroll(false, false);
            setFadeScrollBars(false);
            setScrollingDisabled(true, false);

            list = new List<>(BrainOutClient.Skin, "list-default");
            this.setActor(list);

            list.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
                {
                    switch (button)
                    {
                        case Input.Buttons.LEFT:
                            if (list.getSelected() != null)
                            {
                                sendSelectedItem();
                            }
                            break;

                        case Input.Buttons.RIGHT:
                            if (!isHidden)
                                hide();
                            break;
                    }

                    return true;
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                {
                    Stage stage = searchField.getStage();
                    oldFocus = stage.getScrollFocus();
                    stage.setScrollFocus(list.getParent());
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                {
                    Stage stage = searchField.getStage();

                    if (oldFocus != null)
                    {
                        stage.setScrollFocus(oldFocus);
                        oldFocus = null;
                    }

                    list.getSelection().clear();
                }

                @Override
                public boolean mouseMoved (InputEvent event, float x, float y) {
                    list.setSelectedIndex(Math.min(list.getItems().size - 1, (int)((list.getHeight() - y) / list.getItemHeight())));
                    return true;
                }
            });

            hideListener = new InputListener()
            {
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                    Actor target = event.getTarget();
                    if (isAscendantOf(target)) return false;
                    hide();
                    return false;
                }

                public boolean keyDown (InputEvent event, int keycode) {
                    if (keycode == Input.Keys.ESCAPE) hide();
                    return false;
                }
            };

            searchKeyboardControl = new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode)
                {
                    switch(keycode)
                    {
                        case Input.Keys.DOWN:
                            if (list.getSelectedIndex() == -1)
                                list.setSelectedIndex(0);

                            else if (list.getSelectedIndex() < list.getItems().size - 1)
                                list.setSelectedIndex(list.getSelectedIndex() + 1);

                            updateScrolling();

                            break;

                        case Input.Keys.UP:
                            if (list.getSelectedIndex() == -1)
                                list.setSelectedIndex(list.getItems().size - 1);

                            else if (list.getSelectedIndex() > 0)
                                list.setSelectedIndex(list.getSelectedIndex() - 1);

                            updateScrolling();

                            break;

                        case Input.Keys.ENTER:
                            sendSelectedItem();
                            break;
                    }
                    return true;
                }
            };

            this.notFoundLabel = new Container();
            this.notFoundLabel.setActor(new Label(L.get("NOTHING_FOUND"), BrainOutClient.Skin, "title-gray"));
            this.notFoundLabel.align(Align.left);
        }

        private void updateScrolling()
        {
            float contentHeight = getHeight() - getStyle().background.getBottomHeight() - getStyle().background.getTopHeight();
            float currentScroll = contentHeight + getScrollY();

            if (list.getSelected() == null) return;

            float currentSelectedPosition = list.getStyle().background.getTopHeight() + (list.getSelectedIndex() + 1) * list.getItemHeight();

            //scroll down
            if (currentSelectedPosition > currentScroll - list.getItemHeight())
                setScrollY(currentSelectedPosition - contentHeight + list.getItemHeight());

            //scroll up
            else if (currentSelectedPosition < getScrollY() + list.getItemHeight() * 2)
                setScrollY(currentSelectedPosition - list.getItemHeight() * 2);
        }

        private void sendSelectedItem()
        {
            if (list.getSelected() != null)
            {
                hide();
                list.clearListeners();

                callback.evaluate(list.getSelected().record);
            }
        }

        public void show()
        {
            Stage stage = searchField.getStage();

            stage.removeCaptureListener(hideListener);
            stage.addCaptureListener(hideListener);

            float heightOffset = getStyle().background.getTopHeight() + getStyle().background.getBottomHeight();
            heightOffset += list.getStyle().background.getTopHeight() + list.getStyle().background.getBottomHeight();
            setHeight(Math.max(1, Math.min(list.getItems().size, 10)) * list.getItemHeight() + heightOffset);
            setX(-getStyle().background.getLeftWidth());
            setY(-getHeight() - getStyle().background.getBottomHeight());

            isHidden = false;
            searchField.addActor(this);

            setWidth(searchField.getWidth() + getStyle().background.getLeftWidth() + getStyle().background.getRightWidth());

            getStage().addListener(searchKeyboardControl);
        }

        public void hide()
        {
            isHidden = true;

            Stage stage = searchField.getStage();
            stage.removeCaptureListener(hideListener);
            stage.removeListener(searchKeyboardControl);
            this.remove();
        }

        public void setItems(ResultObject[] items)
        {
            setActor(list);
            list.setItems(items);
            list.setSelectedIndex(0);
        }

        public void showNotFound()
        {
            list.clearItems();
            setActor(notFoundLabel);
            notFoundLabel.padLeft(getStyle().background.getLeftWidth() + list.getStyle().background.getLeftWidth());
        }
    }

    static public class SearchFieldStyle {
        public BitmapFont font;
        public Color fontColor = new Color(1, 1, 1, 1);
        /** Optional. */
        public Color disabledFontColor;
        /** Optional. */
        public Drawable background;
        public ScrollPane.ScrollPaneStyle scrollStyle;
        public List.ListStyle listStyle;
        /** Optional. */
        public Drawable backgroundOver, backgroundOpen, backgroundDisabled;

        public SearchFieldStyle () {
        }

        public SearchFieldStyle (BitmapFont font, Color fontColor, Drawable background, ScrollPane.ScrollPaneStyle scrollStyle,
                               List.ListStyle listStyle) {
            this.font = font;
            this.fontColor.set(fontColor);
            this.background = background;
            this.scrollStyle = scrollStyle;
            this.listStyle = listStyle;
        }

        public SearchFieldStyle (SelectBox.SelectBoxStyle style) {
            this.font = style.font;
            this.fontColor.set(style.fontColor);
            if (style.disabledFontColor != null) this.disabledFontColor = new Color(style.disabledFontColor);
            this.background = style.background;
            this.backgroundOver = style.backgroundOver;
            this.backgroundOpen = style.backgroundOpen;
            this.backgroundDisabled = style.backgroundDisabled;
            this.scrollStyle = new ScrollPane.ScrollPaneStyle(style.scrollStyle);
            this.listStyle = new List.ListStyle(style.listStyle);
        }
    }

    public void setStyle (SearchFieldStyle style) {
        if (style == null) throw new IllegalArgumentException("style cannot be null.");
        this.style = style;
        if (searchFieldResultList != null) {
            searchFieldResultList.setStyle(style.scrollStyle);
            searchFieldResultList.list.setStyle(style.listStyle);
        }
        invalidateHierarchy();
    }

    class ResultObject
    {
        int priority;
        MarketService.MarketItemEntry record;
        String id;
        String name;

        public ResultObject(int priority, MarketService.MarketItemEntry record, String id, String name)
        {
            this.priority = priority;
            this.record = record;
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
