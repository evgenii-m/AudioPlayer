package ru.push.caudioplayer.core.facades.dto;

import java.util.Arrays;

public enum ImageSize {
	SMALL("small"),
	MEDIUM("medium"),
	LARGE("large"),
	EXTRALARGE("extralarge"),
	UNKNOWN("")
	;

	private String value;

	ImageSize(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ImageSize fromValue(String value) {
		return (value != null) ?
				Arrays.stream(values())
						.filter(v -> v.getValue().equals(value))
						.findFirst().orElse(UNKNOWN) :
				null;
	}
}
