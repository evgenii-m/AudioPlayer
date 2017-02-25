package ru.push.caudioplayer.utils;

import org.springframework.stereotype.Component;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/13/17
 */
@Component
public class TrackTimeLabelBuilder {
  private static final long ONE_SECOND_MS = 1000;
  private static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;
  private static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;

  private static final String DEFAULT_TIME_STRING_FORMAT = "%d:%02d:%02d";
  private static final String DEFAULT_TIME_SHORT_STRING_FORMAT = "%d:%02d";
  private static final String DEFAULT_LABEL_FORMAT = DEFAULT_TIME_STRING_FORMAT + " / "
      + DEFAULT_TIME_STRING_FORMAT;
  private static final String DEFAULT_LABEL_SHORT_FORMAT = DEFAULT_TIME_SHORT_STRING_FORMAT + " / "
      + DEFAULT_TIME_SHORT_STRING_FORMAT;

  public String buildTimeString(long timeMs) {
    long hours = timeMs / ONE_HOUR_MS;
    long minutes = (timeMs % ONE_HOUR_MS) / ONE_MINUTE_MS;
    long seconds = (timeMs % ONE_MINUTE_MS) / ONE_SECOND_MS;

    return (hours > 0) ?
        String.format(DEFAULT_TIME_STRING_FORMAT, hours, minutes, seconds) :
        String.format(DEFAULT_TIME_SHORT_STRING_FORMAT, minutes, seconds);
  }

  public String buildTimeLabel(long currentTimeMs, long endTimeMs) {
    long ctHours = currentTimeMs / ONE_HOUR_MS;
    long ctMinutes = (currentTimeMs % ONE_HOUR_MS) / ONE_MINUTE_MS;
    long ctSeconds = (currentTimeMs % ONE_MINUTE_MS) / ONE_SECOND_MS;
    long etHours = endTimeMs / ONE_HOUR_MS;
    long etMinutes = (endTimeMs % ONE_HOUR_MS) / ONE_MINUTE_MS;
    long etSeconds = (endTimeMs % ONE_MINUTE_MS) / ONE_SECOND_MS;

    return (etHours > 0) ?
        String.format(DEFAULT_LABEL_FORMAT, ctHours, ctMinutes, ctSeconds, etHours, etMinutes, etSeconds) :
        String.format(DEFAULT_LABEL_SHORT_FORMAT, ctMinutes, ctSeconds, etMinutes, etSeconds);
  }
}
