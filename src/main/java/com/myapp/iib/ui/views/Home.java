package com.myapp.iib.ui.views;

import com.myapp.iib.ui.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.myapp.iib.ui.components.FlexBoxLayout;
import com.myapp.iib.ui.layout.size.Horizontal;
import com.myapp.iib.ui.layout.size.Right;
import com.myapp.iib.ui.layout.size.Uniform;
import com.myapp.iib.ui.util.UIUtils;

@PageTitle("Welcome")
@Route(value = "Welcome", layout = MainLayout.class)
public class Home extends ViewFrame {

	public Home() {
		setId("home");
		setViewContent(createContent());
	}

	private Component createContent() {
		Html intro = new Html("<p>An  application to view the components of IIB and</p>");

		Html productivity = new Html("<p>The starter gives you a productivity boost and a head start. " +
				"You get an app shell with a typical hierarchical left-hand menu. The shell, the views and the " +
				"components are all responsive and touch friendly, which makes them great for desktop and mobile" +
				"use. The views are built with Java, which enhances Java developers' productivity by allowing them to" +
				"do all in one language.</p>");

		Html features = new Html("<p>The app comes with multiple list views to edit master-detail data. " +
				"Views can be divided horizontally or vertically to open up the details, and the details can " +
				"also be split into multiple tabs for extra space. The details can also be opened fullscreen " +
				"to maximize the use of space. Additionally there is an opt-in option for opening multiple " +
				"application views in tabs within the app, for quick comparison or navigation between data. " +
				"You enable this feature by setting <code>MainLayout.navigationTabs</code> to true.</p>");

		Anchor documentation = new Anchor("https://vaadin.com/docs/business-app/overview.html", UIUtils.createButton("Read the documentation", VaadinIcon.EXTERNAL_LINK));
		Anchor starter = new Anchor("https://vaadin.com/start/latest/business-app", UIUtils.createButton("Start a new project with Business App", VaadinIcon.EXTERNAL_LINK));

		FlexBoxLayout links = new FlexBoxLayout(documentation, starter);
		links.setFlexWrap(FlexWrap.WRAP);
		links.setSpacing(Right.S);

		FlexBoxLayout content = new FlexBoxLayout(intro, productivity, features, links);
		content.setFlexDirection(FlexDirection.COLUMN);
		content.setMargin(Horizontal.AUTO);
		content.setMaxWidth("840px");
		content.setPadding(Uniform.RESPONSIVE_L);
		return content;
	}

}
