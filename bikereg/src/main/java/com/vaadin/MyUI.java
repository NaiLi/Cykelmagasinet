package com.vaadin;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;

import java.io.File;

/**
 *
 */
@Theme("mytheme")
@Widgetset("com.vaadin.MyAppWidgetset")
public class MyUI extends UI {

    /* User interface components are stored in session. */
    private Table biketList = new Table();
    private Window overlay = new Window();
    private VerticalLayout header = new VerticalLayout();
    private Button addNewButton = new Button("Lägg till");
    private VerticalLayout overlayLayout = new VerticalLayout();
    private FormLayout editorLayout = new FormLayout();
    private FieldGroup editorFields = new FieldGroup();
    private Window subWindow = new Window("");
    private boolean add = false;

    String basepath = VaadinService.getCurrent()
            .getBaseDirectory().getAbsolutePath();
    FileResource logoResource = new FileResource(new File(basepath +
            "/VAADIN/icons/wheel.png"));
    FileResource discardResource = new FileResource(new File(basepath +
            "/VAADIN/icons/ic_action_discard.png"));
    FileResource editResource = new FileResource(new File(basepath +
            "/VAADIN/icons/ic_action_edit.png"));
    FileResource saveResource = new FileResource(new File(basepath +
            "/VAADIN/icons/ic_action_save.png"));
    FileResource exitResource = new FileResource(new File(basepath +
            "/VAADIN/icons/ic_action_exit.png"));

    private Image logo = new Image("", logoResource);
    private Image removeButton = new Image("",discardResource);
    private Image editItemButton = new Image("",editResource);
    private Image saveItemButton = new Image("",saveResource);

    private static final String ARTICLE = "Artikelnummer";
    private static final String NAME = "Namn";
    private static final String PRICE = "Pris";
    private static final String[] fieldNames = new String[] {ARTICLE,NAME,PRICE, "Typ", "Modell", "Färg"};



    // Create filter layout
    private HorizontalLayout columns1 = new HorizontalLayout();
    private HorizontalLayout columns2 = new HorizontalLayout();
    private HorizontalLayout modelLayout = new HorizontalLayout();
    private HorizontalLayout priceLayout = new HorizontalLayout();
    private HorizontalLayout typeLayout = new HorizontalLayout();

    private Label modelLabel = new Label("Modell");
    private Label priceLabel = new Label("Pris");
    private Label priceRangeLabel = new Label("1000-10000 kr");
    private Label typeLabel = new Label("Typ");

    private Select typeSelect = new Select();
    private OptionGroup model = new OptionGroup();
    private Slider priceSlide = new Slider(1000,10000);

    private TextField searchField = new TextField();
    /*
     * Create dummy database
     */
    IndexedContainer listContainer = createDummyDatasource();

    /*
     * Init layouts
     * Function runs when application starts
     */
    protected void init(VaadinRequest request) {
        initLayout();
        initList();
        initEditor();
        initSearch();
        initButtons();
    }

    /*
     * Init skeleton layout
     */
    private void initLayout() {

        // Root element body
        VerticalLayout body = new VerticalLayout();
        setContent(body);
        body.setStyleName("body");

        // Create header layout
        header.setStyleName("header");
        header.addComponent(logo);

        // Create filter layout
        VerticalLayout filterLayout = new VerticalLayout();
        filterLayout.addStyleName("filterLayout");
        typeLayout.addComponent(typeLabel);
            typeSelect.addItem("Off road");
            typeSelect.addItem("City");
            typeSelect.addItem("Landsväg");
            typeSelect.addItem("Enhjuling");
        typeLayout.addComponent(typeSelect);
        modelLayout.addStyleName("modellayout");
        modelLayout.addComponent(modelLabel);
        modelLayout.addComponent(model);
        model.setStyleName("optionGroup");
            model.addItem("Dam");
            model.addItem("Barn");
            model.addItem("Herr");
        columns1.addComponent(typeLayout);
        columns1.addComponent(modelLayout);

        priceSlide.setWidth("100px");
        priceLayout.addComponent(priceLabel);
        priceLayout.addComponent(priceSlide);
        priceLayout.addComponent(priceRangeLabel);
        priceLayout.setComponentAlignment(priceRangeLabel,Alignment.MIDDLE_LEFT);
        columns2.addComponent(priceLayout);
        columns2.addComponent(searchField);

        columns2.setWidth("100%");
        columns2.setExpandRatio(priceLayout, 1);
        searchField.addStyleName("searchField");
        filterLayout.addComponent(columns1);
        filterLayout.addComponent(columns2);

        // Create list layout
        VerticalLayout listLayout = new VerticalLayout();
        listLayout.addStyleName("listLayout");
        listLayout.addComponent(filterLayout);

        // Add list of bikes
        listLayout.addComponent(biketList);
        listLayout.setSizeFull();
        listLayout.setExpandRatio(biketList, 1); // use all vertical space left from the footer
        biketList.setSizeFull();


        // Footer with add button
        HorizontalLayout bottomLayout = new HorizontalLayout();
        bottomLayout.setStyleName("footer");
        listLayout.addComponent(bottomLayout);
        bottomLayout.addComponent(addNewButton);
        bottomLayout.setWidth("100%");

        // The editor is initially not visible
        overlay.setWidth("80%");
        overlay.setHeight("90%");
        overlay.center();
        overlay.addStyleName("overlay");
        overlay.setContent(overlayLayout);


        // Add all components to the main body
        body.addComponent(header);
        body.addComponent(listLayout);
    }

    private void initEditor() {

		// Create form
        for (String fieldName : fieldNames) {
            TextField field = new TextField(fieldName);
            /*if(fieldName == ARTICLE) { If not interesting
                //field.setVisible(false);
            }*/
            //
            field.setReadOnly(true);
            editorLayout.addComponent(field);
            field.setWidth("70%");

			/*
			 * We use a FieldGroup to connect multiple components to a data
			 * source at once.
			 */
            editorFields.bind(field, fieldName);
        }

        // Init buttons in form
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        removeButton.setWidth("40px");
        saveItemButton.setWidth("40px");
        editItemButton.setWidth("40px");
        hl.addComponent(saveItemButton);
        hl.addComponent(editItemButton);
        hl.addComponent(removeButton);
        hl.setComponentAlignment(saveItemButton, Alignment.MIDDLE_RIGHT);
        hl.setComponentAlignment(editItemButton, Alignment.MIDDLE_RIGHT);
        hl.setComponentAlignment(removeButton, Alignment.MIDDLE_RIGHT);
        hl.setExpandRatio(saveItemButton, 1);
        hl.setExpandRatio(editItemButton, 1);

        overlayLayout.addComponent(editorLayout);
        overlayLayout.addComponent(hl);
        overlayLayout.setHeight("100%");
        overlayLayout.setExpandRatio(editorLayout,1);
        saveItemButton.setVisible(false);

        // Set to read only as default
        editorFields.setReadOnly(true);
    }

    private void initSearch() {

		/*
		 * We want to show a subtle prompt in the search field. We could also
		 * set a caption that would be shown above the field or description to
		 * be shown in a tooltip.
		 */
        searchField.setInputPrompt("Sök bland artiklar");

		/*
		 * Granularity for sending events over the wire can be controlled. By
		 * default simple changes like writing a text in TextField are sent to
		 * server with the next Ajax call. You can set your component to be
		 * immediate to send the changes to server immediately after focus
		 * leaves the field. Here we choose to send the text over the wire as
		 * soon as user stops writing for a moment.
		 */
        searchField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);

		/*
		 * When the event happens, we handle it in the anonymous inner class.
		 * You may choose to use separate controllers (in MVC) or presenters (in
		 * MVP) instead. In the end, the preferred application architecture is
		 * up to you.
		 */
        searchField.addTextChangeListener(new FieldEvents.TextChangeListener() {
            public void textChange(final FieldEvents.TextChangeEvent event) {

				/* Reset the filter for the ListContainer. */
                listContainer.removeAllContainerFilters();
                listContainer.addContainerFilter(new ListFilter(event
                        .getText()));
            }
        });
    }

    /*
     * A custom filter for searching names and companies in the
     * listContainer.
     */
    private class ListFilter implements Container.Filter {
        private String needle;

        public ListFilter(String needle) {
            this.needle = needle.toLowerCase();
        }

        public boolean passesFilter(Object itemId, Item item) {
			String haystack = ("" + item.getItemProperty(ARTICLE).getValue()
					+ item.getItemProperty(NAME).getValue()
                    + item.getItemProperty(PRICE).getValue())
                    + item.getItemProperty("Färg").getValue()
                    + item.getItemProperty("Modell").getValue();
			return haystack.contains(needle);
        }

        public boolean appliesToProperty(Object id) {
            return true;
        }
    }

    private void initButtons() {


        addNewButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {

                // Create new object in beginning of container (after filtering)
                listContainer.removeAllContainerFilters();
                Object objectId = listContainer.addItemAt(0);

				/*
				 * Each Item has a set of Properties that hold values. Here we
				 * set a couple of those.
				 */
                // Set next article number and placeholder for new bike name
                int article = listContainer.size() + 1;
                biketList.getContainerProperty(objectId, ARTICLE).setValue(
                        Integer.toString(article));
                biketList.getContainerProperty(objectId, ARTICLE).setReadOnly(true);
                biketList.getContainerProperty(objectId, NAME).setValue(
                        "Ny cykel");

                add = true;
                // Select the new bike to get the edit window opened
                biketList.select(objectId);
            }
        });

        removeButton.addClickListener(new MouseEvents.ClickListener() {

            public void click(MouseEvents.ClickEvent event) {

                VerticalLayout vl = new VerticalLayout();
                vl.setStyleName("confirmationWindow");
                vl.addComponent(new Label("Är du säker på att du vill radera posten?"));
                Button yes = new Button("Ja");
                Button no = new Button("Nej");
                HorizontalLayout hl = new HorizontalLayout();
                hl.setWidth("100%");
                hl.addComponent(yes);
                hl.addComponent(no);
                hl.setComponentAlignment(yes,Alignment.MIDDLE_CENTER);
                hl.setComponentAlignment(no,Alignment.MIDDLE_LEFT);

                vl.addComponent(hl);

                subWindow.setWidth("40%");
                subWindow.setHeight("20%");
                subWindow.setContent(vl);
                subWindow.center();
                addWindow(subWindow);

                yes.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent clickEvent) {
                        Object objectId = biketList.getValue();
                        biketList.removeItem(objectId);
                        editorFields.setReadOnly(true);
                        subWindow.close();

                        if(overlay.isClosable()) {
                            overlay.close();
                        }
                    }
                });
                no.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent clickEvent) {
                        subWindow.close();
                    }
                });



            }
        });

        editItemButton.addClickListener(new MouseEvents.ClickListener() {
            @Override
            public void click(MouseEvents.ClickEvent event) {

                editorFields.setReadOnly(false);
                editItemButton.setVisible(false);
                saveItemButton.setVisible(true);

            }
        });
        saveItemButton.addClickListener(new MouseEvents.ClickListener() {
            @Override
            public void click(MouseEvents.ClickEvent event) {

                saveItem();
                //overlay.close(); //if window should be closed on save
            }
        });

        priceSlide.addValueChangeListener(new Property.ValueChangeListener() {

            public void valueChange(Property.ValueChangeEvent event) {
                int value = (int)Math.floor(priceSlide.getValue());

                priceRangeLabel.setValue(value + " - 10000 kr");

            }
        });
    }

    private void saveItem() {

        // When saving, change buttons etc.
        editorFields.setReadOnly(true);
        editItemButton.setVisible(true);
        saveItemButton.setVisible(false);
        // Update the list
        editorFields.setBuffered(false);
    }

    private void initList() {

        biketList.setContainerDataSource(listContainer);
        biketList.setVisibleColumns(new String[] {NAME,PRICE,"Typ", "Modell", "Färg", ARTICLE});
        biketList.setSelectable(true);
        biketList.setImmediate(true);

        biketList.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                Object objectId = biketList.getValue();

				// Open editor on select
                if (objectId != null) {
                    editorFields.setItemDataSource(biketList
                            .getItem(objectId));

                    openEditor();
                }
            }
        });
    }

    private void openEditor() {
        if(!add) {
            editorFields.setReadOnly(true);
            saveItemButton.setVisible(false);
            editItemButton.setVisible(true);
        } else {
            editorFields.setReadOnly(false);
            saveItemButton.setVisible(true);
            editItemButton.setVisible(false);
            add = false;
        }
        addWindow(overlay);
    }

    /*
     * Generate some in-memory example data to play with. In a real application
     * we could be using SQLContainer, JPAContainer or some other to persist the
     * data.
     */
    private static IndexedContainer createDummyDatasource() {
        IndexedContainer ic = new IndexedContainer();

        for (String p : fieldNames) {
            ic.addContainerProperty(p, String.class, "");
        }

		/* Create dummy data by randomly combining first and last names */
        String[] articles = { "1", "2", "3", "4", "5",
                "6", "7"};

        String[] names = { "Sya röd", "Blåvik blå", "Helgebo gul", "Ulrika grön", "Ljungsbro vit",
                "Omberg rosa", "Bjälbo svart"};

        String[] prices = {"9 999", "12 299", "7 799", "1 337"};
        String[] types = {"Dam", "Herr", "Barn"};
        String[] models = {"Off road", "City", "Landsväg", "Enhjuling"};
        String[] colors = {"Blå", "Gulgrön", "Mörkvit", "Lila", "Grön"};

        for (int i = 0; i < 100; i++) {
            Object id = ic.addItem();
            ic.getContainerProperty(id, ARTICLE).setValue(
                    articles[(int) (articles.length * Math.random())]);//articles[i]);
            ic.getContainerProperty(id, NAME).setValue(//names[i]);
                    names[(int) (names.length * Math.random())]);
            ic.getContainerProperty(id, PRICE).setValue(
                    prices[(int) (prices.length * Math.random())]);
            ic.getContainerProperty(id, "Typ").setValue(
                    types[(int) (types.length * Math.random())]);
            ic.getContainerProperty(id, "Modell").setValue(
                    models[(int) (models.length * Math.random())]);
            ic.getContainerProperty(id, "Färg").setValue(
                    colors[(int) (colors.length * Math.random())]);
        }

        return ic;
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
