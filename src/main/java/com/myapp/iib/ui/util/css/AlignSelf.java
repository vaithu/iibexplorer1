package com.myapp.iib.ui.util.css;

public enum AlignSelf {

	BASLINE("baseline"), CENTER("center"), END("end"), START("start"), STRETCH(
			"stretch");

	private final String value;

	AlignSelf(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
