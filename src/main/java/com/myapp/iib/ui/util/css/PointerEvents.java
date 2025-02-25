package com.myapp.iib.ui.util.css;

public enum PointerEvents {

	AUTO("auto"), NONE("none");

	private final String value;

	PointerEvents(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
