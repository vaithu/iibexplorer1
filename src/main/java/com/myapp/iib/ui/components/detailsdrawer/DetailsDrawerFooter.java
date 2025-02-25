package com.myapp.iib.ui.components.detailsdrawer;

import com.myapp.iib.ui.components.FlexBoxLayout;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.shared.Registration;
import com.myapp.iib.ui.layout.size.Horizontal;
import com.myapp.iib.ui.layout.size.Right;
import com.myapp.iib.ui.layout.size.Vertical;
import com.myapp.iib.ui.util.LumoStyles;
import com.myapp.iib.ui.util.UIUtils;

public class DetailsDrawerFooter extends FlexBoxLayout {

	private final Button save;
	private final Button cancel;

	public DetailsDrawerFooter() {
		setBackgroundColor(LumoStyles.Color.Contrast._5);
		setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
		setSpacing(Right.S);
		setWidthFull();

		save = UIUtils.createPrimaryButton("Save");
		cancel = UIUtils.createTertiaryButton("Cancel");
		add(save, cancel);
	}

	public Registration addSaveListener(
			ComponentEventListener<ClickEvent<Button>> listener) {
		return save.addClickListener(listener);
	}

	public Registration addCancelListener(
			ComponentEventListener<ClickEvent<Button>> listener) {
		return cancel.addClickListener(listener);
	}

}
