package com.myapp.iib.ui.components;

import com.vaadin.flow.component.html.Span;
import com.myapp.iib.ui.util.UIUtils;
import com.myapp.iib.ui.util.css.lumo.BadgeColor;
import com.myapp.iib.ui.util.css.lumo.BadgeShape;
import com.myapp.iib.ui.util.css.lumo.BadgeSize;

import java.util.StringJoiner;

import static com.myapp.iib.ui.util.css.lumo.BadgeShape.PILL;

public class Badge extends Span {

	public Badge(String text) {
		this(text, BadgeColor.NORMAL);
	}

	public Badge(String text, BadgeColor color) {
		super(text);
		UIUtils.setTheme(color.getThemeName(), this);
	}

	public Badge(String text, BadgeColor color, BadgeSize size, BadgeShape shape) {
		super(text);
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add(color.getThemeName());
		if (shape.equals(PILL)) {
			joiner.add(shape.getThemeName());
		}
		if (size.equals(BadgeSize.S)) {
			joiner.add(size.getThemeName());
		}
		UIUtils.setTheme(joiner.toString(), this);
	}

}
