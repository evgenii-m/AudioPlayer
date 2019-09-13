package ru.push.caudioplayer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {

	private static final String DEFAULT_TIMESTAMP_FROMAT = "dd/MM/yyyy HH:mm:ss";

	public static String getCurrentTimestamp() {
		return new SimpleDateFormat(DEFAULT_TIMESTAMP_FROMAT).format(new Date());
	}

	public static String getCurrentTimestamp(String formatPattern) {
		return new SimpleDateFormat(formatPattern).format(new Date());
	}
}
