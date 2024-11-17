package org.example.service.quartz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RateLimitService {

   // private final int MAX_REQUESTS_PER_DAY = 2;
    private ConcurrentHashMap<Long, Integer> userLimits = new ConcurrentHashMap<>();
//    public RateLimitService() {
//        scheduleDailyReset();
//    }
//
    public void canMakeRequest(Long chatId) {
        userLimits.putIfAbsent(chatId, 0);
        int requests = userLimits.get(chatId);
        userLimits.put(chatId, requests + 1);
    }
//
//    public void scheduleDailyReset() {
//        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
//        long delayUntil9PM = calculateDelayUtilNext9PM();
//
//        scheduled.scheduleAtFixedRate(() -> userRequests.clear(),
//                delayUntil9PM,
//                TimeUnit.DAYS.toMillis(1),
//                TimeUnit.MILLISECONDS);
//
//    }
//
//    public long calculateDelayUtilNext9PM() {
//        LocalTime now = LocalTime.now();
//        LocalTime nextReset = LocalTime.of(21, 33);
//
//        if (now.isAfter(nextReset)) {
//            nextReset = nextReset.plusHours(24);
//
//        }
//        //return TimeUnit.SECONDS.convert(nextReset.toSecondOfDay() - now.toSecondOfDay(), TimeUnit.SECONDS);
//        return Duration.between(now, nextReset).toMillis();
//    }

    public void resetLimits() {
        userLimits.replaceAll((chatId, limit) -> 0);
        log.info("Лимиты были сброшены" + LocalDate.now());
    }
    public Integer getLimit(Long chatId) {
        return userLimits.getOrDefault(chatId,0);
    }
    public Integer setLimit(Long chatId, Integer limit) {
        return userLimits.put(chatId, limit);
    }
}
