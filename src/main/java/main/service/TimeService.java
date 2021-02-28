package main.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeService {
    public static LocalDateTime getLocalDateTime(long seconds) {
        return Instant.ofEpochSecond(seconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static long getTimestamp(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
