package org.example.service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimitService {

    private final int MAX_REQUESTS_PER_DAY = 2;
    private ConcurrentHashMap<Long, Integer> userRequests = new ConcurrentHashMap<>();
    public RateLimitService() {
        scheduleDailyReset();
    }

    public boolean canMakeRequest(Long chatId) {



        userRequests.putIfAbsent(chatId, 0);
        int requests = userRequests.get(chatId);

        if (requests >= MAX_REQUESTS_PER_DAY) {
            return false; // Лимит достигнут
        }

        userRequests.put(chatId, requests + 1);
        return true;

    }

    public void scheduleDailyReset() {
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        long delayUntil9PM = calculateDelayUtilNext9PM();

        scheduled.scheduleAtFixedRate(() -> userRequests.clear(),
                delayUntil9PM,
                TimeUnit.DAYS.toMillis(1),
                TimeUnit.MILLISECONDS);

    }

    public long calculateDelayUtilNext9PM() {
        LocalTime now = LocalTime.now();
        LocalTime nextReset = LocalTime.of(21, 33);

        if (now.isAfter(nextReset)) {
            nextReset = nextReset.plusHours(24);

        }
        //return TimeUnit.SECONDS.convert(nextReset.toSecondOfDay() - now.toSecondOfDay(), TimeUnit.SECONDS);
        return Duration.between(now, nextReset).toMillis();
    }
}
