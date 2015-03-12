package com.vaadin;

import com.google.gwt.user.client.ui.ImageBundle;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.*;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import java.io.File;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("Exsitebike")
@Theme("funky")
public class ExsitebikeUI extends UI {

	/* User interface components are stored in session. */
	private Table contactList = new Table();

    private Label title = new Label("Cykelmagasinet AB");
    private Window overlay = new Window();
    private VerticalLayout header = new VerticalLayout();

	private TextField searchField = new TextField();
    private Slider priceSlide = new Slider(1000,10000);
	private Button addNewContactButton = new Button("Lägg till");
	private FormLayout editorLayout = new FormLayout();
	private FieldGroup editorFields = new FieldGroup();
    private Window subWindow = new Window("");

    String basepath = VaadinService.getCurrent()
            .getBaseDirectory().getAbsolutePath();
    FileResource logoResource = new FileResource(new File(basepath +
            "/WEB-INF/icons/wheel.png"));
    FileResource discardResource = new FileResource(new File(basepath +
            "/WEB-INF/icons/ic_action_discard.png"));
    FileResource editResource = new FileResource(new File(basepath +
            "/WEB-INF/icons/ic_action_edit.png"));
    FileResource saveResource = new FileResource(new File(basepath +
            "/WEB-INF/icons/ic_action_save.png"));
    FileResource exitResource = new FileResource(new File(basepath +
            "/WEB-INF/icons/ic_action_exit.png"));

    private Image logo = new Image("", logoResource);
    private Image removeContactButton = new Image("",discardResource);
    private Image editItemButton = new Image("",editResource);
    private Image saveItemButton = new Image("",saveResource);
    private Image closeOverlayButton = new Image("", exitResource);

	private static final String ARTICLE = "Artikelnummer";
    private static final String NAME = "Namn";
	private static final String PRICE = "Pris";
	private static final String[] fieldNames = new String[] {ARTICLE,NAME,PRICE};

    //private Bike firstBike;

	/*
	 * Create dummy database
	 */
	IndexedContainer contactContainer = createDummyDatasource();

	/*
	 * Init layouts
	 * Function runs when application starts
	 */
	protected void init(VaadinRequest request) {
		initLayout();
		initContactList();
		initEditor();
		initSearch();
        initButtons();
	}

	/*
	 * Init skeleton layout
	 */
	private void initLayout() {

        // Root element body
        AbsoluteLayout body = new AbsoluteLayout();
        setContent(body);

        // Create header layout
        header.setStyleName("header");
        header.addComponent(logo);

        // Create list layout
        VerticalLayout listLayout = new VerticalLayout();
        listLayout.addStyleName("listLayout");

        // Add filter layout
        VerticalLayout filterLayout = new VerticalLayout();
        filterLayout.addComponent(searchField);
        filterLayout.addComponent(priceSlide);

        listLayout.addComponent(filterLayout);//, "top:0px; right:10%; left:10%");

        // Add list of bikes
        listLayout.addComponent(contactList);
        listLayout.setSizeFull();
        listLayout.setExpandRatio(contactList, 1); // use all vertical space left from the footer
        contactList.setSizeFull();


        // Footer with add button
        HorizontalLayout bottomLayout = new HorizontalLayout();
        listLayout.addComponent(bottomLayout);
        bottomLayout.addComponent(addNewContactButton);
        bottomLayout.setWidth("100%");


        /* Print a bike item (testing)
        Bike bike = new Bike(1,"morfars cykel");
        bike.setPrice(999.2);
        BikeItemView bikeItem = new BikeItemView(bike);*/

		///searchField.setWidth("100%");
        //bikeItem.setWidth("100%");

        // The editor is initially not visible
        overlay.setWidth("80%");
        overlay.setHeight("60%");
        overlay.center();
        overlay.addStyleName("overlay");
        overlay.setContent(editorLayout);


        // Add all components to the main body
        body.addComponent(header);
        body.addComponent(listLayout);//, "left:13%; top:36%; right:13%; z-index:1;");
        //body.addComponent(overlay, "left:10%; top:38%; right:10%; z-index:10;");
        //addWindow(overlay);
	}

	private void initEditor() {

        //HorizontalLayout exit = new HorizontalLayout();
        //exit.setWidth("100%");
        //overlay.setComponent(closeOverlayButton)//, "top:3%; right:1%; z-index:10;");
        //overlay.setComponentAlignment(closeOverlayButton, Alignment.MIDDLE_RIGHT);
        //editorLayout.addComponent(exit);

		/* User interface can be created dynamically to reflect underlying data.
		* Initially the form is not editable */
        for (String fieldName : fieldNames) {
            TextField field = new TextField(fieldName);
            if(fieldName == ARTICLE) {
                //field.setVisible(false);
            }
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

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.addComponent(saveItemButton);
        hl.addComponent(editItemButton);
        hl.addComponent(removeContactButton);
        hl.setComponentAlignment(saveItemButton, Alignment.MIDDLE_RIGHT);
        hl.setComponentAlignment(editItemButton, Alignment.MIDDLE_RIGHT);
        hl.setComponentAlignment(removeContactButton, Alignment.MIDDLE_RIGHT);
        hl.setExpandRatio(saveItemButton, 1);
        hl.setExpandRatio(editItemButton, 1);
        /*
        editorLayout.addComponent(removeContactButton);
        editorLayout.addComponent(editItemButton);
        editorLayout.addComponent(saveItemButton);*/
        editorLayout.addComponent(hl);
        saveItemButton.setVisible(false);

        //editorLayout.addComponent(img);
		/*
		 * Data can be buffered in the user interface. When doing so, commit()
		 * writes the changes to the data source. Here we choose to write the
		 * changes automatically without calling commit().
		 */
        editorFields.setReadOnly(true);
        //editorFields.setBuffered(false);
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
		searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);

		/*
		 * When the event happens, we handle it in the anonymous inner class.
		 * You may choose to use separate controllers (in MVC) or presenters (in
		 * MVP) instead. In the end, the preferred application architecture is
		 * up to you.
		 */
		searchField.addTextChangeListener(new TextChangeListener() {
			public void textChange(final TextChangeEvent event) {

				/* Reset the filter for the contactContainer. */
				contactContainer.removeAllContainerFilters();
				contactContainer.addContainerFilter(new ContactFilter(event
						.getText()));
			}
		});
	}

	/*
	 * A custom filter for searching names and companies in the
	 * contactContainer.
	 */
	private class ContactFilter implements Filter {
		private String needle;

		public ContactFilter(String needle) {
			this.needle = needle.toLowerCase();
		}

		public boolean passesFilter(Object itemId, Item item) {/*
			String haystack = ("" + item.getItemProperty(FNAME).getValue()
					+ item.getItemProperty(LNAME).getValue() + item
					.getItemProperty(COMPANY).getValue()).toLowerCase();
			return haystack.contains(needle);*/
            return false;
		}

		public boolean appliesToProperty(Object id) {
			return true;
		}
	}

	private void initButtons() {


		addNewContactButton.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {

                editorFields.setReadOnly(true);
                saveItemButton.setVisible(false);
                editItemButton.setVisible(true);
				/*
				 * Rows in the Container data model are called Item. Here we add
				 * a new row in the beginning of the list.
				 */
				contactContainer.removeAllContainerFilters();
				Object contactId = contactContainer.addItemAt(0);

				/*
				 * Each Item has a set of Properties that hold values. Here we
				 * set a couple of those.
				 */
                int article = contactContainer.size() + 1;
				contactList.getContainerProperty(contactId, ARTICLE).setValue(
						Integer.toString(article));
                contactList.getContainerProperty(contactId, ARTICLE).setReadOnly(true);
				contactList.getContainerProperty(contactId, NAME).setValue(
						"Contact");

				/* Lets choose the newly created contact to edit it. */
				contactList.select(contactId);
			}
		});

		removeContactButton.addClickListener(new MouseEvents.ClickListener() {

			public void click(MouseEvents.ClickEvent event) {

                VerticalLayout vl = new VerticalLayout();
                vl.addComponent(new Label("Är du säker på att du vill radera posten?"));
                Button yes = new Button("Ja");
                Button no = new Button("Nej");
                HorizontalLayout hl = new HorizontalLayout();
                hl.setWidth("100%");
                hl.addComponent(yes);
                hl.addComponent(no);

                vl.addComponent(hl);

                subWindow.setWidth("60%");
                subWindow.setHeight("10%");
                subWindow.setContent(vl);
                subWindow.center();
                addWindow(subWindow);

                yes.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent clickEvent) {
                        Object contactId = contactList.getValue();
                        contactList.removeItem(contactId); //TODO make this work
                        editorFields.setReadOnly(true);
                        subWindow.close();
                        /*
                        if(overlay.isClosable()) {
                            overlay.close();
                        }*/
                    }
                });
                no.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent clickEvent) {
                        subWindow.close();
                    }
                });



			}
		});
/*
        closeOverlayButton.addClickListener(new MouseEvents.ClickListener() {
            @Override
            public void click(MouseEvents.ClickEvent event) {
                overlay.setVisible(false);
                editorFields.setReadOnly(true);
                //editOrSaveItemButton.setCaption("Redigera");
                editItemButton.setVisible(true);
            }
        });*/

        editItemButton.addClickListener(new MouseEvents.ClickListener() {
            @Override
            public void click(MouseEvents.ClickEvent event) {

                editorFields.setReadOnly(false);
                //editOrSaveItemButton.setCaption("Spara");
                //editOrSaveItemButton = new Image("",saveResource);
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

            public void valueChange(ValueChangeEvent event) {
                double value = (Double) priceSlide.getValue();

                // Use the value

            }
        });
	}

    private void saveItem() {
        //if click on save
        editorFields.setReadOnly(true);
        //editOrSaveItemButton.setCaption("Redigera");
        //editOrSaveItemButton = new Image("",editResource);
        editItemButton.setVisible(true);
        saveItemButton.setVisible(false);
        editorFields.setBuffered(false);
    }

	private void initContactList() {

		contactList.setContainerDataSource(contactContainer);
		contactList.setVisibleColumns(new String[] {NAME,PRICE});
		contactList.setSelectable(true);
		contactList.setImmediate(true);

		contactList.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                Object contactId = contactList.getValue();

				/*
				 * When a contact is selected from the list, we want to show
				 * that in our editor on the right. This is nicely done by the
				 * FieldGroup that binds all the fields to the corresponding
				 * Properties in our contact at once.
				 */
                if (contactId != null) {
                    editorFields.setItemDataSource(contactList
                            .getItem(contactId));
                }
                editorFields.setReadOnly(true);
                saveItemButton.setVisible(false);
                editItemButton.setVisible(true);
                addWindow(overlay);
                //overlay.setVisible(contactId != null);
            }
        });
	}

	/*
	 * Generate some in-memory example data to play with. In a real application
	 * we could be using SQLContainer, JPAContainer or some other to persist the
	 * data.
	 */
	private static IndexedContainer createDummyDatasource() {
		IndexedContainer ic = new IndexedContainer();

        //IndexedContainer anotherIC = new IndexedContainer();
        //anotherIC.addItem(new Bike(2,"sisters bike"));

		for (String p : fieldNames) {
			ic.addContainerProperty(p, String.class, "");
		}

		/* Create dummy data by randomly combining first and last names */
		String[] articles = { "1", "2", "3", "4", "5",
				"6", "7"};

		String[] names = { "Henning", "Allis", "Hege", "Tristan", "Johan",
				"Ranveig", "Olle"};

		for (int i = 0; i < 7; i++) {
			Object id = ic.addItem();
			ic.getContainerProperty(id, ARTICLE).setValue(
					articles[i]);
			ic.getContainerProperty(id, NAME).setValue(names[i]);
					//names[(int) (names.length * Math.random())]);
		}

		return ic;
	}

}
