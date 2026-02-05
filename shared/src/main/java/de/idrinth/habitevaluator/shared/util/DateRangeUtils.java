package de.idrinth.habitevaluator.shared.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for common date range operations used across PDF export and data aggregation.
 */
public final class DateRangeUtils {

    private DateRangeUtils() {
        // Utility class
    }

    /**
     * Generates a list of all dates in the range [from, to] inclusive.
     *
     * @param from the start date (inclusive)
     * @param to   the end date (inclusive)
     * @return list of LocalDate objects in the range
     */
    public static List<LocalDate> getDateRange(LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            dates.add(d);
        }
        return dates;
    }

    /**
     * Formats dates in a range using the specified formatter.
     *
     * @param from      the start date (inclusive)
     * @param to        the end date (inclusive)
     * @param formatter the DateTimeFormatter to use
     * @return list of formatted date strings
     */
    public static List<String> formatDateLabels(LocalDate from, LocalDate to, DateTimeFormatter formatter) {
        List<String> labels = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            labels.add(d.format(formatter));
        }
        return labels;
    }

    /**
     * Filters a list of items to only include those within the specified date range.
     *
     * @param items         the list of items to filter
     * @param dateExtractor function to extract the date from each item
     * @param from          the start date (inclusive)
     * @param to            the end date (inclusive)
     * @param <T>           the type of items in the list
     * @return filtered list containing only items within the date range
     */
    public static <T> List<T> filterByDateRange(List<T> items,
                                                 Function<T, LocalDate> dateExtractor,
                                                 LocalDate from,
                                                 LocalDate to) {
        return items.stream()
                .filter(item -> {
                    LocalDate date = dateExtractor.apply(item);
                    return date != null && !date.isBefore(from) && !date.isAfter(to);
                })
                .collect(Collectors.toList());
    }

    /**
     * Checks if a date falls within the specified range (inclusive).
     *
     * @param date the date to check
     * @param from the start date (inclusive)
     * @param to   the end date (inclusive)
     * @return true if the date is within the range
     */
    public static boolean isInRange(LocalDate date, LocalDate from, LocalDate to) {
        return date != null && !date.isBefore(from) && !date.isAfter(to);
    }

    /**
     * Calculates the number of days in the range [from, to] inclusive.
     *
     * @param from the start date (inclusive)
     * @param to   the end date (inclusive)
     * @return number of days in the range
     */
    public static long daysBetweenInclusive(LocalDate from, LocalDate to) {
        return java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
    }

    /**
     * Returns the day index (0-based) of a date within a range.
     *
     * @param date      the date to find the index for
     * @param startDate the start of the range
     * @return the 0-based index, or -1 if the date is before the start
     */
    public static int getDayIndex(LocalDate date, LocalDate startDate) {
        if (date.isBefore(startDate)) {
            return -1;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, date);
    }
}
