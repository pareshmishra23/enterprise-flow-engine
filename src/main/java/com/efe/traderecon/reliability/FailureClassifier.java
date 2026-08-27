package com.efe.traderecon.reliability;

import java.util.concurrent.RejectedExecutionException;

@FunctionalInterface
public interface FailureClassifier {
    boolean isRetryable(Throwable failure);

    static FailureClassifier defaultClassifier() {
        return failure -> {
            Throwable current = failure;
            while (current != null) {
                if (current instanceof RejectedExecutionException) {
                    return true;
                }
                if (current instanceof IllegalArgumentException
                        || current instanceof NullPointerException
                        || current instanceof IllegalStateException) {
                    return false;
                }
                current = current.getCause();
            }
            return true;
        };
    }
}
