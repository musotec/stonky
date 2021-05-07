package tech.muso.stonky.repository.service.impl

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal data class DayOfEpoch(val epochTimeSeconds: Long) {
    // trade day start is set to 9:30. UTC is used to reduce object create time. (is GMT-4)
    private val tradeDayStart = LocalDateTime.ofEpochSecond(epochTimeSeconds, 0, ZoneOffset.UTC)
        .withHour(13)
        .withMinute(30)
        .withSecond(0)

    // FIXME: (intended) does not take into account the underlying and whether or not it uses standard market hours.
    val marketStartTimestamp: Long = tradeDayStart.toEpochSecond(ZoneOffset.UTC)
    val marketEndTimestamp: Long = marketStartTimestamp + (7 * 60 * 60) + (30 * 60) // add 7.5hrs for trade day end

    val startLocalDate = tradeDayStart.withHour(4).withMinute(0).withSecond(0)  // GMT-4 (NYSE)
    val endLocalDate = startLocalDate.plusDays(1)   // until next day start

    val offsetDayEpochSeconds = startLocalDate.toEpochSecond(ZoneOffset.UTC)
}

internal inline fun LocalDateTime.toZonedDate() = ZonedDateTime.of(this, ZoneOffset.UTC)
internal inline fun Long.toBasicIsoDate() = LocalDateTime.ofEpochSecond(this, 0, ZoneOffset.UTC)
    .format(DateTimeFormatter.BASIC_ISO_DATE)