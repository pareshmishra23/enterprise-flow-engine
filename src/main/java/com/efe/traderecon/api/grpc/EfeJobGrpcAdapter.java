package com.efe.traderecon.api.grpc;

import com.efe.traderecon.api.controller.JobController;
import com.efe.traderecon.api.dto.JobStatusResponse;
import com.efe.traderecon.api.dto.JobSubmissionRequest;
import com.efe.traderecon.api.dto.JobSubmissionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * EFE gRPC Job Service Adapter.
 *
 * Implements the gRPC API boundary contract:
 * - SubmitJob(JobRequest) -> JobResponse
 * - GetJob(JobIdRequest) -> JobResponse
 *
 * It acts as an API adapter and delegates directly to the same EFE JobController / Ikasan pipeline.
 */
@Component
public class EfeJobGrpcAdapter {

    private final JobController jobController;

    public EfeJobGrpcAdapter(JobController jobController) {
        this.jobController = jobController;
    }

    public static class JobRequestGrpc {
        private String jobType;
        private String businessDate;
        private Map<String, Object> parameters;

        public JobRequestGrpc() {}
        public JobRequestGrpc(String jobType, String businessDate, Map<String, Object> parameters) {
            this.jobType = jobType;
            this.businessDate = businessDate;
            this.parameters = parameters;
        }

        public String getJobType() { return jobType; }
        public void setJobType(String jobType) { this.jobType = jobType; }

        public String getBusinessDate() { return businessDate; }
        public void setBusinessDate(String businessDate) { this.businessDate = businessDate; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    public static class JobIdRequestGrpc {
        private String jobId;
        public JobIdRequestGrpc() {}
        public JobIdRequestGrpc(String jobId) { this.jobId = jobId; }
        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
    }

    public static class JobResponseGrpc {
        private String jobId;
        private String jobType;
        private String status;
        private String businessDate;
        private String createdAt;

        public JobResponseGrpc() {}
        public JobResponseGrpc(String jobId, String jobType, String status, String businessDate, String createdAt) {
            this.jobId = jobId;
            this.jobType = jobType;
            this.status = status;
            this.businessDate = businessDate;
            this.createdAt = createdAt;
        }

        public static JobResponseGrpc from(JobSubmissionResponse resp) {
            if (resp == null) return null;
            return new JobResponseGrpc(
                    resp.getJobId(),
                    resp.getJobType(),
                    resp.getStatus(),
                    null,
                    resp.getAcceptedAt() != null ? resp.getAcceptedAt().toString() : null
            );
        }

        public static JobResponseGrpc from(JobStatusResponse resp) {
            if (resp == null) return null;
            return new JobResponseGrpc(
                    resp.getJobId(),
                    resp.getJobType() != null ? resp.getJobType().name() : null,
                    resp.getStatus() != null ? resp.getStatus().name() : null,
                    resp.getBusinessDate() != null ? resp.getBusinessDate().toString() : null,
                    resp.getCreatedAt() != null ? resp.getCreatedAt().toString() : null
            );
        }

        public String getJobId() { return jobId; }
        public String getJobType() { return jobType; }
        public String getStatus() { return status; }
        public String getBusinessDate() { return businessDate; }
        public String getCreatedAt() { return createdAt; }
    }

    public JobResponseGrpc submitJob(JobRequestGrpc request, String correlationId, String idempotencyKey) {
        JobSubmissionRequest req = new JobSubmissionRequest();
        req.setJobType(request.getJobType());
        if (request.getBusinessDate() != null) {
            req.setBusinessDate(LocalDate.parse(request.getBusinessDate()));
        }
        req.setPayload(request.getParameters());

        ResponseEntity<JobSubmissionResponse> entity = jobController.submitJob(req, idempotencyKey, correlationId, null);
        return JobResponseGrpc.from(entity.getBody());
    }

    public JobResponseGrpc getJob(JobIdRequestGrpc request) {
        ResponseEntity<JobStatusResponse> entity = jobController.getJobStatus(request.getJobId());
        return JobResponseGrpc.from(entity.getBody());
    }
}
