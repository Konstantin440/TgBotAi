package org.example.service.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResetLimitJob implements Job {

    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        rateLimitService.resetLimits();
        System.out.println("Лимиты сброшены в 21:00");

    }
}
