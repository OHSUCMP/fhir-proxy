package edu.ohsu.cmp.fhirproxy.service;

import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.Payload;
import edu.ohsu.cmp.fhirproxy.DeleteStaleClientInfoJob;
import edu.ohsu.cmp.fhirproxy.exception.ClientInfoNotFoundException;
import edu.ohsu.cmp.fhirproxy.model.ClientInfo;
import edu.ohsu.cmp.fhirproxy.util.CryptoUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.*;

@Service
public class CacheService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, ClientInfo> map;
    private final String salt;

    @Autowired
    private ApplicationContext ctx;

    public CacheService() {
        this.map = new HashMap<>();
        this.salt = Base64.getEncoder().encodeToString(CryptoUtil.randomBytes(64));;
    }

    public boolean exists(String key) {
        return map.containsKey(key);
    }

    public String put(ClientInfo clientInfo) {
        String key = Base64.getEncoder().encodeToString(DigestUtils.sha512(clientInfo.toString() + salt));

        if ( ! map.containsKey(key) ) {
            map.put(key, clientInfo);

            setupDeleteStaleClientInfoJob(clientInfo, key);
        }

        return key;
    }

    public ClientInfo get(String key) throws ClientInfoNotFoundException {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            throw new ClientInfoNotFoundException("client info not found for key=" + key);
        }
    }

    public boolean delete(String key) {
        return map.remove(key) != null;
    }

////////////////////////////////////////////////////////////////////////////////////
/// private stuff
///

    private void setupDeleteStaleClientInfoJob(ClientInfo clientInfo, String cacheKey) {
        Scheduler scheduler = ctx.getBean(Scheduler.class);
        Date shutdownTimestamp = deriveExpirationTimestamp(clientInfo.getBearerToken());

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(DeleteStaleClientInfoJob.JOBDATA_APPLICATIONCONTEXT, ctx);
        jobDataMap.put(DeleteStaleClientInfoJob.JOBDATA_CACHEKEY, cacheKey);

        String jobId = Base64.getEncoder().encodeToString(DigestUtils.sha1(cacheKey + salt));

        JobKey jobKey = new JobKey("deleteStaleClientInfoJob-" + jobId);
        JobDetail job = JobBuilder.newJob(DeleteStaleClientInfoJob.class)
                .storeDurably()
                .withIdentity(jobKey)
                .withDescription("Auto-delete stale client info for clientId=" + clientInfo.getClientId() + " at " + shutdownTimestamp)
                .usingJobData(jobDataMap)
                .build();

        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(DeleteStaleClientInfoJob.class);
        jobDetailFactory.setDescription("Invoke Shutdown User Workspace Job service...");
        jobDetailFactory.setDurability(true);

        Trigger trigger = TriggerBuilder.newTrigger().forJob(job)
                .withIdentity("deleteStaleClientInfoTrigger-" + jobId)
                .withDescription("Delete stale client info trigger for clientId=" + clientInfo.getClientId())
                .startAt(shutdownTimestamp)
                .build();

        try {
            if ( ! scheduler.isStarted() ) {
                scheduler.start();
            }

            if (scheduler.checkExists(jobKey)) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                logger.warn("found pre-existing job for jobId=" + jobId +
                        ", but this should have been cleared earlier, it shouldn't have gotten this far.  ???");
                logger.info("deleting job: " + jobDetail.getDescription());
                scheduler.deleteJob(jobKey);
            }

            logger.info("scheduling job: " + job.getKey().getName() + " - " + job.getDescription());
            scheduler.scheduleJob(job, trigger);

        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    private Date deriveExpirationTimestamp(String bearerToken) {
        try {
            String[] parts = bearerToken.split("\\.");
            String payloadJSON = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JWTParser parser = new JWTParser();
            Payload payload = parser.parsePayload(payloadJSON);
            return payload.getExpiresAt();

        } catch (Exception e) {
            logger.warn("couldn't parse bearerToken - will use default expiration timestamp of 1 day from now.");
            logger.debug("caught " + e.getClass().getName() + " parsing bearerToken - " + e.getMessage(), e);

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, 1);
            return cal.getTime();
        }
    }
}
