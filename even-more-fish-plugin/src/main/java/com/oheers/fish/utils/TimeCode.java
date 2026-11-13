package com.oheers.fish.utils;

import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * A time code representing an exact minute of a day.
 */
@NullMarked
public record TimeCode(DayOfWeek day, @Range(from = 0, to = 23) int hour, @Range(from = 0, to = 59) int minute) implements Comparable<TimeCode> {

    public static TimeCode now() {
        return fromDateTime(LocalDateTime.now());
    }

    public static TimeCode fromDateTime(LocalDateTime time) {
        return exact(time.getDayOfWeek(), time.getHour(), time.getMinute());
    }

    public static TimeCode exact(DayOfWeek day, int hour, int minute) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("TimeCode hour must be between 0 and 23.");
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("TimeCode minute must be between 0 and 59.");
        }
        return new TimeCode(day, hour, minute);
    }

    public long toMillis() {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);

        LocalDate targetDay = now.toLocalDate().with(TemporalAdjusters.nextOrSame(day));
        ZonedDateTime target = targetDay.atTime(hour, minute).atZone(zone);
        if (!target.isAfter(now)) {
            target = target.plusWeeks(1);
        }
        return target.toInstant().toEpochMilli();
    }

    @Override
    public String toString() {
        return day + "-" + hour + "-" + minute;
    }

    @Override
    public int compareTo(TimeCode other) {
        int dayResult = Integer.compare(this.day.getValue(), other.day.getValue());
        if (dayResult != 0) {
            return dayResult;
        }
        int hourResult = Integer.compare(this.hour, other.hour);
        if (hourResult != 0) {
            return hourResult;
        }
        return Integer.compare(this.minute, other.minute);
    }

}
