package com.work.assistant.common.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author aidan.liu
 */
public class DateUtils {

  public static final String DATE_FORMAT_TIME_01 = "yyyy-MM-dd HH:mm:ss";

  public static final String DATE_FORMAT_TIME_02 = "yyyy-MM-dd HH:mm";

  public static final String DATE_FORMAT_TIME_03 = "yyyy年MM月dd日 HH:mm:ss";

  public static final String DATE_FORMAT_01 = "yyyy-MM-dd";

  public static final String DATE_FORMAT_02 = "yyyy年MM月dd日";

  public static final String DATE_FORMAT_03 = "HH:mm";

  public static final String DATE_MONTH = "yyyy-MM";

  public static final String DATE_YEAR = "yyyy";


  public static LocalDateTime stringToLocalDateTime(String str, String formatStr) {
    return StringUtils.isBlank(str) ? null : LocalDateTime.parse(str, DateTimeFormatter.ofPattern(formatStr));
  }

  public static String localDateTimeToString(LocalDateTime localDateTime, String formatStr) {
    return null == localDateTime ? null : DateTimeFormatter.ofPattern(formatStr).format(localDateTime);
  }

  public static String localDateToString(LocalDate localDate, String formatStr) {
    return null == localDate ? null : DateTimeFormatter.ofPattern(formatStr).format(localDate);
  }

  public static LocalDate stringToLocalDate(String str, String formatStr) {

    if (StringUtils.isBlank(str)) {
      return null;
    }

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatStr);
    return LocalDate.parse(str, dateTimeFormatter);
  }

  public static String convertLocalDateTimeFormat(String str, String oldFormatStr, String newFormatStr) {

    LocalDateTime localDateTime = stringToLocalDateTime(str, oldFormatStr);
    if (null != localDateTime) {
      return localDateTimeToString(localDateTime, newFormatStr);
    }
    return "";
  }

  public static long calculateWorkDeadlineMinutes(LocalDateTime localDateTime, String startWorkTime) {

    String workDeadlineTimeStr = localDateToString(localDateTime.toLocalDate(), DATE_FORMAT_01) + " " + startWorkTime;
    LocalDateTime workDeadlineTime = stringToLocalDateTime(workDeadlineTimeStr, DATE_FORMAT_TIME_02);
    if (null != workDeadlineTime) {
      Duration duration = Duration.between(localDateTime, workDeadlineTime);
      return duration.toMinutes();
    }
    return 0L;
  }

  public static long calculateOffWorkMinutes(LocalDateTime localDateTime, String endWorkTime) {

    String wordTimeStr = localDateToString(localDateTime.toLocalDate(), DATE_FORMAT_01) + " " + endWorkTime;
    LocalDateTime offWorkTime = stringToLocalDateTime(wordTimeStr, DATE_FORMAT_TIME_02);
    if (null != offWorkTime) {
      Duration duration = Duration.between(offWorkTime, localDateTime);
      return duration.toMinutes();
    }
    return 0L;
  }
}
