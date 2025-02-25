package com.myapp.iib.ui.views;

import com.myapp.iib.backend.Payment;
import com.myapp.iib.ui.MainLayout;
import com.myapp.iib.ui.components.FlexBoxLayout;
import com.myapp.iib.ui.components.ListItem;
import com.myapp.iib.ui.layout.size.*;
import com.myapp.iib.ui.util.*;
import com.myapp.iib.ui.util.css.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@CssImport("./styles/views/statistics.css")
@PageTitle("Statistics")
@Route(value = "statistics", layout = MainLayout.class)
public class Statistics extends ViewFrame {

    private static final String CLASS_NAME = "statistics";
    public static final String MAX_WIDTH = "1024px";

    public Statistics() {
        setViewContent(createContent());
    }

    private Component createContent() {
        Component payments = createPayments();
        Component transactions = createTransactions();
        Component docs = createDocs();

        FlexBoxLayout content = new FlexBoxLayout(payments, transactions, docs);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setFlexDirection(FlexDirection.COLUMN);
        return content;
    }

    private Component createPayments() {
        FlexBoxLayout payments = new FlexBoxLayout(
                createHeader(VaadinIcon.CREDIT_CARD, "Payments"),
                createPaymentsCharts());
        payments.setBoxSizing(BoxSizing.BORDER_BOX);
        payments.setDisplay(Display.BLOCK);
        payments.setMargin(Top.L);
        payments.setMaxWidth(MAX_WIDTH);
        payments.setPadding(Horizontal.RESPONSIVE_L);
        payments.setWidthFull();
        return payments;
    }

    private FlexBoxLayout createHeader(VaadinIcon icon, String title) {
        FlexBoxLayout header = new FlexBoxLayout(
                UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, icon),
                UIUtils.createH3Label(title));
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setMargin(Bottom.L, Horizontal.RESPONSIVE_L);
        header.setSpacing(Right.L);
        return header;
    }

    private Component createPaymentsCharts() {
        Row charts = new Row();
        UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, charts);
        UIUtils.setBorderRadius(BorderRadius.S, charts);
        UIUtils.setShadow(Shadow.XS, charts);

        for (Payment.Status status : Payment.Status.values()) {
            charts.add(createPaymentChart(status));
        }

        return charts;
    }

    private Component createPaymentChart(Payment.Status status) {
        int value;

        switch (status) {
            case PENDING:
                value = 24;
                break;

            case SUBMITTED:
                value = 40;
                break;

            case CONFIRMED:
                value = 32;
                break;

            default:
                value = 4;
                break;
        }

        FlexBoxLayout textContainer = new FlexBoxLayout(
                UIUtils.createH2Label(Integer.toString(value)),
                UIUtils.createLabel(FontSize.S, "%"));
        textContainer.setAlignItems(FlexComponent.Alignment.BASELINE);
        textContainer.setPosition(Position.ABSOLUTE);
        textContainer.setSpacing(Right.XS);

		/*Chart chart = createProgressChart(status, value);

		FlexBoxLayout chartContainer = new FlexBoxLayout(chart, textContainer);
		chartContainer.setAlignItems(FlexComponent.Alignment.CENTER);
		chartContainer
				.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		chartContainer.setPosition(Position.RELATIVE);
		chartContainer.setHeight("120px");
		chartContainer.setWidth("120px");*/

        FlexBoxLayout paymentChart = new FlexBoxLayout(
                new Label(status.getName()));
        paymentChart.addClassName(CLASS_NAME + "__payment-chart");
        paymentChart.setAlignItems(FlexComponent.Alignment.CENTER);
        paymentChart.setFlexDirection(FlexDirection.COLUMN);
        paymentChart.setPadding(Bottom.S, Top.M);
        return paymentChart;
    }


    private Component createTransactions() {
        FlexBoxLayout transactions = new FlexBoxLayout(
                createHeader(VaadinIcon.MONEY_EXCHANGE, "Transactions"));
        transactions.setBoxSizing(BoxSizing.BORDER_BOX);
        transactions.setDisplay(Display.BLOCK);
        transactions.setMargin(Top.XL);
        transactions.setMaxWidth(MAX_WIDTH);
        transactions.setPadding(Horizontal.RESPONSIVE_L);
        transactions.setWidthFull();
        return transactions;
    }


    private Component createDocs() {
        Component reports = createReports();
        Component logs = createLogs();

        Row docs = new Row(reports, logs);
        docs.addClassName(LumoStyles.Margin.Top.XL);
        UIUtils.setMaxWidth(MAX_WIDTH, docs);
        docs.setWidthFull();

        return docs;
    }

    private Component createReports() {
        FlexBoxLayout header = createHeader(VaadinIcon.RECORDS, "Reports");

        Tabs tabs = new Tabs();
        for (String label : new String[]{"All", "Archive", "Workflows",
                "Support"}) {
            tabs.add(new Tab(label));
        }

        Div items = new Div(
                new ListItem(
                        UIUtils.createIcon(IconSize.M, TextColor.TERTIARY,
                                VaadinIcon.CHART),
                        "Weekly Report", "Generated Oct 5, 2018",
                        createInfoButton()),
                new ListItem(
                        UIUtils.createIcon(IconSize.M, TextColor.TERTIARY,
                                VaadinIcon.SITEMAP),
                        "Payment Workflows", "Last modified Oct 24, 2018",
                        createInfoButton()));
        items.addClassNames(LumoStyles.Padding.Vertical.S);

        Div card = new Div(tabs, items);
        UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, card);
        UIUtils.setBorderRadius(BorderRadius.S, card);
        UIUtils.setShadow(Shadow.XS, card);

        FlexBoxLayout reports = new FlexBoxLayout(header, card);
        reports.addClassName(CLASS_NAME + "__reports");
        reports.setFlexDirection(FlexDirection.COLUMN);
        reports.setPadding(Bottom.XL, Left.RESPONSIVE_L);
        return reports;
    }

    private Component createLogs() {
        FlexBoxLayout header = createHeader(VaadinIcon.EDIT, "Logs");

        Tabs tabs = new Tabs();
        for (String label : new String[]{"All", "Transfer", "Security",
                "Change"}) {
            tabs.add(new Tab(label));
        }

        Div items = new Div(
                new ListItem(
                        UIUtils.createIcon(IconSize.M, TextColor.TERTIARY,
                                VaadinIcon.EXCHANGE),
                        "Transfers (October)", "Generated Oct 31, 2018",
                        createInfoButton()),
                new ListItem(
                        UIUtils.createIcon(IconSize.M, TextColor.TERTIARY,
                                VaadinIcon.SHIELD),
                        "Security Log", "Updated 16:31 CET",
                        createInfoButton()));
        items.addClassNames(LumoStyles.Padding.Vertical.S);

        Div card = new Div(tabs, items);
        UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, card);
        UIUtils.setBorderRadius(BorderRadius.S, card);
        UIUtils.setShadow(Shadow.XS, card);

        FlexBoxLayout logs = new FlexBoxLayout(header, card);
        logs.addClassName(CLASS_NAME + "__logs");
        logs.setFlexDirection(FlexDirection.COLUMN);
        logs.setPadding(Bottom.XL, Right.RESPONSIVE_L);
        return logs;
    }

    private Button createInfoButton() {
        Button infoButton = UIUtils.createSmallButton(VaadinIcon.INFO);
        infoButton.addClickListener(
                e -> UIUtils.showNotification("Not implemented yet."));
        return infoButton;
    }
}
