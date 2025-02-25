package com.myapp.iib.ui.components.detailsdrawer;

import com.myapp.iib.ui.components.FlexBoxLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport("./styles/components/details-drawer.css")
public class DetailsDrawer extends FlexBoxLayout {

	private final String CLASS_NAME = "details-drawer";

	private final FlexBoxLayout header;
	private final FlexBoxLayout content;
	private final FlexBoxLayout footer;

	public enum Position {
		BOTTOM, RIGHT
	}

	public DetailsDrawer(Position position, Component... components) {
		setClassName(CLASS_NAME);
		setPosition(position);

		header = new FlexBoxLayout();
		header.setClassName(CLASS_NAME + "__header");

		content = new FlexBoxLayout(components);
		content.setClassName(CLASS_NAME + "__content");
		content.setFlexDirection(FlexDirection.COLUMN);

		footer = new FlexBoxLayout();
		footer.setClassName(CLASS_NAME + "__footer");

		add(header, content, footer);
	}

	public void setHeader(Component... components) {
		this.header.removeAll();
		this.header.add(components);
	}

	public FlexBoxLayout getHeader() {
		return this.header;
	}

	public void setContent(Component... components) {
		this.content.removeAll();
		this.content.add(components);
	}

	public void setFooter(Component... components) {
		this.footer.removeAll();
		this.footer.add(components);
	}

	public void setPosition(Position position) {
		getElement().setAttribute("position", position.name().toLowerCase());
	}

	public void hide() {
		getElement().setAttribute("open", false);
	}

	public void show() {
		getElement().setAttribute("open", true);
	}
}
