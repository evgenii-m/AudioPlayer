package ru.push.caudioplayer.core.facades.dto;

import ru.push.caudioplayer.utils.DateTimeUtils;

public class NotificationData {
	private static final String NOTIFICATION_MESSAGE_FORMAT = "%s: %s";

	private String timestamp;
	private String text;


	public NotificationData(String text) {
		this.timestamp = DateTimeUtils.getCurrentTimestamp();
		this.text = text;
	}

	public NotificationData(String timestamp, String text) {
		this.timestamp = timestamp;
		this.text = text;
	}

	public String getMessage() {
		return String.format(NOTIFICATION_MESSAGE_FORMAT, timestamp, text);
	}

	@Override
	public String toString() {
		return "NotificationData{" +
				"timestamp='" + timestamp + '\'' +
				", text='" + text + '\'' +
				'}';
	}
}
