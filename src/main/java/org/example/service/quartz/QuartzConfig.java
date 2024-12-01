package org.example.service.quartz;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
    @Bean
    public JobDetail resetLimitJobDetail() {
        return JobBuilder.newJob(ResetLimitJob.class)
                .withIdentity("resetLimitJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger resetLimitJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(resetLimitJobDetail())
                .withIdentity("resetLimitTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(8, 00))
                .build();
    }
}
