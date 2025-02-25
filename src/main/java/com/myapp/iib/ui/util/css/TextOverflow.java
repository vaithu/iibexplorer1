package com.myapp.iib.ui.util.css;

public enum TextOverflow {

	CLIP("clip"),
	ELLIPSIS("ellipsis");

	private final String value;

	TextOverflow(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
