package ru.push.caudioplayer.core.playlist.model;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 3/1/17
 */
public enum MediaSourceType {
  FILE, HTTP_STREAM, DEEZER_MEDIA;

	public String value() {
		return name();
	}

	public static MediaSourceType fromValue(String v) {
		return valueOf(v);
	}
}
