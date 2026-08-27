package com.efe.traderecon.flow.ingestion;

import com.efe.traderecon.api.dto.ReconciliationJobResponse;
import com.efe.traderecon.ikasan.model.IkasanProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JobRegistrationResponseProducer implements IkasanProducer<ReconciliationJobResponse> {
    private static final Logger log = LoggerFactory.getLogger(JobRegistrationResponseProducer.class);

    @Override
    public String getName() {
        return "job-registration-response-producer";
    }

    @Override
    public void produce(ReconciliationJobResponse response) {
        log.info("Produced HTTP 201 Response payload for Job [{}] with status [{}]",
                response.getJobId(), response.getStatus());
    }
}
