package edu.ohsu.cmp.fhirproxy;

import edu.ohsu.cmp.fhirproxy.service.CacheService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DeleteStaleClientInfoJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(DeleteStaleClientInfoJob.class);

    public static final String JOBDATA_APPLICATIONCONTEXT = "applicationContext";
    public static final String JOBDATA_CACHEKEY = "cacheKey";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String name = jobExecutionContext.getJobDetail().getKey().getName();

        logger.info("running job {} fired at {}", name, jobExecutionContext.getFireTime());

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        ApplicationContext ctx = (ApplicationContext) jobDataMap.get(JOBDATA_APPLICATIONCONTEXT);
        String cacheKey = jobDataMap.getString(JOBDATA_CACHEKEY);

        CacheService cacheService = ctx.getBean(CacheService.class);
        if (cacheService.delete(cacheKey)) {
            logger.info("client info deleted for job: " + name);
        } else {
            logger.warn("client info not found for job: " + name + ", nothing to delete.  ???");
        }
    }
}
