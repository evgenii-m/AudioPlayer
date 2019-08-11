package ru.push.caudioplayer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtils {

	public static String readStreamAsOneString(InputStream stream) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
		StringBuffer result = new StringBuffer();
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}

}
