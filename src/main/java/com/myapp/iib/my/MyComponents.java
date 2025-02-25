package com.myapp.iib.my;

import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.vaadin.flow.components.Components;
import com.holonplatform.vaadin.flow.components.Input;
import com.holonplatform.vaadin.flow.components.PropertyListing;
import com.holonplatform.vaadin.flow.navigator.Navigator;
import com.myapp.iib.ui.MainLayout;
import com.myapp.iib.ui.components.navigation.bar.AppBar;
import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.dom.Element;
import com.myapp.iib.ui.components.FlexBoxLayout;
import com.myapp.iib.ui.components.ListItem;
import com.myapp.iib.ui.layout.size.Horizontal;
import com.myapp.iib.ui.layout.size.Top;
import com.myapp.iib.ui.layout.size.Vertical;
import com.myapp.iib.ui.util.LumoStyles;
import com.myapp.iib.ui.util.UIUtils;
import com.myapp.iib.ui.util.css.BoxSizing;
import org.vaadin.tabs.PagedTabs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyComponents {

    public static final String MIN_DESKTOP_WIDTH = "700px";
    public static final String MARGIN_RIGHT = "margin-right";
    public static final String AUTO = "auto";
    public static final int MIN_DESKTOP_COLUMNS = 4;
    public static final FormLayout.ResponsiveStep.LabelsPosition DEFAULT_POS = FormLayout.ResponsiveStep.LabelsPosition.TOP;
    public static final FormLayout.ResponsiveStep MOBILE_RS = getResponsiveStep("0", 1);
    public static final int DEFAULT_COL_SPAN = 2;
    public static final String LEFT_SMALL_PADDING = LumoStyles.Padding.Horizontal.S;
    public static final String RIGHT_SMALL_PADDING = LumoStyles.Padding.Right.S;
    public static final String CARD = "card";
    public static final String CONTAINER = "container";

    public static Button yesBtn() {
        return Components.button()
                .withThemeVariants(ButtonVariant.LUMO_PRIMARY)
                .text("Yes")
                .build();
    }

    public static Button noBtn() {
        return Components.button()
                .withThemeVariants(ButtonVariant.LUMO_CONTRAST)
                .text("No")
                .build();
    }

    public static HorizontalLayout dialogFooter(Button yes, Button no) {
        return Components.hl()
                .addAndAlign(yes, FlexComponent.Alignment.END)
                .addAndAlign(no, FlexComponent.Alignment.START)
                .fullWidth()
                .build();
    }

    public static boolean validateComponent(ComboBox component) {

        if (component.getValue() == null) {
            component.setInvalid(true);
            component.setPlaceholder("Cannot be empty");
            return false;
        } else {
            component.setInvalid(false);
            return true;
        }
    }

    public static Button refreshBtn() {
        return Components.button()
                .icon(VaadinIcon.REFRESH)
                .build();
    }

    public static Input<String> searchBox() {

        return Components.input.string()
//                .placeholder("Search")
//                .valueChangeMode(ValueChangeMode.LAZY)
                .prefixComponent(VaadinIcon.SEARCH.create())
                .clearButtonVisible(true)
                .blankValuesAsNull(true)
                .build();

    }

    public static Button searchBtn() {
        return Components.button().withThemeVariants(ButtonVariant.LUMO_PRIMARY)
                .text("Search")
                .build();
    }

    public static Button addBtn() {
        return Components.button().withThemeVariants(ButtonVariant.LUMO_PRIMARY)
                .text("Add")
                .icon(VaadinIcon.PLUS)
                .build();
    }

    public static FlexBoxLayout myFlexBoxLayout() {

        FlexBoxLayout content = new FlexBoxLayout();

        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
//        content.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setMargin(Horizontal.RESPONSIVE_L, Vertical.RESPONSIVE_L);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    public static Hr hr() {
        Hr hr = new Hr();
        hr.addClassName("solid");
        return hr;
    }

    public static FlexBoxLayout vlFlexBoxLayout(String title, Component... components) {

        FlexBoxLayout content = new FlexBoxLayout();
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.addClassNames(MyComponents.CARD,MyComponents.CONTAINER);
//        content.setSpacing(Vertical.XS);
        content.setHeightFull();
        content.add(UIUtils.createH3Label(title));
        content.add(hr());
        content.add(components);
//        content.getElement().removeAttribute("margin-top");
        return content;
    }

    public static FormLayout.ResponsiveStep getResponsiveStep(String minWidth, int columns) {
        return new FormLayout.ResponsiveStep(minWidth, columns,
                DEFAULT_POS);
    }

    public static FormLayout.ResponsiveStep getDefaultResponsiveStep() {
        return MOBILE_RS;
    }

    public static void successNotify(String msg) {
        Notification.show(msg).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public static List<FormLayout.ResponsiveStep> responsiveSteps() {

        List<FormLayout.ResponsiveStep> responsiveSteps = new ArrayList<>();
        responsiveSteps.add(MOBILE_RS);

        responsiveSteps.add(getResponsiveStep("21em", 2));

        responsiveSteps.add(getResponsiveStep(MIN_DESKTOP_WIDTH, MIN_DESKTOP_COLUMNS));

        return responsiveSteps;
    }

    public static ListItem divider() {
        ListItem divider = new ListItem("");
        divider.setDividerVisible(true);
        return divider;
    }

    public static void setPageTitle(String title) {
        UI.getCurrent().getPage().setTitle(title);
    }

    public static void setAppBarTitle(String title) {
        MainLayout.get().getAppBar().setTitle(title);
    }

    public static AppBar initAppBar(String title, Class<? extends Component> aClass) {

        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.getContextIcon().addClickListener(e -> UI.getCurrent().navigate(aClass));
        appBar.setTitle(title);
        return appBar;
    }

    public static AppBar initAppBar(String title) {

        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.getContextIcon().addClickListener(e -> Navigator.get().navigateBack());
        appBar.setTitle(title);
        return appBar;
    }

    public static Button trashButton() {
        return Components.button()
                .icon(VaadinIcon.TRASH)
                .withThemeName("small")
                .withThemeVariants(ButtonVariant.LUMO_ERROR)
                .build();
    }

    public static Button editButton() {
        return Components.button().icon(VaadinIcon.EDIT)
                .withThemeVariants(ButtonVariant.LUMO_SUCCESS)
                .withThemeName("small")
                .build()

                ;
    }

    public static void errorNotification(String text) {
        Notification.show(text).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }


    public static void goTo(Class aClass) {
//        Naviga
    }

    public static void write(Object o) {
        System.out.println(o);
    }

    public static String[] getDefaultPadding() {
        return new String[]{LumoStyles.Padding.Bottom.M,
                LEFT_SMALL_PADDING, LumoStyles.Padding.Top.S};
    }

    public static Label createH3Label(String text) {
        Label label = UIUtils.createH3Label(text);
        label.addClassName(MyComponents.LEFT_SMALL_PADDING);
        return label;
    }

    public static Component getCurrencyLabel() {
        return Components.label().text(MyUtils.getCurrencySymbol()).build();
    }

    public static Component editListingItem(PropertyListing listing, PropertyBox propertyBox) {
        Button editButton = MyComponents.editButton();
        editButton.addClickListener(buttonClickEvent -> listing.editItem(propertyBox));
        return editButton;
    }

    public static IronIcon saveIcon() { // Not Working
        IronIcon ironIcon = new IronIcon("icons", "save");
        ironIcon.getElement().setAttribute("icon", "icons:save");
        return ironIcon;
    }


    public static void addToCard(Element element, String... s) {
        element.getClassList().addAll(Arrays.asList(s.clone()));
    }

    public static Input<String> searchField(String placeHolder) {
        return Components.input.string()
                .placeholder(placeHolder)
                .prefixComponent(VaadinIcon.SEARCH.create())
                .clearButtonVisible(true)
                .blankValuesAsNull(true)
                .emptyValuesAsNull(true)
                .fullWidth()
                .build();
    }

    public static void addToTab(PagedTabs pagedTabs, String title, VerticalLayout verticalLayout, boolean selected) {

        pagedTabs.getSelectedTab().setSelected(false);

        Tab tab = pagedTabs.get(title);
        verticalLayout.setPadding(false);
verticalLayout.setSpacing(false);
        if (tab != null) {
            pagedTabs.select(tab);
            tab.removeAll();
            tab.add(verticalLayout);
        } else {
          tab =  pagedTabs.add(title,verticalLayout);
        }

        tab.setSelected(selected);


    }

    public static EnhancedDialog showEnhancedDialog(String title,Component component) {

        EnhancedDialog dialog = new EnhancedDialog();
        dialog.setHeader(title);
        dialog.setContent(component);
        dialog.setFooter(Components.hl()
                .justifyContentMode(FlexComponent.JustifyContentMode.END)
                .add(Components.button("Close",buttonClickEvent -> dialog.close())).build());
        dialog.setWidth("60%");
        dialog.setResizable(true);
        dialog.open();
        dialog.setCloseOnEsc(true);


        return dialog;
    }
}