package com.myapp.iib.ui.util.css;

public enum Overflow {

	AUTO("auto"), HIDDEN("hidden"), SCROLL("scroll"), VISIBLE("visible");

	private final String value;

	Overflow(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
