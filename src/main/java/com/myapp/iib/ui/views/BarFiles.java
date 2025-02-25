package com.myapp.iib.ui.views;

import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.vaadin.flow.components.Components;
import com.holonplatform.vaadin.flow.components.Input;
import com.holonplatform.vaadin.flow.components.PropertyInputForm;
import com.holonplatform.vaadin.flow.components.PropertyListing;
import com.holonplatform.vaadin.flow.navigator.Navigator;
import com.holonplatform.vaadin.flow.navigator.annotations.OnShow;
import com.holonplatform.vaadin.flow.navigator.annotations.QueryParameter;
import com.myapp.iib.ui.MainLayout;
import com.myapp.iib.ui.components.navigation.bar.AppBar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.myapp.iib.admin.FlowUtils;
import com.myapp.iib.admin.wrapper.FlowUtilsWrapper;
import com.myapp.iib.my.BrokerUtils;
import com.myapp.iib.my.MyComponents;
import com.myapp.iib.my.MyUtils;
import com.myapp.iib.ui.components.FlexBoxLayout;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.myapp.iib.model.EGComponenetsModel.*;

@PageTitle("Bar Files")
@Route(value = "barfiles", layout = MainLayout.class)
public class BarFiles extends ViewFrame {


    @QueryParameter
    private String node;

    private PropertyListing listing;

    private List<PropertyBox> wrapperBox;
    private Input<String> searchField;


    @PostConstruct
    public void init() {

        wrapperBox = new ArrayList<>();
        setViewContent(createContent());

    }

    private Component createContent() {
        FlexBoxLayout layout = MyComponents.myFlexBoxLayout();
        layout.add(createRow1());
        layout.add(createListing());
        layout.setHeightFull();
        return layout;
    }

    @OnShow
    public void load() {
        MyComponents.setAppBarTitle(node);
        FlowUtils flowUtils = new FlowUtils()
                .brokerFile(BrokerUtils.prepBrokerFile(node))
                .connect();

        wrapperBox.addAll(FlowUtilsWrapper.showEGComponenets(flowUtils.allMFs()));
        listing.refresh();

    }

    private Component createRow1() {
        searchField = MyComponents.searchField("Search");
        searchField.addValueChangeListener(stringValueChangeEvent -> listing.refresh());

        return Components.hl()
                .fullWidth()
                .addAndExpand(searchField, 1)
                .build();
    }

    private Component createListing() {

        wrapperBox = new ArrayList<>();
        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(wrapperBox);

        dataProvider.addFilter(propertyBox -> MyUtils.addFilter(searchField, propertyBox,
                NAME,BAR_FILE,PARENT_NAME,EG_NAME,DEPLOYED_TIME)
        );


        listing = Components.listing.properties(SNO,EG_NAME,NAME,BAR_FILE)
                .dataSource(dataProvider)
                .styleNames(MyComponents.CARD, MyComponents.CONTAINER)
                .columnReorderingAllowed(true)
                .resizable(true)
                .fullSize()
                .columnsAutoWidth()
                .withThemeVariants(GridVariant.LUMO_COLUMN_BORDERS)
                .itemDetailsVisibleOnClick(true)
                .itemDetailsComponent(propertyBox -> {
                    PropertyInputForm inputForm = Components.input.form( PARENT_NAME,PARENT_TYPE, DEPLOYED_TIME)
                            .initializer(layout -> {
                                layout.setSizeFull();
                                layout.setResponsiveSteps(MyComponents.getResponsiveStep("480px",2));
                            })
                            .withPostProcessor((property, input) -> input.setReadOnly(true))
                            .build();
                    inputForm.setValue(propertyBox);
                    return inputForm.getComponent();
                })
                .header(NAME,"Messsage Flow Name")
                .build();

        return listing.getComponent();
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
