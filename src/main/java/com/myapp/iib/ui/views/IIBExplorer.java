package com.myapp.iib.ui.views;

import com.holonplatform.core.Validator;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.vaadin.flow.components.*;
import com.holonplatform.vaadin.flow.navigator.annotations.OnShow;
import com.holonplatform.vaadin.flow.navigator.annotations.QueryParameter;
import com.ibm.broker.config.proxy.*;
import com.myapp.iib.admin.FlowUtils;
import com.myapp.iib.admin.wrapper.FlowUtilsWrapper;
import com.myapp.iib.model.*;
import com.myapp.iib.my.BrokerUtils;
import com.myapp.iib.my.MyComponents;
import com.myapp.iib.my.MyUtils;
import com.myapp.iib.ui.MainLayout;
import com.myapp.iib.ui.components.FlexBoxLayout;
import com.myapp.iib.ui.components.detailsdrawer.DetailsDrawer;
import com.myapp.iib.ui.components.detailsdrawer.DetailsDrawerHeader;
import com.myapp.iib.ui.layout.size.Horizontal;
import com.myapp.iib.ui.layout.size.Top;
import com.myapp.iib.ui.util.UIUtils;
import com.myapp.iib.ui.util.css.BoxSizing;
import com.myapp.iib.ui.util.css.Display;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tabs.PagedTabs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.myapp.iib.model.AbstractModel.NAME;
import static com.myapp.iib.model.AbstractModel.SNO;
import static com.myapp.iib.model.URLsUsedModel.URL;


@PageTitle("IIBExplorer")
@Route(value = "", layout = MainLayout.class)
@VaadinSessionScope
//@PreserveOnRefresh
public class IIBExplorer extends SplitViewFrame {

    private static final Logger logger = LoggerFactory.getLogger(IIBExplorer.class);


    private ValidatableSingleSelect<String> nodeComboBox;
    //    private SingleSelect<MessageFlowProxy> flowComboBox, searchMFComboBox;
    private SingleSelect<ExecutionGroupProxy> serverComboBox;
    //    private SingleSelect<SubFlowProxy> sfComboBox;
    private PropertyListing listing;
    private FlowUtils flowUtils;
    private Input<String> searchField;
    private List<PropertyBox> wrapperBox;

    private DetailsDrawer detailsDrawer;
    private DetailsDrawerHeader detailsDrawerHeader;

    @QueryParameter
    private String nodeName;
    private SplitLayout splitLayout;
    private PropertyListing udpListing;
    private PropertyInputForm wlmInputForm;
    private List<ExecutionGroupProxy> egsList;

    public IIBExplorer() {

        initialize();

        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());
        setViewDetailsPosition(Position.BOTTOM);
//        restoreNodeDetails();
    }

    private DetailsDrawer createDetailsDrawer() {
        detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);

        // Header
        detailsDrawerHeader = new DetailsDrawerHeader("");
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> detailsDrawer.hide());
        detailsDrawer.setHeader(detailsDrawerHeader);

        return detailsDrawer;
    }

    private void showMFDetails(PropertyBox propertyBox) {
        /*detailsDrawerHeader.setTitle(propertyBox.getValue(NAME));
        detailsDrawer.setContent(createMFDetails(propertyBox));
        detailsDrawer.show();*/

        MyComponents.showEnhancedDialog(propertyBox.getValue(NAME),createMFDetails(propertyBox));


    }

    private Component createMFDetails(PropertyBox propertyBox) {

        MessageFlowProxy mf = FlowUtilsWrapper.findMf(propertyBox, flowUtils);
        VerticalLayout container = new VerticalLayout();
        PagedTabs tabs = new PagedTabs(container);
        tabs.add("Properties", mfProperties(mf), false);
        tabs.add("ActivityLog", activityLog(mf), false);
        tabs.add("UDPs", udps(mf), false);
        tabs.add("WLM", wlm(mf), false);
        try {
            FlowUtilsWrapper.buildEventMonitoringConfigScreen(mf);
        } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException | IOException e) {
            e.printStackTrace();
            logger.error(e.getLocalizedMessage(),e);
        }
        return new Div(tabs, container);
    }

    private void showRestAPIDetails(PropertyBox propertyBox) {
        /*detailsDrawerHeader.setTitle(propertyBox.getValue(NAME));
        detailsDrawer.setContent(createRestAPIDetails(propertyBox));
        detailsDrawer.show();*/
        MyComponents.showEnhancedDialog(propertyBox.getValue(NAME),createRestAPIDetails(propertyBox));
    }

    private void showSFDetails(PropertyBox propertyBox) {
        /*detailsDrawerHeader.setTitle(propertyBox.getValue(NAME));
        detailsDrawer.setContent(createSFDetails(propertyBox));
        detailsDrawer.show();*/
        MyComponents.showEnhancedDialog(propertyBox.getValue(NAME),createSFDetails(propertyBox));
    }

    private Component createSFDetails(PropertyBox propertyBox) {
        VerticalLayout container = new VerticalLayout();
        PagedTabs tabs = new PagedTabs(container);
        SubFlowProxy subFlowProxy = FlowUtilsWrapper.findSf(propertyBox, flowUtils);
        tabs.add("Properties", sfProperties(subFlowProxy), false);
        return new Div(tabs, container);
    }

    private Component sfProperties(SubFlowProxy subFlowProxy) {

        Input<String> searchField = MyComponents.searchField("Search");
        HorizontalLayout horizontalLayout = Components.hl()
                .fullWidth()
                .addAndExpand(searchField, 1)
                .build();

        ListDataProvider<PropertyBox> dataProvider1 = new ListDataProvider<>(FlowUtilsWrapper
                .getSFProperties(subFlowProxy));

        dataProvider1.addFilter(pb -> MyUtils.addFilter(searchField, pb,
                MFProperties.NODE_NAME, MFProperties.NODE_TYPE)
        );

        PropertyListing listing = Components.listing.properties(MFProperties.PROPERTIES)
                .dataSource(dataProvider1)
                .columnReorderingAllowed(true)
                .resizable(true)
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .build();

        searchField.addValueChangeListener(stringValueChangeEvent -> listing.refresh());

        return Components.vl()
                .fullWidth()
                .add(MyComponents.hr())
                .add(horizontalLayout, listing.getComponent())
                .build();
    }

    private void showEGDetails(PropertyBox propertyBox) {
        /*detailsDrawerHeader.setTitle(propertyBox.getValue(NAME));
        detailsDrawer.setContent(createEGDetails(propertyBox));
        detailsDrawer.show();*/
        MyComponents.showEnhancedDialog(propertyBox.getValue(NAME),createEGDetails(propertyBox));
    }

    private Component createEGDetails(PropertyBox propertyBox) {

        VerticalLayout container = new VerticalLayout();
        PagedTabs tabs = new PagedTabs(container);
        ExecutionGroupProxy egProxy = flowUtils.egName(propertyBox.getValue(EGComponenetsModel.EG_NAME)).egProxy();
        if (egProxy != null) {
            tabs.add("Properties", egProperties(egProxy), false);
        } else {
            MyComponents.errorNotification("Click on a row that contains server name");
        }

        return new Div(tabs, container);
    }

    private Component egProperties(ExecutionGroupProxy egProxy) {

        Input<String> searchField = MyComponents.searchField("Search");
        HorizontalLayout horizontalLayout = Components.hl()
                .fullWidth()
                .addAndExpand(searchField, 1)
                .build();

        ListDataProvider<PropertyBox> dataProvider1 = new ListDataProvider<>(FlowUtilsWrapper
                .discoverEGProperties(egProxy));

        dataProvider1.addFilter(pb -> MyUtils.addFilter(searchField, pb,
                MFProperties.NODE_NAME, MFProperties.NODE_TYPE)
        );

        PropertyListing listing = Components.listing.properties(MFProperties.PROPERTIES)
                .dataSource(dataProvider1)
                .columnReorderingAllowed(true)
                .resizable(true)
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .build();

        searchField.addValueChangeListener(stringValueChangeEvent -> listing.refresh());

        return Components.vl()
                .fullWidth()
                .add(MyComponents.hr())
                .add(horizontalLayout, listing.getComponent())
                .build();
    }

    private Component createRestAPIDetails(PropertyBox propertyBox) {

        VerticalLayout container = new VerticalLayout();
        PagedTabs tabs = new PagedTabs(container);
        RestApiProxy restApiProxy = flowUtils.egName(propertyBox
                .getValue(RestApiModel.EG_NAME))
                .restAPIName(propertyBox.getValue(NAME))
                .restApiProxy();
        tabs.add("Properties", restAPIProperties(restApiProxy), false);
        return new Div(tabs, container);
    }

    private Component restAPIProperties(RestApiProxy restApiProxy) {

        Input<String> searchField = MyComponents.searchField("Search");
        HorizontalLayout horizontalLayout = Components.hl()
                .fullWidth()
                .addAndExpand(searchField, 1)
                .build();

        ListDataProvider<PropertyBox> dataProvider1 = new ListDataProvider<>(FlowUtilsWrapper
                .getRestAPIProperties(restApiProxy));

        dataProvider1.addFilter(pb -> MyUtils.addFilter(searchField, pb,
                MFProperties.NODE_NAME, MFProperties.NODE_TYPE)
        );

        PropertyListing listing = Components.listing.properties(MFProperties.PROPERTIES)
                .dataSource(dataProvider1)
                .columnReorderingAllowed(true)
                .resizable(true)
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .build();

        searchField.addValueChangeListener(stringValueChangeEvent -> listing.refresh());

        return Components.vl()
                .fullWidth()
                .add(MyComponents.hr())
                .add(horizontalLayout, listing.getComponent())
                .build();
    }


    private Component udps(MessageFlowProxy mf) {

        if (mf != null) {
            wrapperBox = new ArrayList<>(FlowUtilsWrapper.fetchUDPs(mf));
        } else {
            MyComponents.errorNotification("MessageFlowProxy is null");
            wrapperBox = new ArrayList<>(Collections.EMPTY_LIST);
        }

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);
        Input<String> udpValueFld = Components.input.string()
                .clearButtonVisible(true)
                .fullWidth()
                .build();

        udpListing = Components.listing.properties(UDPsUsedModel.PROPERTIES)
                .editor(UDPsUsedModel.UDP_PROP, udpValueFld)
                .dataSource(dataProvider)
//                .hidden(UDPsUsedModel.UUID)
                .hidden(NAME)
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
                                    wrapperBox.get(Integer.parseInt(propertyBox.getValue(UDPsUsedModel.SNO)) - 1).setValue(UDPsUsedModel.UDP_PROP, udpValueFld.getValue());
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
        return Components.vl()
                .fullWidth()
                .add(udpListing.getComponent())


                .build();

    }

    @SneakyThrows
    private Component wlm(MessageFlowProxy mf) {

        wlmInputForm = Components.input.form(WLMModel.PROPERTIES)
                .composer((formLayout1, propertyInputGroup) -> {
                    formLayout1.add(UIUtils.createH3Label("Work Load Management"));
                    formLayout1.add(new Hr());
                    propertyInputGroup.getBindings().forEach(propertyInputBinding ->
                            formLayout1.add(propertyInputBinding.getComponent()));

                })
                .initializer(formLayout1 -> formLayout1
                        .setResponsiveSteps(MyComponents.getResponsiveStep("480px", 1)))

                .build();

        wlmInputForm.setReadOnly(true);
        wlmInputForm.setValue(PropertyBox.builder(WLMModel.PROPERTIES)
                .set(WLMModel.NAME, mf.getName())
                .set(WLMModel.MAX_RATE, mf.getMaximumRateMsgsPerSec())
                .set(WLMModel.NOTIFY_THRESHOLD, mf.getNotificationThresholdMsgsPerSec())
                .set(WLMModel.ADDL_INS, mf.getAdditionalInstances())
                .build());

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
//                .width("50%")
                .addAndAlign(wlmInputForm.getComponent(), FlexComponent.Alignment.CENTER)
                .add(new Hr())
                .add(editLayout)
//                .styleNames(MyComponents.CARD, MyComponents.CONTAINER, LumoStyles.Padding.Top.L)
                .build();


    }

    private Component activityLog(MessageFlowProxy mf) {

        ValidatableInput<Integer> lastNHours = Components.input.number(Integer.class)
                .label("Filter by Last N hours")
//                .withValue(1)
                .validatable()
//                .withValidator(Validator.min(1))
                .clearButtonVisible(true)
                .build();

        SingleSelect<String> activityLogLogLevel = Components.input.singleSelect(String.class)
                .items("Info", "Error", "Warning")
                .label("Filter By Status")
                .clearButtonVisible(true)
                .build();

//        activityLogLogLevel.select("Info");


        Input<String> searchBox = MyComponents.searchBox();
        searchBox.hasPlaceholder().get().setPlaceholder("Search");

        Button refresh = MyComponents.refreshBtn();

        HorizontalLayout horizontalLayout = Components.hl()
                .spacing()
                .addAndExpand(searchBox, 1)
//                .add(activityLogLogLevel)
//                .add(lastNHours)
//                .add(refresh)
                .fullWidth()
                .alignItems(FlexComponent.Alignment.BASELINE)
                .build();

        wrapperBox = new ArrayList<>(FlowUtilsWrapper.activityLog(mf, lastNHours.getValue(), activityLogLogLevel.getValue()));
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);
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
                .sortUsing(ActivityLogModel.TIMESTAMP)
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

            List<PropertyBox> list = FlowUtilsWrapper.activityLog(mf, lastNHours.getValue(), activityLogLogLevel.getValue());
            if (!list.isEmpty()) {

//                wrapperBox.clear();
                wrapperBox.addAll(list);
                dataProvider.refreshAll();
//                wrapperBox = new ArrayList<>(list);
//                MyUtils.write(wrapperBox);
//                dataProvider.addDataProviderListener(dataChangeEvent -> dataChangeEvent.getSource().refreshAll());
                activityLogListing.refresh();
            } else {
                MyComponents.errorNotification("No Activity Log found for :" + lastNHours.getValue());
            }

        });

        return Components.vl()
                .fullWidth()
                .add(horizontalLayout, activityLogListing.getComponent())
                .build();
    }


    private Component mfProperties(MessageFlowProxy mf) {
        Input<String> searchField = MyComponents.searchField("Search");
        HorizontalLayout horizontalLayout = Components.hl()
                .fullWidth()
                .addAndExpand(searchField, 1)
                .build();
        ListDataProvider<PropertyBox> dataProvider;
        if (mf != null) {
            dataProvider = new ListDataProvider<>(FlowUtilsWrapper.getMFProperties(mf));
        } else {
            MyComponents.errorNotification("MessageFlowProxy is null");
            dataProvider = new ListDataProvider<>(Collections.EMPTY_LIST);
        }

        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                MFProperties.NODE_NAME, MFProperties.NODE_TYPE)
        );

        PropertyListing listing = Components.listing.properties(MFProperties.PROPERTIES)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .build();

        searchField.addValueChangeListener(stringValueChangeEvent -> listing.refresh());

        return Components.vl()
                .fullWidth()
                .add(horizontalLayout, listing.getComponent())
                .build()
                ;

    }

    @OnShow
    public void load() {
        if (nodeName != null) {
            nodeComboBox.setValue(nodeName);
        }

    }

    private void initialize() {
        egsList = new ArrayList<>(Collections.EMPTY_LIST);

        serverComboBox = Components.input.singleSelect(ExecutionGroupProxy.class)
                .clearButtonVisible(true)
                .label("Server to deploy")
                .fullWidth()
                .dataSource(DataProvider.ofCollection(egsList))
                .build();
        wrapperBox = new ArrayList<>();

        splitLayout = new SplitLayout();
        splitLayout.setHeightFull();

        splitLayout.addClassNames(MyComponents.CARD, MyComponents.CONTAINER);
        showMFs();
        splitLayout.addToSecondary(setRightPanel());
    }

    /*private void reset() {
        wrapperBox.clear();
        dataProvider.clearFilters();
        *//*if (listing != null) {
            splitLayout.remove(listing.getComponent());
        }*//*

    }*/

    /*private void restoreNodeDetails() {
        if (!nodeComboBox.isValid() && nodeName != null) {
            connect(nodeName);
        }
    }*/

    private Component createRow1() {

//        List<String> brokerFiles = BrokerUtils.brokerFiles();
//        ListDataProvider<String> brkrDataProvider = DataProvider.ofCollection(BrokerUtils.brokerFiles());

        nodeComboBox = Components.input.singleSelect(String.class)
                .dataSource(DataProvider.ofCollection(BrokerUtils.brokerFiles()))
                .label("Node")
                .itemCaptionGenerator(file -> StringUtils.substringBefore(file, "."))
                .withValueChangeListener(e -> connect(e.getValue()))
                .validatable()
                .withValidator(Validator.notEmpty())
                .required()
                .autofocus(true)
                .clearButtonVisible(true)
                .width("20%")
                .build();


//        brkrDataProvider.getItems().stream().findFirst().ifPresent(s -> nodeComboBox.setValue(s));

        searchField = MyComponents.searchField("Search");
        searchField.addValueChangeListener(stringValueChangeEvent -> {
            if (nodeComboBox.isValid()) {
                listing.refresh();
            }
        });

                /*Components.input.string()
                .placeholder("Search by Message Flow Name")
                .prefixComponent(VaadinIcon.SEARCH.create())
                .clearButtonVisible(true)
                .withValueChangeListener(stringValueChangeEvent -> {
                    if (nodeComboBox.isValid()) {
                        listing.refresh();
                    }
                })
                .blankValuesAsNull(true)
                .emptyValuesAsNull(true)
                .build();*/

        Button refreshBtn = Components.button()
                .icon(VaadinIcon.REFRESH)
                .withClickListener(buttonClickEvent -> nodeComboBox.getSelectedItem().ifPresent(file -> {
                    connect(file);
                }))
                .build();

        return Components.hl()
                .fullWidth()
                .spacing()
                .add(nodeComboBox)
                .addAndExpand(searchField, 1)
                .add(refreshBtn)
                .alignItems(FlexComponent.Alignment.BASELINE)
//                .withThemeName("mobile")
                .elementConfiguration(element -> MyComponents.addToCard(element, MyComponents.CARD, MyComponents.CONTAINER))
                .build();
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout();
        content.add(createRow1());
        content.add(splitLayout);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setDisplay(Display.BLOCK);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void showMFs() {

//        wrapperBox = new ArrayList<>();
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(flowUtils != null ?
                FlowUtilsWrapper.basicMFListing(flowUtils.allMFs()).collect(Collectors.toList()) : Collections.EMPTY_LIST);
        dataProvider.addFilter(propertyBox -> searchField.getValue() == null || StringUtils
                .containsIgnoreCase(propertyBox.getValue(FlowProxyModel.NAME), searchField.getValue()));

        listing = Components.listing.properties(FlowProxyModel.PROPERTIES)
                .dataSource(dataProvider)
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_WRAP_CELL_CONTENT)
                .componentRenderer(FlowProxyModel.STATUS, properties -> Components.span()
                        .withThemeName(properties.getValue(FlowProxyModel.STATUS) ? "badge success" : "badge error")
                        .description(BrokerUtils.getRunStatus(properties.getValue(FlowProxyModel.STATUS)))
                        .build())
                .withComponentColumn(this::showMFMoreAction)
                .header("Action")
                .add()
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullSize()
                .contextMenu()
                
                .add()
                .build();

        setSplitLayout(listing);
//        return splitLayout;
    }

    private Component showMFMoreAction(PropertyBox propertyBox) {
        Button button = Components.button().icon(VaadinIcon.ELLIPSIS_DOTS_V)
                .withThemeName("tertiary")

                .build();

        Components.contextMenu()
                .openOnClick(true)
                .withItem("Details", e -> showMFDetails(propertyBox))
                .withItem("Stop", e -> {
                    try {
                        FlowUtilsWrapper.findMf(propertyBox, flowUtils).stop();
                        UIUtils.showNotification("Stop command issued");
                    } catch (ConfigManagerProxyLoggedException er) {
                        MyComponents.errorNotification(er.getLocalizedMessage());
                    }
                })
                .withItem("Start", e -> {
                    try {
                        FlowUtilsWrapper.findMf(propertyBox, flowUtils).start();
                        UIUtils.showNotification("Start command issued");
                    } catch (ConfigManagerProxyLoggedException er) {
                        MyComponents.errorNotification(er.getLocalizedMessage());
                    }
                })
                .withItem("Stop forcefully", e -> {
                    try {
                        FlowUtilsWrapper.findMf(propertyBox, flowUtils).stop(BrokerProxy.StopMode.restartExecutionGroup);
                        UIUtils.showNotification("Force Stop command issued. This will restart the EG as well");
                    } catch (ConfigManagerProxyLoggedException er) {
                        MyComponents.errorNotification(er.getLocalizedMessage());
                    }
                })
                .withItem("Refresh flow", e -> {
                    try {
                        MessageFlowProxy mf = FlowUtilsWrapper.findMf(propertyBox, flowUtils);

                        if (mf.isRunning()) {
                            mf.stop();
                            Thread.sleep(30000);
                        }

                        mf.start();
                        UIUtils.showNotification("Flow has been restarted. Check administrative logs for more details");
                    } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException | InterruptedException er) {
                        MyComponents.errorNotification(er.getLocalizedMessage());
                    }
                })
                .withItem("Reload EG", e -> {
                    try {
                        ExecutionGroupProxy proxy = flowUtils.egName(propertyBox.getValue(MFProperties.EG_NAME)).egProxy();
                        proxy.stop();
                        Thread.sleep(30000);
                        proxy.start();
                        UIUtils.showNotification("Reload issue completed. Check administrative logs for further details");
                    } catch (ConfigManagerProxyLoggedException | InterruptedException er) {
                        MyComponents.errorNotification(er.getLocalizedMessage());
                    }
                })
                .withItem("Undeploy", e -> {
                    Components.dialog.question(b -> {
                        if (b) {
                            DeployResult result = FlowUtils.undeploy(FlowUtilsWrapper.findMf(propertyBox, flowUtils));
                            UIUtils.showNotification("UnDeployment status is "
                                    + result.getCompletionCode().toString());
                        }
                    }).text("Are you sure want to undeploy?")
                            .open();
                })
                .withItem("EnableMonitoring", e -> {
                    try {
                        FlowUtilsWrapper.findMf(propertyBox, flowUtils).setRuntimeProperty("This/monitoring", "active");
                        UIUtils.showNotification("EnableMonitoring command issued");
                    } catch (ConfigManagerProxyLoggedException er) {
                        MyComponents.errorNotification(er.getLocalizedMessage());
                    }
                })
                .withItem("DisableMonitoring", e -> {
                    try {
                        FlowUtilsWrapper.findMf(propertyBox, flowUtils)
                                .setRuntimeProperty("This/monitoring", "inactive");
                        UIUtils.showNotification("EnableMonitoring command issued");
                    } catch (ConfigManagerProxyLoggedException er) {
                        MyComponents.errorNotification(er.getLocalizedMessage());
                    }
                })
                .withItem("Deploy bar file")
                .onClick(e -> {
                    uploadComponent();
                })
                .add()
                .withItem("HTTP Port Details", e -> {
                    showHTTPPortDetails(propertyBox);
                })
                .build(button);
        
        
        return button;
    }

    @SneakyThrows
    private void showHTTPPortDetails(PropertyBox propertyBox) {

        ExecutionGroupProxy egProxy = flowUtils.egName(propertyBox.getValue(FlowProxyModel.EG_NAME))
                .egProxy();
        BrokerProxy bp = (BrokerProxy) egProxy.getParent();

        PropertyBox pb = PropertyBox.builder(HTTPModel.PROPERTIES)
                .set(HTTPModel.EG_HTTP, egProxy.getRuntimeProperty("HTTPConnector/port"))
                .set(HTTPModel.EG_HTTPS, egProxy.getRuntimeProperty("HTTPSConnector/port"))
                .set(HTTPModel.NODE_HTTP, bp.getHTTPListenerProperty("HTTPConnector/port"))
                .set(HTTPModel.NODE_HTTPS, bp.getHTTPListenerProperty("HTTPConnector/port"))
                .build();


        PropertyInputForm inputForm = Components.input.form(HTTPModel.PROPERTIES)
                .initializer(formLayout -> {
                    formLayout.setResponsiveSteps(MyComponents.MOBILE_RS);
                    formLayout.setSizeUndefined();
                })
                .withPostProcessor((property, input) -> input.setReadOnly(true))
                .build();

        inputForm.setValue(pb);
        Components.dialog
                .confirm()
                .width("25%")
                .withComponent(inputForm)
                .open();


    }

    @SneakyThrows
    private void showBarProperties(InputStream inputStream, String barFileName) {

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(FlowUtilsWrapper
                .readBar(IOUtils.toByteArray(inputStream), barFileName));

        listing = Components.listing.properties(BarModel.PROPERTIES)
                .dataSource(dataProvider)
                .frozenColumns(2)
                .readOnly(SNO,true)
                .fullHeight()
                .columnsAutoWidth()
                .columnReorderingAllowed(true)
                .withThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COLUMN_BORDERS)
                .editable()
                .editorBuffered(true)
                .withComponentColumn(propertyBox -> Components.button("Edit", buttonClickEvent ->
                        listing.editItem(propertyBox)))
                .editorComponent(propertyBox -> new Div(
                        Components.button("Save", buttonClickEvent -> {

                                    wrapperBox.get(propertyBox.getValue(BarModel.SNO) - 1)
                                            .setValue(BarModel.VALUE, propertyBox.getValue(BarModel.VALUE));
                                    UIUtils.showNotification("Property updated successfully");

                                    listing.refreshItem(propertyBox);
                                }
                        )
                        , Components.button("Cancel", buttonClickEvent ->
                        listing.cancelEditing())
                )).header("Action")
                .displayAsLast()
                .add()

                .build();

        setSplitLayout(listing);

    }

    private void uploadComponent() {

        Input<Boolean> edit_bar_file = Components.input
                .boolean_()
                .label("Edit Bar File")
                .build();

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        Upload upload = new Upload(memoryBuffer);
        upload.setDropAllowed(true);
//        upload.setAutoUpload(false);

        egsList.clear();
        egsList.addAll(flowUtils.allEGs());
        serverComboBox.refresh();

        Dialog dialog = Components.dialog
                .message()
                .draggable(true)
                .resizable(true)
                .withComponent(Components.vl()
                        .spacing()
                        .add(serverComboBox)
                        .add(edit_bar_file)
                        .add(upload)

                        .build())
                .open();

        upload.addSucceededListener(event -> {
            edit_bar_file.getValueIfPresent().ifPresent(aBoolean -> {
                if (aBoolean) {
                    showBarProperties(memoryBuffer.getInputStream(), event.getFileName());
                    dialog.close();
                }
            });
        });


    }

    private Component setRightPanel() {

        return Components.vl()
//                .spacing()
//                .withoutSpacing()
//                .withoutMargin()
//                .sizeUndefined()
                .add(Components.button("Message Flows", buttonClickEvent -> {
                        showMFs();
                }))
                .add(Components.button("Sub Flows", buttonClickEvent -> {
                    showSFs();
                }))
                .add(Components.button("Applications", buttonClickEvent -> {
                    showApplications();
                }))
                .add(Components.button("RestAPIs", buttonClickEvent -> {
                    showRestAPIs();
                }))
                .add(Components.button("Queues", buttonClickEvent -> {
                    showQueues();
                }))
                .add(Components.button("Bar Files", buttonClickEvent -> {
                    showBarFiles();
                }))
                .add(Components.button("DSNs", buttonClickEvent -> {
                    showDSNs();
                }))
                .add(Components.button("File Nodes", buttonClickEvent -> {
                    showFileNodes();
                }))
                .add(Components.button("UDPs", buttonClickEvent -> {
                    showUDPs();
                }))
                .add(Components.button("URLs", buttonClickEvent -> {
                    showURLs();
                }))
                .add(Components.button("Static Libraries", buttonClickEvent -> {
                    showStaticLibs();
                }))
                .add(Components.button("Shared Libraries", buttonClickEvent -> {
                    showSharedLibs();
                }))
                .add(Components.button("EGs", buttonClickEvent -> {
                    showEGs();
                }))
                .add(Components.button("ConfigServices", buttonClickEvent -> {
                    showConfigSvces();
                }))
                .add(Components.button("AdministrativeLogs", buttonClickEvent -> {
                    showAdminLogs();
                }))
                .add(Components.button("Node Properties", buttonClickEvent -> {
                    showNodeProperties();
                }))

                .alignItems(FlexComponent.Alignment.STRETCH)
                .build();


    }

    private void showSFs() {

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(flowUtils != null ?
                FlowUtilsWrapper.basicSFListing(flowUtils.allSubFlows()).collect(Collectors.toList())
                : Collections.EMPTY_LIST);


        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                FlowProxyModel.EG_NAME, NAME,
                FlowProxyModel.PARENT_NAME, FlowProxyModel.PARENT_TYPE));

        listing = Components.listing.properties(FlowProxyModel.PROPERTIES)
                .dataSource(dataProvider)

                .hidden(FlowProxyModel.STATUS)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .withSelectionListener(selectionEvent -> {
                    selectionEvent.getFirstSelectedItem().ifPresent(this::showSFDetails);
                })
                .build();

        setSplitLayout(listing);
    }

    private void showNodeProperties() {
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(FlowUtilsWrapper.discoverBrkProperties(flowUtils.brokerProxy()));
        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                BrokerPropertiesModel.NODE_NAME, BrokerPropertiesModel.NODE_TYPE));

        listing = Components.listing.properties(BrokerPropertiesModel.PROPERTIES)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)

                .build();

        setSplitLayout(listing);
    }

    private void showConfigSvces() {

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(FlowUtilsWrapper.configSvces(flowUtils.brokerProxy()));
        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox, ConfigSvcModel.CS_NAME
                , ConfigSvcModel.CS_TYPE
                , ConfigSvcModel.PROP_NAME
                , ConfigSvcModel.PROP_TYPE
        ));

        listing = Components.listing.properties(ConfigSvcModel.PROPERTIES)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)

                .build();

        setSplitLayout(listing);


    }

    private void showEGs() {

        wrapperBox = new ArrayList<>(FlowUtilsWrapper.showEGComponenets(flowUtils.allMFs()));
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);

        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                EGComponenetsModel.NAME, EGComponenetsModel.BAR_FILE, EGComponenetsModel.PARENT_NAME,
                EGComponenetsModel.EG_NAME, EGComponenetsModel.DEPLOYED_TIME)
        );

        listing = Components.listing.properties(EGComponenetsModel.SNO, EGComponenetsModel.EG_NAME, EGComponenetsModel.PID, EGComponenetsModel.NAME, EGComponenetsModel.DEPLOYED_TIME)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullSize()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .withSelectionListener(selectionEvent -> {
                    selectionEvent.getFirstSelectedItem().ifPresent(this::showEGDetails);
                })
                /*.itemDetailsVisibleOnClick(true)
                .itemDetailsComponent(propertyBox -> {
                    PropertyInputForm inputForm = Components.input.form(EGComponenetsModel.PARENT_NAME, EGComponenetsModel.PARENT_TYPE, EGComponenetsModel.BAR_FILE
                    )
                            .initializer(layout -> {
                                layout.setSizeFull();
                                layout.setResponsiveSteps(MyComponents.getResponsiveStep("480px", 2));
                            })
                            .withPostProcessor((property, input) -> input.setReadOnly(true))
                            .build();
                    inputForm.setValue(propertyBox);
                    return inputForm.getComponent();
                })*/
                .header(EGComponenetsModel.NAME, "Messsage Flow Name")
                .build();

        setSplitLayout(listing);
    }

    private void showAdminLogs() {

        ValidatableInput<Integer> lastNHours = Components.input.number(Integer.class)
                .label("Filter by Last N hours")
//                .withValue(1)
                .validatable()
//                .withValidator(Validator.min(1))
                .clearButtonVisible(true)
                .placeholder("Optional")
                .build();

        SingleSelect<String> logLevel = Components.input.singleSelect(String.class)
                .items("Info", "Error", "Warning")
                .label("Filter By Status")
                .clearButtonVisible(true)
                .placeholder("Optional")
                .build();

//        logLevel.select("Info");

        FormLayout formLayout = Components.formLayout()
                .add(UIUtils.createH3Label("Choose the filters"))
                .add(MyComponents.hr())
                .add(logLevel)
                .add(lastNHours)


                .responsiveSteps(MyComponents.getDefaultResponsiveStep())
                .build();

        Components.dialog.question(b -> {
            if (b && lastNHours.isValid()) {
                wrapperBox = new ArrayList<>(FlowUtilsWrapper.adminLog(flowUtils.brokerProxy(), lastNHours.getValue(),
                        logLevel.getValue()));
                ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);

                dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                        ActivityLogModel.DETAIL
                        )
                );

                listing = Components.listing.properties(ActivityLogModel.SNO, ActivityLogModel.TIMESTAMP,
                        ActivityLogModel.STATUS, ActivityLogModel.SOURCE, ActivityLogModel.DETAIL)
                        .dataSource(dataProvider)
                        .columnReorderingAllowed(true)
                        .resizable(true)
                        .fullHeight()
                        .flexGrow(ActivityLogModel.SNO, 0)
                        .flexGrow(ActivityLogModel.TIMESTAMP, 1)
                        .flexGrow(ActivityLogModel.STATUS, 0)
                        .flexGrow(ActivityLogModel.SOURCE, 0)
                        .flexGrow(ActivityLogModel.DETAIL, 2)
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

                setSplitLayout(listing);


                if (wrapperBox.isEmpty()) {
                    MyComponents.errorNotification("No logs found");
                }
            }
        })
                .withComponent(formLayout)
                .modal(true)
                .maxWidth("20%")
                .denialButtonConfigurator(baseButtonConfigurator -> {
                    baseButtonConfigurator.text("Cancel");
                })
                .confirmButtonConfigurator(baseButtonConfigurator -> {
                    baseButtonConfigurator.text("Search");
                })
                .open();
    }

    private void showSharedLibs() {
        wrapperBox = new ArrayList<>(FlowUtilsWrapper.sharedLibsUsedListing(flowUtils.allSharedLibs()).collect(Collectors.toList()));
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);

        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                LibsModel.NAME, LibsModel.EG_NAME, LibsModel.PARENT_NAME)

        );


        listing = Components.listing.properties(LibsModel.PROPERTIES)
                .dataSource(dataProvider)
//                .hidden(LibsModel.UUID)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .header(LibsModel.NAME, "Library Name")
                .build();

        setSplitLayout(listing);
    }

    private void showFileNodes() {

        wrapperBox = new ArrayList<>(FlowUtilsWrapper.filesNodesUsedListing(flowUtils.allMFs()));

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);

        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                FilesNodeModel.NAME, FilesNodeModel.NODE_NAME, FilesNodeModel.NODE_TYPE, FilesNodeModel.LOCAL_DIR, FilesNodeModel.PATTERN
                , FilesNodeModel.FTP, FilesNodeModel.FTP_TYPE, FilesNodeModel.FTP_USER, FilesNodeModel.FTP_DIR,
                FilesNodeModel.FTP_MODE, FilesNodeModel.FTP_SCAN_DELAY, FilesNodeModel.PARENT_NAME, FilesNodeModel.PARENT_TYPE, FilesNodeModel.EG_NAME)
        );


        listing = Components.listing.properties(FilesNodeModel.SNO, FilesNodeModel.NAME, FilesNodeModel.NODE_NAME,
                FilesNodeModel.NODE_TYPE, FilesNodeModel.LOCAL_DIR, FilesNodeModel.PATTERN)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .withItemClickListener(e -> {
                    PropertyBox propertyBox = e.getItem();
                    PropertyInputForm inputForm = Components.input.form(FilesNodeModel.FTP, FilesNodeModel.FTP_TYPE,
                            FilesNodeModel.FTP_USER, FilesNodeModel.FTP_DIR, FilesNodeModel.FTP_MODE,
                            FilesNodeModel.FTP_SCAN_DELAY, FilesNodeModel.PARENT_NAME, FilesNodeModel.PARENT_TYPE, FilesNodeModel.EG_NAME)
                            .initializer(layout -> {
                                layout.setWidthFull();
                            })
                            .withPostProcessor((property, input) -> input.setReadOnly(true))
                            .build();
                    inputForm.setValue(propertyBox);

                    MyComponents.showEnhancedDialog(propertyBox.getValue(NAME), inputForm.getComponent());

                })
                .header(NAME, "Messsage Flow Name")
                .build();

        setSplitLayout(listing);
    }

    private void showUDPs() {
        wrapperBox = new ArrayList<>(FlowUtilsWrapper.udpsUsedListing(flowUtils.allMFs()));
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);

        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                UDPsUsedModel.NAME, UDPsUsedModel.UDP_NAME, UDPsUsedModel.UDP_PROP, UDPsUsedModel.EG_NAME, UDPsUsedModel.PARENT_NAME)

        );


        listing = Components.listing.properties(UDPsUsedModel.PROPERTIES)
                .dataSource(dataProvider)
//                .hidden(UDPsUsedModel.UUID)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .header(NAME, "Messsage Flow Name")
                .build();

        setSplitLayout(listing);
    }

    private void showURLs() {
        wrapperBox = new ArrayList<>(FlowUtilsWrapper.urlsUsedListing(nodeComboBox.getValue(), flowUtils.allMFs()));

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);

        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                URLsUsedModel.NAME, URLsUsedModel.URL, URLsUsedModel.PROXY, URLsUsedModel.NODE_NAME, URLsUsedModel.NODE_TYPE, URLsUsedModel.PARENT_NAME, URLsUsedModel.EG_NAME)
        );


        listing = Components.listing.properties(SNO, NAME, URL)
                .dataSource(dataProvider)
                .styleNames(MyComponents.CARD, MyComponents.CONTAINER)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .withItemClickListener(e -> {
                    PropertyBox propertyBox = e.getItem();
                    PropertyInputForm inputForm = Components.input.form(URLsUsedModel.NODE_NAME, URLsUsedModel.NODE_TYPE,
                            URLsUsedModel.PROXY, URLsUsedModel.PARENT_NAME, URLsUsedModel.EG_NAME)
                            .initializer(layout -> {
                                layout.setWidthFull();
                                layout.setResponsiveSteps(MyComponents.getResponsiveStep("480px", 2));
                            })
                            .withPostProcessor((property, input) -> input.setReadOnly(true))
                            .build();
                    inputForm.setValue(propertyBox);
                    MyComponents.showEnhancedDialog(propertyBox.getValue(NAME), inputForm.getComponent());

                })
                .withComponentColumn(propertyBox -> {
                    boolean isInput = propertyBox.getValue(URLsUsedModel.NODE_TYPE).contains("Input");
                    return Components.span()
                            .text(isInput ? "Input" : "Request")
                            .withThemeName(isInput ? "badge primary" : "badge success primary")
                            .build();
                })
                .header("WS URL Type")
                .add()
                .header(URLsUsedModel.NAME, "Messsage Flow Name")
                .build();

        setSplitLayout(listing);
    }

    private void showStaticLibs() {
        wrapperBox = new ArrayList<>(FlowUtilsWrapper.staticLibsUsedListing(flowUtils.allStaticLibs()).collect(Collectors.toList()));

        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);

        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                LibsModel.NAME, LibsModel.EG_NAME, LibsModel.PARENT_NAME)

        );


        listing = Components.listing.properties(LibsModel.PROPERTIES)
                .dataSource(dataProvider)
//                .hidden(LibsModel.UUID)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .header(LibsModel.NAME, "Library Name")
                .build();

        setSplitLayout(listing);
    }


    private void setSplitLayout(PropertyListing listing) {
        splitLayout.setSplitterPosition(85);
        splitLayout.addToPrimary(listing.getComponent());
    }

    private void showApplications() {

        wrapperBox = new ArrayList<>(FlowUtilsWrapper.applnListing(flowUtils.allApplications()));
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);
        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                ApplnModel.NAME, ApplnModel.EG_NAME, ApplnModel.STATIC_LIB, ApplnModel.SHALED_LIB_DEPENDENTENTS, ApplnModel.SHALED_LIB_DEPENDENCIES)
        );

        listing = Components.listing.properties(ApplnModel.PROPERTIES)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .columnsAutoWidth()
                .componentRenderer(ApplnModel.STATUS, properties -> Components.span()
                        .withThemeName(properties.getValue(ApplnModel.STATUS) ? "badge success" : "badge error")
                        .description(BrokerUtils.getRunStatus(properties.getValue(ApplnModel.STATUS)))
                        .build())
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .withItemClickListener(e -> {
                    PropertyBox propertyBox = e.getItem();
                    PropertyInputForm inputForm = Components.input.form(ApplnModel.EG_NAME)
                            .initializer(layout -> {
                                layout.setWidthFull();
                            })
                            .withPostProcessor((property, input) -> input.setReadOnly(true))
                            .build();
                    inputForm.setValue(propertyBox);
                    MyComponents.showEnhancedDialog(propertyBox.getValue(NAME), inputForm.getComponent());
                })
                .header(ApplnModel.NAME, "Application Name")
                .build();


        setSplitLayout(listing);


    }

    private void showRestAPIs() {

//        listing.getDataProvider().refreshAll();
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(FlowUtilsWrapper.restAPIsListing(flowUtils.allRestAPIs()));
        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                RestApiModel.NAME, RestApiModel.EG_NAME, RestApiModel.LOCAL_DEF_URL,
                RestApiModel.LOCAL_BASE_URL, RestApiModel.BASE_URL, RestApiModel.DEF_URL)
        );

        listing = Components.listing.properties(RestApiModel.PROPERTIES)
//                .hidden(RestApiModel.UUID)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_WRAP_CELL_CONTENT)
                .withSelectionListener(selectionEvent -> {
                    selectionEvent.getFirstSelectedItem().ifPresent(this::showRestAPIDetails);
                })
                .build();

        setSplitLayout(listing);
    }

    private void showQueues() {

        wrapperBox = new ArrayList<>(FlowUtilsWrapper.queuesUsedListing(flowUtils.allMFs()));
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);
        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                QueuesUsedModel.NAME, QueuesUsedModel.EG_NAME, QueuesUsedModel.PARENT_NAME, QueuesUsedModel.QUEUE_NAME)
        );

        listing = Components.listing.properties(QueuesUsedModel.PROPERTIES)
                .dataSource(dataProvider)
//                .hidden(QueuesUsedModel.UUID)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .flexGrow(QueuesUsedModel.SNO, 0)
                .flexGrow(QueuesUsedModel.NAME, 1)
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .header(QueuesUsedModel.NAME, "Messsage Flow Name")
                .build();

        setSplitLayout(listing);
    }

    private void showBarFiles() {
        wrapperBox = new ArrayList<>(FlowUtilsWrapper.showEGComponenets(flowUtils.allMFs()));
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);
        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                EGComponenetsModel.NAME, EGComponenetsModel.BAR_FILE, EGComponenetsModel.PARENT_NAME,
                EGComponenetsModel.EG_NAME, EGComponenetsModel.DEPLOYED_TIME));


        listing = Components.listing.properties(EGComponenetsModel.SNO, EGComponenetsModel.EG_NAME, EGComponenetsModel.NAME, EGComponenetsModel.BAR_FILE)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullSize()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .withItemClickListener(e -> {
                    PropertyBox propertyBox = e.getItem();
                    PropertyInputForm inputForm = Components.input.form(EGComponenetsModel.PARENT_NAME, EGComponenetsModel.PARENT_TYPE, EGComponenetsModel.DEPLOYED_TIME)
                            .initializer(layout -> {
                                layout.setSizeFull();
                                layout.setResponsiveSteps(MyComponents.getResponsiveStep("480px", 2));
                            })
                            .withPostProcessor((property, input) -> input.setReadOnly(true))
                            .build();
                    inputForm.setValue(propertyBox);
                    MyComponents.showEnhancedDialog(propertyBox.getValue(NAME), inputForm.getComponent());
                })
                .header(NAME, "Messsage Flow Name")
                .build();
        setSplitLayout(listing);
    }

    private void showDSNs() {

        wrapperBox = new ArrayList<>(FlowUtilsWrapper.dsnsUsedListing(flowUtils.allMFs()));
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);

        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                DSNModel.NAME, DSNModel.DSN, DSNModel.NODE_NAME, DSNModel.NODE_TYPE, DSNModel.PARENT_NAME, DSNModel.EG_NAME)
        );


        listing = Components.listing.properties(DSNModel.SNO, DSNModel.NAME, DSNModel.DSN, DSNModel.NODE_NAME, DSNModel.NODE_TYPE)
                .dataSource(dataProvider)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullHeight()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .withItemClickListener(e -> {
                    PropertyBox propertyBox = e.getItem();
                    PropertyInputForm inputForm = Components.input.form(DSNModel.PARENT_NAME, DSNModel.EG_NAME)
                            .initializer(layout -> {
                                layout.setWidthFull();
                            })
                            .withPostProcessor((property, input) -> input.setReadOnly(true))
                            .build();
                    inputForm.setValue(propertyBox);
                    MyComponents.showEnhancedDialog(propertyBox.getValue(NAME), inputForm.getComponent());

                })
                .header(NAME, "Messsage Flow Name")
                .build();

        setSplitLayout(listing);
    }

    /*private void viewDetails(PropertyBox propertyBox) {
        Navigator.get()
                .navigation(MessageFlowDetails.class)
                .withQueryParameter("node", nodeComboBox.getValue())
                .withQueryParameter("egName", propertyBox.getValue(FlowProxyModel.EG_NAME))
                .withQueryParameter("parentType", propertyBox.getValue(FlowProxyModel.PARENT_TYPE))
                .withQueryParameter("parentName", propertyBox.getValue(FlowProxyModel.PARENT_NAME))
                .withQueryParameter("mfName", propertyBox.getValue(FlowProxyModel.NAME))
                .navigate();

//                .navigateTo(MessageFlowDetails.class,propertyBox.getValue(FlowProxyModel.UUID));
    }*/

    private void connect(String brokerFile) {

        if (flowUtils != null) {
            flowUtils.disconnect();
        }


        if (nodeComboBox.isValid()) {

            if (!StringUtils.endsWith(brokerFile, "broker")) {
                brokerFile += ".broker";
            }

//            brokerFile = StringUtils.endsWith(brokerFile, "broker") ? "" : ".broker";


            flowUtils = new FlowUtils()
                    .brokerFile(BrokerUtils.prepBrokerFile(brokerFile))
                    .connect()
            ;

            showMFs();

//            Context.get().classLoaderScope().ifPresent(contextScope -> contextScope.put("flowUtils",flowUtils));

          /*  wrapperBox.clear();
//            nodeName = brokerFile;

            wrapperBox.addAll(FlowUtilsWrapper.basicMFListing(flowUtils.allMFs()).collect(Collectors.toList()));
            listing.refresh();*/
        }
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
    }


}
