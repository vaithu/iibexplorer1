package com.myapp.iib.ui.views;

import com.holonplatform.core.Validator;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.vaadin.flow.components.*;
import com.holonplatform.vaadin.flow.navigator.Navigator;
import com.holonplatform.vaadin.flow.navigator.annotations.OnShow;
import com.holonplatform.vaadin.flow.navigator.annotations.QueryParameter;
import com.myapp.iib.ui.MainLayout;
import com.myapp.iib.ui.components.navigation.bar.AppBar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.myapp.iib.admin.FlowUtils;
import com.myapp.iib.model.ActivityLogModel;
import com.myapp.iib.my.BrokerUtils;
import com.myapp.iib.my.MyComponents;
import com.myapp.iib.my.MyUtils;
import com.myapp.iib.ui.components.FlexBoxLayout;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PageTitle("AdministrativeLog")
@Route(value = "AdministrativeLog", layout = MainLayout.class)
public class AdministrativeLogs extends ViewFrame {


    @QueryParameter
    private String node;


    private List<PropertyBox> activityLogBox;
    private ValidatableInput<Date> filterByDate;
    private SingleSelect<String> logLevel;
    private FlowUtils flowUtils;


    @PostConstruct
    public void init() {

        activityLogBox = new ArrayList<>();
        setViewContent(createContent());

    }

    private Component createContent() {
        FlexBoxLayout layout = MyComponents.myFlexBoxLayout();
        layout.add(adminLog());
        layout.setHeightFull();
        return layout;
    }

    @OnShow
    public void load() {
        MyComponents.setAppBarTitle(node);

        flowUtils = new FlowUtils()
                .brokerFile(BrokerUtils.prepBrokerFile(node))
                .connect();

//        activityLogBox.addAll(FlowUtilsWrapper.adminLog(flowUtils.brokerProxy(), filterByDate.getValue(),logLevel.getValue()));

    }

    private Component adminLog() {


        filterByDate = Components.input.date()
                .withValue(new Date())
                .label("Filter By Date")
                .validatable()
                .withValidator(Validator.lessOrEqual(new Date()))
                .withValidator(Validator.notNull())
                .clearButtonVisible(true)
                .build();

        logLevel = Components.input.singleSelect(String.class)
                .items("Info", "Error", "Warning")
                .label("Filter By Status")
                .clearButtonVisible(true)
                .build();

//        logLevel.select("Info");


        Input<String> searchBox = MyComponents.searchBox();
        searchBox.hasPlaceholder().get().setPlaceholder("Search");

        Button refresh = MyComponents.refreshBtn();

        HorizontalLayout horizontalLayout = Components.hl()
                .spacing()
                .addAndExpand(searchBox, 1)
                .add(logLevel)
                .add(filterByDate)
                .add(refresh)
                .fullWidth()
                .alignItems(FlexComponent.Alignment.BASELINE)
                .build();

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(activityLogBox);
        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchBox, propertyBox,
                ActivityLogModel.DETAIL
                )
        );

        PropertyListing listing = Components.listing.properties(ActivityLogModel.SNO, ActivityLogModel.TIMESTAMP,
                ActivityLogModel.STATUS,ActivityLogModel.SOURCE, ActivityLogModel.DETAIL)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .flexGrow(ActivityLogModel.SNO, 0)
                .flexGrow(ActivityLogModel.TIMESTAMP, 1)
                .flexGrow(ActivityLogModel.STATUS, 0)
                .flexGrow(ActivityLogModel.SOURCE, 0)
                .flexGrow(ActivityLogModel.DETAIL, 2)
//                .header(ActivityLogModel.IS_ERROR,"Status")
//                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_WRAP_CELL_CONTENT)
                .componentRenderer(ActivityLogModel.TIMESTAMP, propertyBox ->
                        new Label(MyUtils.dateToLocalDateTime(propertyBox.getValue(ActivityLogModel.TIMESTAMP))))
                .componentRenderer(ActivityLogModel.STATUS, BrokerUtils::formatLogStatus)
                .itemDetailsVisibleOnClick(true)
                .itemDetailsComponent(propertyBox -> {
                    PropertyInputForm inputForm = Components.input.form(ActivityLogModel.TAGS, ActivityLogModel.INSERTS)
                            .initializer(HasSize::setWidthFull)
                            .withPostProcessor((property, input) -> input.setReadOnly(true))
                            .build();
                    inputForm.setValue(propertyBox);
                    return inputForm.getComponent();
                })
                .build();

        searchBox.addValueChangeListener(stringValueChangeEvent -> listing.refresh());

        refresh.addClickListener(buttonClickEvent -> {

            if (filterByDate.isValid()) {

//                List<PropertyBox> list = FlowUtilsWrapper.adminLog(flowUtils.brokerProxy(), filterByDate.getValue(),logLevel.getValue());
                /*if (!list.isEmpty()) {
                    activityLogBox.clear();
                    activityLogBox.addAll(list);
                    listing.refresh();
                }else {
                    MyComponents.errorNotification("No Admin Log found for :"+ filterByDate.getValue());
                }*/

            }
        });

        return MyComponents.vlFlexBoxLayout("AdministrativeLog", horizontalLayout, listing.getComponent());


    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
    }

    private AppBar initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.getContextIcon().addClickListener(e -> Navigator.get()
                .navigation(IIBExplorer.class)
                .withQueryParameter("nodeName", StringUtils.substringBefore(node, "."))
                .navigate()
        );
        appBar.setTitle(node);
        return appBar;
    }
}
