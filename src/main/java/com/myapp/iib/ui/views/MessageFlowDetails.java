package com.myapp.iib.ui.views;

import com.holonplatform.core.Validator;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.vaadin.flow.components.*;
import com.holonplatform.vaadin.flow.navigator.Navigator;
import com.holonplatform.vaadin.flow.navigator.annotations.OnShow;
import com.holonplatform.vaadin.flow.navigator.annotations.QueryParameter;
import com.ibm.broker.config.proxy.MessageFlowProxy;
import com.myapp.iib.model.UDPsUsedModel;
import com.myapp.iib.my.BrokerUtils;
import com.myapp.iib.my.MyComponents;
import com.myapp.iib.my.MyUtils;
import com.myapp.iib.ui.MainLayout;
import com.myapp.iib.ui.components.navigation.bar.AppBar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.myapp.iib.admin.FlowUtils;
import com.myapp.iib.admin.wrapper.FlowUtilsWrapper;
import com.myapp.iib.model.ActivityLogModel;
import com.myapp.iib.model.MFProperties;
import com.myapp.iib.model.WLMModel;
import com.myapp.iib.ui.components.FlexBoxLayout;
import com.myapp.iib.ui.layout.size.Vertical;
import com.myapp.iib.ui.util.LumoStyles;
import com.myapp.iib.ui.util.UIUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.myapp.iib.model.BrokerModel.NODE_NAME;
import static com.myapp.iib.model.BrokerModel.NODE_TYPE;

@PageTitle("MessageFlow Details")
@Route(value = "mf-details", layout = MainLayout.class)
public class MessageFlowDetails extends ViewFrame {

    private MessageFlowProxy mf;

    private FlowUtils flowUtils;

    @QueryParameter
    private String node;

    @QueryParameter
    private String egName;

    @QueryParameter
    private String parentType;

    @QueryParameter
    private String parentName;

    @QueryParameter
    private String mfName;
    private PropertyInputForm wlmInputForm;
    private PropertyListing udpListing;
    private List<PropertyBox> udpWrapperBox;
    private List<PropertyBox> activityLogBox;
    private List<PropertyBox> adminLogBox;
    private List<PropertyBox> mfPropertiesBox;
    private ValidatableInput<Date> activityLogFilterByDate;
    private SingleSelect<String> activityLogLogLevel;
    private ValidatableInput<Date> adminLogFilterByDate;
    private SingleSelect<String> adminLogLevel;

    @PostConstruct
    public void init() {
        udpWrapperBox = new ArrayList<>();
        activityLogBox = new ArrayList<>();
        adminLogBox = new ArrayList<>();
        mfPropertiesBox = new ArrayList<>();
//        flowUtils = (FlowUtils) MyUtils.getClassFromContext(FlowUtils.class);

        setViewContent(createContent());
    }

    private Component createContent() {
        FlexBoxLayout layout = MyComponents.myFlexBoxLayout();
//        layout.add(createRow1());
//        layout.add(createListing());
        layout.setSpacing(Vertical.XL);
        layout.add(mfProperties());
        layout.add(activityLog());
        layout.add(adminLog());
        layout.add(udps());
        layout.add(wlm());
        layout.setHeightFull();
        return layout;
    }

    @OnShow
    public void load() {
        MyComponents.setAppBarTitle(mfName);
        flowUtils = new FlowUtils()
                .brokerFile(BrokerUtils.prepBrokerFile(node))
                .connect();

        switch (parentType) {
            case "Application":
                mf = flowUtils.egName(egName)
                        .applicationName(parentName)
                        .mfName(mfName)
                        .getMFlowProxyFromApp();
            case "RestAPI":
                mf = flowUtils.egName(egName)
                        .restAPIName(parentName)
                        .mfName(mfName)
                        .getMFlowProxyFromApp();
            case "Static Libray":
                mf = flowUtils.egName(egName)
                        .staticLibName(parentName)
                        .mfName(mfName)
                        .getMFlowProxyFromApp();
        }


        fetchWlm();
        fetchUDPs();
        fetchActivityLog();
        fetchAdminLog();
        fetchMFProperties();
    }

    @SneakyThrows
    private void fetchMFProperties() {
        mfPropertiesBox.addAll(FlowUtilsWrapper.getMFProperties(mf));
        FlowUtils.discoverNodeConnections(mf);
    }

    private void fetchActivityLog() {
//        activityLogBox.addAll(FlowUtilsWrapper.activityLog(mf, activityLogFilterByDate.getValue(), activityLogLogLevel.getValue()));
    }

    private void fetchAdminLog() {
        activityLogBox.addAll(FlowUtilsWrapper.adminLog(flowUtils.brokerProxy(), mf, adminLogFilterByDate.getValue(), adminLogLevel.getValue()));
    }

    private void fetchUDPs() {

        udpWrapperBox.addAll(FlowUtilsWrapper.fetchUDPs(mf));
        udpListing.refresh();
    }

    @SneakyThrows
    private void fetchWlm() {
        wlmInputForm.setValue(PropertyBox.builder(WLMModel.PROPERTIES)
                .set(WLMModel.NAME, mf.getName())
                .set(WLMModel.MAX_RATE, mf.getMaximumRateMsgsPerSec())
                .set(WLMModel.NOTIFY_THRESHOLD, mf.getNotificationThresholdMsgsPerSec())
                .set(WLMModel.ADDL_INS, mf.getAdditionalInstances())
                .build());
    }

    @SneakyThrows
    private Component wlm() {

        FormLayout formLayout = new FormLayout();

        wlmInputForm = Components.input.form(WLMModel.PROPERTIES)
                .composer((formLayout1, propertyInputGroup) -> {
                    formLayout1.add(UIUtils.createH3Label("Work Load Management"));
                    formLayout1.add(new Hr());
                    propertyInputGroup.getBindings().forEach(propertyInputBinding -> formLayout1.add(propertyInputBinding.getComponent()));

                })
                .initializer(formLayout1 -> formLayout1.setResponsiveSteps(MyComponents.getResponsiveStep("480px", 1)))

                .build();

        wlmInputForm.setReadOnly(true);

        HorizontalLayout editLayout = Components.hl()
                .fullWidth()
                .spacing()
                .justifyContentMode(FlexComponent.JustifyContentMode.END)
                .add(Components.button("Edit", buttonClickEvent -> wlmInputForm.setReadOnly(false)))
                .add(Components.button("Submit", buttonClickEvent -> {
                    FlowUtilsWrapper.updateWLM(mf, wlmInputForm.getValueIfPresent());
                    UIUtils.showNotification("Changes updated successfully");
                    wlmInputForm.setReadOnly(true);
                }))
                .build();

        return Components.vl()
                .spacing()
                .width("50%")
                .addAndAlign(wlmInputForm.getComponent(), FlexComponent.Alignment.CENTER)
                .add(new Hr())
                .add(editLayout)
                .styleNames(MyComponents.CARD, MyComponents.CONTAINER, LumoStyles.Padding.Top.L)
                .build();


    }

    private Component udps() {

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(udpWrapperBox);

        Input<String> udpValueFld = Components.input.string()
                .clearButtonVisible(true)
                .fullWidth()
                .build();

        udpListing = Components.listing.properties(UDPsUsedModel.PROPERTIES)
                .editor(UDPsUsedModel.UDP_PROP, udpValueFld)
                .dataSource(dataProvider)
//                .hidden(UDPsUsedModel.UUID)
                .hidden(UDPsUsedModel.NAME)
                .hidden(UDPsUsedModel.FLOW_TYPE)
                .hidden(UDPsUsedModel.PARENT_NAME)
                .hidden(UDPsUsedModel.EG_NAME)
                .readOnly(UDPsUsedModel.SNO, true)
                .readOnly(UDPsUsedModel.UDP_NAME, true)
//                .fullSize()
                .flexGrow(UDPsUsedModel.SNO, 0)
                .flexGrow(UDPsUsedModel.UDP_NAME, 1)
                .flexGrow(UDPsUsedModel.UDP_PROP, 3)
                .editable()
                .editorBuffered(true)
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .columnReorderingAllowed(true)
                .resizable(true)
                .withComponentColumn(propertyBox -> Components.button("Edit", buttonClickEvent ->
                        udpListing.editItem(propertyBox)))
                .editorComponent(propertyBox -> new Div(
                        Components.button("Save", buttonClickEvent -> {
                                    FlowUtils.updateUDP(mf, propertyBox.getValue(UDPsUsedModel.UDP_NAME), udpValueFld.getValue());
                                    udpWrapperBox.get(Integer.parseInt(propertyBox.getValue(UDPsUsedModel.SNO)) - 1).setValue(UDPsUsedModel.UDP_PROP, udpValueFld.getValue());
                                    UIUtils.showNotification("Property updated successfully");

                                    udpListing.refreshItem(propertyBox);
                                }
                        )
                        , Components.button("Cancel", buttonClickEvent ->
                        udpListing.cancelEditing())


                )).header("Action")
                .displayAsLast()
                .add()
                .build();
        return MyComponents.vlFlexBoxLayout("User Defined Properties", udpListing.getComponent());

    }

    private Component activityLog() {


        activityLogFilterByDate = Components.input.date()
                .withValue(new Date())
                .label("Filter By Date")
                .validatable()
                .withValidator(Validator.lessOrEqual(new Date()))
                .withValidator(Validator.notNull())
                .clearButtonVisible(true)
                .build();

        activityLogLogLevel = Components.input.singleSelect(String.class)
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
                .add(activityLogLogLevel)
                .add(activityLogFilterByDate)
                .add(refresh)
                .fullWidth()
                .alignItems(FlexComponent.Alignment.BASELINE)
                .build();

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(activityLogBox);
        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchBox, propertyBox,
                ActivityLogModel.DETAIL
                )
        );

        PropertyListing activityLogListing = Components.listing.properties(ActivityLogModel.SNO, ActivityLogModel.TIMESTAMP,
                ActivityLogModel.STATUS, ActivityLogModel.SOURCE, ActivityLogModel.DETAIL)
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

        searchBox.addValueChangeListener(stringValueChangeEvent -> activityLogListing.refresh());

        refresh.addClickListener(buttonClickEvent -> {

            if (activityLogFilterByDate.isValid()) {

              /*  List<PropertyBox> list = FlowUtilsWrapper.activityLog(mf, activityLogFilterByDate.getValue(), activityLogLogLevel.getValue());
                if (!list.isEmpty()) {
                    activityLogBox.clear();
                    activityLogBox.addAll(list);
                    activityLogListing.refresh();
                } else {
                    MyComponents.errorNotification("No Activity Log found for :" + activityLogFilterByDate.getValue());
                }*/

            }
        });

        return MyComponents.vlFlexBoxLayout("Activity Log", horizontalLayout, activityLogListing.getComponent());
    }

    private Component adminLog() {


        adminLogFilterByDate = Components.input.date()
                .withValue(new Date())
                .label("Filter By Date")
                .validatable()
                .withValidator(Validator.lessOrEqual(new Date()))
                .withValidator(Validator.notNull())
                .clearButtonVisible(true)
                .build();

        adminLogLevel = Components.input.singleSelect(String.class)
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
                .add(activityLogLogLevel)
                .add(adminLogFilterByDate)
                .add(refresh)
                .fullWidth()
                .alignItems(FlexComponent.Alignment.BASELINE)
                .build();

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(adminLogBox);
        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchBox, propertyBox,
                ActivityLogModel.DETAIL
                )
        );

        PropertyListing listing = Components.listing.properties(ActivityLogModel.SNO, ActivityLogModel.TIMESTAMP,
                ActivityLogModel.STATUS, ActivityLogModel.SOURCE, ActivityLogModel.DETAIL)
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

            if (adminLogFilterByDate.isValid()) {

                /*List<PropertyBox> list = FlowUtilsWrapper.adminLog(flowUtils.brokerProxy(), adminLogFilterByDate.getValue(), activityLogLogLevel.getValue());
                if (!list.isEmpty()) {
                    adminLogBox.clear();
                    adminLogBox.addAll(list);
                    listing.refresh();
                } else {
                    MyComponents.errorNotification("No Admin Log found for :" + adminLogFilterByDate.getValue());
                }*/

            }
        });

        return MyComponents.vlFlexBoxLayout("AdministrativeLog", horizontalLayout, listing.getComponent());


    }

    private Component mfProperties() {
        Input<String> searchField = MyComponents.searchField("Search");
        HorizontalLayout horizontalLayout = Components.hl()
                .fullWidth()
                .addAndExpand(searchField, 1)
                .build();

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(mfPropertiesBox);

        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                NODE_NAME,NODE_TYPE)
        );

        PropertyListing listing = Components.listing.properties(MFProperties.PROPERTIES)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .build();

        searchField.addValueChangeListener(stringValueChangeEvent -> listing.refresh());

        /*FlexBoxLayout content = new FlexBoxLayout();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setDisplay(Display.BLOCK);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        content.add(horizontalLayout,listing.getComponent());*/

        return MyComponents.vlFlexBoxLayout("Message Flow Properties", horizontalLayout, listing.getComponent());
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();

//        MyComponents.initAppBar(mfName);
//        BrokerUtils.initAppBar(mfName,node);
    }

    private AppBar initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.getContextIcon().addClickListener(e -> Navigator.get()
                .navigation(IIBExplorer.class)
                .withQueryParameter("nodeName", StringUtils.substringBefore(node, "."))
                .navigate()
        );
        appBar.setTitle(mfName);
        return appBar;
    }


}
