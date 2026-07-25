package com.oheers.fish.utils;

import com.oheers.fish.FishUtils;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Range;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;

/**
 * A time code representing an exact minute of a day.
 */
public record TimeCode(@NonNull String code) {

    public static TimeCode now() {
        return fromDateTime(LocalDateTime.now());
    }

    public static TimeCode fromDateTime(LocalDateTime time) {
        return exact(time.getDayOfWeek(), time.getHour(), time.getMinute());
    }

    public static TimeCode exact(@NonNull DayOfWeek day, @Range(from = 0, to = 23) int hour, @Range(from = 0, to = 59) int minute) {
        return new TimeCode(day + "-" + hour + "-" + minute);
    }

    @Override
    public @NonNull String code() {
        return this.code;
    }

    public long toMillis() {
        String[] split = code.split("-");
        if (split.length != 3) {
            return -1;
        }
        DayOfWeek day = FishUtils.getDay(split[0]);
        Integer hour = FishUtils.getInteger(split[1]);
        Integer minute = FishUtils.getInteger(split[2]);
        if (day == null || hour == null || minute == null) {
            return -1;
        }

        LocalDate targetDay = LocalDate.now().with(TemporalAdjusters.nextOrSame(day));
        LocalDateTime exactTime = targetDay.atTime(hour, minute);

        ZonedDateTime zoned = exactTime.atZone(ZoneId.systemDefault());
        return zoned.toInstant().toEpochMilli();
    }

    public Long toMillisNullable() {
        long millis = toMillis();
        if (millis == -1) {
            return null;
        }
        return millis;
    }

    public static Comparator<TimeCode> getComparator() {
        return Comparator.comparingLong(TimeCode::toMillis);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TimeCode other)) {
            return false;
        }
        return other.code.equals(this.code);
    }

    @Override
    public @NonNull String toString() {
        return code;
    }

}
