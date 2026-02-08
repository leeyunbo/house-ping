package com.yunbok.houseping.support.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@UtilityClass
public class DateParsingUtil {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YYYY_MM_DD_DOT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter YYYY_MM_DD_DASH = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public static LocalDate parse(String dateStr) {
        if (isBlankOrDash(dateStr)) {
            return null;
        }

        String cleaned = dateStr.trim();

        return tryParseDateTime(cleaned, YYYYMMDDHHMM)
                .or(() -> tryParse(cleaned, YYYY_MM_DD_DOT))
                .or(() -> tryParse(cleaned, YYYYMMDD))
                .or(() -> tryParse(cleaned, YYYY_MM_DD_DASH))
                .orElse(null);
    }

    public static boolean isBlankOrDash(String str) {
        return str == null || str.isBlank() || "-".equals(str.trim());
    }

    private static Optional<LocalDate> tryParse(String dateStr, DateTimeFormatter formatter) {
        try {
            return Optional.of(LocalDate.parse(dateStr, formatter));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<LocalDate> tryParseDateTime(String dateStr, DateTimeFormatter formatter) {
        try {
            if (dateStr.length() == 12 && dateStr.matches("\\d{12}")) {
                LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
                return Optional.of(dateTime.toLocalDate());
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
