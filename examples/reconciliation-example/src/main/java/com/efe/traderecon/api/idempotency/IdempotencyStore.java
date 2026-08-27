package com.efe.traderecon.api.idempotency;

import com.efe.traderecon.api.dto.JobSubmissionResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyStore {

    private final ConcurrentHashMap<String, JobSubmissionResponse> store = new ConcurrentHashMap<>();

    public Optional<JobSubmissionResponse> get(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(idempotencyKey.trim()));
    }

    public void put(String idempotencyKey, JobSubmissionResponse response) {
        if (idempotencyKey != null && !idempotencyKey.isBlank() && response != null) {
            store.put(idempotencyKey.trim(), response);
        }
    }

    public boolean exists(String idempotencyKey) {
        return idempotencyKey != null && store.containsKey(idempotencyKey.trim());
    }

    public void clear() {
        store.clear();
    }
}
