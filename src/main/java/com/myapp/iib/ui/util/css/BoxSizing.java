package com.myapp.iib.ui.util.css;

public enum BoxSizing {

	BORDER_BOX("border-box"), CONTENT_BOX("content-box");

	private final String value;

	BoxSizing(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
