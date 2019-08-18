package ru.push.caudioplayer.core.deezer;

public enum DeezerApiParam {
	ACCESS_TOKEN("access_token");

	String value;

	DeezerApiParam(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}


	@Override
	public String toString() {
		return "DeezerApiParam{" +
				"value='" + value + '\'' +
				'}';
	}
}
